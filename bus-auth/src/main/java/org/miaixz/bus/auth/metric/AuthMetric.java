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
package org.miaixz.bus.auth.metric;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.metric.shared.state.AtomicCacheStateStore;
import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.core.basic.normal.Errors;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.Port;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.core.net.tls.TlsCipherSuite;
import org.miaixz.bus.core.net.tls.TlsClientAuth;
import org.miaixz.bus.core.net.tls.TlsVersion;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.extra.json.JsonProvider;
import org.miaixz.bus.fabric.guard.route.AddressPolicy;

/**
 * Defines the immutable cross-protocol authentication runtime contract and all product-supplied ports.
 *
 * @author Kimi Liu
 */
public final class AuthMetric {

    /**
     * Maximum printable identifier length.
     */
    private static final int MAX_IDENTIFIER_LENGTH = 128;

    /**
     * Maximum invocation timeout.
     */
    private static final Duration MAX_INVOCATION_TIMEOUT = Duration.ofSeconds(30);

    /**
     * Default connection timeout for fixed transport policies.
     */
    private static final Duration DEFAULT_CONNECT_TIMEOUT = Duration.ofSeconds(10);

    /**
     * Default read timeout for fixed transport policies.
     */
    private static final Duration DEFAULT_READ_TIMEOUT = Duration.ofSeconds(30);

    /**
     * Default response bound for fixed transport policies.
     */
    private static final long DEFAULT_RESPONSE_BYTES = Normal.MEBI;

    /**
     * TLS versions admitted by authentication policies.
     */
    private static final List<TlsVersion> SECURE_TLS_VERSIONS = List.of(TlsVersion.TLSv1_3, TlsVersion.TLSv1_2);

    /**
     * AEAD cipher suites admitted by authentication policies.
     */
    private static final List<TlsCipherSuite> SECURE_CIPHERS = List.of(
            TlsCipherSuite.TLS_AES_128_GCM_SHA256,
            TlsCipherSuite.TLS_AES_256_GCM_SHA384,
            TlsCipherSuite.TLS_CHACHA20_POLY1305_SHA256,
            TlsCipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256,
            TlsCipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,
            TlsCipherSuite.TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384,
            TlsCipherSuite.TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384,
            TlsCipherSuite.TLS_ECDHE_ECDSA_WITH_CHACHA20_POLY1305_SHA256,
            TlsCipherSuite.TLS_ECDHE_RSA_WITH_CHACHA20_POLY1305_SHA256);

    /**
     * Prevents construction of the contract namespace.
     */
    private AuthMetric() {
        // No initialization required.
    }

    /**
     * Creates the sole tenant-aware state adapter over a product-owned atomic cache.
     *
     * <p>
     * The returned adapter does not close the cache. The product must retain and close the cache only after all state
     * operations and the authentication runtime have stopped.
     * </p>
     *
     * @param cache product-owned atomic byte cache
     * @return tenant-aware authentication state store
     */
    public static StateStore atomicStateStore(final CacheX<String, byte[]> cache) {
        return new AtomicCacheStateStore(cache);
    }

    /**
     * Creates the sole successful outcome for an operation with no return value.
     *
     * @return successful Void outcome carrying the Java-standard null completion value
     */
    public static Outcome<Void> completed() {
        return new Success<>(null);
    }

    /**
     * Validates one positive limit no greater than the strict maximum.
     *
     * @param value   configured limit
     * @param maximum strict maximum
     * @param name    diagnostic name
     */
    private static void validateLimit(final int value, final int maximum, final String name) {
        Assert.isTrue(
                value > Normal._0 && value <= maximum,
                () -> new ValidateException(name + " must be positive and no greater than the strict limit"));
    }

    /**
     * Validates a required runtime port.
     *
     * @param value port value
     * @param name  diagnostic name
     * @param <T>   port type
     * @return validated port
     */
    private static <T> T port(final T value, final String name) {
        return Assert.notNull(value, () -> new ValidateException(name + " must not be null"));
    }

    /**
     * Returns a required trimmed text value.
     *
     * @param value text value
     * @param name  diagnostic name
     * @return trimmed text
     */
    private static String required(final String value, final String name) {
        return StringKit.trim(Assert.notBlank(value, () -> new ValidateException(name + " must not be blank")));
    }

    /**
     * Returns validated printable ASCII identifier text.
     *
     * @param value identifier text
     * @param name  diagnostic name
     * @return validated identifier
     */
    private static String printable(final String value, final String name) {
        final String current = required(value, name);
        Assert.isTrue(
                current.length() <= MAX_IDENTIFIER_LENGTH,
                () -> new ValidateException(name + " must contain at most 128 characters"));
        for (int index = Normal._0; index < current.length(); index++) {
            final char character = current.charAt(index);
            if (character < 0x20 || character > 0x7e) {
                throw new ValidateException(name + " must contain printable ASCII only");
            }
        }
        return current;
    }

    /**
     * Returns independent required bytes.
     *
     * @param value source bytes
     * @param name  diagnostic name
     * @return copied bytes
     */
    private static byte[] bytes(final byte[] value, final String name) {
        return Assert.notNull(value, () -> new ValidateException(name + " must not be null")).clone();
    }

    /**
     * Rejects carriage returns and line feeds in protocol text.
     *
     * @param value protocol text
     * @param name  diagnostic name
     * @return validated text
     */
    private static String rejectLineBreaks(final String value, final String name) {
        if (value.indexOf('\r') >= Normal._0 || value.indexOf('\n') >= Normal._0) {
            throw new ValidateException(name + " must not contain line breaks");
        }
        return value;
    }

    /**
     * Validates one absolute request URI.
     *
     * @param value request URI
     * @return validated URI
     */
    private static URI validateUri(final URI value) {
        final URI uri = Assert.notNull(value, () -> new ValidateException("Request URI must not be null"));
        Assert.isTrue(
                uri.isAbsolute() && uri.getUserInfo() == null && uri.getFragment() == null,
                () -> new ValidateException(
                        "Request URI must be absolute and contain no user information or fragment"));
        return uri;
    }

    /**
     * Normalizes and snapshots HTTP headers.
     *
     * @param values source headers
     * @return normalized immutable headers
     */
    private static Map<String, List<String>> normalizeHeaders(final Map<String, List<String>> values) {
        final Map<String, List<String>> source = Assert
                .notNull(values, () -> new ValidateException("Request headers must not be null"));
        final LinkedHashMap<String, List<String>> result = new LinkedHashMap<>();
        for (final Map.Entry<String, List<String>> entry : source.entrySet()) {
            final String name = rejectLineBreaks(required(entry.getKey(), "Header name"), "Header name")
                    .toLowerCase(Locale.ROOT);
            final ArrayList<String> merged = new ArrayList<>(result.getOrDefault(name, List.of()));
            for (final String value : Assert
                    .notNull(entry.getValue(), () -> new ValidateException("Header values must not be null"))) {
                merged.add(
                        rejectLineBreaks(
                                Assert.notNull(value, () -> new ValidateException("Header value must not be null")),
                                "Header value"));
            }
            result.put(name, List.copyOf(merged));
        }
        return Map.copyOf(result);
    }

