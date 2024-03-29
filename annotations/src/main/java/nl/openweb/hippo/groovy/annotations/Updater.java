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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Updater annotation to define properties for the updater
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Updater {
    enum LogTarget {
        DEFAULT(""),
        LOG_FILES("LOG FILES"),
        REPOSITORY("REPOSITORY");

        private final String value;

        LogTarget(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }
    String name();

    String description() default "";

    String path() default "";

    String xpath() default "";

    long batchSize() default 10L;

    boolean dryRun() default false;

    String parameters() default "";

    long throttle() default 1000L;

    String mixin() default "";

    LogTarget logTarget() default LogTarget.DEFAULT;
}
