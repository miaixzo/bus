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

import java.net.SocketOption;
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
import org.miaixz.bus.fabric.*;
import org.miaixz.bus.fabric.codec.frame.FrameCodec;
import org.miaixz.bus.fabric.codec.frame.LineCodec;
import org.miaixz.bus.fabric.guard.GuardRule;
import org.miaixz.bus.fabric.network.proxy.ProxyHeader;
import org.miaixz.bus.fabric.network.proxy.ProxyPlan;
import org.miaixz.bus.fabric.network.tls.TlsPolicy;
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
 */
public final class SocketX {

    /**
     * Immutable execution specification.
     */
    private final SocketSpec spec;

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
     * @param builder configuration source used to create the immutable exchange specification
     */
    private SocketX(final Builder builder) {
        final Context current = require(builder.context, "Context");
        final EventObserver currentObserver = builder.observer == null ? EventObserver.noop() : builder.observer;
        final TlsPolicy tlsPolicy = tlsPolicy(current);
        this.spec = new SocketSpec(current, builder.uri, Address.from(builder.uri), builder.headers.build(),
                builder.timeout, tlsPolicy.context(), tlsPolicy.settings(), builder.frameCodec, builder.handler(),
                builder.guard, builder.filter, currentObserver, builder.proxy, builder.proxyHeader,
                builder.socketOptions, builder.listener, builder.pooled);
        this.runner = new SocketRunner(spec);
        this.callback = builder.callback;
    }

    /**
     * Creates a socket builder.
     *
     * @param context shared context
     * @return new socket exchange builder bound to the context
     */
    public static Builder builder(final Context context) {
        return new Builder(require(context, "Context"));
    }

    /**
     * Returns the socket protocol.
     *
     * @return transport protocol derived from the target address
     */
    public Protocol protocol() {
        return spec.address().protocol();
    }

    /**
     * Returns the target address.
     *
     * @return immutable target address
     */
    public Address address() {
        return spec.address();
    }

    /**
     * Returns socket execution path.
     *
     * @return execution itinerary containing the socket protocol and target address
     */
    public Itinerary itinerary() {
        return Itinerary.of(protocol(), address());
    }

    /**
     * Returns request headers.
     *
     * @return immutable headers sent when the socket is opened
     */
    public Headers headers() {
        return spec.headers();
    }

    /**
     * Returns timeout policy.
     *
     * @return timeout policy captured by this exchange
     */
    public Timeout timeout() {
        return spec.timeout();
    }

    /**
     * Returns socket tuning options.
     *
     * @return socket tuning options captured by this exchange
     */
    public SocketOptions options() {
        return spec.socketOptions();
    }

    /**
     * Creates a protocol-neutral message from this socket exchange and payload.
     *
     * @param payload payload to attach to the protocol-neutral message
     * @return message representing this exchange and the supplied payload
     */
    public Message message(final Payload payload) {
        return Message.of(protocol(), address(), headers(), payload, null);
    }

    /**
     * Opens a socket session.
     *
     * @return opened socket session
     */
    public SocketSession open() {
        return call().execute();
    }

    /**
     * Executes this exchange synchronously.
     *
     * @return socket session produced by synchronous execution
     */
    public SocketSession execute() {
        return open();
    }

    /**
     * Connects this exchange synchronously.
     *
     * @return connected socket session
     */
    public SocketSession connect() {
        return execute();
    }

    /**
     * Creates a single-use call for this exchange.
     *
     * @return new single-use call that opens this exchange
     */
    public Call<SocketSession> call() {
        return SocketCall.create(
                spec.context().reactor().dispatcher(),
                callback,
                spec.observer(),
                spec.timeout(),
                cancellation -> Mediator.execute(Type.SOCKET, cancellation, runner::open),
                dispatchKey());
    }

    /**
     * Enqueues this exchange asynchronously.
     *
     * @return enqueued call for this exchange
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
        return spec.address().scheme() + Symbol.COLON + Symbol.SLASH + Symbol.SLASH + spec.address().host()
                + Symbol.C_COLON + spec.address().port();
    }

    /**
     * Validates required references.
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
     * Resolves the complete Socket-specific or generic TLS policy.
     *
     * @param context shared context
     * @return configured or shared default TLS policy
     */
    private static TlsPolicy tlsPolicy(final Context context) {
        final TlsPolicy configured = context.options().get(SocketOptions.TLS_POLICY);
        return configured == null ? TlsPolicy.resolve(context.options()) : configured;
    }

