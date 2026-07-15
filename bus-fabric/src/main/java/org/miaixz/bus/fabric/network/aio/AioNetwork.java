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

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;

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
import org.miaixz.bus.fabric.Wiring;
import org.miaixz.bus.fabric.network.Conduit;
import org.miaixz.bus.fabric.network.Connection;
import org.miaixz.bus.fabric.network.Destination;
import org.miaixz.bus.fabric.network.dns.DnsResolver;
import org.miaixz.bus.fabric.network.dns.DnsResult;
import org.miaixz.bus.fabric.network.tcp.TcpServer;
import org.miaixz.bus.fabric.protocol.socket.SocketOptions;
import org.miaixz.bus.fabric.runtime.dispatch.Dispatcher;

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
        this.listener = Wiring.safe(listener == null ? Wiring.noop() : listener, null);
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
            group = AioGroup.create(current.threadNum());
            return new AioNetwork(group, AioProvider.system(), DnsResolver.system(), Wiring.noop(), current);
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
            group = AioGroup.create(current.threadNum());
            return new AioNetwork(group, AioProvider.system(), DnsResolver.system(),
                    listener == null ? Wiring.noop() : listener, current);
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
            group = AioGroup.create(current.threadNum());
            return new AioNetwork(group, AioProvider.system(),
                    Assert.notNull(resolver, () -> new ValidateException("DNS resolver must not be null")),
                    listener == null ? Wiring.noop() : listener, current);
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
                    current.threadNum(),
                    Assert.notNull(dispatcher, () -> new ValidateException("Dispatcher must not be null")));
            return new AioNetwork(group, AioProvider.system(),
                    Assert.notNull(resolver, () -> new ValidateException("DNS resolver must not be null")),
                    listener == null ? Wiring.noop() : listener, current);
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
        return connect(address, timeout, Wiring.noop());
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
        final Listener<Object> current = Wiring.safe(Wiring.compose(this.listener, listener), null);
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
            listener.open(connection);
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
        return server(address, handler, Wiring.noop());
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
                .openServer(checkedAddress, group, Wiring.compose(this.listener, listener), socketOptions);
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
         * Lifecycle listener.
         */
        private final Listener<Object> listener;

        /**
         * Lifecycle state.
         */
        private volatile Status state = Status.OPENED;

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
            this.listener = Wiring.safe(listener, null);
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
            return state;
        }

        /**
         * Reads bytes.
         *
         * @param buffer target buffer
         * @return read future
         */
        @Override
        public CompletableFuture<Integer> read(final ByteBuffer buffer) {
            return aio.read(buffer);
        }

        /**
         * Writes bytes.
         *
         * @param buffer source buffer
         * @return write future
         */
        @Override
        public CompletableFuture<Integer> write(final ByteBuffer buffer) {
            return aio.write(buffer);
        }

        /**
         * Returns health.
         *
         * @return true when healthy
         */
        @Override
        public boolean healthy() {
            return state == Status.OPENED && aio.opened();
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
            if (state != Status.CLOSED) {
                aio.close();
                state = Status.CLOSED;
                listener.close(this);
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
         * Creates an adapter.
         *
         * @param aio channel
         */
        private AioConduit(final AioChannel aio) {
            this.aio = Assert.notNull(aio, () -> new ValidateException("AIO channel must not be null"));
        }

        /**
         * Reads bytes.
         *
         * @param target target buffer
         * @return read future
         */
        @Override
        public CompletableFuture<Integer> read(final ByteBuffer target) {
            return aio.read(target);
        }

        /**
         * Reads bytes with a handler.
         *
         * @param target  target buffer
         * @param handler completion handler
         */
        @Override
        public void read(final ByteBuffer target, final CompletionHandler<Integer, ByteBuffer> handler) {
            aio.read(target, handler);
        }

        /**
         * Writes bytes.
         *
         * @param source source buffer
         * @return write future
         */
        @Override
        public CompletableFuture<Integer> write(final ByteBuffer source) {
            return aio.write(source);
        }

        /**
         * Writes bytes with a handler.
         *
         * @param source  source buffer
         * @param handler completion handler
         */
        @Override
        public void write(final ByteBuffer source, final CompletionHandler<Integer, ByteBuffer> handler) {
            aio.write(source, handler);
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

    }

}
