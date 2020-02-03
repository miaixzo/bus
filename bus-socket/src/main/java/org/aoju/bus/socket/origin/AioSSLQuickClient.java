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

import org.aoju.bus.socket.origin.plugins.ssl.SSLConfig;
import org.aoju.bus.socket.origin.plugins.ssl.SSLService;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutionException;

/**
 * AIO实现的客户端服务
 *
 * @author Kimi Liu
 * @version 5.5.5
 * @since JDK 1.8+
 */
public final class AioSSLQuickClient<T> extends AioQuickClient<T> {

    private SSLService sslService;

    private SSLConfig sslConfig = new SSLConfig();

    /**
     * @param host             远程服务器地址
     * @param port             远程服务器端口号
     * @param protocol         协议编解码
     * @param messageProcessor 消息处理器
     */
    public AioSSLQuickClient(String host, int port, Protocol<T> protocol, Message<T> messageProcessor) {
        super(host, port, protocol, messageProcessor);
    }

    @Override
    public AioSession<T> start(AsynchronousChannelGroup asynchronousChannelGroup) throws IOException, ExecutionException, InterruptedException {
        //启动SSL服务
        sslConfig.setClientMode(true);
        sslService = new SSLService(sslConfig);
        AsynchronousSocketChannel socketChannel = AsynchronousSocketChannel.open(asynchronousChannelGroup);
        socketChannel.connect(new InetSocketAddress(config.getHost(), config.getPort())).get();
        //连接成功则构造AIOSession对象
        session = new SSLAioSession<>(socketChannel, config, new TcpReadHandler<>(), new TcpWriteHandler<>(), sslService, bufferPool.allocateBufferPage());
        session.initSession();
        return session;
    }

    public AioSSLQuickClient<T> setKeyStore(String keyStoreFile, String keystorePassword) {
        sslConfig.setKeyFile(keyStoreFile);
        sslConfig.setKeystorePassword(keystorePassword);
        return this;
    }


    public AioSSLQuickClient<T> setKeyPassword(String keyPassword) {
        sslConfig.setKeyPassword(keyPassword);
        return this;
    }

    public AioSSLQuickClient<T> setTrust(String trustFile, String trustPassword) {
        sslConfig.setTrustFile(trustFile);
        sslConfig.setTrustPassword(trustPassword);
        return this;
    }

}
