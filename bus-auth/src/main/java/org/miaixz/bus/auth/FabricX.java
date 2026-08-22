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
package org.miaixz.bus.auth;

import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.miaixz.bus.auth.shared.SecurityBaseline;
import org.miaixz.bus.core.io.ByteString;
import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.TimeoutException;
import org.miaixz.bus.core.net.Http.Method;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.fabric.Call;
import org.miaixz.bus.fabric.Fabric;
import org.miaixz.bus.fabric.Message;
import org.miaixz.bus.fabric.Payload;
import org.miaixz.bus.fabric.UnoUrl;
import org.miaixz.bus.fabric.codec.frame.Frame;
import org.miaixz.bus.fabric.codec.frame.FrameCodec;
import org.miaixz.bus.fabric.network.tls.TlsPolicy;
import org.miaixz.bus.fabric.protocol.http.HttpRequest;
import org.miaixz.bus.fabric.protocol.http.HttpResponse;
import org.miaixz.bus.fabric.protocol.http.HttpX;
import org.miaixz.bus.fabric.protocol.http.auth.HttpAuth;
import org.miaixz.bus.fabric.protocol.http.body.PayloadBody;
import org.miaixz.bus.fabric.protocol.http.cache.HttpCacheControl;
import org.miaixz.bus.fabric.protocol.socket.SocketOptions;
import org.miaixz.bus.fabric.protocol.socket.SocketSession;
import org.miaixz.bus.fabric.protocol.socket.SocketX;

/**
 * Provides the runtime-scoped Fabric entry used by authentication protocols and Vendor Sources.
 * <p>
 * This class is the only bus-auth type allowed to create or operate Fabric transports and access Fabric runtime
 * services. HTTP callers receive a policy-bound builder because the exact HTTP message belongs to the calling
 * authentication protocol. Socket callers receive the authentication-owned {@link Socket} facade and therefore never
 * handle Fabric calls, sessions, messages, payloads, or TLS policies directly.
 * </p>
 * <p>
 * This class does not encode protocol messages, select credentials, parse responses, map protocol errors, load project
 * data, dispatch Source capabilities, or expose application HTTP routes.
 * </p>
 *
 * @author Kimi Liu
 */
public class FabricX {

    /**
     * Fabric execution context created and owned by this authentication Runtime facade.
     */
    private final org.miaixz.bus.fabric.Context context;

    /**
     * Immutable non-relaxable security policy applied to outbound authentication requests.
     */
    private final SecurityBaseline securityBaseline;

    /**
     * Creates a validated immutable Runtime facade from one completed builder.
     *
     * @param builder completed FabricX builder
     * @throws IllegalArgumentException if the security baseline is {@code null}
     */
    public FabricX(final Builder builder) {
        final Builder source = Assert.notNull(builder, "FabricX builder must not be null");
        this.context = org.miaixz.bus.fabric.Context.create();
        this.securityBaseline = Assert
                .notNull(source.securityBaseline, "Authentication security baseline must not be null");
    }

    /**
     * Creates an empty builder for one Runtime-scoped authentication Fabric facade.
     *
     * @return new mutable builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates one guarded outbound HTTP request builder for the selected authentication protocol.
     * <p>
     * The caller retains responsibility for the exact URL, method, headers, query, body, credential use, response
     * parsing, protocol error mapping, and response lifecycle. Every invocation returns a new builder and never mutates
     * Runtime or Source state.
     * </p>
     *
     * @param fabric   Runtime-scoped authentication Fabric facade
     * @param protocol actual authentication protocol governing the remote endpoint
     * @param timeout  remaining end-to-end operation timeout
     * @return fresh Fabric HTTP builder with timeout and address policy already applied
     * @throws IllegalArgumentException if an argument is {@code null}
     * @throws TimeoutException         if the operation timeout has already expired
     */
    public static Http http(final FabricX fabric, final Protocol protocol, final Timeout timeout) {
        final FabricX runtime = Assert.notNull(fabric, "Authentication Fabric facade must not be null");
        final Protocol selected = Assert.notNull(protocol, "Authentication HTTP protocol must not be null");
        final Timeout remaining = Assert.notNull(timeout, "Authentication HTTP timeout must not be null");
        if (remaining.expired()) {
            throw new TimeoutException("Authentication HTTP timeout has expired");
        }
        return new Http(Fabric.http(runtime.context).timeout(transportTimeout(remaining))
                .addressPolicy(runtime.securityBaseline.require(selected).addressPolicy().unwrap()));
    }

