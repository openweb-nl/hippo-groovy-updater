/*
 * Copyright 2017 Open Web IT B.V. (https://www.openweb.nl/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nl.openweb.hippo.groovy.model;

/**
 * Constants for generating bootstrap xml
 */
public final class Constants {
    private Constants(){
        //no public construction
    }

    public final class Files {
        private Files(){
            //no public construction
        }

        public static final String GROOVY_EXTENSION = ".groovy";
        public static final String XML_EXTENSION = ".xml";
        public static final String YAML_EXTENSION = ".yaml";
        public static final String ECM_EXTENSIONS_NAME = "hippoecm-extension.xml";
    }

    public final class PropertyName {
        private PropertyName(){
            //no public construction
        }

        public static final String JCR_PRIMARY_TYPE = "jcr:primaryType";
        public static final String HIPPOSYS_BATCHSIZE = "hipposys:batchsize";
        public static final String HIPPOSYS_DRYRUN = "hipposys:dryrun";
        public static final String HIPPOSYS_PARAMETERS = "hipposys:parameters";
        public static final String HIPPOSYS_QUERY = "hipposys:query";
        public static final String HIPPOSYS_SCRIPT = "hipposys:script";
        public static final String HIPPOSYS_THROTTLE = "hipposys:throttle";
        public static final String HIPPOSYS_DESCRIPTION = "hipposys:description";
        public static final String HIPPOSYS_PATH = "hipposys:path";
        public static final String HIPPO_SEQUENCE = "hippo:sequence";
        public static final String HIPPO_CONTENTRESOURCE = "hippo:contentresource";
        public static final String HIPPO_CONTENTROOT = "hippo:contentroot";
        public static final String HIPPO_RELOADONSTARTUP = "hippo:reloadonstartup";
        public static final String HIPPO_VERSION = "hippo:version";
    }

    public final class NodeType {
        private NodeType(){
            //no public construction
        }

        public static final String HIPPOSYS_UPDATERINFO = "hipposys:updaterinfo";
        public static final String HIPPO_INITIALIZEITEM = "hippo:initializeitem";
        public static final String HIPPO_INITIALIZE = "hippo:initialize";
        public static final String HIPPO_INITIALIZEFOLDER = "hippo:initializefolder";
    }

    public final class ValueType {
        private ValueType(){
            //no public construction
        }

        public static final String STRING = "String";
        public static final String NAME = "Name";
        public static final String LONG = "Long";
        public static final String BOOLEAN = "Boolean";
        public static final String DOUBLE = "Double";
    }
}
