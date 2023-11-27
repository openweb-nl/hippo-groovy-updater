package resourcebundles

import nl.openweb.hippo.groovy.annotations.Bootstrap
import nl.openweb.hippo.groovy.annotations.Updater
import org.apache.jackrabbit.commons.cnd.CompactNodeTypeDefReader
import org.apache.jackrabbit.commons.cnd.CompactNodeTypeDefWriter
import org.apache.jackrabbit.core.nodetype.InvalidNodeTypeDefException
import org.apache.jackrabbit.core.nodetype.NodeTypeManagerImpl
import org.apache.jackrabbit.core.nodetype.NodeTypeRegistry
import org.apache.jackrabbit.spi.QNodeTypeDefinition
import org.hippoecm.repository.jackrabbit.HippoCompactNodeTypeDefReader
import org.hippoecm.repository.jackrabbit.HippoNodeTypeRegistry
import org.hippoecm.repository.util.JcrCompactNodeTypeDefWriter
import org.onehippo.repository.update.BaseNodeUpdateVisitor

import javax.jcr.NamespaceRegistry
import javax.jcr.Node
import javax.jcr.RepositoryException
import javax.jcr.Session

@Bootstrap(contentroot = Bootstrap.ContentRoot.REGISTRY)
@Updater(
        name = "Update cnd's'",
        description = '''This is a workaround script to reload cnd's without checks. 
Ideal to remove doctypes from cnd's or change inheritence structures ''',
        parameters = "cndreload.json"
)
class CndReloader extends BaseNodeUpdateVisitor {
    List<Map<String, String>> cnds
    def iterator
    Session session
    NamespaceRegistry namespaceRegistry
    HippoNodeTypeRegistry nodeTypeRegistry

    @Override
    void initialize(final Session session) throws RepositoryException {
        super.initialize(session)
        NodeTypeRegistry.disableCheckForReferencesInContentException = true;
        namespaceRegistry = session.getWorkspace().getNamespaceRegistry();
        nodeTypeRegistry = (HippoNodeTypeRegistry) ((NodeTypeManagerImpl) session.getWorkspace().getNodeTypeManager()).getNodeTypeRegistry()
    }

    @Override
    Node firstNode(Session session) throws RepositoryException {
        this.session = session
        cnds = parametersMap.get("cnd")
        iterator = cnds.iterator()
        return nextNode()
    }

    @Override
    Node nextNode() throws RepositoryException {
        if (iterator.hasNext()) {
            def mapping = iterator.next()
            def namespaceURI = mapping.get("namespaceURI")
            def cndContent = mapping.get("cndcontent")
            try {
                updateCnd(namespaceURI, cndContent)
            }

            catch (Exception e) {
                log.error("Failed to update cnd for '${namespaceURI}', moving on!\n${e.getMessage()}")
                visitorContext.reportFailed(namespaceURI)
            }
            return session.getRootNode()
        }
        return null;
    }

    void updateCnd(String namespaceUri, String cndContent) {
        final CompactNodeTypeDefReader<QNodeTypeDefinition, CompactNodeTypeDefWriter.NamespaceMapping> cndReader =
                new HippoCompactNodeTypeDefReader(new StringReader(cndContent), namespaceUri, namespaceRegistry)
        String existingContent = JcrCompactNodeTypeDefWriter.compactNodeTypeDef(session.getWorkspace(), namespaceRegistry.getPrefix(namespaceUri))

        final List<QNodeTypeDefinition> ntdList = cndReader.getNodeTypeDefinitions();

        importNodeTypes(ntdList)

        final CompactNodeTypeDefReader<QNodeTypeDefinition, CompactNodeTypeDefWriter.NamespaceMapping> currentCndReader =
                new HippoCompactNodeTypeDefReader(new StringReader(existingContent), namespaceUri, namespaceRegistry)
        final List<QNodeTypeDefinition> existingNtdList = currentCndReader.getNodeTypeDefinitions()
        removeOldMappings(existingNtdList, ntdList)
    }

    private void importNodeTypes(List<QNodeTypeDefinition> ntdList) {
        for (QNodeTypeDefinition ntd : ntdList) {
            try {
                if (!nodeTypeRegistry.isRegistered(ntd.name)) {
                    log.debug("Registering node type {}", ntd.name)
                    if(!visitorContext.dryRun) {
                        nodeTypeRegistry.registerNodeType(ntd);
                    }
                } else {
                    log.debug("Replacing node type {}", ntd.name);
                    if(!visitorContext.dryRun) {
                        nodeTypeRegistry.ignoreNextConflictingContent();
                        nodeTypeRegistry.reregisterNodeType(ntd);
                    }
                }
            } catch (InvalidNodeTypeDefException e) {
                throw new RepositoryException("Invalid node type definition for node type ${ntd.name}", e);
            }
        }
    }

    @Override
    boolean doUpdate(Node node) throws RepositoryException {
        return false
    }

    @Override
    boolean undoUpdate(Node node) throws RepositoryException, UnsupportedOperationException {
        return false
    }

    void removeOldMappings(List<QNodeTypeDefinition> oldContent, List<QNodeTypeDefinition> newContent) {
        log.debug("attempting removing of old definitions")
        for (QNodeTypeDefinition nodeTypeDefinition : oldContent) {
            def name = nodeTypeDefinition.name.localName
            def contained = false
            for (QNodeTypeDefinition newDefinition : newContent) {
                if(name == newDefinition.name.localName){
                    contained = true
                    break
                }
            }
            if(!contained) {
                log.debug("nodetype not found in new definition ${name}, removing")
                if (!visitorContext.dryRun) {
                    nodeTypeRegistry.unregisterNodeType(nodeTypeDefinition.name);
                }
            }
        }
    }
}