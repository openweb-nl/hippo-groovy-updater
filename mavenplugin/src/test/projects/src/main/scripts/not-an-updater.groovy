import org.onehippo.repository.update.BaseNodeUpdateVisitor

import javax.jcr.Node

class TestNotAnUpdater extends BaseNodeUpdateVisitor {
    boolean doUpdate(Node node) {
        log.info("I should be skipped")
        return true
    }

    boolean undoUpdate(Node node) {
        throw new UnsupportedOperationException('Updater does not implement undoUpdate method')
    }
}