    /**
     * Creates one internal timeout-bound Fabric socket builder.
     *
     * @param fabric  Runtime-scoped authentication Fabric facade
     * @param timeout remaining end-to-end operation timeout
     * @return internal Fabric socket builder with the remaining timeout already applied
     * @throws IllegalArgumentException if an argument is {@code null}
     * @throws TimeoutException         if the operation timeout has already expired
     */
    private static SocketX.Builder socketBuilder(final FabricX fabric, final Timeout timeout) {
        final FabricX runtime = Assert.notNull(fabric, "Authentication Fabric facade must not be null");
        final Timeout remaining = Assert.notNull(timeout, "Authentication socket timeout must not be null");
        if (remaining.expired()) {
            throw new TimeoutException("Authentication socket timeout has expired");
        }
        return Fabric.socket(runtime.context).timeout(transportTimeout(remaining));
    }

    /**
     * Converts the remaining authentication timeout into the internal Fabric transport representation.
     *
     * @param timeout remaining authentication operation timeout
     * @return internal Fabric timeout
     */
    private static org.miaixz.bus.fabric.Timeout transportTimeout(final Timeout timeout) {
        final Timeout.Settings settings = Assert.notNull(timeout, "Authentication timeout must not be null")
                .effective();
        return new org.miaixz.bus.fabric.Timeout(settings.connect(), settings.read(), settings.write(), settings.call(),
                settings.ping(), settings.close());
    }

    /**
     * Opens one timeout-bound socket hidden behind the authentication-owned session facade.
     * <p>
     * The frame codec is supplied by the protocol implementation, while FabricX exclusively owns transport selection,
     * Fabric call execution, session lifecycle, payload conversion, and any later TLS upgrade.
     * </p>
     *
     * @param fabric  Runtime-scoped authentication Fabric facade
     * @param timeout remaining end-to-end operation timeout
     * @param framer  authentication protocol stream framer
     * @param host    remote host
     * @param port    remote port
     * @param secure  {@code true} to establish TLS immediately; {@code false} to establish plaintext TCP
     * @return opened authentication-owned socket facade
     * @throws IllegalArgumentException if an argument is invalid
     * @throws TimeoutException         if the operation timeout has already expired
     */
    public static Socket socket(
            final FabricX fabric,
            final Timeout timeout,
            final Framer framer,
            final String host,
            final int port,
            final boolean secure) {
        final SocketX.Builder builder = socketBuilder(fabric, timeout)
                .frame(new FrameCodecAdapter(Assert.notNull(framer, "Authentication socket framer must not be null")));
        if (secure) {
            builder.tls(Assert.notBlank(host, "Authentication socket host must not be blank"), port);
        } else {
            builder.tcp(Assert.notBlank(host, "Authentication socket host must not be blank"), port);
        }
        return new Socket(await(builder.build().call(), timeout), fabric);
    }

    /**
     * Returns the Fabric Runtime clock shared by every protocol in this authentication runtime.
     *
     * @param fabric Runtime-scoped authentication Fabric facade
     * @return shared monotonic-aware Runtime clock
     */
    public static Clock clock(final FabricX fabric) {
        return new Clock(Assert.notNull(fabric, "Authentication Fabric facade must not be null").context.clock());
    }

    /**
     * Returns the immutable security baseline bound to this Runtime-scoped Fabric facade.
     *
     * @param fabric Runtime-scoped authentication Fabric facade
     * @return non-relaxable authentication security baseline
     */
    public static SecurityBaseline securityBaseline(final FabricX fabric) {
        return Assert.notNull(fabric, "Authentication Fabric facade must not be null").securityBaseline;
    }

    /**
     * Resolves the TLS policy inherited by socket authentication protocols.
     *
     * @param fabric Runtime-scoped authentication Fabric facade
     * @return explicit socket TLS policy or the policy resolved from the Runtime options
     */
    private static TlsPolicy socketTlsPolicy(final FabricX fabric) {
        final FabricX runtime = Assert.notNull(fabric, "Authentication Fabric facade must not be null");
        final TlsPolicy policy = runtime.context.options().get(SocketOptions.TLS_POLICY);
        return policy == null ? TlsPolicy.resolve(runtime.context.options()) : policy;
    }

    /**
     * Waits for one Fabric operation within the remaining authentication timeout.
     *
     * @param call    single-use Fabric call
     * @param timeout shared end-to-end operation timeout
     * @param <T>     operation result type
     * @return completed operation value
     * @throws TimeoutException if no operation time remains
     */
    private static <T> T await(final Call<T> call, final Timeout timeout) {
        final Duration remaining = Assert.notNull(timeout, "Authentication timeout must not be null").remaining();
        if (remaining.isZero()) {
            throw new TimeoutException("Authentication transport timeout has expired");
        }
        return Assert.notNull(call, "Fabric call must not be null").await(remaining);
    }

    /**
     * Authentication-owned exclusive socket session.
     * <p>
     * This facade deliberately exposes only byte-oriented operations required by authentication protocols. The backing
     * Fabric session, message model, payload ownership, call lifecycle, and TLS policy never cross the FabricX
     * boundary.
     * </p>
     *
     * @author Kimi Liu
     */
    public static class Socket implements AutoCloseable {

