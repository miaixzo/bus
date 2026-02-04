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
package org.miaixz.bus.http.plugin.sse;

import org.miaixz.bus.http.Request;

/**
 * An interface for a Server-Sent Events (SSE) source, defining the basic operations for interacting with an event
 * source. Implementations of this interface are responsible for managing the SSE connection, including retrieving the
 * original request and canceling the connection.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface EventSource {

    /**
     * Returns the original request that initiated this event source.
     *
     * @return The original HTTP request.
     */
    Request request();

    /**
     * Immediately and forcefully releases the resources held by this event source. If the event source is already
     * closed or canceled, this method has no effect.
     */
    void cancel();

    /**
     * A factory for creating new {@link EventSource} instances.
     */
    interface Factory {

        /**
         * Creates and immediately returns a new event source. Creating an event source initiates an asynchronous
         * process to connect to the server. The listener will be notified when the connection is successful or fails.
         * The caller must cancel the returned event source when it is no longer in use.
         *
         * @param request  The HTTP request used to initiate the event source.
         * @param listener The event source listener to receive connection status and event data.
         * @return A new {@link EventSource} instance.
         */
        EventSource newEventSource(Request request, EventSourceListener listener);
    }

}
