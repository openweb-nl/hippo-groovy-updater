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
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.maven.plugin.MojoExecutionException;

import nl.openweb.hippo.groovy.model.ScriptClass;
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
     * @param parsedScriptClasses list of scriptclasses to use in the generation
     * @throws MojoExecutionException
     */
    private void writeEcmExtensions(final List<ScriptClass> parsedScriptClasses) throws MojoExecutionException {
        final Node ecmExtensionNode = getEcmExtensionNode(sourceDir, targetDir, parsedScriptClasses, initializeNamePrefix);
        if (ecmExtensionNode == null) {
            throw new MojoExecutionException("No input for " + ECM_EXTENSIONS_NAME);
        }
        targetDir.mkdirs();

        marshal(ecmExtensionNode, new File(targetDir, ECM_EXTENSIONS_NAME));
    }

    /**
     * Generate updater xml files from groovy scripts
     *
     * @param scriptClassList groovy scripts to parse
     * @return list of valid parsed groovy files
     */
    @Override
    public List<ScriptClass> processUpdateScripts(final List<ScriptClass> scriptClassList) throws MojoExecutionException {
        List<ScriptClass> scriptClasses = processGroovyScripts(scriptClassList);
        writeEcmExtensions(scriptClasses);
        return scriptClasses;
    }

    /**
     * Generate updater xml from groovy file
     *
     * @param script groovy script to parse
     * @return parsing successful
     */
    @Override
    protected boolean processUpdateScript(final ScriptClass script) {
        getLog().debug("Converting " + script.getFile().getAbsolutePath() + " to updater xml");
        final Node updateScriptNode = getUpdateScriptNode(sourceDir, script);
        if (updateScriptNode == null) {
            getLog().warn("Skipping file: " + script.getFile().getAbsolutePath() + ", not a valid updatescript");
            return false;
        }
        final File targetFile = new File(targetDir, getUpdateScriptXmlFilename(sourceDir, script.getFile()));
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
