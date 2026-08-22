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
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.exception.TimeoutException;
import org.miaixz.bus.core.net.Http.Method;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.fabric.*;
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

        /**
         * Wraps a nullable normalized URL at an internal adapter boundary.
         *
         * @param value backing normalized URL
         * @return wrapped URL or {@code null}
         */
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

        /**
         * Returns decoded URL user information username.
         *
         * @return username or an empty value
         */
        public String username() {
            return delegate.username();
        }

        /**
         * Returns decoded URL user information password.
         *
         * @return password or an empty value
         */
        public String password() {
            return delegate.password();
        }

        /**
         * Returns the decoded URL fragment.
         *
         * @return fragment or {@code null}
         */
        public String fragment() {
            return delegate.fragment();
        }

        /**
         * Returns the flattened query value count.
         *
         * @return query value count
         */
        public int querySize() {
            return delegate.querySize();
        }

        /**
         * Returns the decoded query name at one flattened index.
         *
         * @param index zero-based query index
         * @return decoded query name
         */
        public String queryParameterName(final int index) {
            return delegate.queryParameterName(index);
        }

        /**
         * Returns the decoded query value at one flattened index.
         *
         * @param index zero-based query index
         * @return decoded query value or {@code null}
         */
        public String queryParameterValue(final int index) {
            return delegate.queryParameterValue(index);
        }

        /**
         * Returns the normalized URL with sensitive components redacted.
         *
         * @return redacted URL text
         */
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

        /**
         * Sets the URL scheme.
         *
         * @param value scheme value
         * @return this builder
         */
        public UrlBuilder scheme(final String value) {
            delegate.scheme(value);
            return this;
        }

        /**
         * Sets the URL host.
         *
         * @param value host value
         * @return this builder
         */
        public UrlBuilder host(final String value) {
            delegate.host(value);
            return this;
        }

        /**
         * Sets the URL port.
         *
         * @param value destination port
         * @return this builder
         */
        public UrlBuilder port(final int value) {
            delegate.port(value);
            return this;
        }

        /**
         * Sets the encoded or decoded URL path accepted by Fabric.
         *
         * @param value path value
         * @return this builder
         */
        public UrlBuilder path(final String value) {
            delegate.path(value);
            return this;
        }

        /**
         * Adds one query value.
         *
         * @param name  query name
         * @param value query value
         * @return this builder
         */
        public UrlBuilder query(final String name, final String value) {
            delegate.query(name, value);
            return this;
        }

        /**
         * Sets the URL fragment.
         *
         * @param value fragment value
         * @return this builder
         */
        public UrlBuilder fragment(final String value) {
            delegate.fragment(value);
            return this;
        }

        /**
         * Builds the normalized immutable URL.
         *
         * @return immutable URL
         */
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

        /**
         * Backing immutable Bus Fabric headers.
         */
        private final org.miaixz.bus.fabric.Headers delegate;

        /**
         * Creates an authentication header view over immutable Fabric headers.
         *
         * @param delegate backing Fabric headers
         */
        public Headers(final org.miaixz.bus.fabric.Headers delegate) {
            this.delegate = Assert.notNull(delegate, "Fabric headers must not be null");
        }

        /**
         * Returns an empty immutable header view.
         *
         * @return empty headers
         */
        public static Headers empty() {
            return new Headers(org.miaixz.bus.fabric.Headers.empty());
        }

        /**
         * Creates headers from alternating names and values.
         *
         * @param values alternating header names and values
         * @return immutable headers
         */
        public static Headers of(final String... values) {
            return new Headers(org.miaixz.bus.fabric.Headers.of(values));
        }

        /**
         * Creates headers from one value per name.
         *
         * @param values header values keyed by name
         * @return immutable headers
         */
        public static Headers of(final Map<String, String> values) {
            return new Headers(org.miaixz.bus.fabric.Headers.of(values));
        }

        /**
         * Creates an empty mutable header builder.
         *
         * @return new header builder
         */
        public static HeadersBuilder builder() {
            return new HeadersBuilder(org.miaixz.bus.fabric.Headers.builder());
        }

        /**
         * Returns the backing Fabric headers inside the facade boundary.
         *
         * @return backing immutable headers
         */
        private org.miaixz.bus.fabric.Headers unwrap() {
            return delegate;
        }

        /**
         * Returns the first value for a case-insensitive header name.
         *
         * @param name header name
         * @return first value or {@code null}
         */
        public String get(final String name) {
            return delegate.get(name);
        }

        /**
         * Returns every value for a case-insensitive header name.
         *
         * @param name header name
         * @return immutable header values
         */
        public List<String> values(final String name) {
            return delegate.values(name);
        }

        /**
         * Reports whether a header name is present.
         *
         * @param name header name
         * @return whether the name is present
         */
        public boolean contains(final String name) {
            return delegate.contains(name);
        }

        /**
         * Returns the flattened header entry count.
         *
         * @return header entry count
         */
        public int size() {
            return delegate.size();
        }

        /**
         * Returns the header name at one flattened entry index.
         *
         * @param index zero-based entry index
         * @return header name
         */
        public String name(final int index) {
            return delegate.name(index);
        }

        /**
         * Returns the header value at one flattened entry index.
         *
         * @param index zero-based entry index
         * @return header value
         */
        public String value(final int index) {
            return delegate.value(index);
        }

        /**
         * Returns the parsed Content-Length value.
         *
         * @return content length or the Fabric absence marker
         */
        public long contentLength() {
            return delegate.contentLength();
        }

        /**
         * Returns a copy containing one additional header value.
         *
         * @param name  header name
         * @param value header value
         * @return updated immutable headers
         */
        public Headers with(final String name, final String value) {
            return new Headers(delegate.with(name, value));
        }

        /**
         * Returns a copy without one header name.
         *
         * @param name header name
         * @return updated immutable headers
         */
        public Headers without(final String name) {
            return new Headers(delegate.without(name));
        }

        /**
         * Returns a builder initialized from these headers.
         *
         * @return initialized header builder
         */
        public HeadersBuilder newBuilder() {
            return new HeadersBuilder(delegate.newBuilder());
        }

        /**
         * Returns an immutable multi-value header map.
         *
         * @return header values keyed by normalized name
         */
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

        /**
         * Backing mutable Bus Fabric header builder.
         */
        private final org.miaixz.bus.fabric.Headers.Builder delegate;

        /**
         * Creates a facade over one Fabric header builder.
         *
         * @param delegate backing header builder
         */
        public HeadersBuilder(final org.miaixz.bus.fabric.Headers.Builder delegate) {
            this.delegate = Assert.notNull(delegate, "Fabric headers builder must not be null");
        }

        /**
         * Adds one header value without replacing existing values.
         *
         * @param name  header name
         * @param value header value
         * @return this builder
         */
        public HeadersBuilder add(final String name, final String value) {
            delegate.add(name, value);
            return this;
        }

        /**
         * Replaces all values for one header name.
         *
         * @param name  header name
         * @param value replacement value
         * @return this builder
         */
        public HeadersBuilder set(final String name, final String value) {
            delegate.set(name, value);
            return this;
        }

        /**
         * Removes all values for one header name.
         *
         * @param name header name
         * @return this builder
         */
        public HeadersBuilder remove(final String name) {
            delegate.remove(name);
            return this;
        }

        /**
         * Freezes the accumulated header values.
         *
         * @return immutable headers
         */
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

        /**
         * Backing lifecycle-bound Bus Fabric payload body.
         */
        private final PayloadBody delegate;

        /**
         * Creates an authentication body facade.
         *
         * @param delegate backing Fabric payload body
         */
        public Body(final PayloadBody delegate) {
            this.delegate = Assert.notNull(delegate, "Fabric HTTP body must not be null");
        }

        /**
         * Wraps a nullable Fabric body at an internal adapter boundary.
         *
         * @param value backing Fabric body
         * @return wrapped body or {@code null}
         */
        private static Body wrap(final PayloadBody value) {
            return value == null ? null : new Body(value);
        }

        /**
         * Returns an empty repeatable body.
         *
         * @return empty body
         */
        public static Body empty() {
            return new Body(PayloadBody.empty());
        }

        /**
         * Creates a UTF-8 body from text and its media type.
         *
         * @param value body text
         * @param media body media type
         * @return immutable repeatable body
         */
        public static Body of(final String value, final MediaType media) {
            return new Body(PayloadBody.of(Payload.of(value.getBytes(Charset.UTF_8)), media));
        }

        /**
         * Creates a body from bytes and its media type.
         *
         * @param value body bytes
         * @param media body media type
         * @return immutable repeatable body
         */
        public static Body of(final byte[] value, final MediaType media) {
            return new Body(PayloadBody.of(Payload.of(value), media));
        }

        /**
         * Returns the backing Fabric body inside the facade boundary.
         *
         * @return backing payload body
         */
        private PayloadBody unwrap() {
            return delegate;
        }

        /**
         * Returns an owned immutable byte-string representation.
         *
         * @return owned body bytes
         */
        public ByteString ownedBytes() {
            return delegate.ownedBytes();
        }

        /**
         * Returns the declared media type.
         *
         * @return body media type
         */
        public MediaType media() {
            return delegate.media();
        }

        /**
         * Returns the declared body length when available.
         *
         * @return body length or the Fabric absence marker
         */
        public long length() {
            return delegate.length();
        }

        /**
         * Reports whether the payload may be consumed more than once.
         *
         * @return whether the body is repeatable
         */
        public boolean repeatable() {
            return delegate.repeatable();
        }

        /**
         * Returns the underlying Bus payload for explicit streaming operations.
         * <p>
         * The returned payload retains the lifecycle of this body and must be consumed before the owning response is
         * closed. Callers that materialize ordinary bounded documents should continue to use {@link #bytes(long)}.
         * </p>
         *
         * @return original response payload or its progress-tracking wrapper
         */
        public Payload payload() {
            return delegate.payload();
        }

        /**
         * Materializes the complete body using the Fabric default boundary.
         *
         * @return body bytes
         */
        public byte[] bytes() {
            return delegate.bytes();
        }

        /**
         * Materializes at most the explicitly bounded document size.
         *
         * @param maximumBytes maximum accepted body bytes
         * @return bounded body bytes
         */
        public byte[] bytes(final long maximumBytes) {
            return delegate.bytes(maximumBytes);
        }

        /**
         * Decodes complete body text using the supplied charset.
         *
         * @param charset text charset
         * @return decoded body text
         */
        public String text(final java.nio.charset.Charset charset) {
            return delegate.text(charset);
        }

        /**
         * Decodes bounded body text using the supplied charset.
         *
         * @param charset      text charset
         * @param maximumBytes maximum accepted body bytes
         * @return decoded bounded body text
         */
        public String text(final java.nio.charset.Charset charset, final long maximumBytes) {
            return delegate.text(charset, maximumBytes);
        }

        /**
         * Closes the backing payload body.
         */
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

        /**
         * Backing Bus Fabric HTTP execution builder.
         */
        private final HttpX.Builder delegate;

        /**
         * Creates an authentication HTTP facade.
         *
         * @param delegate backing Fabric HTTP builder
         */
        public Http(final HttpX.Builder delegate) {
            this.delegate = Assert.notNull(delegate, "Fabric HTTP builder must not be null");
        }

        /**
         * Sets the absolute request URL.
         *
         * @param value absolute URL text
         * @return this builder
         */
        public Http url(final String value) {
            delegate.url(value);
            return this;
        }

        /**
         * Selects HTTP GET for the configured URL.
         *
         * @return this builder
         */
        public Http get() {
            delegate.get();
            return this;
        }

        /**
         * Sets the request URL and selects HTTP GET.
         *
         * @param value absolute URL text
         * @return this builder
         */
        public Http get(final String value) {
            delegate.get(value);
            return this;
        }

        /**
         * Selects HTTP POST for the configured URL.
         *
         * @return this builder
         */
        public Http post() {
            delegate.post();
            return this;
        }

        /**
         * Sets the request URL and selects HTTP POST.
         *
         * @param value absolute URL text
         * @return this builder
         */
        public Http post(final String value) {
            delegate.post(value);
            return this;
        }

        /**
         * Sets the registered HTTP method.
         *
         * @param method HTTP method
         * @return this builder
         */
        public Http method(final Method method) {
            delegate.method(method);
            return this;
        }

        /**
         * Sets an HTTP method by its registered text.
         *
         * @param method HTTP method text
         * @return this builder
         */
        public Http method(final String method) {
            delegate.method(method);
            return this;
        }

        /**
         * Adds one string header value.
         *
         * @param name  header name
         * @param value header value
         * @return this builder
         */
        public Http header(final String name, final String value) {
            delegate.header(name, value);
            return this;
        }

        /**
         * Adds one object header value using Fabric conversion.
         *
         * @param name  header name
         * @param value header value
         * @return this builder
         */
        public Http header(final String name, final Object value) {
            delegate.header(name, value);
            return this;
        }

        /**
         * Adds immutable authentication headers.
         *
         * @param headers header values
         * @return this builder
         */
        public Http headers(final Headers headers) {
            delegate.headers(Assert.notNull(headers, "Authentication HTTP headers must not be null").unwrap());
            return this;
        }

        /**
         * Adds map-backed header values using Fabric conversion.
         *
         * @param headers header values keyed by name
         * @return this builder
         */
        public Http headers(final Map<String, ?> headers) {
            delegate.headers(headers);
            return this;
        }

        /**
         * Adds one string query value.
         *
         * @param name  query name
         * @param value query value
         * @return this builder
         */
        public Http query(final String name, final String value) {
            delegate.query(name, value);
            return this;
        }

        /**
         * Adds one object query value using Fabric conversion.
         *
         * @param name  query name
         * @param value query value
         * @return this builder
         */
        public Http query(final String name, final Object value) {
            delegate.query(name, value);
            return this;
        }

        /**
         * Adds map-backed query values using Fabric conversion.
         *
         * @param values query values keyed by name
         * @return this builder
         */
        public Http query(final Map<String, ?> values) {
            delegate.query(values);
            return this;
        }

        /**
         * Adds one string form value.
         *
         * @param name  form name
         * @param value form value
         * @return this builder
         */
        public Http form(final String name, final String value) {
            delegate.form(name, value);
            return this;
        }

        /**
         * Adds one object form value using Fabric conversion.
         *
         * @param name  form name
         * @param value form value
         * @return this builder
         */
        public Http form(final String name, final Object value) {
            delegate.form(name, value);
            return this;
        }

        /**
         * Adds map-backed form values using Fabric conversion.
         *
         * @param values form values keyed by name
         * @return this builder
         */
        public Http form(final Map<String, ?> values) {
            delegate.form(values);
            return this;
        }

        /**
         * Sets a text request body using Fabric defaults.
         *
         * @param value body text
         * @return this builder
         */
        public Http body(final String value) {
            delegate.body(value);
            return this;
        }

        /**
         * Sets a text request body and media type.
         *
         * @param value body text
         * @param media body media type
         * @return this builder
         */
        public Http body(final String value, final MediaType media) {
            delegate.body(value, media);
            return this;
        }

        /**
         * Sets a binary request body and media type.
         *
         * @param value body bytes
         * @param media body media type
         * @return this builder
         */
        public Http body(final byte[] value, final MediaType media) {
            delegate.body(value, media);
            return this;
        }

        /**
         * Sets a lifecycle-bound authentication body.
         *
         * @param body request body
         * @return this builder
         */
        public Http body(final Body body) {
            delegate.body(Assert.notNull(body, "Authentication HTTP body must not be null").unwrap());
            return this;
        }

        /**
         * Sets the request media type.
         *
         * @param media media type
         * @return this builder
         */
        public Http media(final MediaType media) {
            delegate.media(media);
            return this;
        }

        /**
         * Executes the configured request.
         *
         * @return closeable HTTP response
         */
        public Response execute() {
            return Response.wrap(delegate.execute());
        }

        /**
         * Executes the configured request and decodes its response text.
         *
         * @return response text
         */
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

        /**
         * Backing immutable Bus Fabric HTTP request.
         */
        private final HttpRequest delegate;

        /**
         * Creates an authentication request facade.
         *
         * @param delegate backing Fabric request
         */
        public Request(final HttpRequest delegate) {
            this.delegate = Assert.notNull(delegate, "Fabric HTTP request must not be null");
        }

        /**
         * Wraps a nullable Fabric request at an internal adapter boundary.
         *
         * @param value backing Fabric request
         * @return wrapped request or {@code null}
         */
        private static Request wrap(final HttpRequest value) {
            return value == null ? null : new Request(value);
        }

        /**
         * Creates an empty mutable request builder.
         *
         * @return new request builder
         */
        public static RequestBuilder builder() {
            return new RequestBuilder(HttpRequest.builder());
        }

        /**
         * Returns the backing Fabric request inside the facade boundary.
         *
         * @return backing immutable request
         */
        private HttpRequest unwrap() {
            return delegate;
        }

        /**
         * Returns a builder initialized from this request.
         *
         * @return initialized request builder
         */
        public RequestBuilder toBuilder() {
            return new RequestBuilder(delegate.toBuilder());
        }

        /**
         * Returns the registered HTTP method.
         *
         * @return HTTP method
         */
        public Method method() {
            return delegate.method();
        }

        /**
         * Returns the immutable request URL.
         *
         * @return request URL
         */
        public Url url() {
            return Url.wrap(delegate.url());
        }

        /**
         * Returns immutable request headers.
         *
         * @return request headers
         */
        public Headers headers() {
            return new Headers(delegate.headers());
        }

        /**
         * Returns the optional request body.
         *
         * @return request body or {@code null}
         */
        public Body body() {
            return Body.wrap(delegate.body());
        }

        /**
         * Returns the exact HTTP method text.
         *
         * @return method text
         */
        public String methodText() {
            return delegate.methodText();
        }

        /**
         * Returns the normalized request authority.
         *
         * @return request authority
         */
        public String authority() {
            return delegate.authority();
        }

        /**
         * Returns the encoded request target.
         *
         * @return request target
         */
        public String requestTarget() {
            return delegate.requestTarget();
        }

        /**
         * Returns the declared request body length.
         *
         * @return body length or the Fabric absence marker
         */
        public long bodyLength() {
            return delegate.bodyLength();
        }

        /**
         * Reports whether the complete request can be replayed.
         *
         * @return whether the request is replayable
         */
        public boolean replayable() {
            return delegate.replayable();
        }

        /**
         * Returns the untyped request tag.
         *
         * @return request tag or {@code null}
         */
        public Object tag() {
            return delegate.tag();
        }

        /**
         * Returns the request tag when it matches an expected type.
         *
         * @param type expected tag type
         * @param <T>  expected tag value type
         * @return matching tag or {@code null}
         */
        public <T> T tag(final Class<T> type) {
            return delegate.tag(type);
        }

        /**
         * Reports whether the request URL uses HTTPS.
         *
         * @return whether the request uses HTTPS
         */
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

        /**
         * Backing mutable Bus Fabric request builder.
         */
        private final HttpRequest.Builder delegate;

        /**
         * Creates a facade over one Fabric request builder.
         *
         * @param delegate backing request builder
         */
        public RequestBuilder(final HttpRequest.Builder delegate) {
            this.delegate = Assert.notNull(delegate, "Fabric HTTP request builder must not be null");
        }

        /**
         * Sets the HTTP method.
         *
         * @param method HTTP method
         * @return this builder
         */
        public RequestBuilder method(final Method method) {
            delegate.method(method);
            return this;
        }

        /**
         * Sets the immutable request URL.
         *
         * @param url request URL
         * @return this builder
         */
        public RequestBuilder url(final Url url) {
            delegate.url(Assert.notNull(url, "Authentication URL must not be null").delegate);
            return this;
        }

        /**
         * Sets immutable request headers.
         *
         * @param headers request headers
         * @return this builder
         */
        public RequestBuilder headers(final Headers headers) {
            delegate.headers(Assert.notNull(headers, "Authentication HTTP headers must not be null").unwrap());
            return this;
        }

        /**
         * Sets the User-Agent header.
         *
         * @param value user-agent value
         * @return this builder
         */
        public RequestBuilder userAgent(final String value) {
            delegate.userAgent(value);
            return this;
        }

        /**
         * Sets the request body.
         *
         * @param body request body
         * @return this builder
         */
        public RequestBuilder body(final Body body) {
            delegate.body(Assert.notNull(body, "Authentication HTTP body must not be null").unwrap());
            return this;
        }

        /**
         * Sets a text body using Fabric defaults.
         *
         * @param value body text
         * @return this builder
         */
        public RequestBuilder text(final String value) {
            delegate.text(value);
            return this;
        }

        /**
         * Sets a text body and media type.
         *
         * @param value body text
         * @param media body media type
         * @return this builder
         */
        public RequestBuilder text(final String value, final MediaType media) {
            delegate.text(value, media);
            return this;
        }

        /**
         * Sets an application request tag.
         *
         * @param tag request tag
         * @return this builder
         */
        public RequestBuilder tag(final Object tag) {
            delegate.tag(tag);
            return this;
        }

        /**
         * Builds the immutable request.
         *
         * @return immutable request
         */
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

        /**
         * Backing lifecycle-bound Bus Fabric HTTP response.
         */
        private final HttpResponse delegate;

        /**
         * Creates an authentication response facade.
         *
         * @param delegate backing Fabric response
         */
        public Response(final HttpResponse delegate) {
            this.delegate = Assert.notNull(delegate, "Fabric HTTP response must not be null");
        }

        /**
         * Wraps a nullable Fabric response at an internal adapter boundary.
         *
         * @param value backing Fabric response
         * @return wrapped response or {@code null}
         */
        private static Response wrap(final HttpResponse value) {
            return value == null ? null : new Response(value);
        }

        /**
         * Creates an empty mutable response builder.
         *
         * @return new response builder
         */
        public static ResponseBuilder builder() {
            return new ResponseBuilder(HttpResponse.builder());
        }

        /**
         * Returns the backing Fabric response inside the facade boundary.
         *
         * @return backing response
         */
        private HttpResponse unwrap() {
            return delegate;
        }

        /**
         * Returns a builder initialized from this response.
         *
         * @return initialized response builder
         */
        public ResponseBuilder toBuilder() {
            return new ResponseBuilder(delegate.toBuilder());
        }

        /**
         * Returns the request that produced this response.
         *
         * @return originating request or {@code null}
         */
        public Request request() {
            return Request.wrap(delegate.request());
        }

        /**
         * Returns the HTTP status code.
         *
         * @return response status code
         */
        public int code() {
            return delegate.code();
        }

        /**
         * Returns the HTTP status message.
         *
         * @return response status message
         */
        public String message() {
            return delegate.message();
        }

        /**
         * Returns immutable response headers.
         *
         * @return response headers
         */
        public Headers headers() {
            return new Headers(delegate.headers());
        }

        /**
         * Returns the optional lifecycle-bound response body.
         *
         * @return response body or {@code null}
         */
        public Body body() {
            return Body.wrap(delegate.body());
        }

        /**
         * Reports whether the status code is successful.
         *
         * @return whether the response is successful
         */
        public boolean successful() {
            return delegate.successful();
        }

        /**
         * Returns the parsed response cache-control directives.
         *
         * @return cache-control view
         */
        public CacheControl cacheControl() {
            return new CacheControl(delegate.cacheControl());
        }

        /**
         * Decodes the response body as text using Fabric defaults.
         *
         * @return response body text
         */
        public String text() {
            return delegate.text();
        }

        /**
         * Materializes the response body using the Fabric default boundary.
         *
         * @return response body bytes
         */
        public byte[] bytes() {
            return delegate.bytes();
        }

        /**
         * Materializes at most the explicitly bounded response document size.
         *
         * @param maximumBytes maximum accepted response bytes
         * @return bounded response body bytes
         */
        public byte[] bytes(final long maximumBytes) {
            return delegate.bytes(maximumBytes);
        }

        /**
         * Closes the response and its body.
         */
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

        /**
         * Backing immutable Bus Fabric cache-control value.
         */
        private final HttpCacheControl delegate;

        /**
         * Creates an authentication cache-control view.
         *
         * @param delegate backing Fabric cache-control value
         */
        public CacheControl(final HttpCacheControl delegate) {
            this.delegate = Assert.notNull(delegate, "Fabric cache control must not be null");
        }

        /**
         * Reports whether reuse requires origin revalidation.
         *
         * @return whether {@code no-cache} is present
         */
        public boolean noCache() {
            return delegate.noCache();
        }

        /**
         * Reports whether storage is prohibited.
         *
         * @return whether {@code no-store} is present
         */
        public boolean noStore() {
            return delegate.noStore();
        }

        /**
         * Returns the parsed maximum age in seconds.
         *
         * @return maximum age or the Fabric absence marker
         */
        public int maxAgeSeconds() {
            return delegate.maxAgeSeconds();
        }

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

        /**
         * Backing immutable Bus Fabric outbound-address policy.
         */
        private final org.miaixz.bus.fabric.guard.route.AddressPolicy delegate;

        /**
         * Creates an explicit outbound address policy.
         *
         * @param allowedSchemes     allowed transport schemes
         * @param allowedPorts       allowed destination ports
         * @param allowedTargetCidrs allowed resolved target network ranges
         * @param allowedPeerCidrs   allowed connected peer network ranges
         */
        public AddressPolicy(final Set<Protocol> allowedSchemes, final Set<Integer> allowedPorts,
                final Set<String> allowedTargetCidrs, final Set<String> allowedPeerCidrs) {
            this.delegate = new org.miaixz.bus.fabric.guard.route.AddressPolicy(allowedSchemes, allowedPorts,
                    allowedTargetCidrs, allowedPeerCidrs);
        }

        /**
         * Returns the backing Fabric address policy inside the facade boundary.
         *
         * @return backing address policy
         */
        private org.miaixz.bus.fabric.guard.route.AddressPolicy unwrap() {
            return delegate;
        }

        /**
         * Returns allowed transport schemes.
         *
         * @return immutable allowed schemes
         */
        public Set<Protocol> allowedSchemes() {
            return delegate.allowedSchemes();
        }

        /**
         * Returns allowed destination ports.
         *
         * @return immutable allowed ports
         */
        public Set<Integer> allowedPorts() {
            return delegate.allowedPorts();
        }

        /**
         * Returns allowed resolved target network ranges.
         *
         * @return immutable target CIDRs
         */
        public Set<String> allowedTargetCidrs() {
            return delegate.allowedTargetCidrs();
        }

        /**
         * Returns allowed connected peer network ranges.
         *
         * @return immutable peer CIDRs
         */
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

        /**
         * Backing mutable Bus Fabric response builder.
         */
        private final HttpResponse.Builder delegate;

        /**
         * Creates a facade over one Fabric response builder.
         *
         * @param delegate backing response builder
         */
        public ResponseBuilder(final HttpResponse.Builder delegate) {
            this.delegate = Assert.notNull(delegate, "Fabric HTTP response builder must not be null");
        }

        /**
         * Sets the originating request.
         *
         * @param request originating request
         * @return this builder
         */
        public ResponseBuilder request(final Request request) {
            delegate.request(Assert.notNull(request, "Authentication HTTP request must not be null").unwrap());
            return this;
        }

        /**
         * Sets the HTTP status code.
         *
         * @param code status code
         * @return this builder
         */
        public ResponseBuilder code(final int code) {
            delegate.code(code);
            return this;
        }

        /**
         * Sets the HTTP status message.
         *
         * @param message status message
         * @return this builder
         */
        public ResponseBuilder message(final String message) {
            delegate.message(message);
            return this;
        }

        /**
         * Sets immutable response headers.
         *
         * @param headers response headers
         * @return this builder
         */
        public ResponseBuilder headers(final Headers headers) {
            delegate.headers(Assert.notNull(headers, "Authentication HTTP headers must not be null").unwrap());
            return this;
        }

        /**
         * Sets the response body.
         *
         * @param body response body
         * @return this builder
         */
        public ResponseBuilder body(final Body body) {
            delegate.body(Assert.notNull(body, "Authentication HTTP body must not be null").unwrap());
            return this;
        }

        /**
         * Sets the nested network response.
         *
         * @param response network response or {@code null}
         * @return this builder
         */
        public ResponseBuilder networkResponse(final Response response) {
            delegate.networkResponse(response == null ? null : response.unwrap());
            return this;
        }

        /**
         * Sets the nested cache response.
         *
         * @param response cache response or {@code null}
         * @return this builder
         */
        public ResponseBuilder cacheResponse(final Response response) {
            delegate.cacheResponse(response == null ? null : response.unwrap());
            return this;
        }

        /**
         * Sets the preceding response in a redirect or authentication chain.
         *
         * @param response prior response or {@code null}
         * @return this builder
         */
        public ResponseBuilder priorResponse(final Response response) {
            delegate.priorResponse(response == null ? null : response.unwrap());
            return this;
        }

        /**
         * Sets the request-send timestamp.
         *
         * @param value epoch millisecond timestamp
         * @return this builder
         */
        public ResponseBuilder sentRequestAtMillis(final long value) {
            delegate.sentRequestAtMillis(value);
            return this;
        }

        /**
         * Sets the response-receive timestamp.
         *
         * @param value epoch millisecond timestamp
         * @return this builder
         */
        public ResponseBuilder receivedResponseAtMillis(final long value) {
            delegate.receivedResponseAtMillis(value);
            return this;
        }

        /**
         * Builds the immutable response.
         *
         * @return immutable response
         */
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