        /**
         * Backing Fabric session owned exclusively by this facade.
         */
        private final SocketSession session;

        /**
         * Runtime facade providing the socket TLS policy.
         */
        private final FabricX fabric;

        /**
         * Creates an authentication-owned wrapper for one open Fabric session.
         *
         * @param session backing Fabric socket session
         * @param fabric  Runtime-scoped authentication Fabric facade
         */
        public Socket(final SocketSession session, final FabricX fabric) {
            this.session = Assert.notNull(session, "Fabric socket session must not be null");
            this.fabric = Assert.notNull(fabric, "Authentication Fabric facade must not be null");
        }

        /**
         * Sends one protocol frame.
         *
         * @param payload complete encoded protocol frame
         * @param timeout remaining end-to-end operation timeout
         */
        public void send(final byte[] payload, final Timeout timeout) {
            await(session.send(Payload.of(Assert.notNull(payload, "Socket payload must not be null"))), timeout);
        }

        /**
         * Receives one protocol frame.
         *
         * @param maximumBytes maximum accepted decoded payload size
         * @param timeout      remaining end-to-end operation timeout
         * @return received protocol frame bytes
         */
        public byte[] receive(final long maximumBytes, final Timeout timeout) {
            Assert.isTrue(maximumBytes > 0, "Maximum socket payload size must be positive");
            final Message message = await(session.receive(), timeout);
            return message.payload().bytes(maximumBytes);
        }

        /**
         * Upgrades this existing plaintext connection to TLS.
         *
         * @param timeout remaining end-to-end operation timeout
         */
        public void upgradeTls(final Timeout timeout) {
            await(session.upgradeTls(socketTlsPolicy(fabric)), timeout);
        }

        /**
         * Closes the backing Fabric session.
         */
        @Override
        public void close() {
            session.close();
        }

    }

    /**
     * Defines the authentication-owned byte-stream frame boundary used by socket protocols.
     * <p>
     * Protocol packages implement this contract without depending on Fabric frame or socket APIs. FabricX adapts each
     * implementation to the transport-specific codec internally when opening the connection.
     * </p>
     *
     * @author Kimi Liu
     */
    public interface Framer {

        /**
         * Consumes newly received stream bytes and returns complete protocol frames.
         *
         * @param input newly received stream bytes
         * @return immutable complete frame payloads in wire order
         */
        List<ByteString> decode(Buffer input);

        /**
         * Encodes one complete protocol frame into the destination stream buffer.
         *
         * @param payload complete protocol frame payload
         * @param output  destination stream buffer
         */
        void encode(ByteString payload, Buffer output);

        /**
         * Creates an independent framer for one socket session.
         *
         * @return independent framer
         */
        Framer fork();

        /**
         * Discards incomplete input retained by this framer.
         */
        void reset();

    }

    /**
     * Adapts the authentication-owned framer contract to Fabric internally.
     *
     * @author Kimi Liu
     */
    private static final class FrameCodecAdapter implements FrameCodec {

        /**
         * Authentication protocol framer delegated by this adapter.
         */
        private final Framer framer;

        /**
         * Creates one internal Fabric frame-codec adapter.
         *
         * @param framer authentication protocol framer
         */
        private FrameCodecAdapter(final Framer framer) {
            this.framer = Assert.notNull(framer, "Authentication socket framer must not be null");
        }

        /**
         * Converts authentication frame payloads into internal Fabric frames.
         *
         * @param input newly received stream bytes
         * @return complete internal Fabric frames
         */
        @Override
        public List<Frame> decode(final Buffer input) {
            final List<ByteString> payloads = framer.decode(input);
            final List<Frame> frames = new ArrayList<>(payloads.size());
            payloads.forEach(payload -> frames.add(Frame.of(payload)));
            return List.copyOf(frames);
        }

        /**
         * Delegates one internal Fabric frame to the authentication protocol framer.
         *
         * @param frame  internal Fabric frame
         * @param output destination stream buffer
         */
        @Override
        public void encode(final Frame frame, final Buffer output) {
            framer.encode(Assert.notNull(frame, "Fabric frame must not be null").payload(), output);
        }

        /**
         * Creates an independent adapter and authentication framer for one session.
         *
         * @return independent internal adapter
         */
        @Override
        public FrameCodecAdapter fork() {
            return new FrameCodecAdapter(framer.fork());
        }

        /**
         * Discards incomplete bytes retained by the authentication framer.
         */
        @Override
        public void reset() {
            framer.reset();
        }

    }

    /**
     * Authentication-owned time source backed internally by the Fabric Runtime clock.
     *
     * @author Kimi Liu
     */
    public static class Clock {

        /**
         * Backing Fabric time source.
         */
        private final org.miaixz.bus.fabric.Clock delegate;