    /**
     * Snapshots a map of string lists.
     *
     * @param values source map
     * @param name   diagnostic name
     * @return immutable map
     */
    private static Map<String, List<String>> stringLists(final Map<String, List<String>> values, final String name) {
        final Map<String, List<String>> source = Assert
                .notNull(values, () -> new ValidateException(name + " must not be null"));
        final LinkedHashMap<String, List<String>> result = new LinkedHashMap<>();
        for (final Map.Entry<String, List<String>> entry : source.entrySet()) {
            final String key = required(entry.getKey(), name + " key");
            final ArrayList<String> list = new ArrayList<>();
            for (final String value : Assert
                    .notNull(entry.getValue(), () -> new ValidateException(name + " values must not be null"))) {
                list.add(Assert.notNull(value, () -> new ValidateException(name + " value must not be null")));
            }
            result.put(key, List.copyOf(list));
        }
        return Map.copyOf(result);
    }

    /**
     * Creates a recursively immutable string-keyed map.
     *
     * @param values source map
     * @param name   diagnostic name
     * @return recursively immutable map
     */
    private static Map<String, Object> immutableMap(final Map<String, Object> values, final String name) {
        final Map<String, Object> source = Assert
                .notNull(values, () -> new ValidateException(name + " must not be null"));
        final LinkedHashMap<String, Object> result = new LinkedHashMap<>();
        for (final Map.Entry<String, Object> entry : source.entrySet()) {
            result.put(required(entry.getKey(), name + " key"), immutable(entry.getValue(), name + " value"));
        }
        return Map.copyOf(result);
    }

    /**
     * Creates one recursively immutable supported attribute value.
     *
     * @param value source value
     * @param name  diagnostic name
     * @return immutable value
     */
    private static Object immutable(final Object value, final String name) {
        final Object current = Assert.notNull(value, () -> new ValidateException(name + " must not be null"));
        if (current instanceof byte[] bytes) {
            return bytes.clone();
        }
        if (current instanceof char[] characters) {
            return characters.clone();
        }
        if (current instanceof Map<?, ?> map) {
            final LinkedHashMap<Object, Object> result = new LinkedHashMap<>();
            for (final Map.Entry<?, ?> entry : map.entrySet()) {
                result.put(
                        immutable(entry.getKey(), name + " map key"),
                        immutable(entry.getValue(), name + " map value"));
            }
            return Map.copyOf(result);
        }
        if (current instanceof List<?> list) {
            final ArrayList<Object> result = new ArrayList<>(list.size());
            for (final Object element : list) {
                result.add(immutable(element, name + " list element"));
            }
            return List.copyOf(result);
        }
        if (current instanceof Set<?> set) {
            final LinkedHashSet<Object> result = new LinkedHashSet<>();
            for (final Object element : set) {
                result.add(immutable(element, name + " set element"));
            }
            return Set.copyOf(result);
        }
        return current;
    }

    /**
     * Creates an immutable non-null set.
     *
     * @param values source values
     * @param name   diagnostic name
     * @param <T>    element type
     * @return immutable set
     */
    private static <T> Set<T> immutableSet(final Set<T> values, final String name) {
        final Set<T> source = Assert.notNull(values, () -> new ValidateException(name + " must not be null"));
        for (final T value : source) {
            Assert.notNull(value, () -> new ValidateException(name + " must contain no null elements"));
        }
        return Set.copyOf(source);
    }

    /**
     * Validates the permitted TLS version list.
     *
     * @param values TLS versions
     * @return immutable permitted versions
     */
    private static List<TlsVersion> secureVersions(final List<TlsVersion> values) {
        final List<TlsVersion> source = List
                .copyOf(Assert.notNull(values, () -> new ValidateException("TLS versions must not be null")));
        for (final TlsVersion value : source) {
            Assert.isTrue(
                    value == TlsVersion.TLSv1_3 || value == TlsVersion.TLSv1_2,
                    () -> new ValidateException("Only TLS 1.3 and TLS 1.2 are permitted"));
        }
        return source;
    }

    /**
     * Validates the permitted AEAD cipher list.
     *
     * @param values cipher suites
     * @return immutable permitted cipher suites
     */
    private static List<TlsCipherSuite> secureCiphers(final List<TlsCipherSuite> values) {
        final List<TlsCipherSuite> source = List
                .copyOf(Assert.notNull(values, () -> new ValidateException("TLS cipher suites must not be null")));
        for (final TlsCipherSuite value : source) {
            Assert.isTrue(
                    SECURE_CIPHERS.contains(value),
                    () -> new ValidateException("TLS cipher suite is not permitted"));
        }
        return source;
    }

    /**
     * Validates CIDR notation through the shared Fabric address policy.
     *
     * @param schemes allowed schemes
     * @param ports   allowed ports
     * @param targets target CIDRs
     * @param peers   peer CIDRs
     */
    private static void validateAddressPolicy(
            final Set<Protocol> schemes,
            final Set<Integer> ports,
            final Set<String> targets,
            final Set<String> peers) {
        new AddressPolicy(schemes, ports, targets, peers);
    }

    /**
     * Validates cross-field transport policy invariants.
     *
     * @param schemes              allowed schemes
     * @param versions             TLS versions
     * @param ciphers              cipher suites
     * @param clientAuth           client authentication mode
     * @param requireLocalIdentity local identity requirement
     * @param requireStartTls      StartTLS requirement
     * @param redirectLimit        redirect limit
     * @param verifyHostname       hostname verification requirement
     */
    private static void validateTransportShape(
            final Set<Protocol> schemes,
            final List<TlsVersion> versions,
            final List<TlsCipherSuite> ciphers,
            final TlsClientAuth clientAuth,
            final boolean requireLocalIdentity,
            final boolean requireStartTls,
            final int redirectLimit,
            final boolean verifyHostname) {
        final boolean radius = schemes.equals(Set.of(Protocol.RADIUS));
        if (radius) {
            Assert.isTrue(
                    versions.isEmpty() && ciphers.isEmpty() && clientAuth == TlsClientAuth.NONE && !requireLocalIdentity
                            && !requireStartTls && redirectLimit == Normal._0 && !verifyHostname,
                    () -> new ValidateException("RADIUS policy must not enable HTTP or TLS features"));
            return;
        }
        Assert.isTrue(
                !versions.isEmpty() && !ciphers.isEmpty() && verifyHostname,
                () -> new ValidateException("Secure transport policies require TLS and hostname verification"));
        Assert.isTrue(
                !requireStartTls || schemes.equals(Set.of(Protocol.LDAP)),
                () -> new ValidateException("StartTLS is available only for LDAP"));
        Assert.isTrue(
                redirectLimit == Normal._0 || schemes.equals(Set.of(Protocol.HTTPS)),
                () -> new ValidateException("Redirects are available only for HTTPS"));
    }

    /**
     * Returns one positive duration.
     *
     * @param value duration value
     * @param name  diagnostic name
     * @return positive duration
     */
    private static Duration positive(final Duration value, final String name) {
        final Duration duration = Assert.notNull(value, () -> new ValidateException(name + " must not be null"));
        Assert.isTrue(
                !duration.isZero() && !duration.isNegative(),
                () -> new ValidateException(name + " must be positive"));
        return duration;
    }

