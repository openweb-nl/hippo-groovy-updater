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
package nl.openweb.hippo.groovy.annotations;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BootstrapTest {
    @Test
    public void testBootstrapAnnotation(){
        assertEquals("", Bootstrap.ContentRoot.DEFAULT.toString());
        assertEquals("registry", Bootstrap.ContentRoot.REGISTRY.toString());
        assertEquals("queue", Bootstrap.ContentRoot.QUEUE.toString());
    }
}
