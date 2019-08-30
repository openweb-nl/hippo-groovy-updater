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

package nl.openweb.hippo.groovy.maven;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.openweb.hippo.groovy.maven.processor.ScriptProcessor;
import nl.openweb.hippo.groovy.maven.processor.ScriptProcessorXML;
import nl.openweb.hippo.groovy.maven.processor.ScriptProcessorYAML;
import static nl.openweb.hippo.groovy.ScriptClassFactory.getScriptClasses;
import static nl.openweb.tools.Tools.compareFolders;
import static org.junit.Assert.assertFalse;

public class ScriptProcessorTest {

    static Logger logger = LoggerFactory.getLogger(ScriptProcessorTest.class);
    ScriptProcessor processor;

    @Test
    public void testXML() throws URISyntaxException, MojoExecutionException, IOException {
        processor = new ScriptProcessorXML();
        processor.setInitializeNamePrefix("sampleproject-update-");
        processor.setLog(new SystemStreamLog());

        File input = new File(getClass().getResource("/src/main/scripts").toURI());
        File xml_output = new File(new File(getClass().getResource("/").toURI()), "xml_output");

        File resource = new File(getClass()
            .getResource("/src/main/resources/hippoecm-extension.xml").toURI());
        File targetResource = new File(xml_output, "hippoecm-extension.xml");
        if (xml_output.exists()) {
            FileUtils.deleteDirectory(xml_output);
            assertFalse(xml_output.exists());
        }
        //Preparation: existing ecm-extension.xml in target
        targetResource.mkdirs();
        Files.copy(resource.toPath(), targetResource.toPath(), StandardCopyOption.REPLACE_EXISTING);
        processor.setSourceDir(input);
        processor.setTargetDir(xml_output);

        processor.processUpdateScripts(getScriptClasses(input));
        File xml_expected = new File(getClass().getResource("/target_xml").toURI());

        compareFolders(xml_expected, xml_output);
    }

    @Test
    public void testYaml() throws URISyntaxException, MojoExecutionException, IOException {
        processor = new ScriptProcessorYAML();
        processor.setInitializeNamePrefix("my-hippo-updater-");
        processor.setLog(new SystemStreamLog());

        File input = new File(getClass().getResource("/src/main/scripts").toURI());
        File yaml_output = new File(new File(getClass().getResource("/").toURI()), "yaml_output");

        File resource = new File(getClass()
            .getResource("/src/main/resources/hcm-actions.yaml").toURI());
        File targetResource = new File(yaml_output, "hcm-actions.yaml");
        if (yaml_output.exists()) {
            FileUtils.deleteDirectory(yaml_output);
            assertFalse(yaml_output.exists());
        }
        processor.setSourceDir(input);
        processor.setTargetDir(yaml_output);
        ((ScriptProcessorYAML) processor).setYamlContentPath("hcm-content/configuration/update");
        ((ScriptProcessorYAML) processor).setYamlConfigurationPath("hcm-config/configuration/update");

        if (yaml_output.exists()) {
            FileUtils.deleteDirectory(yaml_output);
            assertFalse(yaml_output.exists());
        }
        targetResource.mkdirs();
        Files.copy(resource.toPath(), targetResource.toPath(), StandardCopyOption.REPLACE_EXISTING);
        processor.processUpdateScripts(getScriptClasses(input));
        File yaml_expected = new File(getClass().getResource("/target_yaml").toURI());

        compareFolders(yaml_expected, yaml_output);
    }
}