        /**
         * Creates an authentication time source over one Fabric clock.
         *
         * @param delegate backing Fabric clock
         */
        public Clock(final org.miaixz.bus.fabric.Clock delegate) {
            this.delegate = Assert.notNull(delegate, "Fabric clock must not be null");
        }

        /**
         * Creates a system time source.
         *
         * @return shared-system-backed authentication clock
         */
        public static Clock system() {
            return new Clock(org.miaixz.bus.fabric.Clock.system());
        }

        /**
         * Creates a fixed time source.
         *
         * @param instant fixed instant
         * @return fixed authentication clock
         */
        public static Clock fixed(final Instant instant) {
            return new Clock(org.miaixz.bus.fabric.Clock.fixed(instant));
        }

        /**
         * Returns the current instant.
         *
         * @return current instant
         */
        public Instant now() {
            return delegate.now();
        }

        /**
         * Returns current epoch milliseconds.
         *
         * @return epoch milliseconds
         */
        public long millis() {
            return delegate.millis();
        }

        /**
         * Returns monotonic nanoseconds.
         *
         * @return monotonic nanoseconds
         */
        public long nanos() {
            return delegate.nanos();
        }

    }

    /**
     * Authentication-owned immutable URL value.
     *
     * @author Kimi Liu
     */
    public static class Url {

        /**
         * Backing normalized Fabric URL.
         */
        private final UnoUrl delegate;

        /**
         * Creates an authentication URL wrapper.
         *
         * @param delegate backing normalized URL
         */
        public Url(final UnoUrl delegate) {
            this.delegate = Assert.notNull(delegate, "Fabric URL must not be null");
        }

        private static Url wrap(final UnoUrl value) {
            return value == null ? null : new Url(value);
        }

        /**
         * Parses one absolute URL.
         *
         * @param value absolute URL text
         * @return normalized authentication URL
         */
        public static Url parse(final String value) {
            return new Url(UnoUrl.parse(value));
        }

        /**
         * Returns the default port for one URL scheme.
         *
         * @param scheme URL scheme
         * @return default port or {@code -1}
         */
        public static int defaultPort(final String scheme) {
            return UnoUrl.defaultPort(scheme);
        }

        /**
         * Creates an empty URL builder.
         *
         * @return authentication URL builder
         */
        public static UrlBuilder builder() {
            return new UrlBuilder(UnoUrl.builder());
        }

        /**
         * Returns the normalized scheme.
         *
         * @return URL scheme
         */
        public String scheme() {
            return delegate.scheme();
        }

        /**
         * Returns the normalized host.
         *
         * @return URL host
         */
        public String host() {
            return delegate.host();
        }

        /**
         * Returns the effective port.
         *
         * @return URL port
         */
        public int port() {
            return delegate.port();
        }

        /**
         * Returns the normalized path.
         *
         * @return URL path
         */
        public String path() {
            return delegate.path();
        }

        public String username() {
            return delegate.username();
        }

        public String password() {
            return delegate.password();
        }

        public String fragment() {
            return delegate.fragment();
        }

        public int querySize() {
            return delegate.querySize();
        }

        public String queryParameterName(final int index) {
            return delegate.queryParameterName(index);
        }

        public String queryParameterValue(final int index) {
            return delegate.queryParameterValue(index);
        }

        public String redact() {
            return delegate.redact();
        }

        /**
         * Returns immutable decoded query values.
         *
         * @return query map
         */
        public Map<String, List<String>> query() {
            return delegate.query();
        }

        /**
         * Returns the first decoded value for one query name.
         *
         * @param name query name
         * @return first value or {@code null}
         */
        public String query(final String name) {
            return delegate.query(name);
        }

        /**
         * Returns the first decoded value for one query parameter.
         *
         * @param name query parameter name
         * @return first value or {@code null}
         */
        public String queryParameter(final String name) {
            return delegate.queryParameter(name);
        }

        /**
         * Returns a copy containing one additional query value.
         *
         * @param name  query name
         * @param value query value
         * @return updated immutable URL
         */
        public Url withQuery(final String name, final String value) {
            return new Url(delegate.withQuery(name, value));
        }

        /**
         * Returns a copy without one query name.
         *
         * @param name query name
         * @return updated immutable URL
         */
        public Url withoutQuery(final String name) {
            return new Url(delegate.withoutQuery(name));
        }

        /**
         * Resolves a reference against this URL.
         *
         * @param link reference text
         * @return resolved URL or {@code null}
         */
        public Url resolve(final String link) {
            final UnoUrl resolved = delegate.resolve(link);
            return resolved == null ? null : new Url(resolved);
        }

        /**
         * Returns a builder initialized from this URL.
         *
         * @return initialized URL builder
         */
        public UrlBuilder newBuilder() {
            return new UrlBuilder(delegate.newBuilder());
        }

