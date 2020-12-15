/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org sandao and other contributors.               *
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

import org.aoju.bus.core.io.PageBuffer;
import org.aoju.bus.logger.Logger;

import javax.net.ssl.*;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * TLS/SSL服务
 * keytool -genkey -validity 36000 -alias www.aoju.org -keyalg RSA -keystore server.keystore
 *
 * @author Kimi Liu
 * @version 6.1.5
 * @since JDK 1.8+
 */
public class SslService {

    private final boolean isClient;
    private final ClientAuth clientAuth;
    private final CompletionHandler<Integer, HandshakeModel> handshakeCompletionHandler = new CompletionHandler<Integer, HandshakeModel>() {
        @Override
        public void completed(Integer result, HandshakeModel attachment) {
            if (result == -1) {
                attachment.setEof(true);
            }
            synchronized (attachment) {
                doHandshake(attachment);
            }
        }

        @Override
        public void failed(Throwable exc, HandshakeModel attachment) {
            attachment.setEof(true);
            attachment.getHandshakeCallback().callback();
        }
    };
    private SSLContext sslContext;

    public SslService(boolean isClient, ClientAuth clientAuth) {
        this.isClient = isClient;
        this.clientAuth = clientAuth;
    }

    public void initKeyStore(InputStream keyStoreInputStream, String keyStorePassword, String keyPassword) {
        try {

            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            KeyStore ks = KeyStore.getInstance("JKS");
            ks.load(keyStoreInputStream, keyStorePassword.toCharArray());
            kmf.init(ks, keyPassword.toCharArray());
            KeyManager[] keyManagers = kmf.getKeyManagers();

            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyManagers, null, new SecureRandom());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void initTrust(InputStream trustInputStream, String trustPassword) {
        try {
            TrustManager[] trustManagers;
            if (trustInputStream != null) {
                KeyStore ts = KeyStore.getInstance("JKS");
                ts.load(trustInputStream, trustPassword.toCharArray());
                TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
                tmf.init(ts);
                trustManagers = tmf.getTrustManagers();
            } else {
                trustManagers = new TrustManager[]{new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                    }

                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }
                }};
            }
            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustManagers, new SecureRandom());

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    HandshakeModel createSSLEngine(AsynchronousSocketChannel socketChannel, PageBuffer pageBuffer) {
        try {
            HandshakeModel handshakeModel = new HandshakeModel();
            SSLEngine sslEngine = sslContext.createSSLEngine();
            SSLSession session = sslEngine.getSession();
            sslEngine.setUseClientMode(isClient);
            if (clientAuth != null) {
                switch (clientAuth) {
                    case OPTIONAL:
                        sslEngine.setWantClientAuth(true);
                        break;
                    case REQUIRE:
                        sslEngine.setNeedClientAuth(true);
                        break;
                    case NONE:
                        break;
                    default:
                        throw new Error("Unknown auth " + clientAuth);
                }
            }
            handshakeModel.setSslEngine(sslEngine);
            handshakeModel.setAppWriteBuffer(pageBuffer.allocate(session.getApplicationBufferSize()));
            handshakeModel.setNetWriteBuffer(pageBuffer.allocate(session.getPacketBufferSize()));
            handshakeModel.getNetWriteBuffer().buffer().flip();
            handshakeModel.setAppReadBuffer(pageBuffer.allocate(session.getApplicationBufferSize()));
            handshakeModel.setNetReadBuffer(pageBuffer.allocate(session.getPacketBufferSize()));
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
     * @param handshakeModel 握手模式
     */
    public void doHandshake(HandshakeModel handshakeModel) {
        SSLEngineResult result;
        try {
            SSLEngineResult.HandshakeStatus handshakeStatus;
            ByteBuffer netReadBuffer = handshakeModel.getNetReadBuffer().buffer();
            ByteBuffer appReadBuffer = handshakeModel.getAppReadBuffer().buffer();
            ByteBuffer netWriteBuffer = handshakeModel.getNetWriteBuffer().buffer();
            ByteBuffer appWriteBuffer = handshakeModel.getAppWriteBuffer().buffer();
            SSLEngine engine = handshakeModel.getSslEngine();

            //握手阶段网络断链
            if (handshakeModel.isEof()) {
                Logger.info("the ssl handshake is terminated");
                handshakeModel.getHandshakeCallback().callback();
                return;
            }
            while (!handshakeModel.isFinished()) {
                handshakeStatus = engine.getHandshakeStatus();
                if (Logger.get().isDebug()) {
                    Logger.info("握手状态:" + handshakeStatus);
                }
                switch (handshakeStatus) {
                    case NEED_UNWRAP:
                        //解码
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
                            netReadBuffer.clear();
                        }
                        switch (result.getStatus()) {
                            case OK:
                                break;
                            case BUFFER_OVERFLOW:
                                Logger.warn("doHandshake BUFFER_OVERFLOW");
                                break;
                            //两种情况会触发BUFFER_UNDERFLOW,1:读到的数据不够,2:netReadBuffer空间太小
                            case BUFFER_UNDERFLOW:
                                Logger.warn("doHandshake BUFFER_UNDERFLOW");
                                return;
                            default:
                                throw new IllegalStateException("Invalid SSL status: " + result.getStatus());
                        }
                        break;
                    case NEED_WRAP:
                        if (netWriteBuffer.hasRemaining()) {
                            Logger.info("数据未输出完毕...");
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
                                Logger.warn("NEED_WRAP BUFFER_OVERFLOW");
                                break;
                            case BUFFER_UNDERFLOW:
                                throw new SSLException("Buffer underflow occured after a wrap. I don't think we should ever get here.");
                            case CLOSED:
                                Logger.warn("closed");
                                try {
                                    netWriteBuffer.flip();
                                    netReadBuffer.clear();
                                } catch (Exception e) {
                                    Logger.warn("Failed to send server's CLOSE message due to socket channel's failure.");
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
                        Logger.error("NOT_HANDSHAKING");
                        break;
                    default:
                        throw new IllegalStateException("Invalid SSL status: " + handshakeStatus);
                }
            }
            handshakeModel.getHandshakeCallback().callback();

        } catch (Exception e) {
            Logger.warn("ignore doHandshake exception: {}", e.getMessage());
            handshakeModel.setEof(true);
            handshakeModel.getHandshakeCallback().callback();
        }
    }

}
