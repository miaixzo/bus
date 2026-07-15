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

import static org.miaixz.bus.fabric.Builder.*;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.SocketChannel;
import java.time.Duration;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;
import java.util.function.Function;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.StatefulException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.fabric.Address;
import org.miaixz.bus.fabric.Context;
import org.miaixz.bus.fabric.Filter;
import org.miaixz.bus.fabric.Handler;
import org.miaixz.bus.fabric.Headers;
import org.miaixz.bus.fabric.Lifecycle;
import org.miaixz.bus.fabric.Listener;
import org.miaixz.bus.fabric.Message;
import org.miaixz.bus.fabric.Payload;
import org.miaixz.bus.fabric.Session;
import org.miaixz.bus.fabric.Status;
import org.miaixz.bus.fabric.codec.frame.FrameCodec;
import org.miaixz.bus.fabric.codec.frame.LineCodec;
import org.miaixz.bus.fabric.guard.GuardRule;
import org.miaixz.bus.fabric.network.Ingress;
import org.miaixz.bus.fabric.network.proxy.ProxyHeader;
import org.miaixz.bus.fabric.network.proxy.ProxyHeaderReader;
import org.miaixz.bus.fabric.network.tcp.TcpServer;
import org.miaixz.bus.fabric.observe.EventObserver;
import org.miaixz.bus.fabric.protocol.Demuxer;
import org.miaixz.bus.fabric.protocol.socket.frame.SocketCodec;
import org.miaixz.bus.fabric.runtime.Activity;
import org.miaixz.bus.fabric.runtime.FilterChain;
import org.miaixz.bus.fabric.runtime.dispatch.DispatchHandle;
import org.miaixz.bus.fabric.runtime.dispatch.Dispatcher;
import org.miaixz.bus.fabric.runtime.lifecycle.LifecycleScope;

