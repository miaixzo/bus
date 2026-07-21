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

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.fabric.Address;
import org.miaixz.bus.fabric.Call;
import org.miaixz.bus.fabric.Callback;
import org.miaixz.bus.fabric.Context;
import org.miaixz.bus.fabric.Filter;
import org.miaixz.bus.fabric.Handler;
import org.miaixz.bus.fabric.Headers;
import org.miaixz.bus.fabric.Listener;
import org.miaixz.bus.fabric.Message;
import org.miaixz.bus.fabric.Payload;
import org.miaixz.bus.fabric.Timeout;
import org.miaixz.bus.fabric.codec.frame.FrameCodec;
import org.miaixz.bus.fabric.guard.GuardRule;
import org.miaixz.bus.fabric.network.proxy.ProxyHeader;
import org.miaixz.bus.fabric.network.tls.TlsSettings;
import org.miaixz.bus.fabric.network.tls.context.TlsContext;
import org.miaixz.bus.fabric.observe.EventObserver;
import org.miaixz.bus.fabric.protocol.Demuxer;
import org.miaixz.bus.fabric.protocol.Itinerary;
import org.miaixz.bus.fabric.protocol.Mediator;
import org.miaixz.bus.fabric.protocol.Mediator.Type;
import org.miaixz.bus.fabric.protocol.socket.calls.SocketCall;

