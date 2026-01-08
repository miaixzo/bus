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
package org.miaixz.bus.http.socket;

import org.miaixz.bus.core.io.ByteString;
import org.miaixz.bus.http.Httpd;
import org.miaixz.bus.http.Request;

/**
 * A non-blocking interface to a WebSocket. In normal operation, a WebSocket will be processed through a sequence of
 * states: open, message, and close. Use a {@link WebSocket.Factory} (typically the {@link Httpd} client) to create
 * instances.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface WebSocket {

    /**
     * @return The original request that initiated this WebSocket.
     */
    Request request();

    /**
     * Returns the number of bytes of application data that have been enqueued to be transmitted to the server. This
     * doesn't include framing overhead. It also doesn't include any bytes that have been buffered by the operating
     * system or network intermediaries. This method returns 0 if no messages are waiting in the queue. It may return a
     * non-zero value after the web socket has been canceled; this indicates that enqueued messages were not
     * transmitted.
     *
     * @return The size of the outgoing message queue in bytes.
     */
    long queueSize();

    /**
     * Attempts to enqueue {@code text} to be transmitted as a text (type {@code 0x1}) message.
     * <p>
     * This method returns true if the message was enqueued. Messages that would overflow the outgoing message buffer
     * will be rejected and trigger a {@linkplain #close graceful shutdown} of this web socket. This method returns
     * false in that case, and in any other case where this web socket is closing, closed, or canceled.
     *
     * @param text The text message to send.
     * @return true if the message was successfully enqueued for sending.
     */
    boolean send(String text);

    /**
     * Attempts to enqueue {@code bytes} to be transmitted as a binary (type {@code 0x2}) message.
     * <p>
     * This method returns true if the message was enqueued. Messages that would overflow the outgoing message buffer
     * (16 MiB) will be rejected and trigger a {@linkplain #close graceful shutdown} of this web socket. This method
     * returns false in that case, and in any other case where this web socket is closing, closed, or canceled.
     *
     * @param bytes The binary message to send.
     * @return true if the message was successfully enqueued for sending.
     */
    boolean send(ByteString bytes);

    /**
     * Attempts to initiate a graceful shutdown of this web socket. Any already-enqueued messages will be transmitted
     * before the close message is sent but subsequent calls to {@link #send} will return false and their messages will
     * not be enqueued.
     *
     * @param code   A status code as defined by <a href="http://tools.ietf.org/html/rfc6455#section-7.4">Section 7.4 of
     *               RFC 6455</a>.
     * @param reason A descriptive reason for the close, or {@code null}.
     * @return true if the close message was successfully enqueued.
     * @throws IllegalArgumentException if the code is invalid.
     */
    boolean close(int code, String reason);

    /**
     * Immediately and violently release resources held by this web socket, discarding any enqueued messages. This does
     * nothing if the web socket has already been closed or canceled.
     */
    void cancel();

    /**
     * A factory for creating WebSockets.
     */
    interface Factory {

        /**
         * Creates a new web socket and immediately returns it. Creating a web socket initiates an asynchronous process
         * to connect the socket. Once the socket is successfully connected or fails to connect, the {@code listener}
         * will be notified. The caller must either close or cancel the returned web socket when it is no longer in use.
         *
         * @param request  The HTTP request to upgrade to a WebSocket.
         * @param listener The listener to receive events for this WebSocket.
         * @return The new WebSocket.
         */
        WebSocket newWebSocket(Request request, WebSocketListener listener);
    }

}
