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
import java.io.IOException;
import java.util.List;

import javax.jcr.NamespaceException;

import org.apache.jackrabbit.spi.NameFactory;
import org.apache.jackrabbit.spi.commons.conversion.NameParser;
import org.apache.jackrabbit.spi.commons.name.NameFactoryImpl;
import org.apache.jackrabbit.spi.commons.namespace.NamespaceMapping;
import org.codehaus.plexus.util.FileUtils;

import groovy.lang.GroovyClassLoader;
import nl.openweb.hippo.groovy.exception.ScriptParseException;
import nl.openweb.hippo.groovy.model.ScriptClass;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static nl.openweb.hippo.groovy.Generator.NEWLINE;
import static nl.openweb.hippo.groovy.Generator.getAnnotationClasses;
import static nl.openweb.hippo.groovy.Generator.getAnnotations;
import static nl.openweb.hippo.groovy.Generator.stripAnnotations;

public class ScriptClassFactory {
    private static final String LINE_END_WINDOWS = "\r\n";
    private static final String LINE_END_LINUX = "\n";
    private static final String LINE_END_MAC = "\r";
    private static final NamespaceMapping NAMESPACE_MAPPING = new NamespaceMapping();
    private static final NameFactory NAME_FACTORY = NameFactoryImpl.getInstance();
    private static final GroovyClassLoader GROOVY_CLASS_LOADER = new GroovyClassLoader();

    private ScriptClassFactory() {
        //No instantiating of this class
    }

    private static NamespaceMapping getNamespaceMapping() throws NamespaceException {
        if (!NAMESPACE_MAPPING.hasPrefix("")) {
            NAMESPACE_MAPPING.setMapping("", "");
        }
        return NAMESPACE_MAPPING;
    }

    private static void validateScriptClass(final ScriptClass scriptClass) {
        if (scriptClass == null || scriptClass.getUpdater() == null) {
            return;
        }
        final String name = scriptClass.getUpdater().name();

        try {
            NameParser.parse(name, getNamespaceMapping(), NAME_FACTORY);
        } catch (Exception e) {
            throw new ScriptParseException("Error parsing the updater name for: " + scriptClass.getFile().getAbsolutePath(), e);
        }
    }

    /**
     * Returns a class that has actually nothing but the Bootstrap and Updater Annotations
     *
     * @param file the file to make a class representation of
     * @return a fake class with the Bootstrap and Updater annotations
     */
    public static ScriptClass getInterpretingClass(final File file) {
        return getInterpretingClass(file, false);
    }

    /**
     * Returns a class that has actually nothing but the Bootstrap and Updater Annotations
     *
     * @param file          the file to make a class representation of
     * @param keepLineCount keep linecount when stripping the annotations in the scriptcontent
     * @return a fake class with the Bootstrap and Updater annotations
     */
    public static ScriptClass getInterpretingClass(final File file, final boolean keepLineCount) {
        GROOVY_CLASS_LOADER.clearCache();
        String script;
        try {
            script = readFileEnsuringLinuxLineEnding(file);

            String imports = getAnnotationClasses().stream()
                .map(clazz -> "import " + clazz.getCanonicalName() + ";" + LINE_END_LINUX)
                .collect(joining());
            String interpretCode = imports + getAnnotations(script).stream().collect(joining(LINE_END_LINUX)) + LINE_END_LINUX + "class InterpretClass {}";
            script = stripAnnotations(script, keepLineCount);
            final ScriptClass scriptClass = new ScriptClass(file, GROOVY_CLASS_LOADER.parseClass(interpretCode), script);
            validateScriptClass(scriptClass);
            return scriptClass;
        } catch (IOException e) {
            return null;
        }
    }

    public static String readFileEnsuringLinuxLineEnding(final File file) throws IOException {
        String content = FileUtils.fileRead(file);
        if (content.contains(LINE_END_MAC)) {
            content = content.replace(LINE_END_WINDOWS, LINE_END_LINUX)
                .replace(LINE_END_MAC, LINE_END_LINUX);
        }
        return content.replaceAll("[\\t ]+" + NEWLINE, NEWLINE);
    }

    public static List<ScriptClass> getScriptClasses(File sourceDir) {
        return Generator.getGroovyFiles(sourceDir).stream().map(ScriptClassFactory::getInterpretingClass)
            .filter(script -> script.isValid() && !script.isExcluded()).collect(toList());
    }
}
