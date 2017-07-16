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

package nl.openweb.hippo.groovy.maven;

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Parameter;

import nl.openweb.hippo.groovy.maven.processor.ScriptProcessor;
import static nl.openweb.hippo.groovy.Generator.addClassPath;

/**
 * Groovy Updater Maven Plugin to generate bootstrap from groovy files
 */
public abstract class GroovyToUpdaterBootstrap extends AbstractMojo {

    @Parameter(defaultValue = "${project.build.scriptSourceDirectory}", property = "sourceDir")
    private File sourceDir;
    @Parameter(defaultValue = "${project.build.outputDirectory}", property = "targetDir")
    private File targetDir;
    @Parameter(defaultValue = "hippo-updater-", property = "initializeNamePrefix")
    private String initializeNamePrefix;

    public void execute() throws MojoExecutionException, MojoFailureException {
        logPluginConfigurationItems();
        getLog().info("Add outputDirectory to classpath for project files: " + targetDir.getPath());
        addClassPath(targetDir.getPath());

        getProcessor().processUpdateScripts();
    }

    protected void logPluginConfigurationItems(){
        final Log log = getLog();
        log.info("sourceDir: " + sourceDir.getAbsolutePath());
        log.info("targetDir: " + targetDir.getAbsolutePath());
        log.info("initializeNamePrefix: " + initializeNamePrefix);

    }

    private ScriptProcessor getProcessor(){
        ScriptProcessor processor = getProcessorBase();
        processor.setLog(getLog());
        processor.setTargetDir(targetDir);
        processor.setSourceDir(sourceDir);
        processor.setInitializeNamePrefix(initializeNamePrefix);
        return processor;
    }

    protected abstract ScriptProcessor getProcessorBase();
}
