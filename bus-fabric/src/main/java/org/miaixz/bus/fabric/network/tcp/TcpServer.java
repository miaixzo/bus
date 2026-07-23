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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.exception.SocketException;
import org.miaixz.bus.core.lang.exception.StatefulException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.core.xyz.ThreadKit;
import org.miaixz.bus.fabric.Address;
import org.miaixz.bus.fabric.Call;
import org.miaixz.bus.fabric.Handler;
import org.miaixz.bus.fabric.Headers;
import org.miaixz.bus.fabric.Listener;
import org.miaixz.bus.fabric.Message;
import org.miaixz.bus.fabric.Payload;
import org.miaixz.bus.fabric.Session;
import org.miaixz.bus.fabric.Status;
import org.miaixz.bus.fabric.network.Ingress;
import org.miaixz.bus.fabric.observe.EventObserver;
import org.miaixz.bus.fabric.observe.ObservationMarker;
import org.miaixz.bus.fabric.protocol.MonoCall;
import org.miaixz.bus.fabric.protocol.socket.SocketOptions;
import org.miaixz.bus.fabric.runtime.Activity;
import org.miaixz.bus.fabric.runtime.dispatch.DispatchHandle;
import org.miaixz.bus.fabric.runtime.dispatch.Dispatcher;
import org.miaixz.bus.fabric.runtime.lifecycle.LifecycleScope;
import org.miaixz.bus.logger.Logger;

