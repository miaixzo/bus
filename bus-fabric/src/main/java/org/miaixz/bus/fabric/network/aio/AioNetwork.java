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
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.io.sink.Sink;
import org.miaixz.bus.core.io.source.Source;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.exception.SocketException;
import org.miaixz.bus.core.lang.exception.TimeoutException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.xyz.ExceptionKit;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.fabric.Address;
import org.miaixz.bus.fabric.Handler;
import org.miaixz.bus.fabric.Listener;
import org.miaixz.bus.fabric.Status;
import org.miaixz.bus.fabric.Timeout;
import org.miaixz.bus.fabric.network.Conduit;
import org.miaixz.bus.fabric.network.Connection;
import org.miaixz.bus.fabric.network.Destination;
import org.miaixz.bus.fabric.network.dns.DnsResolver;
import org.miaixz.bus.fabric.network.dns.DnsResult;
import org.miaixz.bus.fabric.network.tcp.TcpServer;
import org.miaixz.bus.fabric.observe.EventObserver;
import org.miaixz.bus.fabric.observe.ObservationMarker;
import org.miaixz.bus.fabric.protocol.socket.SocketOptions;
import org.miaixz.bus.fabric.runtime.dispatch.Dispatcher;
import org.miaixz.bus.fabric.runtime.lifecycle.LifecycleScope;

