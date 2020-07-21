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
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import javax.xml.bind.JAXB;

import org.apache.commons.lang3.StringUtils;

import nl.openweb.hippo.groovy.annotations.Bootstrap;
import nl.openweb.hippo.groovy.annotations.Updater;
import nl.openweb.hippo.groovy.model.Constants;
import nl.openweb.hippo.groovy.model.Constants.ValueType;
import nl.openweb.hippo.groovy.model.ScriptClass;
import nl.openweb.hippo.groovy.model.jaxb.Node;
import nl.openweb.hippo.groovy.model.jaxb.Property;
import static nl.openweb.hippo.groovy.Marshal.CDATA_START;
import static nl.openweb.hippo.groovy.model.Constants.Files.ECM_EXTENSIONS_NAME;
import static nl.openweb.hippo.groovy.model.Constants.Files.XML_EXTENSION;
import static nl.openweb.hippo.groovy.model.Constants.NodeType.HIPPO_INITIALIZEFOLDER;
import static nl.openweb.hippo.groovy.model.Constants.NodeType.HIPPO_INITIALIZEITEM;
import static nl.openweb.hippo.groovy.model.Constants.PropertyName.HIPPOSYS_SCRIPT;
import static nl.openweb.hippo.groovy.model.Constants.PropertyName.HIPPO_CONTENTRESOURCE;
import static nl.openweb.hippo.groovy.model.Constants.PropertyName.HIPPO_CONTENTROOT;
import static nl.openweb.hippo.groovy.model.Constants.PropertyName.HIPPO_RELOADONSTARTUP;
import static nl.openweb.hippo.groovy.model.Constants.PropertyName.HIPPO_SEQUENCE;
import static nl.openweb.hippo.groovy.model.Constants.PropertyName.HIPPO_VERSION;
import static nl.openweb.hippo.groovy.model.Constants.PropertyName.JCR_MIXIN_TYPES;
import static nl.openweb.hippo.groovy.model.Constants.PropertyName.JCR_PRIMARY_TYPE;

/**
 * Generator to parse a groovy file to the bootstrap xmls
 */
public final class XmlGenerator extends Generator {

    public static final String CDATA_END = "]]>";
    public static final String SEPARATOR = "/";

    private XmlGenerator() {
        super();
    }

    /**
     * Parse file to updater node
     *
     * @param sourceDir the directory to read the resources from
     * @param script    the script to use for source
     * @return Node object representing the groovy updater to marshall to xml
     */
    public static Node getUpdateScriptNode(final File sourceDir, final ScriptClass script) {
        final Updater updater = script.getUpdater();
        final Node rootnode = XmlGenerator.createNode(updater.name());
        final List<Object> properties = rootnode.getNodeOrProperty();
        final Map<String, Object> propertiesForUpdater = PropertyCollector.getPropertiesForUpdater(script, sourceDir);
        for (Map.Entry<String, Object> entry : propertiesForUpdater.entrySet()) {
            String valueType = getValueType(entry);
            Object value = entry.getValue();
            if (HIPPOSYS_SCRIPT.equals(entry.getKey())) {
                value = wrap(value.toString());
            }
            properties.add(createProperty(entry.getKey(), value, valueType));
        }
        return rootnode;
    }

    private static String getValueType(final Map.Entry<String, Object> entry) {
        if (JCR_PRIMARY_TYPE.equals(entry.getKey()) || JCR_MIXIN_TYPES.equals(entry.getKey())) {
            return ValueType.NAME;
        } else if (entry.getValue() instanceof Long) {
            return ValueType.LONG;
        } else if (entry.getValue() instanceof Boolean) {
            return ValueType.BOOLEAN;
        }
        return ValueType.STRING;
    }

    private static void addStringPropertyIfNotEmpty(final List<Object> properties, final String name, final String value) {
        if (StringUtils.isNotBlank(value)) {
            properties.add(createProperty(name, value, ValueType.STRING));
        }
    }

    /**
     * Wrap string with empty lines
     *
     * @param content
     * @return the content starting and ending with a newline character
     * <p>
     * "<![CDATA[" + v + "]]>"
     */
    private static String wrap(final String content) {
        return CDATA_START + NEWLINE + content + NEWLINE + CDATA_END;
    }

    public static Property createProperty(final String name, final Object value, final String type) {
        final Property property = new Property();
        property.setName(name);
        property.setType(type);
        addValueToProperty(property, value);
      return property;
    }

    public static Property addValueToProperty(final Property property, final Object value) {
        if(value instanceof List) {
            List list = (List) value;
            for (Object objectValue : list) {
                property.getValue().add(objectValue.toString());
            }
            property.setMultiple(true);
            return property;
        }
        property.getValue().add(value.toString());
        return property;
    }

