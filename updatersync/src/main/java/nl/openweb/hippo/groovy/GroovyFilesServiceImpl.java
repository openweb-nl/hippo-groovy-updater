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

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.xml.bind.JAXBException;

import org.hippoecm.repository.util.JcrUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.openweb.hippo.groovy.annotations.Updater;
import nl.openweb.hippo.groovy.model.ScriptClass;
import static nl.openweb.hippo.groovy.Generator.getGroovyFiles;
import static nl.openweb.hippo.groovy.ScriptClassFactory.getInterpretingClass;
import static nl.openweb.hippo.groovy.model.Constants.NodeType.HIPPOSYS_UPDATERINFO;
import static nl.openweb.hippo.groovy.model.Constants.PropertyName.HIPPOSYS_BATCHSIZE;
import static nl.openweb.hippo.groovy.model.Constants.PropertyName.HIPPOSYS_DESCRIPTION;
import static nl.openweb.hippo.groovy.model.Constants.PropertyName.HIPPOSYS_PARAMETERS;
import static nl.openweb.hippo.groovy.model.Constants.PropertyName.HIPPOSYS_PATH;
import static nl.openweb.hippo.groovy.model.Constants.PropertyName.HIPPOSYS_QUERY;
import static nl.openweb.hippo.groovy.model.Constants.PropertyName.HIPPOSYS_SCRIPT;
import static nl.openweb.hippo.groovy.model.Constants.PropertyName.HIPPOSYS_THROTTLE;
import static nl.openweb.hippo.groovy.util.WatchFilesUtils.SCRIPT_ROOT;

public class GroovyFilesServiceImpl implements GroovyFilesService {

    private static final Logger log = LoggerFactory.getLogger(GroovyFilesServiceImpl.class);

    private static void warnAndThrow(final String message, final Object... args) {
        throw new GroovyFileException(warn(message, args));
    }

    private static String warn(final String message, final Object... args) {
        String warning = String.format(message, args);
        log.warn(warning);
        return warning;
    }

    private static String info(final String message, final Object... args) {
        String warning = String.format(message, args);
        log.info(warning);
        return warning;
    }

    /**
     * do the update or create a node of the groovy file
     * this will not save the session
     *
     * @param parent parentnode of the Node te be
     * @param file file to transform into a Node
     * @return success
     * @throws RepositoryException
     */
    private static boolean setUpdateScriptJcrNode(Node parent, File file) throws RepositoryException {
        ScriptClass scriptClass = getInterpretingClass(file);
        if(!scriptClass.isValid()){
            return false;
        }
        final Updater updater = scriptClass.getUpdater();
        String name = updater.name();
        if(parent.hasNode(name)){
            info("Updating existing script {}", name);
            parent.getNode(name).remove();
        }
        Node scriptNode = parent.addNode(name, HIPPOSYS_UPDATERINFO);

        scriptNode.setProperty(HIPPOSYS_BATCHSIZE, updater.batchSize());
        scriptNode.setProperty(HIPPOSYS_DESCRIPTION, updater.description());
        scriptNode.setProperty(HIPPOSYS_PARAMETERS, updater.parameters());
        scriptNode.setProperty(updater.xpath().isEmpty() ? HIPPOSYS_PATH : HIPPOSYS_QUERY,
                updater.xpath().isEmpty() ? updater.path() : updater.xpath());
        scriptNode.setProperty(HIPPOSYS_SCRIPT, scriptClass.getContent());
        scriptNode.setProperty(HIPPOSYS_THROTTLE, updater.throttle());
        return true;
    }

    private Node getRegistryNode(Session session) throws RepositoryException {
        final Node scriptRegistry = JcrUtils.getNodeIfExists(SCRIPT_ROOT, session);
        if (scriptRegistry == null) {
            warnAndThrow("Cannot find files root at '{}'", SCRIPT_ROOT);
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
     * @param file file to transform
     * @return success
     * @throws IOException
     * @throws RepositoryException
     * @throws JAXBException
     */
    public boolean importGroovyFile(Session session, File file) throws IOException, RepositoryException, JAXBException {
        return setUpdateScriptJcrNode(getRegistryNode(session), file);
    }
}
