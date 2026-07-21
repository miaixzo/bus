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
package org.miaixz.bus.fabric.protocol.socket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;

import org.miaixz.bus.core.io.sink.Sink;
import org.miaixz.bus.core.io.source.Source;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.SocketException;
import org.miaixz.bus.core.lang.exception.StatefulException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.core.xyz.ThreadKit;
import org.miaixz.bus.fabric.Address;
import org.miaixz.bus.fabric.Context;
import org.miaixz.bus.fabric.Filter;
import org.miaixz.bus.fabric.Handler;
import org.miaixz.bus.fabric.Headers;
import org.miaixz.bus.fabric.Lifecycle;
import org.miaixz.bus.fabric.Listener;
import org.miaixz.bus.fabric.Message;
import org.miaixz.bus.fabric.Payload;
import org.miaixz.bus.fabric.Status;
import org.miaixz.bus.fabric.Timeout;
import org.miaixz.bus.fabric.codec.frame.FrameCodec;
import org.miaixz.bus.fabric.codec.frame.LineCodec;
import org.miaixz.bus.fabric.guard.GuardRule;
import org.miaixz.bus.fabric.network.Conduit;
import org.miaixz.bus.fabric.network.Connection;
import org.miaixz.bus.fabric.network.Destination;
import org.miaixz.bus.fabric.network.Ingress;
import org.miaixz.bus.fabric.network.proxy.ProxyHeader;
import org.miaixz.bus.fabric.network.proxy.ProxyHeaderReader;
import org.miaixz.bus.fabric.network.tls.TlsChannel;
import org.miaixz.bus.fabric.network.tls.TlsEngine;
import org.miaixz.bus.fabric.network.tls.TlsSettings;
import org.miaixz.bus.fabric.network.tls.context.TlsContext;
import org.miaixz.bus.fabric.observe.EventObserver;
import org.miaixz.bus.fabric.observe.ObservationMarker;
import org.miaixz.bus.fabric.protocol.Demuxer;
import org.miaixz.bus.fabric.protocol.socket.frame.SocketCodec;
import org.miaixz.bus.fabric.runtime.Activity;
import org.miaixz.bus.fabric.runtime.FilterChain;
import org.miaixz.bus.fabric.runtime.dispatch.DispatchHandle;
import org.miaixz.bus.fabric.runtime.dispatch.Dispatcher;
import org.miaixz.bus.fabric.runtime.lifecycle.LifecycleScope;
import org.miaixz.bus.fabric.runtime.resource.Cancellation;

