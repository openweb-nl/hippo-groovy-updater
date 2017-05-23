package nl.openweb.hippo.groovy;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.JAXB;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.plexus.util.FileUtils;

import groovy.lang.GroovyClassLoader;
import nl.openweb.hippo.groovy.annotations.Bootstrap;
import nl.openweb.hippo.groovy.annotations.Updater;
import nl.openweb.hippo.groovy.model.Constants;
import nl.openweb.hippo.groovy.model.Constants.ValueType;
import nl.openweb.hippo.groovy.model.DefaultBootstrap;
import nl.openweb.hippo.groovy.model.jaxb.Node;
import nl.openweb.hippo.groovy.model.jaxb.Property;
import static nl.openweb.hippo.groovy.Marshal.CDATA_START;
import static nl.openweb.hippo.groovy.model.Constants.Files.ECM_EXTENSIONS_NAME;
import static nl.openweb.hippo.groovy.model.Constants.Files.GROOVY_EXTENSION;
import static nl.openweb.hippo.groovy.model.Constants.Files.XML_EXTENSION;
import static nl.openweb.hippo.groovy.model.Constants.NodeType.HIPPOSYS_UPDATERINFO;
import static nl.openweb.hippo.groovy.model.Constants.NodeType.HIPPO_INITIALIZEFOLDER;
import static nl.openweb.hippo.groovy.model.Constants.PropertyName.HIPPOSYS_BATCHSIZE;
import static nl.openweb.hippo.groovy.model.Constants.PropertyName.HIPPOSYS_DESCRIPTION;
import static nl.openweb.hippo.groovy.model.Constants.PropertyName.HIPPOSYS_DRYRUN;
import static nl.openweb.hippo.groovy.model.Constants.PropertyName.HIPPOSYS_PARAMETERS;
import static nl.openweb.hippo.groovy.model.Constants.PropertyName.HIPPOSYS_PATH;
import static nl.openweb.hippo.groovy.model.Constants.PropertyName.HIPPOSYS_QUERY;
import static nl.openweb.hippo.groovy.model.Constants.PropertyName.HIPPOSYS_SCRIPT;
import static nl.openweb.hippo.groovy.model.Constants.PropertyName.HIPPOSYS_THROTTLE;
import static nl.openweb.hippo.groovy.model.Constants.PropertyName.HIPPO_CONTENTRESOURCE;
import static nl.openweb.hippo.groovy.model.Constants.PropertyName.HIPPO_CONTENTROOT;
import static nl.openweb.hippo.groovy.model.Constants.PropertyName.HIPPO_RELOADONSTARTUP;
import static nl.openweb.hippo.groovy.model.Constants.PropertyName.HIPPO_SEQUENCE;
import static nl.openweb.hippo.groovy.model.Constants.PropertyName.HIPPO_VERSION;
import static nl.openweb.hippo.groovy.model.Constants.PropertyName.JCR_PRIMARY_TYPE;

/**
 * Generator to parse a groovy file to the bootstrap xmls
 */
public final class XmlGenerator {

    private static final GroovyClassLoader gcl = new GroovyClassLoader();
    public static final String CDATA_END = "]]>";
    public static final String NEWLINE = "\n";
    private static final String REGEX_WHITESPACE = "\\s*";
    private static final String REGEX_ATTR_NAME = "([A-Za-z]\\w*)";
    private static final String REGEX_ATTR_VALUE = "((\"[^\"]*\")|[^\\)]|true|false)*";
    private static final String REGEX_ATTRIBUTES = REGEX_WHITESPACE + REGEX_ATTR_NAME + REGEX_WHITESPACE + "=" + REGEX_WHITESPACE + REGEX_ATTR_VALUE + REGEX_WHITESPACE;
    public static final String SEPARATOR = "/";

    private XmlGenerator() {
    }

