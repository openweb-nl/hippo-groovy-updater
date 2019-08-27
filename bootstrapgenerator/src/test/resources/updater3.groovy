import nl.openweb.hippo.groovy.annotations.Bootstrap
import nl.openweb.hippo.groovy.annotations.Updater
import org.onehippo.repository.update.BaseNodeUpdateVisitor

import javax.jcr.Node
import nl.openweb.hippo.groovy.annotations.Bootstrap.ContentRoot;
@Bootstrap(contentroot = ContentRoot.QUEUE, sequence = 99999.0d, reload = true, version = "2")
@Updater(name = "Test Updater 3", path = "", xpath = "//element(*, hippo:document)"
        , description = "Test things, like reserved words as import or others", batchSize = 1L, throttle = 200L, dryRun = true, parameters = "{prop: val}")

class TestUpdater3 extends BaseNodeUpdateVisitor {
    boolean doUpdate(Node node) {
        log.info "manipulate node < > & an %^&* /> {}", node.path
        return true
    }

    boolean undoUpdate(Node node) {
        throw new UnsupportedOperationException('Updater does not implement undoUpdate method')
    }
}