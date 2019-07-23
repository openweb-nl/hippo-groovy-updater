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
package nl.openweb.hippo.groovy.maven.processor;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;

import nl.openweb.hippo.groovy.annotations.Bootstrap;
import nl.openweb.hippo.groovy.model.ScriptClass;
import static nl.openweb.hippo.groovy.YamlGenerator.getUpdateScriptYamlFilename;
import static nl.openweb.hippo.groovy.YamlGenerator.getUpdateYamlScript;
import static nl.openweb.hippo.groovy.YamlGenerator.getYamlString;

public class ScriptProcessorYAML extends ScriptProcessor {
    protected String yamlContentPath;
    protected String yamlConfigurationPath;

    /**
     * Generate updater xml files from groovy scripts
     *
     * @param groovyFiles groovy scripts to parse
     * @return list of valid parsed groovy files
     */
    @Override
    public List<ScriptClass> processUpdateScripts(final List<ScriptClass> groovyFiles) throws MojoExecutionException {
        return processGroovyScripts(groovyFiles);
    }

    /**
     * Generate updater yaml from groovy file
     *
     * @param scriptClass groovy script to parse
     * @return parsing successful
     */
    @Override
    protected boolean processUpdateScript(final ScriptClass scriptClass) {
        getLog().debug("Converting " + scriptClass.getFile().getAbsolutePath() + " to updater yaml");
        final String updateScript = getYamlString(getUpdateYamlScript(sourceDir, scriptClass));
        if (StringUtils.isBlank(updateScript)) {
            getLog().warn("Skipping file: " + scriptClass.getFile().getAbsolutePath() + ", not a valid updatescript");
            return false;
        }
        final String targetPath =
                Bootstrap.ContentRoot.REGISTRY.equals(scriptClass.getBootstrap(true).contentroot()) ?
                        yamlConfigurationPath : yamlContentPath;
        final File targetFile = new File(new File(targetDir, targetPath), getUpdateScriptYamlFilename(sourceDir, scriptClass));
        targetFile.getParentFile().mkdirs();
        return marshal(updateScript, targetFile);
    }

    protected boolean marshal(final String fileContent, final File file) {
        try {
            getLog().info("Writing " + file.getAbsolutePath());
            FileUtils.write(file, fileContent, Charset.defaultCharset());
            return true;
        } catch (final IOException e) {
            getLog().error("Error parsing to yaml: " + file.getAbsolutePath(), e);
            return false;
        }
    }

    public void setYamlContentPath(final String yamlPath) {
        this.yamlContentPath = yamlPath;
    }

    public void setYamlConfigurationPath(final String yamlPath) {
        this.yamlConfigurationPath = yamlPath;
    }
}
