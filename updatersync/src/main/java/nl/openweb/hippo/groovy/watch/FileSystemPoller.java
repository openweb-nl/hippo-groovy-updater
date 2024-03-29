/*
 * Copied from webfiles service module
 *
 * Copyright 2014-2015 Hippo B.V. (http://www.onehippo.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.openweb.hippo.groovy.watch;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.openweb.hippo.groovy.GroovyFileException;

/**
 * File system observer that polls for changes. After a set of observed changes has been reported, the polling thread
 * sleeps a fixed amount of time.
 */
public class FileSystemPoller implements FileSystemObserver, FileAlterationListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileSystemPoller.class);

    private final FileFilter fileNameFilter;
    private final Map<Path, FileSystemListener> listeners;
    private final FileAlterationMonitor monitor;
    private FileSystemListener listenerForCurrentChange;

    public FileSystemPoller(final FileFilter fileNameFilter, final long pollingDelayMillis) {
        this.fileNameFilter = fileNameFilter;
        listeners = new HashMap<>();
        monitor = new FileAlterationMonitor(pollingDelayMillis);
        try {
            monitor.start();
        } catch (Exception e) {
            throw new GroovyFileException("Failed to start filemonitor", e);
        }
    }

    @Override
    public synchronized void registerDirectory(final Path directory, final FileSystemListener listener) throws IOException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Registering {}", directory);
        }

        final FileAlterationObserver observer = new FileAlterationObserver(directory.toFile(), this.fileNameFilter);
        try {
            // Force the registered directory to be read into memory before we start observing changes,
            // otherwise the first run will generate a 'create' event for each file and directory.
            observer.initialize();
        } catch (Exception e) {
            throw new IOException("Failed to initialize observer for " + directory, e);
        }
        observer.addListener(this);
        listeners.put(directory, listener);
        monitor.addObserver(observer);
    }

    @Override
    public List<Path> getObservedRootDirectories() {
        return new ArrayList<>(listeners.keySet());
    }

    @Override
    public void onStart(final FileAlterationObserver observer) {
        final Path observedPath = observer.getDirectory().toPath();
        listenerForCurrentChange = listeners.get(observedPath);
        if (listenerForCurrentChange != null) {
            LOGGER.debug("Start collecting changes in {}", observedPath);
            listenerForCurrentChange.fileSystemChangesStarted();
        } else {
            LOGGER.warn("Ignoring file system changes in unknown directory: {}", observedPath);
        }
    }

    @Override
    public void onDirectoryCreate(final File directory) {
        if (listenerForCurrentChange != null) {
            final Path path = directory.toPath();
            LOGGER.debug("Create directory {}", path);
            listenerForCurrentChange.directoryCreated(path);
        }
    }

    @Override
    public void onDirectoryChange(final File directory) {
        if (listenerForCurrentChange != null) {
            final Path path = directory.toPath();
            LOGGER.debug("Change directory {}", path);
            listenerForCurrentChange.directoryModified(path);
        }
    }

    @Override
    public void onDirectoryDelete(final File directory) {
        if (listenerForCurrentChange != null) {
            final Path path = directory.toPath();
            LOGGER.debug("Delete directory {}", path);
            listenerForCurrentChange.directoryDeleted(path);
        }
    }

    @Override
    public void onFileCreate(final File file) {
        if (listenerForCurrentChange != null) {
            final Path path = file.toPath();
            LOGGER.debug("Create file {}", path);
            listenerForCurrentChange.fileCreated(path);
        }
    }

    @Override
    public void onFileChange(final File file) {
        if (listenerForCurrentChange != null) {
            final Path path = file.toPath();
            LOGGER.debug("Change file {}", path);
            listenerForCurrentChange.fileModified(path);
        }
    }

    @Override
    public void onFileDelete(final File file) {
        if (listenerForCurrentChange != null) {
            final Path path = file.toPath();
            LOGGER.debug("Delete file {}", path);
            listenerForCurrentChange.fileDeleted(path);
        }
    }

    @Override
    public void onStop(final FileAlterationObserver observer) {
        if (listenerForCurrentChange != null) {
            LOGGER.debug("Stop collecting changes in {}", observer.getDirectory());
            listenerForCurrentChange.fileSystemChangesStopped();
        }
    }

    @Override
    public void shutdown() {
        try {
            monitor.stop();
        } catch (IllegalStateException ignored) {
            // throw when the monitor was already stopped
        } catch (Exception e) {
            LOGGER.debug("Ignored error while shutting down", e);
        }
    }
}
