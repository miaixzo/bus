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
package org.miaixz.bus.http.metric;

import org.miaixz.bus.http.Response;

import java.io.IOException;

/**
 * Intercepts, observes, modifies, and potentially short-circuits requests and their corresponding responses. Typically,
 * interceptors add, remove, or transform headers on requests or responses.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface Interceptor {

    /**
     * Intercepts the given {@code chain} to process a network request.
     *
     * @param chain The network call chain.
     * @return The {@link Response} from the network request.
     * @throws IOException if an I/O error occurs during interception.
     */
    Response intercept(NewChain chain) throws IOException;

    /**
     * Provides a description of what this interceptor implementation does. This can be useful for other business
     * scenarios or services, such as distributed tracing.
     */
    default void instructions() {

    }

}
