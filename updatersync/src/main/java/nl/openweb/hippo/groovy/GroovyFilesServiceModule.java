/*
 * Modifications Copyright 2019 Open Web IT B.V. (https://www.openweb.nl/)
 *
 * This file is Copyrighted to Hippo B.V. but there has been modification done to
 * via Open Web IT B.V. these modification (See the modification via version control history)
 * are licence to Open Web IT B.V.
 * Under Apache License, Version 2.0. Please see the Original Copyright notice blew.
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
 *
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

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.onehippo.cms7.services.HippoServiceRegistry;
import org.onehippo.cms7.services.autoreload.AutoReloadService;
import org.onehippo.repository.modules.AbstractReconfigurableDaemonModule;
import org.onehippo.repository.modules.RequiresService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.openweb.hippo.groovy.watch.GroovyFilesWatcher;
import nl.openweb.hippo.groovy.watch.GroovyFilesWatcherConfig;
import nl.openweb.hippo.groovy.watch.GroovyFilesWatcherJcrConfig;

@RequiresService(types = AutoReloadService.class, optional = true)
public class GroovyFilesServiceModule extends AbstractReconfigurableDaemonModule {

    private static final Logger LOGGER = LoggerFactory.getLogger(GroovyFilesServiceModule.class);
    private GroovyFilesServiceImpl service;
    private GroovyFilesWatcherConfig config;
    private GroovyFilesWatcher watcher;

    @Override
    protected void doConfigure(final Node moduleConfig) throws RepositoryException {
        if (config == null) {
            config = new GroovyFilesWatcherJcrConfig(moduleConfig);
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
    protected void doInitialize(final Session session) {
        LOGGER.debug("Starting Service for checking Groovy");
        service = new GroovyFilesServiceImpl();
        HippoServiceRegistry.registerService(service, GroovyFilesService.class);

        watcher = new GroovyFilesWatcher(config, service, session);
    }

    @Override
    protected void doShutdown() {
        if (service != null) {
            HippoServiceRegistry.unregisterService(service, GroovyFilesService.class);
        }
        if (watcher != null) {
            watcher.shutdown();
        }
    }
}