    /**
     * Parse file to updater node
     *
     * @param file the groovy file to use for source
     * @return Node object representing the groovy updater to marshall to xml
     */
    public static Node getUpdateScriptNode(File file) {
        String content;
        final Updater updater;
        try {
            content = FileUtils.fileRead(file);
            Class scriptClass = getScriptClass(file);
            updater = (Updater) scriptClass.getDeclaredAnnotation(Updater.class);
        } catch (IOException e) {
            return null;
        }

        if(updater == null){
            return null;
        }
        Node rootnode = XmlGenerator.createNode(updater.name());
        List<Object> properties = rootnode.getNodeOrProperty();
        properties.add(createProperty(JCR_PRIMARY_TYPE, HIPPOSYS_UPDATERINFO, ValueType.NAME));
        properties.add(createProperty(HIPPOSYS_BATCHSIZE, updater.batchSize(), ValueType.LONG));
        addStringPropertyIfNotEmpty(properties, HIPPOSYS_DESCRIPTION, updater.description());
        properties.add(createProperty(HIPPOSYS_DRYRUN, updater.dryRun(), ValueType.BOOLEAN));
        addStringPropertyIfNotEmpty(properties, HIPPOSYS_PARAMETERS, updater.parameters());
        addStringPropertyIfNotEmpty(properties, HIPPOSYS_PATH, updater.path());
        addStringPropertyIfNotEmpty(properties, HIPPOSYS_QUERY, updater.xpath());
        addStringPropertyIfNotEmpty(properties, HIPPOSYS_SCRIPT, processScriptContent(content));
        properties.add(createProperty(HIPPOSYS_THROTTLE, updater.throttle(), ValueType.LONG));
        return rootnode;
    }

    public static Class getScriptClass(File file) throws IOException {
        gcl.clearCache();
        return gcl.parseClass(file);
    }

    /**
     * Add a classpath to the groovy parsing engine, for example if the groovy script uses classes from within the project
     * @param path path to add to the classpath
     */
    public static void addClassPath(String path){
        gcl.addClasspath(path);
    }

    private static void addStringPropertyIfNotEmpty(List<Object> properties, String name, String value){
        if(StringUtils.isNotBlank(value)){
            properties.add(createProperty(name, value, ValueType.STRING));
        }
    }

    /**
     * Do some useful tweaks to make the script pleasant and readable
     */
    private static String processScriptContent(String script){
        String stripAnnotations = stripAnnotations(script, Bootstrap.class, Updater.class);
        return wrap(stripAnnotations);
    }

    public static String stripAnnotations(final String script, final Class<? extends Annotation>... classes){
        String result = script;
        for (Class<? extends Annotation> aClass : classes) {
            if(result.contains(aClass.getPackage().getName()) &&
                    result.contains(aClass.getSimpleName())) {
                result = stripAnnotation(result, aClass.getSimpleName());
                result = stripAnnotation(result, aClass.getName());
                result = result.replaceAll("import\\s*" + aClass.getName() + "\\s*[;]?\n", "");
            }
        }
        return result;
    }

    private static String stripAnnotation(final String script, final String className) {
        String annotationName = "@" + className;
        String regex = annotationName + REGEX_WHITESPACE + "(\\((" + REGEX_ATTRIBUTES + ")?\\))?"; //seems usefull need to eliminate in-string parentheses
        String s = script.replaceAll(regex, NEWLINE);
        s = s.replaceAll("(\n){3,}", "\n\n");
        return s;
    }

    /**
     * Wrap string with empty lines
     * @param content
     * @return the content starting and ending with a newline character
     *
     * "<![CDATA[" + v + "]]>"
     */
    private static String wrap(final String content) {
        return CDATA_START + NEWLINE + content + NEWLINE + CDATA_END;
    }

    public static Property createProperty(final String name, final Object value, final String type) {
        Property property = new Property();
        property.setName(name);
        property.setType(type);
        property.getValue().add(value.toString());
        property.setMultiple(false);
        return property;
    }

    /**
     * Generate files to generate a node model for the hippoecm-extension.xml
     * @param sourcePath sourcepath of groovy files
     * @param files groovy files, need to be relative to the source path
     * @param updaterNamePrefix prefix for the initialize items nodes
     * @return Node object representing the hippoecm-extension to marshall to xml
     */
    public static Node getEcmExtensionNode(File sourcePath, List<File> files, String updaterNamePrefix) {
        List<Object> properties;
        Node rootnode = getExistingEcmExtensions(sourcePath);
        if(rootnode==null) {
            rootnode = createNode(Constants.NodeType.HIPPO_INITIALIZE);
            properties = rootnode.getNodeOrProperty();
            properties.add(XmlGenerator.createProperty(JCR_PRIMARY_TYPE, HIPPO_INITIALIZEFOLDER, ValueType.STRING));
        } else {
            properties = rootnode.getNodeOrProperty();

        }
        files.stream().map(file -> createInitializeItem(sourcePath, file, updaterNamePrefix)).filter(Objects::nonNull)
                .sorted(Comparator.comparingDouble(node -> Double.valueOf(node.getPropertyByName(HIPPO_SEQUENCE).getSingleValue())))
                .forEach(properties::add);
        return rootnode;
    }