        /**
         * Returns a builder initialized from a resolved reference.
         *
         * @param link reference text
         * @return initialized URL builder or {@code null}
         */
        public UrlBuilder newBuilder(final String link) {
            final UnoUrl.Builder builder = delegate.newBuilder(link);
            return builder == null ? null : new UrlBuilder(builder);
        }

        /**
         * Returns this URL as a URI.
         *
         * @return normalized URI
         */
        public URI toUri() {
            return delegate.toUri();
        }

        /**
         * Returns the encoded URL text.
         *
         * @return encoded URL
         */
        public String encoded() {
            return delegate.encoded();
        }

        /**
         * Returns query parameter names.
         *
         * @return immutable names
         */
        public Set<String> queryParameterNames() {
            return delegate.queryParameterNames();
        }

        /**
         * Returns decoded values for one query name.
         *
         * @param name query name
         * @return immutable values
         */
        public List<String> queryParameterValues(final String name) {
            return delegate.queryParameterValues(name);
        }

        /**
         * Returns the normalized URL text.
         *
         * @return normalized URL
         */
        @Override
        public String toString() {
            return delegate.toString();
        }

        /**
         * Compares normalized URL values.
         *
         * @param object comparison candidate
         * @return whether both URLs are equal
         */
        @Override
        public boolean equals(final Object object) {
            return this == object || object instanceof Url other && delegate.equals(other.delegate);
        }

        /**
         * Returns the normalized URL hash.
         *
         * @return URL hash code
         */
        @Override
        public int hashCode() {
            return delegate.hashCode();
        }

    }

    /**
     * Mutable authentication URL builder.
     *
     * @author Kimi Liu
     */
    public static class UrlBuilder {

        /**
         * Backing Fabric URL builder.
         */
        private final UnoUrl.Builder delegate;

        /**
         * Creates an authentication URL builder wrapper.
         *
         * @param delegate backing URL builder
         */
        public UrlBuilder(final UnoUrl.Builder delegate) {
            this.delegate = Assert.notNull(delegate, "Fabric URL builder must not be null");
        }

        public UrlBuilder scheme(final String value) {
            delegate.scheme(value);
            return this;
        }

        public UrlBuilder host(final String value) {
            delegate.host(value);
            return this;
        }

        public UrlBuilder port(final int value) {
            delegate.port(value);
            return this;
        }

        public UrlBuilder path(final String value) {
            delegate.path(value);
            return this;
        }

        public UrlBuilder query(final String name, final String value) {
            delegate.query(name, value);
            return this;
        }

        public UrlBuilder fragment(final String value) {
            delegate.fragment(value);
            return this;
        }

        public Url build() {
            return new Url(delegate.build());
        }

    }

    /**
     * Authentication-owned immutable HTTP headers.
     *
     * @author Kimi Liu
     */
    public static class Headers {

        private final org.miaixz.bus.fabric.Headers delegate;

        public Headers(final org.miaixz.bus.fabric.Headers delegate) {
            this.delegate = Assert.notNull(delegate, "Fabric headers must not be null");
        }

        private org.miaixz.bus.fabric.Headers unwrap() {
            return delegate;
        }

        public static Headers empty() {
            return new Headers(org.miaixz.bus.fabric.Headers.empty());
        }

        public static Headers of(final String... values) {
            return new Headers(org.miaixz.bus.fabric.Headers.of(values));
        }

        public static Headers of(final Map<String, String> values) {
            return new Headers(org.miaixz.bus.fabric.Headers.of(values));
        }

        public static HeadersBuilder builder() {
            return new HeadersBuilder(org.miaixz.bus.fabric.Headers.builder());
        }

        public String get(final String name) {
            return delegate.get(name);
        }

        public List<String> values(final String name) {
            return delegate.values(name);
        }

        public boolean contains(final String name) {
            return delegate.contains(name);
        }

        public int size() {
            return delegate.size();
        }

        public String name(final int index) {
            return delegate.name(index);
        }

        public String value(final int index) {
            return delegate.value(index);
        }

        public long contentLength() {
            return delegate.contentLength();
        }

        public Headers with(final String name, final String value) {
            return new Headers(delegate.with(name, value));
        }

        public Headers without(final String name) {
            return new Headers(delegate.without(name));
        }

        public HeadersBuilder newBuilder() {
            return new HeadersBuilder(delegate.newBuilder());
        }

        public Map<String, List<String>> asMap() {
            return delegate.asMap();
        }

    }

    /**
     * Mutable authentication HTTP-header builder.
     *
     * @author Kimi Liu
     */
    public static class HeadersBuilder {

        private final org.miaixz.bus.fabric.Headers.Builder delegate;

        public HeadersBuilder(final org.miaixz.bus.fabric.Headers.Builder delegate) {
            this.delegate = Assert.notNull(delegate, "Fabric headers builder must not be null");
        }

