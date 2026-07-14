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
import java.net.SocketAddress;
import java.net.SocketOption;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.exception.SocketException;
import org.miaixz.bus.core.lang.exception.TimeoutException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.Timeout;
import org.miaixz.bus.fabric.protocol.socket.SocketOptions;
import org.miaixz.bus.fabric.runtime.dispatch.Dispatcher;

/**
 * AIO socket channel with future and completion-handler operations.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class AioChannel implements AutoCloseable {

    /**
     * JDK socket channel.
     */
    private final AsynchronousSocketChannel channel;

    /**
     * Close flag.
     */
    private final AtomicBoolean closed;

    /**
     * Runtime dispatcher for blocking AIO waits.
     */
    private final Dispatcher dispatcher;

    /**
     * Whether this channel owns the dispatcher lifecycle.
     */
    private final boolean ownsDispatcher;

    /**
     * Socket tuning options.
     */
    private final SocketOptions options;

    /**
     * Local socket address.
     */
    private volatile SocketAddress local;

    /**
     * Remote socket address.
     */
    private volatile SocketAddress remote;

    /**
     * Creates an AIO channel.
     *
     * @param channel    JDK channel
     * @param dispatcher runtime dispatcher
     */
    AioChannel(final AsynchronousSocketChannel channel, final Dispatcher dispatcher) {
        this(channel, dispatcher, SocketOptions.defaults());
    }

    /**
     * Creates an AIO channel.
     *
     * @param channel    JDK channel
     * @param dispatcher runtime dispatcher
     * @param options    socket options
     */
    AioChannel(final AsynchronousSocketChannel channel, final Dispatcher dispatcher, final SocketOptions options) {
        this(channel, dispatcher, false, options);
    }

    /**
     * Creates an AIO channel with a private dispatcher.
     *
     * @param channel JDK channel
     */
    AioChannel(final AsynchronousSocketChannel channel) {
        this(channel, Dispatcher.create(), true, SocketOptions.defaults());
    }

    /**
     * Creates an AIO channel.
     *
     * @param channel        JDK channel
     * @param dispatcher     runtime dispatcher
     * @param ownsDispatcher true when close should stop dispatcher
     */
    private AioChannel(final AsynchronousSocketChannel channel, final Dispatcher dispatcher,
            final boolean ownsDispatcher, final SocketOptions options) {
        this.channel = Assert.notNull(channel, () -> new ValidateException("AIO channel must not be null"));
        this.dispatcher = Assert.notNull(dispatcher, () -> new ValidateException("AIO dispatcher must not be null"));
        this.ownsDispatcher = ownsDispatcher;
        this.options = options == null ? SocketOptions.defaults() : options;
        this.closed = new AtomicBoolean();
        applySocketOptions();
    }

    /**
     * Connects this channel.
     *
     * @param address socket address
     * @param timeout timeout policy
     * @return connection future
     */
    public CompletableFuture<Void> connect(final SocketAddress address, final Timeout timeout) {
        final SocketAddress checkedAddress = Assert
                .notNull(address, () -> new ValidateException("Socket address must not be null"));
        final Timeout checkedTimeout = Assert.notNull(timeout, () -> new ValidateException("Timeout must not be null"));
        final long connectNanos = checkedTimeout.connect().toNanos();
        if (!checkedTimeout.connect().isZero() && connectNanos <= Normal._1) {
            close();
            return CompletableFuture.failedFuture(new TimeoutException("AIO connect timed out"));
        }
        final Future<Void> operation;
        try {
            operation = channel.connect(checkedAddress);
        } catch (final RuntimeException e) {
            close();
            return CompletableFuture.failedFuture(new SocketException("AIO connect failed", e));
        }
        return dispatcher.run("aio:connect", () -> {
            try {
                if (checkedTimeout.connect().isZero()) {
                    operation.get();
                } else {
                    operation.get(connectNanos, TimeUnit.NANOSECONDS);
                }
                local = channel.getLocalAddress();
                remote = channel.getRemoteAddress();
            } catch (final java.util.concurrent.TimeoutException e) {
                operation.cancel(true);
                close();
                throw new TimeoutException("AIO connect timed out", e);
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
                close();
                throw new InternalException("Interrupted while connecting AIO channel", e);
            } catch (final ExecutionException e) {
                close();
                throw new SocketException("AIO connect failed", e.getCause());
            } catch (final IOException e) {
                close();
                throw new SocketException("Unable to read AIO channel addresses", e);
            }
        });
    }

    /**
     * Reads bytes.
     *
     * @param target target buffer
     * @return read future
     */
    public CompletableFuture<Integer> read(final ByteBuffer target) {
        final ByteBuffer checkedTarget = Assert
                .notNull(target, () -> new ValidateException("Read target must not be null"));
        try {
            final Future<Integer> operation = channel.read(checkedTarget.duplicate());
            return integerFuture(operation, "AIO read failed");
        } catch (final RuntimeException e) {
            return CompletableFuture.failedFuture(new SocketException("AIO read failed", e));
        }
    }

    /**
     * Reads bytes with a handler.
     *
     * @param target  target buffer
     * @param handler completion handler
     */
    public void read(final ByteBuffer target, final CompletionHandler<Integer, ByteBuffer> handler) {
        final CompletionHandler<Integer, ByteBuffer> checkedHandler = Assert
                .notNull(handler, () -> new ValidateException("Read handler must not be null"));
        complete(target, checkedHandler, read(target));
    }

    /**
     * Writes bytes.
     *
     * @param source source buffer
     * @return write future
     */
    public CompletableFuture<Integer> write(final ByteBuffer source) {
        final ByteBuffer checkedSource = Assert
                .notNull(source, () -> new ValidateException("Write source must not be null"));
        try {
            final ByteBuffer view = checkedSource.asReadOnlyBuffer();
            if (view.remaining() > options.writeChunkSize()) {
                view.limit(view.position() + options.writeChunkSize());
            }
            final Future<Integer> operation = channel.write(view);
            return integerFuture(operation, "AIO write failed");
        } catch (final RuntimeException e) {
            return CompletableFuture.failedFuture(new SocketException("AIO write failed", e));
        }
    }

    /**
     * Writes bytes with a handler.
     *
     * @param source  source buffer
     * @param handler completion handler
     */
    public void write(final ByteBuffer source, final CompletionHandler<Integer, ByteBuffer> handler) {
        final CompletionHandler<Integer, ByteBuffer> checkedHandler = Assert
                .notNull(handler, () -> new ValidateException("Write handler must not be null"));
        complete(source, checkedHandler, write(source));
    }

    /**
     * Returns the local socket address.
     *
     * @return local address
     */
    public SocketAddress local() {
        return local;
    }

    /**
     * Returns the remote socket address.
     *
     * @return remote address
     */
    public SocketAddress remote() {
        return remote;
    }

    /**
     * Closes this channel.
     */
    @Override
    public void close() {
        if (closed.compareAndSet(false, true)) {
            try {
                channel.close();
            } catch (final IOException e) {
                throw new SocketException("Unable to close AIO channel", e);
            } finally {
                if (ownsDispatcher) {
                    dispatcher.close();
                }
            }
        }
    }

    /**
     * Returns whether the channel is open.
     *
     * @return true when open
     */
    boolean opened() {
        return !closed.get() && channel.isOpen();
    }

    /**
     * Returns socket options.
     *
     * @return socket options
     */
    SocketOptions options() {
        return options;
    }

    /**
     * Applies configured JDK socket options.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void applySocketOptions() {
        for (final java.util.Map.Entry<SocketOption<?>, Object> entry : options.socketOptions().entrySet()) {
            try {
                channel.setOption((SocketOption) entry.getKey(), entry.getValue());
            } catch (final IOException e) {
                throw new SocketException("Unable to apply AIO socket option", e);
            }
        }
    }

    /**
     * Wraps a JDK integer future.
     *
     * @param operation operation
     * @param message   failure message
     * @return future
     */
    private CompletableFuture<Integer> integerFuture(final Future<Integer> operation, final String message) {
        return dispatcher.supply("aio:operation", () -> {
            try {
                return operation.get();
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new InternalException("Interrupted while waiting for AIO operation", e);
            } catch (final ExecutionException e) {
                throw new SocketException(message, e.getCause());
            }
        });
    }

    /**
     * Completes a handler from a future.
     *
     * @param buffer  buffer
     * @param handler handler
     * @param future  future
     */
    private static void complete(
            final ByteBuffer buffer,
            final CompletionHandler<Integer, ByteBuffer> handler,
            final CompletableFuture<Integer> future) {
        future.whenComplete((value, error) -> {
            if (error == null) {
                handler.completed(value, buffer);
            } else {
                handler.failed(error, buffer);
            }
        });
    }

}