    /**
     * Generate files to generate a node model for the hippoecm-extension.xml
     *
     * @param sourcePath        sourcepath of groovy files
     * @param targetDir         the target where the ecmExtensions from resources would be
     * @param scriptClasses     groovy files, need to be relative to the source path
     * @param updaterNamePrefix prefix for the initialize items nodes   @return Node object representing the
     *                          hippoecm-extension to marshall to xml
     * @return the Node object for the ecm-extensions
     */
    public static Node getEcmExtensionNode(final File sourcePath, final File targetDir, final List<ScriptClass> scriptClasses, final String updaterNamePrefix) {
        final List<Object> properties;
        final Node rootnode;
        final Node ecmExtensionsScriptNode = getExistingEcmExtensions(sourcePath);
        final Node ecmExtensionTargetNode = getExistingEcmExtensions(targetDir);

        rootnode = createNode(Constants.NodeType.HIPPO_INITIALIZE);
        properties = rootnode.getNodeOrProperty();
        properties.add(createProperty(JCR_PRIMARY_TYPE, HIPPO_INITIALIZEFOLDER, ValueType.STRING));

        final Stream<Node> sourceStream = Stream.concat(ecmExtensionsScriptNode == null ? Stream.empty() : ecmExtensionsScriptNode.getSubnodes().stream(),
            ecmExtensionTargetNode == null ? Stream.empty() : ecmExtensionTargetNode.getSubnodes().stream());

        Stream.concat(sourceStream, scriptClasses.stream().map(script -> createInitializeItem(sourcePath, script, updaterNamePrefix)).filter(Objects::nonNull))
            .sorted(Comparator.comparingDouble(node -> Double.valueOf(node.getPropertyByName(HIPPO_SEQUENCE).getSingleValue())))
            .distinct()
            .forEach(properties::add);
        return rootnode;
    }

    private static Node getExistingEcmExtensions(final File sourcePath) {
        final File extensions = new File(sourcePath, ECM_EXTENSIONS_NAME);
        if (extensions.exists()) {
            return JAXB.unmarshal(extensions, Node.class);
        }
        return null;
    }

    /**
     * Create initialize item for the given file
     *
     * @param sourcePath  sourcepath of groovy files
     * @param scriptClass groovy files, need to be relative to the source path
     * @param namePrefix  prefix for the initialize items nodes
     * @return Node object representing the initializeitem node for the hippoecm-extension to marshall to xml
     */
    private static Node createInitializeItem(final File sourcePath, final ScriptClass scriptClass, final String namePrefix) {
        final Bootstrap bootstrap = scriptClass.getBootstrap(true);

        final String resource = getUpdateScriptXmlFilename(sourcePath, scriptClass.getFile());
        final Node initNode = createNode(namePrefix + resource);
        final List<Object> properties = initNode.getNodeOrProperty();

        Bootstrap.ContentRoot contentroot = getContentroot(bootstrap);

        properties.add(createProperty(JCR_PRIMARY_TYPE, HIPPO_INITIALIZEITEM, ValueType.NAME));
        addStringPropertyIfNotEmpty(properties, HIPPO_CONTENTRESOURCE, resource);
        properties.add(createProperty(HIPPO_CONTENTROOT, getUpdatePath(contentroot), ValueType.STRING));
        properties.add(createProperty(HIPPO_SEQUENCE, bootstrap.sequence(), ValueType.DOUBLE));
        if (bootstrap.reload()) {
            properties.add(createProperty(HIPPO_RELOADONSTARTUP, bootstrap.reload(), ValueType.BOOLEAN));
        }
        addStringPropertyIfNotEmpty(properties, HIPPO_VERSION, bootstrap.version());
        return initNode;
    }

    /**
     * Get update script xml filename
     *
     * @param basePath path to make returning path relative to
     * @param file     File object for the groovy script
     * @return the path, relative to the basePath, converted \ to /, with xml extension
     */
    public static String getUpdateScriptXmlFilename(final File basePath, final File file) {
        final String fileName = file.getAbsolutePath().substring(basePath.getAbsolutePath().length() + 1);
        return sanitizeFileName(fileName) + XML_EXTENSION;
    }

    /**
     * Utility method to create a Node with given name
     *
     * @param name name for the node
     * @return Node with given name
     */
    public static Node createNode(final String name) {
        final Node node = new Node();
        String initName = name;
        if (initName.endsWith(XML_EXTENSION)) {
            initName = initName.substring(0, initName.length() - XML_EXTENSION.length());
        }
        initName = initName.replaceAll(SEPARATOR, "-");
        node.setName(initName);
        return node;
    }
}
