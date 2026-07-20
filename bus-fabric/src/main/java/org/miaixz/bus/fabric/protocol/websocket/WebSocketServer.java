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
package org.miaixz.bus.fabric.protocol.websocket;

import static org.miaixz.bus.fabric.Builder.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.SocketException;
import org.miaixz.bus.core.lang.exception.StatefulException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.HTTP;
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
import org.miaixz.bus.fabric.guard.GuardRule;
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
import org.miaixz.bus.fabric.protocol.socket.SocketOptions;
import org.miaixz.bus.fabric.protocol.websocket.upgrade.WebSocketUpgrade;
import org.miaixz.bus.fabric.runtime.Activity;
import org.miaixz.bus.fabric.runtime.FilterChain;
import org.miaixz.bus.fabric.runtime.dispatch.DispatchHandle;
import org.miaixz.bus.fabric.runtime.dispatch.Dispatcher;
import org.miaixz.bus.fabric.runtime.lifecycle.LifecycleScope;
import org.miaixz.bus.fabric.runtime.resource.Cancellation;

/**
 * WebSocket server that upgrades accepted plain or TLS connections through {@link WebSocketUpgrade}.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class WebSocketServer implements Lifecycle {

    /**
     * Shared context.
     */
    private final Context context;

    /**
     * Bind address.
     */
    private final Address address;

    /**
     * Accepted HTTP upgrade path.
     */
    private final String path;

    /**
     * Socket options used by the listening and accepted channels.
     */
    private final SocketOptions socketOptions;

    /**
     * Additional successful upgrade response headers.
     */
    private final Headers responseHeaders;

    /**
     * Unified timeout policy for TLS, sessions, and server shutdown.
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
     * Message handler.
     */
    private final Handler handler;

    /**
     * Optional guard rule.
     */
    private final GuardRule guard;

    /**
     * Optional message filter.
     */
    private final Filter filter;

    /**
     * Event observer.
     */
    private final EventObserver observer;

    /**
     * Server lifecycle listener.
     */
    private final Listener<? super WebSocketServer> listener;

    /**
     * Accepted session lifecycle listener.
     */
    private final Listener<? super WebSocketSession> sessionListener;

    /**
     * Context dispatcher shared by accept setup, TLS, and sessions.
     */
    private final Dispatcher dispatcher;

    /**
     * Server lifecycle scope.
     */
    private final LifecycleScope lifecycle;

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
     * Sessions registered only after successful HTTP upgrade.
     */
    private final Queue<WebSocketSession> sessions;

    /**
     * Accepted transports, including TLS and HTTP handshakes still in progress.
     */
    private final Queue<AcceptedTransport> transports;

    /**
     * Raw channels still being inspected for an optional PROXY header.
     */
    private final Queue<SocketChannel> acceptedChannels;

    /**
     * Accepted setup and accept-loop handles.
     */
    private final Queue<DispatchHandle> handles;

    /**
     * Creates a WebSocket server from one validated builder snapshot.
     *
     * @param builder builder
     */
    private WebSocketServer(final Builder builder) {
        this.context = require(builder.context, "Context");
        this.address = require(builder.address, "WebSocket bind address");
        this.path = require(builder.path, "WebSocket path");
        this.socketOptions = require(builder.socketOptions, "Socket options");
        this.responseHeaders = require(builder.responseHeaders, "WebSocket response headers");
        this.timeout = require(builder.timeout, "WebSocket server timeout");
        this.tlsContext = builder.tlsContext;
        this.tlsSettings = builder.tlsSettings;
        validateTlsPair(this.tlsContext, this.tlsSettings);
        this.handler = builder.handler();
        this.guard = builder.guard;
        this.filter = builder.filter;
        this.observer = EventObserver.safe(builder.observer);
        this.listener = builder.listener;
        this.sessionListener = builder.sessionListener();
        this.dispatcher = context.reactor().dispatcher();
        this.lifecycle = LifecycleScope.resource(this, "websocket-server", listener, observer);
        this.lifecycleLock = new Object();
        this.started = new AtomicBoolean();
        this.shuttingDown = new AtomicBoolean();
        this.sessions = new ConcurrentLinkedQueue<>();
        this.transports = new ConcurrentLinkedQueue<>();
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
     * Starts this server once.
     *
     * @return this server
     */
    public WebSocketServer start() {
        synchronized (lifecycleLock) {
            if (running()) {
                return this;
            }
            if (shuttingDown.get() || lifecycle.state().terminal()) {
                throw new StatefulException("WebSocket server is already closed");
            }
            if (!started.compareAndSet(false, true)) {
                throw new StatefulException("WebSocket server can only be started once");
            }
            ServerSocketChannel opened = null;
            try {
                opened = ServerSocketChannel.open();
                opened.bind(new InetSocketAddress(address.host(), address.port()), socketOptions.backlog());
                serverChannel = opened;
                lifecycle.open(this);
                acceptHandle = track(
                        dispatcher.background(
                                WEBSOCKET_ACTIVITY_ACCEPT,
                                this,
                                Activity.of(WEBSOCKET_ACTIVITY_ACCEPT, this::acceptLoop)));
                return this;
            } catch (final IOException e) {
                closeServerChannel(opened);
                serverChannel = null;
                final SocketException failure = new SocketException("Unable to start WebSocket server", e);
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
    public WebSocketServer execute() {
        return start();
    }

    /**
     * Sends close code 1001, waits for the configured close interval, and releases remaining resources.
     *
     * @return true when lifecycle changed
     */
    public boolean close() {
        return shutdown(true);
    }

    /**
     * Cancels this server and all accepted resources immediately.
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
     * Returns whether this server is accepting connections.
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
     * Returns immutable server attributes.
     *
     * @return attributes
     */
    public Map<String, Object> attributes() {
        return Map.of(ATTRIBUTE_OBSERVER, observer, ATTRIBUTE_SOCKET_OPTIONS, socketOptions);
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
                                    WEBSOCKET_ACTIVITY_ACCEPT,
                                    channel,
                                    Activity.of(WEBSOCKET_ACTIVITY_ACCEPT, () -> handleAccepted(channel))));
                } catch (final RuntimeException e) {
                    acceptedChannels.remove(channel);
                    closeAcceptedChannel(channel);
                    throw e;
                }
            }
        } catch (final IOException e) {
            if (!shuttingDown.get()) {
                failServer(new SocketException("WebSocket server accept failed", e));
            }
        } catch (final RuntimeException e) {
            if (!shuttingDown.get()) {
                failServer(e);
            }
        }
    }

    /**
     * Performs PROXY inspection, optional TLS, and one delegated HTTP upgrade before creating a session.
     *
     * @param channel accepted channel
     */
    private void handleAccepted(final SocketChannel channel) {
        AcceptedTransport transport = null;
        WebSocketSession session = null;
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
            transport = new AcceptedTransport(channel, ingress);
            transports.add(transport);
            acceptedChannels.remove(channel);
            if (shuttingDown.get()) {
                transport.close();
                return;
            }
            final WebSocketUpgrade.Result upgrade;
            if (tlsContext == null) {
                upgrade = WebSocketUpgrade
                        .upgrade(ingress, path, responseHeaders, headers -> validateOpening(peerAddress, headers));
            } else {
                final TlsChannel tls = TlsChannel.wrap(
                        ingress,
                        TlsEngine.createServer(tlsContext, peerAddress, tlsSettings),
                        context.listener(),
                        dispatcher,
                        timeout);
                transport.secure(tls);
                await(tls.handshake(), "WebSocket server TLS handshake failed");
                upgrade = WebSocketUpgrade.upgrade(
                        tls.source(),
                        tls.sink(),
                        path,
                        responseHeaders,
                        headers -> validateOpening(peerAddress, headers));
            }
            if (shuttingDown.get()) {
                transport.close();
                return;
            }
            final Filter sessionFilter = FilterChain.compose(context.filter(), filter);
            session = new WebSocketSession(peerAddress, upgrade.source(), upgrade.sink(), null, handler, context,
                    timeout, dispatchKey(peerAddress), guard, WebSocketRole.SERVER,
                    sessionAttributes(upgrade.requestHeaders(), proxy.header()), transport, sessionFilter, observer,
                    registryListener(transport), Cancellation.create());
            if (shuttingDown.get()) {
                session.close(_1001, "Server shutting down");
                return;
            }
            transferred = true;
            transport = null;
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
            if (!transferred && transport != null) {
                transports.remove(transport);
                closeTransport(transport);
            } else if (!transferred) {
                closeAcceptedChannel(channel);
            }
        }
    }

    /**
     * Applies the configured opening filter and guard before the upgrade response is written.
     *
     * @param peerAddress peer address
     * @param headers     request headers
     */
    private void validateOpening(final Address peerAddress, final Headers headers) {
        Message opening = Message.of(peerAddress.protocol(), peerAddress, headers, Payload.empty(), WEBSOCKET_OPEN);
        opening = FilterChain.apply(opening, context.filter(), filter);
        if (guard != null) {
            guard.check(opening).throwIfRejected();
        }
    }

    /**
     * Creates a common WS/WSS registry listener that removes ownership before user callbacks.
     *
     * @param transport accepted transport
     * @return listener
     */
    private Listener<WebSocketSession> registryListener(final AcceptedTransport transport) {
        return new Listener<>() {

            @Override
            public void open(final WebSocketSession source) {
                sessions.add(source);
                notifySessionOpen(source);
            }

            @Override
            public void close(final WebSocketSession source) {
                remove(source);
                notifySessionClose(source);
            }

            @Override
            public void failure(final WebSocketSession source, final Throwable cause) {
                remove(source);
                notifySessionFailure(source, cause);
            }

            /**
             * Removes session and transport ownership before forwarding the terminal callback.
             *
             * @param source terminal session
             */
            private void remove(final WebSocketSession source) {
                sessions.remove(source);
                transports.remove(transport);
            }
        };
    }

    /**
     * Tracks a dispatch handle until completion.
     *
     * @param handle handle
     * @return tracked handle
     */
    private DispatchHandle track(final DispatchHandle handle) {
        handles.add(handle);
        handle.future().whenComplete((ignored, cause) -> handles.remove(handle));
        return handle;
    }

    /**
     * Cancels and clears all tracked handles.
     */
    private void cancelHandles() {
        DispatchHandle handle = handles.poll();
        while (handle != null) {
            handle.cancel();
            handle = handles.poll();
        }
    }

    /**
     * Stops accepting, closes sessions, and releases every accepted transport.
     *
     * @param graceful true to send close code 1001 and wait
     * @return true when lifecycle changed
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
            lifecycle.emit(ObservationMarker.WEBSOCKET_FAILED, failure);
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
                currentFailure = append(currentFailure, new SocketException("Unable to close WebSocket server", e));
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
     * Requests graceful code-1001 close or cancellation for all registered sessions.
     *
     * @param graceful true to close normally
     * @param failure  current cleanup failure
     * @return aggregated cleanup failure
     */
    private RuntimeException terminateSessions(final boolean graceful, final RuntimeException failure) {
        RuntimeException currentFailure = failure;
        for (final WebSocketSession session : new ArrayList<>(sessions)) {
            try {
                if (graceful) {
                    session.close(_1001, "Server shutting down");
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
     * Returns whether accepted sessions or transports still own resources.
     *
     * @return true when resources remain
     */
    private boolean hasAcceptedResources() {
        return !sessions.isEmpty() || !transports.isEmpty() || !acceptedChannels.isEmpty();
    }

    /**
     * Cancels remaining work and force-closes lower-level resources.
     *
     * @param failure current cleanup failure
     * @return aggregated cleanup failure
     */
    private RuntimeException forceClose(final RuntimeException failure) {
        RuntimeException currentFailure = failure;
        cancelHandles();
        currentFailure = terminateSessions(false, currentFailure);
        for (final AcceptedTransport transport : new ArrayList<>(transports)) {
            try {
                transport.close();
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
        transports.clear();
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
     * Builds immutable session attributes from the validated upgrade.
     *
     * @param headers     request headers
     * @param proxyHeader optional PROXY metadata
     * @return attributes
     */
    private Map<String, Object> sessionAttributes(final Headers headers, final ProxyHeader proxyHeader) {
        final LinkedHashMap<String, Object> values = new LinkedHashMap<>();
        values.put(ATTRIBUTE_HEADERS, headers);
        values.put(ATTRIBUTE_OBSERVER, observer);
        values.put(ATTRIBUTE_SOCKET_OPTIONS, socketOptions);
        if (proxyHeader != null) {
            values.put(ATTRIBUTE_PROXY_HEADER, proxyHeader);
        }
        return Map.copyOf(values);
    }

    /**
     * Resolves one peer address and assigns the effective WS or WSS scheme.
     *
     * @param channel     accepted channel
     * @param proxyHeader optional PROXY metadata
     * @return peer address
     */
    private Address peerAddress(final SocketChannel channel, final ProxyHeader proxyHeader) {
        final String scheme = tlsContext == null ? Protocol.WS.name : Protocol.WSS.name;
        if (proxyHeader != null && proxyHeader.sourceAddress() != null) {
            final Address source = proxyHeader.sourceAddress();
            return new Address(scheme, source.host(), source.port(), Symbol.SLASH);
        }
        try {
            final SocketAddress remote = channel.getRemoteAddress();
            if (!(remote instanceof InetSocketAddress socket)) {
                throw new ProtocolException("Accepted WebSocket peer address must be InetSocketAddress");
            }
            return new Address(scheme, socket.getHostString(), socket.getPort(), Symbol.SLASH);
        } catch (final IOException e) {
            throw new ProtocolException("Unable to resolve accepted WebSocket peer address", e);
        }
    }

    /**
     * Builds a stable dispatch key for one accepted session.
     *
     * @param peerAddress peer address
     * @return dispatch key
     */
    private static String dispatchKey(final Address peerAddress) {
        return peerAddress.scheme() + Symbol.COLON + Symbol.SLASH + Symbol.SLASH + peerAddress.host() + Symbol.C_COLON
                + peerAddress.port();
    }

    /**
     * Notifies the user listener after a session enters the registry.
     *
     * @param session session
     */
    private void notifySessionOpen(final WebSocketSession session) {
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
    private void notifySessionClose(final WebSocketSession session) {
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
    private void notifySessionFailure(final WebSocketSession session, final Throwable cause) {
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
        lifecycle.emit(ObservationMarker.WEBSOCKET_FAILED, cause);
        try {
            sessionListener.failure(null, cause);
        } catch (final RuntimeException e) {
            lifecycle.emit(ObservationMarker.LISTENER_FAILED, e);
        }
    }

    /**
     * Closes one accepted transport without destabilizing its setup activity.
     *
     * @param transport accepted transport
     */
    private void closeTransport(final AcceptedTransport transport) {
        try {
            transport.close();
        } catch (final RuntimeException e) {
            lifecycle.emit(ObservationMarker.WEBSOCKET_FAILED, e);
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
            lifecycle.emit(
                    ObservationMarker.WEBSOCKET_FAILED,
                    new SocketException("Unable to close accepted WebSocket channel", e));
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
     * Returns a timeout policy with one replacement automatic ping interval.
     *
     * @param timeout source timeout
     * @param ping    ping interval
     * @return updated timeout
     */
    private static Timeout withPing(final Timeout timeout, final Duration ping) {
        return new Timeout(timeout.connect(), timeout.read(), timeout.write(), timeout.call(), ping, timeout.close());
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
     * @param name  field name
     * @param <T>   value type
     * @return value
     */
    private static <T> T require(final T value, final String name) {
        return Assert.notNull(value, () -> new ValidateException(name + " must not be null"));
    }

    /**
     * Accepted channel ownership shared by plain and TLS WebSocket setup paths.
     */
    private static final class AcceptedTransport implements AutoCloseable {

        /**
         * Raw accepted channel closed last.
         */
        private final SocketChannel accepted;

        /**
         * Ingress retaining any prefetched PROXY payload.
         */
        private final Ingress ingress;

        /**
         * Idempotent close guard.
         */
        private final AtomicBoolean closed;

        /**
         * Optional TLS boundary installed before handshake.
         */
        private volatile TlsChannel tls;

        /**
         * Creates a plain accepted transport.
         *
         * @param accepted raw channel
         * @param ingress  ingress over the same channel
         */
        private AcceptedTransport(final SocketChannel accepted, final Ingress ingress) {
            this.accepted = require(accepted, "Accepted channel");
            this.ingress = require(ingress, "Accepted ingress");
            this.closed = new AtomicBoolean();
        }

        /**
         * Installs one TLS boundary before its handshake begins.
         *
         * @param channel TLS channel
         */
        private synchronized void secure(final TlsChannel channel) {
            final TlsChannel current = require(channel, "Accepted TLS channel");
            if (tls != null) {
                current.close();
                throw new StatefulException("Accepted WebSocket transport already has TLS");
            }
            if (closed.get()) {
                current.close();
                throw new StatefulException("Accepted WebSocket transport is closed");
            }
            tls = current;
        }

        /**
         * Closes TLS, ingress, and the raw accepted channel in order exactly once.
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
                failure = append(failure, new SocketException("Unable to close accepted WebSocket channel", e));
            }
            if (failure != null) {
                throw failure;
            }
        }

    }

    /**
     * Builder for WebSocket servers.
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
         * Route path.
         */
        private String path = Symbol.SLASH;

        /**
         * Socket options.
         */
        private SocketOptions socketOptions = SocketOptions.defaults();

        /**
         * Additional response headers.
         */
        private Headers responseHeaders = Headers.empty();

        /**
         * Unified timeout policy.
         */
        private Timeout timeout = Timeout.defaults();

        /**
         * Optional server TLS context.
         */
        private TlsContext tlsContext;

        /**
         * Optional server TLS settings.
         */
        private TlsSettings tlsSettings;

        /**
         * Message handler.
         */
        private Handler handler = Demuxer.noop();

        /**
         * Optional demuxer builder.
         */
        private Demuxer.Builder demuxer;

        /**
         * Optional guard.
         */
        private GuardRule guard;

        /**
         * Optional filter.
         */
        private Filter filter;

        /**
         * Event observer.
         */
        private EventObserver observer = EventObserver.noop();

        /**
         * Server listener.
         */
        private Listener<? super WebSocketServer> listener;

        /**
         * Session listener.
         */
        private Listener<? super WebSocketSession> sessionListener;

        /**
         * Open handler.
         */
        private Consumer<WebSocketSession> openHandler = session -> {
        };

        /**
         * Error handler.
         */
        private Consumer<Throwable> errorHandler = cause -> {
        };

        /**
         * Creates a builder.
         *
         * @param context shared context
         */
        private Builder(final Context context) {
            this.context = context;
        }

        /**
         * Sets bind host and port.
         *
         * @param host host
         * @param port port
         * @return this builder
         */
        public Builder bind(final String host, final int port) {
            validateHost(host);
            validatePort(port);
            return bind(new Address(Protocol.WS.name, host, port, Symbol.SLASH));
        }

        /**
         * Sets bind address.
         *
         * @param address bind address
         * @return this builder
         */
        public Builder bind(final Address address) {
            final Address current = require(address, "WebSocket bind address");
            if (!Protocol.WS.name.equalsIgnoreCase(current.scheme()) || !Symbol.SLASH.equals(current.path())) {
                throw new ValidateException("WebSocket server bind address must be ws://host:port/");
            }
            this.address = current;
            return this;
        }

        /**
         * Sets route path.
         *
         * @param value absolute path
         * @return this builder
         */
        public Builder path(final String value) {
            if (StringKit.isBlank(value) || StringKit.containsAny(value, Symbol.C_CR, Symbol.C_LF)
                    || !value.startsWith(Symbol.SLASH)) {
                throw new ValidateException("WebSocket server path must be absolute and single-line");
            }
            this.path = value;
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
         * @param options options
         * @return this builder
         */
        public Builder socketOptions(final SocketOptions options) {
            this.socketOptions = require(options, "Socket options");
            return this;
        }

        /**
         * Sets the unified timeout policy for TLS, sessions, and shutdown.
         *
         * @param timeout timeout policy
         * @return this builder
         */
        public Builder timeout(final Timeout timeout) {
            this.timeout = require(timeout, "WebSocket server timeout");
            return this;
        }

        /**
         * Enables server-side TLS with context and settings configured as one pair.
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
         * Adds a response header.
         *
         * @param name  name
         * @param value value
         * @return this builder
         */
        public Builder header(final String name, final String value) {
            rejectReservedHeader(name);
            responseHeaders = responseHeaders.with(name, value);
            return this;
        }

        /**
         * Merges response headers.
         *
         * @param headers headers
         * @return this builder
         */
        public Builder headers(final Headers headers) {
            final Headers current = require(headers, "Headers");
            for (final Map.Entry<String, List<String>> entry : current.asMap().entrySet()) {
                rejectReservedHeader(entry.getKey());
                for (final String value : entry.getValue()) {
                    responseHeaders = responseHeaders.with(entry.getKey(), value);
                }
            }
            return this;
        }

        /**
         * Sets the selected subprotocol returned after validating that the client requested it.
         *
         * @param protocol subprotocol
         * @return this builder
         */
        public Builder subprotocol(final String protocol) {
            if (StringKit.isBlank(protocol) || StringKit.containsAny(protocol, Symbol.C_CR, Symbol.C_LF)) {
                throw new ValidateException("WebSocket subprotocol must be non-blank and single-line");
            }
            responseHeaders = responseHeaders.with(HTTP.SEC_WEBSOCKET_PROTOCOL, protocol);
            return this;
        }

        /**
         * Sets the automatic ping interval while preserving every other timeout component.
         *
         * @param value ping interval
         * @return this builder
         */
        public Builder ping(final Duration value) {
            final Duration checked = require(value, "WebSocket ping interval");
            if (checked.isNegative()) {
                throw new ValidateException("WebSocket ping interval must be non-negative");
            }
            this.timeout = withPing(timeout, checked);
            return this;
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
         * Adds channel handler.
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
        public Builder onText(final BiConsumer<WebSocketSession, String> handler) {
            this.demuxer = null;
            this.handler = handler == null ? Demuxer.noop()
                    : (session, message) -> handler
                            .accept((WebSocketSession) session, message.payload().text(Charset.UTF_8));
            return this;
        }

        /**
         * Sets open handler.
         *
         * @param handler open handler
         * @return this builder
         */
        public Builder onOpen(final Consumer<WebSocketSession> handler) {
            this.openHandler = handler == null ? session -> {
            } : handler;
            return composeSessionListener();
        }

        /**
         * Sets error handler.
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
         * Sets guard.
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
         * Sets server listener.
         *
         * @param listener listener
         * @return this builder
         */
        public Builder listener(final Listener<? super WebSocketServer> listener) {
            this.listener = listener;
            return this;
        }

        /**
         * Sets session listener.
         *
         * @param listener listener
         * @return this builder
         */
        public Builder sessionListener(final Listener<? super WebSocketSession> listener) {
            this.sessionListener = listener;
            return this;
        }

        /**
         * Builds a server.
         *
         * @return server
         */
        public WebSocketServer build() {
            if (address == null) {
                throw new ValidateException("WebSocket server bind address must be set");
            }
            validateTlsPair(tlsContext, tlsSettings);
            return new WebSocketServer(this);
        }

        /**
         * Builds and starts a server.
         *
         * @return server
         */
        public WebSocketServer start() {
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
        private Listener<? super WebSocketSession> sessionListener() {
            return sessionListener == null ? new Listener<>() {
            } : sessionListener;
        }

        /**
         * Composes callback listener.
         *
         * @return this builder
         */
        private Builder composeSessionListener() {
            this.sessionListener = new Listener<>() {

                @Override
                public void open(final WebSocketSession source) {
                    openHandler.accept(source);
                }

                @Override
                public void failure(final WebSocketSession source, final Throwable cause) {
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
         * Copies socket options without losing idle timeout or KCP wire version.
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
         * Rejects response headers owned by the upgrade protocol.
         *
         * @param name header name
         */
        private static void rejectReservedHeader(final String name) {
            if (HTTP.UPGRADE.equalsIgnoreCase(name) || HTTP.CONNECTION.equalsIgnoreCase(name)
                    || HTTP.SEC_WEBSOCKET_ACCEPT.equalsIgnoreCase(name)) {
                throw new ValidateException("WebSocket server response header is reserved: " + name);
            }
        }

        /**
         * Validates host.
         *
         * @param host host
         */
        private static void validateHost(final String host) {
            if (StringKit.isBlank(host) || StringKit.containsAny(host, Symbol.C_CR, Symbol.C_LF)) {
                throw new ValidateException("WebSocket server host must be non-blank and single-line");
            }
        }

        /**
         * Validates port.
         *
         * @param port port
         */
        private static void validatePort(final int port) {
            if (port < Normal._1 || port > Normal._65535) {
                throw new ValidateException("WebSocket server port must be between 1 and 65535");
            }
        }

    }

}
