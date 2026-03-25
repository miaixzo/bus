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
package org.miaixz.bus.socket;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;

import org.miaixz.bus.socket.buffer.WriteBuffer;

/**
 * Abstract base class representing a network session.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public abstract class Session {

    /**
     * Session status: closed.
     */
    protected static final byte SESSION_STATUS_CLOSED = 1;
    /**
     * Session status: in the process of closing.
     */
    protected static final byte SESSION_STATUS_CLOSING = 2;
    /**
     * Session status: active and enabled.
     */
    protected static final byte SESSION_STATUS_ENABLED = 3;

    /**
     * Current status of the session.
     *
     * @see Session#SESSION_STATUS_CLOSED
     * @see Session#SESSION_STATUS_CLOSING
     * @see Session#SESSION_STATUS_ENABLED
     */
    protected byte status = SESSION_STATUS_ENABLED;
    /**
     * Whether the read channel has reached the end-of-stream.
     */
    protected boolean eof;
    protected int modCount = 0;
    /**
     * An optional attachment object.
     */
    private Object attachment;

    /**
     * Gets the `WriteBuffer` for data output.
     *
     * @return The `WriteBuffer`.
     */
    public abstract WriteBuffer writeBuffer();

    /**
     * Gets the read buffer object.
     *
     * @return The `ByteBuffer` for reading.
     */
    public abstract ByteBuffer readBuffer();

    /**
     * Forcibly closes the current session. Any pending data in the write buffer may be lost.
     */
    public final void close() {
        close(true);
    }

    /**
     * Pauses the read operation until `signalRead` is called.
     */
    public abstract void awaitRead();

    /**
     * Resumes the read operation. This method should only be used in asynchronous processing mode.
     */
    public abstract void signalRead();

    /**
     * Closes the session.
     *
     * @param immediate If `true`, closes immediately; if `false`, closes after flushing the write buffer.
     */
    public abstract void close(boolean immediate);

    /**
     * Gets the unique identifier for this session.
     *
     * @return The session ID.
     */
    public String getSessionID() {
        return "Session-" + hashCode();
    }

    /**
     * Checks if the current session is invalid (closing or closed).
     *
     * @return `true` if the session is invalid.
     */
    public boolean isInvalid() {
        return status != SESSION_STATUS_ENABLED;
    }

    /**
     * Gets the attachment object.
     *
     * @param <A> The type of the attachment.
     * @return The attachment.
     */
    public final <A> A getAttachment() {
        return (A) attachment;
    }

    /**
     * Attaches an object to this session.
     *
     * @param <A>        The type of the attachment.
     * @param attachment The object to attach.
     */
    public final <A> void setAttachment(A attachment) {
        this.attachment = attachment;
    }

    /**
     * Gets the local address of this session.
     *
     * @return The local address.
     * @throws IOException if an I/O error occurs.
     * @see AsynchronousSocketChannel#getLocalAddress()
     */
    public abstract InetSocketAddress getLocalAddress() throws IOException;

    /**
     * Gets the remote address of this session.
     *
     * @return The remote address.
     * @throws IOException if an I/O error occurs.
     * @see AsynchronousSocketChannel#getRemoteAddress()
     */
    public abstract InetSocketAddress getRemoteAddress() throws IOException;

    /**
     * Gets an `InputStream` for this session.
     *
     * @return An `InputStream`.
     * @throws IOException if an I/O error occurs.
     */
    public InputStream getInputStream() throws IOException {
        throw new UnsupportedOperationException();
    }

    /**
     * Gets an `InputStream` of a known length.
     *
     * @param length The length of the `InputStream`.
     * @return An `InputStream`.
     * @throws IOException if an I/O error occurs.
     */
    public InputStream getInputStream(int length) throws IOException {
        throw new UnsupportedOperationException();
    }

}
