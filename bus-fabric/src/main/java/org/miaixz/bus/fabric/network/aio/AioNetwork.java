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
import java.util.concurrent.atomic.AtomicInteger;

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
import org.miaixz.bus.fabric.Builder;
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
import org.miaixz.bus.fabric.runtime.Activity;
import org.miaixz.bus.fabric.runtime.dispatch.DispatchHandle;
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
     * Worker group owned and shut down by this network.
     */
    private final AioGroup group;

    /**
     * Provider used to open native client and server channels.
     */
    private final AioProvider provider;

    /**
     * Open servers and channels awaiting reverse-order cleanup.
     */
    private final ConcurrentLinkedDeque<AutoCloseable> managed;

    /**
     * Resolver used to obtain candidate addresses for client connections.
     */
    private final DnsResolver resolver;

    /**
     * Atomic guard that makes network shutdown idempotent.
     */
    private final AtomicBoolean closed;

    /**
     * Failure-safe listener applied to every network operation.
     */
    private final Listener<Object> listener;

    /**
     * Socket options applied to newly opened channels and servers.
     */
    private final SocketOptions socketOptions;

    /**
     * Creates a network.
     *
     * @param group    worker group owned by the network
     * @param provider provider used to open AIO resources
     * @param resolver resolver used for client host names
     * @param listener optional network-wide lifecycle listener
     */
    private AioNetwork(final AioGroup group, final AioProvider provider, final DnsResolver resolver,
            final Listener<Object> listener) {
        this(group, provider, resolver, listener, SocketOptions.defaults());
    }

    /**
     * Creates a network.
     *
     * @param group         worker group owned by the network
     * @param provider      provider used to open AIO resources
     * @param resolver      resolver used for client host names
     * @param listener      optional network-wide lifecycle listener
     * @param socketOptions options applied to newly opened sockets, or {@code null} for defaults
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
     * @return a network backed by a newly created worker group and system provider
     * @throws InternalException if the worker group or network cannot be created
     */
    public static AioNetwork create() {
        return create(SocketOptions.defaults());
    }

    /**
     * Creates a default AIO network.
     *
     * @param socketOptions options controlling worker count and socket behavior, or {@code null} for defaults
     * @return a network backed by a newly created worker group and system provider
     * @throws InternalException if the worker group or network cannot be created
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
     * @param listener optional listener applied to all connections and servers
     * @return a network backed by a newly created worker group and system provider
     * @throws InternalException if the worker group or network cannot be created
     */
    public static AioNetwork create(final Listener<Object> listener) {
        return create(listener, SocketOptions.defaults());
    }

    /**
     * Creates a default AIO network with a lifecycle listener.
     *
     * @param listener      optional listener applied to all connections and servers
     * @param socketOptions options controlling worker count and socket behavior, or {@code null} for defaults
     * @return a network backed by a newly created worker group and system provider
     * @throws InternalException if the worker group or network cannot be created
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
     * @param listener optional listener applied to all connections and servers
     * @param resolver non-null resolver used for client host names
     * @return a network backed by a newly created worker group and system provider
     * @throws InternalException if the worker group or network cannot be created
     */
    public static AioNetwork create(final Listener<Object> listener, final DnsResolver resolver) {
        return create(listener, resolver, SocketOptions.defaults());
    }

    /**
     * Creates a default AIO network with lifecycle listener and DNS resolver.
     *
     * @param listener      optional listener applied to all connections and servers
     * @param resolver      non-null resolver used for client host names
     * @param socketOptions options controlling worker count and socket behavior, or {@code null} for defaults
     * @return a network backed by a newly created worker group and system provider
     * @throws InternalException if the worker group or network cannot be created
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
     * @param listener   optional listener applied to all connections and servers
     * @param resolver   non-null resolver used for client host names
     * @param dispatcher non-null dispatcher shared by the new worker group
     * @return a network backed by a newly created worker group and system provider
     * @throws InternalException if the worker group or network cannot be created
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
     * @param listener      optional listener applied to all connections and servers
     * @param resolver      non-null resolver used for client host names
     * @param dispatcher    non-null dispatcher shared by the new worker group
     * @param socketOptions options controlling worker count and socket behavior, or {@code null} for defaults
     * @return a network backed by a newly created worker group and system provider
     * @throws InternalException if the worker group or network cannot be created
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
     * @return worker group owned by this network
     */
    public AioGroup group() {
        return group;
    }

    /**
     * Returns socket options.
     *
     * @return socket options used for newly opened resources
     */
    public SocketOptions socketOptions() {
        return socketOptions;
    }

    /**
     * Opens a client connection.
     *
     * @param address logical remote address to resolve and connect
     * @param timeout timeout policy applied to each native connection attempt
     * @return future completed with the first successful connection or the aggregate connection failure
     * @throws ValidateException if the address or timeout policy is {@code null}
     */
    public CompletableFuture<Connection> connect(final Address address, final Timeout timeout) {
        return connect(address, timeout, null);
    }

    /**
     * Opens a client connection with an additional lifecycle listener.
     *
     * @param address  logical remote address to resolve and connect
     * @param timeout  timeout policy applied to each native connection attempt
     * @param listener optional listener composed with the network-wide listener
     * @return future completed with the first successful connection or the aggregate connection failure
     * @throws ValidateException if the address or timeout policy is {@code null}
     */
    public CompletableFuture<Connection> connect(
            final Address address,
            final Timeout timeout,
            final Listener<Object> listener) {
        final Address checkedAddress = Assert.notNull(address, () -> new ValidateException("Address must not be null"));
        final Timeout checkedTimeout = Assert.notNull(timeout, () -> new ValidateException("Timeout must not be null"));
        if (closed.get()) {
            return CompletableFuture.failedFuture(new SocketException("AIO network is closed"));
        }
        final Listener<Object> current = compose(this.listener, listener);
        final DnsResult result = resolver.resolve(checkedAddress.host());
        if (result.empty()) {
            return CompletableFuture
                    .failedFuture(new SocketException("Unable to resolve host " + checkedAddress.host()));
        }
        final ConnectRace race = new ConnectRace(checkedAddress, checkedTimeout, current, result.addresses());
        race.startPair(Normal._0, null);
        return race.result;
    }

    /**
     * Creates a TCP server.
     *
     * @param address local address on which the server listens
     * @param handler handler that receives accepted sessions and messages
     * @return opened and managed TCP server
     * @throws ValidateException if the address or handler is {@code null}
     */
    public TcpServer server(final Address address, final Handler handler) {
        return server(address, handler, null);
    }

    /**
     * Creates a TCP server with an additional lifecycle listener.
     *
     * @param address  local address on which the server listens
     * @param handler  handler that receives accepted sessions and messages
     * @param listener optional listener composed with the network-wide listener
     * @return opened and managed TCP server
     * @throws ValidateException if the address or handler is {@code null}
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
     * Closes managed resources in reverse registration order and then shuts down the worker group.
     *
     * @throws InternalException if any managed resource or the worker group cannot be closed
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
     * @param first  network-wide listener, or {@code null}
     * @param second per-operation listener, or {@code null}
     * @return failure-safe listener that invokes the configured delegates in order
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
     * @param listener listener to wrap, or {@code null}
     * @return no-op listener for {@code null}, otherwise a wrapper that suppresses callback failures
     */
    private static Listener<Object> safe(final Listener<Object> listener) {
        return listener == null ? NoopListener.INSTANCE : new SafeListener(listener);
    }

    /**
     * Runs a bounded, deterministic Happy Eyeballs race. At most two channels are pending at once; later address pairs
     * are considered only after the current pair has failed.
     */
    private final class ConnectRace {

        /**
         * Logical destination whose resolved addresses are being raced.
         */
        private final Address address;

        /**
         * Connection timeout policy applied independently to each channel attempt.
         */
        private final Timeout timeout;

        /**
         * Listener attached to the winning connection.
         */
        private final Listener<Object> listener;

        /**
         * Stable resolver-order address snapshot partitioned into pairs.
         */
        private final List<InetAddress> addresses;

        /**
         * Future completed by the first successful connection or the terminal aggregate failure.
         */
        private final CompletableFuture<Connection> result = new CompletableFuture<>();

        /**
         * Attempts that still own an open channel or pending connect operation.
         */
        private final ConcurrentLinkedDeque<Attempt> attempts = new ConcurrentLinkedDeque<>();

        /**
         * Ensures that exactly one winner, failure, or cancellation becomes terminal.
         */
        private final AtomicBoolean terminal = new AtomicBoolean();

        /**
         * Delayed second attempt for the current pair, or {@code null} when none is scheduled.
         */
        private volatile DispatchHandle delayed;

        /**
         * Creates a deterministic connection race over a resolved address snapshot.
         *
         * @param address   logical destination
         * @param timeout   connection timeout policy
         * @param listener  listener attached to the winning connection
         * @param addresses resolved addresses in stable resolver order
         */
        private ConnectRace(final Address address, final Timeout timeout, final Listener<Object> listener,
                final List<InetAddress> addresses) {
            this.address = address;
            this.timeout = timeout;
            this.listener = listener;
            this.addresses = List.copyOf(addresses);
            result.whenComplete((connection, cause) -> {
                if (result.isCancelled()) {
                    terminal.set(true);
                    cancelRemaining(null);
                }
            });
        }

        /**
         * Starts the next pair and schedules its second address after the Happy Eyeballs delay.
         *
         * @param index           first address index in the pair
         * @param previousFailure failure accumulated from earlier pairs, or {@code null}
         */
        private void startPair(final int index, final Throwable previousFailure) {
            if (terminal.get()) {
                return;
            }
            if (index >= addresses.size()) {
                fail(previousFailure);
                return;
            }
            final int count = Math.min(Normal._2, addresses.size() - index);
            final AtomicInteger remaining = new AtomicInteger(count);
            final Throwable[] failures = new Throwable[] { previousFailure };
            startAttempt(index, index + count, remaining, failures);
            if (count == Normal._2 && !terminal.get()) {
                delayed = group.dispatcher().schedule(
                        "aio-happy-eyeballs",
                        Builder.HAPPY_EYEBALLS_DELAY,
                        Activity.of(
                                "aio-happy-eyeballs",
                                () -> startAttempt(index + Normal._1, index + count, remaining, failures)));
            }
        }

        /**
         * Opens and starts one native channel connection attempt.
         *
         * @param index     address index to connect
         * @param nextPair  first index of the next pair
         * @param remaining attempts in the current pair that have not failed
         * @param failures  synchronized single-slot aggregate failure holder
         */
        private void startAttempt(
                final int index,
                final int nextPair,
                final AtomicInteger remaining,
                final Throwable[] failures) {
            if (terminal.get()) {
                return;
            }
            final AioChannel channel;
            try {
                channel = provider.openChannel(group, socketOptions);
            } catch (final RuntimeException failure) {
                attemptFailed(nextPair, remaining, failures, failure);
                return;
            }
            managed.add(channel);
            final InetSocketAddress socket = new InetSocketAddress(addresses.get(index), address.port());
            final CompletableFuture<Void> operation = channel.connect(socket, timeout);
            final Attempt attempt = new Attempt(channel, operation);
            attempts.add(attempt);
            operation.whenComplete((ignored, cause) -> {
                if (cause != null) {
                    closeAttempt(attempt);
                    attemptFailed(nextPair, remaining, failures, cause);
                    return;
                }
                final Connection connection = new AioConnection(
                        Destination.of(address.protocol(), address, socketOptions.toOptions()), channel, listener);
                if (terminal.compareAndSet(false, true)) {
                    attempts.remove(attempt);
                    cancelRemaining(channel);
                    result.complete(connection);
                } else {
                    closeAttempt(attempt);
                }
            });
        }

        /**
         * Aggregates an attempt failure and advances after every attempt in the pair has failed.
         *
         * @param nextPair  first index of the next address pair
         * @param remaining attempts in the current pair that have not failed
         * @param failures  synchronized aggregate failure holder
         * @param cause     current attempt failure
         */
        private void attemptFailed(
                final int nextPair,
                final AtomicInteger remaining,
                final Throwable[] failures,
                final Throwable cause) {
            synchronized (failures) {
                final Throwable current = ExceptionKit.unwrap(cause);
                if (failures[0] != null && failures[0] != current) {
                    current.addSuppressed(ExceptionKit.unwrap(failures[0]));
                }
                failures[0] = current;
            }
            if (remaining.decrementAndGet() == Normal._0 && !terminal.get()) {
                final DispatchHandle pending = delayed;
                if (pending != null) {
                    group.dispatcher().cancel(pending);
                    delayed = null;
                }
                startPair(nextPair, failures[0]);
            }
        }

        /**
         * Publishes the terminal aggregate failure and closes all remaining attempts.
         *
         * @param failure final connection failure
         */
        private void fail(final Throwable failure) {
            if (!terminal.compareAndSet(false, true)) {
                return;
            }
            final Throwable root = ExceptionKit.unwrap(failure);
            final RuntimeException terminalFailure;
            if (root instanceof TimeoutException timedOut) {
                terminalFailure = timedOut;
            } else {
                terminalFailure = new SocketException("Unable to connect to resolved host " + address.host(), root);
            }
            listener.failure(AioNetwork.this, terminalFailure);
            cancelRemaining(null);
            result.completeExceptionally(terminalFailure);
        }

        /**
         * Cancels every pending attempt except the channel selected as the winner.
         *
         * @param winner winning channel, or {@code null} when no attempt succeeded
         */
        private void cancelRemaining(final AioChannel winner) {
            final DispatchHandle pending = delayed;
            if (pending != null) {
                group.dispatcher().cancel(pending);
                delayed = null;
            }
            Attempt attempt = attempts.poll();
            while (attempt != null) {
                if (attempt.channel != winner) {
                    attempt.operation.cancel(true);
                    managed.remove(attempt.channel);
                    IoKit.closeQuietly(attempt.channel);
                }
                attempt = attempts.poll();
            }
        }

        /**
         * Removes and closes one failed or losing attempt.
         *
         * @param attempt attempt to close
         */
        private void closeAttempt(final Attempt attempt) {
            attempts.remove(attempt);
            managed.remove(attempt.channel);
            IoKit.closeQuietly(attempt.channel);
        }
    }

    /**
     * Pending native connect operation and the channel it exclusively owns.
     *
     * @param channel   channel opened for this attempt
     * @param operation asynchronous connect completion
     */
    private record Attempt(AioChannel channel, CompletableFuture<Void> operation) {
    }

    /**
     * AIO network connection.
     */
    private static final class AioConnection implements Connection {

        /**
         * Immutable logical and transport destination of the connection.
         */
        private final Destination destination;

        /**
         * Native AIO channel owned by the connection.
         */
        private final AioChannel aio;

        /**
         * Protocol-facing conduit backed by the native channel.
         */
        private final Conduit conduit;

        /**
         * Lifecycle state and listener notification scope.
         */
        private final LifecycleScope scope;

        /**
         * Creates a connection.
         *
         * @param destination logical and transport destination metadata
         * @param aio         connected native channel owned by the connection
         * @param listener    failure-safe lifecycle listener
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
         * @return immutable destination metadata for this connection
         */
        @Override
        public Destination destination() {
            return destination;
        }

        /**
         * Returns the conduit.
         *
         * @return conduit that reads from and writes to the native channel
         */
        @Override
        public Conduit conduit() {
            return conduit;
        }

        /**
         * Returns state.
         *
         * @return current lifecycle state
         */
        @Override
        public Status state() {
            return scope.state();
        }

        /**
         * Returns the protocol-layer source.
         *
         * @return protocol-facing source backed by the connection conduit
         */
        @Override
        public Source source() {
            return conduit.source();
        }

        /**
         * Returns the protocol-layer sink.
         *
         * @return protocol-facing sink backed by the connection conduit
         */
        @Override
        public Sink sink() {
            return conduit.sink();
        }

        /**
         * Returns health.
         *
         * @return {@code true} when the lifecycle is open and the native channel remains open
         */
        @Override
        public boolean healthy() {
            return scope.state() == Status.OPENED && aio.opened();
        }

        /**
         * Returns idle state.
         *
         * @return {@code false}, because this adapter does not track connection idleness
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
         * Native channel adapted by this conduit.
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
         * @param aio non-null native channel to adapt
         */
        private AioConduit(final AioChannel aio) {
            this.aio = Assert.notNull(aio, () -> new ValidateException("AIO channel must not be null"));
            this.source = new AioSource();
            this.sink = new AioSink();
        }

        /**
         * Reads bytes into a core.io buffer.
         *
         * @param target    buffer that receives bytes from the channel
         * @param byteCount maximum number of bytes to append
         * @return future containing the validated read count, including {@code -1} at end of stream
         */
        @Override
        public CompletableFuture<Long> read(final Buffer target, final long byteCount) {
            if (target == null) {
                return CompletableFuture.failedFuture(new ValidateException("Read target must not be null"));
            }
            if (byteCount < Normal._0) {
                return CompletableFuture.failedFuture(new ValidateException("Read byte count must not be negative"));
            }
            final long before = target.size();
            final CompletableFuture<Long> operation;
            try {
                operation = aio.read(target, byteCount);
            } catch (final RuntimeException e) {
                return CompletableFuture.failedFuture(e);
            }
            if (operation == null) {
                return CompletableFuture.failedFuture(new InternalException("AIO read returned a null future"));
            }
            return operation.thenApply(count -> validateReadResult(count, byteCount, before, target.size()));
        }

        /**
         * Writes bytes from a core.io buffer.
         *
         * @param source    buffer whose leading bytes are consumed by the write
         * @param byteCount exact number of bytes to write
         * @return future containing the validated number of consumed bytes
         */
        @Override
        public CompletableFuture<Long> write(final Buffer source, final long byteCount) {
            if (source == null) {
                return CompletableFuture.failedFuture(new ValidateException("Write source must not be null"));
            }
            if (byteCount < Normal._0 || byteCount > source.size()) {
                return CompletableFuture
                        .failedFuture(new ValidateException("Write byte count must be between zero and source size"));
            }
            final long before = source.size();
            final CompletableFuture<Long> operation;
            try {
                operation = aio.write(source, byteCount);
            } catch (final RuntimeException e) {
                return CompletableFuture.failedFuture(e);
            }
            if (operation == null) {
                return CompletableFuture.failedFuture(new InternalException("AIO write returned a null future"));
            }
            return operation.thenApply(count -> validateWriteResult(count, byteCount, before, source.size()));
        }

        /**
         * Validates one completed channel read.
         *
         * @param count     byte count reported by the channel, or {@code null} for an invalid completion
         * @param requested maximum byte count requested from the channel
         * @param before    target buffer size before the read
         * @param after     target buffer size after the read
         * @return validated count, including {@code -1} for end of stream
         */
        private static long validateReadResult(
                final Long count,
                final long requested,
                final long before,
                final long after) {
            if (count == null) {
                throw new InternalException("AIO read returned a null byte count");
            }
            if (count < Normal.__1 || count > requested) {
                throw new InternalException("AIO read returned an invalid byte count: " + count);
            }
            if (requested == Normal._0 && count != Normal._0) {
                throw new InternalException("AIO zero-byte read returned a nonzero result: " + count);
            }
            final long appended = after - before;
            if ((count == Normal.__1 && appended != Normal._0) || (count >= Normal._0 && appended != count)) {
                throw new InternalException("AIO read count did not match appended bytes");
            }
            return count;
        }

        /**
         * Validates one completed channel write.
         *
         * @param count     byte count reported by the channel, or {@code null} for an invalid completion
         * @param requested exact byte count requested from the channel
         * @param before    source buffer size before the write
         * @param after     source buffer size after the write
         * @return validated count equal to the requested byte count
         */
        private static long validateWriteResult(
                final Long count,
                final long requested,
                final long before,
                final long after) {
            if (count == null) {
                throw new InternalException("AIO write returned a null byte count");
            }
            if (count < Normal._0 || count > requested) {
                throw new InternalException("AIO write returned an invalid byte count: " + count);
            }
            if (count != requested || before - after != requested) {
                throw new InternalException("AIO write did not fully consume requested bytes");
            }
            return count;
        }

        /**
         * Returns the core.io source view.
         *
         * @return reusable protocol-facing source view
         */
        @Override
        public Source source() {
            return source;
        }

        /**
         * Returns the core.io sink view.
         *
         * @return reusable protocol-facing sink view
         */
        @Override
        public Sink sink() {
            return sink;
        }

        /**
         * Returns open state.
         *
         * @return {@code true} while the underlying native channel is open
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
         * @param future  non-null asynchronous byte-count operation
         * @param message message used when an interruption or checked failure is converted to an I/O exception
         * @return non-null byte count produced by the operation
         * @throws IOException if waiting is interrupted or the operation fails with a checked cause
         */
        private static long await(final CompletableFuture<Long> future, final String message) throws IOException {
            try {
                final Long count = Assert.notNull(future, () -> new ValidateException("IO future must not be null"))
                        .get();
                if (count == null) {
                    throw new InternalException("AIO operation returned a null byte count");
                }
                return count;
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
                if (cause instanceof Error error) {
                    throw error;
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
             * @param sink      buffer that receives bytes from the native channel
             * @param byteCount maximum number of bytes to append
             * @return positive number of bytes read, {@code 0} for a zero-byte request, or {@code -1} at end of stream
             * @throws IOException if the asynchronous read cannot be completed
             */
            @Override
            public long read(final Buffer sink, final long byteCount) throws IOException {
                final long count = await(AioConduit.this.read(sink, byteCount), "Unable to read AIO source");
                if (count == Normal._0 && byteCount != Normal._0) {
                    throw new InternalException("AIO source returned zero for a positive read request");
                }
                return count;
            }

            /**
             * Returns the no-op timeout.
             *
             * @return shared timeout instance that imposes no source deadline
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
             * @param source    buffer whose leading bytes are written and consumed
             * @param byteCount exact number of bytes to write
             * @throws IOException if the asynchronous write cannot be completed
             */
            @Override
            public void write(final Buffer source, final long byteCount) throws IOException {
                final long before = source == null ? Normal._0 : source.size();
                final long count = await(AioConduit.this.write(source, byteCount), "Unable to write AIO sink");
                if (count != byteCount || before - source.size() != byteCount) {
                    throw new InternalException("AIO sink did not fully consume requested bytes");
                }
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
             * @return shared timeout instance that imposes no sink deadline
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
     * Ordered pair of lifecycle listeners invoked for each event.
     *
     * @param first  listener invoked first
     * @param second listener invoked after the first, even if the first fails
     */
    private record CompositeListener(Listener<Object> first, Listener<Object> second) implements Listener<Object> {

        /**
         * Handles open events.
         *
         * @param source object whose lifecycle opened
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
         * @param source object whose lifecycle closed
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
         * @param source object whose lifecycle failed
         * @param cause  failure reported by the source
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
     * Listener wrapper that prevents delegate failures from escaping network lifecycle transitions.
     *
     * @param delegate listener whose runtime failures are suppressed
     */
    private record SafeListener(Listener<Object> delegate) implements Listener<Object> {

        /**
         * Handles open events.
         *
         * @param source object whose lifecycle opened
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
         * @param source object whose lifecycle closed
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
         * @param source object whose lifecycle failed
         * @param cause  failure reported by the source
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