        public HeadersBuilder add(final String name, final String value) {
            delegate.add(name, value);
            return this;
        }

        public HeadersBuilder set(final String name, final String value) {
            delegate.set(name, value);
            return this;
        }

        public HeadersBuilder remove(final String name) {
            delegate.remove(name);
            return this;
        }

        public Headers build() {
            return new Headers(delegate.build());
        }

    }

    /**
     * Authentication-owned closeable HTTP payload body.
     *
     * @author Kimi Liu
     */
    public static class Body implements AutoCloseable {

        private final PayloadBody delegate;

        public Body(final PayloadBody delegate) {
            this.delegate = Assert.notNull(delegate, "Fabric HTTP body must not be null");
        }

        private static Body wrap(final PayloadBody value) {
            return value == null ? null : new Body(value);
        }

        private PayloadBody unwrap() {
            return delegate;
        }

        public static Body empty() {
            return new Body(PayloadBody.empty());
        }

        public static Body of(final String value, final MediaType media) {
            return new Body(PayloadBody.of(Payload.of(value.getBytes(StandardCharsets.UTF_8)), media));
        }

        public static Body of(final byte[] value, final MediaType media) {
            return new Body(PayloadBody.of(Payload.of(value), media));
        }

        public ByteString ownedBytes() {
            return delegate.ownedBytes();
        }

        public MediaType media() {
            return delegate.media();
        }

        public long length() {
            return delegate.length();
        }

        public boolean repeatable() {
            return delegate.repeatable();
        }

        public byte[] bytes() {
            return delegate.bytes();
        }

        public byte[] bytes(final long maximumBytes) {
            return delegate.bytes(maximumBytes);
        }

        public String text(final Charset charset) {
            return delegate.text(charset);
        }

        public String text(final Charset charset, final long maximumBytes) {
            return delegate.text(charset, maximumBytes);
        }

        @Override
        public void close() {
            delegate.close();
        }

    }

    /**
     * Authentication-owned outbound HTTP builder and execution boundary.
     *
     * @author Kimi Liu
     */
    public static class Http {

        private final HttpX.Builder delegate;

        public Http(final HttpX.Builder delegate) {
            this.delegate = Assert.notNull(delegate, "Fabric HTTP builder must not be null");
        }

        public Http url(final String value) {
            delegate.url(value);
            return this;
        }

        public Http get() {
            delegate.get();
            return this;
        }

        public Http get(final String value) {
            delegate.get(value);
            return this;
        }

        public Http post() {
            delegate.post();
            return this;
        }

        public Http post(final String value) {
            delegate.post(value);
            return this;
        }

        public Http method(final Method method) {
            delegate.method(method);
            return this;
        }

        public Http method(final String method) {
            delegate.method(method);
            return this;
        }

        public Http header(final String name, final String value) {
            delegate.header(name, value);
            return this;
        }

        public Http header(final String name, final Object value) {
            delegate.header(name, value);
            return this;
        }

        public Http headers(final Headers headers) {
            delegate.headers(Assert.notNull(headers, "Authentication HTTP headers must not be null").unwrap());
            return this;
        }

        public Http headers(final Map<String, ?> headers) {
            delegate.headers(headers);
            return this;
        }

        public Http query(final String name, final String value) {
            delegate.query(name, value);
            return this;
        }

        public Http query(final String name, final Object value) {
            delegate.query(name, value);
            return this;
        }

        public Http query(final Map<String, ?> values) {
            delegate.query(values);
            return this;
        }

        public Http form(final String name, final String value) {
            delegate.form(name, value);
            return this;
        }

        public Http form(final String name, final Object value) {
            delegate.form(name, value);
            return this;
        }

        public Http form(final Map<String, ?> values) {
            delegate.form(values);
            return this;
        }

        public Http body(final String value) {
            delegate.body(value);
            return this;
        }

        public Http body(final String value, final MediaType media) {
            delegate.body(value, media);
            return this;
        }

        public Http body(final byte[] value, final MediaType media) {
            delegate.body(value, media);
            return this;
        }

        public Http body(final Body body) {
            delegate.body(Assert.notNull(body, "Authentication HTTP body must not be null").unwrap());
            return this;
        }

        public Http media(final MediaType media) {
            delegate.media(media);
            return this;
        }

        public Response execute() {
            return Response.wrap(delegate.execute());
        }

        public String executeText() {
            return delegate.executeText();
        }

    }

    /**
     * Authentication-owned immutable HTTP request.
     *
     * @author Kimi Liu
     */
    public static class Request {

        private final HttpRequest delegate;

        public Request(final HttpRequest delegate) {
            this.delegate = Assert.notNull(delegate, "Fabric HTTP request must not be null");
        }

        private static Request wrap(final HttpRequest value) {
            return value == null ? null : new Request(value);
        }

