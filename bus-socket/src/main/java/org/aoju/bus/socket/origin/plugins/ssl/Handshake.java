/*
 * The MIT License
 *
 * Copyright (c) 2017 aoju.org All rights reserved.
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
package org.aoju.bus.socket.origin.plugins.ssl;

import javax.net.ssl.SSLEngine;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;

/**
 * @author Kimi Liu
 * @version 5.0.1
 * @since JDK 1.8+
 */
public class Handshake {

    private AsynchronousSocketChannel socketChannel;
    private SSLEngine sslEngine;
    private ByteBuffer appWriteBuffer;
    private ByteBuffer netWriteBuffer;
    private ByteBuffer appReadBuffer;

    private ByteBuffer netReadBuffer;
    private Callback handshakeCallback;
    private boolean eof;
    private boolean finished;

    public AsynchronousSocketChannel getSocketChannel() {
        return socketChannel;
    }

    public void setSocketChannel(AsynchronousSocketChannel socketChannel) {
        this.socketChannel = socketChannel;
    }

    public ByteBuffer getAppWriteBuffer() {
        return appWriteBuffer;
    }

    public void setAppWriteBuffer(ByteBuffer appWriteBuffer) {
        this.appWriteBuffer = appWriteBuffer;
    }

    public ByteBuffer getNetWriteBuffer() {
        return netWriteBuffer;
    }

    public void setNetWriteBuffer(ByteBuffer netWriteBuffer) {
        this.netWriteBuffer = netWriteBuffer;
    }

    public ByteBuffer getAppReadBuffer() {
        return appReadBuffer;
    }

    public void setAppReadBuffer(ByteBuffer appReadBuffer) {
        this.appReadBuffer = appReadBuffer;
    }

    public ByteBuffer getNetReadBuffer() {
        return netReadBuffer;
    }

    public void setNetReadBuffer(ByteBuffer netReadBuffer) {
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

    public Callback getHandshakeCallback() {
        return handshakeCallback;
    }

    public void setHandshakeCallback(Callback handshakeCallback) {
        this.handshakeCallback = handshakeCallback;
    }

    public boolean isEof() {
        return eof;
    }

    public void setEof(boolean eof) {
        this.eof = eof;
    }
}
