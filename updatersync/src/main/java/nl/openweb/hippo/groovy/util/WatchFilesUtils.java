/*
 * Modifications Copyright 2017 Open Web IT B.V. (https://www.openweb.nl/)
 *
 * This file is Copyrighted to Hippo B.V. but there has been modification done to
 * via Open Web IT B.V. these modification (See the modification via version control history)
 * are licence to Open Web IT B.V.
 * Under Apache License, Version 2.0. Please see the Original Copyright notice blew.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * Copyright 2015 Hippo B.V. (http://www.onehippo.com)
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
package nl.openweb.hippo.groovy.util;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.openweb.hippo.groovy.watch.GroovyFilesWatcherConfig;


public class WatchFilesUtils {

    public static final String PROJECT_BASEDIR_PROPERTY = "project.basedir";
    public static final String RESOURCE_FILES_LOCATION_IN_MODULE = "src/main/resources";
    public static final String SCRIPT_FILES_LOCATION_IN_MODULE = "src/main/scripts";
    public static final String SCRIPT_ROOT = "/hippo:configuration/hippo:update/hippo:registry";
    public static final String[] DEFAULT_INCLUDED_FILES = {};
    public static final String[] DEFAULT_EXCLUDED_DIRECTORIES = {};
    public static final String[] DEFAULT_USE_WATCH_SERVICE_ON_OS_NAMES = {};
    public static final String[] DEFAULT_WATCHED_MODULES = {};
    public static final Long DEFAULT_WATCH_DELAY_MILLIS = 500L;
    public static final Long DEFAULT_MAX_FILE_LENGTH_KB = 256L;
    private static final Logger log = LoggerFactory.getLogger(WatchFilesUtils.class);

    public static Path getProjectBaseDir() {
        final String projectBaseDir = System.getProperty(PROJECT_BASEDIR_PROPERTY);
        if (projectBaseDir != null && !projectBaseDir.isEmpty()) {
            final Path baseDir = FileSystems.getDefault().getPath(projectBaseDir);
            if (Files.isDirectory(baseDir)) {
                log.debug("Basedir found: " + baseDir.toString());
                return baseDir;
            } else {
                log.warn("Watching groovy files is disabled: environment variable '{}' does not point to a directory", PROJECT_BASEDIR_PROPERTY);
            }
        } else {
            log.info("Watching groovy files is disabled: environment variable '{}' not set or empty", PROJECT_BASEDIR_PROPERTY);
        }
        return null;
    }

    public static List<Path> getGroovyFilesDirectories(final Path projectBaseDir,
                                                       final GroovyFilesWatcherConfig config) {

        List<Path> filesDirectories = new ArrayList<>(config.getWatchedModules().size());
        log.debug("getting groovy file directories");
        for (String watchedModule : config.getWatchedModules()) {
            final Path modulePath = projectBaseDir.resolve(watchedModule);
            List<Path> paths = new ArrayList<>();
            paths.add(modulePath.resolve(SCRIPT_FILES_LOCATION_IN_MODULE));
            paths.add(modulePath.resolve(RESOURCE_FILES_LOCATION_IN_MODULE));
            List<Path> pathList = paths.stream().filter(Files::isDirectory).collect(Collectors.toList());
            filesDirectories.addAll(pathList);
            log.debug("Found {} paths to add for watching. {}", pathList.size(), pathList.stream().map(Path::toString)
                    .collect(Collectors.joining(", ")));
            if (pathList.isEmpty()) {
                log.warn("Cannot watch groovy files in module '{}': it does not contain directory '{}' or {}",
                        watchedModule, SCRIPT_FILES_LOCATION_IN_MODULE, RESOURCE_FILES_LOCATION_IN_MODULE);
            }
        }
        return filesDirectories;
    }
}
