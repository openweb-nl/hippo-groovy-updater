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
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.maven.plugin.MojoExecutionException;

import nl.openweb.hippo.groovy.model.jaxb.Node;
import static nl.openweb.hippo.groovy.Marshal.getMarshaller;
import static nl.openweb.hippo.groovy.XmlGenerator.getEcmExtensionNode;
import static nl.openweb.hippo.groovy.XmlGenerator.getUpdateScriptNode;
import static nl.openweb.hippo.groovy.XmlGenerator.getUpdateScriptXmlFilename;
import static nl.openweb.hippo.groovy.model.Constants.Files.ECM_EXTENSIONS_NAME;

public class ScriptProcessorXML extends ScriptProcessor{

    /**
     * Write hippoecm-extension.xml file
     *
     * @param parsedGroovyFiles list of files to use in the generation
     * @throws MojoExecutionException
     */
    private void writeEcmExtensions(final List<File> parsedGroovyFiles) throws MojoExecutionException {
        final Node ecmExtensionNode = getEcmExtensionNode(sourceDir, targetDir, parsedGroovyFiles, initializeNamePrefix);
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
    public List<File> processUpdateScripts(final List<File> groovyFiles) throws MojoExecutionException{
        List<File> files = super.processUpdateScripts(groovyFiles);
        writeEcmExtensions(files);
        return files;
    }

    /**
     * Generate updater xml from groovy file
     *
     * @param file groovy script to parse
     * @return parsing successful
     */
    @Override
    protected boolean processUpdateScript(final File file) {
        getLog().debug("Converting " + file.getAbsolutePath() + " to updater xml");
        final Node updateScriptNode = getUpdateScriptNode(file);
        if (updateScriptNode == null) {
            getLog().warn("Skipping file: " + file.getAbsolutePath() + ", not a valid updatescript");
            return false;
        }
        final File targetFile = new File(targetDir, getUpdateScriptXmlFilename(sourceDir, file));
        targetFile.getParentFile().mkdirs();
        getLog().info("Write " + targetFile.getAbsolutePath());
        return marshal(updateScriptNode, targetFile);
    }

    /**
     * Write the node in XML style to the file
     * @param node
     * @param file
     * @return result of writing to the file
     */
    protected boolean marshal(final Node node, final File file) {
        try {
            getMarshaller().marshal(node, file);
            return true;
        } catch (final JAXBException e) {
            getLog().error("Failed to make xml: " + file.getAbsolutePath(), e);
            return false;
        }
    }
}
