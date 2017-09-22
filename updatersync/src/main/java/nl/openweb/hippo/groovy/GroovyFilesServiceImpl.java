/*
 * Modifications Copyright 2017 Open Web IT B.V. (https://www.openweb.nl/)
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
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.xml.bind.JAXBException;

import org.codehaus.plexus.util.FileUtils;
import org.hippoecm.repository.util.JcrUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.openweb.hippo.groovy.annotations.Bootstrap;
import nl.openweb.hippo.groovy.annotations.Updater;
import static nl.openweb.hippo.groovy.Generator.getGroovyFiles;
import static nl.openweb.hippo.groovy.Generator.getInterpretingClass;
import static nl.openweb.hippo.groovy.Generator.stripAnnotations;
import static nl.openweb.hippo.groovy.model.Constants.NodeType.HIPPOSYS_UPDATERINFO;
import static nl.openweb.hippo.groovy.model.Constants.PropertyName.HIPPOSYS_BATCHSIZE;
import static nl.openweb.hippo.groovy.model.Constants.PropertyName.HIPPOSYS_DESCRIPTION;
import static nl.openweb.hippo.groovy.model.Constants.PropertyName.HIPPOSYS_PARAMETERS;
import static nl.openweb.hippo.groovy.model.Constants.PropertyName.HIPPOSYS_PATH;
import static nl.openweb.hippo.groovy.model.Constants.PropertyName.HIPPOSYS_QUERY;
import static nl.openweb.hippo.groovy.model.Constants.PropertyName.HIPPOSYS_SCRIPT;
import static nl.openweb.hippo.groovy.model.Constants.PropertyName.HIPPOSYS_THROTTLE;

public class GroovyFilesServiceImpl implements GroovyFilesService {

    private static final Logger log = LoggerFactory.getLogger(GroovyFilesServiceImpl.class);

    public GroovyFilesServiceImpl() {
        ClassLoader cl = ClassLoader.getSystemClassLoader();
        URL[] urls = ((URLClassLoader) cl).getURLs();
        Arrays.stream(urls).map(URL::getPath).forEach(Generator::addClassPath);
    }

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

    public static void setUpdateScriptJcrNode(Node parent, File file) throws RepositoryException {
        String content;
        final Updater updater;
        try {
            content = FileUtils.fileRead(file);
            Class scriptClass = getInterpretingClass(file);
            updater = (Updater) scriptClass.getDeclaredAnnotation(Updater.class);
        } catch (IOException e) {
            return;
        }
        String name = updater.name();
        Node scriptNode = parent.hasNode(name) ? parent.getNode(name) : parent.addNode(name, HIPPOSYS_UPDATERINFO);

        scriptNode.setProperty(HIPPOSYS_BATCHSIZE, updater.batchSize());
        scriptNode.setProperty(HIPPOSYS_DESCRIPTION, updater.description());
        scriptNode.setProperty(HIPPOSYS_PARAMETERS, updater.parameters());
        scriptNode.setProperty(updater.xpath().isEmpty() ? HIPPOSYS_PATH : HIPPOSYS_QUERY,
                updater.xpath().isEmpty() ? updater.parameters() : updater.xpath());
        scriptNode.setProperty(HIPPOSYS_SCRIPT, stripAnnotations(content, Updater.class, Bootstrap.class, Bootstrap.ContentRoot.class));
        scriptNode.setProperty(HIPPOSYS_THROTTLE, updater.throttle());
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

    public void importGroovyFile(Session session, File file) throws IOException, RepositoryException, JAXBException {
        setUpdateScriptJcrNode(getRegistryNode(session), file);
        session.save();
    }
}
