import nl.openweb.hippo.groovy.annotations.Bootstrap
import nl.openweb.hippo.groovy.annotations.Updater
import nl.openweb.tools.Tools
import org.onehippo.repository.update.BaseNodeUpdateVisitor

import javax.jcr.Node

@Updater(name = "Test Updater", xpath = "//element(*, hippo:document)")
@Bootstrap(reload = true)
class TestUpdater extends BaseNodeUpdateVisitor {
    boolean doUpdate(Node node) {
        log.info(Tools.getEnvironmentInfo())
        log.info "manipulate node < > & an %^&* /> {}", node.path
        return true
    }

    boolean undoUpdate(Node node) {
        throw new UnsupportedOperationException('Updater does not implement undoUpdate method')
    }
}