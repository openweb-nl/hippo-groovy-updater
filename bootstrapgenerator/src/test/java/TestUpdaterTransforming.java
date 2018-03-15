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
import org.junit.Before;
import org.junit.Test;

import nl.openweb.hippo.groovy.Generator;
import nl.openweb.hippo.groovy.ScriptClassFactory;
import nl.openweb.hippo.groovy.XmlGenerator;
import nl.openweb.hippo.groovy.YamlGenerator;
import nl.openweb.hippo.groovy.annotations.Bootstrap;
import nl.openweb.hippo.groovy.annotations.Exclude;
import nl.openweb.hippo.groovy.annotations.Updater;
import nl.openweb.hippo.groovy.model.ScriptClass;
import nl.openweb.hippo.groovy.model.jaxb.Node;
import static java.util.stream.Collectors.toList;
import static nl.openweb.hippo.groovy.Generator.getAnnotation;
import static nl.openweb.hippo.groovy.Generator.getAnnotationClasses;
import static nl.openweb.hippo.groovy.Generator.stripAnnotations;
import static nl.openweb.hippo.groovy.Marshal.getMarshaller;
import static nl.openweb.hippo.groovy.ScriptClassFactory.getInterpretingClass;
import static nl.openweb.hippo.groovy.XmlGenerator.getContentroot;
import static nl.openweb.hippo.groovy.YamlGenerator.getYamlString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;


public class TestUpdaterTransforming {

    @Before
    public void setup(){
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
    }

    public static void enforceWindowsFileEndings(File file) throws IOException {
        String content = FileUtils.fileRead(file);
        String lfContent = content.replaceAll("\r\n", "\n");
        FileUtils.fileWrite(file, lfContent.replaceAll("\n", "\r\n"));
    }

    private void checkGeneration(String name) throws URISyntaxException, IOException, JAXBException {
        URL testfileUrl = getClass().getResource(name + ".groovy");
        URL testfileResultUrl = getClass().getResource(name + ".xml");
        URL testfileResultUrlYaml = getClass().getResource(name + ".yaml");

        File file = new File(testfileUrl.toURI());
        File resultFile = new File(testfileResultUrl.toURI());
        File resultFileYaml = new File(testfileResultUrlYaml.toURI());

        enforceWindowsFileEndings(file);

        Node updateScriptNode = XmlGenerator.getUpdateScriptNode(getInterpretingClass(file));
        StringWriter writer = new StringWriter();

        getMarshaller().marshal(updateScriptNode, writer);
        final String xml = writer.toString();
        final String yaml = getYamlString(YamlGenerator.getUpdateYamlScript(getInterpretingClass(file)));

        String expectedContent = FileUtils.fileRead(resultFile);
        String expectedContentYaml = FileUtils.fileRead(resultFileYaml);
        assertEquals("failed xml parsing of " + name, expectedContent, xml);
        assertEquals("failed yaml parsing of " + name, expectedContentYaml, yaml);
    }