    /**
     * Resolves a protocol registered default port.
     *
     * @param protocol endpoint protocol
     * @return registered port
     */
    private static int defaultPort(final Protocol protocol) {
        return switch (protocol) {
            case HTTP -> Port._80.getPort();
            case HTTPS -> Port._443.getPort();
            case LDAP -> Port._389.getPort();
            case LDAPS -> Port._636.getPort();
            case RADIUS -> Port._1812.getPort();
            default -> throw new ValidateException("Protocol does not define an authentication default port");
        };
    }

    /**
     * Stable failure classifications used by every protocol mapper.
     */
    public enum FailureKind {
        /**
         * Input validation rejection.
         */
        VALIDATION,
        /**
         * Authentication rejection.
         */
        AUTHENTICATION,
        /**
         * Authorization rejection.
         */
        AUTHORIZATION,
        /**
         * State conflict.
         */
        CONFLICT,
        /**
         * Rate limit rejection.
         */
        RATE_LIMIT,
        /**
         * Remote dependency failure.
         */
        REMOTE,
        /**
         * Runtime configuration failure.
         */
        CONFIGURATION,
        /**
         * Unrecoverable internal failure.
         */
        INTERNAL
    }

    /**
     * Closed result algebra returned by authentication engines.
     *
     * @param <T> successful value type
     */
    public sealed interface Outcome<T> permits Success, Rejected, Failed {
    }

    /**
     * Resolves registered clients.
     */
    @FunctionalInterface
    public interface ClientResolver {

        /**
         * Resolves one registered client.
         *
         * @param invocation operation context
         * @param clientId   client identifier
         * @return asynchronous optional client
         */
        CompletionStage<Optional<Client>> resolve(Invocation invocation, String clientId);
    }

    /**
     * Resolves authentication subjects.
     */
    @FunctionalInterface
    public interface SubjectResolver {

        /**
         * Resolves one authentication subject.
         *
         * @param invocation operation context
         * @param subjectId  subject identifier
         * @return asynchronous optional subject
         */
        CompletionStage<Optional<Subject>> resolve(Invocation invocation, String subjectId);
    }

    /**
     * Resolves tenant- and algorithm-scoped key candidates.
     */
    @FunctionalInterface
    public interface KeyResolver {

        /**
         * Resolves tenant-scoped key candidates.
         *
         * @param invocation operation context
         * @param use        key use
         * @param algorithm  algorithm
         * @param keyId      optional key identifier
         * @return key candidates
         */
        CompletionStage<List<KeyMaterial>> resolve(Invocation invocation, String use, String algorithm, String keyId);
    }

    /**
     * Resolves copied secret characters.
     */
    @FunctionalInterface
    public interface SecretResolver {

        /**
         * Resolves copied secret characters.
         *
         * @param invocation operation context
         * @param kind       secret kind
         * @param identifier secret identifier
         * @return copied secret
         */
        CompletionStage<char[]> resolve(Invocation invocation, String kind, String identifier);
    }

    /**
     * Provides atomic tenant-aware state operations.
     */
    public interface StateStore {

        /**
         * Creates state only when no unexpired value exists.
         *
         * @param invocation context
         * @param key        state key
         * @param value      copied bytes
         * @param ttl        positive TTL
         * @return insertion result
         */
        CompletionStage<Boolean> putIfAbsent(Invocation invocation, String key, byte[] value, Duration ttl);

        /**
         * Reads one state value.
         *
         * @param invocation context
         * @param key        state key
         * @return copied value or empty
         */
        CompletionStage<Optional<byte[]>> get(Invocation invocation, String key);

        /**
         * Atomically reads and removes one state value.
         *
         * @param invocation context
         * @param key        state key
         * @return atomically removed copied value or empty
         */
        CompletionStage<Optional<byte[]>> take(Invocation invocation, String key);

        /**
         * Atomically replaces an expected state value.
         *
         * @param invocation context
         * @param key        state key
         * @param expected   expected bytes
         * @param update     replacement bytes
         * @param ttl        replacement TTL
         * @return replacement result
         */
        CompletionStage<Boolean> compareAndSet(
                Invocation invocation,
                String key,
                byte[] expected,
                byte[] update,
                Duration ttl);

        /**
         * Atomically removes one state value.
         *
         * @param invocation context
         * @param key        state key
         * @return removal result
         */
        CompletionStage<Boolean> remove(Invocation invocation, String key);
    }

    /**
     * Persists and resolves authorization grants.
     */
    public interface GrantStore {

        /**
         * Saves one grant snapshot.
         *
         * @param invocation context
         * @param grant      grant snapshot
         * @return completion
         */
        CompletionStage<Void> save(Invocation invocation, Grant grant);

        /**
         * Finds one grant.
         *
         * @param invocation context
         * @param grantId    grant identifier
         * @return optional grant
         */
        CompletionStage<Optional<Grant>> find(Invocation invocation, String grantId);

        /**
         * Revokes one grant.
         *
         * @param invocation context
         * @param grantId    grant identifier
         * @return revocation result
         */
        CompletionStage<Boolean> revoke(Invocation invocation, String grantId);
    }

    /**
     * Persists and resolves subject consent.
     */
    public interface ConsentStore {

        /**
         * Saves one consent snapshot.
         *
         * @param invocation context
         * @param consent    consent snapshot
         * @return completion
         */
        CompletionStage<Void> save(Invocation invocation, Consent consent);

        /**
         * Finds subject consent for one client.
         *
         * @param invocation context
         * @param subjectId  subject identifier
         * @param clientId   client identifier
         * @return optional consent
         */
        CompletionStage<Optional<Consent>> find(Invocation invocation, String subjectId, String clientId);
    }

    /**
     * Emits immutable security events.
     */
    @FunctionalInterface
    public interface AuditSink {

        /**
         * Emits one immutable security event.
         *
         * @param invocation context
         * @param event      security event
         * @return asynchronous completion
         */
        CompletionStage<Void> emit(Invocation invocation, SecurityEvent event);
    }

    /**
     * Supplies all security-sensitive current times.
     */
    @FunctionalInterface
    public interface ClockSource {

        /**
         * Returns the current security time.
         *
         * @return current security time
         */
        Instant now();
    }

    /**
     * Supplies cryptographically secure independent random bytes.
     */
    @FunctionalInterface
    public interface RandomSource {

        /**
         * Returns a new random byte array of the exact requested length.
         *
         * @param length exact requested length
         * @return new random byte array
         */
        byte[] nextBytes(int length);
    }

    /**
     * Exchanges one outbound HTTP request.
     */
    @FunctionalInterface
    public interface ProtocolTransport {

        /**
         * Exchanges one HTTP request.
         *
         * @param invocation context
         * @param request    HTTP request
         * @param policy     transport policy
         * @return response stage
         */
        CompletionStage<Response> exchange(Invocation invocation, Request request, TransportPolicy policy);
    }

    /**
     * Opens one exclusive stream session.
     */
    @FunctionalInterface
    public interface StreamTransport {

        /**
         * Opens one exclusive stream session.
         *
         * @param invocation context
         * @param endpoint   remote endpoint
         * @param policy     transport policy
         * @return session stage
         */
        CompletionStage<StreamSession> open(Invocation invocation, Endpoint endpoint, TransportPolicy policy);
    }

