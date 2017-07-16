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

public final class Tools {
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
}
