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
import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.commons.lang3.tuple.Pair;
import org.codehaus.plexus.util.FileUtils;
import org.junit.Test;

import nl.openweb.hippo.groovy.Generator;
import nl.openweb.hippo.groovy.XmlGenerator;
import nl.openweb.hippo.groovy.YamlGenerator;
import nl.openweb.hippo.groovy.annotations.Bootstrap;
import nl.openweb.hippo.groovy.annotations.Updater;
import nl.openweb.hippo.groovy.model.jaxb.Node;
import static java.util.stream.Collectors.toList;
import static nl.openweb.hippo.groovy.Generator.getAnnotation;
import static nl.openweb.hippo.groovy.Generator.getAnnotationClasses;
import static nl.openweb.hippo.groovy.Generator.getBootstrap;
import static nl.openweb.hippo.groovy.Generator.getFullAnnotation;
import static nl.openweb.hippo.groovy.Generator.stripAnnotations;
import static nl.openweb.hippo.groovy.Marshal.getMarshaller;
import static nl.openweb.hippo.groovy.YamlGenerator.getYamlString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class TestUpdaterTransforming {
    @Test
    public void testXmlUpdatescriptCreating() throws URISyntaxException, IOException, JAXBException {
        checkGeneration("updater");
        checkGeneration("updater2");
        checkGeneration("updater3");
    }

    private void checkGeneration(String name) throws URISyntaxException, IOException, JAXBException {
        URL testfileUrl = getClass().getResource(name + ".groovy");
        URL testfileResultUrl = getClass().getResource(name + ".xml");
        URL testfileResultUrlYaml = getClass().getResource(name + ".yaml");

        File file = new File(testfileUrl.toURI());
        File resultFile = new File(testfileResultUrl.toURI());
        File resultFileYaml = new File(testfileResultUrlYaml.toURI());


        Node updateScriptNode = XmlGenerator.getUpdateScriptNode(file);
        StringWriter writer = new StringWriter();

        getMarshaller().marshal(updateScriptNode, writer);
        final String xml = writer.toString();
        final String yaml = getYamlString(YamlGenerator.getUpdateYamlScript(file));

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

        assertEquals("failed stripping", expectedContent, stripAnnotations(content, getAnnotationClasses()));

    }

    @Test
    public void generateHippoEcmExtensions() throws URISyntaxException, IOException, JAXBException {
        URI resourceURI = getClass().getResource("").toURI();
        File root = new File(resourceURI);
        List<File> groovyFiles = Generator.getGroovyFiles(root);
        Node node = XmlGenerator.getEcmExtensionNode(root, new File(root, "target"), groovyFiles, "my-updater-prefix-");

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
        List<File> groovyFiles = Generator.getGroovyFiles(root);
        Node node = XmlGenerator.getEcmExtensionNode(root, new File(root, "target"), groovyFiles, "my-updater-prefix-");

        StringWriter writer = new StringWriter();

        getMarshaller().marshal(node, writer);
        final String xml = writer.toString();
        URL testfileResultUrl = getClass().getResource("sub-hippoecm-extension.xml");
        File resultFile = new File(testfileResultUrl.toURI());

        String expectedContent = FileUtils.fileRead(resultFile);
        assertEquals(expectedContent, xml);
    }

    @Test
    public void generateHcmActions() throws URISyntaxException, IOException {
        URI resourceURI = getClass().getResource("").toURI();
        File root = new File(resourceURI);
        List<File> groovyFiles = Generator.getGroovyFiles(root);

        //registry or unversioned scripts
        List<Pair<File, Bootstrap>> reloadByActionList = groovyFiles.stream().map(file -> Pair.of(file, getBootstrap(file)))
                .filter(pair -> pair.getValue() != null &&
                        pair.getValue().reload())
                .filter(pair -> pair.getValue().contentroot().equals(Bootstrap.ContentRoot.REGISTRY) ||
                        pair.getValue().version().isEmpty())
                .collect(toList());

        String yaml = YamlGenerator.getHcmActionsList(root, new File(root, "target"), reloadByActionList);

        URL testfileResultUrl = getClass().getResource("resulting-hcm-actions.yaml");
        File resultFile = new File(testfileResultUrl.toURI());
        String expectedContent = FileUtils.fileRead(resultFile);
        assertEquals(expectedContent, yaml);
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

        assertEquals(updaterFull, getFullAnnotation(content, Updater.class));
        assertEquals(updaterFull2, getFullAnnotation(content2, Updater.class));
        assertEquals(bootstrapFull, getFullAnnotation(content, Bootstrap.class));
        assertEquals(bootstrapFull2, getFullAnnotation(content2, Bootstrap.class));
    }

    @Test
    public void getAnnotationClassesTest() throws Exception {
        List<Class<?>> classes = getAnnotationClasses();

        assertEquals(3, classes.size());
        assertTrue(classes.contains(Bootstrap.class));
        assertTrue(classes.contains(Updater.class));
        assertTrue(classes.contains(Bootstrap.ContentRoot.class));
    }


}