    /**
     * Binds a managed stream server.
     */
    @FunctionalInterface
    public interface StreamServerTransport {

        /**
         * Binds one managed stream server.
         *
         * @param invocation context
         * @param endpoint   local endpoint
         * @param policy     transport policy
         * @param handler    session handler
         * @return binding stage
         */
        CompletionStage<StreamServerBinding> bind(
                Invocation invocation,
                Endpoint endpoint,
                TransportPolicy policy,
                StreamSessionHandler handler);
    }

    /**
     * Exclusive asynchronous stream session.
     */
    public interface StreamSession {

        /**
         * Writes copied outbound bytes.
         *
         * @param bytes copied outbound bytes
         * @return completion
         */
        CompletionStage<Void> write(byte[] bytes);

        /**
         * Reads one bounded stream chunk.
         *
         * @param maxBytes maximum returned bytes
         * @return read stage
         */
        CompletionStage<StreamRead> readChunk(int maxBytes);

        /**
         * Upgrades this session to TLS.
         *
         * @param policy StartTLS policy
         * @return upgrade completion
         */
        CompletionStage<Void> upgradeTls(TransportPolicy policy);

        /**
         * Closes this session idempotently.
         *
         * @return idempotent close completion
         */
        CompletionStage<Void> close();
    }

    /**
     * Managed stream server binding.
     */
    public interface StreamServerBinding {

        /**
         * Returns the effective local endpoint.
         *
         * @return effective local endpoint
         */
        Endpoint localEndpoint();

        /**
         * Closes the binding after session drain.
         *
         * @return shared drain close completion
         */
        CompletionStage<Void> close();
    }

    /**
     * Handles one accepted stream session.
     */
    @FunctionalInterface
    public interface StreamSessionHandler {

        /**
         * Handles one accepted session.
         *
         * @param invocation accepted session context
         * @param session    exclusive session
         * @return handler completion
         */
        CompletionStage<Void> onSession(Invocation invocation, StreamSession session);
    }

    /**
     * Exchanges one complete UDP datagram.
     */
    @FunctionalInterface
    public interface DatagramTransport {

        /**
         * Exchanges one complete datagram.
         *
         * @param invocation context
         * @param endpoint   remote endpoint
         * @param datagram   request datagram
         * @param policy     transport policy
         * @return response datagram
         */
        CompletionStage<Datagram> exchange(
                Invocation invocation,
                Endpoint endpoint,
                Datagram datagram,
                TransportPolicy policy);
    }

    /**
     * Binds a managed UDP server.
     */
    @FunctionalInterface
    public interface DatagramServerTransport {

        /**
         * Binds one managed datagram server.
         *
         * @param invocation context
         * @param endpoint   local endpoint
         * @param policy     transport policy
         * @param handler    datagram handler
         * @return binding stage
         */
        CompletionStage<DatagramServerBinding> bind(
                Invocation invocation,
                Endpoint endpoint,
                TransportPolicy policy,
                DatagramHandler handler);
    }

    /**
     * Managed datagram server binding.
     */
    public interface DatagramServerBinding {

        /**
         * Returns the effective local endpoint.
         *
         * @return effective local endpoint
         */
        Endpoint localEndpoint();

        /**
         * Closes the binding after handler drain.
         *
         * @return shared drain close completion
         */
        CompletionStage<Void> close();
    }

    /**
     * Handles one complete datagram.
     */
    @FunctionalInterface
    public interface DatagramHandler {

        /**
         * Handles one complete datagram.
         *
         * @param invocation packet context
         * @param datagram   packet
         * @return optional response stage
         */
        CompletionStage<Optional<Datagram>> onDatagram(Invocation invocation, Datagram datagram);
    }

    /**
     * Immutable invocation context shared by one protocol operation.
     *
     * @param tenantId      printable tenant identifier
     * @param correlationId printable correlation identifier
     * @param timeout       positive operation timeout no greater than thirty seconds
     * @param remoteAddress optional remote endpoint
     * @param attributes    recursively immutable protocol attributes
     */
    public record Invocation(String tenantId, String correlationId, Duration timeout, Endpoint remoteAddress,
            Map<String, Object> attributes) {

        /**
         * Validates and snapshots invocation state.
         *
         * @param tenantId      tenant identifier
         * @param correlationId correlation identifier
         * @param timeout       operation timeout
         * @param remoteAddress optional remote endpoint
         * @param attributes    invocation attributes
         */
        public Invocation {
            tenantId = printable(tenantId, "Tenant identifier");
            correlationId = printable(correlationId, "Correlation identifier");
            timeout = Assert.notNull(timeout, () -> new ValidateException("Invocation timeout must not be null"));
            Assert.isTrue(
                    !timeout.isZero() && !timeout.isNegative() && timeout.compareTo(MAX_INVOCATION_TIMEOUT) <= 0,
                    () -> new ValidateException("Invocation timeout must be between zero and thirty seconds"));
            attributes = immutableMap(attributes, "Invocation attributes");
        }

        /**
         * Returns a recursively independent attribute snapshot.
         *
         * @return recursively independent attributes
         */
        @Override
        public Map<String, Object> attributes() {
            return immutableMap(attributes, "Invocation attributes");
        }
    }

    /**
     * Immutable outbound HTTP request preserving raw signature inputs.
     *
     * @param method   bus-core HTTP method
     * @param uri      absolute request URI without user information or fragment
     * @param headers  normalized lower-case header values
     * @param query    decoded query values
     * @param rawQuery original raw query text
     * @param body     original request body bytes
     */
    public record Request(Http.Method method, URI uri, Map<String, List<String>> headers,
            Map<String, List<String>> query, String rawQuery, byte[] body) {

        /**
         * Validates and snapshots request state.
         *
         * @param method   HTTP method
         * @param uri      request URI
         * @param headers  request headers
         * @param query    decoded query
         * @param rawQuery raw query
         * @param body     request body
         */
        public Request {
            method = Assert.notNull(method, () -> new ValidateException("Request method must not be null"));
            uri = validateUri(uri);
            headers = normalizeHeaders(headers);
            query = stringLists(query, "Request query");
            rawQuery = rawQuery == null ? "" : rejectLineBreaks(rawQuery, "Raw query");
            body = bytes(body, "Request body");
        }

        /**
         * Returns an independent body copy.
         *
         * @return copied body bytes
         */
        @Override
        public byte[] body() {
            return body.clone();
        }
    }

    /**
     * Immutable HTTP response returned by the protocol transport.
     *
     * @param status  HTTP status code
     * @param headers normalized response headers
     * @param body    bounded response body
     */
    public record Response(int status, Map<String, List<String>> headers, byte[] body) {

        /**
         * Validates and snapshots response state.
         *
         * @param status  HTTP status
         * @param headers response headers
         * @param body    response body
         */
        public Response {
            Assert.isTrue(
                    status >= 100 && status <= 599,
                    () -> new ValidateException("Response status must be between 100 and 599"));
            headers = normalizeHeaders(headers);
            body = bytes(body, "Response body");
        }

        /**
         * Returns an independent response body copy.
         *
         * @return copied response body
         */
        @Override
        public byte[] body() {
            return body.clone();
        }
    }