/**
 * Socket server listener that accepts TCP sessions through the shared fabric runtime.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class SocketServer implements Lifecycle {

    /**
     * Shared context.
     */
    private final Context context;

    /**
     * Bind address.
     */
    private final Address address;

    /**
     * Socket options.
     */
    private final SocketOptions socketOptions;

    /**
     * Unified timeout policy for accepted sessions, TLS, and server shutdown.
     */
    private final Timeout timeout;

    /**
     * Optional server TLS context.
     */
    private final TlsContext tlsContext;

    /**
     * Optional server TLS settings paired with {@link #tlsContext}.
     */
    private final TlsSettings tlsSettings;

    /**
     * Frame codec.
     */
    private final FrameCodec frameCodec;

    /**
     * Message handler.
     */
    private final Handler handler;

    /**
     * Guard rule.
     */
    private final GuardRule guard;

    /**
     * Message filter.
     */
    private final Filter filter;

    /**
     * Event observer.
     */
    private final EventObserver observer;

    /**
     * Server lifecycle listener.
     */
    private final Listener<? super SocketServer> listener;

    /**
     * Accepted session lifecycle listener.
     */
    private final Listener<? super SocketSession> sessionListener;

    /**
     * Runtime dispatcher.
     */
    private final Dispatcher dispatcher;

    /**
     * Server lifecycle coordination lock.
     */
    private final Object lifecycleLock;

    /**
     * One-shot start guard.
     */
    private final AtomicBoolean started;

    /**
     * Terminal shutdown guard.
     */
    private final AtomicBoolean shuttingDown;

    /**
     * Listening server channel.
     */
    private volatile ServerSocketChannel serverChannel;

    /**
     * Background accept-loop handle.
     */
    private volatile DispatchHandle acceptHandle;

    /**
     * Lifecycle scope.
     */
    private final LifecycleScope lifecycle;

    /**
     * Accepted sessions.
     */
    private final Queue<SocketSession> sessions;

    /**
     * Accepted transports, including handshakes that have not produced sessions yet.
     */
    private final Queue<AcceptedConnection> connections;

    /**
     * Raw channels still being inspected for an optional PROXY header.
     */
    private final Queue<SocketChannel> acceptedChannels;

    /**
     * Accepted dispatch handles.
     */
    private final Queue<DispatchHandle> handles;

    /**
     * Creates a socket server from a builder.
     *
     * @param builder builder
     */
    private SocketServer(final Builder builder) {
        this.context = require(builder.context, "Context");
        this.address = require(builder.address, "Socket bind address");
        this.socketOptions = builder.socketOptions == null ? SocketOptions.defaults() : builder.socketOptions;
        this.timeout = require(builder.timeout, "Socket server timeout");
        this.tlsContext = builder.tlsContext;
        this.tlsSettings = builder.tlsSettings;
        validateTlsPair(this.tlsContext, this.tlsSettings);
        this.frameCodec = builder.frameCodec == null ? FrameCodec.line() : builder.frameCodec;
        this.handler = builder.handler();
        this.guard = builder.guard;
        this.filter = builder.filter;
        this.observer = EventObserver.safe(builder.observer);
        this.listener = builder.listener;
        this.sessionListener = builder.sessionListener();
        this.dispatcher = context.reactor().dispatcher();
        this.lifecycle = LifecycleScope.resource(this, "socket-server", listener, observer);
        this.lifecycleLock = new Object();
        this.started = new AtomicBoolean();
        this.shuttingDown = new AtomicBoolean();
        this.sessions = new ConcurrentLinkedQueue<>();
        this.connections = new ConcurrentLinkedQueue<>();
        this.acceptedChannels = new ConcurrentLinkedQueue<>();
        this.handles = new ConcurrentLinkedQueue<>();
    }

    /**
     * Creates a builder.
     *
     * @param context shared context
     * @return builder
     */
    public static Builder builder(final Context context) {
        return new Builder(require(context, "Context"));
    }

    /**
     * Starts this server.
     *
     * @return this server
     */
    public SocketServer start() {
        synchronized (lifecycleLock) {
            if (running()) {
                return this;
            }
            if (shuttingDown.get() || lifecycle.state().terminal()) {
                throw new StatefulException("Socket server is already closed");
            }
            if (!started.compareAndSet(false, true)) {
                throw new StatefulException("Socket server can only be started once");
            }
            ServerSocketChannel opened = null;
            try {
                opened = ServerSocketChannel.open();
                opened.bind(new InetSocketAddress(address.host(), address.port()), socketOptions.backlog());
                serverChannel = opened;
                lifecycle.open(this);
                acceptHandle = track(
                        dispatcher.background(
                                org.miaixz.bus.fabric.Builder.SOCKET_ACTIVITY_ACCEPT,
                                this,
                                Activity.of(org.miaixz.bus.fabric.Builder.SOCKET_ACTIVITY_ACCEPT, this::acceptLoop)));
                return this;
            } catch (final IOException e) {
                closeServerChannel(opened);
                serverChannel = null;
                final SocketException failure = new SocketException("Unable to start socket server", e);
                lifecycle.fail(failure);
                throw failure;
            } catch (final RuntimeException e) {
                closeServerChannel(opened);
                serverChannel = null;
                lifecycle.fail(e);
                throw e;
            }
        }
    }

    /**
     * Executes this server by starting it.
     *
     * @return this server
     */
    public SocketServer execute() {
        return start();
    }

    /**
     * Closes this server.
     *
     * @return true when lifecycle changed
     */
    public boolean close() {
        return shutdown(true);
    }

    /**
     * Cancels this server.
     *
     * @return true when lifecycle changed
     */
    public boolean cancel() {
        return shutdown(false);
    }

    /**
     * Returns lifecycle state.
     *
     * @return state
     */
    @Override
    public Status state() {
        return lifecycle.state();
    }

    /**
     * Returns whether this server is running.
     *
     * @return true when running
     */
    @Override
    public boolean running() {
        final ServerSocketChannel current = serverChannel;
        return !shuttingDown.get() && lifecycle.state() == Status.OPENED && current != null && current.isOpen();
    }

    /**
     * Returns bind address.
     *
     * @return address
     */
    public Address address() {
        return address;
    }

    /**
     * Returns server attributes.
     *
     * @return immutable attributes
     */
    public Map<String, Object> attributes() {
        return Map.of(
                org.miaixz.bus.fabric.Builder.ATTRIBUTE_OBSERVER,
                observer,
                org.miaixz.bus.fabric.Builder.ATTRIBUTE_SOCKET_OPTIONS,
                socketOptions);
    }

    /**
     * Accepts raw channels until shutdown closes the listening channel.
     */
    private void acceptLoop() {
        try {
            while (!shuttingDown.get()) {
                final ServerSocketChannel current = serverChannel;
                if (current == null || !current.isOpen()) {
                    return;
                }
                final SocketChannel channel = current.accept();
                if (shuttingDown.get()) {
                    closeAcceptedChannel(channel);
                    return;
                }
                acceptedChannels.add(channel);
                try {
                    track(
                            dispatcher.background(
                                    org.miaixz.bus.fabric.Builder.SOCKET_ACTIVITY_ACCEPT,
                                    channel,
                                    Activity.of(
                                            org.miaixz.bus.fabric.Builder.SOCKET_ACTIVITY_ACCEPT,
                                            () -> handleAccepted(channel))));
                } catch (final RuntimeException e) {
                    acceptedChannels.remove(channel);
                    closeAcceptedChannel(channel);
                    throw e;
                }
            }
        } catch (final IOException e) {
            if (!shuttingDown.get()) {
                failServer(new SocketException("Socket server accept failed", e));
            }
        } catch (final RuntimeException e) {
            if (!shuttingDown.get()) {
                failServer(e);
            }
        }
    }

    /**
     * Converts one accepted channel into an ingress-backed socket session.
     *
     * @param channel accepted channel
     */
    private void handleAccepted(final SocketChannel channel) {
        AcceptedConnection connection = null;
        SocketSession session = null;
        boolean transferred = false;
        try {
            if (shuttingDown.get()) {
                return;
            }
            final ProxyHeaderReader.Result proxy = ProxyHeaderReader.read(channel);
            if (shuttingDown.get()) {
                return;
            }
            final Address peerAddress = peerAddress(channel, proxy.header());
            final Ingress ingress = Ingress.of(peerAddress, channel, proxy.payload());
            connection = new AcceptedConnection(channel, ingress);
            connections.add(connection);
            acceptedChannels.remove(channel);
            if (shuttingDown.get()) {
                connection.close();
                return;
            }
            if (tlsContext != null) {
                final TlsChannel tls = TlsChannel.wrap(
                        ingress,
                        TlsEngine.createServer(tlsContext, peerAddress, tlsSettings),
                        context.listener(),
                        dispatcher,
                        timeout);
                connection.secure(tls);
                await(tls.handshake(), "Socket server TLS handshake failed");
            }
            if (shuttingDown.get()) {
                connection.close();
                return;
            }
            Message opening = Message.of(
                    peerAddress.protocol(),
                    peerAddress,
                    Headers.empty(),
                    Payload.empty(),
                    org.miaixz.bus.fabric.Builder.SOCKET_TAG_OPEN);
            opening = FilterChain.apply(opening, context.filter(), filter);
            if (guard != null) {
                guard.check(opening).throwIfRejected();
            }
            final Filter sessionFilter = FilterChain.compose(context.filter(), filter);
            final Map<String, Object> attributes = attributes(proxy.header(), sessionFilter);
            session = new SocketSession(peerAddress, connection, null, null, SocketCodec.of(frameCodec), Demuxer.noop(),
                    attributes, null, registryListener(connection), context.options().materializeMaxBytes(),
                    socketOptions, dispatcher, context.clock(), timeout, Cancellation.create());
            if (shuttingDown.get()) {
                session.close();
                return;
            }
            startReader(session);
            transferred = true;
            connection = null;
            session = null;
        } catch (final RuntimeException e) {
            if (session != null) {
                session.cancel();
            }
            if (!shuttingDown.get()) {
                notifySetupFailure(e);
            }
        } finally {
            acceptedChannels.remove(channel);
            if (!transferred && connection != null) {
                connections.remove(connection);
                closeConnection(connection);
            } else if (!transferred) {
                closeAcceptedChannel(channel);
            }
        }
    }

    /**
     * Starts one long-lived background reader for an accepted session.
     *
     * @param session session
     */
    private void startReader(final SocketSession session) {
        track(
                dispatcher.background(
                        org.miaixz.bus.fabric.Builder.SOCKET_ACTIVITY_READ,
                        session,
                        Activity.of(org.miaixz.bus.fabric.Builder.SOCKET_ACTIVITY_READ, () -> readLoop(session))));
    }

    /**
     * Reads and dispatches messages until the session reaches a terminal state.
     *
     * @param session session
     */
    private void readLoop(final SocketSession session) {
        try {
            while (!shuttingDown.get() && session.opened()) {
                final Message message = session.receive().execute();
                handler.message(session, message);
            }
        } catch (final RuntimeException e) {
            if (session.opened()) {
                session.cancel();
            }
            if (!shuttingDown.get()) {
                notifyHandlerFailure(session, e);
            }
        }
    }

    /**
     * Creates a registry-owning listener that removes resources before forwarding callbacks.
     *
     * @param connection accepted connection
     * @return listener
     */
    private Listener<SocketSession> registryListener(final AcceptedConnection connection) {
        return new Listener<>() {

            /**
             * Registers the opened session before forwarding the lifecycle callback.
             */
            @Override
            public void open(final SocketSession source) {
                sessions.add(source);
                notifySessionOpen(source);
            }

            /**
             * Removes terminal ownership before forwarding the normal-close callback.
             */
            @Override
            public void close(final SocketSession source) {
                remove(source);
                notifySessionClose(source);
            }

            /**
             * Removes terminal ownership before forwarding the failure callback.
             */
            @Override
            public void failure(final SocketSession source, final Throwable cause) {
                remove(source);
                notifySessionFailure(source, cause);
            }

            /**
             * Removes the session and transport registries before any user callback.
             *
             * @param source terminal session
             */
            private void remove(final SocketSession source) {
                sessions.remove(source);
                connections.remove(connection);
            }
        };
    }

    /**
     * Tracks a dispatch handle until completion.
     *
     * @param handle dispatch handle
     * @return tracked handle
     */
    private DispatchHandle track(final DispatchHandle handle) {
        handles.add(handle);
        handle.future().whenComplete((ignored, cause) -> handles.remove(handle));
        return handle;
    }

    /**
     * Cancels accepted dispatch handles.
     */
    private void cancelHandles() {
        DispatchHandle handle = handles.poll();
        while (handle != null) {
            handle.cancel();
            handle = handles.poll();
        }
    }

    /**
     * Stops accepting, terminates sessions, and releases every accepted transport.
     *
     * @param graceful true to allow sessions the configured close interval
     * @return true when the server lifecycle changed
     */
    private boolean shutdown(final boolean graceful) {
        if (!shuttingDown.compareAndSet(false, true)) {
            return false;
        }
        lifecycle.closing();
        RuntimeException failure = stopAccept(null);
        failure = terminateSessions(graceful, failure);
        if (graceful) {
            awaitGracefulClose();
        }
        failure = forceClose(failure);
        if (graceful) {
            if (failure == null) {
                return lifecycle.close(this);
            }
            lifecycle.fail(failure);
            throw failure;
        }
        final boolean changed = lifecycle.cancel();
        if (failure != null) {
            lifecycle.emit(ObservationMarker.SOCKET_FAILED, failure);
        }
        return changed;
    }

    /**
     * Stops the accept loop by closing the listening channel before cancelling its handle.
     *
     * @param failure current cleanup failure
     * @return aggregated cleanup failure
     */
    private RuntimeException stopAccept(final RuntimeException failure) {
        RuntimeException currentFailure = failure;
        final ServerSocketChannel current = serverChannel;
        serverChannel = null;
        if (current != null) {
            try {
                current.close();
            } catch (final IOException e) {
                currentFailure = append(currentFailure, new SocketException("Unable to close socket server", e));
            }
        }
        final DispatchHandle currentHandle = acceptHandle;
        acceptHandle = null;
        if (currentHandle != null) {
            try {
                currentHandle.cancel();
            } catch (final RuntimeException e) {
                currentFailure = append(currentFailure, e);
            }
        }
        return currentFailure;
    }

    /**
     * Requests normal close or cancellation for every registered session.
     *
     * @param graceful true to close normally
     * @param failure  current cleanup failure
     * @return aggregated cleanup failure
     */
    private RuntimeException terminateSessions(final boolean graceful, final RuntimeException failure) {
        RuntimeException currentFailure = failure;
        for (final SocketSession session : new ArrayList<>(sessions)) {
            try {
                if (graceful) {
                    session.close();
                } else {
                    session.cancel();
                }
            } catch (final RuntimeException e) {
                currentFailure = append(currentFailure, e);
            }
        }
        return currentFailure;
    }

    /**
     * Waits until all accepted resources close or the unified close timeout expires.
     */
    private void awaitGracefulClose() {
        final long startedAt = context.clock().nanos();
        final long limit = durationNanos(timeout.close());
        while (hasAcceptedResources() && elapsed(context.clock().nanos(), startedAt) < limit) {
            if (!ThreadKit.sleep(Normal._1)) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    /**
     * Returns whether accepted sessions or transports still own lower-level resources.
     *
     * @return true when resources remain
     */
    private boolean hasAcceptedResources() {
        return !sessions.isEmpty() || !connections.isEmpty() || !acceptedChannels.isEmpty();
    }

    /**
     * Cancels remaining work and force-closes lower-level resources after the grace interval.
     *
     * @param failure current cleanup failure
     * @return aggregated cleanup failure
     */
    private RuntimeException forceClose(final RuntimeException failure) {
        RuntimeException currentFailure = failure;
        cancelHandles();
        currentFailure = terminateSessions(false, currentFailure);
        for (final AcceptedConnection connection : new ArrayList<>(connections)) {
            try {
                connection.close();
            } catch (final RuntimeException e) {
                currentFailure = append(currentFailure, e);
            }
        }
        for (final SocketChannel channel : new ArrayList<>(acceptedChannels)) {
            try {
                channel.close();
            } catch (final IOException e) {
                currentFailure = append(currentFailure, new SocketException("Unable to close accepted channel", e));
            }
        }
        sessions.clear();
        connections.clear();
        acceptedChannels.clear();
        return currentFailure;
    }

    /**
     * Fails the server and releases all resources after an accept-loop failure.
     *
     * @param cause accept-loop failure
     */
    private void failServer(final RuntimeException cause) {
        if (!shuttingDown.compareAndSet(false, true)) {
            return;
        }
        RuntimeException failure = stopAccept(cause);
        failure = terminateSessions(false, failure);
        failure = forceClose(failure);
        lifecycle.fail(failure);
    }

    /**
     * Waits for one TLS setup future and preserves its runtime cause.
     *
     * @param future  setup future
     * @param message checked-failure message
     * @param <T>     result type
     * @return completed result
     */
    private static <T> T await(final CompletableFuture<T> future, final String message) {
        try {
            return require(future, "Setup future").join();
        } catch (final CompletionException e) {
            final Throwable cause = e.getCause();
            if (cause instanceof RuntimeException runtime) {
                throw runtime;
            }
            throw new InternalException(message, cause == null ? e : cause);
        }
    }

    /**
     * Closes one accepted connection without destabilizing its setup activity.
     *
     * @param connection accepted connection
     */
    private void closeConnection(final AcceptedConnection connection) {
        try {
            connection.close();
        } catch (final RuntimeException e) {
            lifecycle.emit(ObservationMarker.SOCKET_FAILED, e);
        }
    }

    /**
     * Closes one raw accepted channel without destabilizing the accept loop.
     *
     * @param channel accepted channel
     */
    private void closeAcceptedChannel(final SocketChannel channel) {
        if (channel == null) {
            return;
        }
        try {
            channel.close();
        } catch (final IOException e) {
            lifecycle.emit(ObservationMarker.SOCKET_FAILED, new SocketException("Unable to close accepted channel", e));
        }
    }

    /**
     * Quietly closes a listening channel that failed during startup.
     *
     * @param channel listening channel
     */
    private static void closeServerChannel(final ServerSocketChannel channel) {
        if (channel == null) {
            return;
        }
        try {
            channel.close();
        } catch (final IOException ignored) {
            // The original startup failure remains authoritative.
        }
    }

    /**
     * Converts a duration to a saturated nanosecond interval.
     *
     * @param duration duration
     * @return nanoseconds
     */
    private static long durationNanos(final Duration duration) {
        try {
            return duration.toNanos();
        } catch (final ArithmeticException e) {
            return Long.MAX_VALUE;
        }
    }

    /**
     * Computes non-negative elapsed nanoseconds with wrap-safe subtraction.
     *
     * @param now       current monotonic time
     * @param startedAt start time
     * @return elapsed nanoseconds
     */
    private static long elapsed(final long now, final long startedAt) {
        final long value = now - startedAt;
        return value < Normal.LONG_ZERO ? Long.MAX_VALUE : value;
    }

    /**
     * Aggregates cleanup failures using suppressed causes.
     *
     * @param failure current failure
     * @param next    next failure
     * @return primary failure
     */
    private static RuntimeException append(final RuntimeException failure, final RuntimeException next) {
        if (failure == null) {
            return next;
        }
        if (failure != next) {
            failure.addSuppressed(next);
        }
        return failure;
    }

    /**
     * Builds session attributes.
     *
     * @param proxyHeader   parsed PROXY header
     * @param sessionFilter session filter
     * @return attributes
     */
    private Map<String, Object> attributes(final ProxyHeader proxyHeader, final Filter sessionFilter) {
        final java.util.LinkedHashMap<String, Object> values = new java.util.LinkedHashMap<>();
        values.put(org.miaixz.bus.fabric.Builder.ATTRIBUTE_OBSERVER, observer);
        values.put(org.miaixz.bus.fabric.Builder.ATTRIBUTE_SOCKET_OPTIONS, socketOptions);
        if (sessionFilter != null) {
            values.put(org.miaixz.bus.fabric.Builder.ATTRIBUTE_FILTER, sessionFilter);
        }
        if (guard != null) {
            values.put(org.miaixz.bus.fabric.Builder.ATTRIBUTE_GUARD, guard);
        }
        if (proxyHeader != null) {
            values.put(org.miaixz.bus.fabric.Builder.ATTRIBUTE_PROXY_HEADER, proxyHeader);
        }
        return Map.copyOf(values);
    }

    /**
     * Resolves peer address.
     *
     * @param channel     channel
     * @param proxyHeader parsed PROXY header
     * @return peer address
     */
    private Address peerAddress(final SocketChannel channel, final ProxyHeader proxyHeader) {
        if (proxyHeader != null && proxyHeader.sourceAddress() != null) {
            return proxyHeader.sourceAddress();
        }
        try {
            final SocketAddress remote = channel.getRemoteAddress();
            if (!(remote instanceof InetSocketAddress socket)) {
                throw new ProtocolException("Accepted socket remote address must be InetSocketAddress");
            }
            return new Address(address.scheme(), socket.getHostString(), socket.getPort(), Symbol.SLASH);
        } catch (final java.io.IOException e) {
            throw new ProtocolException("Unable to resolve accepted socket peer address", e);
        }
    }

    /**
     * Notifies the user listener after a session enters the registry.
     *
     * @param session session
     */
    private void notifySessionOpen(final SocketSession session) {
        try {
            sessionListener.open(session);
        } catch (final RuntimeException e) {
            lifecycle.emit(ObservationMarker.LISTENER_FAILED, e);
        }
    }

    /**
     * Notifies the user listener after a session leaves the registry normally.
     *
     * @param session session
     */
    private void notifySessionClose(final SocketSession session) {
        try {
            sessionListener.close(session);
        } catch (final RuntimeException e) {
            lifecycle.emit(ObservationMarker.LISTENER_FAILED, e);
        }
    }

    /**
     * Notifies the user listener after a failed session leaves the registry.
     *
     * @param session session
     * @param cause   failure cause
     */
    private void notifySessionFailure(final SocketSession session, final Throwable cause) {
        try {
            sessionListener.failure(session, cause);
        } catch (final RuntimeException e) {
            lifecycle.emit(ObservationMarker.LISTENER_FAILED, e);
        }
    }

    /**
     * Reports a setup failure for a connection that never became a session.
     *
     * @param cause setup failure
     */
    private void notifySetupFailure(final Throwable cause) {
        lifecycle.emit(ObservationMarker.SOCKET_FAILED, cause);
        try {
            sessionListener.failure(null, cause);
        } catch (final RuntimeException e) {
            lifecycle.emit(ObservationMarker.LISTENER_FAILED, e);
        }
    }

    /**
     * Reports a reader or user-handler failure without changing server state.
     *
     * @param session session
     * @param cause   failure cause
     */
    private void notifyHandlerFailure(final SocketSession session, final Throwable cause) {
        try {
            handler.failure(session, cause);
        } catch (final RuntimeException e) {
            lifecycle.emit(ObservationMarker.LISTENER_FAILED, e);
        }
    }

    /**
     * Validates that server TLS context and settings are configured as one pair.
     *
     * @param context  TLS context
     * @param settings TLS settings
     */
    private static void validateTlsPair(final TlsContext context, final TlsSettings settings) {
        if ((context == null) != (settings == null)) {
            throw new ValidateException("TLS context and settings must be configured together");
        }
    }

    /**
     * Validates a required value.
     *
     * @param value value
     * @param name  name
     * @param <T>   value type
     * @return value
     */
    private static <T> T require(final T value, final String name) {
        return Assert.notNull(value, () -> new ValidateException(name + " must not be null"));
    }

    /**
     * Connection view that owns one accepted channel, its ingress, and an optional TLS boundary.
     */
    private static final class AcceptedConnection implements Connection {

        /**
         * Raw accepted channel closed last during teardown.
         */
        private final SocketChannel accepted;

        /**
         * Ingress that preserves bytes prefetched while parsing the optional PROXY header.
         */
        private final Ingress ingress;

        /**
         * Idempotent close guard.
         */
        private final AtomicBoolean closed;

        /**
         * Optional TLS conduit installed before the handshake starts.
         */
        private volatile TlsChannel tls;

        /**
         * Creates an accepted plain connection.
         *
         * @param accepted raw accepted channel
         * @param ingress  ingress over the same channel
         */
        private AcceptedConnection(final SocketChannel accepted, final Ingress ingress) {
            this.accepted = require(accepted, "Accepted channel");
            this.ingress = require(ingress, "Accepted ingress");
            this.closed = new AtomicBoolean();
        }

        /**
         * Installs the TLS conduit exactly once before its handshake begins.
         *
         * @param channel TLS conduit
         */
        private synchronized void secure(final TlsChannel channel) {
            final TlsChannel current = require(channel, "Accepted TLS channel");
            if (tls != null) {
                current.close();
                throw new StatefulException("Accepted connection already has TLS");
            }
            if (closed.get()) {
                current.close();
                throw new StatefulException("Accepted connection is closed");
            }
            tls = current;
        }

        /**
         * Returns the ingress destination.
         *
         * @return destination
         */
        @Override
        public Destination destination() {
            return ingress.destination();
        }

        /**
         * Returns the TLS plain-text boundary when configured, otherwise the raw ingress.
         *
         * @return conduit
         */
        @Override
        public Conduit conduit() {
            final TlsChannel current = tls;
            return current == null ? ingress : current;
        }

        /**
         * Returns the active protocol-layer source.
         *
         * @return source
         */
        @Override
        public Source source() {
            return conduit().source();
        }

        /**
         * Returns the active protocol-layer sink.
         *
         * @return sink
         */
        @Override
        public Sink sink() {
            return conduit().sink();
        }

        /**
         * Returns the accepted connection state.
         *
         * @return state
         */
        @Override
        public Status state() {
            if (closed.get()) {
                return Status.CLOSED;
            }
            final TlsChannel current = tls;
            return current == null ? ingress.state() : current.state();
        }

        /**
         * Returns whether every active layer remains open.
         *
         * @return true when healthy
         */
        @Override
        public boolean healthy() {
            if (closed.get() || !accepted.isOpen() || !ingress.healthy()) {
                return false;
            }
            final TlsChannel current = tls;
            return current == null || current.opened();
        }

        /**
         * Returns whether this dedicated accepted connection is idle and reusable by its session.
         *
         * @return true when healthy
         */
        @Override
        public boolean idle() {
            return healthy();
        }

        /**
         * Closes TLS, ingress, and the accepted channel in that order exactly once.
         */
        @Override
        public void close() {
            if (!closed.compareAndSet(false, true)) {
                return;
            }
            RuntimeException failure = null;
            final TlsChannel current = tls;
            if (current != null) {
                try {
                    current.close();
                } catch (final RuntimeException e) {
                    failure = e;
                }
            }
            try {
                ingress.close();
            } catch (final RuntimeException e) {
                failure = append(failure, e);
            }
            try {
                accepted.close();
            } catch (final IOException e) {
                failure = append(failure, new SocketException("Unable to close accepted channel", e));
            }
            if (failure != null) {
                throw failure;
            }
        }

    }

    /**
     * Builder for socket servers.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public static final class Builder {

        /**
         * Shared context.
         */
        private final Context context;

        /**
         * Bind address.
         */
        private Address address;

        /**
         * Socket options.
         */
        private SocketOptions socketOptions;

        /**
         * Unified timeout policy.
         */
        private Timeout timeout;

        /**
         * Optional server TLS context.
         */
        private TlsContext tlsContext;

        /**
         * Optional server TLS settings.
         */
        private TlsSettings tlsSettings;

        /**
         * Frame codec.
         */
        private FrameCodec frameCodec;

        /**
         * Message handler.
         */
        private Handler handler;

        /**
         * Demuxer builder.
         */
        private Demuxer.Builder demuxer;

        /**
         * Guard rule.
         */
        private GuardRule guard;

        /**
         * Message filter.
         */
        private Filter filter;

        /**
         * Observer.
         */
        private EventObserver observer;

        /**
         * Server lifecycle listener.
         */
        private Listener<? super SocketServer> listener;

        /**
         * Session listener.
         */
        private Listener<? super SocketSession> sessionListener;

        /**
         * Open handler.
         */
        private Consumer<SocketSession> openHandler;

        /**
         * Error handler.
         */
        private Consumer<Throwable> errorHandler;

        /**
         * Creates a builder.
         *
         * @param context shared context
         */
        private Builder(final Context context) {
            this.context = context;
            this.socketOptions = SocketOptions.defaults();
            this.timeout = Timeout.defaults();
            this.frameCodec = FrameCodec.line();
            this.handler = Demuxer.noop();
            this.observer = EventObserver.noop();
            this.openHandler = session -> {
            };
            this.errorHandler = cause -> {
            };
        }

        /**
         * Sets a bind host and port.
         *
         * @param host host
         * @param port port
         * @return this builder
         */
        public Builder bind(final String host, final int port) {
            validateHost(host);
            validatePort(port);
            return bind(new Address(Protocol.TCP.name, host, port, Symbol.SLASH));
        }

        /**
         * Sets a bind address.
         *
         * @param address bind address
         * @return this builder
         */
        public Builder bind(final Address address) {
            final Address current = require(address, "Socket bind address");
            if (!Protocol.TCP.name.equalsIgnoreCase(current.scheme()) || !Symbol.SLASH.equals(current.path())) {
                throw new ValidateException("Socket server bind address must be tcp://host:port/");
            }
            this.address = current;
            return this;
        }

        /**
         * Sets listen backlog.
         *
         * @param value backlog
         * @return this builder
         */
        public Builder backlog(final int value) {
            return socketOptions(copySocketOptions().backlog(value).build());
        }

        /**
         * Sets I/O thread count.
         *
         * @param value I/O thread count
         * @return this builder
         */
        public Builder ioThreads(final int value) {
            return socketOptions(copySocketOptions().ioThreads(value).build());
        }

        /**
         * Sets socket options.
         *
         * @param options socket options
         * @return this builder
         */
        public Builder socketOptions(final SocketOptions options) {
            this.socketOptions = require(options, "Socket options");
            return this;
        }

        /**
         * Sets the unified timeout policy for accepted sessions, TLS, and shutdown.
         *
         * @param timeout timeout policy
         * @return this builder
         */
        public Builder timeout(final Timeout timeout) {
            this.timeout = require(timeout, "Socket server timeout");
            return this;
        }

        /**
         * Enables server-side TLS with a context and settings configured as one pair.
         *
         * @param context  TLS context
         * @param settings TLS settings
         * @return this builder
         */
        public Builder tls(final TlsContext context, final TlsSettings settings) {
            this.tlsContext = require(context, "TLS context");
            this.tlsSettings = require(settings, "TLS settings");
            return this;
        }

        /**
         * Sets read buffer size.
         *
         * @param size size
         * @return this builder
         */
        public Builder readBufferSize(final int size) {
            return socketOptions(copySocketOptions().readBufferSize(size).build());
        }

        /**
         * Sets write chunk size.
         *
         * @param size size
         * @return this builder
         */
        public Builder writeChunkSize(final int size) {
            return socketOptions(copySocketOptions().writeChunkSize(size).build());
        }

        /**
         * Sets write chunk count.
         *
         * @param count count
         * @return this builder
         */
        public Builder writeChunkCount(final int count) {
            return socketOptions(copySocketOptions().writeChunkCount(count).build());
        }

        /**
         * Sets read buffer retention.
         *
         * @param retain retain flag
         * @return this builder
         */
        public Builder retainReadBuffer(final boolean retain) {
            return socketOptions(copySocketOptions().retainReadBuffer(retain).build());
        }

        /**
         * Sets idle timeout.
         *
         * @param timeout idle timeout
         * @return this builder
         */
        public Builder idleTimeout(final Duration timeout) {
            return socketOptions(copySocketOptions().idleTimeout(timeout).build());
        }

        /**
         * Sets frame codec.
         *
         * @param codec codec
         * @return this builder
         */
        public Builder frame(final FrameCodec codec) {
            this.frameCodec = require(codec, "Frame codec");
            return this;
        }

        /**
         * Uses line frame codec.
         *
         * @return this builder
         */
        public Builder lineFrame() {
            return frame(FrameCodec.line());
        }

        /**
         * Uses delimiter frame codec.
         *
         * @param delimiter delimiter
         * @return this builder
         */
        public Builder delimiterFrame(final byte[] delimiter) {
            return frame(LineCodec.of(delimiter));
        }

        /**
         * Uses fixed frame codec.
         *
         * @param length length
         * @return this builder
         */
        public Builder fixedFrame(final int length) {
            return frame(FrameCodec.length(length));
        }

        /**
         * Uses length-field frame codec.
         *
         * @return this builder
         */
        public Builder lengthFieldFrame() {
            return frame(FrameCodec.lengthField());
        }

        /**
         * Uses raw frame codec.
         *
         * @return this builder
         */
        public Builder rawFrame() {
            return frame(FrameCodec.raw());
        }

        /**
         * Sets message handler.
         *
         * @param handler handler
         * @return this builder
         */
        public Builder onMessage(final Handler handler) {
            this.handler = handler == null ? Demuxer.noop() : handler;
            this.demuxer = null;
            return this;
        }

        /**
         * Adds a channel handler.
         *
         * @param channel channel
         * @param handler handler
         * @return this builder
         */
        public Builder channel(final String channel, final Handler handler) {
            demuxer().channel(channel, handler);
            return this;
        }

        /**
         * Sets fallback handler.
         *
         * @param handler handler
         * @return this builder
         */
        public Builder fallback(final Handler handler) {
            demuxer().fallback(handler);
            return this;
        }

        /**
         * Sets channel header.
         *
         * @param name header name
         * @return this builder
         */
        public Builder channelHeader(final String name) {
            demuxer().header(name);
            return this;
        }

        /**
         * Sets channel resolver.
         *
         * @param resolver resolver
         * @return this builder
         */
        public Builder resolver(final Function<Message, String> resolver) {
            demuxer().resolver(resolver);
            return this;
        }

        /**
         * Sets text handler.
         *
         * @param handler text handler
         * @return this builder
         */
        public Builder onText(final Consumer<String> handler) {
            this.demuxer = null;
            this.handler = handler == null ? Demuxer.noop()
                    : (session, message) -> handler.accept(message.payload().text(Charset.UTF_8));
            return this;
        }

        /**
         * Sets accepted session open handler.
         *
         * @param handler open handler
         * @return this builder
         */
        public Builder onOpen(final Consumer<SocketSession> handler) {
            this.openHandler = handler == null ? session -> {
            } : handler;
            return composeSessionListener();
        }

        /**
         * Sets accepted session error handler.
         *
         * @param handler error handler
         * @return this builder
         */
        public Builder onError(final Consumer<Throwable> handler) {
            this.errorHandler = handler == null ? cause -> {
            } : handler;
            return composeSessionListener();
        }

        /**
         * Sets guard rule.
         *
         * @param guard guard
         * @return this builder
         */
        public Builder guard(final GuardRule guard) {
            this.guard = guard;
            return this;
        }

        /**
         * Sets filter.
         *
         * @param filter filter
         * @return this builder
         */
        public Builder filter(final Filter filter) {
            this.filter = filter;
            return this;
        }

        /**
         * Sets observer.
         *
         * @param observer observer
         * @return this builder
         */
        public Builder observe(final EventObserver observer) {
            this.observer = observer == null ? EventObserver.noop() : observer;
            return this;
        }

        /**
         * Sets server lifecycle listener.
         *
         * @param listener listener
         * @return this builder
         */
        public Builder listener(final Listener<? super SocketServer> listener) {
            this.listener = listener;
            return this;
        }

        /**
         * Sets accepted session lifecycle listener.
         *
         * @param listener listener
         * @return this builder
         */
        public Builder sessionListener(final Listener<? super SocketSession> listener) {
            this.sessionListener = listener;
            return this;
        }

        /**
         * Builds a socket server.
         *
         * @return socket server
         */
        public SocketServer build() {
            if (address == null) {
                throw new ValidateException("Socket server bind address must be set");
            }
            validateTlsPair(tlsContext, tlsSettings);
            return new SocketServer(this);
        }

        /**
         * Builds and starts a socket server.
         *
         * @return socket server
         */
        public SocketServer start() {
            return build().start();
        }

        /**
         * Returns effective handler.
         *
         * @return handler
         */
        private Handler handler() {
            return demuxer == null ? handler : demuxer.build();
        }

        /**
         * Returns effective session listener.
         *
         * @return listener
         */
        private Listener<? super SocketSession> sessionListener() {
            return sessionListener == null ? new Listener<>() {
            } : sessionListener;
        }

        /**
         * Composes open and error callbacks.
         *
         * @return this builder
         */
        private Builder composeSessionListener() {
            this.sessionListener = new Listener<>() {

                /**
                 * Forwards an opened session to the configured consumer.
                 */
                @Override
                public void open(final SocketSession source) {
                    openHandler.accept(source);
                }

                /**
                 * Forwards a session failure to the configured error consumer.
                 */
                @Override
                public void failure(final SocketSession source, final Throwable cause) {
                    errorHandler.accept(cause);
                }
            };
            return this;
        }

        /**
         * Returns demuxer builder.
         *
         * @return demuxer builder
         */
        private Demuxer.Builder demuxer() {
            if (demuxer == null) {
                demuxer = Demuxer.builder();
            }
            return demuxer;
        }

        /**
         * Copies socket options.
         *
         * @return builder
         */
        private SocketOptions.Builder copySocketOptions() {
            return SocketOptions.builder().readBufferSize(socketOptions.readBufferSize())
                    .writeChunkSize(socketOptions.writeChunkSize()).writeChunkCount(socketOptions.writeChunkCount())
                    .backlog(socketOptions.backlog()).ioThreads(socketOptions.ioThreads())
                    .socketOptions(socketOptions.socketOptions()).retainReadBuffer(socketOptions.retainReadBuffer())
                    .idleTimeout(socketOptions.idleTimeout()).kcpWireVersion(socketOptions.kcpWireVersion());
        }

        /**
         * Validates host.
         *
         * @param host host
         */
        private static void validateHost(final String host) {
            if (StringKit.isBlank(host) || StringKit.containsAny(host, Symbol.C_CR, Symbol.C_LF)) {
                throw new ValidateException("Socket server host must be non-blank and single-line");
            }
        }

        /**
         * Validates port.
         *
         * @param port port
         */
        private static void validatePort(final int port) {
            if (port < Normal._1 || port > Normal._65535) {
                throw new ValidateException("Socket server port must be between 1 and 65535");
            }
        }

    }

}
