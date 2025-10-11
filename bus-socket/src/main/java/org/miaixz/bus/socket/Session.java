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
 * @since Java 17+
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