    /**
     * Parses a target URI.
     *
     * @param value raw socket target URL
     * @return validated socket target URI
     */
    private static URI parseTarget(final String value) {
        if (StringKit.isBlank(value) || StringKit.containsAny(value, Symbol.C_CR, Symbol.C_LF)) {
            throw new ValidateException("Socket URL must be non-blank and single-line");
        }
        try {
            final URI parsed = new URI(value.trim());
            final String scheme = parsed.getScheme();
            if (!Protocol.TCP.name.equalsIgnoreCase(scheme) && !Protocol.UDP.name.equalsIgnoreCase(scheme)
                    && !Protocol.TLS.name.equalsIgnoreCase(scheme) && !SOCKET_X_KCP_SCHEME.equalsIgnoreCase(scheme)
                    && !Protocol.SOCKET.name.equalsIgnoreCase(scheme) && !AIO_SCHEME.equalsIgnoreCase(scheme)) {
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
     * @param scheme supported socket transport scheme
     * @param host   remote target host name or IP address
     * @param port   remote target port
     * @return socket target URL assembled from the transport parts
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
     * @param value timeout duration to validate
     * @return the supplied duration after null and range validation
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
     * @param options context option snapshot to inspect
     * @return true when socket-specific keys exist
     */
    private static boolean hasSocketOptions(final Options options) {
        return options.contains(OPTION_SOCKET_READ_BUFFER_SIZE) || options.contains(OPTION_SOCKET_WRITE_CHUNK_SIZE)
                || options.contains(OPTION_SOCKET_WRITE_CHUNK_COUNT) || options.contains(OPTION_SOCKET_IO_THREADS)
                || options.contains(OPTION_SOCKET_OPTIONS) || options.contains(OPTION_SOCKET_RETAIN_READ_BUFFER)
                || options.contains(OPTION_SOCKET_IDLE_TIMEOUT);
    }

    /**
     * Socket exchange builder.
     *
     * @author Kimi Liu
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
         * Outbound proxy policy, independent from inbound PROXY protocol metadata.
         */
        private ProxyPlan proxy;

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
            final Timeout configured = context.options().get(OPTION_TIMEOUT);
            this.timeout = configured == null ? Timeout.defaults() : configured;
            this.socketOptions = hasSocketOptions(context.options()) ? SocketOptions.from(context.options())
                    : SocketOptions.defaults();
            this.frameCodec = FrameCodec.line();
            this.handler = Demuxer.noop();
            this.observer = EventObserver.noop();
            this.proxy = ProxyPlan.inherit();
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
         * @param url raw socket target URL
         * @return this builder
         */
        public Builder to(final String url) {
            this.uri = parseTarget(url);
            return this;
        }

        /**
         * Sets target URL.
         *
         * @param url raw socket target URL forwarded to {@link #to(String)}
         * @return this builder
         */
        public Builder url(final String url) {
            return to(url);
        }

        /**
         * Sets a TCP target.
         *
         * @param host remote TCP host name or IP address
         * @param port remote TCP port
         * @return this builder
         */
        public Builder tcp(final String host, final int port) {
            return to(target(Protocol.TCP.name, host, port));
        }

        /**
         * Sets a TLS-over-TCP target.
         *
         * @param host remote TLS host name or IP address
         * @param port remote TLS port
         * @return this builder
         */
        public Builder tls(final String host, final int port) {
            return to(target(Protocol.TLS.name, host, port));
        }

        /**
         * Sets a UDP target.
         *
         * @param host remote UDP host name or IP address
         * @param port remote UDP port
         * @return this builder
         */
        public Builder udp(final String host, final int port) {
            return to(target(Protocol.UDP.name, host, port));
        }

        /**
         * Sets a KCP target.
         *
         * @param host remote KCP host name or IP address
         * @param port remote KCP port
         * @return this builder
         */
        public Builder kcp(final String host, final int port) {
            return to(target(SOCKET_X_KCP_SCHEME, host, port));
        }

        /**
         * Appends a header.
         *
         * @param name  socket opening header name
         * @param value socket opening header value
         * @return this builder
         */
        public Builder header(final String name, final String value) {
            headers.add(name, value);
            return this;
        }

        /**
         * Merges headers.
         *
         * @param headers header collection whose values are appended to this builder
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
         * @param timeout non-negative duration assigned to every timeout phase
         * @return this builder
         */
        public Builder timeout(final Duration timeout) {
            this.timeout = Timeout.of(validateDuration(timeout));
            return this;
        }

        /**
         * Sets timeout policy.
         *
         * @param timeout complete timeout policy for socket operations
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
        public <T> Builder socketOption(final SocketOption<T> option, final T value) {
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
         * @param codec codec used to delimit inbound and outbound messages
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
            return frame(LineCodec.of(delimiter));
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
         * @param handler message handler, or {@code null} to install a no-op handler
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
         * @param channel channel identifier selected by the demultiplexer
         * @param handler handler invoked for messages on the channel
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
         * @param resolver function that resolves a channel identifier from each message
         * @return this builder
         */
        public Builder resolver(final Function<Message, String> resolver) {
            demuxer().resolver(resolver);
            return this;
        }

        /**
         * Sets a UTF-8 text message handler.
         *
         * @param handler UTF-8 text consumer, or {@code null} to install a no-op handler
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
         * @param handler consumer invoked after a session opens, or {@code null} for no action
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
         * @param handler consumer invoked when opening fails, or {@code null} for no action
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
         * @param guard rule evaluated for socket messages
         * @return this builder
         */
        public Builder guard(final GuardRule guard) {
            this.guard = guard;
            return this;
        }

        /**
         * Sets message filter.
         *
         * @param filter filter applied to socket messages
         * @return this builder
         */
        public Builder filter(final Filter filter) {
            this.filter = filter;
            return this;
        }

        /**
         * Sets observer.
         *
         * @param observer event observer, or {@code null} to disable observation
         * @return this builder
         */
        public Builder observe(final EventObserver observer) {
            this.observer = observer == null ? EventObserver.noop() : observer;
            return this;
        }

        /**
         * Sets the outbound network proxy policy.
         *
         * @param proxy non-null inherited, system, direct, HTTP, or SOCKS policy
         * @return this builder
         */
        public Builder proxy(final ProxyPlan proxy) {
            this.proxy = require(proxy, "Proxy plan");
            return this;
        }

        /**
         * Inherits the context-level network proxy policy.
         *
         * @return this builder
         */
        public Builder inheritProxy() {
            return proxy(ProxyPlan.inherit());
        }

        /**
         * Forces selection through the system proxy selector.
         *
         * @return this builder
         */
        public Builder systemProxy() {
            return proxy(ProxyPlan.system());
        }

        /**
         * Forces a direct connection and bypasses every configured selector.
         *
         * @return this builder
         */
        public Builder directProxy() {
            return proxy(ProxyPlan.direct());
        }

        /**
         * Routes the stream through an HTTP CONNECT proxy.
         *
         * @param proxy plain HTTP proxy address used for TCP and TLS streams
         * @return this builder
         */
        public Builder httpProxy(final Address proxy) {
            return proxy(ProxyPlan.http(require(proxy, "HTTP proxy address")));
        }

        /**
         * Routes the stream or datagram through a SOCKS5 proxy.
         *
         * @param proxy plain stream address of the SOCKS5 server
         * @return this builder
         */
        public Builder socksProxy(final Address proxy) {
            return proxy(ProxyPlan.socks(require(proxy, "SOCKS proxy address")));
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
         * @param callback callback notified when asynchronous opening succeeds or fails
         * @return this builder
         */
        public Builder callback(final Callback<SocketSession> callback) {
            this.callback = callback;
            return this;
        }

        /**
         * Sets lifecycle listener.
         *
         * @param listener listener notified of socket session lifecycle events
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
         * @return immutable socket exchange built from the current configuration
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
         * @return opened socket session
         */
        public SocketSession open() {
            return build().open();
        }

        /**
         * Connects a built exchange.
         *
         * @return connected socket session
         */
        public SocketSession connect() {
            return open();
        }

        /**
         * Executes a built exchange.
         *
         * @return socket session produced by synchronous execution
         */
        public SocketSession execute() {
            return build().execute();
        }

        /**
         * Creates a call for a built exchange.
         *
         * @return new single-use call for the built exchange
         */
        public Call<SocketSession> call() {
            return build().call();
        }

        /**
         * Enqueues a built exchange asynchronously.
         *
         * @return enqueued call for the built exchange
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
         * @return configured direct handler or the assembled channel demultiplexer
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
         * @return lazily initialized channel demultiplexer builder
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
         * @return mutable builder initialized from the current socket option snapshot
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
