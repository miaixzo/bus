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
import org.miaixz.bus.http.Response;

/**
 * A listener for events related to a WebSocket connection.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class WebSocketListener {

    /**
     * Invoked when a web socket has been accepted by the remote peer and may begin transmitting messages.
     *
     * @param webSocket The WebSocket that has been opened.
     * @param response  The HTTP response from the upgrade request.
     */
    public void onOpen(WebSocket webSocket, Response response) {

    }

    /**
     * Invoked when a text (type {@code 0x1}) message has been received.
     *
     * @param webSocket The WebSocket that received the message.
     * @param text      The text content of the message.
     */
    public void onMessage(WebSocket webSocket, String text) {

    }

    /**
     * Invoked when a binary (type {@code 0x2}) message has been received.
     *
     * @param webSocket The WebSocket that received the message.
     * @param bytes     The binary content of the message.
     */
    public void onMessage(WebSocket webSocket, ByteString bytes) {

    }

    /**
     * Invoked when the remote peer has indicated that no more incoming messages will be transmitted.
     *
     * @param webSocket The WebSocket that is closing.
     * @param code      The status code from the remote peer.
     * @param reason    The reason for the closure, or an empty string.
     */
    public void onClosing(WebSocket webSocket, int code, String reason) {

    }

    /**
     * Invoked when both peers have indicated that no more messages will be transmitted and the connection has been
     * successfully released. No further calls to this listener will be made.
     *
     * @param webSocket The WebSocket that has been closed.
     * @param code      The status code from the remote peer.
     * @param reason    The reason for the closure, or an empty string.
     */
    public void onClosed(WebSocket webSocket, int code, String reason) {

    }

    /**
     * Invoked when a web socket has been closed due to an error reading from or writing to the network. Both outgoing
     * and incoming messages may have been lost. No further calls to this listener will be made.
     *
     * @param webSocket The WebSocket that failed.
     * @param throwable The exception that caused the failure.
     * @param response  The HTTP response from the upgrade request, or null if no response was received.
     */
    public void onFailure(WebSocket webSocket, Throwable throwable, Response response) {

    }

}
