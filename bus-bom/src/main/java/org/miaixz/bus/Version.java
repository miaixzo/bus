/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus;

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
 * @since Java 17+
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

}
