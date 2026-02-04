/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org sandao and other contributors.             ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
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