    /**
     * Immutable resolved subject.
     *
     * @param id         stable subject identifier
     * @param attributes recursively immutable subject attributes
     */
    public record Subject(String id, Map<String, Object> attributes) {

        /**
         * Validates and snapshots subject state.
         *
         * @param id         subject identifier
         * @param attributes subject attributes
         */
        public Subject {
            id = required(id, "Subject identifier");
            attributes = immutableMap(attributes, "Subject attributes");
        }

        /**
         * Returns a recursively independent attribute snapshot.
         *
         * @return recursively independent attributes
         */
        @Override
        public Map<String, Object> attributes() {
            return immutableMap(attributes, "Subject attributes");
        }
    }

    /**
     * Immutable registered client.
     *
     * @param id           stable client identifier
     * @param redirectUris immutable registered redirect URI set
     * @param attributes   recursively immutable client attributes
     */
    public record Client(String id, Set<URI> redirectUris, Map<String, Object> attributes) {

        /**
         * Validates and snapshots client state.
         *
         * @param id           client identifier
         * @param redirectUris registered redirect URIs
         * @param attributes   client attributes
         */
        public Client {
            id = required(id, "Client identifier");
            redirectUris = immutableSet(redirectUris, "Client redirect URIs");
            attributes = immutableMap(attributes, "Client attributes");
        }

        /**
         * Returns a recursively independent attribute snapshot.
         *
         * @return recursively independent attributes
         */
        @Override
        public Map<String, Object> attributes() {
            return immutableMap(attributes, "Client attributes");
        }
    }

    /**
     * Immutable cryptographic key material returned by the key resolver.
     *
     * @param keyId     stable key identifier
     * @param use       key use
     * @param algorithm fixed algorithm identifier
     * @param material  encoded key material
     */
    public record KeyMaterial(String keyId, String use, String algorithm, byte[] material) {

        /**
         * Validates and snapshots key material.
         *
         * @param keyId     key identifier
         * @param use       key use
         * @param algorithm key algorithm
         * @param material  encoded key material
         */
        public KeyMaterial {
            keyId = required(keyId, "Key identifier");
            use = required(use, "Key use");
            algorithm = required(algorithm, "Key algorithm");
            material = bytes(material, "Key material");
        }

        /**
         * Returns an independent encoded key copy.
         *
         * @return independent encoded key copy
         */
        @Override
        public byte[] material() {
            return material.clone();
        }

        /**
         * Returns a fixed redacted representation.
         *
         * @return fixed redacted representation
         */
        @Override
        public String toString() {
            return "KeyMaterial[REDACTED]";
        }
    }

    /**
     * Immutable authorization grant.
     *
     * @param id         stable grant identifier
     * @param subjectId  subject identifier
     * @param clientId   client identifier
     * @param scopes     immutable granted scopes
     * @param expiresAt  grant expiration
     * @param attributes recursively immutable grant attributes
     */
    public record Grant(String id, String subjectId, String clientId, Set<String> scopes, Instant expiresAt,
            Map<String, Object> attributes) {

        /**
         * Validates and snapshots grant state.
         *
         * @param id         grant identifier
         * @param subjectId  subject identifier
         * @param clientId   client identifier
         * @param scopes     granted scopes
         * @param expiresAt  expiration time
         * @param attributes grant attributes
         */
        public Grant {
            id = required(id, "Grant identifier");
            subjectId = required(subjectId, "Grant subject identifier");
            clientId = required(clientId, "Grant client identifier");
            scopes = immutableSet(scopes, "Grant scopes");
            expiresAt = Assert.notNull(expiresAt, () -> new ValidateException("Grant expiration must not be null"));
            attributes = immutableMap(attributes, "Grant attributes");
        }

        /**
         * Returns a recursively independent attribute snapshot.
         *
         * @return recursively independent attributes
         */
        @Override
        public Map<String, Object> attributes() {
            return immutableMap(attributes, "Grant attributes");
        }
    }

    /**
     * Immutable subject consent.
     *
     * @param subjectId subject identifier
     * @param clientId  client identifier
     * @param scopes    immutable consented scopes
     * @param grantedAt consent time
     */
    public record Consent(String subjectId, String clientId, Set<String> scopes, Instant grantedAt) {

        /**
         * Validates and snapshots consent state.
         *
         * @param subjectId subject identifier
         * @param clientId  client identifier
         * @param scopes    consented scopes
         * @param grantedAt consent time
         */
        public Consent {
            subjectId = required(subjectId, "Consent subject identifier");
            clientId = required(clientId, "Consent client identifier");
            scopes = immutableSet(scopes, "Consent scopes");
            grantedAt = Assert.notNull(grantedAt, () -> new ValidateException("Consent time must not be null"));
        }
    }

    /**
     * Immutable security audit event.
     *
     * @param type          stable event type
     * @param occurredAt    event time from the runtime clock
     * @param correlationId invocation correlation identifier
     * @param subjectId     optional subject identifier
     * @param attributes    recursively immutable safe event attributes
     */
    public record SecurityEvent(String type, Instant occurredAt, String correlationId, String subjectId,
            Map<String, Object> attributes) {

        /**
         * Validates and snapshots event state.
         *
         * @param type          event type
         * @param occurredAt    event time
         * @param correlationId correlation identifier
         * @param subjectId     optional subject identifier
         * @param attributes    event attributes
         */
        public SecurityEvent {
            type = required(type, "Security event type");
            occurredAt = Assert
                    .notNull(occurredAt, () -> new ValidateException("Security event time must not be null"));
            correlationId = printable(correlationId, "Security event correlation identifier");
            subjectId = subjectId == null ? null : required(subjectId, "Security event subject identifier");
            attributes = immutableMap(attributes, "Security event attributes");
        }

        /**
         * Returns a recursively independent attribute snapshot.
         *
         * @return recursively independent attributes
         */
        @Override
        public Map<String, Object> attributes() {
            return immutableMap(attributes, "Security event attributes");
        }
    }

    /**
     * Immutable safe protocol failure.
     *
     * @param kind      stable failure classification
     * @param error     copied Bus error entry
     * @param retryable whether a later attempt may succeed
     */
    public record Failure(FailureKind kind, Errors error, boolean retryable) {

        /**
         * Validates and snapshots the supplied Bus error.
         *
         * @param kind      failure classification
         * @param error     Bus error
         * @param retryable retry flag
         */
        public Failure {
            kind = Assert.notNull(kind, () -> new ValidateException("Failure kind must not be null"));
            final Errors current = Assert.notNull(error, () -> new ValidateException("Failure error must not be null"));
            error = new Errors.Entry(required(current.getKey(), "Failure error key"),
                    required(current.getValue(), "Failure error value"));
        }
    }

    /**
     * Successful engine result.
     *
     * @param value result value, or {@code null} for a successful Void operation created by {@link #completed()}
     * @param <T>   value type
     */
    public record Success<T>(T value) implements Outcome<T> {

        /**
         * Retains the result value. A {@code null} value is reserved for the Void completion factory.
         *
         * @param value success value
         */
        public Success {
            // No initialization required.
            // A null value is the only Java representation of successful Outcome<Void> completion.
        }
    }

