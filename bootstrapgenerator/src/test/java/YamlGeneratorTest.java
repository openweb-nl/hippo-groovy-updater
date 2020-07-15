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
import java.net.URISyntaxException;
import java.net.URL;

import javax.xml.bind.JAXBException;

import org.codehaus.plexus.util.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import nl.openweb.hippo.groovy.Generator;
import nl.openweb.hippo.groovy.YamlGenerator;
import nl.openweb.hippo.groovy.annotations.Bootstrap;
import static nl.openweb.hippo.groovy.ScriptClassFactory.getInterpretingClass;
import static nl.openweb.hippo.groovy.ScriptClassFactory.readFileEnsuringLinuxLineEnding;
import static nl.openweb.hippo.groovy.YamlGenerator.getYamlString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class YamlGeneratorTest {

    final File sourceDir = new File(getClass().getResource("/").toURI());

    public YamlGeneratorTest() throws URISyntaxException {
    }

    @BeforeEach
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
        checkGeneration("updaterdata/updater6");
        checkGeneration("updaterdata/updater7");
    }

    private void checkGeneration(String name) throws URISyntaxException, IOException, JAXBException {
        URL testfileUrl = getClass().getResource(name + ".groovy");
        URL testfileResultUrlYaml = getClass().getResource(name + ".yaml");

        File file = new File(testfileUrl.toURI());
        File resultFileYaml = new File(testfileResultUrlYaml.toURI());

        Utilities.enforceWindowsFileEndings(file);

        final String yaml = getYamlString(YamlGenerator.getUpdateYamlScript(sourceDir, getInterpretingClass(file)));

        String expectedContentYaml = readFileEnsuringLinuxLineEnding(resultFileYaml);
        assertEquals(expectedContentYaml, yaml, "failed yaml parsing of " + name);
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
