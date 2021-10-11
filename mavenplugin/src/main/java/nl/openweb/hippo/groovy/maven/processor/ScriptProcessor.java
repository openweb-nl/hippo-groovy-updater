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
package nl.openweb.hippo.groovy.maven.processor;

import java.io.File;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

import nl.openweb.hippo.groovy.model.ScriptClass;
import static java.util.stream.Collectors.toList;
import static nl.openweb.hippo.groovy.ScriptClassFactory.getScriptClasses;

public abstract class ScriptProcessor {
    protected Log log;
    protected File sourceDir;
    protected File targetDir;

    /**
     * Generate updater files from groovy scripts
     *
     * @return list of valid parsed groovy files
     */
    public List<ScriptClass> processUpdateScripts() throws MojoExecutionException {
        return processUpdateScripts(getScriptClasses(sourceDir));
    }

    public abstract List<ScriptClass> processUpdateScripts(final List<ScriptClass> scriptClasses) throws MojoExecutionException;

    /**
     * Generate updater files from groovy scripts
     *
     * @param scriptClasses groovy scripts to parse
     * @return list of valid parsed groovy files
     */
    protected List<ScriptClass> processGroovyScripts(final List<ScriptClass> scriptClasses) {
        getLog().info("Converting " + scriptClasses.size() + " groovy scripts to bootstrap format");
        return scriptClasses.stream().filter(this::processUpdateScript).collect(toList());
    }

    /**
     * Generate updater bootstrap file from groovy file
     *
     * @param scriptClass groovy script to parse
     * @return parsing successful
     */
    protected abstract boolean processUpdateScript(final ScriptClass scriptClass);

    protected Log getLog() {
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
}