/**
 * Immutable socket exchange.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class SocketX {

    /**
     * Immutable execution snapshot.
     */
    private final SocketSnapshot snapshot;

    /**
     * Execution runner.
     */
    private final SocketRunner runner;

    /**
     * Callback managed by the shared call lifecycle.
     */
    private final Callback<SocketSession> callback;

    /**
     * Creates an exchange.
     *
     * @param builder builder
     */
    private SocketX(final Builder builder) {
        final Context current = require(builder.context, "Context");
        final EventObserver currentObserver = builder.observer == null ? EventObserver.noop() : builder.observer;
        final TlsContext tlsContext = tlsContext(current);
        final TlsSettings tlsSettings = tlsContext == null ? null : tlsSettings(current);
        this.snapshot = new SocketSnapshot(current, builder.uri, Address.from(builder.uri), builder.headers.build(),
                builder.timeout, tlsContext, tlsSettings, builder.frameCodec, builder.handler(), builder.guard,
                builder.filter, currentObserver, builder.proxyHeader, builder.socketOptions, builder.listener,
                builder.pooled);
        this.runner = new SocketRunner(snapshot);
        this.callback = builder.callback;
    }

    /**
     * Creates a socket builder.
     *
     * @param context shared context
     * @return builder
     */
    public static Builder builder(final Context context) {
        return new Builder(require(context, "Context"));
    }

    /**
     * Returns the socket protocol.
     *
     * @return protocol
     */
    public Protocol protocol() {
        return snapshot.address().protocol();
    }

    /**
     * Returns the target address.
     *
     * @return address
     */
    public Address address() {
        return snapshot.address();
    }

    /**
     * Returns socket execution path.
     *
     * @return itinerary
     */
    public Itinerary itinerary() {
        return Itinerary.of(protocol(), address());
    }

    /**
     * Returns request headers.
     *
     * @return headers
     */
    public Headers headers() {
        return snapshot.headers();
    }

    /**
     * Returns timeout policy.
     *
     * @return timeout
     */
    public Timeout timeout() {
        return snapshot.timeout();
    }

    /**
     * Returns socket tuning options.
     *
     * @return socket options
     */
    public SocketOptions options() {
        return snapshot.socketOptions();
    }

    /**
     * Creates a protocol-neutral message from this socket exchange and payload.
     *
     * @param payload payload
     * @return message
     */
    public Message message(final Payload payload) {
        return Message.of(protocol(), address(), headers(), payload, null);
    }

    /**
     * Opens a socket session.
     *
     * @return session
     */
    public SocketSession open() {
        return call().execute();
    }

    /**
     * Executes this exchange synchronously.
     *
     * @return session
     */
    public SocketSession execute() {
        return open();
    }

    /**
     * Connects this exchange synchronously.
     *
     * @return session
     */
    public SocketSession connect() {
        return execute();
    }

    /**
     * Creates a single-use call for this exchange.
     *
     * @return socket call
     */
    public Call<SocketSession> call() {
        return SocketCall.create(
                snapshot.context().reactor().dispatcher(),
                callback,
                snapshot.observer(),
                cancellation -> Mediator.execute(Type.SOCKET, cancellation, runner::open),
                dispatchKey());
    }

    /**
     * Enqueues this exchange asynchronously.
     *
     * @return call
     */
    public Call<SocketSession> enqueue() {
        return call().enqueue();
    }

    /**
     * Builds a stable dispatch key for asynchronous opens.
     *
     * @return dispatch key
     */
    public String dispatchKey() {
        return snapshot.address().scheme() + Symbol.COLON + Symbol.SLASH + Symbol.SLASH + snapshot.address().host()
                + Symbol.C_COLON + snapshot.address().port();
    }

    /**
     * Validates required references.
     *
     * @param value value
     * @param name  field name
     * @param <T>   type
     * @return value
     */
    private static <T> T require(final T value, final String name) {
        return Assert.notNull(value, () -> new ValidateException(name + " must not be null"));
    }

    /**
     * Resolves the socket TLS context from typed options.
     *
     * @param context shared context
     * @return TLS context or null when explicitly disabled
     */
    private static TlsContext tlsContext(final Context context) {
        if (context.options().contains(org.miaixz.bus.fabric.Builder.OPTION_SOCKET_TLS_CONTEXT)) {
            return context.options().get(org.miaixz.bus.fabric.Builder.OPTION_SOCKET_TLS_CONTEXT);
        }
        final TlsContext configured = context.options().get(org.miaixz.bus.fabric.Builder.OPTION_TLS_CONTEXT);
        return configured == null ? TlsContext.defaults() : configured;
    }

    /**
     * Resolves socket TLS settings from typed options.
     *
     * @param context shared context
     * @return TLS settings
     */
    private static TlsSettings tlsSettings(final Context context) {
        if (context.options().contains(org.miaixz.bus.fabric.Builder.OPTION_SOCKET_TLS_SETTINGS)) {
            final TlsSettings configured = context.options()
                    .get(org.miaixz.bus.fabric.Builder.OPTION_SOCKET_TLS_SETTINGS);
            return configured == null ? TlsSettings.defaults() : configured;
        }
        final TlsSettings configured = context.options().get(org.miaixz.bus.fabric.Builder.OPTION_TLS_SETTINGS);
        return configured == null ? TlsSettings.defaults() : configured;
    }

    /**
     * Parses a target URI.
     *
     * @param value target
     * @return URI
     */
    private static URI parseTarget(final String value) {
        if (StringKit.isBlank(value) || StringKit.containsAny(value, Symbol.C_CR, Symbol.C_LF)) {
            throw new ValidateException("Socket URL must be non-blank and single-line");
        }
        try {
            final URI parsed = new URI(value.trim());
            final String scheme = parsed.getScheme();
            if (!Protocol.TCP.name.equalsIgnoreCase(scheme) && !Protocol.UDP.name.equalsIgnoreCase(scheme)
                    && !Protocol.TLS.name.equalsIgnoreCase(scheme)
                    && !org.miaixz.bus.fabric.Builder.SOCKET_X_KCP_SCHEME.equalsIgnoreCase(scheme)
                    && !Protocol.SOCKET.name.equalsIgnoreCase(scheme)
                    && !org.miaixz.bus.fabric.Builder.AIO_SCHEME.equalsIgnoreCase(scheme)) {
                throw new ProtocolException("Socket URL must use tcp, tls, udp, kcp, socket, or aio");
            }
            Address.from(parsed);
            return parsed;
        } catch (final URISyntaxException e) {
            throw new ProtocolException("Invalid socket URL", e);
        }
    }

    /**
     * Builds a socket URI from transport parts.
     *
     * @param scheme scheme
     * @param host   host
     * @param port   port
     * @return URI string
     */
    private static String target(final String scheme, final String host, final int port) {
        if (StringKit.isBlank(host) || StringKit.containsAny(host, Symbol.C_CR, Symbol.C_LF)) {
            throw new ValidateException("Socket host must be non-blank and single-line");
        }
        if (port < Normal._1 || port > Normal._65535) {
            throw new ValidateException("Socket port must be between 1 and 65535");
        }
        final String current = host.trim();
        final String authority = current.indexOf(Symbol.C_COLON) >= 0 && !current.startsWith(Symbol.BRACKET_LEFT)
                ? Symbol.BRACKET_LEFT + current + Symbol.BRACKET_RIGHT
                : current;
        return scheme + Symbol.COLON + Symbol.SLASH + Symbol.SLASH + authority + Symbol.C_COLON + port;
    }

    /**
     * Validates a duration.
     *
     * @param value duration
     * @return duration
     */
    private static Duration validateDuration(final Duration value) {
        final Duration checked = Assert
                .notNull(value, () -> new ValidateException("Timeout must be non-null and non-negative"));
        Assert.isTrue(!checked.isNegative(), () -> new ValidateException("Timeout must be non-null and non-negative"));
        return checked;
    }

    /**
     * Returns whether a context options map contains socket-specific keys.
     *
     * @param options options
     * @return true when socket-specific keys exist
     */
    private static boolean hasSocketOptions(final org.miaixz.bus.fabric.Options options) {
        return options.contains(org.miaixz.bus.fabric.Builder.OPTION_SOCKET_READ_BUFFER_SIZE)
                || options.contains(org.miaixz.bus.fabric.Builder.OPTION_SOCKET_WRITE_CHUNK_SIZE)
                || options.contains(org.miaixz.bus.fabric.Builder.OPTION_SOCKET_WRITE_CHUNK_COUNT)
                || options.contains(org.miaixz.bus.fabric.Builder.OPTION_SOCKET_IO_THREADS)
                || options.contains(org.miaixz.bus.fabric.Builder.OPTION_SOCKET_OPTIONS)
                || options.contains(org.miaixz.bus.fabric.Builder.OPTION_SOCKET_RETAIN_READ_BUFFER)
                || options.contains(org.miaixz.bus.fabric.Builder.OPTION_SOCKET_IDLE_TIMEOUT);
    }

    /**
     * Socket exchange builder.
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
         * URI.
         */
        private URI uri;

        /**
         * Headers builder.
         */
        private Headers.Builder headers;

        /**
         * Timeout policy.
         */
        private Timeout timeout;

        /**
         * Socket tuning options.
         */
        private SocketOptions socketOptions;

        /**
         * Frame codec.
         */
        private FrameCodec frameCodec;

        /**
         * Handler.
         */
        private Handler handler;

        /**
         * Optional demuxer builder.
         */
        private Demuxer.Builder demuxer;

        /**
         * Guard.
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
         * Parsed PROXY protocol metadata.
         */
        private ProxyHeader proxyHeader;

        /**
         * Callback.
         */
        private Callback<SocketSession> callback;

        /**
         * Session lifecycle listener.
         */
        private Listener<? super SocketSession> listener;

        /**
         * Whether TCP sessions use the shared connection pool.
         */
        private boolean pooled;

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
            this.headers = Headers.builder();
            final Timeout configured = context.options().get(org.miaixz.bus.fabric.Builder.OPTION_TIMEOUT);
            this.timeout = configured == null ? Timeout.defaults() : configured;
            this.socketOptions = hasSocketOptions(context.options()) ? SocketOptions.from(context.options())
                    : SocketOptions.defaults();
            this.frameCodec = FrameCodec.line();
            this.handler = Demuxer.noop();
            this.observer = EventObserver.noop();
            this.callback = null;
            this.listener = null;
            this.openHandler = session -> {
            };
            this.errorHandler = cause -> {
            };
        }

        /**
         * Sets target.
         *
         * @param url URL
         * @return this builder
         */
        public Builder to(final String url) {
            this.uri = parseTarget(url);
            return this;
        }

        /**
         * Sets target URL.
         *
         * @param url URL
         * @return this builder
         */
        public Builder url(final String url) {
            return to(url);
        }

        /**
         * Sets a TCP target.
         *
         * @param host host
         * @param port port
         * @return this builder
         */
        public Builder tcp(final String host, final int port) {
            return to(target(Protocol.TCP.name, host, port));
        }

        /**
         * Sets a TLS-over-TCP target.
         *
         * @param host host
         * @param port port
         * @return this builder
         */
        public Builder tls(final String host, final int port) {
            return to(target(Protocol.TLS.name, host, port));
        }

        /**
         * Sets a UDP target.
         *
         * @param host host
         * @param port port
         * @return this builder
         */
        public Builder udp(final String host, final int port) {
            return to(target(Protocol.UDP.name, host, port));
        }

        /**
         * Sets a KCP target.
         *
         * @param host host
         * @param port port
         * @return this builder
         */
        public Builder kcp(final String host, final int port) {
            return to(target(org.miaixz.bus.fabric.Builder.SOCKET_X_KCP_SCHEME, host, port));
        }

        /**
         * Appends a header.
         *
         * @param name  name
         * @param value value
         * @return this builder
         */
        public Builder header(final String name, final String value) {
            headers.add(name, value);
            return this;
        }

        /**
         * Merges headers.
         *
         * @param headers headers
         * @return this builder
         */
        public Builder headers(final Headers headers) {
            require(headers, "Headers");
            for (final Map.Entry<String, List<String>> entry : headers.asMap().entrySet()) {
                for (final String value : entry.getValue()) {
                    this.headers.add(entry.getKey(), value);
                }
            }
            return this;
        }

        /**
         * Sets timeout.
         *
         * @param timeout timeout
         * @return this builder
         */
        public Builder timeout(final Duration timeout) {
            this.timeout = Timeout.of(validateDuration(timeout));
            return this;
        }

        /**
         * Sets timeout policy.
         *
         * @param timeout timeout policy
         * @return this builder
         */
        public Builder timeout(final Timeout timeout) {
            this.timeout = require(timeout, "Timeout");
            return this;
        }

        /**
         * Sets all socket tuning options.
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
         * @param size read buffer size
         * @return this builder
         */
        public Builder readBufferSize(final int size) {
            return socketOptions(copySocketOptions().readBufferSize(size).build());
        }

        /**
         * Sets write chunk size.
         *
         * @param size write chunk size
         * @return this builder
         */
        public Builder writeChunkSize(final int size) {
            return socketOptions(copySocketOptions().writeChunkSize(size).build());
        }

        /**
         * Sets write chunk count hint.
         *
         * @param count write chunk count
         * @return this builder
         */
        public Builder writeChunkCount(final int count) {
            return socketOptions(copySocketOptions().writeChunkCount(count).build());
        }

        /**
         * Sets AIO read I/O thread count.
         *
         * @param ioThreads I/O thread count
         * @return this builder
         */
        public Builder ioThreads(final int ioThreads) {
            return socketOptions(copySocketOptions().ioThreads(ioThreads).build());
        }

        /**
         * Adds one JDK socket option.
         *
         * @param option socket option
         * @param value  option value
         * @param <T>    value type
         * @return this builder
         */
        public <T> Builder socketOption(final java.net.SocketOption<T> option, final T value) {
            return socketOptions(copySocketOptions().socketOption(option, value).build());
        }

        /**
         * Sets whether a read buffer is retained by each session.
         *
         * @param retain true to retain
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
            return socketOptions(copySocketOptions().idleTimeout(validateDuration(timeout)).build());
        }

        /**
         * Sets the KCP wire-format version retained in the socket option snapshot.
         *
         * @param version wire-format version, either {@code 1} or {@code 2}
         * @return this builder
         */
        public Builder kcpWireVersion(final int version) {
            return socketOptions(copySocketOptions().kcpWireVersion(version).build());
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
         * Uses the default LF-delimited frame codec.
         *
         * @return this builder
         */
        public Builder lineFrame() {
            return frame(FrameCodec.line());
        }

        /**
         * Uses a delimiter-based frame codec.
         *
         * @param delimiter frame delimiter
         * @return this builder
         */
        public Builder delimiterFrame(final byte[] delimiter) {
            return frame(org.miaixz.bus.fabric.codec.frame.LineCodec.of(delimiter));
        }

        /**
         * Uses a fixed-length frame codec.
         *
         * @param length frame length
         * @return this builder
         */
        public Builder fixedFrame(final int length) {
            return frame(FrameCodec.length(length));
        }

        /**
         * Uses the default length-field frame codec.
         *
         * @return this builder
         */
        public Builder lengthFieldFrame() {
            return frame(FrameCodec.lengthField());
        }

        /**
         * Uses an unframed raw byte codec.
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
         * Registers a channel message handler.
         *
         * @param channel channel id
         * @param handler handler
         * @return this builder
         */
        public Builder channel(final String channel, final Handler handler) {
            demuxer().channel(channel, handler);
            return this;
        }

        /**
         * Sets fallback message handler for unmatched channels.
         *
         * @param handler fallback handler
         * @return this builder
         */
        public Builder fallback(final Handler handler) {
            demuxer().fallback(handler);
            return this;
        }

        /**
         * Sets the header used for channel lookup.
         *
         * @param name header name
         * @return this builder
         */
        public Builder channelHeader(final String name) {
            demuxer().header(name);
            return this;
        }

        /**
         * Sets a custom message channel resolver.
         *
         * @param resolver resolver
         * @return this builder
         */
        public Builder resolver(final Function<Message, String> resolver) {
            demuxer().resolver(resolver);
            return this;
        }

        /**
         * Sets a UTF-8 text message handler.
         *
         * @param handler text handler
         * @return this builder
         */
        public Builder onText(final Consumer<String> handler) {
            this.demuxer = null;
            if (handler == null) {
                this.handler = Demuxer.noop();
            } else {
                this.handler = (session, message) -> handler.accept(message.payload().text(StandardCharsets.UTF_8));
            }
            return this;
        }

        /**
         * Sets open handler.
         *
         * @param handler open handler
         * @return this builder
         */
        public Builder onOpen(final Consumer<SocketSession> handler) {
            this.openHandler = handler == null ? session -> {
            } : handler;
            return composeCallback();
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
            return composeCallback();
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
         * Sets message filter.
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
         * Attaches parsed PROXY protocol metadata to the session context.
         *
         * @param proxyHeader proxy metadata
         * @return this builder
         */
        public Builder proxyHeader(final ProxyHeader proxyHeader) {
            this.proxyHeader = proxyHeader;
            return this;
        }

        /**
         * Parses and attaches PROXY protocol metadata to the session context.
         *
         * @param line PROXY protocol v1 line
         * @return this builder
         */
        public Builder proxyHeader(final String line) {
            return proxyHeader(ProxyHeader.parse(line));
        }

        /**
         * Sets callback.
         *
         * @param callback callback
         * @return this builder
         */
        public Builder callback(final Callback<SocketSession> callback) {
            this.callback = callback;
            return this;
        }

        /**
         * Sets lifecycle listener.
         *
         * @param listener lifecycle listener
         * @return this builder
         */
        public Builder listener(final Listener<? super SocketSession> listener) {
            this.listener = listener;
            return this;
        }

        /**
         * Enables shared TCP connection pooling for this socket exchange.
         *
         * @return this builder
         */
        public Builder pooled() {
            this.pooled = true;
            return this;
        }

        /**
         * Builds an exchange.
         *
         * @return exchange
         */
        public SocketX build() {
            if (uri == null) {
                throw new ValidateException("Socket target must be set");
            }
            return new SocketX(this);
        }

        /**
         * Opens a built exchange.
         *
         * @return session
         */
        public SocketSession open() {
            return build().open();
        }

        /**
         * Connects a built exchange.
         *
         * @return session
         */
        public SocketSession connect() {
            return open();
        }

        /**
         * Executes a built exchange.
         *
         * @return session
         */
        public SocketSession execute() {
            return build().execute();
        }

        /**
         * Creates a call for a built exchange.
         *
         * @return socket call
         */
        public Call<SocketSession> call() {
            return build().call();
        }

        /**
         * Enqueues a built exchange asynchronously.
         *
         * @return call
         */
        public Call<SocketSession> enqueue() {
            return build().enqueue();
        }

        /**
         * Composes open and error handlers into a callback.
         *
         * @return this builder
         */
        private Builder composeCallback() {
            this.callback = new Callback<>() {

                /**
                 * Forwards a successful open session to the configured open handler.
                 *
                 * @param value opened socket session
                 */
                @Override
                public void success(final SocketSession value) {
                    openHandler.accept(value);
                }

                /**
                 * Forwards an open failure to the configured error handler.
                 *
                 * @param cause failure cause
                 */
                @Override
                public void failure(final Throwable cause) {
                    errorHandler.accept(cause);
                }
            };
            return this;
        }

        /**
         * Returns the configured handler.
         *
         * @return handler
         */
        private Handler handler() {
            if (demuxer != null) {
                return demuxer.build();
            }
            return handler == null ? Demuxer.noop() : handler;
        }

        /**
         * Returns the demuxer builder.
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
         * Copies current socket options into a builder.
         *
         * @return copied builder
         */
        private SocketOptions.Builder copySocketOptions() {
            return SocketOptions.builder().readBufferSize(socketOptions.readBufferSize())
                    .writeChunkSize(socketOptions.writeChunkSize()).writeChunkCount(socketOptions.writeChunkCount())
                    .backlog(socketOptions.backlog()).ioThreads(socketOptions.ioThreads())
                    .socketOptions(socketOptions.socketOptions()).retainReadBuffer(socketOptions.retainReadBuffer())
                    .idleTimeout(socketOptions.idleTimeout()).kcpWireVersion(socketOptions.kcpWireVersion());
        }

    }

}
