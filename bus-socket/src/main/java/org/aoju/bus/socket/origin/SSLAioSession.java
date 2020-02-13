/*
 * The MIT License
 *
 * Copyright (c) 2015-2020 aoju.org All rights reserved.
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
package org.aoju.bus.socket.origin;

import org.aoju.bus.core.io.PageBuffer;
import org.aoju.bus.core.io.VirtualBuffer;
import org.aoju.bus.logger.Logger;
import org.aoju.bus.socket.origin.plugins.ssl.Handshake;
import org.aoju.bus.socket.origin.plugins.ssl.SSLService;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;

/**
 * @author Kimi Liu
 * @version 5.5.8
 * @since JDK 1.8+
 */
class SSLAioSession<T> extends TcpAioSession<T> {

    private ByteBuffer netWriteBuffer;

    private ByteBuffer netReadBuffer;
    private SSLEngine sslEngine = null;

    /**
     * 完成握手置null
     */
    private Handshake handshakeModel;
    /**
     * 完成握手置null
     */
    private SSLService sslService;

    SSLAioSession(AsynchronousSocketChannel channel, ServerConfig<T> config, TcpReadHandler<T> aioReadCompletionHandler, TcpWriteHandler<T> aioWriteCompletionHandler, SSLService sslService, PageBuffer pageBuffer) {
        super(channel, config, aioReadCompletionHandler, aioWriteCompletionHandler, pageBuffer);
        this.handshakeModel = sslService.createSSLEngine(channel);
        this.sslService = sslService;
    }

    @Override
    void writeToChannel() {
        checkInitialized();
        if (netWriteBuffer != null && netWriteBuffer.hasRemaining()) {
            writeToChannel0(netWriteBuffer);
            return;
        }
        super.writeToChannel();
    }


    @Override
    void initSession() {
        this.sslEngine = handshakeModel.getSslEngine();
        this.netWriteBuffer = ByteBuffer.allocate(sslEngine.getSession().getPacketBufferSize());
        this.netWriteBuffer.flip();
        this.netReadBuffer = ByteBuffer.allocate(readBuffer.buffer().capacity());
        this.handshakeModel.setHandshakeCallback(() -> {
            synchronized (SSLAioSession.this) {
                handshakeModel = null;//释放内存
                SSLAioSession.this.notifyAll();
            }
            sslService = null;//释放内存
            continueRead();
        });
        sslService.doHandshake(handshakeModel);
    }

    /**
     * 校验是否已完成初始化,如果还处于Handshake阶段则阻塞当前线程
     */
    private void checkInitialized() {
        if (handshakeModel == null) {
            return;
        }
        synchronized (this) {
            if (handshakeModel == null) {
                return;
            }
            try {
                this.wait();
            } catch (InterruptedException e) {
                Logger.debug(e.getMessage(), e);
            }
        }
    }

    @Override
    void readFromChannel(boolean eof) {
        checkInitialized();
        doUnWrap();
        super.readFromChannel(eof);
    }

    @Override
    protected void continueRead() {
        readFromChannel0(netReadBuffer);
    }

    @Override
    protected void continueWrite(VirtualBuffer writeBuffer) {
        doWrap(writeBuffer);
        writeToChannel0(netWriteBuffer);
    }

    private void doWrap(VirtualBuffer writeBuffer) {
        try {
            netWriteBuffer.compact();
            SSLEngineResult result = sslEngine.wrap(writeBuffer.buffer(), netWriteBuffer);
            while (result.getStatus() != SSLEngineResult.Status.OK) {
                switch (result.getStatus()) {
                    case BUFFER_OVERFLOW:
                        Logger.info("doWrap BUFFER_OVERFLOW");
                        break;
                    case BUFFER_UNDERFLOW:
                        Logger.info("doWrap BUFFER_UNDERFLOW");
                        break;
                    default:
                        Logger.error("doWrap Result:" + result.getStatus());
                }
                result = sslEngine.wrap(writeBuffer.buffer(), netWriteBuffer);
            }
            netWriteBuffer.flip();
        } catch (SSLException e) {
            throw new RuntimeException(e);
        }
    }

    private void doUnWrap() {
        try {
            netReadBuffer.flip();
            ByteBuffer readBuffer = super.readBuffer.buffer();
            SSLEngineResult result = sslEngine.unwrap(netReadBuffer, readBuffer);
            boolean closed = false;
            while (!closed && result.getStatus() != SSLEngineResult.Status.OK) {
                switch (result.getStatus()) {
                    case BUFFER_OVERFLOW:
                        // Could attempt to drain the dst buffer of any already obtained
                        // data, but we'll just increase it to the size needed.
                        int appSize = readBuffer.capacity() * 2 < sslEngine.getSession().getApplicationBufferSize() ? readBuffer.capacity() * 2 : sslEngine.getSession().getApplicationBufferSize();
                        Logger.info("doUnWrap BUFFER_OVERFLOW:" + appSize);
                        ByteBuffer b = ByteBuffer.allocate(appSize + readBuffer.position());
                        readBuffer.flip();
                        b.put(readBuffer);
                        readBuffer = b;
                        // retry the operation.
                        break;
                    case BUFFER_UNDERFLOW:

                        if (netReadBuffer.limit() == netReadBuffer.capacity()) {
                            int netSize = netReadBuffer.capacity() * 2 < sslEngine.getSession().getPacketBufferSize() ? netReadBuffer.capacity() * 2 : sslEngine.getSession().getPacketBufferSize();
                            Logger.debug("BUFFER_UNDERFLOW:" + netSize);
                            ByteBuffer b1 = ByteBuffer.allocate(netSize);
                            b1.put(netReadBuffer);
                            netReadBuffer = b1;
                        } else {
                            if (netReadBuffer.position() > 0) {
                                netReadBuffer.compact();
                            } else {
                                netReadBuffer.position(netReadBuffer.limit());
                                netReadBuffer.limit(netReadBuffer.capacity());
                            }
                            Logger.debug("BUFFER_UNDERFLOW,continue read:" + netReadBuffer);
                        }

                        return;
                    case CLOSED:
                        Logger.debug("doUnWrap Result:" + result.getStatus());
                        closed = true;
                        break;
                    default:
                        Logger.error("doUnWrap Result:" + result.getStatus());
                        // other cases: CLOSED, OK.
                }
                result = sslEngine.unwrap(netReadBuffer, readBuffer);
            }
            netReadBuffer.compact();
        } catch (SSLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close(boolean immediate) {
        super.close(immediate);
        if (status == SESSION_STATUS_CLOSED) {
            sslEngine.closeOutbound();
        }
    }

}
