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

import java.io.File;

import nl.openweb.hippo.groovy.annotations.Bootstrap;
import nl.openweb.hippo.groovy.annotations.Exclude;
import nl.openweb.hippo.groovy.annotations.Updater;

public class ScriptClass {
    private final Class<?> interpretClass;
    private final String content;
    private final File file;
    private final Updater updater;
    private final Bootstrap bootstrap;

    public ScriptClass(final File file, Class<?> interpretClass, String content) {
        this.interpretClass = interpretClass;
        this.updater = interpretClass.getAnnotation(Updater.class);
        this.bootstrap = interpretClass.getAnnotation(Bootstrap.class);
        this.content = content;
        this.file = file;
    }

    public File getFile() {
        return file;
    }

    public final Updater getUpdater() {
        return updater;
    }

    public final Bootstrap getBootstrap() {
        return bootstrap;
    }

    public final Bootstrap getBootstrap(boolean defaultToDefault) {
        return interpretClass.isAnnotationPresent(Bootstrap.class) || !defaultToDefault ?
                getBootstrap() :
                DefaultBootstrap.getBootstrap();
    }

    public String getContent() {
        return content;
    }

    public boolean isValid() {
        return interpretClass.isAnnotationPresent(Updater.class);
    }

    public boolean isExcluded() {return interpretClass.isAnnotationPresent(Exclude.class); }
}
