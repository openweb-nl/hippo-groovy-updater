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

package maintenance


import nl.openweb.hippo.groovy.annotations.Bootstrap
import nl.openweb.hippo.groovy.annotations.Updater
import org.onehippo.repository.update.BaseNodeUpdateVisitor

import javax.jcr.Node
import javax.jcr.RepositoryException

@Bootstrap(sequence = 32000d, contentroot = Bootstrap.ContentRoot.REGISTRY)
@Updater(name = 'Cleanup \'permanent\' formdata', xpath = "//formdata/permanent/*//element(*,hst:formdata)[@hst:creationtime<xs:dateTime(\"2016-06-01T00:00:00.000+02:00\")] order by @hst:creationtime descending",
  description = "Permanently removes formdata! All formdata that matches the query will be deleted. Empty formdatacontainers will be removed.",
  batchSize = 100L)
class CleanupPermanentFormdata extends BaseNodeUpdateVisitor {

  boolean doUpdate(Node node) {
    remove(node, 20)
    return true
  }

  private void remove(final Node node, int ancestorsToRemove) throws RepositoryException {
    final Node parent = node.parent
    log.debug "Removed node ${node.path}"
    node.remove()

    if (ancestorsToRemove > 0 && parent != null
      && parent.isNodeType("hst:formdatacontainer") && !"permanent".equals(parent.getName()) && parent.getNodes().getSize() == 0) {
      remove(parent, ancestorsToRemove - 1)
    }
  }

  @Override
  boolean undoUpdate(final Node node) throws RepositoryException, UnsupportedOperationException {
    return false
  }
}