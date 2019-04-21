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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.codehaus.plexus.util.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import nl.openweb.hippo.groovy.Generator;
import nl.openweb.hippo.groovy.ScriptClassFactory;
import nl.openweb.hippo.groovy.XmlGenerator;
import nl.openweb.hippo.groovy.annotations.Bootstrap;
import nl.openweb.hippo.groovy.model.ScriptClass;
import nl.openweb.hippo.groovy.model.jaxb.Node;
import static nl.openweb.hippo.groovy.Marshal.getMarshaller;
import static nl.openweb.hippo.groovy.ScriptClassFactory.getInterpretingClass;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class XmlGeneratorTest {

    final File sourceDir = new File(getClass().getResource("/").toURI());

    public XmlGeneratorTest() throws URISyntaxException {
    }

    @BeforeAll
    public void setup() {
        Generator.setDefaultContentRoot(Bootstrap.ContentRoot.QUEUE);
    }

    @Test
    public void testScrubbingAnnotations() throws JAXBException, IOException, URISyntaxException {
        checkGeneration("sub/annotatestrip");
    }

    @Test
    public void testUpdatescriptCreating() throws URISyntaxException, IOException, JAXBException {
        checkGeneration("updater");
        checkGeneration("updater2");
        checkGeneration("updater3");
        checkGeneration("sub/updater2");
        checkGeneration("sub/updater3");
        checkGeneration("updaterdata/updater4");
        checkGeneration("updaterdata/updater5");
    }

    private void checkGeneration(String name) throws URISyntaxException, IOException, JAXBException {
        URL testfileUrl = getClass().getResource(name + ".groovy");
        URL testfileResultUrl = getClass().getResource(name + ".xml");

        File file = new File(testfileUrl.toURI());
        File resultFile = new File(testfileResultUrl.toURI());

        Utilities.enforceWindowsFileEndings(file);

        Node updateScriptNode = XmlGenerator.getUpdateScriptNode(sourceDir, getInterpretingClass(file));
        StringWriter writer = new StringWriter();

        getMarshaller().marshal(updateScriptNode, writer);
        final String xml = writer.toString();

        String expectedContent = FileUtils.fileRead(resultFile);
        assertEquals("failed xml parsing of " + name, expectedContent, xml);
    }

    @Test
    public void generateHippoEcmExtensions() throws URISyntaxException, IOException, JAXBException {
        URI resourceURI = getClass().getResource("").toURI();
        File root = new File(resourceURI);
        List<ScriptClass> scriptClasses = ScriptClassFactory.getScriptClasses(root);
        Node node = XmlGenerator.getEcmExtensionNode(root, new File(root, "target"), scriptClasses, "my-updater-prefix-");

        StringWriter writer = new StringWriter();

        getMarshaller().marshal(node, writer);
        final String xml = writer.toString();
        URL testfileResultUrl = getClass().getResource("resulting-hippoecm-extension.xml");
        File resultFile = new File(testfileResultUrl.toURI());

        String expectedContent = FileUtils.fileRead(resultFile);
        assertEquals(expectedContent, xml);
    }

    @Test
    public void generateNewHippoEcmExtensions() throws URISyntaxException, IOException, JAXBException {
        URI resourceURI = getClass().getResource("sub").toURI();
        File root = new File(resourceURI);
        List<ScriptClass> scriptClasses = ScriptClassFactory.getScriptClasses(root);
        Node node = XmlGenerator.getEcmExtensionNode(root, new File(root, "target"), scriptClasses, "my-updater-prefix-");

        StringWriter writer = new StringWriter();

        getMarshaller().marshal(node, writer);
        final String xml = writer.toString();
        URL testfileResultUrl = getClass().getResource("sub-hippoecm-extension.xml");
        File resultFile = new File(testfileResultUrl.toURI());

        String expectedContent = FileUtils.fileRead(resultFile);
        assertEquals(expectedContent, xml);

        // Do it again, multiple runs shouldn't generate duplicates
        File intermediate = Files.createTempDirectory("test").toFile();
        FileWriter fileWriter = new FileWriter(new File(intermediate, "hippoecm-extension.xml"));
        getMarshaller().marshal(node, fileWriter);

        Node node2 = XmlGenerator.getEcmExtensionNode(root, intermediate, scriptClasses, "my-updater-prefix-");
        StringWriter writer2 = new StringWriter();
        intermediate.delete();
        getMarshaller().marshal(node2, writer2);
        final String xml2 = writer2.toString();

        assertEquals(xml, xml2);
    }

    @Test
    public void checkEcmExtensionGeneration() throws URISyntaxException, JAXBException, IOException, ClassNotFoundException {
        Generator.setDefaultContentRoot(Bootstrap.ContentRoot.REGISTRY);
        URI resourceURI = getClass().getResource("sub").toURI();
        File root = new File(resourceURI);
        List<ScriptClass> scriptClasses = ScriptClassFactory.getScriptClasses(root);
        Node node = XmlGenerator.getEcmExtensionNode(root, new File(root, "target"), scriptClasses, "my-updater-prefix-");

        StringWriter writer = new StringWriter();

        getMarshaller().marshal(node, writer);
        final String xml = writer.toString();
        URL testfileResultUrl = getClass().getResource("sub-hippoecm-extension.xml");
        File resultFile = new File(testfileResultUrl.toURI());

        String unExpectedContent = FileUtils.fileRead(resultFile);
        assertNotEquals(unExpectedContent, xml);
    }
}
