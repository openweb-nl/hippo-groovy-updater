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
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.maven.plugin.MojoExecutionException;

import nl.openweb.hippo.groovy.YamlGenerator;
import nl.openweb.hippo.groovy.annotations.Bootstrap;
import static java.util.stream.Collectors.toList;
import static nl.openweb.hippo.groovy.Generator.getBootstrap;
import static nl.openweb.hippo.groovy.YamlGenerator.HCM_ACTIONS_NAME;
import static nl.openweb.hippo.groovy.YamlGenerator.getUpdateScriptYamlFilename;
import static nl.openweb.hippo.groovy.YamlGenerator.getUpdateYamlScript;
import static nl.openweb.hippo.groovy.YamlGenerator.getYamlString;
import static nl.openweb.hippo.groovy.model.Constants.Files.YAML_EXTENSION;

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
        processReloading(files);
        return files;
    }

    private void processReloading(final List<File> files) throws MojoExecutionException {
        try {
            List<Pair<File, Bootstrap>> pairs = files.stream().map(file -> Pair.of(file, getBootstrap(file)))
                    .filter(pair -> pair.getValue() != null &&
                            pair.getValue().reload())
                    .collect(toList());

            //registry or unversioned scripts
            List<Pair<File, Bootstrap>> reloadByActionList = pairs.stream()
                    .filter(pair -> pair.getValue().contentroot().equals(Bootstrap.ContentRoot.REGISTRY) ||
                            pair.getValue().version().isEmpty())
                    .collect(toList());
            String hcmActionsList = YamlGenerator.getHcmActionsList(sourceDir, targetDir, reloadByActionList);
            if(StringUtils.isNotBlank(hcmActionsList)){
                marshal(hcmActionsList, new File(targetDir, HCM_ACTIONS_NAME));
            }

            //queue and version
            pairs.stream().filter(pair -> pair.getValue().contentroot().equals(Bootstrap.ContentRoot.QUEUE) &&
                    !pair.getValue().version().isEmpty())
                    .forEach(pair -> renameWithVersion(pair.getKey(), pair.getValue()));
        } catch (IOException e) {
            throw new MojoExecutionException("failed to generate hcm-actions.yaml",e);
        }
    }

    private void renameWithVersion(File file, Bootstrap bootstrap) {

        String oldFileName = getUpdateScriptYamlFilename(targetDir, file);
        String newFileName = getUpdateScriptYamlFilename(targetDir, file)
                .replaceAll(YAML_EXTENSION, "-v" + bootstrap.version() + YAML_EXTENSION);
        File oldFile = new File(new File(targetDir, yamlPath), oldFileName);
        File newFile = new File(new File(targetDir, yamlPath), newFileName);
        try {
            Files.move(oldFile.toPath(), newFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.error("Failed to move versioned updater to new filename: " + newFileName);
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
        final String updateScript = getYamlString(getUpdateYamlScript(file));
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
