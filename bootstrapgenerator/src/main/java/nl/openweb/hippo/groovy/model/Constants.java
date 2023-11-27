/*
 * Copyright 2019 Open Web IT B.V. (https://www.openweb.nl/)
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
 * Constants for generating bootstrap files
 */
public final class Constants {
    private Constants() {
        //no public construction
    }

    public static final class Files {
        private Files() {
            //no public construction
        }

        public static final String GROOVY_EXTENSION = ".groovy";
        public static final String YAML_EXTENSION = ".yaml";
    }

    public static final class PropertyName {
        private PropertyName() {
            //no public construction
        }

        public static final String JCR_PRIMARY_TYPE = "jcr:primaryType";
        public static final String JCR_MIXIN_TYPES = "jcr:mixinTypes";
        public static final String HIPPOSYS_BATCHSIZE = "hipposys:batchsize";
        public static final String HIPPOSYS_DRYRUN = "hipposys:dryrun";
        public static final String HIPPOSYS_LOGTARGET = "hipposys:logtarget";
        public static final String HIPPOSYS_PARAMETERS = "hipposys:parameters";
        public static final String HIPPOSYS_QUERY = "hipposys:query";
        public static final String HIPPOSYS_SCRIPT = "hipposys:script";
        public static final String HIPPOSYS_THROTTLE = "hipposys:throttle";
        public static final String HIPPOSYS_DESCRIPTION = "hipposys:description";
        public static final String HIPPOSYS_PATH = "hipposys:path";
    }

    public static final class NodeType {
        private NodeType() {
            //no public construction
        }

        public static final String HIPPOSYS_UPDATERINFO = "hipposys:updaterinfo";

    }
}
