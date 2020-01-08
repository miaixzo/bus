/*
 * The MIT License
 *
 * Copyright (c) 2020 aoju.org All rights reserved.
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

import org.aoju.bus.socket.origin.plugins.ssl.ClientAuth;
import org.aoju.bus.socket.origin.plugins.ssl.SSLConfig;
import org.aoju.bus.socket.origin.plugins.ssl.SSLService;

import java.io.IOException;

/**
 * AIO服务端
 *
 * @author Kimi Liu
 * @version 5.5.1
 * @since JDK 1.8+
 */
public class AioSSLQuickServer<T> extends AioQuickServer<T> {

    private SSLConfig sslConfig = new SSLConfig();

    private SSLService sslService;

    /**
     * @param port             绑定服务端口号
     * @param protocol         协议编解码
     * @param messageProcessor 消息处理器
     */
    public AioSSLQuickServer(int port, Protocol<T> protocol, Message<T> messageProcessor) {
        super(port, protocol, messageProcessor);
    }

    @Override
    public void start() throws IOException {
        sslService = new SSLService(sslConfig);
        start0(channel -> new SSLAioSession<>(channel, config, aioReadCompletionHandler, aioWriteCompletionHandler, sslService, bufferPool.allocateBufferPage()));
    }

    public AioSSLQuickServer<T> setKeyStore(String keyStoreFile, String keystorePassword) {
        sslConfig.setKeyFile(keyStoreFile);
        sslConfig.setKeystorePassword(keystorePassword);
        return this;
    }

    public AioSSLQuickServer<T> setKeyPassword(String keyPassword) {
        sslConfig.setKeyPassword(keyPassword);
        return this;
    }

    public AioSSLQuickServer<T> setTrust(String trustFile, String trustPassword) {
        sslConfig.setTrustFile(trustFile);
        sslConfig.setTrustPassword(trustPassword);
        return this;
    }

    public AioSSLQuickServer<T> setClientAuth(ClientAuth clientAuth) {
        sslConfig.setClientAuth(clientAuth);
        return this;
    }

}