/**
 * Socket server listener that accepts TCP sessions through the shared fabric runtime.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class SocketServer implements Lifecycle {

    /**
     * Open message tag.
     */

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
     * TCP accept server.
     */
    private volatile TcpServer tcpServer;

    /**
     * Lifecycle scope.
     */
    private final LifecycleScope lifecycle;

    /**
     * Accepted sessions.
     */
    private final Queue<SocketSession> sessions;

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
        this.frameCodec = builder.frameCodec == null ? FrameCodec.line() : builder.frameCodec;
        this.handler = builder.handler();
        this.guard = builder.guard;
        this.filter = builder.filter;
        this.observer = EventObserver.safe(builder.observer);
        this.listener = builder.listener;
        this.sessionListener = builder.sessionListener();
        this.dispatcher = context.reactor().dispatcher();
        this.lifecycle = LifecycleScope.resource(this, "socket-server", listener, observer);
        this.sessions = new ConcurrentLinkedQueue<>();
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
        if (running()) {
            return this;
        }
        if (lifecycle.state().terminal()) {
            throw new StatefulException("Socket server is already closed");
        }
        final TcpServer current = new TcpServer(address, tcpListener(), dispatcher, socketOptions);
        current.accept(acceptedHandler());
        current.start();
        tcpServer = current;
        lifecycle.open(this);
        return this;
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
        cancelHandles();
        final TcpServer current = tcpServer;
        tcpServer = null;
        if (current != null) {
            current.close();
        }
        SocketSession session = sessions.poll();
        while (session != null) {
            session.close();
            session = sessions.poll();
        }
        return lifecycle.close(this);
    }

    /**
     * Cancels this server.
     *
     * @return true when lifecycle changed
     */
    public boolean cancel() {
        cancelHandles();
        final TcpServer current = tcpServer;
        tcpServer = null;
        if (current != null) {
            current.close();
        }
        SocketSession session = sessions.poll();
        while (session != null) {
            session.cancel();
            session = sessions.poll();
        }
        return lifecycle.cancel();
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
        final TcpServer current = tcpServer;
        return lifecycle.state() == Status.OPENED && current != null && current.running();
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
        return Map.of(ATTRIBUTE_OBSERVER, observer, ATTRIBUTE_SOCKET_OPTIONS, socketOptions);
    }

    /**
     * Creates a TCP lifecycle listener.
     *
     * @return listener
     */
    private Listener<Object> tcpListener() {
        return new Listener<>() {

            @Override
            public void failure(final Object source, final Throwable cause) {
                lifecycle.fail(cause);
            }
        };
    }

    /**
     * Creates the accepted ingress handler.
     *
     * @return handler
     */
    private Handler acceptedHandler() {
        return (accepted, message) -> {
            final Object tag = message.tag();
            if (!(tag instanceof SocketChannel channel)) {
                final ProtocolException cause = new ProtocolException("TCP accept message tag must be SocketChannel");
                accepted.close();
                lifecycle.fail(cause);
                handler.failure(accepted, cause);
                return;
            }
            track(
                    dispatcher.enqueue(
                            SOCKET_ACTIVITY_ACCEPT,
                            Activity.of(SOCKET_ACTIVITY_ACCEPT, () -> handleAccepted(accepted, channel))));
        };
    }

    /**
     * Converts one accepted channel into an ingress-backed socket session.
     *
     * @param accepted accepted TCP session
     * @param channel  accepted channel
     */
    private void handleAccepted(final Session accepted, final SocketChannel channel) {
        Ingress ingress = null;
        try {
            final ProxyHeaderReader.Result proxy = ProxyHeaderReader.read(channel);
            final Address peerAddress = peerAddress(channel, proxy.header());
            ingress = Ingress.of(peerAddress, channel, proxy.payload());
            Message opening = Message
                    .of(peerAddress.protocol(), peerAddress, Headers.empty(), Payload.empty(), SOCKET_TAG_OPEN);
            opening = FilterChain.apply(opening, context.filter(), filter);
            if (guard != null) {
                guard.check(opening).throwIfRejected();
            }
            final Filter sessionFilter = FilterChain.compose(context.filter(), filter);
            final Map<String, Object> attributes = attributes(proxy.header(), sessionFilter);
            final Ingress currentIngress = ingress;
            final SocketSession session = SocketSession
                    .create(peerAddress, currentIngress, SocketCodec.of(frameCodec), Demuxer.noop(), attributes, () -> {
                        currentIngress.close();
                        accepted.close();
                    }, sessionListener, context.options().materializeMaxBytes(), socketOptions);
            ingress = null;
            sessions.add(session);
            readNext(session);
        } catch (final RuntimeException e) {
            handler.failure(accepted, e);
            if (ingress != null) {
                ingress.close();
            }
            accepted.close();
        }
    }

    /**
     * Reads and dispatches the next session message.
     *
     * @param session session
     */
    private void readNext(final SocketSession session) {
        track(
                dispatcher.enqueue(
                        SOCKET_ACTIVITY_READ,
                        Activity.of(SOCKET_ACTIVITY_READ, () -> session.receive().whenComplete((message, cause) -> {
                            if (cause != null) {
                                failSession(session, cause);
                                return;
                            }
                            track(
                                    dispatcher.enqueue(
                                            SOCKET_ACTIVITY_MESSAGE,
                                            Activity.of(SOCKET_ACTIVITY_MESSAGE, () -> dispatch(session, message))));
                        }))));
    }

    /**
     * Dispatches a received message.
     *
     * @param session session
     * @param message message
     */
    private void dispatch(final SocketSession session, final Message message) {
        try {
            handler.message(session, message);
            if (session.opened()) {
                readNext(session);
            }
        } catch (final RuntimeException e) {
            failSession(session, e);
        }
    }

    /**
     * Fails an accepted session.
     *
     * @param session session
     * @param cause   failure cause
     */
    private void failSession(final SocketSession session, final Throwable cause) {
        sessions.remove(session);
        handler.failure(session, cause);
        notifySessionFailure(session, cause);
        session.close();
    }

    /**
     * Tracks a dispatch handle until completion.
     *
     * @param handle dispatch handle
     */
    private void track(final DispatchHandle handle) {
        handles.add(handle);
        handle.future().whenComplete((ignored, cause) -> handles.remove(handle));
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
     * Builds session attributes.
     *
     * @param proxyHeader   parsed PROXY header
     * @param sessionFilter session filter
     * @return attributes
     */
    private Map<String, Object> attributes(final ProxyHeader proxyHeader, final Filter sessionFilter) {
        final java.util.LinkedHashMap<String, Object> values = new java.util.LinkedHashMap<>();
        values.put(ATTRIBUTE_OBSERVER, observer);
        values.put(ATTRIBUTE_SOCKET_OPTIONS, socketOptions);
        if (sessionFilter != null) {
            values.put(ATTRIBUTE_FILTER, sessionFilter);
        }
        if (guard != null) {
            values.put(ATTRIBUTE_GUARD, guard);
        }
        if (proxyHeader != null) {
            values.put(ATTRIBUTE_PROXY_HEADER, proxyHeader);
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
     * Notifies session failure.
     *
     * @param session session
     * @param cause   failure cause
     */
    private void notifySessionFailure(final SocketSession session, final Throwable cause) {
        try {
            sessionListener.failure(session, cause);
        } catch (final RuntimeException e) {
            lifecycle.fail(e);
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

                @Override
                public void open(final SocketSession source) {
                    openHandler.accept(source);
                }

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
                    .connectTimeout(socketOptions.connectTimeout()).idleTimeout(socketOptions.idleTimeout());
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
