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
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.codehaus.plexus.util.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import nl.openweb.hippo.groovy.Generator;
import nl.openweb.hippo.groovy.ScriptClassFactory;
import nl.openweb.hippo.groovy.YamlGenerator;
import nl.openweb.hippo.groovy.annotations.Bootstrap;
import nl.openweb.hippo.groovy.model.ScriptClass;
import static java.util.stream.Collectors.toList;
import static nl.openweb.hippo.groovy.Generator.getContentroot;
import static nl.openweb.hippo.groovy.ScriptClassFactory.getInterpretingClass;
import static nl.openweb.hippo.groovy.YamlGenerator.getYamlString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class YamlGeneratorTest {

    final File sourceDir = new File(getClass().getResource("/").toURI());

    public YamlGeneratorTest() throws URISyntaxException {
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
        URL testfileResultUrlYaml = getClass().getResource(name + ".yaml");

        File file = new File(testfileUrl.toURI());
        File resultFileYaml = new File(testfileResultUrlYaml.toURI());

        Utilities.enforceWindowsFileEndings(file);

        final String yaml = getYamlString(YamlGenerator.getUpdateYamlScript(sourceDir, getInterpretingClass(file)));

        String expectedContentYaml = FileUtils.fileRead(resultFileYaml);
        assertEquals("failed yaml parsing of " + name, expectedContentYaml, yaml);
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

    private boolean isRegistryOrUnversioned(ScriptClass scriptClass) {
        Bootstrap bootstrap = scriptClass.getBootstrap(true);
        return bootstrap.reload() &&
                (bootstrap.version().isEmpty() || getContentroot(bootstrap).equals(Bootstrap.ContentRoot.REGISTRY));
    }

    @Test
    public void checkDefaultingContentRootYamlFile() throws URISyntaxException, IOException, JAXBException {
        Generator.setDefaultContentRoot(Bootstrap.ContentRoot.REGISTRY);
        URL testfileUrl = getClass().getResource("updater.groovy");
        URL testfileResultUrlYaml = getClass().getResource("updater.yaml");

        File file = new File(testfileUrl.toURI());
        File resultFileYaml = new File(testfileResultUrlYaml.toURI());
        Utilities.enforceWindowsFileEndings(file);

        final String yaml = getYamlString(YamlGenerator.getUpdateYamlScript(sourceDir, getInterpretingClass(file)));

        String unExpectedContentYaml = FileUtils.fileRead(resultFileYaml);
        assertNotEquals("failed yaml parsing of updater", unExpectedContentYaml, yaml);
    }
}
