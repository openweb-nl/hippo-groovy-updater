import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.codehaus.plexus.util.FileUtils;
import org.junit.Test;

import nl.openweb.hippo.groovy.XmlGenerator;
import nl.openweb.hippo.groovy.annotations.Bootstrap;
import nl.openweb.hippo.groovy.annotations.Updater;
import nl.openweb.hippo.groovy.model.jaxb.Node;
import static nl.openweb.hippo.groovy.Marshal.getMarshaller;
import static nl.openweb.hippo.groovy.XmlGenerator.stripAnnotations;
import static org.junit.Assert.assertEquals;


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

        File file = new File(testfileUrl.toURI());
        File resultFile = new File(testfileResultUrl.toURI());


        Node updateScriptNode = XmlGenerator.getUpdateScriptNode(file);
        StringWriter writer = new StringWriter();

        getMarshaller().marshal(updateScriptNode, writer);
        final String xml = writer.toString();

        String expectedContent = FileUtils.fileRead(resultFile);
        assertEquals("failed parsing of " + name, expectedContent, xml);
    }

    @Test
    public void testStripAnnotations() throws URISyntaxException, IOException {
        URL testfileUrl = getClass().getResource("updater.groovy");
        URL testfileResultUrl = getClass().getResource("updater.groovy.stripped");

        File file = new File(testfileUrl.toURI());
        File resultFile = new File(testfileResultUrl.toURI());

        String content = FileUtils.fileRead(file);
        String expectedContent = FileUtils.fileRead(resultFile);

        assertEquals("failed stripping", expectedContent, stripAnnotations(content, Bootstrap.class, Updater.class));
        assertEquals("failed stripping", expectedContent, stripAnnotations(content, Updater.class, Bootstrap.class));

    }

    @Test
    public void generateHippoEcmExtensions() throws URISyntaxException, IOException, JAXBException {
        URI resourceURI = getClass().getResource("").toURI();
        File root = new File(resourceURI);
        List<File> groovyFiles = XmlGenerator.getGroovyFiles(root);
        Node node = XmlGenerator.getEcmExtensionNode(root, groovyFiles, "my-updater-prefix-");

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
        List<File> groovyFiles = XmlGenerator.getGroovyFiles(root);
        Node node = XmlGenerator.getEcmExtensionNode(root, groovyFiles, "my-updater-prefix-");

        StringWriter writer = new StringWriter();

        getMarshaller().marshal(node, writer);
        final String xml = writer.toString();
        URL testfileResultUrl = getClass().getResource("sub-hippoecm-extension.xml");
        File resultFile = new File(testfileResultUrl.toURI());

        String expectedContent = FileUtils.fileRead(resultFile);
        assertEquals(expectedContent, xml);
    }

}
