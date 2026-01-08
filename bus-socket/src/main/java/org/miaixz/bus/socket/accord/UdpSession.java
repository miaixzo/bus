/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org sandao and other contributors.             ~
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
package org.miaixz.bus.socket.accord;

import org.miaixz.bus.socket.Session;
import org.miaixz.bus.socket.Status;
import org.miaixz.bus.socket.buffer.BufferPage;
import org.miaixz.bus.socket.buffer.WriteBuffer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;

/**
 * Represents a UDP session, which is a logical connection over a connectionless protocol.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class UdpSession extends Session {

    /**
     * The underlying UDP channel associated with this session.
     */
    private final UdpChannel udpChannel;

    /**
     * The remote address of the peer.
     */
    private final SocketAddress remote;

    /**
     * The buffer for writing outgoing data.
     */
    private final WriteBuffer byteBuf;

    /**
     * Constructs a new UdpSession.
     *
     * @param udpChannel      the underlying UDP channel
     * @param remote          the remote address of the peer
     * @param writeBufferPage the buffer page for writing
     */
    public UdpSession(final UdpChannel udpChannel, final SocketAddress remote, BufferPage writeBufferPage) {
        this.udpChannel = udpChannel;
        this.remote = remote;
        this.byteBuf = new WriteBuffer(writeBufferPage, buffer -> udpChannel.write(buffer, this),
                udpChannel.context.getWriteBufferSize(), 1);
        udpChannel.context.getProcessor().stateEvent(this, Status.NEW_SESSION, null);
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public WriteBuffer writeBuffer() {
        return byteBuf;
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public ByteBuffer readBuffer() {
        throw new UnsupportedOperationException("Read buffer is not supported in a UDP session");
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public void awaitRead() {
        throw new UnsupportedOperationException("awaitRead is not supported in a UDP session");
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public void signalRead() {
        throw new UnsupportedOperationException("signalRead is not supported in a UDP session");
    }

    /**
     * Description inherited from parent class or interface.
     * <p>
     * To ensure messages are sent as much as possible, UDP does not support immediate close. This method will flush any
     * pending messages.
     * </p>
     *
     * @param immediate if {@code true}, closes immediately; if {@code false}, closes after sending pending messages.
     */
    @Override
    public void close(boolean immediate) {
        byteBuf.flush();
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public InetSocketAddress getLocalAddress() throws IOException {
        return (InetSocketAddress) udpChannel.getChannel().getLocalAddress();
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public InetSocketAddress getRemoteAddress() {
        return (InetSocketAddress) remote;
    }

}
