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
package org.miaixz.bus.socket.plugin;

import org.miaixz.bus.socket.buffer.BufferPagePool;
import org.miaixz.bus.socket.secure.ssl.ClientAuth;
import org.miaixz.bus.socket.secure.ssl.SslAsynchronousSocketChannel;
import org.miaixz.bus.socket.secure.ssl.SslService;
import org.miaixz.bus.socket.secure.ssl.factory.ClientSSLContextFactory;
import org.miaixz.bus.socket.secure.ssl.factory.SSLContextFactory;
import org.miaixz.bus.socket.secure.ssl.factory.ServerSSLContextFactory;

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
    private final BufferPagePool bufferPagePool;

    public SslPlugin(SSLContextFactory factory, Consumer<SSLEngine> consumer) throws Exception {
        this(factory, consumer, BufferPagePool.DEFAULT_BUFFER_PAGE_POOL);
    }

    public SslPlugin(SSLContextFactory factory) throws Exception {
        this(factory, sslEngine -> sslEngine.setUseClientMode(false), BufferPagePool.DEFAULT_BUFFER_PAGE_POOL);
    }

    public SslPlugin(SSLContextFactory factory, Consumer<SSLEngine> consumer, BufferPagePool bufferPagePool)
            throws Exception {
        this.bufferPagePool = bufferPagePool;
        sslService = new SslService(factory.create(), consumer);
    }

    public SslPlugin(ClientSSLContextFactory factory) throws Exception {
        this(factory, BufferPagePool.DEFAULT_BUFFER_PAGE_POOL);
    }

    public SslPlugin(ClientSSLContextFactory factory, BufferPagePool bufferPagePool) throws Exception {
        this(factory, sslEngine -> sslEngine.setUseClientMode(true), bufferPagePool);
    }

    public SslPlugin(ServerSSLContextFactory factory, ClientAuth clientAuth) throws Exception {
        this(factory, clientAuth, BufferPagePool.DEFAULT_BUFFER_PAGE_POOL);
    }

    public SslPlugin(ServerSSLContextFactory factory, ClientAuth clientAuth, BufferPagePool bufferPagePool)
            throws Exception {
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
        }, bufferPagePool);
    }

    @Override
    public AsynchronousSocketChannel shouldAccept(AsynchronousSocketChannel channel) {
        return new SslAsynchronousSocketChannel(channel, sslService, bufferPagePool.allocateBufferPage());
    }

    public void debug(boolean debug) {
        sslService.debug(debug);
    }

}
