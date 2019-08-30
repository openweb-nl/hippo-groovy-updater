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
import java.util.Collections;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import nl.openweb.hippo.groovy.annotations.Bootstrap;
import nl.openweb.hippo.groovy.annotations.Updater;
import nl.openweb.hippo.groovy.model.ScriptClass;
import static nl.openweb.hippo.groovy.Generator.getContentroot;
import static nl.openweb.hippo.groovy.Generator.getUpdatePath;
import static nl.openweb.hippo.groovy.Generator.sanitizeFileName;
import static nl.openweb.hippo.groovy.model.Constants.Files.YAML_EXTENSION;

/**
 * Generator to parse a groovy file into repository-data YAML
 */
public abstract class YamlGenerator {

    protected YamlGenerator() {
        super();
    }

    /**
     * Parse file to updater node
     *
     * @param sourceDir   the directory to read the resources from
     * @param scriptClass class to use for source
     * @return Node object representing the groovy updater to marshall to xml
     */
    public static Map<String, Map<String, Object>> getUpdateYamlScript(final File sourceDir, final ScriptClass scriptClass) {
        Map<String, Object> properties = PropertyCollector.getPropertiesForUpdater(scriptClass, sourceDir);

        final Map<String, Map<String, Object>> scriptYaml = Collections.singletonMap(getBootstrapPath(scriptClass), properties);
        if (Bootstrap.ContentRoot.REGISTRY.equals(scriptClass.getBootstrap(true).contentroot())) {
            Map<String, Object> config = Collections.singletonMap("config", scriptYaml);
            return Collections.singletonMap("definitions", config);
        }
        return scriptYaml;
    }

    /**
     * Get update script yaml filename
     *
     * @param basePath    path to make returning path relative to
     * @param scriptClass File object for the groovy script
     * @return the path, relative to the basePath, converted \ to /, with yaml extension
     */
    public static String getUpdateScriptYamlFilename(final File basePath, final ScriptClass scriptClass) {
        final String fileName = scriptClass.getFile().getAbsolutePath().substring(basePath.getAbsolutePath().length() + 1);
        final Bootstrap bootstrap = scriptClass.getBootstrap();

        String versionString = bootstrap != null && getContentroot(bootstrap).equals(Bootstrap.ContentRoot.QUEUE) &&
            bootstrap.reload() && !bootstrap.version().isEmpty() ?
            "-v" + bootstrap.version() : StringUtils.EMPTY;

        return sanitizeFileName(fileName) + versionString + YAML_EXTENSION;
    }

    private static String getBootstrapPath(final ScriptClass scriptClass) {
        Updater updater = scriptClass.getUpdater();
        Bootstrap bootstrap = scriptClass.getBootstrap(true);
        Bootstrap.ContentRoot contentroot = getContentroot(bootstrap);
        return getUpdatePath(contentroot) + "/" + updater.name();
    }

    public static String getYamlString(Map<?, ?> map) {
        return map == null ? StringUtils.EMPTY : getYaml().dump(map);
    }

    private static Yaml getYaml() {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        return new Yaml(options);
    }
}
