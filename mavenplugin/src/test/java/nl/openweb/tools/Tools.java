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

package nl.openweb.tools;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;

public final class Tools {
    static Logger logger = LoggerFactory.getLogger(Tools.class);

    private Tools() {
    }

    public static String getEnvironmentInfo() {
        final StringBuilder output = new StringBuilder();
        System.getenv().forEach((key, value) -> output.append(
                key)
                .append(": ")
                .append(value)
                .append("\n"));
        return output.toString();
    }

    public static void compareFolders(File expected, File result) throws IOException {
        final CollectFilesVisitor visitor = new CollectFilesVisitor();
        final CollectFilesVisitor visitor2 = new CollectFilesVisitor();
        List<Path> resultFilesPaths = new ArrayList<>();
        List<Path> resultFoldersPaths = new ArrayList<>();
        List<Path> expectedFilePaths = new ArrayList<>();
        List<Path> expectedFolderPaths = new ArrayList<>();

        visitor.setCollectedFilesList(resultFilesPaths);
        visitor.setCollectedFoldersList(resultFoldersPaths);
        visitor2.setCollectedFilesList(expectedFilePaths);
        visitor2.setCollectedFoldersList(expectedFolderPaths);

        Files.walkFileTree(result.toPath(), visitor);
        Files.walkFileTree(expected.toPath(), visitor2);

        logger.info("comparing {} paths", expectedFilePaths.size());
        assertContentCompares(expectedFilePaths, resultFilesPaths);
        assertNameCompares(expectedFilePaths, resultFilesPaths, expected.getAbsolutePath(), result.getAbsolutePath());
        assertNameCompares(expectedFolderPaths, resultFoldersPaths, expected.getAbsolutePath(), result.getAbsolutePath());

    }

    private static void assertNameCompares(final List<Path> expectedFolderPaths, final List<Path> resultFoldersPaths, final String expectedSourcePath, final String resultSourcePath) {
        assertEquals(expectedFolderPaths.size(), resultFoldersPaths.size());
        for(int i = 1; i < expectedFolderPaths.size(); i++) {
            Path expectedPath = expectedFolderPaths.get(i);
            Path resultPath = resultFoldersPaths.get(i);
            logger.info("Comparing {} and {}", expectedPath.toString(), resultPath.toString());
            assertEquals(expectedPath.getName(expectedPath.getNameCount() - 1),
                    resultPath.getName(resultPath.getNameCount() - 1));
            assertEquals(expectedPath.toString().substring(expectedSourcePath.length()),
                    resultPath.toString().substring(resultSourcePath.length()));
        }
    }

    private static void assertContentCompares(final List<Path> expectedPaths, final List<Path> resultPaths) throws IOException {
        assertEquals(expectedPaths.size(), resultPaths.size());
        for(int i = 0; i < expectedPaths.size(); i++){
            Path expectedPath = expectedPaths.get(i);
            Path resultPath = resultPaths.get(i);
            logger.info("Comparing {} and {}", expectedPath.toString(), resultPath.toString());
            assertEquals("Filecount is wrong", expectedPath.getName(expectedPath.getNameCount() - 1),
                    resultPath.getName(resultPath.getNameCount() - 1));
            assertEquals("Files differ! Incorrect tranform!", Files.readAllLines(expectedPath), Files.readAllLines(resultPath));
        }
    }

    private static class CollectFilesVisitor extends SimpleFileVisitor<Path> {
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
