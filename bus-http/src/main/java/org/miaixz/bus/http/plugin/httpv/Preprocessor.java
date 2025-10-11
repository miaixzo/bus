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
package org.miaixz.bus.http.plugin.httpv;

import org.miaixz.bus.http.Httpv;

/**
 * An interface for a preprocessor that executes before an HTTP request is sent. Preprocessors can operate
 * asynchronously and are ideal for tasks like adding authentication tokens, modifying headers, or logging requests.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface Preprocessor {

    /**
     * Performs the preprocessing logic. After processing, {@link PreChain#proceed()} must be called to continue the
     * request execution, unless the request is to be intentionally blocked.
     *
     * @param chain The processing chain, which provides context and a way to continue the request.
     */
    void doProcess(PreChain chain);

    /**
     * Represents a chain of preprocessors. It provides access to the current HTTP task and allows the chain to proceed
     * to the next preprocessor or to the final request execution.
     */
    interface PreChain {

        /**
         * Gets the current HTTP task being processed.
         *
         * @return The current {@link CoverHttp} task.
         */
        CoverHttp<?> getTask();

        /**
         * Gets the HTTP client instance.
         *
         * @return The {@link Httpv} client instance.
         */
        Httpv getHttp();

        /**
         * Passes control to the next preprocessor in the chain. If this is the last preprocessor, it will trigger the
         * execution of the actual HTTP request.
         */
        void proceed();

    }

}
