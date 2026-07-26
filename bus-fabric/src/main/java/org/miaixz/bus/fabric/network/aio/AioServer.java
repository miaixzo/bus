/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.fabric.network.aio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.atomic.AtomicBoolean;

import org.miaixz.bus.core.Lifecycle.State;
import org.miaixz.bus.core.center.function.ConsumerX;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.SocketException;
import org.miaixz.bus.core.lang.exception.StatefulException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.Address;
import org.miaixz.bus.fabric.Listener;
import org.miaixz.bus.fabric.observe.EventObserver;
import org.miaixz.bus.fabric.protocol.socket.SocketOptions;
import org.miaixz.bus.fabric.runtime.lifecycle.LifecycleScope;

/**
 * Protocol-neutral asynchronous TCP listener backed by an existing {@link AioGroup}.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class AioServer implements AutoCloseable {

    /**
     * Logical bind address.
     */
    private final Address address;

    /**
     * Borrowed asynchronous group.
     */
    private final AioGroup group;

    /**
     * Accepted socket options.
     */
    private final SocketOptions options;

    /**
     * Server lifecycle and accepted-channel owner.
     */
    private final LifecycleScope lifecycle;

    /**
     * Start guard.
     */
    private final AtomicBoolean started;

    /**
     * Close guard.
     */
    private final AtomicBoolean closed;

    /**
     * Active accept consumer.
     */
    private volatile ConsumerX<? super AioChannel> consumer;

    /**
     * Listening channel.
     */
    private volatile AsynchronousServerSocketChannel server;

    /**
     * Creates a server.
     *
     * @param address  bind address
     * @param group    borrowed AIO group
     * @param listener lifecycle listener
     * @param options  socket options
     */
    AioServer(final Address address, final AioGroup group, final Listener<Object> listener,
            final SocketOptions options) {
        this.address = Assert.notNull(address, () -> new ValidateException("AIO server address must not be null"));
        this.group = Assert.notNull(group, () -> new ValidateException("AIO group must not be null"));
        this.options = options == null ? SocketOptions.defaults() : options;
        this.lifecycle = LifecycleScope.resource(this, "aio-server", listener, EventObserver.noop());
        this.started = new AtomicBoolean();
        this.closed = new AtomicBoolean();
    }

    /**
     * Binds and starts accepting channels.
     *
     * @param consumer accepted-channel consumer
     * @return this server
     */
    public AioServer start(final ConsumerX<? super AioChannel> consumer) {
        final ConsumerX<? super AioChannel> current = Assert
                .notNull(consumer, () -> new ValidateException("AIO accept consumer must not be null"));
        if (!started.compareAndSet(false, true)) {
            throw new StatefulException("AIO server can only be started once");
        }
        if (closed.get() || !group.opened()) {
            throw new StatefulException("AIO server is closed");
        }
        AsynchronousServerSocketChannel opened = null;
        try {
            opened = AsynchronousServerSocketChannel.open(group.channelGroup);
            opened.bind(new InetSocketAddress(address.host(), address.port()), options.backlog());
            server = lifecycle.own(opened);
            this.consumer = current;
            lifecycle.open(this);
            acceptNext();
            return this;
        } catch (final IOException | RuntimeException e) {
            closeOpened(opened);
            lifecycle.fail(e);
            throw e instanceof RuntimeException runtime ? runtime
                    : new SocketException("Unable to start AIO server", e);
        }
    }

    /**
     * Returns current lifecycle state.
     *
     * @return state
     */
    public State state() {
        return lifecycle.state();
    }

    /**
     * Registers the single next accept operation.
     */
    private void acceptNext() {
        final AsynchronousServerSocketChannel current = server;
        if (closed.get() || current == null || !current.isOpen()) {
            return;
        }
        try {
            current.accept(this, new CompletionHandler<>() {

                /**
                 * Rearms acceptance before publishing the accepted channel.
                 *
                 * @param channel accepted native channel
                 * @param owner   asynchronous server owning the accept loop
                 */
                @Override
                public void completed(final AsynchronousSocketChannel channel, final AioServer owner) {
                    owner.acceptNext();
                    owner.accepted(channel);
                }

                /**
                 * Terminates the listener after a non-shutdown accept failure.
                 *
                 * @param cause accept failure
                 * @param owner asynchronous server owning the accept loop
                 */
                @Override
                public void failed(final Throwable cause, final AioServer owner) {
                    if (owner.closed.get() || cause instanceof AsynchronousCloseException) {
                        return;
                    }
                    owner.lifecycle.fail(cause);
                    owner.close();
                }

            });
        } catch (final RuntimeException e) {
            if (!closed.get()) {
                lifecycle.fail(e);
                close();
            }
        }
    }

    /**
     * Wraps and publishes an accepted native channel.
     *
     * @param channel accepted channel
     */
    private void accepted(final AsynchronousSocketChannel channel) {
        if (closed.get()) {
            closeOpened(channel);
            return;
        }
        AioChannel accepted = null;
        try {
            accepted = AioChannel.accepted(channel, group.dispatcher(), lifecycle, options);
            consumer.accept(accepted);
        } catch (final RuntimeException e) {
            if (accepted != null) {
                accepted.close();
            } else {
                closeOpened(channel);
            }
        }
    }

    /**
     * Closes the listener and accepted channels.
     */
    @Override
    public void close() {
        if (!closed.compareAndSet(false, true)) {
            return;
        }
        lifecycle.close(this);
        server = null;
        consumer = null;
    }

    /**
     * Closes a partially transferred channel.
     *
     * @param channel closeable channel
     */
    private static void closeOpened(final AutoCloseable channel) {
        if (channel == null) {
            return;
        }
        try {
            channel.close();
        } catch (final Exception ignored) {
            // The start or accept failure remains authoritative.
        }
    }

}
