/*
 * Copyright 2023 Open Web IT B.V. (https://www.openweb.nl/)
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

package nl.openweb.hippo.groovy.watch;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Collections;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.sling.testing.mock.jcr.MockJcr;
import org.junit.jupiter.api.Test;

import nl.openweb.hippo.groovy.GroovyFilesServiceImpl;

import static org.junit.jupiter.api.Assertions.*;
import static nl.openweb.hippo.groovy.util.WatchFilesUtils.SCRIPT_ROOT;

class GroovyFilesWatcherTest {

    private final GroovyFilesServiceImpl service = new GroovyFilesServiceImpl();
    private final Session session;

    private final GroovyFilesWatcher watcher;

    GroovyFilesWatcherTest() throws RepositoryException {
        final GroovyFilesWatcherConfig config = new GroovyFilesWatcherTestConfig();
        session = MockJcr.newSession();
        Node node = session.getRootNode();
        for (final String name : SCRIPT_ROOT.split("/")) {
            node = node.addNode(name);
        }
        watcher = new GroovyFilesWatcher(config, service, session);
    }
    @Test
    void onPathsChanged() throws URISyntaxException, RepositoryException {
        URL testfileUrl = getClass().getResource("/updater-script.groovy");

        Path scriptPath = Path.of(testfileUrl.toURI());
        Path rootPath = scriptPath.getParent();
        watcher.onPathsChanged(rootPath, Collections.singleton(scriptPath));
        final long scriptNodes = session.getRootNode().getNode(SCRIPT_ROOT).getNodes().getSize();
        assertEquals(1, scriptNodes, "Script path not processed");
    }
}