import nl.openweb.hippo.groovy.annotations.Bootstrap
import nl.openweb.hippo.groovy.annotations.Updater
import org.onehippo.repository.update.BaseNodeUpdateVisitor

import javax.jcr.Node

@Bootstrap(contentroot = "queue", sequence = 99999.0d, reload = true, version = "2")
@Updater(name = "Test Updater 3", path = "", xpath = "//element(*, hippo:document)"
        , description = "Test thing", batchSize = 1L, throttle = 200L, dryRun = true, parameters = "{prop: val}")

class TestUpdater3 extends BaseNodeUpdateVisitor {
    boolean doUpdate(Node node) {
        log.info "manipulate node < > & an %^&* /> {}", node.path
        return true
    }

    boolean undoUpdate(Node node) {
        throw new UnsupportedOperationException('Updater does not implement undoUpdate method')
    }
}