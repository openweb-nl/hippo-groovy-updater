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

package org.hippoecm.frontend.plugins.cms.dev.updater

import nl.openweb.hippo.groovy.annotations.Bootstrap
import nl.openweb.hippo.groovy.annotations.Updater
import org.hippoecm.repository.util.JcrUtils
import org.onehippo.repository.update.BaseNodeUpdateVisitor

import javax.jcr.Node
import javax.jcr.RepositoryException

@Bootstrap(sequence = 50000d, reload = true, version = "1")
@Updater(name = "Cleanup Nodes no longer needed", xpath = "/jcr:root")
class Cleanup extends BaseNodeUpdateVisitor{

    String[] removeNodes = [
            "hippo:configuration/hippo:update/hippo:history/Cleanup Nodes no longer needed",
            "hippo:configuration/hippo:update/hippo:registry/Updater from some previous version",
            "hippo:configuration/hippo:update/hippo:registry/Updater I need no more"
    ]

    @Override
    boolean doUpdate(Node node) throws RepositoryException {
        log.debug "Visiting node at ${node.path} as an entry point."

        for (String nodePath : removeNodes) {
            Node deleteNode = JcrUtils.getNodeIfExists(node, nodePath)
            if(deleteNode != null){
                log.debug "Removing node at '${nodePath}'"
                deleteNode.remove()
                visitorContext.reportUpdated(nodePath)
            }else{
                log.debug "Could not find a node at '${nodePath}', skipping"
                visitorContext.reportSkipped(nodePath)
            }
        }
        return false
    }

    @Override
    boolean undoUpdate(Node node) throws RepositoryException, UnsupportedOperationException {
        return false
    }
}