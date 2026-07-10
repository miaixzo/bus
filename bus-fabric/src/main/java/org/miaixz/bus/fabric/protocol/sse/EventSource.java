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
package org.miaixz.bus.fabric.protocol.sse;

import org.miaixz.bus.fabric.protocol.http.HttpRequest;

/**
 * Event source facade backed by the current SSE session model.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface EventSource {

    /**
     * Returns the original HTTP request.
     *
     * @return request
     */
    HttpRequest request();

    /**
     * Cancels the event source and releases the current SSE session when present.
     */
    void cancel();

    /**
     * Factory contract for event source instances.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    interface Factory {

        /**
         * Creates and asynchronously opens an event source.
         *
         * @param request  request
         * @param listener listener
         * @return event source
         */
        EventSource newEventSource(HttpRequest request, EventSourceListener listener);

    }

}
