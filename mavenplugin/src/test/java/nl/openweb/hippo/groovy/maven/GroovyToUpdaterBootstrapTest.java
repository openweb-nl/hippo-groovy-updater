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
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.maven.execution.DefaultMavenExecutionRequest;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.plugin.Mojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.testing.MojoRule;
import org.apache.maven.plugin.testing.resources.TestResources;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.project.ProjectBuildingRequest;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static nl.openweb.tools.Tools.compareFolders;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class GroovyToUpdaterBootstrapTest {
    @Rule
    public MojoRule rule = new MojoRule() {
        @Override
        protected void before() throws Throwable {
            super.before();
        }

        @Override
        protected void after() {
            super.after();
        }
    };

    @Rule
    public TestResources resources = new TestResources();

    private Mojo getMojo(String goal, File pomFile) {
        MavenExecutionRequest executionRequest = new DefaultMavenExecutionRequest();
        ProjectBuildingRequest buildingRequest = executionRequest.getProjectBuildingRequest();

        buildingRequest.setRepositorySession(new DefaultRepositorySystemSession());
        try {
            ProjectBuilder projectBuilder = rule.lookup(ProjectBuilder.class);
            MavenProject project = projectBuilder.build(pomFile, buildingRequest).getProject();
            return rule.lookupConfiguredMojo(project, goal);
        } catch (Exception e) {
            System.out.println("Error getting mojo: ");
            e.printStackTrace();
        }
        return null;
    }

    @Test
    public void testYaml() throws Exception {
        File basedir = resources.getBasedir("");
        File target = new File(basedir, "target/classes");
        File pom = new File(basedir, "pom-to-test-yaml.xml");
        target.mkdirs();

        FileUtils.copyFile(new File(basedir, "src/main/resources/hcm-actions.yaml"), new File(target, "hcm-actions.yaml"));

        runMojo(pom, "generate-yaml");

        File yaml_expected = new File(getClass().getResource("/target_yaml").toURI());
        compareFolders(yaml_expected, target);
    }

    @Test
    public void testYaml2() throws Exception {
        File basedir = resources.getBasedir("");
        File target = new File(basedir, "target/classes2");
        File pom = new File(basedir, "pom-to-test-yaml-2.xml");
        target.mkdirs();

        FileUtils.copyFile(new File(basedir, "src/main/resources/hcm-actions.yaml"), new File(target, "hcm-actions.yaml"));

        runMojo(pom, "generate-yaml");

        File yaml_expected = new File(getClass().getResource("/target_yaml2").toURI());
        compareFolders(yaml_expected, target);
    }

    private void runMojo(File pom, String goal) throws MojoFailureException, MojoExecutionException {
        assertNotNull(pom);
        assertTrue(pom.exists());

        GroovyToUpdaterBootstrap myMojo = (GroovyToUpdaterBootstrap) getMojo(goal, pom);
        assertNotNull(myMojo);
        myMojo.execute();
    }
}