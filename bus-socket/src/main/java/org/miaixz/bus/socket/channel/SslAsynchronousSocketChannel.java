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
package org.miaixz.bus.socket.channel;

import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.socket.buffers.BufferPage;
import org.miaixz.bus.socket.buffers.VirtualBuffer;
import org.miaixz.bus.socket.security.HandshakeModel;
import org.miaixz.bus.socket.security.SslService;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class SslAsynchronousSocketChannel extends AsynchronousSocketChannelProxy {

    private final VirtualBuffer netWriteBuffer;
    private final VirtualBuffer netReadBuffer;
    private final VirtualBuffer appReadBuffer;
    /**
     * 完成握手置null
     */
    private final SslService sslService;
    private SSLEngine sslEngine;
    /**
     * 完成握手置null
     */
    private HandshakeModel handshakeModel;
    private boolean handshake = true;
    /**
     * 自适应的输出长度
     */
    private int adaptiveWriteSize = -1;

    public SslAsynchronousSocketChannel(AsynchronousSocketChannel asynchronousSocketChannel, SslService sslService, BufferPage bufferPage) {
        super(asynchronousSocketChannel);
        this.handshakeModel = sslService.createSSLEngine(asynchronousSocketChannel, bufferPage);
        this.sslService = sslService;
        this.sslEngine = handshakeModel.getSslEngine();
        this.netWriteBuffer = handshakeModel.getNetWriteBuffer();
        this.netReadBuffer = handshakeModel.getNetReadBuffer();
        this.appReadBuffer = handshakeModel.getAppReadBuffer();
    }

    @Override
    public <A> void read(ByteBuffer dst, long timeout, TimeUnit unit, A attachment, CompletionHandler<Integer, ? super A> handler) {
        if (handshake) {
            handshakeModel.setHandshakeCallback(() -> {
                handshake = false;
                synchronized (SslAsynchronousSocketChannel.this) {
                    //释放内存
                    handshakeModel.getAppWriteBuffer().clean();
                    netReadBuffer.buffer().clear();
                    netWriteBuffer.buffer().clear();
                    appReadBuffer.buffer().clear().flip();
                    SslAsynchronousSocketChannel.this.notifyAll();
                }
                if (handshakeModel.isEof()) {
                    handler.completed(-1, attachment);
                } else {
                    SslAsynchronousSocketChannel.this.read(dst, timeout, unit, attachment, handler);
                }
                handshakeModel = null;
            });
            //触发握手
            sslService.doHandshake(handshakeModel);
            return;
        }
        ByteBuffer appBuffer = appReadBuffer.buffer();
        if (appBuffer.hasRemaining()) {
            int pos = dst.position();
            if (appBuffer.remaining() > dst.remaining()) {
                int limit = appBuffer.limit();
                appBuffer.limit(appBuffer.position() + dst.remaining());
                dst.put(appBuffer);
                appBuffer.limit(limit);
            } else {
                dst.put(appBuffer);
            }
            handler.completed(dst.position() - pos, attachment);
            return;
        }

        asynchronousSocketChannel.read(netReadBuffer.buffer(), timeout, unit, attachment, new CompletionHandler<Integer, A>() {
            @Override
            public void completed(Integer result, A attachment) {
                int pos = dst.position();
                ByteBuffer appBuffer = appReadBuffer.buffer();
                appBuffer.clear();
                doUnWrap();
                appBuffer.flip();
                if (appBuffer.remaining() > dst.remaining()) {
                    int limit = appBuffer.limit();
                    appBuffer.limit(appBuffer.position() + dst.remaining());
                    dst.put(appBuffer);
                    appBuffer.limit(limit);
                } else if (appBuffer.hasRemaining()) {
                    dst.put(appBuffer);
                } else if (result > 0) {
                    appBuffer.compact();
                    asynchronousSocketChannel.read(netReadBuffer.buffer(), timeout, unit, attachment, this);
                    return;
                }

                handler.completed(result != -1 ? dst.position() - pos : result, attachment);
            }

            @Override
            public void failed(Throwable exc, A attachment) {
                handler.failed(exc, attachment);
            }
        });
    }

    private void doUnWrap() {
        try {
            ByteBuffer netBuffer = netReadBuffer.buffer();
            ByteBuffer appBuffer = appReadBuffer.buffer();
            netBuffer.flip();
            SSLEngineResult result = sslEngine.unwrap(netBuffer, appBuffer);
            boolean closed = false;
            while (!closed && result.getStatus() != SSLEngineResult.Status.OK) {
                switch (result.getStatus()) {
                    case BUFFER_OVERFLOW:
                        Logger.warn("BUFFER_OVERFLOW error");
                        break;
                    case BUFFER_UNDERFLOW:
                        if (netBuffer.limit() == netBuffer.capacity()) {
                            Logger.warn("BUFFER_UNDERFLOW error");
                        } else {
                            if (Logger.isDebug()) {
                                Logger.debug("BUFFER_UNDERFLOW,continue read:" + netBuffer);
                            }
                            if (netBuffer.position() > 0) {
                                netBuffer.compact();
                            } else {
                                netBuffer.position(netBuffer.limit());
                                netBuffer.limit(netBuffer.capacity());
                            }
                        }
                        return;
                    case CLOSED:
                        Logger.warn("doUnWrap Result:" + result.getStatus());
                        closed = true;
                        break;
                    default:
                        Logger.warn("doUnWrap Result:" + result.getStatus());
                }
                result = sslEngine.unwrap(netBuffer, appBuffer);
            }
            netBuffer.compact();
        } catch (SSLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Future<Integer> read(ByteBuffer dst) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <A> void read(ByteBuffer[] dsts, int offset, int length, long timeout, TimeUnit unit, A attachment, CompletionHandler<Long, ? super A> handler) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <A> void write(ByteBuffer src, long timeout, TimeUnit unit, A attachment, CompletionHandler<Integer, ? super A> handler) {
        if (handshake) {
            checkInitialized();
        }
        int pos = src.position();
        doWrap(src);
        asynchronousSocketChannel.write(netWriteBuffer.buffer(), timeout, unit, attachment, new CompletionHandler<Integer, A>() {
            @Override
            public void completed(Integer result, A attachment) {
                if (result == -1) {
                    System.err.println("aaaaaaaaaaa");
                }
                if (netWriteBuffer.buffer().hasRemaining()) {
                    asynchronousSocketChannel.write(netWriteBuffer.buffer(), timeout, unit, attachment, this);
                } else {
                    handler.completed(src.position() - pos, attachment);
                }
            }

            @Override
            public void failed(Throwable exc, A attachment) {
                handler.failed(exc, attachment);
            }
        });
    }

    /**
     * 校验是否已完成初始化,如果还处于Handshake阶段则阻塞当前线程
     */
    private void checkInitialized() {
        if (!handshake) {
            return;
        }
        synchronized (this) {
            if (!handshake) {
                return;
            }
            try {
                this.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void doWrap(ByteBuffer writeBuffer) {
        try {
            ByteBuffer netBuffer = netWriteBuffer.buffer();
            netBuffer.compact();
            int limit = writeBuffer.limit();
            if (adaptiveWriteSize > 0 && writeBuffer.remaining() > adaptiveWriteSize) {
                writeBuffer.limit(writeBuffer.position() + adaptiveWriteSize);
            }
            SSLEngineResult result = sslEngine.wrap(writeBuffer, netBuffer);
            while (result.getStatus() != SSLEngineResult.Status.OK) {
                switch (result.getStatus()) {
                    case BUFFER_OVERFLOW:
                        netBuffer.clear();
                        writeBuffer.limit(writeBuffer.position() + ((writeBuffer.limit() - writeBuffer.position() >> 1)));
                        adaptiveWriteSize = writeBuffer.remaining();
//                        logger.info("doWrap BUFFER_OVERFLOW maybeSize:{}", maybeWriteSize);
                        break;
                    case BUFFER_UNDERFLOW:
                        Logger.info("doWrap BUFFER_UNDERFLOW");
                        break;
                    default:
                        Logger.warn("doWrap Result:" + result.getStatus());
                }
                result = sslEngine.wrap(writeBuffer, netBuffer);
            }
            writeBuffer.limit(limit);
            netBuffer.flip();
        } catch (SSLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Future<Integer> write(ByteBuffer src) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <A> void write(ByteBuffer[] srcs, int offset, int length, long timeout, TimeUnit unit, A attachment, CompletionHandler<Long, ? super A> handler) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void close() throws IOException {
        netWriteBuffer.clean();
        netReadBuffer.clean();
        appReadBuffer.clean();
        try {
            sslEngine.closeInbound();
        } catch (SSLException e) {
            Logger.warn("ignore closeInbound exception: {}", e.getMessage());
        }
        sslEngine.closeOutbound();
        asynchronousSocketChannel.close();

    }

}
