import nl.openweb.hippo.groovy.annotations.Bootstrap
import nl.openweb.hippo.groovy.annotations.Updater
import org.onehippo.repository.update.BaseNodeUpdateVisitor

import javax.jcr.Node

@Bootstrap(contentroot = Bootstrap.ContentRoot.REGISTRY, reload = true, version = "1.6")
@Updater(name = "Test Updater 2", path = "/content", description = "Test thing", batchSize = 1L, throttle = 200L, dryRun = true, parameters = "{prop: val}")
class TestUpdater2 extends BaseNodeUpdateVisitor {
    boolean doUpdate(Node node) {
        log.info "manipulate node < > & an %^&* /> {}", node.path
        return true
    }

    boolean undoUpdate(Node node) {
        throw new UnsupportedOperationException('Updater does not implement undoUpdate method')
    }
}