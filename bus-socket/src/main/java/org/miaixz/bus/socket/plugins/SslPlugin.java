/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org sandao and other contributors.             *
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
package org.miaixz.bus.socket.plugins;

import org.miaixz.bus.socket.buffers.BufferFactory;
import org.miaixz.bus.socket.buffers.BufferPool;
import org.miaixz.bus.socket.channel.SslAsynchronousSocketChannel;
import org.miaixz.bus.socket.security.ClientAuth;
import org.miaixz.bus.socket.security.SslService;
import org.miaixz.bus.socket.security.factory.ClientSSLContextFactory;
import org.miaixz.bus.socket.security.factory.SSLContextFactory;
import org.miaixz.bus.socket.security.factory.ServerSSLContextFactory;

import javax.net.ssl.SSLEngine;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.function.Consumer;

/**
 * SSL/TLS通信插件
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public final class SslPlugin<T> extends AbstractPlugin<T> {

    private final SslService sslService;
    private final BufferPool bufferPool;

    public SslPlugin(SSLContextFactory factory, Consumer<SSLEngine> consumer) throws Exception {
        this(factory, consumer, BufferFactory.DISABLED_BUFFER_FACTORY.create());
    }

    public SslPlugin(SSLContextFactory factory, Consumer<SSLEngine> consumer, BufferPool bufferPool) throws Exception {
        this.bufferPool = bufferPool;
        sslService = new SslService(factory.create(), consumer);
    }

    public SslPlugin(ClientSSLContextFactory factory) throws Exception {
        this(factory, BufferFactory.DISABLED_BUFFER_FACTORY.create());
    }

    public SslPlugin(ClientSSLContextFactory factory, BufferPool bufferPool) throws Exception {
        this(factory, sslEngine -> sslEngine.setUseClientMode(true), bufferPool);
    }

    public SslPlugin(ServerSSLContextFactory factory, ClientAuth clientAuth) throws Exception {
        this(factory, clientAuth, BufferFactory.DISABLED_BUFFER_FACTORY.create());
    }

    public SslPlugin(ServerSSLContextFactory factory, ClientAuth clientAuth, BufferPool bufferPool) throws Exception {
        this(factory, sslEngine -> {
            sslEngine.setUseClientMode(false);
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
        }, bufferPool);
    }

    @Override
    public AsynchronousSocketChannel shouldAccept(AsynchronousSocketChannel channel) {
        return new SslAsynchronousSocketChannel(channel, sslService, bufferPool.allocateBufferPage());
    }

}
