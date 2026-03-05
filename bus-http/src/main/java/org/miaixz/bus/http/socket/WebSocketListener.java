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
