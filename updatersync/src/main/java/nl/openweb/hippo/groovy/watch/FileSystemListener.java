/*
 * Copied from webfiles service module
 *
 * Copyright 2014-2015 Hippo B.V. (http://www.onehippo.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.openweb.hippo.groovy.watch;

import java.nio.file.Path;

/**
 * Callbacks of a {@link FileSystemObserver}. Each set of changes always starts with {@link #fileSystemChangesStarted()}
 * and ends with {@link #fileSystemChangesStopped()}.
 */
public interface FileSystemListener {

    void fileSystemChangesStarted();

    void directoryCreated(final Path directory);

    void directoryModified(final Path directory);

    void directoryDeleted(final Path directory);

    void fileCreated(final Path file);

    void fileModified(final Path file);

    void fileDeleted(final Path file);

    void fileSystemChangesStopped();
}
