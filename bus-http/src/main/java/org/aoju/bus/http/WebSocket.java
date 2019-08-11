/*
 * The MIT License
 *
 * Copyright (c) 2017, aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.http;

import org.aoju.bus.core.io.ByteString;

/**
 * A non-blocking interface to a web socket. Use the {@linkplain WebSocket.Factory factory} to
 * create instances; usually this is {@link HttpClient}.
 *
 * <h3>Web Socket Lifecycle</h3>
 * <p>
 * Upon normal operation each web socket progresses through a sequence of states:
 *
 * <ul>
 * <li><strong>Connecting:</strong> the initial state of each web socket. Messages may be enqueued
 * but they won't be transmitted until the web socket is open.
 * <li><strong>Open:</strong> the web socket has been accepted by the remote peer and is fully
 * operational. Messages in either direction are enqueued for immediate transmission.
 * <li><strong>Closing:</strong> first of the peers on the web socket has initiated a graceful
 * shutdown. The web socket will continue to transmit already-enqueued messages but will
 * refuse to enqueue new ones.
 * <li><strong>Closed:</strong> the web socket has transmitted all of its messages and has
 * received all messages from the peer.
 * </ul>
 * <p>
 * Web sockets may fail due to HTTP upgrade problems, connectivity problems, or if either peer
 * chooses to short-circuit the graceful shutdown process:
 *
 * <ul>
 * <li><strong>Canceled:</strong> the web socket connection failed. Messages that were
 * successfully enqueued by either peer may not have been transmitted to the other.
 * </ul>
 * <p>
 * Note that the state progression is independent for each peer. Arriving at a gracefully-closed
 * state indicates that a peer has sent all of its outgoing messages and received all of its
 * incoming messages. But it does not guarantee that the other peer will successfully receive all of
 * its incoming messages.
 *
 * @author Kimi Liu
 * @version 3.0.5
 * @since JDK 1.8
 */
public interface WebSocket {

    Request request();

    long queueSize();

    boolean send(String text);

    boolean send(ByteString bytes);

    boolean close(int code, String reason);

    void cancel();

    interface Factory {

        WebSocket newWebSocket(Request request, WebSocketListener listener);

    }

}
