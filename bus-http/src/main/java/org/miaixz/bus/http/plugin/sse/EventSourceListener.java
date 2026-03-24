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
package org.miaixz.bus.http.plugin.sse;

import org.miaixz.bus.http.Response;

/**
 * A listener for server-sent events (SSE), defining callback methods for handling the lifecycle and events of an event
 * source. Subclasses can override these methods to respond to the opening, event reception, closing, or failure of an
 * event source.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public abstract class EventSourceListener {

    /**
     * Invoked when the event source has been accepted by the remote peer and may begin transmitting events.
     *
     * @param eventSource The event source instance.
     * @param response    The HTTP response from the server.
     */
    public void onOpen(EventSource eventSource, Response response) {

    }

    /**
     * Invoked when a new server-sent event is received.
     *
     * @param eventSource The event source instance.
     * @param id          The event ID, which may be null.
     * @param type        The event type, which may be null.
     * @param data        The event data.
     */
    public void onEvent(EventSource eventSource, String id, String type, String data) {

    }

    /**
     * Invoked when the event source has been closed normally. No further calls to this listener will be made.
     *
     * @param eventSource The event source instance.
     */
    public void onClosed(EventSource eventSource) {

    }

    /**
     * Invoked when the event source has been closed due to a network error. It is possible that some incoming events
     * have been lost. No further calls to this listener will be made.
     *
     * @param eventSource The event source instance.
     * @param throwable   The exception that occurred, which may be null.
     * @param response    The HTTP response from the server, which may be null.
     */
    public void onFailure(EventSource eventSource, Throwable throwable, Response response) {

    }

}
