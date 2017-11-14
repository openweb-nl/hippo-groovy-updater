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

package nl.openweb.hippo.groovy.maven;import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.openweb.hippo.groovy.maven.processor.ScriptProcessor;
import nl.openweb.hippo.groovy.maven.processor.ScriptProcessorXML;
import nl.openweb.hippo.groovy.maven.processor.ScriptProcessorYAML;
import static nl.openweb.hippo.groovy.ScriptClassFactory.getScriptClasses;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class TestMavenPlugin{

    Logger logger = LoggerFactory.getLogger(TestMavenPlugin.class);
    ScriptProcessor processor;

    @Test
    public void testXML() throws URISyntaxException, MojoExecutionException, IOException {
        processor = new ScriptProcessorXML();
        processor.setInitializeNamePrefix("sampleproject-update-");
        processor.setLog(new SystemStreamLog());

        File input = new File(getClass().getResource("/src/scripts").toURI());
        File xml_output = new File(new File(getClass().getResource("/").toURI()), "xml_output");

        File resource = new File(getClass()
                .getResource("/src/resources/hippoecm-extension.xml").toURI());
        File targetResource = new File(xml_output, "hippoecm-extension.xml");
        if(xml_output.exists()){
            FileUtils.deleteDirectory(xml_output);
            assertFalse(xml_output.exists());
        }
        //Preparation: existing ecm-extension.xml in target
        targetResource.mkdirs();
        Files.copy(resource.toPath(), targetResource.toPath(), StandardCopyOption.REPLACE_EXISTING);
        processor.setSourceDir(input);
        processor.setTargetDir(xml_output);

        processor.processUpdateScripts(getScriptClasses(input));
        File xml_expected = new File(getClass().getResource("/target_xml").toURI());

        compareFolders(xml_expected, xml_output);
    }

    @Test
    public void testYaml() throws URISyntaxException, MojoExecutionException, IOException {
        processor = new ScriptProcessorYAML();
        processor.setInitializeNamePrefix("my-hippo-updater-");
        processor.setLog(new SystemStreamLog());

        File input = new File(getClass().getResource("/src/scripts").toURI());
        File yaml_output = new File(new File(getClass().getResource("/").toURI()), "yaml_output");

        File resource = new File(getClass()
                .getResource("/src/resources/hcm-actions.yaml").toURI());
        File targetResource = new File(yaml_output, "hcm-actions.yaml");
        if(yaml_output.exists()){
            FileUtils.deleteDirectory(yaml_output);
            assertFalse(yaml_output.exists());
        }
        processor.setSourceDir(input);
        processor.setTargetDir(yaml_output);
        ((ScriptProcessorYAML)processor).setYamlPath("hcm-content/configuration/update");

        if(yaml_output.exists()){
            FileUtils.deleteDirectory(yaml_output);
            assertFalse(yaml_output.exists());
        }
        targetResource.mkdirs();
        Files.copy(resource.toPath(), targetResource.toPath(), StandardCopyOption.REPLACE_EXISTING);
        processor.processUpdateScripts(getScriptClasses(input));
        File yaml_expected = new File(getClass().getResource("/target_yaml").toURI());

        compareFolders(yaml_expected, yaml_output);
    }

    public void compareFolders(File expected, File result) throws IOException {
        final CollectFilesVisitor visitor = new CollectFilesVisitor();
        List<Path> resultFilesPaths = new ArrayList<>();
        List<Path> resultFoldersPaths = new ArrayList<>();

        visitor.setCollectedFilesList(resultFilesPaths);
        visitor.setCollectedFoldersList(resultFoldersPaths);

        Files.walkFileTree(result.toPath(), visitor);

        List<Path> expectedFilePaths = new ArrayList<>();
        List<Path> expectedFolderPaths = new ArrayList<>();
        visitor.setCollectedFilesList(expectedFilePaths);
        visitor.setCollectedFoldersList(expectedFolderPaths);

        Files.walkFileTree(expected.toPath(), visitor);

        logger.info("comparing {} paths", expectedFilePaths.size());
        assertContentCompares(expectedFilePaths, resultFilesPaths);
        assertNameCompares(expectedFolderPaths, resultFoldersPaths);

    }

    private void assertNameCompares(final List<Path> expectedFolderPaths, final List<Path> resultFoldersPaths) {
        assertEquals(expectedFolderPaths.size(), resultFoldersPaths.size());
        for(int i = 1; i < expectedFolderPaths.size(); i++) {
            Path expectedPath = expectedFolderPaths.get(i);
            Path resultPath = resultFoldersPaths.get(i);
            logger.info("Comparing {} and {}", expectedPath.toString(), resultPath.toString());
            assertEquals(expectedPath.getName(expectedPath.getNameCount() - 1),
                    resultPath.getName(resultPath.getNameCount() - 1));
        }
    }

    private void assertContentCompares(final List<Path> expectedPaths, final List<Path> resultPaths) throws IOException {
        assertEquals(expectedPaths.size(), resultPaths.size());
        for(int i = 0; i < expectedPaths.size(); i++){

            Path expectedPath = expectedPaths.get(i);
            Path resultPath = resultPaths.get(i);
            logger.info("Comparing {} and {}", expectedPath.toString(), resultPath.toString());
            assertEquals("Filecount is wrong", expectedPath.getName(expectedPath.getNameCount() - 1),
                    resultPath.getName(resultPath.getNameCount() - 1));
            assertEquals("Files differ! Incorrect tranform!",Files.readAllLines(expectedPath), Files.readAllLines(resultPath));
        }
    }

    private class CollectFilesVisitor extends SimpleFileVisitor<Path>{
        private List<Path> collectedFilesList;
        private List<Path> collectedFoldersList;

        public void setCollectedFilesList(final List<Path> collectedFilesList) {
            this.collectedFilesList = collectedFilesList;
        }

        public void setCollectedFoldersList(final List<Path> collectedFoldersList) {
            this.collectedFoldersList = collectedFoldersList;
        }

        @Override
        public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
            collectedFilesList.add(file);
            return super.visitFile(file, attrs) ;
        }

        @Override
        public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) throws IOException {
            collectedFoldersList.add(dir);
            return super.preVisitDirectory(dir, attrs);
        }
    }
}