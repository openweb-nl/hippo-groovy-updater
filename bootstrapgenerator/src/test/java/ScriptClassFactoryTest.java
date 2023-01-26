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
import java.nio.charset.Charset;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import nl.openweb.hippo.groovy.ScriptClassFactory;
import nl.openweb.hippo.groovy.exception.ScriptParseException;
import nl.openweb.hippo.groovy.model.ScriptClass;
import static nl.openweb.hippo.groovy.Generator.stripAnnotations;
import static nl.openweb.hippo.groovy.ScriptClassFactory.readFileEnsuringLinuxLineEnding;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ScriptClassFactoryTest {

    private void testWithName(String updaterName) throws IOException, URISyntaxException {

        URL testfileUrl = getClass().getResource("updater.groovy");
        final String content = readFileEnsuringLinuxLineEnding(new File(testfileUrl.toURI()));

        final File tempFile = File.createTempFile("updater", "mysuffix");
        tempFile.deleteOnExit();
        FileUtils.writeStringToFile(tempFile,content.replace("Test Updater", updaterName), Charset.defaultCharset());

        ScriptClassFactory.getInterpretingClass(tempFile);
    }

    @Test
    public void checkValidUpdater() throws IOException, URISyntaxException {
        testWithName("Test updater with no other things");
        assertThrows(ScriptParseException.class, () -> testWithName("Test: updater"));
        assertThrows(ScriptParseException.class, () -> testWithName("Test:updater"));
        assertThrows(ScriptParseException.class, () -> testWithName("Test/updater"));
        assertThrows(ScriptParseException.class, () -> testWithName("Test[updater"));
        assertThrows(ScriptParseException.class, () -> testWithName("Test]updater"));
        assertThrows(ScriptParseException.class, () -> testWithName("Test*updater"));
    }

    @Test
    public void checkValidUpdaterNameForNull() {
        final File tempFile = new File("nonexistentfile");
        final ScriptClass scriptClass = ScriptClassFactory.getInterpretingClass(tempFile);
        assertNull(scriptClass);
    }

    @Test
    public void checkValidUpdaterNameForNonUpdater() throws URISyntaxException {
        URL testfileUrl = getClass().getResource("updater.groovy.stripped");
        final ScriptClass scriptClass = ScriptClassFactory.getInterpretingClass(new File(testfileUrl.toURI()));
        assertNotNull(scriptClass);
        assertNull(scriptClass.getUpdater());
    }

    @Test
    public void testStripAnnotations() throws URISyntaxException, IOException {
        URL testfileUrl = getClass().getResource("updater.groovy");
        URL testfileResultUrl = getClass().getResource("updater.groovy.stripped");

        File file = new File(testfileUrl.toURI());
        File resultFile = new File(testfileResultUrl.toURI());

        String content = readFileEnsuringLinuxLineEnding(file);
        String expectedContent = readFileEnsuringLinuxLineEnding(resultFile);

        assertEquals(expectedContent, stripAnnotations(content), "failed stripping");
    }

    @ParameterizedTest
    @ValueSource(strings = {"updater.groovy", "edge-cases/CleanupPermanentFormdata.groovy.content"})
    public void testInterpreting(String source) throws URISyntaxException, IOException {
        URL testfileUrl = getClass().getResource(source);
        URL testfileResultUrl = getClass().getResource(source + ".stripped");
        URL testfileSpacedResultUrl = getClass().getResource(source + ".spaced.stripped");

        File file = new File(testfileUrl.toURI());
        File resultFile = new File(testfileResultUrl.toURI());
        File spacedResultFile = new File(testfileSpacedResultUrl.toURI());

        final ScriptClass interpretingClassScrubbed = ScriptClassFactory.getInterpretingClass(file);
        final ScriptClass interpretingClassUnscrubbed = ScriptClassFactory.getInterpretingClass(file, true);

        String expectedScrubbedContent = readFileEnsuringLinuxLineEnding(resultFile);
        String expectedUnscrubbedContent = readFileEnsuringLinuxLineEnding(spacedResultFile);

        assertEquals(expectedScrubbedContent, interpretingClassScrubbed.getContent(), "Content is not well scrubbed");
        assertEquals(expectedUnscrubbedContent, interpretingClassUnscrubbed.getContent(), "Content is not correct");
    }
}
