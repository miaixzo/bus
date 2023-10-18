/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2023 aoju.org sandao and other contributors.               *
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
package org.aoju.bus.socket.security;

import org.aoju.bus.socket.buffers.VirtualBuffer;

import javax.net.ssl.SSLEngine;
import java.nio.channels.AsynchronousSocketChannel;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class HandshakeModel {

    private AsynchronousSocketChannel socketChannel;
    private SSLEngine sslEngine;
    private VirtualBuffer appWriteBuffer;
    private VirtualBuffer netWriteBuffer;
    private VirtualBuffer appReadBuffer;

    private VirtualBuffer netReadBuffer;
    private HandshakeCallback handshakeCallback;
    private boolean eof;
    private boolean finished;

    public AsynchronousSocketChannel getSocketChannel() {
        return socketChannel;
    }

    public void setSocketChannel(AsynchronousSocketChannel socketChannel) {
        this.socketChannel = socketChannel;
    }

    public VirtualBuffer getAppWriteBuffer() {
        return appWriteBuffer;
    }

    public void setAppWriteBuffer(VirtualBuffer appWriteBuffer) {
        this.appWriteBuffer = appWriteBuffer;
    }

    public VirtualBuffer getNetWriteBuffer() {
        return netWriteBuffer;
    }

    public void setNetWriteBuffer(VirtualBuffer netWriteBuffer) {
        this.netWriteBuffer = netWriteBuffer;
    }

    public VirtualBuffer getAppReadBuffer() {
        return appReadBuffer;
    }

    public void setAppReadBuffer(VirtualBuffer appReadBuffer) {
        this.appReadBuffer = appReadBuffer;
    }

    public VirtualBuffer getNetReadBuffer() {
        return netReadBuffer;
    }

    public void setNetReadBuffer(VirtualBuffer netReadBuffer) {
        this.netReadBuffer = netReadBuffer;
    }

    public SSLEngine getSslEngine() {
        return sslEngine;
    }

    public void setSslEngine(SSLEngine sslEngine) {
        this.sslEngine = sslEngine;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    public HandshakeCallback getHandshakeCallback() {
        return handshakeCallback;
    }

    public void setHandshakeCallback(HandshakeCallback handshakeCallback) {
        this.handshakeCallback = handshakeCallback;
    }

    public boolean isEof() {
        return eof;
    }

    public void setEof(boolean eof) {
        this.eof = eof;
    }

}
