package nl.openweb.hippo.groovy.model;

/**
 * Constants for generating bootstrap xml
 */
public interface Constants {

    interface Files {
        String GROOVY_EXTENSION = ".groovy";
        String XML_EXTENSION = ".xml";
        String ECM_EXTENSIONS_NAME = "hippoecm-extension.xml";
    }

    interface PropertyName {
        String JCR_PRIMARY_TYPE = "jcr:primaryType";
        String HIPPOSYS_BATCHSIZE = "hipposys:batchsize";
        String HIPPOSYS_DRYRUN = "hipposys:dryrun";
        String HIPPOSYS_PARAMETERS = "hipposys:parameters";
        String HIPPOSYS_QUERY = "hipposys:query";
        String HIPPOSYS_SCRIPT = "hipposys:script";
        String HIPPOSYS_THROTTLE = "hipposys:throttle";
        String HIPPOSYS_DESCRIPTION = "hipposys:description";
        String HIPPOSYS_PATH = "hipposys:path";
        String HIPPO_SEQUENCE = "hippo:sequence";
        String HIPPO_CONTENTRESOURCE = "hippo:contentresource";
        String HIPPO_CONTENTROOT = "hippo:contentroot";
        String HIPPO_RELOADONSTARTUP = "hippo:reloadonstartup";
        String HIPPO_VERSION = "hippo:version";
    }

    interface NodeType {
        String HIPPOSYS_UPDATERINFO = "hipposys:updaterinfo";
        String HIPPO_INITIALIZE = "hippo:initialize";
        String HIPPO_INITIALIZEFOLDER = "hippo:initializefolder";
    }

    interface ValueType {
        String STRING = "String";
        String NAME = "Name";
        String LONG = "Long";
        String BOOLEAN = "Boolean";
    }
}
