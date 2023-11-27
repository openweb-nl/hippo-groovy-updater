/*
 * Copyright 2019 Open Web IT B.V. (https://www.openweb.nl/)
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
 */

package nl.openweb.hippo.groovy;

import java.io.File;
import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import nl.openweb.hippo.groovy.annotations.Updater;
import nl.openweb.hippo.groovy.model.ScriptClass;
import static nl.openweb.hippo.groovy.model.Constants.NodeType.HIPPOSYS_UPDATERINFO;
import static nl.openweb.hippo.groovy.model.Constants.PropertyName.HIPPOSYS_BATCHSIZE;
import static nl.openweb.hippo.groovy.model.Constants.PropertyName.HIPPOSYS_DESCRIPTION;
import static nl.openweb.hippo.groovy.model.Constants.PropertyName.HIPPOSYS_DRYRUN;
import static nl.openweb.hippo.groovy.model.Constants.PropertyName.HIPPOSYS_LOGTARGET;
import static nl.openweb.hippo.groovy.model.Constants.PropertyName.HIPPOSYS_PARAMETERS;
import static nl.openweb.hippo.groovy.model.Constants.PropertyName.HIPPOSYS_PATH;
import static nl.openweb.hippo.groovy.model.Constants.PropertyName.HIPPOSYS_QUERY;
import static nl.openweb.hippo.groovy.model.Constants.PropertyName.HIPPOSYS_SCRIPT;
import static nl.openweb.hippo.groovy.model.Constants.PropertyName.HIPPOSYS_THROTTLE;
import static nl.openweb.hippo.groovy.model.Constants.PropertyName.JCR_MIXIN_TYPES;
import static nl.openweb.hippo.groovy.model.Constants.PropertyName.JCR_PRIMARY_TYPE;

public class PropertyCollector {
    private PropertyCollector() {
        //no instantiations
    }

    private static void addPropertyIfNotEmpty(final Map<String, Object> properties, final String name, final Object value) {
        if (StringUtils.isNotBlank(value.toString())) {
            properties.put(name, value);
        }
    }

    private static String getValueOrFileContent(final ScriptClass script, final File sourceDir, final String value) {
        final File parentDir = value.startsWith("/") ? sourceDir : script.getFile().getParentFile();
        final File file = new File(parentDir, value);
        if (file.exists()) {
            try {
                return ScriptClassFactory.readFileEnsuringLinuxLineEnding(file);
            } catch (IOException | InvalidPathException e) {
                //do nothing, it's fine
            }
        }
        return value;
    }

    private static List<String> createMultiValueProperty(final String mixin) {
        return Arrays.asList(mixin.split("\\s*,\\s*"));
    }

    public static Map<String, Object> getPropertiesForUpdater(ScriptClass script, File sourceDir) {
        Updater updater = script.getUpdater();

        final Map<String, Object> properties = new LinkedHashMap<>();
        properties.put(JCR_PRIMARY_TYPE, HIPPOSYS_UPDATERINFO);
        properties.put(HIPPOSYS_BATCHSIZE, updater.batchSize());
        addPropertyIfNotEmpty(properties, HIPPOSYS_DESCRIPTION, updater.description());
        properties.put(HIPPOSYS_DRYRUN, updater.dryRun());
        if (StringUtils.isNotBlank(updater.mixin())) {
            addPropertyIfNotEmpty(properties, JCR_MIXIN_TYPES, createMultiValueProperty(updater.mixin()));
        }
        if (StringUtils.isNotBlank(updater.logTarget().toString())) {
            addPropertyIfNotEmpty(properties, HIPPOSYS_LOGTARGET, updater.logTarget().toString());
        }
        addPropertyIfNotEmpty(properties, HIPPOSYS_PARAMETERS, getValueOrFileContent(script, sourceDir, updater.parameters()));
        if (StringUtils.isBlank(updater.xpath())) {
            addPropertyIfNotEmpty(properties, HIPPOSYS_PATH, updater.path());
        }
        addPropertyIfNotEmpty(properties, HIPPOSYS_QUERY, updater.xpath());
        addPropertyIfNotEmpty(properties, HIPPOSYS_SCRIPT, script.getContent());
        properties.put(HIPPOSYS_THROTTLE, updater.throttle());
        return properties;
    }
}
