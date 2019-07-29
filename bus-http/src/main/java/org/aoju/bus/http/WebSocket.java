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
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public interface WebSocket {

    /**
     * Returns the original request that initiated this web socket.
     */
    Request request();

    /**
     * Returns the size in bytes of all messages enqueued to be transmitted to the server. This
     * doesn't include framing overhead. It also doesn't include any bytes buffered by the operating
     * system or network intermediaries. This method returns 0 if no messages are waiting
     * in the queue. If may return a nonzero value after the web socket has been canceled; this
     * indicates that enqueued messages were not transmitted.
     */
    long queueSize();

    /**
     * Attempts to enqueue {@code text} to be UTF-8 encoded and sent as a the data of a text (type
     * {@code 0x1}) message.
     *
     * <p>This method returns true if the message was enqueued. Messages that would overflow the
     * outgoing message buffer will be rejected and trigger a {@linkplain #close graceful shutdown} of
     * this web socket. This method returns false in that case, and in any other case where this
     * web socket is closing, closed, or canceled.
     *
     * <p>This method returns immediately.
     */
    boolean send(String text);

    /**
     * Attempts to enqueue {@code bytes} to be sent as a the data of a binary (type {@code 0x2})
     * message.
     *
     * <p>This method returns true if the message was enqueued. Messages that would overflow the
     * outgoing message buffer (16 MiB) will be rejected and trigger a
     * {@linkplain #close graceful shutdown} of this web socket. This method returns false in that
     * case, and in any other case where this web socket is closing, closed, or canceled.
     *
     * <p>This method returns immediately.
     */
    boolean send(ByteString bytes);

    /**
     * Attempts to initiate a graceful shutdown of this web socket. Any already-enqueued messages will
     * be transmitted before the close message is sent but subsequent calls to {@link #send} will
     * return false and their messages will not be enqueued.
     *
     * <p>This returns true if a graceful shutdown was initiated by this call. It returns false and if
     * a graceful shutdown was already underway or if the web socket is already closed or canceled.
     *
     * @param code   Status code as defined by <a
     *               href="http://tools.ietf.org/html/rfc6455#section-7.4">Section 7.4 of RFC 6455</a>.
     * @param reason Reason for shutting down or {@code null}.
     * @throws IllegalArgumentException if code is invalid.
     */
    boolean close(int code, String reason);

    /**
     * Immediately and violently release resources held by this web socket, discarding any enqueued
     * messages. This does nothing if the web socket has already been closed or canceled.
     */
    void cancel();

    interface Factory {
        /**
         * Creates a new web socket and immediately returns it. Creating a web socket initiates an
         * asynchronous process to connect the socket. Once that succeeds or fails, {@code listener}
         * will be notified. The caller must either close or cancel the returned web socket when it is
         * no longer in use.
         */
        WebSocket newWebSocket(Request request, WebSocketListener listener);
    }
}