    /**
     * Stable protocol rejection.
     *
     * @param failure safe rejection detail
     * @param <T>     absent success type
     */
    public record Rejected<T>(Failure failure) implements Outcome<T> {

        /**
         * Validates the rejection failure.
         *
         * @param failure rejection failure
         */
        public Rejected {
            failure = Assert.notNull(failure, () -> new ValidateException("Rejected failure must not be null"));
        }
    }

    /**
     * Port, configuration, or system failure retaining a non-serialized cause.
     *
     * @param failure safe failure detail
     * @param cause   original failure cause
     * @param <T>     absent success type
     */
    public record Failed<T>(Failure failure, Throwable cause) implements Outcome<T> {

        /**
         * Validates the failure and original cause.
         *
         * @param failure safe failure
         * @param cause   original cause
         */
        public Failed {
            failure = Assert.notNull(failure, () -> new ValidateException("Failed failure must not be null"));
            cause = Assert.notNull(cause, () -> new ValidateException("Failed cause must not be null"));
        }

        /**
         * Returns a fixed redacted representation.
         *
         * @return fixed redacted representation
         */
        @Override
        public String toString() {
            return "Failed[REDACTED]";
        }
    }

    /**
     * Immutable parser and transport allocation limits.
     *
     * @param maxHeaderBytes        maximum aggregate header bytes
     * @param maxParameters         maximum parameter count
     * @param maxParameterBytes     maximum bytes in one parameter
     * @param maxJsonBytes          maximum JSON document bytes
     * @param maxJsonDepth          maximum JSON nesting depth
     * @param maxJwtBytes           maximum JWT bytes
     * @param maxLdapMessageBytes   maximum LDAP message bytes
     * @param maxLdapDepth          maximum LDAP BER nesting depth
     * @param maxScimBulkBytes      maximum SCIM bulk request bytes
     * @param maxScimBulkOperations maximum SCIM bulk operations
     * @param maxRadiusPacketBytes  maximum RADIUS packet bytes
     * @param maxSsfSetBytes        maximum SSF SET bytes
     */
    public record Limits(int maxHeaderBytes, int maxParameters, int maxParameterBytes, int maxJsonBytes,
            int maxJsonDepth, int maxJwtBytes, int maxLdapMessageBytes, int maxLdapDepth, int maxScimBulkBytes,
            int maxScimBulkOperations, int maxRadiusPacketBytes, int maxSsfSetBytes) {

        /**
         * Strict upper-bound values.
         */
        private static final Limits STRICT = new Limits(32 * Normal._1024, 128, 8 * Normal._1024, (int) Normal.MEBI, 32,
                16 * Normal._1024, 2 * (int) Normal.MEBI, 32, (int) Normal.MEBI, 1000, 4096, 64 * Normal._1024);

        /**
         * Validates that every limit is positive and no weaker than {@link #strict()}.
         *
         * @param maxHeaderBytes        header byte limit
         * @param maxParameters         parameter count limit
         * @param maxParameterBytes     parameter byte limit
         * @param maxJsonBytes          JSON byte limit
         * @param maxJsonDepth          JSON depth limit
         * @param maxJwtBytes           JWT byte limit
         * @param maxLdapMessageBytes   LDAP message byte limit
         * @param maxLdapDepth          LDAP depth limit
         * @param maxScimBulkBytes      SCIM bulk byte limit
         * @param maxScimBulkOperations SCIM bulk operation limit
         * @param maxRadiusPacketBytes  RADIUS packet byte limit
         * @param maxSsfSetBytes        SSF SET byte limit
         */
        public Limits {
            validateLimit(maxHeaderBytes, 32 * Normal._1024, "Header byte limit");
            validateLimit(maxParameters, 128, "Parameter count limit");
            validateLimit(maxParameterBytes, 8 * Normal._1024, "Parameter byte limit");
            validateLimit(maxJsonBytes, (int) Normal.MEBI, "JSON byte limit");
            validateLimit(maxJsonDepth, 32, "JSON depth limit");
            validateLimit(maxJwtBytes, 16 * Normal._1024, "JWT byte limit");
            validateLimit(maxLdapMessageBytes, 2 * (int) Normal.MEBI, "LDAP message byte limit");
            validateLimit(maxLdapDepth, 32, "LDAP depth limit");
            validateLimit(maxScimBulkBytes, (int) Normal.MEBI, "SCIM bulk byte limit");
            validateLimit(maxScimBulkOperations, 1000, "SCIM bulk operation limit");
            validateLimit(maxRadiusPacketBytes, 4096, "RADIUS packet byte limit");
            validateLimit(maxSsfSetBytes, 64 * Normal._1024, "SSF SET byte limit");
        }

        /**
         * Returns the maximum permitted limits.
         *
         * @return strict immutable limits
         */
        public static Limits strict() {
            return STRICT;
        }
    }

