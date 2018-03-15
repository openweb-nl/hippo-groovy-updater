import nl.openweb.hippo.groovy.annotations.Updater
import org.onehippo.repository.update.BaseNodeUpdateVisitor

import javax.jcr.Node

@Updater(name = "Test Sub Updater 2", xpath = "//element(*, hippo:document)")
class TestSubUpdater2 extends BaseNodeUpdateVisitor {
    boolean doUpdate(Node node) {
        log.info "manipulate node < > & an %^&* /> {}", node.path
        return true
    }

    boolean undoUpdate(Node node) {
        throw new UnsupportedOperationException('Updater does not implement undoUpdate method')
    }
}