        private HttpRequest unwrap() {
            return delegate;
        }

        public static RequestBuilder builder() {
            return new RequestBuilder(HttpRequest.builder());
        }

        public RequestBuilder toBuilder() {
            return new RequestBuilder(delegate.toBuilder());
        }

        public Method method() {
            return delegate.method();
        }

        public Url url() {
            return Url.wrap(delegate.url());
        }

        public Headers headers() {
            return new Headers(delegate.headers());
        }

        public Body body() {
            return Body.wrap(delegate.body());
        }

        public String methodText() {
            return delegate.methodText();
        }

        public String authority() {
            return delegate.authority();
        }

        public String requestTarget() {
            return delegate.requestTarget();
        }

        public long bodyLength() {
            return delegate.bodyLength();
        }

        public boolean replayable() {
            return delegate.replayable();
        }

        public Object tag() {
            return delegate.tag();
        }

        public <T> T tag(final Class<T> type) {
            return delegate.tag(type);
        }

        public boolean isHttps() {
            return delegate.isHttps();
        }

    }

    /**
     * Mutable authentication HTTP-request builder.
     *
     * @author Kimi Liu
     */
    public static class RequestBuilder {

        private final HttpRequest.Builder delegate;

        public RequestBuilder(final HttpRequest.Builder delegate) {
            this.delegate = Assert.notNull(delegate, "Fabric HTTP request builder must not be null");
        }

        public RequestBuilder method(final Method method) {
            delegate.method(method);
            return this;
        }

        public RequestBuilder url(final Url url) {
            delegate.url(Assert.notNull(url, "Authentication URL must not be null").delegate);
            return this;
        }

        public RequestBuilder headers(final Headers headers) {
            delegate.headers(Assert.notNull(headers, "Authentication HTTP headers must not be null").unwrap());
            return this;
        }

        public RequestBuilder userAgent(final String value) {
            delegate.userAgent(value);
            return this;
        }

        public RequestBuilder body(final Body body) {
            delegate.body(Assert.notNull(body, "Authentication HTTP body must not be null").unwrap());
            return this;
        }

        public RequestBuilder text(final String value) {
            delegate.text(value);
            return this;
        }

        public RequestBuilder text(final String value, final MediaType media) {
            delegate.text(value, media);
            return this;
        }

        public RequestBuilder tag(final Object tag) {
            delegate.tag(tag);
            return this;
        }

        public Request build() {
            return new Request(delegate.build());
        }

    }

    /**
     * Authentication-owned closeable HTTP response.
     *
     * @author Kimi Liu
     */
    public static class Response implements AutoCloseable {

        private final HttpResponse delegate;

        public Response(final HttpResponse delegate) {
            this.delegate = Assert.notNull(delegate, "Fabric HTTP response must not be null");
        }

        private static Response wrap(final HttpResponse value) {
            return value == null ? null : new Response(value);
        }

        private HttpResponse unwrap() {
            return delegate;
        }

        public static ResponseBuilder builder() {
            return new ResponseBuilder(HttpResponse.builder());
        }

        public ResponseBuilder toBuilder() {
            return new ResponseBuilder(delegate.toBuilder());
        }

        public Request request() {
            return Request.wrap(delegate.request());
        }

        public int code() {
            return delegate.code();
        }

        public String message() {
            return delegate.message();
        }

        public Headers headers() {
            return new Headers(delegate.headers());
        }

        public Body body() {
            return Body.wrap(delegate.body());
        }

        public boolean successful() {
            return delegate.successful();
        }

        public CacheControl cacheControl() {
            return new CacheControl(delegate.cacheControl());
        }

        public String text() {
            return delegate.text();
        }

        public byte[] bytes() {
            return delegate.bytes();
        }

        public byte[] bytes(final long maximumBytes) {
            return delegate.bytes(maximumBytes);
        }

        @Override
        public void close() {
            delegate.close();
        }

    }

    /**
     * Authentication-owned HTTP cache-control view.
     *
     * @author Kimi Liu
     */
    public static class CacheControl {

        private final HttpCacheControl delegate;

        public CacheControl(final HttpCacheControl delegate) {
            this.delegate = Assert.notNull(delegate, "Fabric cache control must not be null");
        }

        public boolean noCache() {
            return delegate.noCache();
        }

        public boolean noStore() {
            return delegate.noStore();
        }

        public int maxAgeSeconds() {
            return delegate.maxAgeSeconds();
        }

    }

    /**
     * Creates one HTTP Basic authorization header value.
     *
     * @param username HTTP Basic username
     * @param password HTTP Basic password
     * @return complete Authorization header value
     */
    public static String basic(final String username, final String password) {
        return HttpAuth.basic(username, password).value();
    }