    /**
     * Immutable network and TLS policy consumed by the five transport ports.
     *
     * @param allowedSchemes       permitted Bus protocols
     * @param allowedPorts         permitted destination or bind ports
     * @param tlsVersions          TLS 1.3/1.2 offer list
     * @param cipherSuites         fixed AEAD cipher list
     * @param clientAuth           server-side client certificate mode
     * @param requireLocalIdentity whether a client handshake requires the Context identity
     * @param allowedTargetCidrs   explicit client target networks
     * @param allowedPeerCidrs     explicit server peer networks
     * @param requireStartTls      whether plaintext transport must upgrade before protocol traffic
     * @param redirectLimit        maximum HTTP redirects
     * @param connectTimeout       connection and handshake timeout
     * @param readTimeout          response or stream read timeout
     * @param maxResponseBytes     maximum response or datagram bytes
     * @param verifyHostname       whether TLS hostname verification is mandatory
     */
    public record TransportPolicy(Set<Protocol> allowedSchemes, Set<Integer> allowedPorts, List<TlsVersion> tlsVersions,
            List<TlsCipherSuite> cipherSuites, TlsClientAuth clientAuth, boolean requireLocalIdentity,
            Set<String> allowedTargetCidrs, Set<String> allowedPeerCidrs, boolean requireStartTls, int redirectLimit,
            Duration connectTimeout, Duration readTimeout, long maxResponseBytes, boolean verifyHostname) {

        /**
         * Validates and snapshots the closed transport policy.
         *
         * @param allowedSchemes       allowed schemes
         * @param allowedPorts         allowed ports
         * @param tlsVersions          TLS versions
         * @param cipherSuites         cipher suites
         * @param clientAuth           client authentication mode
         * @param requireLocalIdentity local identity requirement
         * @param allowedTargetCidrs   target CIDRs
         * @param allowedPeerCidrs     peer CIDRs
         * @param requireStartTls      StartTLS requirement
         * @param redirectLimit        redirect limit
         * @param connectTimeout       connect timeout
         * @param readTimeout          read timeout
         * @param maxResponseBytes     response byte limit
         * @param verifyHostname       hostname verification requirement
         */
        public TransportPolicy {
            allowedSchemes = immutableSet(allowedSchemes, "Allowed schemes");
            Assert.isTrue(!allowedSchemes.isEmpty(), () -> new ValidateException("Allowed schemes must not be empty"));
            allowedPorts = immutableSet(allowedPorts, "Allowed ports");
            Assert.isTrue(!allowedPorts.isEmpty(), () -> new ValidateException("Allowed ports must not be empty"));
            tlsVersions = secureVersions(tlsVersions);
            cipherSuites = secureCiphers(cipherSuites);
            clientAuth = Assert.notNull(clientAuth, () -> new ValidateException("TLS client auth must not be null"));
            Assert.isTrue(
                    clientAuth != TlsClientAuth.OPTIONAL,
                    () -> new ValidateException("Optional TLS client authentication is not permitted"));
            allowedTargetCidrs = immutableSet(allowedTargetCidrs, "Allowed target CIDRs");
            allowedPeerCidrs = immutableSet(allowedPeerCidrs, "Allowed peer CIDRs");
            Assert.isTrue(
                    redirectLimit >= Normal._0,
                    () -> new ValidateException("Redirect limit must not be negative"));
            connectTimeout = positive(connectTimeout, "Connect timeout");
            readTimeout = positive(readTimeout, "Read timeout");
            Assert.isTrue(
                    maxResponseBytes > Normal._0,
                    () -> new ValidateException("Maximum response bytes must be positive"));
            validateAddressPolicy(allowedSchemes, allowedPorts, allowedTargetCidrs, allowedPeerCidrs);
            validateTransportShape(
                    allowedSchemes,
                    tlsVersions,
                    cipherSuites,
                    clientAuth,
                    requireLocalIdentity,
                    requireStartTls,
                    redirectLimit,
                    verifyHostname);
        }

        /**
         * Returns the strict HTTPS policy.
         *
         * @return strict HTTPS policy
         */
        public static TransportPolicy strict() {
            return secure(
                    Set.of(Protocol.HTTPS),
                    Set.of(Port._443.getPort()),
                    Set.of(),
                    Set.of(),
                    false,
                    TlsClientAuth.NONE,
                    false,
                    5);
        }

        /**
         * Returns strict HTTPS with additional product-registered ports.
         *
         * @param ports registered HTTPS ports
         * @return HTTPS policy containing port 443 and all supplied ports
         */
        public static TransportPolicy registeredHttpsPorts(final Set<Integer> ports) {
            final LinkedHashSet<Integer> values = new LinkedHashSet<>(immutableSet(ports, "Registered HTTPS ports"));
            values.add(Port._443.getPort());
            return secure(
                    Set.of(Protocol.HTTPS),
                    Set.copyOf(values),
                    Set.of(),
                    Set.of(),
                    false,
                    TlsClientAuth.NONE,
                    false,
                    5);
        }

        /**
         * Creates an LDAPS client policy.
         *
         * @param targetCidrs allowed targets
         * @return LDAPS client policy
         */
        public static TransportPolicy ldapsClient(final Set<String> targetCidrs) {
            return secure(
                    Set.of(Protocol.LDAPS),
                    Set.of(Port._636.getPort()),
                    targetCidrs,
                    Set.of(),
                    false,
                    TlsClientAuth.NONE,
                    false,
                    Normal._0);
        }

        /**
         * Creates an LDAPS server policy.
         *
         * @param peerCidrs allowed peers
         * @return LDAPS server policy
         */
        public static TransportPolicy ldapsServer(final Set<String> peerCidrs) {
            return secure(
                    Set.of(Protocol.LDAPS),
                    Set.of(Port._636.getPort()),
                    Set.of(),
                    peerCidrs,
                    false,
                    TlsClientAuth.NONE,
                    false,
                    Normal._0);
        }

        /**
         * Creates an LDAP StartTLS client policy.
         *
         * @param targetCidrs allowed targets
         * @return LDAP StartTLS client policy
         */
        public static TransportPolicy ldapStartTlsClient(final Set<String> targetCidrs) {
            return secure(
                    Set.of(Protocol.LDAP),
                    Set.of(Port._389.getPort()),
                    targetCidrs,
                    Set.of(),
                    true,
                    TlsClientAuth.NONE,
                    false,
                    Normal._0);
        }

        /**
         * Creates an LDAP StartTLS server policy.
         *
         * @param peerCidrs allowed peers
         * @return LDAP StartTLS server policy
         */
        public static TransportPolicy ldapStartTlsServer(final Set<String> peerCidrs) {
            return secure(
                    Set.of(Protocol.LDAP),
                    Set.of(Port._389.getPort()),
                    Set.of(),
                    peerCidrs,
                    true,
                    TlsClientAuth.NONE,
                    false,
                    Normal._0);
        }

        /**
         * Creates a RADIUS authentication client policy.
         *
         * @param targetCidrs allowed targets
         * @return RADIUS authentication client policy
         */
        public static TransportPolicy radiusAuthenticationClient(final Set<String> targetCidrs) {
            return radius(Port._1812.getPort(), targetCidrs, Set.of());
        }

        /**
         * Creates a RADIUS authentication server policy.
         *
         * @param peerCidrs allowed peers
         * @return RADIUS authentication server policy
         */
        public static TransportPolicy radiusAuthenticationServer(final Set<String> peerCidrs) {
            return radius(Port._1812.getPort(), Set.of(), peerCidrs);
        }

        /**
         * Creates a RADIUS accounting client policy.
         *
         * @param targetCidrs allowed targets
         * @return RADIUS accounting client policy
         */
        public static TransportPolicy radiusAccountingClient(final Set<String> targetCidrs) {
            return radius(Port._1813.getPort(), targetCidrs, Set.of());
        }

        /**
         * Creates a RADIUS accounting server policy.
         *
         * @param peerCidrs allowed peers
         * @return RADIUS accounting server policy
         */
        public static TransportPolicy radiusAccountingServer(final Set<String> peerCidrs) {
            return radius(Port._1813.getPort(), Set.of(), peerCidrs);
        }

        /**
         * Creates one fixed secure policy.
         *
         * @param schemes       allowed schemes
         * @param ports         allowed ports
         * @param targets       target CIDRs
         * @param peers         peer CIDRs
         * @param startTls      StartTLS requirement
         * @param clientAuth    client authentication mode
         * @param localIdentity local identity requirement
         * @param redirects     redirect limit
         * @return secure policy
         */
        private static TransportPolicy secure(
                final Set<Protocol> schemes,
                final Set<Integer> ports,
                final Set<String> targets,
                final Set<String> peers,
                final boolean startTls,
                final TlsClientAuth clientAuth,
                final boolean localIdentity,
                final int redirects) {
            return new TransportPolicy(schemes, ports, SECURE_TLS_VERSIONS, SECURE_CIPHERS, clientAuth, localIdentity,
                    targets, peers, startTls, redirects, DEFAULT_CONNECT_TIMEOUT, DEFAULT_READ_TIMEOUT,
                    DEFAULT_RESPONSE_BYTES, true);
        }

        /**
         * Creates one fixed RADIUS policy.
         *
         * @param port    registered RADIUS port
         * @param targets target CIDRs
         * @param peers   peer CIDRs
         * @return RADIUS policy
         */
        private static TransportPolicy radius(final int port, final Set<String> targets, final Set<String> peers) {
            return new TransportPolicy(Set.of(Protocol.RADIUS), Set.of(port), List.of(), List.of(), TlsClientAuth.NONE,
                    false, targets, peers, false, Normal._0, DEFAULT_CONNECT_TIMEOUT, DEFAULT_READ_TIMEOUT, 4096,
                    false);
        }

        /**
         * Requires a preinstalled local identity for an HTTP or LDAPS client handshake.
         *
         * @return mTLS client policy
         */
        public TransportPolicy mutualTls() {
            Assert.isTrue(
                    clientAuth == TlsClientAuth.NONE && !requireLocalIdentity
                            && (allowedSchemes.equals(Set.of(Protocol.HTTPS))
                                    || allowedSchemes.equals(Set.of(Protocol.LDAPS))),
                    () -> new ValidateException("Mutual TLS is available only for HTTP or LDAPS client policies"));
            return new TransportPolicy(allowedSchemes, allowedPorts, tlsVersions, cipherSuites, clientAuth, true,
                    allowedTargetCidrs, allowedPeerCidrs, requireStartTls, redirectLimit, connectTimeout, readTimeout,
                    maxResponseBytes, verifyHostname);
        }

        /**
         * Requires client certificates on an LDAPS or LDAP StartTLS server.
         *
         * @return client-certificate-authenticated server policy
         */
        public TransportPolicy requireClientCertificates() {
            Assert.isTrue(
                    !requireLocalIdentity && clientAuth == TlsClientAuth.NONE
                            && (allowedSchemes.equals(Set.of(Protocol.LDAPS))
                                    || allowedSchemes.equals(Set.of(Protocol.LDAP))),
                    () -> new ValidateException("Client certificates are available only for LDAP server policies"));
            return new TransportPolicy(allowedSchemes, allowedPorts, tlsVersions, cipherSuites, TlsClientAuth.REQUIRE,
                    false, allowedTargetCidrs, allowedPeerCidrs, requireStartTls, redirectLimit, connectTimeout,
                    readTimeout, maxResponseBytes, verifyHostname);
        }
    }

