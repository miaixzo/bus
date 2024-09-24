/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org sandao and other contributors.             ~
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
 * @author Kimi Liu
 * @since Java 17+
 */
public class UdpSession extends Session {

    private final UdpChannel udpChannel;

    private final SocketAddress remote;

    private final WriteBuffer byteBuf;

    public UdpSession(final UdpChannel udpChannel, final SocketAddress remote, BufferPage writeBufferPage) {
        this.udpChannel = udpChannel;
        this.remote = remote;
        this.byteBuf = new WriteBuffer(writeBufferPage, buffer -> udpChannel.write(buffer, this),
                udpChannel.context.getWriteBufferSize(), 1);
        udpChannel.context.getProcessor().stateEvent(this, Status.NEW_SESSION, null);
    }

    @Override
    public WriteBuffer writeBuffer() {
        return byteBuf;
    }

    @Override
    public ByteBuffer readBuffer() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void awaitRead() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void signalRead() {
        throw new UnsupportedOperationException();
    }

    /**
     * 为确保消息尽可能发送，UDP不支持立即close
     *
     * @param immediate true:立即关闭,false:响应消息发送完后关闭
     */
    @Override
    public void close(boolean immediate) {
        byteBuf.flush();
    }

    @Override
    public InetSocketAddress getLocalAddress() throws IOException {
        return (InetSocketAddress) udpChannel.getChannel().getLocalAddress();
    }

    @Override
    public InetSocketAddress getRemoteAddress() {
        return (InetSocketAddress) remote;
    }

}
