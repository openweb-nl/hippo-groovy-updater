/*
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

import org.onehippo.cms7.services.SingletonService;
import org.onehippo.cms7.services.WhiteboardService;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;

@SingletonService
@WhiteboardService
public interface GroovyFilesService {
    public static final String SCRIPT_ROOT = "/hippo:configuration/hippo:update/hippo:registry";

    public void importGroovyFiles(Session session, File file) throws IOException, RepositoryException;

    public void importGroovyFile(Session session, File file) throws IOException, RepositoryException, JAXBException;
}