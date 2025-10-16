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
