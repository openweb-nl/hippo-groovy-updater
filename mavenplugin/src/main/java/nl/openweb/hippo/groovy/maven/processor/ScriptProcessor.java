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
package nl.openweb.hippo.groovy.maven.processor;

import java.io.File;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

import static java.util.stream.Collectors.toList;
import static nl.openweb.hippo.groovy.Generator.getGroovyFiles;

public abstract class ScriptProcessor {
    protected Log log;
    protected File sourceDir;
    protected File targetDir;
    protected String initializeNamePrefix;

    /**
     * Generate updater files from groovy scripts
     *
     * @return list of valid parsed groovy files
     */
    public List<File> processUpdateScripts() throws MojoExecutionException {
        return processUpdateScripts(getGroovyFiles(sourceDir));
    }

    /**
     * Generate updater files from groovy scripts
     *
     * @param groovyFiles groovy scripts to parse
     * @return list of valid parsed groovy files
     */
    public List<File> processUpdateScripts(final List<File> groovyFiles) throws MojoExecutionException {
        getLog().info("Converting " + groovyFiles.size() + " groovy scripts to bootstrap format");
        return groovyFiles.stream().filter(this::processUpdateScript).collect(toList());
    }

    /**
     * Generate updater bootstrap file from groovy file
     *
     * @param file groovy script to parse
     * @return parsing successful
     */
    abstract protected boolean processUpdateScript(final File file);

    protected Log getLog(){
        return log;
    }

    public void setLog(final Log log) {
        this.log = log;
    }

    public void setSourceDir(final File sourceDir) {
        this.sourceDir = sourceDir;
    }

    public void setTargetDir(final File targetDir) {
        this.targetDir = targetDir;
    }

    public void setInitializeNamePrefix(final String initializeNamePrefix) {
        this.initializeNamePrefix = initializeNamePrefix;
    }

}
