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

import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.exception.SocketException;
import org.miaixz.bus.core.lang.exception.TimeoutException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.Handler;
import org.miaixz.bus.fabric.Listener;
import org.miaixz.bus.fabric.Options;
import org.miaixz.bus.fabric.Payload;
import org.miaixz.bus.fabric.Status;
import org.miaixz.bus.fabric.Timeout;
import org.miaixz.bus.fabric.Wiring;
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
     * Pool lease.
     */
    private final ConnectionLease lease;

    /**
     * Session.
     */
    private SocketSession session;

    /**
     * Released flag.
     */
    private final AtomicBoolean released;

    /**
     * Creates a socket lease.
     *
     * @param lease   lease
     * @param session session
     */
    private SocketLease(final ConnectionLease lease) {
        if (lease == null) {
            throw new ValidateException("Connection lease must not be null");
        }
        this.lease = lease;
        this.released = new AtomicBoolean();
    }

    /**
     * Acquires a lease.
     *
     * @param pool        pool
     * @param destination connection destination
     * @return socket lease
     */
    public static SocketLease acquire(final ConnectionPool pool, final Destination destination) {
        return acquire(
                pool,
                destination,
                Timeout.of(Duration.ofSeconds(10)),
                Wiring.noop(),
                DnsResolver.system(),
                FrameCodec.line(),
                null,
                Map.of(),
                Wiring.noop());
    }

    /**
     * Acquires a lease.
     *
     * @param pool            pool
     * @param destination     connection destination
     * @param timeout         timeout policy
     * @param listener        network lifecycle listener
     * @param resolver        DNS resolver
     * @param frameCodec      frame codec
     * @param handler         message handler
     * @param attributes      session attributes
     * @param sessionListener session lifecycle listener
     * @return socket lease
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
                Options.DEFAULT_MATERIALIZE_MAX_BYTES);
    }

    /**
     * Acquires a lease.
     *
     * @param pool                pool
     * @param destination         connection destination
     * @param timeout             timeout policy
     * @param listener            network lifecycle listener
     * @param resolver            DNS resolver
     * @param frameCodec          frame codec
     * @param handler             message handler
     * @param attributes          session attributes
     * @param sessionListener     session lifecycle listener
     * @param materializeMaxBytes materialize byte threshold
     * @return socket lease
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
        if (pool == null) {
            throw new ValidateException("Connection pool must not be null");
        }
        if (destination == null) {
            throw new ValidateException("Connection destination must not be null");
        }
        final Timeout currentTimeout = require(timeout, "Timeout");
        final Listener<Object> currentListener = Wiring.safe(listener == null ? Wiring.noop() : listener, null);
        final DnsResolver currentResolver = require(resolver, "DNS resolver");
        final FrameCodec currentCodec = require(frameCodec, "Frame codec");
        final SocketOptions socketOptions = SocketOptions.from(destination.options());
        final Map<String, Object> currentAttributes = attributes(socketOptions, attributes);
        final Listener<? super SocketSession> currentSessionListener = sessionListener == null ? Wiring.noop()
                : sessionListener;
        Payload.validateMaterializeMaxBytes(materializeMaxBytes);
        final ConnectionLease lease = pool.acquire(
                destination,
                () -> connect(destination, currentTimeout, currentListener, currentResolver, socketOptions));
        final SocketLease socketLease = new SocketLease(lease);
        try {
            socketLease.session = session(
                    destination,
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
     * @param pool            pool
     * @param destination     connection destination
     * @param timeout         timeout policy
     * @param listener        network lifecycle listener
     * @param resolver        DNS resolver
     * @param dispatcher      shared dispatcher
     * @param frameCodec      frame codec
     * @param handler         message handler
     * @param attributes      session attributes
     * @param sessionListener session lifecycle listener
     * @return socket lease
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
                Options.DEFAULT_MATERIALIZE_MAX_BYTES);
    }

    /**
     * Acquires a lease with a shared dispatcher.
     *
     * @param pool                pool
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
     * @return socket lease
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
        if (pool == null) {
            throw new ValidateException("Connection pool must not be null");
        }
        if (destination == null) {
            throw new ValidateException("Connection destination must not be null");
        }
        final Timeout currentTimeout = require(timeout, "Timeout");
        final Listener<Object> currentListener = Wiring.safe(listener == null ? Wiring.noop() : listener, null);
        final DnsResolver currentResolver = require(resolver, "DNS resolver");
        final Dispatcher currentDispatcher = require(dispatcher, "Dispatcher");
        final FrameCodec currentCodec = require(frameCodec, "Frame codec");
        final SocketOptions socketOptions = SocketOptions.from(destination.options());
        final Map<String, Object> currentAttributes = attributes(socketOptions, attributes);
        final Listener<? super SocketSession> currentSessionListener = sessionListener == null ? Wiring.noop()
                : sessionListener;
        Payload.validateMaterializeMaxBytes(materializeMaxBytes);
        final ConnectionLease lease = pool.acquire(
                destination,
                () -> connect(
                        destination,
                        currentTimeout,
                        currentListener,
                        currentResolver,
                        currentDispatcher,
                        socketOptions));
        final SocketLease socketLease = new SocketLease(lease);
        try {
            socketLease.session = session(
                    destination,
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
     * @return session
     */
    public SocketSession session() {
        if (session == null) {
            throw new ValidateException("Socket session has not been initialized");
        }
        return session;
    }

    /**
     * Releases this lease.
     *
     * @return true when released
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
     * @return true when closed
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
     * @return true when released
     */
    public boolean released() {
        return released.get();
    }

    /**
     * Creates an owner handle for a socket session.
     *
     * @return owner handle
     */
    public Owner owner() {
        return new Owner(this);
    }

    /**
     * Opens a pooled connection.
     *
     * @param destination connection destination
     * @return connection
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
     * @param timeout       timeout
     * @param listener      listener
     * @param resolver      DNS resolver
     * @param socketOptions socket options
     * @return connection
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
     * @param timeout     timeout
     * @param listener    listener
     * @param resolver    DNS resolver
     * @param dispatcher  shared dispatcher
     * @return connection
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
     * @param timeout       timeout
     * @param listener      listener
     * @param resolver      DNS resolver
     * @param dispatcher    shared dispatcher
     * @param socketOptions socket options
     * @return connection
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
     * @param destination connection destination
     * @param connection  connection
     * @return session
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
     * @param connection          connection
     * @param frameCodec          frame codec
     * @param handler             handler
     * @param attributes          attributes
     * @param owner               owner
     * @param listener            listener
     * @param materializeMaxBytes materialize max bytes
     * @param socketOptions       socket options
     * @return session
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
     * @return attributes
     */
    private static Map<String, Object> attributes(final SocketOptions socketOptions, final Map<String, Object> source) {
        final java.util.LinkedHashMap<String, Object> values = new java.util.LinkedHashMap<>(
                source == null ? Map.of() : source);
        values.putIfAbsent(SocketSession.ATTRIBUTE_SOCKET_OPTIONS, socketOptions);
        return Map.copyOf(values);
    }

    /**
     * Validates required values.
     *
     * @param value value
     * @param name  name
     * @param <T>   type
     * @return value
     */
    private static <T> T require(final T value, final String name) {
        if (value == null) {
            throw new ValidateException(name + " must not be null");
        }
        return value;
    }

    /**
     * Auto-closeable owner handle used by socket sessions.
     */
    public static final class Owner implements AutoCloseable {

        /**
         * Lease.
         */
        private final SocketLease lease;

        /**
         * Creates an owner handle.
         *
         * @param lease lease
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
            if (delegate == null) {
                throw new ValidateException("Owned connection delegate must not be null");
            }
            if (owner == null) {
                throw new ValidateException("Owned connection network must not be null");
            }
            this.delegate = delegate;
            this.owner = owner;
            this.closed = new AtomicBoolean();
        }

        @Override
        public Destination destination() {
            return delegate.destination();
        }

        @Override
        public Conduit conduit() {
            return delegate.conduit();
        }

        @Override
        public Status state() {
            return delegate.state();
        }

        @Override
        public CompletableFuture<Integer> read(final ByteBuffer buffer) {
            return delegate.read(buffer);
        }

        @Override
        public CompletableFuture<Integer> write(final ByteBuffer buffer) {
            return delegate.write(buffer);
        }

        @Override
        public boolean healthy() {
            return !closed.get() && delegate.healthy();
        }

        @Override
        public boolean idle() {
            return delegate.idle();
        }

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
