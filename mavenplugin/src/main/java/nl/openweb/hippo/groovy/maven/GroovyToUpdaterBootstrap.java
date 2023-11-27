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

package nl.openweb.hippo.groovy.maven;

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Parameter;

import nl.openweb.hippo.groovy.Generator;
import nl.openweb.hippo.groovy.annotations.Bootstrap;
import nl.openweb.hippo.groovy.maven.processor.ScriptProcessor;

/**
 * Groovy Updater Maven Plugin to generate bootstrap from groovy files
 */
public abstract class GroovyToUpdaterBootstrap extends AbstractMojo {

    @Parameter(defaultValue = "${project.build.scriptSourceDirectory}", property = "sourceDir")
    private File sourceDir;
    @Parameter(defaultValue = "${project.build.outputDirectory}", property = "targetDir")
    private File targetDir;
    @Parameter(defaultValue = "queue", property = "defaultContentRoot")
    private String defaultContentRoot;

    public void execute() {
        logPluginConfigurationItems();
        getLog().info("Add outputDirectory to classpath for project files: " + targetDir.getPath());
        getProcessor().processUpdateScripts();
    }

    protected void logPluginConfigurationItems() {
        final Log log = getLog();
        log.info("sourceDir: " + sourceDir.getAbsolutePath());
        log.info("targetDir: " + targetDir.getAbsolutePath());
    }

    private ScriptProcessor getProcessor() {
        ScriptProcessor processor = getProcessorBase();
        processor.setLog(getLog());
        processor.setTargetDir(targetDir);
        processor.setSourceDir(sourceDir);
        Generator.setDefaultContentRoot(defaultContentRoot.equalsIgnoreCase("registry") ?
            Bootstrap.ContentRoot.REGISTRY : Bootstrap.ContentRoot.QUEUE);
        return processor;
    }

    protected abstract ScriptProcessor getProcessorBase();
}
