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

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.commons.io.FileUtils;

public class Utilities {
    public static void enforceWindowsFileEndings(File file) throws IOException {
        String content = FileUtils.readFileToString(file, Charset.defaultCharset());
        String lfContent = content.replaceAll("\r\n", "\n");
        FileUtils.writeStringToFile(file,lfContent.replaceAll("\n", "\r\n"), Charset.defaultCharset());
    }
}
