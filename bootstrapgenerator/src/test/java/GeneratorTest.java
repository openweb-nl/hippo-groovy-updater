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
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import nl.openweb.hippo.groovy.Generator;
import nl.openweb.hippo.groovy.ScriptClassFactory;
import nl.openweb.hippo.groovy.annotations.Bootstrap;
import nl.openweb.hippo.groovy.annotations.Exclude;
import nl.openweb.hippo.groovy.annotations.Updater;
import static nl.openweb.hippo.groovy.Generator.getAnnotation;
import static nl.openweb.hippo.groovy.Generator.getAnnotationClasses;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GeneratorTest {

    @BeforeAll
    public void setup() {
        Generator.setDefaultContentRoot(Bootstrap.ContentRoot.QUEUE);
    }

    @Test
    public void extractAnnotation() throws URISyntaxException, IOException {
        URL testfileUrl = getClass().getResource("updater.groovy");
        URL testfileUrl2 = getClass().getResource("updater-noimport.groovy");

        String content = ScriptClassFactory.readFileEnsuringLinuxLineEnding(new File(testfileUrl.toURI()));
        String content2 = ScriptClassFactory.readFileEnsuringLinuxLineEnding(new File(testfileUrl2.toURI()));

        String updater = getAnnotation(content, Updater.class.getSimpleName());
        String bootstrap = getAnnotation(content, Bootstrap.class.getSimpleName());

        String fullUpdater = getAnnotation(content, Updater.class.getName());
        String fullBootstrap = getAnnotation(content, Bootstrap.class.getName());

        String updater2 = getAnnotation(content2, Updater.class.getSimpleName());
        String bootstrap2 = getAnnotation(content2, Bootstrap.class.getSimpleName());

        String fullUpdater2 = getAnnotation(content2, Updater.class.getName());
        String fullBootstrap2 = getAnnotation(content2, Bootstrap.class.getName());

        String descriptionExample = "'''This script can be used to do anything.\n" +
                "            (It should allow any notations, for the stripping etc..\n" +
                "            for example a description on how the XPath query should be like //element(*, hippo:document)[mixin:types='project:example']\n" +
                "            or the parameters field, describing like: { \"foobar\": [ \"bar\", \"foo\"]}'''";
        String updaterExpected = "@Updater(name = \"Test Updater\",\n" +
                "        xpath = \"//element(*, hippo:document)\",\n" +
                " description=" + descriptionExample + ", path = \"\", parameters = \" \")";
        String bootstrapExpected = "@Bootstrap(reload = true, sequence = 99999.0d)";
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
}
