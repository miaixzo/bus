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
package org.miaixz.bus.fabric.network.tcp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.time.Duration;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.exception.SocketException;
import org.miaixz.bus.core.lang.exception.StatefulException;
import org.miaixz.bus.core.lang.exception.TimeoutException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.fabric.Address;
import org.miaixz.bus.fabric.Call;
import org.miaixz.bus.fabric.Handler;
import org.miaixz.bus.fabric.Headers;
import org.miaixz.bus.fabric.Listener;
import org.miaixz.bus.fabric.Message;
import org.miaixz.bus.fabric.Payload;
import org.miaixz.bus.fabric.Session;
import org.miaixz.bus.fabric.Status;
import org.miaixz.bus.fabric.Wiring;
import org.miaixz.bus.fabric.protocol.socket.SocketOptions;
import org.miaixz.bus.fabric.runtime.Activity;
import org.miaixz.bus.fabric.runtime.dispatch.DispatchHandle;
import org.miaixz.bus.fabric.runtime.dispatch.Dispatcher;

/**
 * TCP server with a fixed backlog and handler-based accept loop.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class TcpServer implements AutoCloseable {

    /**
     * Listen address.
     */
    private final Address address;

    /**
     * Sessions accepted by this server.
     */
    private final Queue<TcpSession> sessions;

    /**
     * Running flag.
     */
    private final AtomicBoolean running;

    /**
     * Closed flag.
     */
    private final AtomicBoolean closed;

    /**
     * Lifecycle listener.
     */
    private final Listener<Object> listener;

    /**
     * Runtime dispatcher for accepted session writes.
     */
    private final Dispatcher dispatcher;

    /**
     * Listen backlog.
     */
    private final int backlog;

    /**
     * Whether this server owns the dispatcher lifecycle.
     */
    private final boolean ownsDispatcher;

    /**
     * Connection handler.
     */
    private volatile Handler handler;

    /**
     * Server channel.
     */
    private volatile ServerSocketChannel server;

    /**
     * Accept loop dispatch handle.
     */
    private volatile DispatchHandle acceptHandle;

    /**
     * Creates a TCP server.
     *
     * @param address listen address
     */
    public TcpServer(final Address address) {
        this(address, Wiring.noop(), Dispatcher.create(), true);
    }

    /**
     * Creates a TCP server.
     *
     * @param address  listen address
     * @param listener lifecycle listener
     */
    public TcpServer(final Address address, final Listener<Object> listener) {
        this(address, listener, Dispatcher.create(), true);
    }

    /**
     * Creates a TCP server with a shared dispatcher.
     *
     * @param address    listen address
     * @param listener   lifecycle listener
     * @param dispatcher shared dispatcher
     */
    public TcpServer(final Address address, final Listener<Object> listener, final Dispatcher dispatcher) {
        this(address, listener, dispatcher, SocketOptions.defaults());
    }

    /**
     * Creates a TCP server with a shared dispatcher and socket options.
     *
     * @param address    listen address
     * @param listener   lifecycle listener
     * @param dispatcher shared dispatcher
     * @param options    socket options
     */
    public TcpServer(final Address address, final Listener<Object> listener, final Dispatcher dispatcher,
                     final SocketOptions options) {
        this(address, listener, dispatcher, false, options);
    }

    /**
     * Creates a TCP server.
     *
     * @param address        listen address
     * @param listener       lifecycle listener
     * @param dispatcher     runtime dispatcher
     * @param ownsDispatcher true when close should stop dispatcher
     */
    private TcpServer(final Address address, final Listener<Object> listener, final Dispatcher dispatcher,
                      final boolean ownsDispatcher) {
        this(address, listener, dispatcher, ownsDispatcher, SocketOptions.defaults());
    }

    /**
     * Creates a TCP server.
     *
     * @param address        listen address
     * @param listener       lifecycle listener
     * @param dispatcher     runtime dispatcher
     * @param ownsDispatcher true when close should stop dispatcher
     * @param options        socket options
     */
    private TcpServer(final Address address, final Listener<Object> listener, final Dispatcher dispatcher,
                      final boolean ownsDispatcher, final SocketOptions options) {
        this.address = Assert.notNull(address, () -> new ValidateException("Server address must not be null"));
        this.sessions = new ConcurrentLinkedQueue<>();
        this.running = new AtomicBoolean();
        this.closed = new AtomicBoolean();
        this.listener = Wiring.safe(listener == null ? Wiring.noop() : listener, null);
        this.dispatcher = Assert.notNull(dispatcher, () -> new ValidateException("TCP dispatcher must not be null"));
        this.ownsDispatcher = ownsDispatcher;
        this.backlog = (options == null ? SocketOptions.defaults() : options).backlog();
    }

    /**
     * Returns the listen address.
     *
     * @return address
     */
    public Address address() {
        return address;
    }

    /**
     * Returns listen backlog.
     *
     * @return backlog
     */
    public int backlog() {
        return backlog;
    }

    /**
     * Starts the server.
     */
    public void start() {
        if (closed.get()) {
            throw new StatefulException("TCP server is closed");
        }
        if (running.compareAndSet(false, true)) {
            ServerSocketChannel opened = null;
            try {
                opened = ServerSocketChannel.open();
                opened.bind(socket(address), backlog);
                server = opened;
                acceptHandle = dispatcher.enqueue(
                        "tcp-server:" + address.host() + ":" + address.port(),
                        Activity.of("tcp-accept", this::acceptLoop));
                listener.open(this);
            } catch (final IOException e) {
                running.set(false);
                IoKit.closeQuietly(opened);
                listener.failure(this, e);
                throw new SocketException("Unable to start TCP server", e);
            } catch (final RuntimeException e) {
                running.set(false);
                IoKit.closeQuietly(opened);
                listener.failure(this, e);
                throw new InternalException("Unable to start TCP server", e);
            }
        }
    }

    /**
     * Sets the connection handler.
     *
     * @param handler handler
     */
    public void accept(final Handler handler) {
        this.handler = Assert.notNull(handler, () -> new ValidateException("TCP handler must not be null"));
    }

    /**
     * Returns whether this server is running.
     *
     * @return true when running
     */
    public boolean running() {
        final ServerSocketChannel current = server;
        return running.get() && current != null && current.isOpen();
    }

    /**
     * Closes this server.
     */
    @Override
    public void close() {
        if (closed.compareAndSet(false, true)) {
            running.set(false);
            closeServer();
            final DispatchHandle handle = acceptHandle;
            if (handle != null) {
                handle.cancel();
                acceptHandle = null;
            }
            TcpSession session = sessions.poll();
            while (session != null) {
                session.close();
                session = sessions.poll();
            }
            if (ownsDispatcher) {
                dispatcher.close();
            }
            listener.close(this);
        }
    }

    /**
     * Runs the accept loop.
     */
    private void acceptLoop() {
        while (running.get()) {
            try {
                final ServerSocketChannel current = server;
                if (current == null) {
                    return;
                }
                final SocketChannel socket = current.accept();
                handle(socket);
            } catch (final IOException e) {
                if (running.get()) {
                    listener.failure(this, e);
                    throw new SocketException("TCP accept failed", e);
                }
                return;
            }
        }
    }

    /**
     * Handles an accepted socket.
     *
     * @param socket socket
     */
    private void handle(final SocketChannel socket) {
        final Handler current = handler;
        if (current == null) {
            IoKit.closeQuietly(socket);
            return;
        }
        final TcpSession session = new TcpSession(address, socket, listener, dispatcher);
        sessions.add(session);
        listener.open(session);
        final Message message = Message.of(Protocol.TCP, address, Headers.empty(), Payload.empty(), socket);
        try {
            current.message(session, message);
        } catch (final RuntimeException e) {
            listener.failure(session, e);
            session.close();
            throw e;
        }
    }

    /**
     * Closes the server channel.
     */
    private void closeServer() {
        final ServerSocketChannel current = server;
        if (current != null) {
            try {
                current.close();
            } catch (final IOException e) {
                throw new SocketException("Unable to close TCP server", e);
            }
        }
    }

    /**
     * Creates a bind socket address.
     *
     * @param address bind address
     * @return socket address
     */
    private static InetSocketAddress socket(final Address address) {
        return new InetSocketAddress(address.host(), address.port());
    }

    /**
     * Accepted TCP session.
     */
    private static final class TcpSession implements Session {

        /**
         * Session address.
         */
        private final Address address;

        /**
         * Socket channel.
         */
        private final SocketChannel socket;

        /**
         * Lifecycle listener.
         */
        private final Listener<Object> listener;

        /**
         * Runtime dispatcher.
         */
        private final Dispatcher dispatcher;

        /**
         * State.
         */
        private volatile Status state = Status.OPENED;

        /**
         * Creates a session.
         *
         * @param address    address
         * @param socket     socket
         * @param listener   lifecycle listener
         * @param dispatcher runtime dispatcher
         */
        private TcpSession(final Address address, final SocketChannel socket, final Listener<Object> listener,
                           final Dispatcher dispatcher) {
            this.address = address;
            this.socket = socket;
            this.listener = Wiring.safe(listener == null ? Wiring.noop() : listener, null);
            this.dispatcher = dispatcher;
        }

        /**
         * Returns address.
         *
         * @return address
         */
        @Override
        public Address address() {
            return address;
        }

        /**
         * Returns state.
         *
         * @return state
         */
        @Override
        public Status state() {
            return state;
        }

        /**
         * Returns opened state.
         *
         * @return true when opened
         */
        @Override
        public boolean opened() {
            return state == Status.OPENED && socket.isOpen();
        }

        /**
         * Sends a payload.
         *
         * @param payload payload
         * @return send call
         */
        @Override
        public Call<Void> send(final Payload payload) {
            final Payload checkedPayload = Assert
                    .notNull(payload, () -> new ValidateException("Payload must not be null"));
            final CompletableFuture<Void> future = dispatcher.run("tcp:session:send", () -> {
                try {
                    socket.write(java.nio.ByteBuffer.wrap(checkedPayload.bytes()));
                } catch (final IOException e) {
                    throw new SocketException("Unable to send session payload", e);
                }
            });
            return new FutureCall<>(future);
        }

        /**
         * Closes this session.
         *
         * @return true when closed
         */
        @Override
        public boolean close() {
            if (state != Status.CLOSED) {
                IoKit.closeQuietly(socket);
                state = Status.CLOSED;
                listener.close(this);
                return true;
            }
            return false;
        }

        /**
         * Cancels this session.
         *
         * @return true when cancelled
         */
        @Override
        public boolean cancel() {
            if (state != Status.CANCELLED) {
                IoKit.closeQuietly(socket);
                state = Status.CANCELLED;
                listener.failure(this, new StatefulException("TCP session was cancelled"));
                return true;
            }
            return false;
        }

        /**
         * Returns attributes.
         *
         * @return attributes
         */
        @Override
        public Map<String, Object> attributes() {
            return Map.of();
        }

    }

    /**
     * Future-backed call.
     *
     * @param <T> result type
     */
    private static final class FutureCall<T> implements Call<T> {

        /**
         * Future.
         */
        private final CompletableFuture<T> future;

        /**
         * Creates a call.
         *
         * @param future future
         */
        private FutureCall(final CompletableFuture<T> future) {
            this.future = Assert.notNull(future, () -> new ValidateException("TCP call future must not be null"));
        }

        /**
         * Waits for the already-started call to complete.
         *
         * @return completed value
         */
        @Override
        public T execute() {
            return await();
        }

        /**
         * Returns this already-started call.
         *
         * @return this call
         */
        @Override
        public Call<T> enqueue() {
            return this;
        }

        /**
         * Waits for completion.
         *
         * @return completed value
         */
        @Override
        public T await() {
            try {
                return future.get();
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new InternalException("Interrupted while waiting for TCP call", e);
            } catch (final ExecutionException e) {
                throw new InternalException("TCP call failed", e.getCause());
            } catch (final CancellationException e) {
                throw new InternalException("TCP call was cancelled", e);
            }
        }

        /**
         * Waits for completion within a timeout.
         *
         * @param timeout timeout
         * @return completed value
         */
        @Override
        public T await(final Duration timeout) {
            validateTimeout(timeout);
            if (timeout.isZero()) {
                if (!future.isDone()) {
                    cancel();
                    throw new TimeoutException("TCP call timed out");
                }
                return await();
            }
            try {
                return future.get(timeout.toNanos(), TimeUnit.NANOSECONDS);
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new InternalException("Interrupted while waiting for TCP call", e);
            } catch (final ExecutionException e) {
                throw new InternalException("TCP call failed", e.getCause());
            } catch (final CancellationException e) {
                throw new InternalException("TCP call was cancelled", e);
            } catch (final java.util.concurrent.TimeoutException e) {
                cancel();
                throw new TimeoutException("TCP call timed out", e);
            } catch (final ArithmeticException e) {
                throw new ValidateException("Timeout is too large");
            }
        }

        /**
         * Cancels the future.
         *
         * @return true when cancelled
         */
        @Override
        public boolean cancel() {
            return future.cancel(true);
        }

        /**
         * Returns cancellation state.
         *
         * @return true when cancelled
         */
        @Override
        public boolean cancelled() {
            return future.isCancelled();
        }

        /**
         * Returns completion state.
         *
         * @return true when complete
         */
        @Override
        public boolean done() {
            return future.isDone();
        }

        /**
         * Validates timeout.
         *
         * @param timeout timeout
         */
        private static void validateTimeout(final Duration timeout) {
            final Duration checkedTimeout = Assert
                    .notNull(timeout, () -> new ValidateException("Timeout must be non-null and non-negative"));
            Assert.isFalse(
                    checkedTimeout.isNegative(),
                    () -> new ValidateException("Timeout must be non-null and non-negative"));
        }

    }

}
