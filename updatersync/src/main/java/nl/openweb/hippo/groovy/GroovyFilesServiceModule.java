/*
 * Copyright 2014-2015 Hippo B.V. (http://www.onehippo.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nl.openweb.hippo.groovy;

import nl.openweb.hippo.groovy.watch.GroovyFilesWatcher;
import org.onehippo.cms7.services.HippoServiceRegistry;
import org.onehippo.cms7.services.autoreload.AutoReloadService;
import org.onehippo.cms7.services.webfiles.WebFilesService;
import org.onehippo.cms7.services.webfiles.watch.GlobFileNameMatcher;
import org.onehippo.cms7.services.webfiles.watch.WebFilesWatcherConfig;
import org.onehippo.cms7.services.webfiles.watch.WebFilesWatcherJcrConfig;
import org.onehippo.repository.modules.AbstractReconfigurableDaemonModule;
import org.onehippo.repository.modules.RequiresService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

@RequiresService(types = AutoReloadService.class, optional = true )
public class GroovyFilesServiceModule extends AbstractReconfigurableDaemonModule {

    private GroovyFilesServiceImpl service;
    private WebFilesWatcherConfig config;
    private GroovyFilesWatcher watcher;
    private static final Logger LOG = LoggerFactory.getLogger(GroovyFilesServiceModule.class);

    @Override
    protected void doConfigure(final Node moduleConfig) throws RepositoryException {
        if (config == null) {
            config = new WebFilesWatcherJcrConfig(moduleConfig);
        }
    }

    @Override
    protected void onConfigurationChange(final Node moduleConfig) throws RepositoryException {
        doShutdown();
        service = null;
        config = null;
        watcher = null;
        super.onConfigurationChange(moduleConfig);
        doInitialize(session);
    }

    @Override
    protected void doInitialize(final Session session) throws RepositoryException {
        final GlobFileNameMatcher watchedFiles = new GlobFileNameMatcher();
        watchedFiles.includeFiles(config.getIncludedFiles());
        watchedFiles.excludeDirectories(config.getExcludedDirectories());

        LOG.debug("Starting Service for checking Groovy");
        service = new GroovyFilesServiceImpl();
        HippoServiceRegistry.registerService(service, GroovyFilesService.class);

        watcher = new GroovyFilesWatcher(config, service, session);
    }

    @Override
    protected void doShutdown() {
        if (service != null) {
            HippoServiceRegistry.unregisterService(service, WebFilesService.class);
        }
        if (watcher != null) {
            try {
                watcher.shutdown();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
