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

package nl.openweb.hippo.groovy.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Bootstrap annotation to define properties in the hippoecm-extension.xml
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Bootstrap {
    enum ContentRoot{
        DEFAULT(""),
        QUEUE("queue"),
        REGISTRY("registry");

        private String value;

        ContentRoot(String value){
            this.value = value;
        }

        public String toString(){
            return value;
        }
    }

    ContentRoot contentroot() default ContentRoot.DEFAULT;

    double sequence() default 99999.0d;

    boolean reload() default false;

    String version() default "";
}
