/*
 * Copied from webfiles service module
 *
 * Copyright 2014-2015 Hippo B.V. (http://www.onehippo.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.openweb.hippo.groovy.watch;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.lang3.StringUtils;
import org.hippoecm.repository.util.JcrUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.openweb.hippo.groovy.util.WatchFilesUtils;
import static org.hippoecm.repository.util.JcrUtils.getMultipleStringProperty;

public class GroovyFilesWatcherJcrConfig implements GroovyFilesWatcherConfig {

    private static final Logger log = LoggerFactory.getLogger(GroovyFilesWatcherJcrConfig.class);

    private static final String INCLUDED_FILES = "includedFiles";
    private static final String EXCLUDED_DIRECTORIES = "excludedDirectories";
    private static final String USE_WATCH_SERVICE_ON_OS_NAMES = "useWatchServiceOnOsNames";
    private static final String WATCHED_MODULES_PROPERTY = "watchedModules";
    private static final String WATCH_DELAY_MILLIS = "watchDelayMillis";
    private static final String MAX_FILE_LENGTH_KB = "maxFileLengthKb";
    private static final String PARAM_PREFIX = "groovy.sync.";

    private List<String> watchedModules;
    private List<String> includedFiles;
    private List<String> excludedDirs;
    private List<String> useWatchServiceOnOsNames;
    private long watchDelayMillis;
    private long maxFileLengthBytes;

    public GroovyFilesWatcherJcrConfig(final Node configNode) throws RepositoryException {
        watchedModules = getMultipleStringConfig(configNode, WATCHED_MODULES_PROPERTY, WatchFilesUtils.DEFAULT_WATCHED_MODULES);
        includedFiles = getMultipleStringConfig(configNode, INCLUDED_FILES, WatchFilesUtils.DEFAULT_INCLUDED_FILES);
        excludedDirs = getMultipleStringConfig(configNode, EXCLUDED_DIRECTORIES, WatchFilesUtils.DEFAULT_EXCLUDED_DIRECTORIES);
        useWatchServiceOnOsNames = getMultipleStringConfig(configNode, USE_WATCH_SERVICE_ON_OS_NAMES, WatchFilesUtils.DEFAULT_USE_WATCH_SERVICE_ON_OS_NAMES);
        watchDelayMillis = JcrUtils.getLongProperty(configNode, WATCH_DELAY_MILLIS, WatchFilesUtils.DEFAULT_WATCH_DELAY_MILLIS);
        maxFileLengthBytes = 1024 * JcrUtils.getLongProperty(configNode, MAX_FILE_LENGTH_KB, WatchFilesUtils.DEFAULT_MAX_FILE_LENGTH_KB);
    }

    private List<String> getMultipleStringConfig(final Node configNode, final String propertyName, final String[] defaultValue) {
        String[] values = getMultipleStringPropertyOrDefault(configNode, propertyName, defaultValue);
        String[] systemPropertyChecked = getMultipleStringFromSystemProperty(propertyName, values);
        if (log.isDebugEnabled()) {
            log.debug("Configuration value for {} = {}", propertyName, StringUtils.join(systemPropertyChecked, ";"));
        }
        return Collections.unmodifiableList(Arrays.asList(systemPropertyChecked));
    }

    private String[] getMultipleStringFromSystemProperty(final String propertyName, final String[] defaultValue) {
        String propValue = System.getProperty(PARAM_PREFIX + propertyName);
        return StringUtils.isNotBlank(propValue) ? propValue.split(";") : defaultValue;
    }

    private String[] getMultipleStringPropertyOrDefault(final Node configNode, final String propertyName, final String[] defaultValue) {
        try {
            return getMultipleStringProperty(configNode, propertyName, defaultValue);
        } catch (RepositoryException e) {
            log.warn("Error reading configuration property '{}' at {}, using default value instead: {}",
                propertyName, JcrUtils.getNodePathQuietly(configNode), Arrays.asList(defaultValue), e);
            return defaultValue;
        }
    }

    @Override
    public List<String> getWatchedModules() {
        return watchedModules;
    }

    @Override
    public List<String> getIncludedFiles() {
        return includedFiles;
    }

    @Override
    public List<String> getExcludedDirectories() {
        return excludedDirs;
    }

    @Override
    public List<String> getUseWatchServiceOnOsNames() {
        return useWatchServiceOnOsNames;
    }

    @Override
    public long getWatchDelayMillis() {
        return watchDelayMillis;
    }

    @Override
    public long getMaxFileLengthBytes() {
        return maxFileLengthBytes;
    }
}
