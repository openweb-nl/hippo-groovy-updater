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

import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import nl.openweb.hippo.groovy.maven.processor.ScriptProcessor;
import nl.openweb.hippo.groovy.maven.processor.ScriptProcessorYAML;

@Mojo(name = "generate-yaml", threadSafe = true)
public class GroovyToUpdaterYamlBootstrap extends GroovyToUpdaterBootstrap {
    @Parameter(defaultValue = "hcm-content/configuration/update", property = "yamlContentPath")
    private String yamlContentPath;

    @Parameter(defaultValue = "hcm-config/configuration/update", property = "yamlConfigurationPath")
    private String yamlConfigurationPath;

    @Override
    protected void logPluginConfigurationItems() {
        super.logPluginConfigurationItems();
        getLog().info("yamlContentPath: " + yamlContentPath);
        getLog().info("yamlConfigPath: " + yamlConfigurationPath);
    }

    @Override
    protected ScriptProcessor getProcessorBase() {
        ScriptProcessorYAML processor = new ScriptProcessorYAML();
        processor.setYamlContentPath(yamlContentPath);
        processor.setYamlConfigurationPath(yamlConfigurationPath);
        return processor;
    }
}
