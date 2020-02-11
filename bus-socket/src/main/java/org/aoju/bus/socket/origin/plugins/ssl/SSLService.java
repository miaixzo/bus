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
package org.aoju.bus.socket.origin.plugins.ssl;

import org.aoju.bus.logger.Logger;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

/**
 * TLS/SSL服务
 *
 * @author Kimi Liu
 * @version 5.5.8
 * @since JDK 1.8+
 */
public class SSLService {


    private SSLContext sslContext;

    private SSLConfig config;

    private Completion handshakeCompletion = new Completion(this);

    public SSLService(SSLConfig config) {
        init(config);
    }

    private void init(SSLConfig config) {
        try {
            this.config = config;
            KeyManager[] keyManagers = null;
            if (config.getKeyFile() != null) {
                KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
                KeyStore ks = KeyStore.getInstance("JKS");
                ks.load(new FileInputStream(config.getKeyFile()), config.getKeystorePassword().toCharArray());
                kmf.init(ks, config.getKeyPassword().toCharArray());
                keyManagers = kmf.getKeyManagers();
            }

            TrustManager[] trustManagers;
            if (config.getTrustFile() != null) {
                KeyStore ts = KeyStore.getInstance("JKS");
                ts.load(new FileInputStream(config.getTrustFile()), config.getTrustPassword().toCharArray());
                TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
                tmf.init(ts);
                trustManagers = tmf.getTrustManagers();
            } else {
                trustManagers = new TrustManager[]{new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(X509Certificate[] x509Certificates, String s) {
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] x509Certificates, String s) {
                    }

                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }
                }};
            }
            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyManagers, trustManagers, new SecureRandom());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Handshake createSSLEngine(AsynchronousSocketChannel socketChannel) {
        try {
            Handshake handshakeModel = new Handshake();
            SSLEngine sslEngine = sslContext.createSSLEngine();
            SSLSession session = sslEngine.getSession();
            sslEngine.setUseClientMode(config.isClientMode());
            if (!config.isClientMode()) {
                switch (config.getClientAuth()) {
                    case OPTIONAL:
                        sslEngine.setWantClientAuth(true);
                        break;
                    case REQUIRE:
                        sslEngine.setNeedClientAuth(true);
                        break;
                    case NONE:
                        break;
                    default:
                        throw new Error("Unknown auth " + config.getClientAuth());
                }
            }
            handshakeModel.setSslEngine(sslEngine);
            handshakeModel.setAppWriteBuffer(ByteBuffer.allocate(0));
            handshakeModel.setNetWriteBuffer(ByteBuffer.allocate(session.getPacketBufferSize()));
            handshakeModel.getNetWriteBuffer().flip();
            handshakeModel.setAppReadBuffer(ByteBuffer.allocate(1));
            handshakeModel.setNetReadBuffer(ByteBuffer.allocate(1));
            sslEngine.beginHandshake();


            handshakeModel.setSocketChannel(socketChannel);
            return handshakeModel;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * 纯异步实现的SSL握手,
     * 在执行doHandshake期间必须保证当前通道无数据读写正在执行
     * 若触发了数据读写,也应立马终止doHandshake方法
     *
     * @param handshakeModel 握手信息
     */
    public void doHandshake(Handshake handshakeModel) {
        SSLEngineResult result;
        try {
            SSLEngineResult.HandshakeStatus handshakeStatus;
            ByteBuffer netReadBuffer = handshakeModel.getNetReadBuffer();
            ByteBuffer appReadBuffer = handshakeModel.getAppReadBuffer();
            ByteBuffer netWriteBuffer = handshakeModel.getNetWriteBuffer();
            ByteBuffer appWriteBuffer = handshakeModel.getAppWriteBuffer();
            SSLEngine engine = handshakeModel.getSslEngine();

            //握手阶段网络断链
            if (handshakeModel.isEof()) {
                Logger.warn("the ssl handshake is terminated");
                handshakeModel.setFinished(true);
                return;
            }
            while (!handshakeModel.isFinished()) {
                handshakeStatus = engine.getHandshakeStatus();
                switch (handshakeStatus) {
                    case NEED_UNWRAP://解码
                        netReadBuffer.flip();
                        if (netReadBuffer.hasRemaining()) {
                            result = engine.unwrap(netReadBuffer, appReadBuffer);
                            netReadBuffer.compact();
                        } else {
                            netReadBuffer.clear();
                            handshakeModel.getSocketChannel().read(netReadBuffer, handshakeModel, handshakeCompletion);
                            return;
                        }

                        if (result.getHandshakeStatus() == SSLEngineResult.HandshakeStatus.FINISHED) {
                            handshakeModel.setFinished(true);
                            netReadBuffer.clear();
                        }
                        switch (result.getStatus()) {
                            case OK:
                                break;
                            case BUFFER_OVERFLOW:
                                appReadBuffer = enlargeApplicationBuffer(engine, appReadBuffer);
                                handshakeModel.setAppReadBuffer(appReadBuffer);
                                break;
                            case BUFFER_UNDERFLOW:
                                netReadBuffer = handleBufferUnderflow(engine.getSession(), netReadBuffer);
                                handshakeModel.setNetReadBuffer(netReadBuffer);
                                handshakeModel.getSocketChannel().read(netReadBuffer, handshakeModel, handshakeCompletion);
                                return;
                            default:
                                throw new IllegalStateException("Invalid SSL status: " + result.getStatus());
                        }
                        break;
                    case NEED_WRAP:
                        if (netWriteBuffer.hasRemaining()) {
                            Logger.warn("数据未输出完毕...");
                            handshakeModel.getSocketChannel().write(netWriteBuffer, handshakeModel, handshakeCompletion);
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
                                handshakeModel.getSocketChannel().write(netWriteBuffer, handshakeModel, handshakeCompletion);
                                return;
                            case BUFFER_OVERFLOW:
                                Logger.warn("NEED_WRAP BUFFER_OVERFLOW");
                                netWriteBuffer = enlargePacketBuffer(engine.getSession(), netWriteBuffer);
                                if (netWriteBuffer.position() > 0) {
                                    netWriteBuffer.compact();
                                } else {
                                    netWriteBuffer.position(netWriteBuffer.limit());
                                    netWriteBuffer.limit(netWriteBuffer.capacity());
                                }
                                handshakeModel.setNetWriteBuffer(netWriteBuffer);
                                break;
                            case BUFFER_UNDERFLOW:
                                throw new SSLException("Buffer underflow occured after a wrap. I don't think we should ever get here.");
                            case CLOSED:
                                try {
                                    netWriteBuffer.flip();
                                    netReadBuffer.clear();
                                } catch (Exception e) {
                                    Logger.error("Failed to send server's CLOSE message due to socket channel's failure.");
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
                        Logger.info("HandshakeFinished");
                        break;
                    case NOT_HANDSHAKING:
                        Logger.info("NOT_HANDSHAKING");
                        System.exit(-1);
                        break;
                    default:
                        throw new IllegalStateException("Invalid SSL status: " + handshakeStatus);
                }
            }
            Logger.debug("握手完毕");
            handshakeModel.getHandshakeCallback().callback();

        } catch (Exception e) {
            try {
                handshakeModel.getSslEngine().closeInbound();
            } catch (SSLException e1) {
                e1.printStackTrace();
            }
            handshakeModel.getSslEngine().closeOutbound();
            try {
                handshakeModel.getSocketChannel().close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            Logger.error("", e);
        }
    }

    protected ByteBuffer enlargePacketBuffer(SSLSession session, ByteBuffer buffer) {
        return enlargeBuffer(buffer, session.getPacketBufferSize());
    }

    protected ByteBuffer enlargeApplicationBuffer(SSLEngine engine, ByteBuffer buffer) {
        return enlargeBuffer(buffer, engine.getSession().getApplicationBufferSize());
    }

    /**
     * Compares sessionProposedCapacity with buffer's capacity. If buffer's capacity is smaller,
     * returns a buffer with the proposed capacity. If it's equal or larger, returns a buffer
     * with capacity twice the size of the initial one.
     *
     * @param buffer                  - the buffer to be enlarged.
     * @param sessionProposedCapacity - the minimum size of the new buffer, proposed by {@link SSLSession}.
     * @return A new buffer with a larger capacity.
     */
    protected ByteBuffer enlargeBuffer(ByteBuffer buffer, int sessionProposedCapacity) {
        if (sessionProposedCapacity > buffer.capacity()) {
            buffer = ByteBuffer.allocate(sessionProposedCapacity);
        } else {
            buffer = ByteBuffer.allocate(buffer.capacity() * 2);
        }
        return buffer;
    }

    protected ByteBuffer handleBufferUnderflow(SSLSession session, ByteBuffer buffer) {
        if (session.getPacketBufferSize() < buffer.limit()) {
            return buffer;
        } else {
            ByteBuffer replaceBuffer = enlargePacketBuffer(session, buffer);
            buffer.flip();
            replaceBuffer.put(buffer);
            return replaceBuffer;
        }
    }

}
