/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2022 aoju.org sandao and other contributors.               *
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
package org.aoju.bus.socket.plugins;

import org.aoju.bus.core.io.ByteBuffer;
import org.aoju.bus.socket.BufferFactory;
import org.aoju.bus.socket.security.ClientAuth;
import org.aoju.bus.socket.security.SslService;
import org.aoju.bus.socket.security.SslSocketChannel;

import java.io.InputStream;
import java.nio.channels.AsynchronousSocketChannel;

/**
 * SSL/TLS通信插件
 *
 * @author Kimi Liu
 * @version 6.3.5
 * @since Java 17+
 */
public class SslPlugin<T> extends AbstractPlugin<T> {

    private final ByteBuffer bufferPool;
    private SslService sslService;
    private boolean init = false;

    public SslPlugin() {
        this.bufferPool = BufferFactory.DISABLED_BUFFER_FACTORY.create();
    }

    public SslPlugin(ByteBuffer bufferPool) {
        this.bufferPool = bufferPool;
    }

    public void initForServer(InputStream keyStoreInputStream, String keyStorePassword, String keyPassword, ClientAuth clientAuth) {
        initCheck();
        sslService = new SslService(false, clientAuth);
        sslService.initKeyStore(keyStoreInputStream, keyStorePassword, keyPassword);
    }

    public void initForClient() {
        initForClient(null, null);
    }

    public void initForClient(InputStream trustInputStream, String trustPassword) {
        initCheck();
        sslService = new SslService(true, null);
        sslService.initTrust(trustInputStream, trustPassword);
    }

    private void initCheck() {
        if (init) {
            throw new RuntimeException("plugin is already init");
        }
        init = true;
    }

    @Override
    public final AsynchronousSocketChannel shouldAccept(AsynchronousSocketChannel channel) {
        return new SslSocketChannel(channel, sslService, bufferPool.allocatePageBuffer());
    }

}
