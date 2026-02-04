/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
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
