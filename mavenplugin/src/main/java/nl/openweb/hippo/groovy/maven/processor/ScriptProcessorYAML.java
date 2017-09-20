/*
 * Copyright 2017 Open Web IT B.V. (https://www.openweb.nl/)
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

import nl.openweb.hippo.groovy.YamlGenerator;
import static nl.openweb.hippo.groovy.YamlGenerator.HCM_ACTIONS_NAME;
import static nl.openweb.hippo.groovy.YamlGenerator.getUpdateScriptYamlFilename;
import static nl.openweb.hippo.groovy.YamlGenerator.getUpdateYamlScript;

public class ScriptProcessorYAML extends ScriptProcessor{
    protected String yamlPath;

    /**
     * Generate updater xml files from groovy scripts
     *
     * @param groovyFiles groovy scripts to parse
     * @return list of valid parsed groovy files
     */
    public List<File> processUpdateScripts(final List<File> groovyFiles) throws MojoExecutionException {
        List<File> files = super.processUpdateScripts(groovyFiles);
        writeActionList(files);
        return files;
    }

    private void writeActionList(final List<File> files) throws MojoExecutionException {
        try {
            String hcmActionsList = YamlGenerator.getHcmActionsList(sourceDir, targetDir, files);
            if(StringUtils.isNotBlank(hcmActionsList)){
                marshal(hcmActionsList, new File(targetDir, HCM_ACTIONS_NAME));
            }
        } catch (IOException e) {
            throw new MojoExecutionException("failed to generate hcm-actions.yaml",e);
        }
    }

    /**
     * Generate updater yaml from groovy file
     *
     * @param file groovy script to parse
     * @return parsing successful
     */
    @Override
    protected boolean processUpdateScript(final File file) {
        getLog().debug("Converting " + file.getAbsolutePath() + " to updater yaml");
        final String updateScript = getUpdateYamlScript(file);
        if (StringUtils.isBlank(updateScript)) {
            getLog().warn("Skipping file: " + file.getAbsolutePath() + ", not a valid updatescript");
            return false;
        }
        final File targetFile = new File(new File(targetDir, yamlPath), getUpdateScriptYamlFilename(sourceDir, file));
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

    public void setYamlPath(final String yamlPath) {
        this.yamlPath = yamlPath;
    }

}
