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

package nl.openweb.hippo.groovy;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Properties;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.xml.bind.JAXBException;

import org.apache.sling.testing.mock.jcr.MockJcr;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import static nl.openweb.hippo.groovy.model.Constants.NodeType.HIPPOSYS_UPDATERINFO;
import static nl.openweb.hippo.groovy.util.WatchFilesUtils.SCRIPT_ROOT;
import static org.junit.jupiter.api.Assertions.assertEquals;

class GroovyFilesServiceImplTest {
    private GroovyFilesServiceImpl service = new GroovyFilesServiceImpl();
    private Session session;
    private Node scriptRoot;

    @BeforeEach
    void setup() throws RepositoryException {
        session = MockJcr.newSession();
        final Node rootNode = session.getRootNode();
        Node node = rootNode;
        for (final String name : SCRIPT_ROOT.split("/")) {
            node = node.addNode(name);
        }
        scriptRoot = session.getNode(SCRIPT_ROOT);
    }

    @Test
    void importGroovyFileReimport() throws URISyntaxException, RepositoryException, JAXBException, IOException {
        URL testfileUrl = getClass().getResource("/updater.groovy");
        URL testfileProperties = getClass().getResource("/updater.properties");

        File file = new File(testfileUrl.toURI());
        final Node existingNode = scriptRoot.addNode("Test Updater", HIPPOSYS_UPDATERINFO);
        scriptRoot.addNode("Updater Test bogus", HIPPOSYS_UPDATERINFO);
        service.importGroovyFile(session, file);

        Properties testProperties = new Properties();
        testProperties.load(new FileReader(testfileProperties.getFile()));
        assertProps(testProperties, existingNode);
    }

    @Test
    void importGroovyFile() throws URISyntaxException, RepositoryException, JAXBException, IOException {
        URL testfileUrl = getClass().getResource("/updater.groovy");
        URL testfileProperties = getClass().getResource("/updater.properties");

        File file = new File(testfileUrl.toURI());

        service.importGroovyFile(session, file);
        final Node updaterNode = scriptRoot.getNodes().nextNode();
        Properties testProperties = new Properties();
        testProperties.load(new FileReader(testfileProperties.getFile()));
        assertProps(testProperties, updaterNode);
    }

    @Test
    void importGroovyFileScript() throws URISyntaxException, RepositoryException, JAXBException, IOException {
        URL testfileUrl = getClass().getResource("/updater-script.groovy");
        URL testfileProperties = getClass().getResource("/updater.properties");
        File file = new File(testfileUrl.toURI());

        service.importGroovyFile(session, file);
        final Node updaterNode = scriptRoot.getNodes().nextNode();
        Properties testProperties = new Properties();
        testProperties.load(new FileReader(testfileProperties.getFile()));
        //just not the xpath
        testProperties.remove("hipposys:query");
        assertProps(testProperties, updaterNode);
    }

    @Test
    void importGroovyFileWithParametersFile() throws URISyntaxException, RepositoryException, JAXBException, IOException {
        URL testfileUrl = getClass().getResource("/updater-ext.groovy");
        URL testfileProperties = getClass().getResource("/updater.properties");

        File file = new File(testfileUrl.toURI());

        service.importGroovyFile(session, file);

        final Node updaterNode = scriptRoot.getNodes().nextNode();
        Properties testProperties = new Properties();
        testProperties.load(new FileReader(testfileProperties.getFile()));
        assertProps(testProperties, updaterNode);
    }

    void assertProps(Properties expected, Node node) throws RepositoryException {
        final PropertyIterator properties = node.getProperties();
        if (expected.size() != properties.getSize()) {
            throw new AssertionFailedError("Amount of properties varies", expected.size(), properties.getSize());
        }
        while (properties.hasNext()) {
            final Property property = properties.nextProperty();
            assertEquals(expected.getProperty(property.getName()), property.getString(), "Mismatch of property: " + property.getName());
        }
    }
}