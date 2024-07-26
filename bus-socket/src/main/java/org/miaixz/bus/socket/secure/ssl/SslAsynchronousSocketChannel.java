/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.socket.secure.ssl;

import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.socket.buffer.BufferPage;
import org.miaixz.bus.socket.buffer.VirtualBuffer;
import org.miaixz.bus.socket.metric.channels.AsynchronousChannelProvider;
import org.miaixz.bus.socket.metric.channels.AsynchronousSocketChannelProxy;
import org.miaixz.bus.socket.metric.handler.FutureCompletionHandler;

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
 * 握手相关实现
 *
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
    private boolean closed = false;

    public SslAsynchronousSocketChannel(AsynchronousSocketChannel asynchronousSocketChannel, SslService sslService,
            BufferPage bufferPage) {
        super(asynchronousSocketChannel);
        this.handshakeModel = sslService.createSSLEngine(asynchronousSocketChannel, bufferPage);
        this.sslService = sslService;
        this.sslEngine = handshakeModel.getSslEngine();
        this.netWriteBuffer = handshakeModel.getNetWriteBuffer();
        this.netReadBuffer = handshakeModel.getNetReadBuffer();
        this.appReadBuffer = handshakeModel.getAppReadBuffer();
    }

    @Override
    public <A> void read(ByteBuffer dst, long timeout, TimeUnit unit, A attachment,
            CompletionHandler<Integer, ? super A> handler) {
        // 处于握手阶段
        if (handshake) {
            doHandshake(dst, timeout, unit, attachment, handler);
            return;
        }

        ByteBuffer appBuffer = appReadBuffer.buffer();
        // appBuffer还有残留数据，先腾空
        if (appBuffer.hasRemaining()) {
            handleAppBuffer(dst, attachment, handler, appBuffer);
            return;
        }
        // netBuffer还有残留，尝试解码
        ByteBuffer netBuffer = netReadBuffer.buffer();
        if (netBuffer.hasRemaining()) {
            appBuffer.compact();
            doUnWrap(netBuffer, appBuffer);
            appBuffer.flip();
        }
        // appBuffer还有残留数据，先腾空
        if (appBuffer.hasRemaining()) {
            handleAppBuffer(dst, attachment, handler, appBuffer);
            return;
        }

        netBuffer.compact();
        asynchronousSocketChannel.read(netBuffer, timeout, unit, attachment, new CompletionHandler<>() {
            int index = 0;

            @Override
            public void completed(Integer result, A attachment) {
                if (result == AsynchronousChannelProvider.READ_MONITOR_SIGNAL) {
                    return;
                } else if (result == AsynchronousChannelProvider.READABLE_SIGNAL) {
                    asynchronousSocketChannel.read(netBuffer, timeout, unit, attachment, this);
                    return;
                } else if (result == -1) {
                    handler.completed(result, attachment);
                    return;
                }

                // 密文解包
                ByteBuffer appBuffer = appReadBuffer.buffer();
                if (appBuffer.hasRemaining()) {
                    failed(new IOException("解包算法异常..."), attachment);
                    return;
                }
                appBuffer.clear();
                ByteBuffer netBuffer = netReadBuffer.buffer();
                netBuffer.flip();
                SSLEngineResult.Status status = doUnWrap(netBuffer, appBuffer);
                appBuffer.flip();

                // 存在doUnWrap为ok，但appBuffer无数据的情况
                if (appBuffer.hasRemaining()) {
                    if (status != SSLEngineResult.Status.OK) {
                        throw new IllegalStateException();
                    }
                    index = 0;
                    handleAppBuffer(dst, attachment, handler, appBuffer);
                    return;
                }
                if (index >= 16) {
                    Logger.error("maybe trigger bug here...");
                }
                if (status == SSLEngineResult.Status.OK && index < 16 && netBuffer.hasRemaining()) {
                    Logger.error("Possible exception on appBuffer.");
                    index++;
                    completed(result, attachment);
                } else {
                    netBuffer.compact();
                    asynchronousSocketChannel.read(netBuffer, timeout, unit, attachment, this);
                }
            }

            @Override
            public void failed(Throwable exc, A attachment) {
                handler.failed(exc, attachment);
            }
        });

    }

    private <A> void handleAppBuffer(ByteBuffer dst, A attachment, CompletionHandler<Integer, ? super A> handler,
            ByteBuffer appBuffer) {
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
    }

    private <A> void doHandshake(ByteBuffer dst, long timeout, TimeUnit unit, A attachment,
            CompletionHandler<Integer, ? super A> handler) {
        handshakeModel.setHandshakeCallback(() -> {
            handshake = false;
            synchronized (SslAsynchronousSocketChannel.this) {
                // 释放内存
                handshakeModel.getAppWriteBuffer().clean();
                netReadBuffer.buffer().flip();
                netWriteBuffer.buffer().clear();
                appReadBuffer.buffer().clear().flip();
                SslAsynchronousSocketChannel.this.notifyAll();
            }
            if (handshakeModel.getException() != null) {
                handler.failed(handshakeModel.getException(), attachment);
            } else {
                SslAsynchronousSocketChannel.this.read(dst, timeout, unit, attachment, handler);
            }
            handshakeModel = null;
        });
        // 触发握手
        sslService.doHandshake(handshakeModel);
    }

    private SSLEngineResult.Status doUnWrap(ByteBuffer netBuffer, ByteBuffer appBuffer) {
        try {
            SSLEngineResult result = sslEngine.unwrap(netBuffer, appBuffer);
            boolean closed = false;
            while (!closed && result.getStatus() != SSLEngineResult.Status.OK) {
                switch (result.getStatus()) {
                case BUFFER_OVERFLOW:
                    if (sslService.isDebug()) {
                        Logger.info("BUFFER_OVERFLOW error,net:" + netBuffer + " app:" + appBuffer);
                    }
                    break;
                case BUFFER_UNDERFLOW:
                    if (netBuffer.limit() == netBuffer.capacity() && !netBuffer.hasRemaining()) {
                        if (sslService.isDebug()) {
                            Logger.error("BUFFER_UNDERFLOW error");
                        }
                    } else {
                        if (sslService.isDebug()) {
                            Logger.error("BUFFER_UNDERFLOW,continue read:" + netBuffer);
                        }
                    }
                    return result.getStatus();
                case CLOSED:
                    if (sslService.isDebug()) {
                        Logger.info("doUnWrap Result:" + result.getStatus());
                    }
                    closed = true;
                    break;
                default:
                    if (sslService.isDebug()) {
                        Logger.info("doUnWrap Result:" + result.getStatus());
                    }
                }
                result = sslEngine.unwrap(netBuffer, appBuffer);
            }
            return result.getStatus();
        } catch (SSLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Future<Integer> read(ByteBuffer dst) {
        FutureCompletionHandler<Integer, Object> readFuture = new FutureCompletionHandler<>();
        read(dst, 0, TimeUnit.MILLISECONDS, null, readFuture);
        return readFuture;
    }

    @Override
    public <A> void read(ByteBuffer[] dsts, int offset, int length, long timeout, TimeUnit unit, A attachment,
            CompletionHandler<Long, ? super A> handler) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <A> void write(ByteBuffer src, long timeout, TimeUnit unit, A attachment,
            CompletionHandler<Integer, ? super A> handler) {
        if (handshake) {
            checkInitialized();
        }
        int pos = src.position();
        try {
            doWrap(src);
        } catch (SSLException e) {
            handler.failed(e, attachment);
            return;
        }
        if (src.position() - pos == 0) {
            Logger.error("write error:" + src + " netWrite:" + netWriteBuffer.buffer());
        }
        asynchronousSocketChannel.write(netWriteBuffer.buffer(), timeout, unit, attachment, new CompletionHandler<>() {
            @Override
            public void completed(Integer result, A attachment) {
                if (result == -1) {
                    Logger.error("aaaaaaaaaaa");
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

    private void doWrap(ByteBuffer writeBuffer) throws SSLException {
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
                break;
            case BUFFER_UNDERFLOW:
                if (sslService.isDebug()) {
                    Logger.error("doWrap BUFFER_UNDERFLOW");
                }
                break;
            case CLOSED:
                throw new SSLException("SSLEngine has " + result.getStatus());
            default:
                if (sslService.isDebug()) {
                    Logger.error("doWrap Result:" + result.getStatus());
                }
            }
            result = sslEngine.wrap(writeBuffer, netBuffer);
        }
        writeBuffer.limit(limit);
        netBuffer.flip();

    }

    @Override
    public Future<Integer> write(ByteBuffer src) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <A> void write(ByteBuffer[] srcs, int offset, int length, long timeout, TimeUnit unit, A attachment,
            CompletionHandler<Long, ? super A> handler) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void close() throws IOException {
        if (closed) {
            return;
        }
        netWriteBuffer.clean();
        netReadBuffer.clean();
        appReadBuffer.clean();
        try {
            sslEngine.closeInbound();
        } catch (SSLException ignore) {
        }
        sslEngine.closeOutbound();
        asynchronousSocketChannel.close();
    }

}
