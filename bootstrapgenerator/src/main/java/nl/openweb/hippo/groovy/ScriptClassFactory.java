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
import static nl.openweb.hippo.groovy.Generator.getAnnotationClasses;

public class ScriptClassFactory {
    private static final String LINE_END_WINDOWS = "\r\n";
    private static final String LINE_END_LINUX = "\n";
    private static final String LINE_END_MAC = "\r";
    private static final NamespaceMapping namespaceResolver = new NamespaceMapping();
    private static final NameFactory nameFactory = NameFactoryImpl.getInstance();
    private static GroovyClassLoader groovyClassLoader = new GroovyClassLoader();

    private ScriptClassFactory() {
        //No instantiating of this class
    }

    private static NamespaceMapping getNamespaceResolver() throws NamespaceException {
        if(!namespaceResolver.hasPrefix("")){
            namespaceResolver.setMapping("", "");
        }
        return namespaceResolver;
    }

    /**
     * Returns a class that has actually nothing but the Bootstrap and Updater Annotations
     *
     * @param file the file to make a class representation of
     * @return a fake class with the Bootstrap and Updater annotations
     */
    public static ScriptClass getInterpretingClass(final File file) {
        final ScriptClass scriptClass = getInterpretingClassStrippingCode(file);
        validateScriptClass(scriptClass);
        return scriptClass;
    }

    private static void validateScriptClass(final ScriptClass scriptClass) {
        if (scriptClass == null || scriptClass.getUpdater() == null) {
            return;
        }
        final String name = scriptClass.getUpdater().name();

        try {
            NameParser.parse(name, getNamespaceResolver(), nameFactory);
        } catch (Exception e) {
            throw new ScriptParseException("Error parsing the updater name for: " + scriptClass.getFile().getAbsolutePath(), e);
        }
    }

    public static ScriptClass getInterpretingClassStrippingCode(final File file) {
        groovyClassLoader.clearCache();
        String script;
        try {
            script = readFileEnsuringLinuxLineEnding(file);

            String imports = getAnnotationClasses().stream()
                    .map(clazz -> "import " + clazz.getCanonicalName() + ";")
                    .collect(joining());

            String interpretCode = imports + script.replaceAll("import .+\n", "")
                    .replaceAll("package\\s.*\n", "")
                    .replaceAll("extends\\s.*\\{[^\\u001a]*", "{}");

            interpretCode = scrubAnnotations(interpretCode);

            return new ScriptClass(file, groovyClassLoader.parseClass(interpretCode), script);
        } catch (IOException e) {
            return null;
        }
    }

    public static String readFileEnsuringLinuxLineEnding(final File file) throws IOException {
        String content = FileUtils.fileRead(file);
        if (content.contains(LINE_END_WINDOWS)) {
            return content.replaceAll(LINE_END_WINDOWS, LINE_END_LINUX)
                    .replaceAll(LINE_END_MAC, LINE_END_LINUX);
        }
        return content;
    }

    private static String scrubAnnotations(final String interpretCode) {
        String possibleAnnotationNames = getAnnotationClasses().stream()
                .map(annotation -> annotation.getCanonicalName().replace(".", "\\.") + "|" + annotation.getSimpleName())
                .collect(joining("|"));
        return interpretCode.replaceAll("@((?!" + possibleAnnotationNames + ")[\\w]+)([\\s]+|(\\([^\\)]*\\)))", "");
    }

    public static List<ScriptClass> getScriptClasses(File sourceDir) {
        return Generator.getGroovyFiles(sourceDir).stream().map(ScriptClassFactory::getInterpretingClass)
                .filter(script -> script.isValid() && !script.isExcluded()).collect(toList());
    }
}