/**
 * TCP server with a fixed backlog and handler-based accept loop.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class TcpServer implements AutoCloseable {

    /**
     * Local logical address bound when the server starts.
     */
    private final Address address;

    /**
     * Concurrent registry of accepted sessions that have not terminated.
     */
    private final Queue<TcpSession> sessions;

    /**
     * Server lifecycle coordination lock.
     */
    private final Object lifecycleLock;

    /**
     * Start guard allowing only one bind attempt.
     */
    private final AtomicBoolean started;

    /**
     * Accept-loop intent flag cleared before the server channel is closed.
     */
    private final AtomicBoolean running;

    /**
     * Atomic guard that makes global shutdown idempotent.
     */
    private final AtomicBoolean closed;

    /**
     * Lifecycle listener, replaced by a no-op singleton when absent.
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
     * Per-read buffer size for accepted sessions.
     */
    private final int readBufferSize;

    /**
     * Whether this server owns the dispatcher lifecycle.
     */
    private final boolean ownsDispatcher;

    /**
     * Handler assigned to newly accepted sockets, or {@code null} before registration.
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
     * @param address local address bound on first start
     * @throws ValidateException if {@code address} is {@code null}
     */
    public TcpServer(final Address address) {
        this(address, null, Dispatcher.create(), true);
    }

    /**
     * Creates a TCP server.
     *
     * @param address  local address bound on first start
     * @param listener optional lifecycle listener
     * @throws ValidateException if {@code address} is {@code null}
     */
    public TcpServer(final Address address, final Listener<Object> listener) {
        this(address, listener, Dispatcher.create(), true);
    }

    /**
     * Creates a TCP server with a shared dispatcher.
     *
     * @param address    local address bound on first start
     * @param listener   optional lifecycle listener
     * @param dispatcher borrowed dispatcher running accept and session activities
     * @throws ValidateException if the address or dispatcher is {@code null}
     */
    public TcpServer(final Address address, final Listener<Object> listener, final Dispatcher dispatcher) {
        this(address, listener, dispatcher, SocketOptions.defaults());
    }

    /**
     * Creates a TCP server with a shared dispatcher and socket options.
     *
     * @param address    local address bound on first start
     * @param listener   optional lifecycle listener
     * @param dispatcher borrowed dispatcher running accept and session activities
     * @param options    socket tuning options, or {@code null} for defaults
     * @throws ValidateException if the address or dispatcher is {@code null}
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
        this.lifecycleLock = new Object();
        this.started = new AtomicBoolean();
        this.running = new AtomicBoolean();
        this.closed = new AtomicBoolean();
        this.listener = listener == null ? NoopListener.INSTANCE : listener;
        this.dispatcher = Assert.notNull(dispatcher, () -> new ValidateException("TCP dispatcher must not be null"));
        this.ownsDispatcher = ownsDispatcher;
        final SocketOptions currentOptions = options == null ? SocketOptions.defaults() : options;
        this.backlog = currentOptions.backlog();
        this.readBufferSize = currentOptions.readBufferSize();
    }

    /**
     * Returns the listen address.
     *
     * @return local logical address configured for binding
     */
    public Address address() {
        return address;
    }

    /**
     * Returns listen backlog.
     *
     * @return configured maximum pending-connection backlog
     */
    public int backlog() {
        return backlog;
    }

    /**
     * Binds the server channel and submits the single accept loop.
     *
     * @throws SocketException   if the server channel cannot be opened or bound
     * @throws StatefulException if the server is closed or a start was already attempted
     */
    public void start() {
        synchronized (lifecycleLock) {
            if (closed.get()) {
                throw new StatefulException("TCP server is closed");
            }
            if (!started.compareAndSet(false, true)) {
                throw new StatefulException("TCP server can only be started once");
            }
            running.set(true);
            ServerSocketChannel opened = null;
            try {
                opened = ServerSocketChannel.open();
                opened.bind(socket(address), backlog);
                server = opened;
                acceptHandle = dispatcher.background(
                        Protocol.TCP.name + "-server" + Symbol.COLON + address.host() + Symbol.COLON + address.port(),
                        this,
                        Activity.of(Protocol.TCP.name + "-accept", this::acceptLoop));
                notifyOpen(this);
            } catch (final IOException e) {
                running.set(false);
                IoKit.closeQuietly(opened);
                server = null;
                notifyFailure(this, e);
                throw new SocketException("Unable to start TCP server", e);
            } catch (final RuntimeException e) {
                running.set(false);
                IoKit.closeQuietly(opened);
                server = null;
                notifyFailure(this, e);
                throw new InternalException("Unable to start TCP server", e);
            }
        }
    }

    /**
     * Sets the connection handler.
     *
     * @param handler non-null callback for accepted sessions and their messages
     * @throws ValidateException if {@code handler} is {@code null}
     */
    public void accept(final Handler handler) {
        this.handler = Assert.notNull(handler, () -> new ValidateException("TCP handler must not be null"));
    }

    /**
     * Returns whether this server is running.
     *
     * @return {@code true} when the accept intent is active and the server channel remains open
     */
    public boolean running() {
        final ServerSocketChannel current = server;
        return running.get() && current != null && current.isOpen();
    }

    /**
     * Stops acceptance, closes all observed sessions, and closes the dispatcher only when this server owns it.
     *
     * @throws InternalException if any server, session, accept task, or owned-dispatcher cleanup fails
     */
    @Override
    public void close() {
        if (!closed.compareAndSet(false, true)) {
            return;
        }
        final List<TcpSession> closing;
        Throwable failure = null;
        synchronized (lifecycleLock) {
            running.set(false);
            try {
                closeServer();
            } catch (final RuntimeException e) {
                failure = e;
            }
            final DispatchHandle handle = acceptHandle;
            if (handle != null) {
                try {
                    handle.cancel();
                } catch (final RuntimeException e) {
                    failure = collect(failure, e);
                }
                acceptHandle = null;
            }
            closing = new ArrayList<>(sessions);
        }
        for (final TcpSession session : closing) {
            try {
                session.close();
            } catch (final RuntimeException e) {
                failure = collect(failure, e);
            }
        }
        sessions.clear();
        if (ownsDispatcher) {
            try {
                dispatcher.close();
            } catch (final RuntimeException e) {
                failure = collect(failure, e);
            }
        }
        notifyClose(this);
        if (failure != null) {
            throw new InternalException("Unable to close TCP server", failure);
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
                    notifyFailure(this, e);
                    throw new SocketException("TCP accept failed", e);
                }
                return;
            }
        }
    }

    /**
     * Handles an accepted socket.
     *
     * @param socket newly accepted channel whose ownership is transferred to a session or closed
     */
    private void handle(final SocketChannel socket) {
        final Handler current = handler;
        if (current == null) {
            IoKit.closeQuietly(socket);
            return;
        }
        final TcpSession session;
        try {
            session = new TcpSession(address, socket, listener, dispatcher, current, sessions, readBufferSize);
        } catch (final RuntimeException e) {
            IoKit.closeQuietly(socket);
            notifyFailure(this, e);
            return;
        }
        synchronized (lifecycleLock) {
            if (closed.get() || !running.get()) {
                session.close();
                return;
            }
            sessions.add(session);
        }
        final Message message = Message.of(Protocol.TCP, address, Headers.empty(), Payload.empty(), session.ingress());
        try {
            current.message(session, message);
            session.startReader();
        } catch (final RuntimeException e) {
            session.fail(e);
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
            } finally {
                server = null;
            }
        }
    }

    /**
     * Aggregates a cleanup failure using suppressed exceptions.
     *
     * @param failure current primary failure
     * @param next    next failure
     * @return primary failure
     */
    private static Throwable collect(final Throwable failure, final Throwable next) {
        if (failure == null) {
            return next;
        }
        if (failure != next) {
            failure.addSuppressed(next);
        }
        return failure;
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
     * Notifies listener open without allowing listener failures to escape.
     *
     * @param source server or session whose lifecycle opened
     */
    private void notifyOpen(final Object source) {
        try {
            listener.open(source);
        } catch (final RuntimeException e) {
            listenerFailed("open", e);
        }
    }

    /**
     * Notifies listener close without allowing listener failures to escape.
     *
     * @param source server or session whose lifecycle closed
     */
    private void notifyClose(final Object source) {
        try {
            listener.close(source);
        } catch (final RuntimeException e) {
            listenerFailed("close", e);
        }
    }

    /**
     * Notifies listener failure without allowing listener failures to escape.
     *
     * @param source server or session whose lifecycle failed
     * @param cause  failure reported by that source
     */
    private void notifyFailure(final Object source, final Throwable cause) {
        try {
            listener.failure(source, cause);
        } catch (final RuntimeException e) {
            listenerFailed("failure", e);
        }
    }

    /**
     * Logs listener callback failures.
     *
     * @param action listener action
     * @param cause  listener failure
     */
    private void listenerFailed(final String action, final RuntimeException cause) {
        Logger.warn(false, "Fabric", cause, "TCP listener {} callback failed", action);
    }

    /**
     * Awaits a conduit operation and preserves runtime failures.
     *
     * @param future  non-null asynchronous byte-count operation to join
     * @param message context used when a checked or missing result must be wrapped
     * @return completed byte count
     */
    private static long await(final CompletableFuture<Long> future, final String message) {
        try {
            final Long result = Assert.notNull(future, () -> new ValidateException("TCP IO future must not be null"))
                    .join();
            return Assert.notNull(result, () -> new InternalException(message + ": missing byte count"));
        } catch (final CompletionException e) {
            final Throwable cause = e.getCause();
            if (cause instanceof RuntimeException runtime) {
                throw runtime;
            }
            if (cause instanceof Error error) {
                throw error;
            }
            throw new InternalException(message, cause);
        }
    }

    /**
     * Accepted TCP session.
     */
    private static final class TcpSession implements Session {

        /**
         * Server address associated with the accepted session.
         */
        private final Address address;

        /**
         * Ingress owning the accepted channel and conduit contract.
         */
        private final Ingress ingress;

        /**
         * Borrowed dispatcher running reader and deferred send activities.
         */
        private final Dispatcher dispatcher;

        /**
         * Session message handler.
         */
        private final Handler handler;

        /**
         * Owning server registry.
         */
        private final Queue<TcpSession> registry;

        /**
         * Read chunk size.
         */
        private final int readBufferSize;

        /**
         * Lifecycle scope.
         */
        private final LifecycleScope scope;

        /**
         * Background reader handle.
         */
        private final AtomicReference<DispatchHandle> readerHandle;

        /**
         * Creates a session.
         *
         * @param address        server address associated with messages from this session
         * @param socket         accepted channel transferred to the session-owned ingress
         * @param listener       optional lifecycle listener
         * @param dispatcher     borrowed runtime dispatcher
         * @param handler        callback receiving session messages and terminal events
         * @param registry       owning server's concurrent session registry
         * @param readBufferSize positive maximum bytes requested per read
         */
        private TcpSession(final Address address, final SocketChannel socket, final Listener<Object> listener,
                final Dispatcher dispatcher, final Handler handler, final Queue<TcpSession> registry,
                final int readBufferSize) {
            this.address = Assert.notNull(address, () -> new ValidateException("TCP session address must not be null"));
            this.ingress = Ingress.of(
                    this.address,
                    Assert.notNull(socket, () -> new ValidateException("TCP session socket must not be null")),
                    null);
            this.dispatcher = Assert
                    .notNull(dispatcher, () -> new ValidateException("TCP session dispatcher must not be null"));
            this.handler = Assert.notNull(handler, () -> new ValidateException("TCP session handler must not be null"));
            this.registry = Assert
                    .notNull(registry, () -> new ValidateException("TCP session registry must not be null"));
            Assert.isTrue(
                    readBufferSize > Normal._0,
                    () -> new ValidateException("TCP read buffer size must be positive"));
            this.readBufferSize = readBufferSize;
            this.readerHandle = new AtomicReference<>();
            this.scope = LifecycleScope.session(
                    this,
                    "tcp-session",
                    listener,
                    EventObserver.noop(),
                    ObservationMarker.SOCKET_OPEN,
                    ObservationMarker.SOCKET_CLOSED,
                    ObservationMarker.SOCKET_FAILED);
            this.scope.own(this.ingress);
            this.scope.open(this);
        }

        /**
         * Returns the accepted ingress.
         *
         * @return accepted ingress
         */
        private Ingress ingress() {
            return ingress;
        }

        /**
         * Starts the background session reader once.
         */
        private void startReader() {
            if (!opened()) {
                throw new StatefulException("TCP session is not open");
            }
            final DispatchHandle created = dispatcher
                    .background("tcp:session:reader", this, Activity.of("tcp:session:reader", this::readLoop));
            if (!readerHandle.compareAndSet(null, created)) {
                created.cancel();
                throw new StatefulException("TCP session reader can only be started once");
            }
            if (!opened()) {
                cancelReader();
            }
        }

        /**
         * Reads peer data until EOF, cancellation, close, or failure.
         */
        private void readLoop() {
            try {
                while (opened()) {
                    final Buffer buffer = new Buffer();
                    final long read = await(ingress.read(buffer, readBufferSize), "Unable to read TCP session");
                    if (read == Normal.__1) {
                        close();
                        return;
                    }
                    if (read == Normal._0) {
                        if (!ThreadKit.sleep(Normal._1)) {
                            cancel();
                            return;
                        }
                        continue;
                    }
                    final Message message = Message
                            .of(Protocol.TCP, address, Headers.empty(), Payload.of(buffer.readByteArray()), ingress);
                    handler.message(this, message);
                }
            } catch (final RuntimeException e) {
                if (opened()) {
                    fail(e);
                }
            }
        }

        /**
         * Returns address.
         *
         * @return server address associated with this accepted session
         */
        @Override
        public Address address() {
            return address;
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
         * Returns opened state.
         *
         * @return {@code true} when both lifecycle scope and ingress remain open
         */
        @Override
        public boolean opened() {
            return scope.state() == Status.OPENED && ingress.opened();
        }

        /**
         * Sends a payload.
         *
         * @param payload payload materialized when the returned call executes
         * @return deferred call that fully writes the payload or fails
         * @throws ValidateException if {@code payload} is {@code null}
         */
        @Override
        public Call<Void> send(final Payload payload) {
            final Payload checkedPayload = Assert
                    .notNull(payload, () -> new ValidateException("Payload must not be null"));
            return MonoCall.<Void>create(
                    "tcp-session-send",
                    "tcp:session:send",
                    dispatcher,
                    EventObserver.noop(),
                    null,
                    () -> write(checkedPayload),
                    this::cancel);
        }

        /**
         * Materializes and fully writes one payload through the ingress contract.
         *
         * @param payload payload to materialize and write completely
         * @return null after a complete write
         * @throws SocketException   if the ingress does not consume the entire payload
         * @throws StatefulException if the session is not open
         */
        private Void write(final Payload payload) {
            if (!opened()) {
                throw new StatefulException("TCP session is not open");
            }
            final Buffer source = new Buffer().write(payload.bytes());
            final long requested = source.size();
            final long written = await(ingress.write(source, requested), "Unable to write TCP session");
            if (written != requested || source.size() != Normal._0) {
                throw new SocketException("TCP session write did not fully consume the payload");
            }
            return null;
        }

        /**
         * Closes this session.
         *
         * @return {@code true} when this invocation performs the close transition
         */
        @Override
        public boolean close() {
            unregister();
            cancelReader();
            final boolean changed = scope.close(this);
            if (changed) {
                notifyHandlerClosed();
            }
            return changed;
        }

        /**
         * Cancels this session.
         *
         * @return {@code true} when this invocation performs the cancellation transition
         */
        @Override
        public boolean cancel() {
            unregister();
            cancelReader();
            final boolean changed = scope.cancel(new StatefulException("TCP session was cancelled"));
            if (changed) {
                notifyHandlerClosed();
            }
            return changed;
        }

        /**
         * Fails this session.
         *
         * @param cause failure reported by the reader or handler path
         * @return {@code true} when this invocation performs the failure transition
         */
        private boolean fail(final Throwable cause) {
            unregister();
            cancelReader();
            final boolean changed = scope.fail(cause);
            if (changed) {
                notifyHandlerFailure(cause);
            }
            return changed;
        }

        /**
         * Removes this session from its owning registry.
         */
        private void unregister() {
            registry.remove(this);
        }

        /**
         * Cancels the background reader handle.
         */
        private void cancelReader() {
            final DispatchHandle current = readerHandle.getAndSet(null);
            if (current != null) {
                current.cancel();
            }
        }

        /**
         * Notifies the handler that the session closed.
         */
        private void notifyHandlerClosed() {
            try {
                handler.closed(this);
            } catch (final RuntimeException e) {
                Logger.warn(false, "Fabric", e, "TCP handler close callback failed");
            }
        }

        /**
         * Notifies the handler that the session failed.
         *
         * @param cause failure cause
         */
        private void notifyHandlerFailure(final Throwable cause) {
            try {
                handler.failure(this, cause);
            } catch (final RuntimeException e) {
                Logger.warn(false, "Fabric", e, "TCP handler failure callback failed");
            }
        }

        /**
         * Returns attributes.
         *
         * @return shared immutable empty attribute map
         */
        @Override
        public Map<String, Object> attributes() {
            return Map.of();
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
