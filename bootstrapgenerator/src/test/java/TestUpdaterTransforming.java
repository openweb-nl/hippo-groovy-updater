import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import javax.xml.bind.JAXB;

import org.codehaus.plexus.util.FileUtils;
import org.junit.Test;

import nl.openweb.hippo.groovy.XmlGenerator;
import nl.openweb.hippo.groovy.model.jaxb.Node;
import static org.junit.Assert.assertEquals;


public class TestUpdaterTransforming {
    @Test
    public void testXmlUpdatescriptCreating() throws URISyntaxException, IOException {
        checkGeneration("updater");
        checkGeneration("updater2");
    }

    private void checkGeneration(String name) throws URISyntaxException, IOException {
        URL testfileUrl = getClass().getResource(name + ".groovy");
        URL testfileResultUrl = getClass().getResource(name + ".xml");

        File file = new File(testfileUrl.toURI());
        File resultFile = new File(testfileResultUrl.toURI());


        Node updateScriptNode = XmlGenerator.getUpdateScriptNode(file);
        StringWriter writer = new StringWriter();

        JAXB.marshal(updateScriptNode, writer);
        final String xml=writer.toString();

        String expectedContent = FileUtils.fileRead(resultFile);
        assertEquals("failed parsing of " + name, expectedContent, xml);
    }


    @Test
    public void generateHippoEcmExtensions() throws URISyntaxException, IOException {
        URI resourceURI = getClass().getResource("").toURI();
        File root = new File(resourceURI);
        List<File> groovyFiles = XmlGenerator.getGroovyFiles(root);
        Node node = XmlGenerator.getEcmExtensionNode(root, groovyFiles, "my-updater-prefix-");

        StringWriter writer = new StringWriter();

        JAXB.marshal(node, writer);
        final String xml=writer.toString();
        URL testfileResultUrl = getClass().getResource("hippoecm-extension.xml");
        File resultFile = new File(testfileResultUrl.toURI());

        String expectedContent = FileUtils.fileRead(resultFile);
        assertEquals(expectedContent, xml);
    }
}
