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
package org.miaixz.bus.socket.secure.ssl;

import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.socket.buffer.BufferPage;

import javax.net.ssl.*;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.function.Consumer;

/**
 * TLS/SSL服务
 * keytool -genkey -validity 36000 -alias www.miaixz.org -keyalg RSA -keystore server.keystore
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public final class SslService {

    private final SSLContext sslContext;
    private final Consumer<SSLEngine> consumer;
    private boolean debug;

    public SslService(SSLContext sslContext, Consumer<SSLEngine> consumer) {
        this.sslContext = sslContext;
        this.consumer = consumer;
    }

    HandshakeModel createSSLEngine(AsynchronousSocketChannel socketChannel, BufferPage bufferPage) {
        try {
            HandshakeModel handshakeModel = new HandshakeModel();
            SSLEngine sslEngine = sslContext.createSSLEngine();
            SSLSession session = sslEngine.getSession();

            //更新SSLEngine配置
            consumer.accept(sslEngine);

            handshakeModel.setSslEngine(sslEngine);
            handshakeModel.setAppWriteBuffer(bufferPage.allocate(session.getApplicationBufferSize()));
            handshakeModel.setNetWriteBuffer(bufferPage.allocate(session.getPacketBufferSize()));
            handshakeModel.getNetWriteBuffer().buffer().flip();
            handshakeModel.setAppReadBuffer(bufferPage.allocate(session.getApplicationBufferSize()));
            handshakeModel.setNetReadBuffer(bufferPage.allocate(session.getPacketBufferSize()));
            sslEngine.beginHandshake();

            handshakeModel.setSocketChannel(socketChannel);
            return handshakeModel;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * 纯异步实现的SSL握手,
     * 在执行doHandshake期间必须保证当前通道无数据读写正在执行。
     * 若触发了数据读写，也应立马终止doHandshake方法
     *
     * @param handshakeModel
     */
    public void doHandshake(HandshakeModel handshakeModel) {
        try {
            ByteBuffer netReadBuffer = handshakeModel.getNetReadBuffer().buffer();
            ByteBuffer appReadBuffer = handshakeModel.getAppReadBuffer().buffer();
            ByteBuffer netWriteBuffer = handshakeModel.getNetWriteBuffer().buffer();
            ByteBuffer appWriteBuffer = handshakeModel.getAppWriteBuffer().buffer();
            SSLEngine engine = handshakeModel.getSslEngine();

            // 握手阶段网络断链
            if (handshakeModel.getException() != null) {
                if (debug) {
                    Logger.info("the ssl handshake is terminated");
                }
                handshakeModel.getHandshakeCallback().callback();
                return;
            }
            SSLEngineResult result;
            SSLEngineResult.HandshakeStatus handshakeStatus;
            while (!handshakeModel.isFinished()) {
                handshakeStatus = engine.getHandshakeStatus();
                if (debug) {
                    Logger.info("握手状态:" + handshakeStatus);
                }
                switch (handshakeStatus) {
                    case NEED_UNWRAP:
                        // 解码
                        netReadBuffer.flip();
                        if (netReadBuffer.hasRemaining()) {
                            result = engine.unwrap(netReadBuffer, appReadBuffer);
                            netReadBuffer.compact();
                        } else {
                            netReadBuffer.clear();
                            handshakeModel.getSocketChannel().read(netReadBuffer, handshakeModel, handshakeCompletionHandler);
                            return;
                        }

                        if (result.getHandshakeStatus() == SSLEngineResult.HandshakeStatus.FINISHED) {
                            handshakeModel.setFinished(true);
                        }
                        switch (result.getStatus()) {
                            case OK:
                                break;
                            case BUFFER_OVERFLOW:
                                Logger.warn("doHandshake BUFFER_OVERFLOW");
                                break;
                            // 两种情况会触发BUFFER_UNDERFLOW,1:读到的数据不够,2:netReadBuffer空间太小
                            case BUFFER_UNDERFLOW:
                                if (netReadBuffer.position() > 0) {
                                    handshakeModel.getSocketChannel().read(netReadBuffer, handshakeModel, handshakeCompletionHandler);
                                } else {
                                    Logger.warn("doHandshake BUFFER_UNDERFLOW");
                                }
                                return;
                            default:
                                throw new IllegalStateException("Invalid SSL status: " + result.getStatus());
                        }
                        break;
                    case NEED_WRAP:
                        if (netWriteBuffer.hasRemaining()) {
                            if (debug) {
                                Logger.info("数据未输出完毕...");
                            }
                            handshakeModel.getSocketChannel().write(netWriteBuffer, handshakeModel, handshakeCompletionHandler);
                            return;
                        }
                        netWriteBuffer.clear();
                        result = engine.wrap(appWriteBuffer, netWriteBuffer);
                        switch (result.getStatus()) {
                            case OK:
                                appWriteBuffer.clear();
                                netWriteBuffer.flip();
                                if (result.getHandshakeStatus() == SSLEngineResult.HandshakeStatus.FINISHED) {
                                    handshakeModel.setFinished(true);
                                }
                                handshakeModel.getSocketChannel().write(netWriteBuffer, handshakeModel, handshakeCompletionHandler);
                                return;
                            case BUFFER_OVERFLOW:
                                if (debug) {
                                    Logger.warn("NEED_WRAP BUFFER_OVERFLOW");
                                }
                                break;
                            case BUFFER_UNDERFLOW:
                                throw new SSLException("Buffer underflow occurred after a wrap. I don't think we should ever get here.");
                            case CLOSED:
                                if (debug) {
                                    Logger.warn("closed");
                                }
                                try {
                                    netWriteBuffer.flip();
                                    netReadBuffer.clear();
                                } catch (Exception e) {
                                    if (debug) {
                                        Logger.error("Failed to send server's CLOSE message due to socket channel's failure.");
                                    }
                                }
                                break;
                            default:
                                throw new IllegalStateException("Invalid SSL status: " + result.getStatus());
                        }
                        break;
                    case NEED_TASK:
                        Runnable task;
                        while ((task = engine.getDelegatedTask()) != null) {
                            task.run();
                        }
                        break;
                    case FINISHED:
                        if (debug) {
                            Logger.info("HandshakeFinished");
                        }
                        break;
                    case NOT_HANDSHAKING:
                        if (debug) {
                            Logger.error("NOT_HANDSHAKING");
                        }
                        break;
                    default:
                        throw new IllegalStateException("Invalid SSL status: " + handshakeStatus);
                }
            }
            handshakeModel.getHandshakeCallback().callback();
        } catch (Exception e) {
            if (debug) {
                Logger.error("ignore doHandshake exception:" + e.getMessage());
            }
            handshakeCompletionHandler.failed(e, handshakeModel);
        }
    }

    public void debug(boolean debug) {
        this.debug = debug;
    }

    boolean isDebug() {
        return debug;
    }

    private final CompletionHandler<Integer, HandshakeModel> handshakeCompletionHandler = new CompletionHandler<>() {
        @Override
        public void completed(Integer result, HandshakeModel attachment) {
            if (result == -1) {
                failed(new IOException("eof"), attachment);
                return;
            }
            synchronized (attachment) {
                doHandshake(attachment);
            }
        }

        @Override
        public void failed(Throwable exc, HandshakeModel attachment) {
            attachment.setException(exc);
            attachment.getHandshakeCallback().callback();
        }
    };

}
