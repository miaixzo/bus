/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.http.plugin.sse;

import org.miaixz.bus.http.Response;

/**
 * A listener for server-sent events (SSE), defining callback methods for handling the lifecycle and events of an event
 * source. Subclasses can override these methods to respond to the opening, event reception, closing, or failure of an
 * event source.
 *
 * @author Kimi Liu
 * @since Java 17+
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
