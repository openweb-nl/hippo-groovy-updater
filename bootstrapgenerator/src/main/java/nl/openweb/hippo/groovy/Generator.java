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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import nl.openweb.hippo.groovy.annotations.Bootstrap;
import nl.openweb.hippo.groovy.annotations.Exclude;
import nl.openweb.hippo.groovy.annotations.Updater;
import nl.openweb.hippo.groovy.model.ScriptClass;
import static nl.openweb.hippo.groovy.model.Constants.Files.GROOVY_EXTENSION;

public abstract class Generator {
    protected static final String NEWLINE = "\n";
    private static final List<Class<?>> ANNOTATED_CLASSES = Arrays.asList(Exclude.class, Bootstrap.class, Updater.class, Bootstrap.ContentRoot.class);
    private static final String HIPPO_CONFIGURATION_UPDATE_PATH_PREFIX = "/hippo:configuration/hippo:update/hippo:";
    private static final String REGEX_WHITESPACE = "\\s*";
    private static final String REGEX_ATTR_NAME = "([A-Za-z]\\w*)";
    private static final String REGEX_ATTR_VALUE_SINGLEQUOTE = "('.*?(?<!\\\\)('))";
    private static final String REGEX_ATTR_VALUE_QUOTE = "(\".*?(?<!\\\\)(\"))";
    private static final String REGEX_ATTR_VALUE_TRIPQUOTE = "('''([\\s\\S]*)''')";
    private static final String REGEX_ATTR_VALUE_SIMPLE = "true|false|([^,^\\)]+)";
    private static final String REGEX_COMMA = "\\s*,*\\s*";
    private static final String REGEX_ATTR_VALUE = "("
            + REGEX_ATTR_VALUE_SINGLEQUOTE
            + "|"
            + REGEX_ATTR_VALUE_QUOTE
            + "|"
            + REGEX_ATTR_VALUE_TRIPQUOTE
            + "|"
            + REGEX_ATTR_VALUE_SIMPLE
            + ")?";
    private static final String REGEX_ATTRIBUTES = REGEX_WHITESPACE + REGEX_ATTR_NAME + REGEX_WHITESPACE + "=" + REGEX_WHITESPACE + REGEX_ATTR_VALUE + REGEX_COMMA;
    private static final String ANNOTATION_PAYLOAD = REGEX_WHITESPACE + "(\\((" + REGEX_ATTRIBUTES + ")*\\))?";

    protected static Bootstrap.ContentRoot defaultContentRoot = Bootstrap.ContentRoot.QUEUE;

    protected Generator() {
    }

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
        final String regex = getAnnotationRegex(className);
        String s = script.replaceAll(regex, StringUtils.EMPTY);
        s = s.replaceAll("(\n){3,}", "\n\n");
        return s;
    }

    public static String getAnnotation(final String script, final String className) {
        final String regex = getAnnotationRegex(className);
        Matcher matcher = Pattern.compile(regex).matcher(script);
        return matcher.find() ? matcher.group() : StringUtils.EMPTY;
    }

    private static String getAnnotationRegex(final String className) {
        final String annotationName = "@" + className;
        return annotationName + ANNOTATION_PAYLOAD;
    }

    public static String getAnnotation(final String script, final Class clazz) {
        String simple = getAnnotation(script, clazz.getSimpleName());

        return StringUtils.isNotBlank(simple) ?
                simple :
                getAnnotation(script, clazz.getCanonicalName());
    }

    public static Bootstrap.ContentRoot getContentroot(final Bootstrap bootstrap) {
        return Bootstrap.ContentRoot.DEFAULT.equals(bootstrap.contentroot()) ? defaultContentRoot : bootstrap.contentroot();
    }

    /**
     * Obtain groovy files from given location
     *
     * @param dir directory to obtain groovy files from
     * @return List of groovy files
     */
    public static List<File> getGroovyFiles(final File dir) {
        final File[] groovyFiles = dir.listFiles(file -> file.isFile() && file.getName().endsWith(GROOVY_EXTENSION));
        final File[] directories = dir.listFiles(File::isDirectory);
        final List<File> allFiles = new ArrayList<>();
        if (groovyFiles != null) {
            allFiles.addAll(Arrays.asList(groovyFiles));
            Collections.sort(allFiles, Comparator.comparing(File::getName));
        }
        if (directories != null) {
            Arrays.stream(directories)
                    .sorted(Comparator.comparing(File::getName))
                    .map(Generator::getGroovyFiles).forEach(allFiles::addAll);
        }
        return Collections.unmodifiableList(allFiles);
    }

    protected static String sanitizeFileName(final String fileName) {
        return FilenameUtils.removeExtension(FilenameUtils.separatorsToUnix(fileName)).replaceAll("\\s", "_");
    }

    protected static String getUpdatePath(Bootstrap.ContentRoot contentroot) {
        return HIPPO_CONFIGURATION_UPDATE_PATH_PREFIX + contentroot;
    }

    /**
     * Technically it's not just Annotations, it's all classes from the Annotations library
     * This is a convenience method.
     *
     * @return a list of the annotation classes
     */
    public static List<Class<?>> getAnnotationClasses() {
        return ANNOTATED_CLASSES;
    }

    public static void setDefaultContentRoot(Bootstrap.ContentRoot contentRoot) {
        defaultContentRoot = contentRoot;
    }

    protected static String getValueOrFileContent(final ScriptClass script, final File sourceDir, final String value) {
        final File parentDir = value.startsWith("/") ? sourceDir : script.getFile().getParentFile();
        final File file = new File(parentDir, value);
        if (file.exists()) {
            try {
                return ScriptClassFactory.readFileEnsuringLinuxLineEnding(file);
            } catch (IOException e) {
                //do nothing, it's fine
            }
        }
        return value;
    }
}
