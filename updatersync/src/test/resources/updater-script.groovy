import nl.openweb.hippo.groovy.annotations.Bootstrap
import nl.openweb.hippo.groovy.annotations.Updater
import org.onehippo.repository.update.BaseNodeUpdateVisitor

import javax.jcr.Node

@Updater(name = "Test Updater",
        parameters = "{\"example\": \"parameter\"}",
        description='''This script can be used to do anything.
            (It should allow any notations, for the stripping etc..
            for example a description on how the XPath query should be like //element(*, hippo:document)[mixin:types='project:example']
            or the parameters field, describing like: { "foobar": [ "bar", "foo"]}''', path = "")

@Bootstrap(reload = true, sequence = 99999.0d)
class TestUpdater extends BaseNodeUpdateVisitor {
    boolean doUpdate(Node node) {
        log.info "manipulate node < > & an %^&* /> {}", node.path
        return true
    }

    boolean undoUpdate(Node node) {
        throw new UnsupportedOperationException('Updater does not implement undoUpdate method')
    }
}