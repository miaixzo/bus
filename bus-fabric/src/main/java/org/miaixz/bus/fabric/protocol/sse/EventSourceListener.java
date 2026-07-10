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

import org.miaixz.bus.fabric.protocol.http.HttpResponse;

/**
 * Listener for event source lifecycle and event callbacks.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public abstract class EventSourceListener {

    /**
     * Invoked after the SSE response has been accepted.
     *
     * @param eventSource event source
     * @param response    HTTP response metadata with an empty body
     */
    public void onOpen(final EventSource eventSource, final HttpResponse response) {
        // Default listener intentionally performs no action.
    }

    /**
     * Invoked for each received server-sent event.
     *
     * @param eventSource event source
     * @param id          event id or null
     * @param type        event type
     * @param data        event data
     */
    public void onEvent(final EventSource eventSource, final String id, final String type, final String data) {
        // Default listener intentionally performs no action.
    }

    /**
     * Invoked when the event stream reaches a normal EOF.
     *
     * @param eventSource event source
     */
    public void onClosed(final EventSource eventSource) {
        // Default listener intentionally performs no action.
    }

    /**
     * Invoked when open or stream processing fails.
     *
     * @param eventSource event source
     * @param throwable   failure cause
     * @param response    response metadata when available
     */
    public void onFailure(final EventSource eventSource, final Throwable throwable, final HttpResponse response) {
        // Default listener intentionally performs no action.
    }

}
