/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2022 aoju.org sandao and other contributors.               *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.aoju.bus.socket;

import org.aoju.bus.core.io.WriteBuffer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;

/**
 * @author Kimi Liu
 * @version 6.3.3
 * @since JDK 1.8+
 */
public class UdpAioSession extends AioSession {

    private final UdpChannel udpChannel;

    private final SocketAddress remote;

    private final WriteBuffer writeBuffer;

    UdpAioSession(final UdpChannel udpChannel, final SocketAddress remote, WriteBuffer writeBuffer) {
        this.udpChannel = udpChannel;
        this.remote = remote;
        this.writeBuffer = writeBuffer;
        udpChannel.config.getProcessor().stateEvent(this, SocketStatus.NEW_SESSION, null);
    }

    @Override
    public WriteBuffer writeBuffer() {
        return writeBuffer;
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

    @Override
    public void close(boolean immediate) {
        writeBuffer.close();
        udpChannel.config.getProcessor().stateEvent(this, SocketStatus.SESSION_CLOSED, null);
        udpChannel.removeSession(remote);
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
