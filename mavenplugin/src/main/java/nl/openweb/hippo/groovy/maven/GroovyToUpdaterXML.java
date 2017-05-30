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

package nl.openweb.hippo.groovy.maven;

import java.io.File;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import nl.openweb.hippo.groovy.model.jaxb.Node;
import static java.util.stream.Collectors.toList;
import static nl.openweb.hippo.groovy.Marshal.getMarshaller;
import static nl.openweb.hippo.groovy.XmlGenerator.addClassPath;
import static nl.openweb.hippo.groovy.XmlGenerator.getEcmExtensionNode;
import static nl.openweb.hippo.groovy.XmlGenerator.getGroovyFiles;
import static nl.openweb.hippo.groovy.XmlGenerator.getUpdateScriptNode;
import static nl.openweb.hippo.groovy.XmlGenerator.getUpdateScriptXmlFilename;
import static nl.openweb.hippo.groovy.model.Constants.Files.ECM_EXTENSIONS_NAME;

/**
 * Groovy Updater Maven Plugin to generate bootstrap from groovy files
 */
@Mojo(name = "generate")
public class GroovyToUpdaterXML extends AbstractMojo {

    @Parameter(defaultValue = "${project.build.scriptSourceDirectory}", property = "sourceDir")
    private File sourceDir;
    @Parameter(defaultValue = "${project.build.outputDirectory}", property = "targetDir")
    private File targetDir;
    @Parameter(defaultValue = "hippo-updater-", property = "initializeNamePrefix")
    private String initializeNamePrefix;

    public void execute() throws MojoExecutionException, MojoFailureException {
        Log log = getLog();
        log.info("sources: " + sourceDir.getAbsolutePath());
        log.info("target: " + targetDir.getAbsolutePath());
        log.info("Add outputDirectory to classpath for project files: " + targetDir.getPath());
        addClassPath(targetDir.getPath());

        List<File> groovyFiles = getGroovyFiles(sourceDir);
        List<File> parsedGroovyFiles = processUpdateScripts(groovyFiles);
        writeEcmExtensions(parsedGroovyFiles);
    }

    /**
     * Write hippoecm-extension.xml file
     *
     * @param parsedGroovyFiles list of files to use in the generation
     * @throws MojoExecutionException
     */
    private void writeEcmExtensions(final List<File> parsedGroovyFiles) throws MojoExecutionException {
        Node ecmExtensionNode = getEcmExtensionNode(sourceDir, parsedGroovyFiles, initializeNamePrefix);
        if (ecmExtensionNode == null) {
            throw new MojoExecutionException("No input for " + ECM_EXTENSIONS_NAME);
        }
        targetDir.mkdirs();

        marshal(ecmExtensionNode, new File(targetDir, ECM_EXTENSIONS_NAME));
    }

    /**
     * Generate updater xml files from groovy scripts
     *
     * @param groovyFiles groovy scripts to parse
     * @return list of valid parsed groovy files
     */
    private List<File> processUpdateScripts(final List<File> groovyFiles) {
        getLog().info("Converting " + groovyFiles.size() + " groovy scripts to updater xml");
        return groovyFiles.stream().filter(this::processUpdateScript).collect(toList());
    }

    /**
     * Generate updater xml from groovy file
     *
     * @param file groovy script to parse
     * @return parsing successful
     */
    private boolean processUpdateScript(File file) {
        getLog().debug("Converting " + file.getAbsolutePath() + " to updater xml");
        Node updateScriptNode = getUpdateScriptNode(file);
        if (updateScriptNode == null) {
            getLog().error("Unparsable file: " + file.getAbsolutePath());
            return false;
        }
        File targetFile = new File(targetDir, getUpdateScriptXmlFilename(sourceDir, file));
        targetFile.getParentFile().mkdirs();
        getLog().info("Write " + targetFile.getAbsolutePath());
        return marshal(updateScriptNode, targetFile);
    }

    private boolean marshal(Node node, File file) {
        try {
            getMarshaller().marshal(node, file);
            return true;
        } catch (JAXBException e) {
            getLog().error("Failed to make xml: " + file.getAbsolutePath(), e);
            return false;
        }
    }


}
