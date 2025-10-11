/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org sandao and other contributors.             ~
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
import org.miaixz.bus.socket.secure.ClientAuth;
import org.miaixz.bus.socket.secure.SslAsynchronousSocketChannel;
import org.miaixz.bus.socket.secure.SslService;
import org.miaixz.bus.socket.secure.factory.ClientSSLContextFactory;
import org.miaixz.bus.socket.secure.factory.SSLContextFactory;
import org.miaixz.bus.socket.secure.factory.ServerSSLContextFactory;

import javax.net.ssl.SSLEngine;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.function.Consumer;

/**
 * A plugin for enabling SSL/TLS communication on socket channels.
 * <p>
 * This plugin integrates SSL/TLS encryption by wrapping standard {@link AsynchronousSocketChannel}s with
 * {@link SslAsynchronousSocketChannel} instances, managing the SSL handshake and data encryption/decryption.
 * </p>
 *
 * @param <T> the type of message object entity handled by this plugin
 * @author Kimi Liu
 * @since Java 17+
 */
public final class SslPlugin<T> extends AbstractPlugin<T> {

    /**
     * The SSL service responsible for managing SSL/TLS operations.
     */
    private final SslService sslService;
    /**
     * The buffer page pool used for allocating buffers for SSL/TLS operations.
     */
    private final BufferPagePool bufferPagePool;

    /**
     * Constructs an {@code SslPlugin} with a custom SSL context factory and an SSLEngine consumer.
     *
     * @param factory  the {@link SSLContextFactory} to create the SSLContext
     * @param consumer a {@link Consumer} to configure the {@link SSLEngine}
     * @throws Exception if an error occurs during SSLContext initialization
     */
    public SslPlugin(SSLContextFactory factory, Consumer<SSLEngine> consumer) throws Exception {
        this(factory, consumer, BufferPagePool.DEFAULT_BUFFER_PAGE_POOL);
    }

    /**
     * Constructs an {@code SslPlugin} for server-side SSL with a custom SSL context factory.
     *
     * @param factory the {@link SSLContextFactory} to create the SSLContext
     * @throws Exception if an error occurs during SSLContext initialization
     */
    public SslPlugin(SSLContextFactory factory) throws Exception {
        this(factory, sslEngine -> sslEngine.setUseClientMode(false), BufferPagePool.DEFAULT_BUFFER_PAGE_POOL);
    }

    /**
     * Constructs an {@code SslPlugin} with a custom SSL context factory, an SSLEngine consumer, and a buffer page pool.
     *
     * @param factory        the {@link SSLContextFactory} to create the SSLContext
     * @param consumer       a {@link Consumer} to configure the {@link SSLEngine}
     * @param bufferPagePool the {@link BufferPagePool} to use for SSL buffers
     * @throws Exception if an error occurs during SSLContext initialization
     */
    public SslPlugin(SSLContextFactory factory, Consumer<SSLEngine> consumer, BufferPagePool bufferPagePool)
            throws Exception {
        this.bufferPagePool = bufferPagePool;
        sslService = new SslService(factory.create(), consumer);
    }

    /**
     * Constructs an {@code SslPlugin} for client-side SSL with a client SSL context factory.
     *
     * @param factory the {@link ClientSSLContextFactory} to create the SSLContext
     * @throws Exception if an error occurs during SSLContext initialization
     */
    public SslPlugin(ClientSSLContextFactory factory) throws Exception {
        this(factory, BufferPagePool.DEFAULT_BUFFER_PAGE_POOL);
    }

    /**
     * Constructs an {@code SslPlugin} for client-side SSL with a client SSL context factory and a buffer page pool.
     *
     * @param factory        the {@link ClientSSLContextFactory} to create the SSLContext
     * @param bufferPagePool the {@link BufferPagePool} to use for SSL buffers
     * @throws Exception if an error occurs during SSLContext initialization
     */
    public SslPlugin(ClientSSLContextFactory factory, BufferPagePool bufferPagePool) throws Exception {
        this(factory, sslEngine -> sslEngine.setUseClientMode(true), bufferPagePool);
    }

    /**
     * Constructs an {@code SslPlugin} for server-side SSL with a server SSL context factory and client authentication
     * requirements.
     *
     * @param factory    the {@link ServerSSLContextFactory} to create the SSLContext
     * @param clientAuth the {@link ClientAuth} setting for client authentication
     * @throws Exception if an error occurs during SSLContext initialization
     */
    public SslPlugin(ServerSSLContextFactory factory, ClientAuth clientAuth) throws Exception {
        this(factory, clientAuth, BufferPagePool.DEFAULT_BUFFER_PAGE_POOL);
    }

    /**
     * Constructs an {@code SslPlugin} for server-side SSL with a server SSL context factory, client authentication
     * requirements, and a buffer page pool.
     *
     * @param factory        the {@link ServerSSLContextFactory} to create the SSLContext
     * @param clientAuth     the {@link ClientAuth} setting for client authentication
     * @param bufferPagePool the {@link BufferPagePool} to use for SSL buffers
     * @throws Exception if an error occurs during SSLContext initialization
     */
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
                    throw new Error("Unknown client authentication mode: " + clientAuth);
            }
        }, bufferPagePool);
    }

    @Override
    public AsynchronousSocketChannel shouldAccept(AsynchronousSocketChannel channel) {
        return new SslAsynchronousSocketChannel(channel, sslService, bufferPagePool.allocateBufferPage());
    }

    /**
     * Enables or disables SSL debug logging.
     *
     * @param debug {@code true} to enable debug logging, {@code false} to disable
     */
    public void debug(boolean debug) {
        sslService.debug(debug);
    }

}
