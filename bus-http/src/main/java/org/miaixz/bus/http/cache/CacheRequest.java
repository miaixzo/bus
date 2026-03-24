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
package org.miaixz.bus.http.cache;

import org.miaixz.bus.core.io.sink.Sink;

import java.io.IOException;

/**
 * An interface for writing a response to the cache.
 * <p>
 * Implementations of this interface provide a {@link Sink} to which the response body can be written.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface CacheRequest {

    /**
     * Returns a sink that can be used to write the response body to the cache.
     *
     * @return The sink for the response body.
     * @throws IOException if an I/O error occurs.
     */
    Sink body() throws IOException;

    /**
     * Aborts this cache request.
     * <p>
     * This should be called when the response body cannot be written, for example, due to a network error.
     * </p>
     */
    void abort();

}
