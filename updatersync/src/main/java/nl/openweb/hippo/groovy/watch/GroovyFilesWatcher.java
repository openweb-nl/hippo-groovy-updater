/*
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

import nl.openweb.hippo.groovy.GroovyFileException;
import nl.openweb.hippo.groovy.GroovyFilesService;
import nl.openweb.hippo.groovy.util.WatchFilesUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Watches a directory with web files for changes, and applies the observed changes to the
 * provided web file service. The provided directory should contain a child directories.
 * Only existing child directories are watched for changes.
 */
public class GroovyFilesWatcher implements SubDirectoriesWatcher.PathChangesListener {

    public static Logger log = LoggerFactory.getLogger(GroovyFilesWatcher.class);

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
        if (config.getWatchedModules().size() > 0) {
            return observeFileSystem(projectBaseDir);
        } else {
            log.info("Watching web files is disabled: no web file modules configured to watch");
        }
        return null;
    }

    private FileSystemObserver observeFileSystem(final Path projectBaseDir) {
        FileSystemObserver fsObserver;
        try {
            fsObserver = createFileSystemObserver();
        } catch (Exception e) {
            log.error("Watching web files is disabled: cannot create file system observer", e);
            return null;
        }

        List<Path> groovyFilesDirectories = WatchFilesUtils.getGroovyFilesDirectories(projectBaseDir, config);
        log.debug("Observe {} paths: {}", groovyFilesDirectories.size(), groovyFilesDirectories.stream().map(Path::toString)
                .collect(Collectors.joining(", ")));
        for (Path groovyFilesDirectory : groovyFilesDirectories) {
            try {
                log.info("About to listen to directories: " + groovyFilesDirectory.toString());
                SubDirectoriesWatcher.watch(groovyFilesDirectory, fsObserver, this);
            } catch (Exception e) {
                log.error("Failed to watch or import web files in module '{}'", groovyFilesDirectory.toString(), e);
            }
        }
        return fsObserver;
    }

    private FileSystemObserver createFileSystemObserver() throws Exception {
        final GlobFileNameMatcher watchedFiles = new GlobFileNameMatcher();
        watchedFiles.includeFiles(config.getIncludedFiles());
        watchedFiles.excludeDirectories(config.getExcludedDirectories());

        if (useWatchService()) {
            log.info("Using file system watcher");
            return new FileSystemWatcher(watchedFiles);
        } else {
            long watchDelayMillis = config.getWatchDelayMillis();
            log.info("Using file system poller (delay: {} ms)", watchDelayMillis);
            return new FileSystemPoller(watchedFiles, watchDelayMillis);
        }
    }

    private boolean useWatchService() {
        final OsNameMatcher matcher = new OsNameMatcher();
        for (String pattern : config.getUseWatchServiceOnOsNames()) {
            try {
                matcher.include(pattern);
            } catch (IllegalArgumentException e) {
                log.warn("Ignoring OS name '{}': {}. On this OS web files will be watched using file system polling.",
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
                final Path relChangedDir = watchedRootDir.relativize(changedPath);
                final String scriptName = relChangedDir.getName(0).toString();
                final String scriptSubDir = getBundleSubDir(relChangedDir);

                log.info("Replacing groovyfile '{}': /{}", scriptName, scriptSubDir);
                try {
                    service.importGroovyFile(session, changedPath.toFile());
                    processedPaths.add(changedPath);
                } catch (IOException e) {
                    // we do not have to take action. An IOException is the result of a concurrent change (delete/move)
                    // during creation or processing of the archive. The change will trigger a new import
                    log.debug("IOException during importing '{}'. This is typically the result of a file that is deleted" +
                            "during the import of a directory. This delete will trigger an event shortly after this" +
                            " exception.", changedPath, e);
                } catch (NullPointerException e) {
                    // sigh....because org.apache.jackrabbit.vault.util.FileInputSource.getByteStream() returns null
                    // on a IOException (for example when a file is deleted during processing) I get an NPE I cannot avoid,
                    // however, it is just similar to the IOException above, typically the result of an event that will
                    // be processed shortly after this exception. Hence, ignore
                    log.debug("NullPointerException we cannot avoid because org.apache.jackrabbit.vault.util.FileInputSource.getByteStream() " +
                            "returns null on IOException. We can ignore this event since it is the result of an event that will " +
                            " be processed shortly after this exception. Hence, ignore change path '{}'", changedPath, e);

                } catch (JAXBException e) {
                    log.error("JAXBException in import", e);
                }
            }
            if (!processedPaths.isEmpty()) {
                session.save();
            }
        } catch (RepositoryException e) {
            if (log.isDebugEnabled()) {
                log.info("Failed to reload web files from '{}', resetting session and trying to reimport whole bundle(s)",
                        changedPaths, e);
            } else {
                log.info("Failed to reload web files from '{}' : '{}', resetting session and trying to reimport whole bundle(s)",
                        changedPaths, e.toString());
            }
            resetSilently(session);
            tryReimportBundles(watchedRootDir, changedPaths);
        }
        final long endTime = System.currentTimeMillis();
        log.info("Replacing web files took {} ms", endTime - startTime);
    }

    @Override
    public void onStop() {
        // nothing to do, but needed for thread synchronization in tests
    }

    private String getBundleSubDir(final Path relChangedDir) {
        if (relChangedDir.getNameCount() == 1) {
            return StringUtils.EMPTY;
        }
        final Path subPath = relChangedDir.subpath(1, relChangedDir.getNameCount());
        // ensure that we use '/' as the JCR path separator, even if the filesystem path uses something else
        return StringUtils.join(subPath.iterator(), '/');
    }

    private void resetSilently(final Session session) {
        try {
            session.refresh(false);
        } catch (RepositoryException e) {
            log.debug("Ignoring that session.refresh(false) failed", e);
        }
    }

    private void tryReimportBundles(final Path watchedRootDir, final Set<Path> changedPaths) {
        final Set<Path> reimportedBundleRoots = new HashSet<>();
        try {
            for (Path changedPath: changedPaths) {
                final Path relChangedDir = watchedRootDir.relativize(changedPath);
                final String bundleName = relChangedDir.getName(0).toString();
                final Path bundleRootDir = watchedRootDir.resolve(bundleName);
                if (reimportedBundleRoots.add(bundleRootDir)) {
                    log.info("Reimporting bundle '{}'", bundleName);
                    service.importGroovyFiles(session, bundleRootDir.toFile());
                }
            }
            session.save();
        } catch (GroovyFileException | RepositoryException | IOException e) {
            log.warn("Failed to reimport web file bundles {}, resetting session", reimportedBundleRoots, e);
            resetSilently(session);
        }
    }

    public void shutdown() throws InterruptedException {
        if (fileSystemObserver != null) {
            fileSystemObserver.shutdown();
        }
    }

}
