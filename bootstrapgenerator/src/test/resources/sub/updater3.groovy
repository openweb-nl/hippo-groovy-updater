import com.sun.xml.txw2.annotation.XmlElement
import nl.openweb.hippo.groovy.annotations.Exclude
import nl.openweb.hippo.groovy.annotations.Updater
import org.onehippo.repository.update.BaseNodeUpdateVisitor

import javax.jcr.Node
import javax.xml.bind.annotation.XmlAccessOrder
import javax.xml.bind.annotation.XmlAccessorOrder


//This has several comments, since real code can do the same
/*
Also, there is some other annotation
 * you never know if there is another thing working it
 * Of course, maybe it could break using:@Updater( in a comment
 */
@XmlElement
@XmlAccessorOrder(
        value = XmlAccessOrder.ALPHABETICAL
)
//@Exclude
@Updater(name = "Test Sub Updater 3", xpath = "//element(*, hippo:document)")
class TestSubUpdater3 extends BaseNodeUpdateVisitor {
    boolean doUpdate(Node node) {
        log.info "manipulate node < > & an %^&* /> {}", node.path
        return true
    }

    boolean undoUpdate(Node node) {
        throw new UnsupportedOperationException('Updater does not implement undoUpdate method')
    }
}