/**
 * Default AIO network adapter for client connections and TCP servers.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class AioNetwork implements AutoCloseable {

    /**
     * Worker group.
     */
    private final AioGroup group;

    /**
     * Provider.
     */
    private final AioProvider provider;

    /**
     * Managed resources.
     */
    private final ConcurrentLinkedDeque<AutoCloseable> managed;

    /**
     * DNS resolver.
     */
    private final DnsResolver resolver;

    /**
     * Close flag.
     */
    private final AtomicBoolean closed;

    /**
     * Lifecycle listener.
     */
    private final Listener<Object> listener;

    /**
     * Socket tuning options.
     */
    private final SocketOptions socketOptions;

    /**
     * Creates a network.
     *
     * @param group    group
     * @param provider provider
     * @param resolver DNS resolver
     * @param listener lifecycle listener
     */
    private AioNetwork(final AioGroup group, final AioProvider provider, final DnsResolver resolver,
            final Listener<Object> listener) {
        this(group, provider, resolver, listener, SocketOptions.defaults());
    }

    /**
     * Creates a network.
     *
     * @param group         group
     * @param provider      provider
     * @param resolver      DNS resolver
     * @param listener      lifecycle listener
     * @param socketOptions socket options
     */
    private AioNetwork(final AioGroup group, final AioProvider provider, final DnsResolver resolver,
            final Listener<Object> listener, final SocketOptions socketOptions) {
        this.group = Assert.notNull(group, () -> new ValidateException("AIO group must not be null"));
        this.provider = Assert.notNull(provider, () -> new ValidateException("AIO provider must not be null"));
        this.resolver = Assert.notNull(resolver, () -> new ValidateException("DNS resolver must not be null"));
        this.managed = new ConcurrentLinkedDeque<>();
        this.closed = new AtomicBoolean();
        this.listener = safe(listener);
        this.socketOptions = socketOptions == null ? SocketOptions.defaults() : socketOptions;
    }

    /**
     * Creates a default AIO network.
     *
     * @return AIO network
     */
    public static AioNetwork create() {
        return create(SocketOptions.defaults());
    }

    /**
     * Creates a default AIO network.
     *
     * @param socketOptions socket options
     * @return AIO network
     */
    public static AioNetwork create(final SocketOptions socketOptions) {
        AioGroup group = null;
        try {
            final SocketOptions current = socketOptions == null ? SocketOptions.defaults() : socketOptions;
            group = AioGroup.create(current.ioThreads());
            return new AioNetwork(group, AioProvider.system(), DnsResolver.system(), null, current);
        } catch (final RuntimeException e) {
            if (group != null) {
                group.shutdown();
            }
            throw new InternalException("Unable to create AIO network", e);
        }
    }

    /**
     * Creates a default AIO network with a lifecycle listener.
     *
     * @param listener lifecycle listener
     * @return AIO network
     */
    public static AioNetwork create(final Listener<Object> listener) {
        return create(listener, SocketOptions.defaults());
    }

    /**
     * Creates a default AIO network with a lifecycle listener.
     *
     * @param listener      lifecycle listener
     * @param socketOptions socket options
     * @return AIO network
     */
    public static AioNetwork create(final Listener<Object> listener, final SocketOptions socketOptions) {
        AioGroup group = null;
        try {
            final SocketOptions current = socketOptions == null ? SocketOptions.defaults() : socketOptions;
            group = AioGroup.create(current.ioThreads());
            return new AioNetwork(group, AioProvider.system(), DnsResolver.system(), listener, current);
        } catch (final RuntimeException e) {
            if (group != null) {
                group.shutdown();
            }
            throw new InternalException("Unable to create AIO network", e);
        }
    }

    /**
     * Creates a default AIO network with lifecycle listener and DNS resolver.
     *
     * @param listener lifecycle listener
     * @param resolver DNS resolver
     * @return AIO network
     */
    public static AioNetwork create(final Listener<Object> listener, final DnsResolver resolver) {
        return create(listener, resolver, SocketOptions.defaults());
    }

    /**
     * Creates a default AIO network with lifecycle listener and DNS resolver.
     *
     * @param listener      lifecycle listener
     * @param resolver      DNS resolver
     * @param socketOptions socket options
     * @return AIO network
     */
    public static AioNetwork create(
            final Listener<Object> listener,
            final DnsResolver resolver,
            final SocketOptions socketOptions) {
        AioGroup group = null;
        try {
            final SocketOptions current = socketOptions == null ? SocketOptions.defaults() : socketOptions;
            group = AioGroup.create(current.ioThreads());
            return new AioNetwork(group, AioProvider.system(),
                    Assert.notNull(resolver, () -> new ValidateException("DNS resolver must not be null")), listener,
                    current);
        } catch (final RuntimeException e) {
            if (group != null) {
                group.shutdown();
            }
            throw new InternalException("Unable to create AIO network", e);
        }
    }

    /**
     * Creates a default AIO network with lifecycle listener, DNS resolver, and shared dispatcher.
     *
     * @param listener   lifecycle listener
     * @param resolver   DNS resolver
     * @param dispatcher shared dispatcher
     * @return AIO network
     */
    public static AioNetwork create(
            final Listener<Object> listener,
            final DnsResolver resolver,
            final Dispatcher dispatcher) {
        return create(listener, resolver, dispatcher, SocketOptions.defaults());
    }

    /**
     * Creates a default AIO network with lifecycle listener, DNS resolver, shared dispatcher, and socket options.
     *
     * @param listener      lifecycle listener
     * @param resolver      DNS resolver
     * @param dispatcher    shared dispatcher
     * @param socketOptions socket options
     * @return AIO network
     */
    public static AioNetwork create(
            final Listener<Object> listener,
            final DnsResolver resolver,
            final Dispatcher dispatcher,
            final SocketOptions socketOptions) {
        AioGroup group = null;
        try {
            final SocketOptions current = socketOptions == null ? SocketOptions.defaults() : socketOptions;
            group = AioGroup.create(
                    current.ioThreads(),
                    Assert.notNull(dispatcher, () -> new ValidateException("Dispatcher must not be null")));
            return new AioNetwork(group, AioProvider.system(),
                    Assert.notNull(resolver, () -> new ValidateException("DNS resolver must not be null")), listener,
                    current);
        } catch (final RuntimeException e) {
            if (group != null) {
                group.shutdown();
            }
            throw new InternalException("Unable to create AIO network", e);
        }
    }

    /**
     * Returns the worker group.
     *
     * @return group
     */
    public AioGroup group() {
        return group;
    }

    /**
     * Returns socket options.
     *
     * @return socket options
     */
    public SocketOptions socketOptions() {
        return socketOptions;
    }

    /**
     * Opens a client connection.
     *
     * @param address address
     * @param timeout timeout policy
     * @return connection future
     */
    public CompletableFuture<Connection> connect(final Address address, final Timeout timeout) {
        return connect(address, timeout, null);
    }

    /**
     * Opens a client connection with an additional lifecycle listener.
     *
     * @param address  address
     * @param timeout  timeout policy
     * @param listener lifecycle listener
     * @return connection future
     */
    public CompletableFuture<Connection> connect(
            final Address address,
            final Timeout timeout,
            final Listener<Object> listener) {
        final Address checkedAddress = Assert.notNull(address, () -> new ValidateException("Address must not be null"));
        final Timeout checkedTimeout = Assert.notNull(timeout, () -> new ValidateException("Timeout must not be null"));
        final Listener<Object> current = compose(this.listener, listener);
        final DnsResult result = resolver.resolve(checkedAddress.host());
        if (result.empty()) {
            return CompletableFuture
                    .failedFuture(new SocketException("Unable to resolve host " + checkedAddress.host()));
        }
        return connectCandidate(checkedAddress, checkedTimeout, current, result.addresses(), Normal._0, null);
    }

    /**
     * Connects to ordered DNS candidates.
     *
     * @param address   target address
     * @param timeout   timeout policy
     * @param listener  lifecycle listener
     * @param addresses ordered candidates
     * @param index     current candidate index
     * @param failure   previous failure
     * @return connection future
     */
    private CompletableFuture<Connection> connectCandidate(
            final Address address,
            final Timeout timeout,
            final Listener<Object> listener,
            final List<InetAddress> addresses,
            final int index,
            final Throwable failure) {
        if (index >= addresses.size()) {
            final Throwable root = ExceptionKit.unwrap(failure);
            if (root instanceof TimeoutException timedOut) {
                listener.failure(this, timedOut);
                return CompletableFuture.failedFuture(timedOut);
            }
            final SocketException failed = new SocketException("Unable to connect to resolved host " + address.host(),
                    root);
            listener.failure(this, failed);
            return CompletableFuture.failedFuture(failed);
        }
        final AioChannel channel = provider.openChannel(group, socketOptions);
        managed.add(channel);
        final InetSocketAddress socket = new InetSocketAddress(addresses.get(index), address.port());
        final CompletableFuture<Connection> opened = new CompletableFuture<>();
        channel.connect(socket, timeout).whenComplete((ignored, cause) -> {
            if (cause != null) {
                managed.remove(channel);
                IoKit.closeQuietly(channel);
                connectCandidate(address, timeout, listener, addresses, index + Normal._1, ExceptionKit.unwrap(cause))
                        .whenComplete((connection, next) -> {
                            if (next == null) {
                                opened.complete(connection);
                            } else {
                                opened.completeExceptionally(ExceptionKit.unwrap(next));
                            }
                        });
                return;
            }
            final Connection connection = new AioConnection(
                    Destination.of(address.protocol(), address, socketOptions.toOptions()), channel, listener);
            opened.complete(connection);
        });
        return opened;
    }

    /**
     * Creates a TCP server.
     *
     * @param address address
     * @param handler handler
     * @return server
     */
    public TcpServer server(final Address address, final Handler handler) {
        return server(address, handler, null);
    }

    /**
     * Creates a TCP server with an additional lifecycle listener.
     *
     * @param address  address
     * @param handler  handler
     * @param listener lifecycle listener
     * @return server
     */
    public TcpServer server(final Address address, final Handler handler, final Listener<Object> listener) {
        final Address checkedAddress = Assert
                .notNull(address, () -> new ValidateException("Server address must not be null"));
        final Handler checkedHandler = Assert
                .notNull(handler, () -> new ValidateException("Server handler must not be null"));
        final TcpServer server = provider
                .openServer(checkedAddress, group, compose(this.listener, listener), socketOptions);
        server.accept(checkedHandler);
        managed.add(server);
        return server;
    }

    /**
     * Closes this network.
     */
    @Override
    public void close() {
        if (closed.compareAndSet(false, true)) {
            final List<Throwable> failures = new ArrayList<>();
            AutoCloseable resource = managed.pollLast();
            while (resource != null) {
                try {
                    resource.close();
                } catch (final Exception e) {
                    failures.add(e);
                }
                resource = managed.pollLast();
            }
            try {
                group.shutdown();
            } catch (final RuntimeException e) {
                failures.add(e);
            }
            if (!failures.isEmpty()) {
                throw new InternalException("Unable to close AIO network", failures.get(0));
            }
        }
    }

    /**
     * Composes the network listener with a per-operation listener.
     *
     * @param first  first listener
     * @param second second listener
     * @return safe composed listener
     */
    private static Listener<Object> compose(final Listener<Object> first, final Listener<Object> second) {
        final Listener<Object> left = first == null ? NoopListener.INSTANCE : first;
        if (second == null) {
            return left;
        }
        return safe(new CompositeListener(left, second));
    }

    /**
     * Protects listener callbacks from escaping.
     *
     * @param listener listener
     * @return safe listener
     */
    private static Listener<Object> safe(final Listener<Object> listener) {
        return listener == null ? NoopListener.INSTANCE : new SafeListener(listener);
    }

    /**
     * AIO network connection.
     */
    private static final class AioConnection implements Connection {

        /**
         * Connection destination.
         */
        private final Destination destination;

        /**
         * AIO channel.
         */
        private final AioChannel aio;

        /**
         * Network conduit adapter.
         */
        private final Conduit conduit;

        /**
         * Lifecycle scope.
         */
        private final LifecycleScope scope;

        /**
         * Creates a connection.
         *
         * @param destination destination
         * @param aio         channel
         * @param listener    lifecycle listener
         */
        private AioConnection(final Destination destination, final AioChannel aio, final Listener<Object> listener) {
            this.destination = Assert
                    .notNull(destination, () -> new ValidateException("Connection destination must not be null"));
            this.aio = Assert.notNull(aio, () -> new ValidateException("AIO channel must not be null"));
            this.conduit = new AioConduit(this.aio);
            this.scope = LifecycleScope.session(
                    this,
                    "aio-connection",
                    listener,
                    EventObserver.noop(),
                    ObservationMarker.CONNECT_SUCCESS,
                    null,
                    ObservationMarker.CONNECT_FAILED);
            this.scope.open(this);
        }

        /**
         * Returns the destination.
         *
         * @return destination
         */
        @Override
        public Destination destination() {
            return destination;
        }

        /**
         * Returns the conduit.
         *
         * @return conduit
         */
        @Override
        public Conduit conduit() {
            return conduit;
        }

        /**
         * Returns state.
         *
         * @return state
         */
        @Override
        public Status state() {
            return scope.state();
        }

        /**
         * Returns the protocol-layer source.
         *
         * @return source view
         */
        @Override
        public Source source() {
            return conduit.source();
        }

        /**
         * Returns the protocol-layer sink.
         *
         * @return sink view
         */
        @Override
        public Sink sink() {
            return conduit.sink();
        }

        /**
         * Returns health.
         *
         * @return true when healthy
         */
        @Override
        public boolean healthy() {
            return scope.state() == Status.OPENED && aio.opened();
        }

        /**
         * Returns idle state.
         *
         * @return true when idle
         */
        @Override
        public boolean idle() {
            return false;
        }

        /**
         * Closes the connection.
         */
        @Override
        public void close() {
            if (scope.state().terminal()) {
                return;
            }
            try {
                aio.close();
            } finally {
                scope.close(this);
            }
        }

    }

    /**
     * Network conduit adapter for AIO channels.
     */
    private static final class AioConduit implements Conduit {

        /**
         * AIO channel.
         */
        private final AioChannel aio;

        /**
         * Source view for protocol readers.
         */
        private final Source source;

        /**
         * Sink view for protocol writers.
         */
        private final Sink sink;

        /**
         * Creates an adapter.
         *
         * @param aio channel
         */
        private AioConduit(final AioChannel aio) {
            this.aio = Assert.notNull(aio, () -> new ValidateException("AIO channel must not be null"));
            this.source = new AioSource();
            this.sink = new AioSink();
        }

        /**
         * Reads bytes into a core.io buffer.
         *
         * @param target    target buffer
         * @param byteCount maximum byte count
         * @return read future
         */
        @Override
        public CompletableFuture<Long> read(final Buffer target, final long byteCount) {
            return aio.read(target, byteCount);
        }

        /**
         * Writes bytes from a core.io buffer.
         *
         * @param source    source buffer
         * @param byteCount byte count to write
         * @return write future
         */
        @Override
        public CompletableFuture<Long> write(final Buffer source, final long byteCount) {
            return aio.write(source, byteCount);
        }

        /**
         * Returns the core.io source view.
         *
         * @return source view
         */
        @Override
        public Source source() {
            return source;
        }

        /**
         * Returns the core.io sink view.
         *
         * @return sink view
         */
        @Override
        public Sink sink() {
            return sink;
        }

        /**
         * Returns open state.
         *
         * @return true when open
         */
        @Override
        public boolean opened() {
            return aio.opened();
        }

        /**
         * Closes the adapter.
         */
        @Override
        public void close() {
            aio.close();
        }

        /**
         * Awaits an asynchronous byte-count operation.
         *
         * @param future  operation future
         * @param message failure message
         * @return byte count
         * @throws IOException when the operation fails
         */
        private static long await(final CompletableFuture<Long> future, final String message) throws IOException {
            try {
                return Assert.notNull(future, () -> new ValidateException("IO future must not be null")).get();
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException(message, e);
            } catch (final ExecutionException e) {
                final Throwable cause = e.getCause();
                if (cause instanceof IOException io) {
                    throw io;
                }
                if (cause instanceof RuntimeException runtime) {
                    throw runtime;
                }
                throw new IOException(message, cause);
            }
        }

        /**
         * Source backed by the AIO conduit.
         */
        private final class AioSource implements Source {

            /**
             * Reads bytes through the enclosing conduit.
             *
             * @param sink      target buffer
             * @param byteCount maximum byte count
             * @return read byte count
             * @throws IOException when reading fails
             */
            @Override
            public long read(final Buffer sink, final long byteCount) throws IOException {
                return await(AioConduit.this.read(sink, byteCount), "Unable to read AIO source");
            }

            /**
             * Returns the no-op timeout.
             *
             * @return timeout
             */
            @Override
            public org.miaixz.bus.core.io.timout.Timeout timeout() {
                return org.miaixz.bus.core.io.timout.Timeout.NONE;
            }

            /**
             * Closes the enclosing conduit.
             */
            @Override
            public void close() {
                AioConduit.this.close();
            }

        }

        /**
         * Sink backed by the AIO conduit.
         */
        private final class AioSink implements Sink {

            /**
             * Writes bytes through the enclosing conduit.
             *
             * @param source    source buffer
             * @param byteCount byte count
             * @throws IOException when writing fails
             */
            @Override
            public void write(final Buffer source, final long byteCount) throws IOException {
                await(AioConduit.this.write(source, byteCount), "Unable to write AIO sink");
            }

            /**
             * Flushes the AIO sink.
             */
            @Override
            public void flush() {
                // AIO socket writes are flushed by the operating system.
            }

            /**
             * Returns the no-op timeout.
             *
             * @return timeout
             */
            @Override
            public org.miaixz.bus.core.io.timout.Timeout timeout() {
                return org.miaixz.bus.core.io.timout.Timeout.NONE;
            }

            /**
             * Closes the enclosing conduit.
             */
            @Override
            public void close() {
                AioConduit.this.close();
            }

        }

    }

    /**
     * Composed listener.
     *
     * @param first  first listener
     * @param second second listener
     */
    private record CompositeListener(Listener<Object> first, Listener<Object> second) implements Listener<Object> {

        /**
         * Handles open events.
         *
         * @param source lifecycle source
         */
        @Override
        public void open(final Object source) {
            RuntimeException failure = null;
            try {
                first.open(source);
            } catch (final RuntimeException e) {
                failure = e;
            }
            try {
                second.open(source);
            } catch (final RuntimeException e) {
                failure = failure == null ? e : failure;
            }
            if (failure != null) {
                throw failure;
            }
        }

        /**
         * Handles close events.
         *
         * @param source lifecycle source
         */
        @Override
        public void close(final Object source) {
            RuntimeException failure = null;
            try {
                first.close(source);
            } catch (final RuntimeException e) {
                failure = e;
            }
            try {
                second.close(source);
            } catch (final RuntimeException e) {
                failure = failure == null ? e : failure;
            }
            if (failure != null) {
                throw failure;
            }
        }

        /**
         * Handles failure events.
         *
         * @param source lifecycle source
         * @param cause  failure cause
         */
        @Override
        public void failure(final Object source, final Throwable cause) {
            RuntimeException failure = null;
            try {
                first.failure(source, cause);
            } catch (final RuntimeException e) {
                failure = e;
            }
            try {
                second.failure(source, cause);
            } catch (final RuntimeException e) {
                failure = failure == null ? e : failure;
            }
            if (failure != null) {
                throw failure;
            }
        }

    }

    /**
     * Safe listener wrapper.
     *
     * @param delegate listener delegate
     */
    private record SafeListener(Listener<Object> delegate) implements Listener<Object> {

        /**
         * Handles open events.
         *
         * @param source lifecycle source
         */
        @Override
        public void open(final Object source) {
            try {
                delegate.open(source);
            } catch (final RuntimeException ignored) {
                // Listener failures must not break network lifecycle transitions.
            }
        }

        /**
         * Handles close events.
         *
         * @param source lifecycle source
         */
        @Override
        public void close(final Object source) {
            try {
                delegate.close(source);
            } catch (final RuntimeException ignored) {
                // Listener failures must not break network lifecycle transitions.
            }
        }

        /**
         * Handles failure events.
         *
         * @param source lifecycle source
         * @param cause  failure cause
         */
        @Override
        public void failure(final Object source, final Throwable cause) {
            try {
                delegate.failure(source, cause);
            } catch (final RuntimeException ignored) {
                // Listener failures must not break network lifecycle transitions.
            }
        }

    }

    /**
     * Internal no-operation listener.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    private enum NoopListener implements Listener<Object> {

        /**
         * Singleton no-operation listener.
         */
        INSTANCE

    }

}
