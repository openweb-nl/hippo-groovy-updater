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

import org.codehaus.plexus.util.FileUtils;
import org.junit.jupiter.api.Test;

import nl.openweb.hippo.groovy.ScriptClassFactory;
import nl.openweb.hippo.groovy.exception.ScriptParseException;
import nl.openweb.hippo.groovy.model.ScriptClass;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ScriptClassFactoryTest {

    private File getTempFileWithContentReplaced(String sourceFile, String targetString, String replaceString) throws URISyntaxException, IOException {
        URL testfileUrl = getClass().getResource(sourceFile);
        final String content = FileUtils.fileRead(new File(testfileUrl.toURI()));

        final File tempFile = File.createTempFile("updater", "mysuffix");
        tempFile.deleteOnExit();
        FileUtils.fileWrite(tempFile, content.replace(targetString, replaceString));
        return tempFile;
    }

    @Test
    public void checkValidUpdater() throws IOException, URISyntaxException {
        final File colonTempfile = getTempFileWithContentReplaced("updater.groovy",
                "Test Updater", "Test: updater");
        final File ColonTempFile2 = getTempFileWithContentReplaced("updater.groovy",
                "Test Updater", "Test:updater");
        assertThrows(ScriptParseException.class, () -> ScriptClassFactory.getInterpretingClass(colonTempfile));
        assertThrows(ScriptParseException.class, () -> ScriptClassFactory.getInterpretingClass(ColonTempFile2));

        final File slashTempFile = getTempFileWithContentReplaced("updater.groovy",
                "Test Updater", "Test/updater");
        assertThrows(ScriptParseException.class, () -> ScriptClassFactory.getInterpretingClass(slashTempFile));

        final File bracketTempFile = getTempFileWithContentReplaced("updater.groovy",
                "Test Updater", "Test[updater");
        assertThrows(ScriptParseException.class, () -> ScriptClassFactory.getInterpretingClass(bracketTempFile));

        final File bracketTempFile2 = getTempFileWithContentReplaced("updater.groovy",
                "Test Updater", "Test]updater");
        assertThrows(ScriptParseException.class, () -> ScriptClassFactory.getInterpretingClass(bracketTempFile2));

        final File wildcardTempFile = getTempFileWithContentReplaced("updater.groovy",
                "Test Updater", "Test*updater");
        assertThrows(ScriptParseException.class, () -> ScriptClassFactory.getInterpretingClass(wildcardTempFile));
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
}
