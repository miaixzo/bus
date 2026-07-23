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
package org.miaixz.bus.fabric.protocol.socket.session;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.miaixz.bus.core.io.sink.Sink;
import org.miaixz.bus.core.io.source.Source;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.exception.SocketException;
import org.miaixz.bus.core.lang.exception.TimeoutException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.Builder;
import org.miaixz.bus.fabric.Handler;
import org.miaixz.bus.fabric.Listener;
import org.miaixz.bus.fabric.Payload;
import org.miaixz.bus.fabric.Status;
import org.miaixz.bus.fabric.Timeout;
import org.miaixz.bus.fabric.codec.frame.FrameCodec;
import org.miaixz.bus.fabric.network.Conduit;
import org.miaixz.bus.fabric.network.Connection;
import org.miaixz.bus.fabric.network.Destination;
import org.miaixz.bus.fabric.network.aio.AioNetwork;
import org.miaixz.bus.fabric.network.dns.DnsResolver;
import org.miaixz.bus.fabric.network.tcp.TcpNetwork;
import org.miaixz.bus.fabric.protocol.socket.SocketOptions;
import org.miaixz.bus.fabric.protocol.socket.SocketSession;
import org.miaixz.bus.fabric.protocol.socket.frame.SocketCodec;
import org.miaixz.bus.fabric.registry.connection.ConnectionLease;
import org.miaixz.bus.fabric.registry.connection.ConnectionPool;
import org.miaixz.bus.fabric.runtime.dispatch.Dispatcher;

