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

import java.util.Collections;
import java.util.List;

public class GroovyFilesWatcherTestConfig implements GroovyFilesWatcherConfig {
    @Override
    public List<String> getWatchedModules() {
        return Collections.emptyList();
    }

    @Override
    public List<String> getIncludedFiles() {
        return null;
    }

    @Override
    public List<String> getExcludedDirectories() {
        return null;
    }

    @Override
    public List<String> getUseWatchServiceOnOsNames() {
        return null;
    }

    @Override
    public long getWatchDelayMillis() {
        return 0;
    }

    @Override
    public long getMaxFileLengthBytes() {
        return 0;
    }
}
