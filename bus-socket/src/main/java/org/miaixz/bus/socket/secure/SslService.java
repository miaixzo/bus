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
package org.miaixz.bus.socket.secure;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.function.Consumer;

import javax.net.ssl.*;

import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.socket.buffer.BufferPage;
import org.miaixz.bus.socket.metric.channel.AsynchronousChannelProvider;

/**
 * TLS/SSL Service. Example keytool command: keytool -genkey -validity 36000 -alias www.miaixz.org -keyalg RSA -keystore
 * server.keystore
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

            // Update SSLEngine configuration
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
     * Purely asynchronous SSL handshake implementation. During the execution of `doHandshake`, it must be ensured that
     * no data reading or writing is in progress on the current channel. If a read/write is triggered, the `doHandshake`
     * method should be terminated immediately.
     *
     * @param handshakeModel The handshake model.
     */
    public void doHandshake(HandshakeModel handshakeModel) {
        try {
            ByteBuffer netReadBuffer = handshakeModel.getNetReadBuffer().buffer();
            ByteBuffer appReadBuffer = handshakeModel.getAppReadBuffer().buffer();
            ByteBuffer netWriteBuffer = handshakeModel.getNetWriteBuffer().buffer();
            ByteBuffer appWriteBuffer = handshakeModel.getAppWriteBuffer().buffer();
            SSLEngine engine = handshakeModel.getSslEngine();

            // Network disconnection during handshake phase
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
                    Logger.info("Handshake status: " + handshakeStatus);
                }
                switch (handshakeStatus) {
                    case NEED_UNWRAP:
                        // Unwrap
                        netReadBuffer.flip();
                        if (netReadBuffer.hasRemaining()) {
                            result = engine.unwrap(netReadBuffer, appReadBuffer);
                            netReadBuffer.compact();
                        } else {
                            netReadBuffer.clear();
                            handshakeModel.getSocketChannel()
                                    .read(netReadBuffer, handshakeModel, handshakeCompletionHandler);
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

                            // BUFFER_UNDERFLOW can be triggered in two cases: 1. insufficient data read, 2.
                            // netReadBuffer is too small
                            case BUFFER_UNDERFLOW:
                                if (netReadBuffer.position() > 0) {
                                    handshakeModel.getSocketChannel()
                                            .read(netReadBuffer, handshakeModel, handshakeCompletionHandler);
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
                                Logger.info("Data not fully written...");
                            }
                            handshakeModel.getSocketChannel()
                                    .write(netWriteBuffer, handshakeModel, handshakeCompletionHandler);
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
                                handshakeModel.getSocketChannel()
                                        .write(netWriteBuffer, handshakeModel, handshakeCompletionHandler);
                                return;

                            case BUFFER_OVERFLOW:
                                if (debug) {
                                    Logger.warn("NEED_WRAP BUFFER_OVERFLOW");
                                }
                                break;

                            case BUFFER_UNDERFLOW:
                                throw new SSLException(
                                        "Buffer underflow occurred after a wrap. I don't think we should ever get here.");

                            case CLOSED:
                                if (debug) {
                                    Logger.warn("closed");
                                }
                                try {
                                    netWriteBuffer.flip();
                                    netReadBuffer.clear();
                                } catch (Exception e) {
                                    if (debug) {
                                        Logger.error(
                                                "Failed to send server's CLOSE message due to socket channel's failure.");
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

        /**
         * Description inherited from parent class or interface.
         */
        @Override
        public void completed(Integer result, HandshakeModel attachment) {
            if (result == -1) {
                failed(new IOException("eof"), attachment);
                return;
            }
            if (result != AsynchronousChannelProvider.READ_MONITOR_SIGNAL) {
                synchronized (attachment) {
                    doHandshake(attachment);
                }
            }
        }

        /**
         * Description inherited from parent class or interface.
         */
        @Override
        public void failed(Throwable exc, HandshakeModel attachment) {
            attachment.setException(exc);
            attachment.getHandshakeCallback().callback();
        }
    };

}