    @Test
    public void testStripAnnotations() throws URISyntaxException, IOException {
        URL testfileUrl = getClass().getResource("updater.groovy");
        URL testfileResultUrl = getClass().getResource("updater.groovy.stripped");

        File file = new File(testfileUrl.toURI());
        File resultFile = new File(testfileResultUrl.toURI());

        String content = FileUtils.fileRead(file);
        String expectedContent = FileUtils.fileRead(resultFile);

        assertEquals("failed stripping", expectedContent, stripAnnotations(content));

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
    public void generateHcmActions() throws URISyntaxException, IOException {
        URI resourceURI = getClass().getResource("").toURI();
        File root = new File(resourceURI);
        List<File> groovyFiles = Generator.getGroovyFiles(root);

        //registry or unversioned scripts
        List<ScriptClass> scriptClassesToReload = groovyFiles.stream().map(ScriptClassFactory::getInterpretingClass)
                .filter(this::isRegistryOrUnversioned)
                .collect(toList());

        String yaml = YamlGenerator.getHcmActionsList(root, new File(root, "target"), scriptClassesToReload);

        URL testfileResultUrl = getClass().getResource("resulting-hcm-actions.yaml");
        File resultFile = new File(testfileResultUrl.toURI());
        String expectedContent = FileUtils.fileRead(resultFile);
        assertEquals(expectedContent, yaml);
    }

    private boolean isRegistryOrUnversioned(ScriptClass scriptClass){
        Bootstrap bootstrap = scriptClass.getBootstrap(true);
        return bootstrap.reload() &&
                (bootstrap.version().isEmpty() || getContentroot(bootstrap).equals(Bootstrap.ContentRoot.REGISTRY));
    }

    @Test
    public void extractAnnotation() throws URISyntaxException, IOException {
        URL testfileUrl = getClass().getResource("updater.groovy");
        URL testfileUrl2 = getClass().getResource("updater-noimport.groovy");

        String content = FileUtils.fileRead(new File(testfileUrl.toURI()));
        String content2 = FileUtils.fileRead(new File(testfileUrl2.toURI()));

        String updater = getAnnotation(content, Updater.class.getSimpleName());
        String bootstrap = getAnnotation(content, Bootstrap.class.getSimpleName());

        String fullUpdater = getAnnotation(content, Updater.class.getName());
        String fullBootstrap = getAnnotation(content, Bootstrap.class.getName());

        String updater2 = getAnnotation(content2, Updater.class.getSimpleName());
        String bootstrap2 = getAnnotation(content2, Bootstrap.class.getSimpleName());

        String fullUpdater2 = getAnnotation(content2, Updater.class.getName());
        String fullBootstrap2 = getAnnotation(content2, Bootstrap.class.getName());


        String updaterExpected = "@Updater(name = \"Test Updater\",\n" +
                "        xpath = \"//element(*, hippo:document)\",\n" +
                " description=\"\", path = \"\", parameters = \" \")";
        String bootstrapExpected = "@Bootstrap(reload = true, sequence = 99999.0d)";
        String updaterFull = "@nl.openweb.hippo.groovy.annotations.Updater(name = \"Test Updater\",\n" +
                "        xpath = \"//element(*, hippo:document)\",\n" +
                " description=\"\", path = \"\", parameters = \" \")";
        String bootstrapFull = "@nl.openweb.hippo.groovy.annotations.Bootstrap(reload = true, sequence = 99999.0d)";
        String bootstrapFull2 = "@nl.openweb.hippo.groovy.annotations.Bootstrap(sequence = 99999.0d)";
        String updaterFull2 = "@nl.openweb.hippo.groovy.annotations.Updater(name = \"Test Updater noimport\",\n" +
                "        xpath = \"//element(*, hippo:document)\",\n" +
                " description=\"\", path = \"\", parameters = \" \")";


        assertEquals(updaterExpected, updater);
        assertEquals(bootstrapExpected, bootstrap);
        assertEquals("", fullUpdater);
        assertEquals("", fullBootstrap);
        assertEquals("", updater2);
        assertEquals("", bootstrap2);
        assertEquals(updaterFull2, fullUpdater2);
        assertEquals(bootstrapFull2, fullBootstrap2);
    }

    @Test
    public void getAnnotationClassesTest() throws Exception {
        List<Class<?>> classes = getAnnotationClasses();

        assertEquals(4, classes.size());
        assertTrue(classes.contains(Exclude.class));
        assertTrue(classes.contains(Bootstrap.class));
        assertTrue(classes.contains(Updater.class));
        assertTrue(classes.contains(Bootstrap.ContentRoot.class));
    }

    @Test
    public void checkDefaultingContentRootYamlFile() throws URISyntaxException, IOException, JAXBException {
        Generator.setDefaultContentRoot(Bootstrap.ContentRoot.REGISTRY);
        URL testfileUrl = getClass().getResource("updater.groovy");
        URL testfileResultUrlYaml = getClass().getResource("updater.yaml");

        File file = new File(testfileUrl.toURI());
        File resultFileYaml = new File(testfileResultUrlYaml.toURI());
        TestUpdaterTransforming.enforceWindowsFileEndings(file);

        final String yaml = getYamlString(YamlGenerator.getUpdateYamlScript(getInterpretingClass(file)));

        String unExpectedContentYaml = FileUtils.fileRead(resultFileYaml);
        assertNotEquals("failed yaml parsing of updater", unExpectedContentYaml, yaml);
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