    private static Node getExistingEcmExtensions(File sourcePath) {
        File extensions = new File(sourcePath, ECM_EXTENSIONS_NAME);
        if(extensions.exists()){
            return JAXB.unmarshal(extensions, Node.class);
        }
        return null;
    }

    /**
     * Create initialize item for the given file
     * @param sourcePath sourcepath of groovy files
     * @param file groovy files, need to be relative to the source path
     * @param namePrefix prefix for the initialize items nodes
     * @return Node object representing the initializeitem node for the hippoecm-extension to marshall to xml
     */
    private static Node createInitializeItem(File sourcePath, File file, String namePrefix) {
        Bootstrap bootstrap;
        try {
            Class scriptClass = getScriptClass(file);
            if(scriptClass.isAnnotationPresent(Updater.class)) {
                bootstrap = (Bootstrap) (scriptClass.isAnnotationPresent(Bootstrap.class) ?
                        scriptClass : DefaultBootstrap.class).getAnnotation(Bootstrap.class);
            }else{
                return null;
            }

        } catch (IOException e) {
            return null;
        }
        String resource = getUpdateScriptXmlFilename(sourcePath, file);
        Node initNode = createNode(namePrefix + resource);
        List<Object> properties = initNode.getNodeOrProperty();

        String contentroot = "queue";
        if (bootstrap.contentroot().equals("registry")) {
            contentroot = bootstrap.contentroot();
        }
        addStringPropertyIfNotEmpty(properties, HIPPO_CONTENTRESOURCE, resource);
        properties.add(createProperty(HIPPO_CONTENTROOT, "/hippo:configuration/hippo:update/hippo:" + contentroot, ValueType.STRING));
        properties.add(createProperty(HIPPO_SEQUENCE, bootstrap.sequence(), "Double"));
        if (bootstrap.reload()) {
            properties.add(createProperty(HIPPO_RELOADONSTARTUP, bootstrap.reload(), "Boolean"));
        }
        addStringPropertyIfNotEmpty(properties, HIPPO_VERSION, bootstrap.version());
        return initNode;
    }

    /**
     * Get update script xml filename
     * @param basePath path to make returning path relative to
     * @param file File object for the groovy script
     * @return the path, relative to the basePath, converted \ to /
     */
    public static String getUpdateScriptXmlFilename(File basePath, File file) {
        String fileName = file.getAbsolutePath().replaceFirst(basePath.getAbsolutePath(), "");
        if(File.pathSeparatorChar != '/'){
            fileName = fileName.replaceAll(File.pathSeparator, SEPARATOR);
        }
        fileName = fileName.substring(1);
        return fileName.endsWith(GROOVY_EXTENSION) ?
                fileName.substring(0, fileName.length() - GROOVY_EXTENSION.length()) + XML_EXTENSION : fileName;
    }

    /**
     * Utility method to create a Node with given name
     * @param name name for the node
     * @return Node with given name
     */
    public static Node createNode(final String name) {
        Node node = new Node();
        String initName = name;
        if(initName.endsWith(XML_EXTENSION)){
            initName = initName.substring(0, initName.length() - XML_EXTENSION.length());
        }
        initName = initName.replaceAll(SEPARATOR, "-");
        node.setName(initName);
        return node;
    }

    /**
     * Obtain groovy files from given location
     * @param dir directory to obtain groovy files from
     * @return List of groovy files
     */
    public static List<File> getGroovyFiles(File dir) {
        File[] groovyFiles = dir.listFiles((file) -> file.isFile() && file.getName().endsWith(GROOVY_EXTENSION));
        File[] directories = dir.listFiles(File::isDirectory);
        final List<File> allFiles = new ArrayList<>();
        if (groovyFiles != null) {
            allFiles.addAll(Arrays.asList(groovyFiles));
        }
        if (directories != null) {
            Arrays.stream(directories).map(XmlGenerator::getGroovyFiles).forEach(allFiles::addAll);
        }
        return Collections.unmodifiableList(allFiles);
    }
}
