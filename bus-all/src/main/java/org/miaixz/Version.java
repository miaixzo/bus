/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz;

import org.miaixz.bus.core.lang.Keys;

/**
 * <p>
 * Version (Application/Service Bus) is a microservice suite and foundational framework. It is written based on Java 8,
 * referencing and drawing inspiration from the design of many existing frameworks and components. It can serve as a
 * foundational middleware for backend service development. The code is concise and the architecture is clear, making it
 * very suitable for learning purposes.
 *
 * <p>
 * The goal is to strive to create a full-stack technical solution that enables the rapid implementation of business
 * requirements, covering everything from the foundational framework - distributed microservice architecture -
 * continuous integration - automated deployment - system monitoring, and more. Additionally, the encapsulated utilities
 * cover strings, numbers, collections, encoding, dates, files, IO, encryption/decryption, JSON, HTTP clients, etc.
 *
 * <p>
 * Contributions in all forms are welcome, including but not limited to optimizations, feature additions, improvements
 * to documentation and code, and reporting of issues and bugs.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class Version extends org.miaixz.bus.core.Version {

    /**
     * Version object, format: tok+ ( '-' tok+)? ( '+' tok+)? Versions are separated by '.' or '-', and the version
     * number may contain '+'. The numeric parts are compared by magnitude, and the string parts are compared
     * lexicographically.
     *
     * <ol>
     * <li>sequence: Major version number</li>
     * <li>pre: Pre-release version number</li>
     * <li>build: Build version</li>
     * </ol>
     *
     * @param v The version string.
     */
    public Version(String v) {
        super(v);
    }

    public String name() {
        return Keys.BUS;
    }

}
