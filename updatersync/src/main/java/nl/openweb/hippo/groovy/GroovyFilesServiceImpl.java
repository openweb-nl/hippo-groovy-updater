/*
 * Modifications Copyright 2019 Open Web IT B.V. (https://www.openweb.nl/)
 *
 * This file is Copyrighted to Hippo B.V. but there has been modification done to
 * via Open Web IT B.V. these modification (See the modification via version control history)
 * are licence to Open Web IT B.V.
 * Under Apache License, Version 2.0. Please see the Original Copyright notice blew.
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
 *
 * Copyright 2014-2015 Hippo B.V. (http://www.onehippo.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
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
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;
import javax.xml.bind.JAXBException;

import org.apache.jackrabbit.value.BooleanValue;
import org.apache.jackrabbit.value.LongValue;
import org.apache.jackrabbit.value.NameValue;
import org.apache.jackrabbit.value.StringValue;
import org.hippoecm.repository.util.JcrUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.openweb.hippo.groovy.annotations.Updater;
import nl.openweb.hippo.groovy.model.ScriptClass;
import static nl.openweb.hippo.groovy.Generator.getGroovyFiles;
import static nl.openweb.hippo.groovy.ScriptClassFactory.getInterpretingClass;
import static nl.openweb.hippo.groovy.model.Constants.NodeType.HIPPOSYS_UPDATERINFO;
import static nl.openweb.hippo.groovy.model.Constants.PropertyName.HIPPOSYS_DESCRIPTION;
import static nl.openweb.hippo.groovy.model.Constants.PropertyName.HIPPOSYS_PARAMETERS;
import static nl.openweb.hippo.groovy.model.Constants.PropertyName.HIPPOSYS_PATH;
import static nl.openweb.hippo.groovy.model.Constants.PropertyName.HIPPOSYS_QUERY;
import static nl.openweb.hippo.groovy.model.Constants.PropertyName.JCR_MIXIN_TYPES;
import static nl.openweb.hippo.groovy.model.Constants.PropertyName.JCR_PRIMARY_TYPE;
import static nl.openweb.hippo.groovy.util.WatchFilesUtils.SCRIPT_ROOT;

public class GroovyFilesServiceImpl implements GroovyFilesService {

    private static final Logger LOGGER = LoggerFactory.getLogger(GroovyFilesServiceImpl.class);

    private static void warnAndThrow(final String message, final Object... args) {
        throw new GroovyFileException(warn(message, args));
    }

    private static String warn(final String message, final Object... args) {
        String warning = String.format(message, args);
        LOGGER.warn(warning);
        return warning;
    }

    private static String info(final String message, final Object... args) {
        String warning = String.format(message, args);
        LOGGER.info(warning);
        return warning;
    }

    /**
     * do the update or create a node of the groovy file this will not save the session
     *
     * @param parent parentnode of the Node te be
     * @param file   file to transform into a Node
     * @return success
     * @throws RepositoryException
     */
    private static boolean setUpdateScriptJcrNode(Node parent, File file) throws RepositoryException {
        ScriptClass scriptClass = getInterpretingClass(file, true);
        if (!scriptClass.isValid()) {
            return false;
        }
        final Updater updater = scriptClass.getUpdater();
        Node scriptNode = getScriptNode(parent, updater.name());
        final Map<String, Object> properties = PropertyCollector.getPropertiesForUpdater(scriptClass, file.getParentFile());
        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            if (!JCR_PRIMARY_TYPE.equals(entry.getKey())) {
                scriptNode.setProperty(entry.getKey(), getValue(entry));
            }
        }
        return true;
    }

    private static Node getScriptNode(final Node parent, final String name) throws RepositoryException {
        if (parent.hasNode(name)) {
            info("Updating existing script %s", name);
            final Node node = parent.getNode(name);

            String[] deleteProperties = new String[]{HIPPOSYS_DESCRIPTION, HIPPOSYS_PARAMETERS, HIPPOSYS_PATH, HIPPOSYS_QUERY};

            for (String deleteProperty : deleteProperties) {
                if (node.hasProperty(deleteProperty)) {
                    node.getProperty(deleteProperty).remove();
                }
            }
            return node;
        }
        return parent.addNode(name, HIPPOSYS_UPDATERINFO);
    }

    private static Value getValue(final Map.Entry<String, Object> entry) throws ValueFormatException {
        if (JCR_PRIMARY_TYPE.equals(entry.getKey()) || JCR_MIXIN_TYPES.equals(entry.getKey())) {
            return NameValue.valueOf(entry.getValue().toString());
        } else if (entry.getValue() instanceof Long) {
            return LongValue.valueOf(entry.getValue().toString());
        } else if (entry.getValue() instanceof Boolean) {
            return BooleanValue.valueOf(entry.getValue().toString());
        }
        return new StringValue(entry.getValue().toString());
    }

    private Node getRegistryNode(Session session) throws RepositoryException {
        final Node scriptRegistry = JcrUtils.getNodeIfExists(SCRIPT_ROOT, session);
        if (scriptRegistry == null) {
            warnAndThrow("Cannot find files root at '%s'", SCRIPT_ROOT);
        }
        return scriptRegistry;
    }

    public void importGroovyFiles(Session session, File file) throws IOException, RepositoryException {
        List<File> groovyFiles = getGroovyFiles(file);
        for (File groovyFile : groovyFiles) {
            importGroovyFiles(session, groovyFile);
        }
    }

    /**
     * This method will take care of updating the node in the repository
     *
     * @param session jcr session tu use
     * @param file    file to transform
     * @return success
     * @throws IOException
     * @throws RepositoryException
     * @throws JAXBException
     */
    public boolean importGroovyFile(Session session, File file) throws IOException, RepositoryException, JAXBException {
        return setUpdateScriptJcrNode(getRegistryNode(session), file);
    }
}