    /**
     * Authentication-owned parsed HTTP authentication challenge.
     *
     * @param scheme     authentication scheme
     * @param realm      optional realm
     * @param parameters immutable authentication parameters
     * @author Kimi Liu
     */
    public record Challenge(String scheme, String realm, Map<String, String> parameters) {

        /**
         * Parses one HTTP authentication challenge.
         *
         * @param value challenge header value
         * @return parsed immutable challenge
         */
        public static Challenge parse(final String value) {
            final org.miaixz.bus.fabric.protocol.http.auth.Challenge parsed = org.miaixz.bus.fabric.protocol.http.auth.Challenge
                    .parse(value);
            return new Challenge(parsed.scheme(), parsed.realm(), parsed.parameters());
        }

    }

    /**
     * Authentication-owned immutable outbound network address policy.
     *
     * @author Kimi Liu
     */
    public static class AddressPolicy {

        private final org.miaixz.bus.fabric.guard.route.AddressPolicy delegate;

        public AddressPolicy(final Set<Protocol> allowedSchemes, final Set<Integer> allowedPorts,
                final Set<String> allowedTargetCidrs, final Set<String> allowedPeerCidrs) {
            this.delegate = new org.miaixz.bus.fabric.guard.route.AddressPolicy(allowedSchemes, allowedPorts,
                    allowedTargetCidrs, allowedPeerCidrs);
        }

        private org.miaixz.bus.fabric.guard.route.AddressPolicy unwrap() {
            return delegate;
        }

        public Set<Protocol> allowedSchemes() {
            return delegate.allowedSchemes();
        }

        public Set<Integer> allowedPorts() {
            return delegate.allowedPorts();
        }

        public Set<String> allowedTargetCidrs() {
            return delegate.allowedTargetCidrs();
        }

        public Set<String> allowedPeerCidrs() {
            return delegate.allowedPeerCidrs();
        }

    }

    /**
     * Mutable authentication HTTP-response builder.
     *
     * @author Kimi Liu
     */
    public static class ResponseBuilder {

        private final HttpResponse.Builder delegate;

        public ResponseBuilder(final HttpResponse.Builder delegate) {
            this.delegate = Assert.notNull(delegate, "Fabric HTTP response builder must not be null");
        }

        public ResponseBuilder request(final Request request) {
            delegate.request(Assert.notNull(request, "Authentication HTTP request must not be null").unwrap());
            return this;
        }

        public ResponseBuilder code(final int code) {
            delegate.code(code);
            return this;
        }

        public ResponseBuilder message(final String message) {
            delegate.message(message);
            return this;
        }

        public ResponseBuilder headers(final Headers headers) {
            delegate.headers(Assert.notNull(headers, "Authentication HTTP headers must not be null").unwrap());
            return this;
        }

        public ResponseBuilder body(final Body body) {
            delegate.body(Assert.notNull(body, "Authentication HTTP body must not be null").unwrap());
            return this;
        }

        public ResponseBuilder networkResponse(final Response response) {
            delegate.networkResponse(response == null ? null : response.unwrap());
            return this;
        }

        public ResponseBuilder cacheResponse(final Response response) {
            delegate.cacheResponse(response == null ? null : response.unwrap());
            return this;
        }

        public ResponseBuilder priorResponse(final Response response) {
            delegate.priorResponse(response == null ? null : response.unwrap());
            return this;
        }

        public ResponseBuilder sentRequestAtMillis(final long value) {
            delegate.sentRequestAtMillis(value);
            return this;
        }

        public ResponseBuilder receivedResponseAtMillis(final long value) {
            delegate.receivedResponseAtMillis(value);
            return this;
        }

        public Response build() {
            return new Response(delegate.build());
        }

    }

    /**
     * Builds one immutable Runtime-scoped authentication Fabric facade.
     * <p>
     * The builder only assembles Fabric infrastructure and the non-relaxable authentication transport policy. It does
     * not configure Sources, credentials, protocol endpoints, workers, registries, or application routes.
     * </p>
     *
     * @author Kimi Liu
     */
    public static class Builder {

        /**
         * Immutable non-relaxable authentication transport policy.
         */
        private SecurityBaseline securityBaseline;

        /**
         * Creates an empty FabricX builder.
         */
        public Builder() {
            // No initialization required.
        }

        /**
         * Sets the non-relaxable authentication transport policy.
         *
         * @param securityBaseline immutable authentication security baseline
         * @return this builder
         */
        public Builder securityBaseline(final SecurityBaseline securityBaseline) {
            this.securityBaseline = Assert
                    .notNull(securityBaseline, "Authentication security baseline must not be null");
            return this;
        }

        /**
         * Validates the required dependencies and creates one immutable FabricX facade.
         *
         * @return immutable Runtime-scoped authentication Fabric facade
         * @throws IllegalArgumentException if a required dependency was not configured
         */
        public FabricX build() {
            return new FabricX(this);
        }

    }

}
