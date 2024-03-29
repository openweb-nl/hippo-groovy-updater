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
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.openweb.hippo.groovy.GroovyFileException;
import nl.openweb.hippo.groovy.GroovyFilesService;
import nl.openweb.hippo.groovy.util.WatchFilesUtils;

/**
 * Watches a directory with groovy files for changes, and applies the observed changes to the provided groovyfile
 * service. The provided directory should contain a child directories. Only existing child directories are watched for
 * changes.
 */
public class GroovyFilesWatcher implements SubDirectoriesWatcher.PathChangesListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(GroovyFilesWatcher.class);

    private final GroovyFilesWatcherConfig config;
    private final GroovyFilesService service;
    private final Session session;
    private final FileSystemObserver fileSystemObserver;

    public GroovyFilesWatcher(final GroovyFilesWatcherConfig config, final GroovyFilesService service,
                              final Session session) {
        this.config = config;
        this.service = service;
        this.session = session;

        this.fileSystemObserver = observeFileSystemIfNeeded();
    }

    private FileSystemObserver observeFileSystemIfNeeded() {
        final Path projectBaseDir = WatchFilesUtils.getProjectBaseDir();
        if (projectBaseDir == null) {
            return null;
        }
        if (config.getWatchedModules().isEmpty()) {
            LOGGER.info("Watching groovy files is disabled: no modules configured to watch in {}", projectBaseDir);
        } else {
            return observeFileSystem(projectBaseDir);
        }
        return null;
    }

    private FileSystemObserver observeFileSystem(final Path projectBaseDir) {
        FileSystemObserver fsObserver;
        try {
            fsObserver = createFileSystemObserver();
        } catch (Exception e) {
            LOGGER.error("Watching groovy files is disabled: cannot create file system observer", e);
            return null;
        }

        List<Path> groovyFilesDirectories = WatchFilesUtils.getGroovyFilesDirectories(projectBaseDir, config);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Observe {} paths: {}", groovyFilesDirectories.size(), groovyFilesDirectories.stream().map(Path::toString)
                .collect(Collectors.joining(", ")));
        }
        for (Path groovyFilesDirectory : groovyFilesDirectories) {
            try {
                LOGGER.info("About to listen to directories: {}", groovyFilesDirectory);
                SubDirectoriesWatcher.watch(groovyFilesDirectory, fsObserver, this);
            } catch (Exception e) {
                LOGGER.error("Failed to watch or import groovy files in module '{}'", groovyFilesDirectory, e);
            }
        }
        return fsObserver;
    }

    private FileSystemObserver createFileSystemObserver() throws IOException {
        final GlobFileNameMatcher watchedFiles = new GlobFileNameMatcher();
        watchedFiles.includeFiles(config.getIncludedFiles());
        watchedFiles.excludeDirectories(config.getExcludedDirectories());

        if (useWatchService()) {
            LOGGER.info("Using file system watcher");
            return new FileSystemWatcher(watchedFiles);
        } else {
            long watchDelayMillis = config.getWatchDelayMillis();
            LOGGER.info("Using file system poller (delay: {} ms)", watchDelayMillis);
            return new FileSystemPoller(watchedFiles, watchDelayMillis);
        }
    }

    private boolean useWatchService() {
        final OsNameMatcher matcher = new OsNameMatcher();
        for (String pattern : config.getUseWatchServiceOnOsNames()) {
            try {
                matcher.include(pattern);
            } catch (IllegalArgumentException e) {
                LOGGER.warn("Ignoring OS name '{}': {}. On this OS files will be watched using file system polling.",
                    pattern, e.getMessage());
            }
        }
        return matcher.matchesCurrentOs();
    }

    @Override
    public void onStart() {
        // nothing to do, but needed for thread synchronization in tests
    }

    @Override
    public void onPathsChanged(final Path watchedRootDir, final Set<Path> changedPaths) {
        final long startTime = System.currentTimeMillis();
        Set<Path> processedPaths = new HashSet<>(changedPaths.size());
        try {
            for (Path changedPath : changedPaths) {
                final Path relevantScriptPath = getRelevantScriptPath(changedPath);
                final Path relChangedDir = watchedRootDir.relativize(relevantScriptPath);
                reloadGroovyFile(processedPaths, relevantScriptPath, relChangedDir);
            }
            if (!processedPaths.isEmpty()) {
                session.save();
            }
        } catch (RepositoryException e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.info("Failed to reload groovy files from '{}', resetting session and trying to reimport whole bundle(s)",
                    changedPaths, e);
            } else if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Failed to reload groovy files from '{}' : '{}', resetting session and trying to reimport whole bundle(s)",
                    changedPaths, e.getMessage());
            }
            resetSilently(session);
            tryReimportBundles(watchedRootDir, changedPaths);
        } catch (IOException e) {
            LOGGER.info("Failure on reading files", e);
        }
        final long endTime = System.currentTimeMillis();
        LOGGER.info("Replacing groovy file took {} ms", endTime - startTime);
    }

    private static Path getRelevantScriptPath(final Path path) throws IOException {
        if(path.toString().endsWith(".groovy")){
            return path;
        } else {
            try(final Stream<Path> files = Files.walk(path.getParent())) {
                return files.filter(file -> containsPathForParameters(file, path)).findFirst().orElse(null);
            }
        }
    }

    private static boolean containsPathForParameters(final Path path, final Path parameters) {
        try {
            final File file = path.toFile();
            return file.isFile() && FileUtils.readFileToString(file, StandardCharsets.UTF_8).contains(parameters.getFileName().toString());
        } catch (IOException e) {
            LOGGER.info("failed to read {}", path, e);
        }
        return false;
    }

    private void reloadGroovyFile(final Set<Path> processedPaths, final Path changedPath, final Path relChangedDir) throws RepositoryException {
        LOGGER.info("Reloading groovyfile '{}'", relChangedDir);
        try {
            if (service.importGroovyFile(session, changedPath.toFile())) {
                processedPaths.add(changedPath);
            } else {
                LOGGER.info("** Failed to process '{}' as a groovy updater", relChangedDir);
            }
        } catch (NullPointerException e) {
            // sigh....because org.apache.jackrabbit.vault.util.FileInputSource.getByteStream() returns null
            // on a IOException (for example when a file is deleted during processing) I get an NPE I cannot avoid,
            // however, it is just similar to the IOException above, typically the result of an event that will
            // be processed shortly after this exception. Hence, ignore
            LOGGER.debug("NullPointerException we cannot avoid because org.apache.jackrabbit.vault.util.FileInputSource.getByteStream() " +
                "returns null on IOException. We can ignore this event since it is the result of an event that will " +
                " be processed shortly after this exception. Hence, ignore change path '{}'", changedPath, e);
        }
    }

    @Override
    public void onStop() {
        // nothing to do, but needed for thread synchronization in tests
    }

    private void resetSilently(final Session session) {
        try {
            session.refresh(false);
        } catch (RepositoryException e) {
            LOGGER.debug("Ignoring that session.refresh(false) failed", e);
        }
    }

    private void tryReimportBundles(final Path watchedRootDir, final Set<Path> changedPaths) {
        final Set<Path> reimportedBundleRoots = new HashSet<>();
        try {
            for (Path changedPath : changedPaths) {
                final Path relChangedDir = watchedRootDir.relativize(changedPath);
                final String bundleName = relChangedDir.getName(0).toString();
                final Path bundleRootDir = watchedRootDir.resolve(bundleName);
                if (reimportedBundleRoots.add(bundleRootDir)) {
                    LOGGER.info("Reimporting bundle '{}'", bundleName);
                    service.importGroovyFiles(session, bundleRootDir.toFile());
                }
            }
            session.save();
        } catch (GroovyFileException | RepositoryException e) {
            LOGGER.warn("Failed to reimport groovy file bundles {}, resetting session", reimportedBundleRoots, e);
            resetSilently(session);
        }
    }

    public void shutdown() {
        if (fileSystemObserver != null) {
            fileSystemObserver.shutdown();
        }
    }
}
