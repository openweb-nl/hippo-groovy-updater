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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.plexus.util.FileUtils;

import groovy.lang.GroovyClassLoader;
import nl.openweb.hippo.groovy.annotations.Bootstrap;
import nl.openweb.hippo.groovy.annotations.Updater;
import static nl.openweb.hippo.groovy.model.Constants.Files.GROOVY_EXTENSION;

public abstract class Generator {
    private static final String REGEX_WHITESPACE = "\\s*";
    private static final String REGEX_ATTR_NAME = "([A-Za-z]\\w*)";
    private static final String REGEX_ATTR_VALUE = "((\"[^\"]*\")|[^\\)]|true|false)*";
    private static final String REGEX_ATTRIBUTES = REGEX_WHITESPACE + REGEX_ATTR_NAME + REGEX_WHITESPACE + "=" + REGEX_WHITESPACE + REGEX_ATTR_VALUE + REGEX_WHITESPACE;
    public static final String NEWLINE = "\n";

    private static final GroovyClassLoader gcl = new GroovyClassLoader();

    /**
     * Returns a class that has actually nothing but the Bootstrap and Updater Annotations
     *
     * @param file the file to make a class representation of
     * @return a fake class with the Bootstrap and Updater annotations
     * @throws IOException
     */
    public static Class getInterpretingClass(final File file) throws IOException {
        gcl.clearCache();
        String script = FileUtils.fileRead(file);
        String interpretCode = "import " + Bootstrap.class.getCanonicalName() + ";";
        interpretCode += "import " + Bootstrap.ContentRoot.class.getCanonicalName() + ";";
        interpretCode += getFullAnnotation(script, Updater.class) + getFullAnnotation(script, Bootstrap.class);
        interpretCode += "class Interpreting { }";
        return gcl.parseClass(interpretCode);
    }

    /**
     * Add a classpath to the groovy parsing engine, for example if the groovy script uses classes from within the
     * project
     *
     * @param path path to add to the classpath
     */
    public static void addClassPath(final String path) {
        gcl.addClasspath(path);
    }

    public static String stripAnnotations(final String script, final Class<?>... classes) {
        String result = script;
        for (final Class<?> aClass : classes) {
            if (result.contains(aClass.getPackage().getName()) &&
                    result.contains(aClass.getSimpleName())) {
                result = stripAnnotation(result, aClass.getSimpleName());
                result = stripAnnotation(result, aClass.getCanonicalName());
                result = result.replaceAll("import\\s*" + aClass.getCanonicalName() + "\\s*[;]?\n", "");
            }
        }
        return result;
    }

    private static String stripAnnotation(final String script, final String className) {
        final String annotationName = "@" + className;
        final String regex = annotationName + REGEX_WHITESPACE + "(\\((" + REGEX_ATTRIBUTES + ")?\\))?"; //seems usefull need to eliminate in-string parentheses
        String s = script.replaceAll(regex, NEWLINE);
        s = s.replaceAll("(\n){3,}", "\n\n");
        return s;
    }

    public static String getAnnotation(final String script, final String className) {
        final String annotationName = "@" + className;
        final String regex = annotationName + REGEX_WHITESPACE + "(\\((" + REGEX_ATTRIBUTES + ")?\\))?"; //seems usefull need to eliminate in-string parentheses
        Matcher matcher = Pattern.compile(regex).matcher(script);
        return matcher.find() ? matcher.group() : StringUtils.EMPTY;
    }

    public static String getFullAnnotation(final String script, final Class clazz) {
        String simple = getAnnotation(script, clazz.getSimpleName());

        return StringUtils.isNotBlank(simple) ?
                simple.replaceFirst("@" + clazz.getSimpleName(), "@" + clazz.getName()) :
                getAnnotation(script, clazz.getName());
    }

    /**
     * Obtain groovy files from given location
     *
     * @param dir directory to obtain groovy files from
     * @return List of groovy files
     */
    public static List<File> getGroovyFiles(final File dir) {
        final File[] groovyFiles = dir.listFiles((file) -> file.isFile() && file.getName().endsWith(GROOVY_EXTENSION));
        final File[] directories = dir.listFiles(File::isDirectory);
        final List<File> allFiles = new ArrayList<>();
        if (groovyFiles != null) {
            allFiles.addAll(Arrays.asList(groovyFiles));
        }
        if (directories != null) {
            Arrays.stream(directories).map(Generator::getGroovyFiles).forEach(allFiles::addAll);
        }
        return Collections.unmodifiableList(allFiles);
    }

    public static final Updater getUpdater(final File file){
        final Updater updater;
        try {
            final Class scriptClass = getInterpretingClass(file);
            updater = (Updater) scriptClass.getDeclaredAnnotation(Updater.class);
        } catch (final IOException e) {
            return null;
        }
        return updater;
    }
}