    /**
     * Immutable protocol endpoint without credentials.
     *
     * @param protocol Bus protocol
     * @param host     non-blank host
     * @param port     explicit port, or zero for the protocol registered default
     */
    public record Endpoint(Protocol protocol, String host, int port) {

        /**
         * Validates and normalizes endpoint state.
         *
         * @param protocol endpoint protocol
         * @param host     endpoint host
         * @param port     endpoint port
         */
        public Endpoint {
            protocol = Assert.notNull(protocol, () -> new ValidateException("Endpoint protocol must not be null"));
            host = required(host, "Endpoint host");
            port = port == Normal._0 ? defaultPort(protocol) : port;
            Assert.isTrue(
                    port >= Normal._1 && port <= Normal._65535,
                    () -> new ValidateException("Endpoint port must be between 1 and 65535"));
        }
    }

    /**
     * Immutable stream read result.
     *
     * @param bytes       stream bytes
     * @param endOfStream whether EOF was reached
     */
    public record StreamRead(byte[] bytes, boolean endOfStream) {

        /**
         * Validates and snapshots stream bytes.
         *
         * @param bytes       stream bytes
         * @param endOfStream EOF flag
         */
        public StreamRead {
            bytes = AuthMetric.bytes(bytes, "Stream read bytes");
            Assert.isTrue(
                    endOfStream || bytes.length > Normal._0,
                    () -> new ValidateException("A non-EOF stream read must contain bytes"));
        }

        /**
         * Returns an independent stream byte copy.
         *
         * @return independent stream byte copy
         */
        @Override
        public byte[] bytes() {
            return bytes.clone();
        }
    }

    /**
     * Immutable complete datagram.
     *
     * @param peer  numeric peer endpoint
     * @param bytes complete datagram bytes
     */
    public record Datagram(Endpoint peer, byte[] bytes) {

        /**
         * Validates and snapshots datagram state.
         *
         * @param peer  peer endpoint
         * @param bytes datagram bytes
         */
        public Datagram {
            peer = Assert.notNull(peer, () -> new ValidateException("Datagram peer must not be null"));
            bytes = AuthMetric.bytes(bytes, "Datagram bytes");
        }

        /**
         * Returns an independent datagram byte copy.
         *
         * @return independent datagram byte copy
         */
        @Override
        public byte[] bytes() {
            return bytes.clone();
        }
    }

    /**
     * Immutable five-port Fabric transport aggregate.
     *
     * @param protocol       HTTP protocol transport
     * @param stream         stream client transport
     * @param streamServer   stream server transport
     * @param datagram       datagram client transport
     * @param datagramServer datagram server transport
     */
    public record Transports(ProtocolTransport protocol, StreamTransport stream, StreamServerTransport streamServer,
            DatagramTransport datagram, DatagramServerTransport datagramServer) {

        /**
         * Validates every required transport port.
         *
         * @param protocol       protocol transport
         * @param stream         stream client transport
         * @param streamServer   stream server transport
         * @param datagram       datagram client transport
         * @param datagramServer datagram server transport
         */
        public Transports {
            protocol = port(protocol, "Protocol transport");
            stream = port(stream, "Stream transport");
            streamServer = port(streamServer, "Stream server transport");
            datagram = port(datagram, "Datagram transport");
            datagramServer = port(datagramServer, "Datagram server transport");
        }
    }

    /**
     * Immutable product-supplied runtime aggregate.
     *
     * @param clients    client resolver
     * @param subjects   subject resolver
     * @param keys       key resolver
     * @param secrets    secret resolver
     * @param states     atomic state store
     * @param grants     grant store
     * @param consents   consent store
     * @param audit      audit sink
     * @param clock      security clock
     * @param random     secure random source
     * @param transports five transport ports
     * @param json       JSON provider
     * @param limits     input limits
     */
    public record Runtime(ClientResolver clients, SubjectResolver subjects, KeyResolver keys, SecretResolver secrets,
            StateStore states, GrantStore grants, ConsentStore consents, AuditSink audit, ClockSource clock,
            RandomSource random, Transports transports, JsonProvider json, Limits limits) {

        /**
         * Validates every required runtime port.
         *
         * @param clients    client resolver
         * @param subjects   subject resolver
         * @param keys       key resolver
         * @param secrets    secret resolver
         * @param states     state store
         * @param grants     grant store
         * @param consents   consent store
         * @param audit      audit sink
         * @param clock      clock source
         * @param random     random source
         * @param transports transport aggregate
         * @param json       JSON provider
         * @param limits     input limits
         */
        public Runtime {
            clients = port(clients, "Client resolver");
            subjects = port(subjects, "Subject resolver");
            keys = port(keys, "Key resolver");
            secrets = port(secrets, "Secret resolver");
            states = port(states, "State store");
            grants = port(grants, "Grant store");
            consents = port(consents, "Consent store");
            audit = port(audit, "Audit sink");
            clock = port(clock, "Clock source");
            random = port(random, "Random source");
            transports = port(transports, "Transports");
            json = port(json, "JSON provider");
            limits = port(limits, "Limits");
        }

        /**
         * Returns a fixed redacted representation.
         *
         * @return fixed redacted representation
         */
        @Override
        public String toString() {
            return "Runtime[REDACTED]";
        }
    }

}
