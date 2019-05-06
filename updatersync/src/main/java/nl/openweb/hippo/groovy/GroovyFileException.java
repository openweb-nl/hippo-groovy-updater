/*
 * Modifications Copyright 2019 Open Web IT B.V. (https://www.openweb.nl/)
 *
 * This file is Copyrighted to Hippo B.V. but there has been modification done to
 * via Open Web IT B.V. these modification (See the modification via version control history)
 * are licence to Open Web IT B.V.
 * Under Apache License, Version 2.0. Please see the Original Copyright notice blew.
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
 *
 * Copyright 2014-2015 Hippo B.V. (http://www.onehippo.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.openweb.hippo.groovy;

public class GroovyFileException extends RuntimeException {

    public GroovyFileException() {
        super();
    }

    public GroovyFileException(final String message) {
        super(message);
    }

    public GroovyFileException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public GroovyFileException(final Throwable cause) {
        super(cause);
    }
}
