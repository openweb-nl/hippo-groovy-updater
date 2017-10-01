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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import groovy.lang.GroovyClassLoader;
import nl.openweb.hippo.groovy.annotations.Exclude;
import nl.openweb.hippo.groovy.annotations.Bootstrap;
import nl.openweb.hippo.groovy.annotations.Updater;
import static nl.openweb.hippo.groovy.model.Constants.Files.GROOVY_EXTENSION;

public abstract class Generator {
    public static final String NEWLINE = "\n";
    private static final String REGEX_WHITESPACE = "\\s*";
    private static final String REGEX_ATTR_NAME = "([A-Za-z]\\w*)";
    private static final String REGEX_ATTR_VALUE = "((\"[^\"]*\")|[^\\)]|true|false)*";
    private static final String REGEX_ATTRIBUTES = REGEX_WHITESPACE + REGEX_ATTR_NAME + REGEX_WHITESPACE + "=" + REGEX_WHITESPACE + REGEX_ATTR_VALUE + REGEX_WHITESPACE;
    private static final GroovyClassLoader gcl = new GroovyClassLoader();

    public static String stripAnnotations(final String script) {
        String result = script;
        for (final Class<?> aClass : getAnnotationClasses()) {
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
        String s = script.replaceAll(regex, StringUtils.EMPTY);
        s = s.replaceAll("(\n){3,}", "\n\n");
        return s;
    }

    public static String getAnnotation(final String script, final String className) {
        final String annotationName = "@" + className;
        final String regex = annotationName + REGEX_WHITESPACE + "(\\((" + REGEX_ATTRIBUTES + ")?\\))?"; //seems usefull need to eliminate in-string parentheses
        Matcher matcher = Pattern.compile(regex).matcher(script);
        return matcher.find() ? matcher.group() : StringUtils.EMPTY;
    }

    public static String getAnnotation(final String script, final Class clazz) {
        String simple = getAnnotation(script, clazz.getSimpleName());

        return StringUtils.isNotBlank(simple) ?
                simple :
                getAnnotation(script, clazz.getCanonicalName());
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

    /**
     * Technically it's not just Annotations, it's all classes from the Annotations library
     * This is a convenience method.
     * @return
     */
    public static List<Class<?>> getAnnotationClasses() {
        return Arrays.asList(Exclude.class, Bootstrap.class, Updater.class, Bootstrap.ContentRoot.class);
    }

}
