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

import nl.openweb.hippo.groovy.annotations.Bootstrap;

/**
 * Default Bootstrap object
 */
@Bootstrap
public class DefaultBootstrap {

    private DefaultBootstrap() {
        //no instantiating
    }

    private static final Bootstrap bootstrap = DefaultBootstrap.class.getAnnotation(Bootstrap.class);

    public static Bootstrap getBootstrap() {
        return bootstrap;
    }
}