/**
 * Socket session lease backed by the shared connection pool.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class SocketLease {

    /**
     * Underlying connection-pool lease owned by this wrapper.
     */
    private final ConnectionLease lease;

    /**
     * Socket session created over the leased connection.
     */
    private SocketSession session;

    /**
     * One-shot guard shared by release and close operations.
     */
    private final AtomicBoolean released;

    /**
     * Creates a socket lease.
     *
     * @param lease pooled connection lease owned by this socket lease
     */
    private SocketLease(final ConnectionLease lease) {
        this.lease = require(lease, "Connection lease");
        this.released = new AtomicBoolean();
    }

    /**
     * Acquires a lease.
     *
     * @param pool        connection pool from which a connection is leased
     * @param destination connection destination
     * @return socket lease using default timeout, resolver, framing, and attributes
     */
    public static SocketLease acquire(final ConnectionPool pool, final Destination destination) {
        return acquire(
                pool,
                destination,
                Timeout.of(Builder.TIMEOUT_DEFAULT_NETWORK),
                null,
                DnsResolver.system(),
                FrameCodec.line(),
                null,
                Map.of(),
                null);
    }

    /**
     * Acquires a lease.
     *
     * @param pool            connection pool from which a connection is leased
     * @param destination     connection destination
     * @param timeout         timeout policy
     * @param listener        network lifecycle listener
     * @param resolver        DNS resolver
     * @param frameCodec      frame codec
     * @param handler         message handler
     * @param attributes      session attributes
     * @param sessionListener session lifecycle listener
     * @return socket lease and initialized session over the pooled connection
     */
    public static SocketLease acquire(
            final ConnectionPool pool,
            final Destination destination,
            final Timeout timeout,
            final Listener<Object> listener,
            final DnsResolver resolver,
            final FrameCodec frameCodec,
            final Handler handler,
            final Map<String, Object> attributes,
            final Listener<? super SocketSession> sessionListener) {
        return acquire(
                pool,
                destination,
                timeout,
                listener,
                resolver,
                frameCodec,
                handler,
                attributes,
                sessionListener,
                Builder.DEFAULT_MATERIALIZE_MAX_BYTES);
    }

    /**
     * Acquires a lease.
     *
     * @param pool                connection pool from which a connection is leased
     * @param destination         connection destination
     * @param timeout             timeout policy
     * @param listener            network lifecycle listener
     * @param resolver            DNS resolver
     * @param frameCodec          frame codec
     * @param handler             message handler
     * @param attributes          session attributes
     * @param sessionListener     session lifecycle listener
     * @param materializeMaxBytes materialize byte threshold
     * @return socket lease and initialized session over the pooled connection
     */
    public static SocketLease acquire(
            final ConnectionPool pool,
            final Destination destination,
            final Timeout timeout,
            final Listener<Object> listener,
            final DnsResolver resolver,
            final FrameCodec frameCodec,
            final Handler handler,
            final Map<String, Object> attributes,
            final Listener<? super SocketSession> sessionListener,
            final long materializeMaxBytes) {
        final ConnectionPool currentPool = require(pool, "Connection pool");
        final Destination currentDestination = require(destination, "Connection destination");
        final Timeout currentTimeout = require(timeout, "Timeout");
        final Listener<Object> currentListener = listener;
        final DnsResolver currentResolver = require(resolver, "DNS resolver");
        final FrameCodec currentCodec = require(frameCodec, "Frame codec");
        final SocketOptions socketOptions = SocketOptions.from(currentDestination.options());
        final Map<String, Object> currentAttributes = attributes(socketOptions, attributes);
        final Listener<? super SocketSession> currentSessionListener = sessionListener;
        Payload.validateMaterializeMaxBytes(materializeMaxBytes);
        final ConnectionLease lease = currentPool.acquire(
                currentDestination,
                () -> connect(currentDestination, currentTimeout, currentListener, currentResolver, socketOptions));
        final SocketLease socketLease = new SocketLease(lease);
        try {
            socketLease.session = session(
                    currentDestination,
                    lease.connection(),
                    currentCodec,
                    handler,
                    currentAttributes,
                    socketLease.owner(),
                    currentSessionListener,
                    materializeMaxBytes,
                    socketOptions);
            return socketLease;
        } catch (final RuntimeException e) {
            lease.close();
            throw e;
        }
    }

    /**
     * Acquires a lease with a shared dispatcher.
     *
     * @param pool            connection pool from which a connection is leased
     * @param destination     connection destination
     * @param timeout         timeout policy
     * @param listener        network lifecycle listener
     * @param resolver        DNS resolver
     * @param dispatcher      shared dispatcher
     * @param frameCodec      frame codec
     * @param handler         message handler
     * @param attributes      session attributes
     * @param sessionListener session lifecycle listener
     * @return socket lease using the supplied shared dispatcher
     */
    public static SocketLease acquire(
            final ConnectionPool pool,
            final Destination destination,
            final Timeout timeout,
            final Listener<Object> listener,
            final DnsResolver resolver,
            final Dispatcher dispatcher,
            final FrameCodec frameCodec,
            final Handler handler,
            final Map<String, Object> attributes,
            final Listener<? super SocketSession> sessionListener) {
        return acquire(
                pool,
                destination,
                timeout,
                listener,
                resolver,
                dispatcher,
                frameCodec,
                handler,
                attributes,
                sessionListener,
                Builder.DEFAULT_MATERIALIZE_MAX_BYTES);
    }

    /**
     * Acquires a lease with a shared dispatcher.
     *
     * @param pool                connection pool from which a connection is leased
     * @param destination         connection destination
     * @param timeout             timeout policy
     * @param listener            network lifecycle listener
     * @param resolver            DNS resolver
     * @param dispatcher          shared dispatcher
     * @param frameCodec          frame codec
     * @param handler             message handler
     * @param attributes          session attributes
     * @param sessionListener     session lifecycle listener
     * @param materializeMaxBytes materialize byte threshold
     * @return socket lease and initialized session using the supplied dispatcher
     */
    public static SocketLease acquire(
            final ConnectionPool pool,
            final Destination destination,
            final Timeout timeout,
            final Listener<Object> listener,
            final DnsResolver resolver,
            final Dispatcher dispatcher,
            final FrameCodec frameCodec,
            final Handler handler,
            final Map<String, Object> attributes,
            final Listener<? super SocketSession> sessionListener,
            final long materializeMaxBytes) {
        final ConnectionPool currentPool = require(pool, "Connection pool");
        final Destination currentDestination = require(destination, "Connection destination");
        final Timeout currentTimeout = require(timeout, "Timeout");
        final Listener<Object> currentListener = listener;
        final DnsResolver currentResolver = require(resolver, "DNS resolver");
        final Dispatcher currentDispatcher = require(dispatcher, "Dispatcher");
        final FrameCodec currentCodec = require(frameCodec, "Frame codec");
        final SocketOptions socketOptions = SocketOptions.from(currentDestination.options());
        final Map<String, Object> currentAttributes = attributes(socketOptions, attributes);
        final Listener<? super SocketSession> currentSessionListener = sessionListener;
        Payload.validateMaterializeMaxBytes(materializeMaxBytes);
        final ConnectionLease lease = currentPool.acquire(
                currentDestination,
                () -> connect(
                        currentDestination,
                        currentTimeout,
                        currentListener,
                        currentResolver,
                        currentDispatcher,
                        socketOptions));
        final SocketLease socketLease = new SocketLease(lease);
        try {
            socketLease.session = session(
                    currentDestination,
                    lease.connection(),
                    currentCodec,
                    handler,
                    currentAttributes,
                    socketLease.owner(),
                    currentSessionListener,
                    materializeMaxBytes,
                    socketOptions);
            return socketLease;
        } catch (final RuntimeException e) {
            lease.close();
            throw e;
        }
    }

    /**
     * Returns the session.
     *
     * @return initialized socket session backed by the leased connection
     */
    public SocketSession session() {
        return Assert.notNull(session, () -> new ValidateException("Socket session has not been initialized"));
    }

    /**
     * Releases this lease.
     *
     * @return {@code true} when this call first released the lease to the pool
     */
    public boolean release() {
        if (!released.compareAndSet(false, true)) {
            return false;
        }
        return lease.release();
    }

    /**
     * Closes this lease.
     *
     * @return {@code true} when this call first closed the session and lease
     */
    public boolean close() {
        if (!released.compareAndSet(false, true)) {
            return false;
        }
        if (session != null) {
            session.close();
        }
        lease.close();
        return true;
    }

    /**
     * Returns whether released.
     *
     * @return {@code true} after either release or close has claimed the lease
     */
    public boolean released() {
        return released.get();
    }

    /**
     * Creates an owner handle for a socket session.
     *
     * @return closeable owner handle delegating to this lease
     */
    public Owner owner() {
        return new Owner(this);
    }

    /**
     * Opens a pooled connection.
     *
     * @param destination connection destination
     * @param timeout     connection timeout policy
     * @param listener    lifecycle listener for the opened connection
     * @param resolver    DNS resolver used for host lookup
     * @return connection wrapper that also owns the newly created AIO network
     */
    private static Connection connect(
            final Destination destination,
            final Timeout timeout,
            final Listener<Object> listener,
            final DnsResolver resolver) {
        return connect(destination, timeout, listener, resolver, SocketOptions.from(destination.options()));
    }

    /**
     * Opens a pooled connection.
     *
     * @param destination   connection destination
     * @param timeout       connection timeout policy
     * @param listener      lifecycle listener for the opened network and connection
     * @param resolver      DNS resolver
     * @param socketOptions socket options
     * @return connection wrapper that also owns the newly created AIO network
     */
    private static Connection connect(
            final Destination destination,
            final Timeout timeout,
            final Listener<Object> listener,
            final DnsResolver resolver,
            final SocketOptions socketOptions) {
        final AioNetwork aio = AioNetwork.create(listener, resolver, socketOptions);
        try {
            final Connection connection = TcpNetwork.create(aio).connect(destination.address(), timeout).get(
                    timeout.connect().isZero() ? Long.MAX_VALUE : timeout.connect().toNanos(),
                    TimeUnit.NANOSECONDS);
            return new OwnedConnection(connection, aio);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            aio.close();
            throw new InternalException("Interrupted while acquiring socket lease", e);
        } catch (final java.util.concurrent.TimeoutException e) {
            aio.close();
            throw new TimeoutException("Socket lease connect timed out", e);
        } catch (final java.util.concurrent.ExecutionException e) {
            aio.close();
            throw new SocketException("Unable to acquire socket lease", e.getCause());
        } catch (final RuntimeException e) {
            aio.close();
            throw e;
        }
    }

    /**
     * Opens a pooled connection with a shared dispatcher.
     *
     * @param destination connection destination
     * @param timeout     connection timeout policy
     * @param listener    lifecycle listener for the opened network and connection
     * @param resolver    DNS resolver
     * @param dispatcher  shared dispatcher
     * @return connection wrapper using and owning an AIO network on the shared dispatcher
     */
    private static Connection connect(
            final Destination destination,
            final Timeout timeout,
            final Listener<Object> listener,
            final DnsResolver resolver,
            final Dispatcher dispatcher) {
        return connect(destination, timeout, listener, resolver, dispatcher, SocketOptions.from(destination.options()));
    }

    /**
     * Opens a pooled connection with a shared dispatcher.
     *
     * @param destination   connection destination
     * @param timeout       connection timeout policy
     * @param listener      lifecycle listener for the opened network and connection
     * @param resolver      DNS resolver
     * @param dispatcher    shared dispatcher
     * @param socketOptions socket options
     * @return connection wrapper using and owning an AIO network on the shared dispatcher
     */
    private static Connection connect(
            final Destination destination,
            final Timeout timeout,
            final Listener<Object> listener,
            final DnsResolver resolver,
            final Dispatcher dispatcher,
            final SocketOptions socketOptions) {
        final AioNetwork aio = AioNetwork.create(listener, resolver, dispatcher, socketOptions);
        try {
            final Connection connection = TcpNetwork.create(aio).connect(destination.address(), timeout).get(
                    timeout.connect().isZero() ? Long.MAX_VALUE : timeout.connect().toNanos(),
                    TimeUnit.NANOSECONDS);
            return new OwnedConnection(connection, aio);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            aio.close();
            throw new InternalException("Interrupted while acquiring socket lease", e);
        } catch (final java.util.concurrent.TimeoutException e) {
            aio.close();
            throw new TimeoutException("Socket lease connect timed out", e);
        } catch (final java.util.concurrent.ExecutionException e) {
            aio.close();
            throw new SocketException("Unable to acquire socket lease", e.getCause());
        } catch (final RuntimeException e) {
            aio.close();
            throw e;
        }
    }

    /**
     * Creates a session for a leased connection.
     *
     * @param destination         connection destination
     * @param connection          leased connection used by the session
     * @param frameCodec          codec delimiting socket messages
     * @param handler             inbound message handler
     * @param attributes          initial session attributes
     * @param owner               resource released with the session
     * @param listener            session lifecycle listener
     * @param materializeMaxBytes maximum payload bytes allowed for materialization
     * @return socket session initialized from destination-derived socket options
     */
    private static SocketSession session(
            final Destination destination,
            final Connection connection,
            final FrameCodec frameCodec,
            final Handler handler,
            final Map<String, Object> attributes,
            final AutoCloseable owner,
            final Listener<? super SocketSession> listener,
            final long materializeMaxBytes) {
        return session(
                destination,
                connection,
                frameCodec,
                handler,
                attributes,
                owner,
                listener,
                materializeMaxBytes,
                SocketOptions.from(destination.options()));
    }

    /**
     * Creates a session for a leased connection.
     *
     * @param destination         connection destination
     * @param connection          leased connection used by the session
     * @param frameCodec          frame codec
     * @param handler             inbound message handler
     * @param attributes          immutable initial session attributes
     * @param owner               resource released when the session terminates
     * @param listener            session lifecycle listener
     * @param materializeMaxBytes maximum payload bytes allowed for materialization
     * @param socketOptions       session socket tuning options
     * @return initialized socket session over the leased connection
     */
    private static SocketSession session(
            final Destination destination,
            final Connection connection,
            final FrameCodec frameCodec,
            final Handler handler,
            final Map<String, Object> attributes,
            final AutoCloseable owner,
            final Listener<? super SocketSession> listener,
            final long materializeMaxBytes,
            final SocketOptions socketOptions) {
        return SocketSession.create(
                destination.address(),
                connection,
                SocketCodec.of(frameCodec),
                handler,
                attributes,
                owner,
                listener,
                materializeMaxBytes,
                socketOptions);
    }

    /**
     * Adds socket options to session attributes.
     *
     * @param socketOptions socket options
     * @param source        source attributes
     * @return immutable attribute map containing socket options when not already supplied
     */
    private static Map<String, Object> attributes(final SocketOptions socketOptions, final Map<String, Object> source) {
        final java.util.LinkedHashMap<String, Object> values = new java.util.LinkedHashMap<>(
                source == null ? Map.of() : source);
        values.putIfAbsent(Builder.ATTRIBUTE_SOCKET_OPTIONS, socketOptions);
        return Map.copyOf(values);
    }

    /**
     * Validates required values.
     *
     * @param value reference to validate
     * @param name  field name included in the validation failure
     * @param <T>   reference type
     * @return validated non-null reference
     */
    private static <T> T require(final T value, final String name) {
        return Assert.notNull(value, () -> new ValidateException(name + " must not be null"));
    }

    /**
     * Auto-closeable owner handle used by socket sessions.
     */
    public static final class Owner implements AutoCloseable {

        /**
         * Socket lease controlled by this owner handle.
         */
        private final SocketLease lease;

        /**
         * Creates an owner handle.
         *
         * @param lease socket lease controlled by the handle
         */
        private Owner(final SocketLease lease) {
            this.lease = require(lease, "Socket lease");
        }

        /**
         * Releases the lease back to the pool.
         */
        public void release() {
            lease.release();
        }

        /**
         * Closes the lease and its connection.
         */
        @Override
        public void close() {
            lease.close();
        }

    }

    /**
     * Connection wrapper that owns the AIO network created for a pooled socket.
     */
    private static final class OwnedConnection implements Connection {

        /**
         * Delegated connection.
         */
        private final Connection delegate;

        /**
         * Owned AIO network.
         */
        private final AioNetwork owner;

        /**
         * Close flag.
         */
        private final AtomicBoolean closed;

        /**
         * Creates an owned connection.
         *
         * @param delegate delegated connection
         * @param owner    owning AIO network
         */
        private OwnedConnection(final Connection delegate, final AioNetwork owner) {
            this.delegate = Assert
                    .notNull(delegate, () -> new ValidateException("Owned connection delegate must not be null"));
            this.owner = Assert
                    .notNull(owner, () -> new ValidateException("Owned connection network must not be null"));
            this.closed = new AtomicBoolean();
        }

        /**
         * Returns the delegated connection destination.
         *
         * @return destination of the delegated pooled connection
         */
        @Override
        public Destination destination() {
            return delegate.destination();
        }

        /**
         * Returns the delegated connection conduit.
         *
         * @return conduit of the delegated pooled connection
         */
        @Override
        public Conduit conduit() {
            return delegate.conduit();
        }

        /**
         * Returns the delegated connection state.
         *
         * @return lifecycle state of the delegated pooled connection
         */
        @Override
        public Status state() {
            return delegate.state();
        }

        /**
         * Returns the delegated source view.
         *
         * @return source view
         */
        @Override
        public Source source() {
            return delegate.source();
        }

        /**
         * Returns the delegated sink view.
         *
         * @return sink view
         */
        @Override
        public Sink sink() {
            return delegate.sink();
        }

        /**
         * Returns whether this owned connection remains healthy.
         *
         * @return true when healthy
         */
        @Override
        public boolean healthy() {
            return !closed.get() && delegate.healthy();
        }

        /**
         * Returns whether the delegated connection is idle.
         *
         * @return true when idle
         */
        @Override
        public boolean idle() {
            return delegate.idle();
        }

        /**
         * Closes the delegated connection and its owning AIO network once.
         */
        @Override
        public void close() {
            if (closed.compareAndSet(false, true)) {
                RuntimeException failure = null;
                try {
                    delegate.close();
                } catch (final RuntimeException e) {
                    failure = e;
                }
                try {
                    owner.close();
                } catch (final RuntimeException e) {
                    if (failure == null) {
                        failure = e;
                    }
                }
                if (failure != null) {
                    throw failure;
                }
            }
        }

    }

}
