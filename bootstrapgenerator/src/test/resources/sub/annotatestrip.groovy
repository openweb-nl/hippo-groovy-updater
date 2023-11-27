package org.hippoecm.frontend.plugins.cms.dev.updater

import nl.openweb.hippo.groovy.annotations.Bootstrap
import nl.openweb.hippo.groovy.annotations.Updater
import org.onehippo.repository.update.BaseNodeUpdateVisitor

import javax.jcr.Node
import javax.jcr.NodeIterator
import javax.jcr.Property
import javax.jcr.RepositoryException
import javax.jcr.Session
import javax.jcr.Value

@Bootstrap
@Updater(name = "Set some property", xpath = "content/documents//element(*,hippotaxonomy:classifiable)[@jcr:primaryType='project:communication' or @jcr:primaryType='project:listing' or @jcr:primaryType='project:newsdocument' or @jcr:primaryType='project:simpledocument' or @jcr:primaryType='project:richtextdocument' or @jcr:primaryType='project:services' or @jcr:primaryType='project:diydocument']")
class SetSomeDocumentProperty extends BaseNodeUpdateVisitor {

    private Set<String> properties

    @Override
    void initialize(Session session) throws RepositoryException {
        properties = new HashSet<>()
        Node theNode = session.getNode("/content/taxonomies/propertytietaxonomy/propertytietaxonomy/properties")
        addKeyAndCallChildsIfCorrectType(theNode)
    }

    void addKeyAndCallChildsIfCorrectType(Node node){
        if(node != null && node.primaryNodeType.name == "hippotaxonomy:category"){
            properties.add(node.getProperty("hippotaxonomy:key").string)
            NodeIterator iterator = node.nodes
            while (iterator.hasNext()){
                addKeyAndCallChildsIfCorrectType(iterator.nextNode())
            }
        }
    }

    @Override
    boolean doUpdate(Node node) {
        if(! node.hasProperty("hippotaxonomy:keys")){
            log.info "node at ${node.path} has no hippotaxonomy:keys property, skipping"
            return false
        }
        boolean value = isTagged(node.getProperty("hippotaxonomy:keys"))
        node.setProperty("myproject:tag", value)
        log.info "set tag to ${value} on path ${node.path}"
        return true
    }

    private boolean isTagged(Property property){
        for(Value value : property.values){
            if(properties.contains(value.string)){
                return true
            }
        }
        return false
    }

    boolean undoUpdate(Node node) {
        node.getProperty("myproject:tag").remove()
        return true
    }
}