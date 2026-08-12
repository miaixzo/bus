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
package org.miaixz.bus.auth.metric.oidc;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.metric.AuthMetric.*;
import org.miaixz.bus.auth.metric.AuthMetric.Runtime;
import org.miaixz.bus.auth.metric.JWT.TrustedAlgorithm;
import org.miaixz.bus.auth.metric.OAuth2.ProtocolError;
import org.miaixz.bus.auth.metric.OIDC.Jwk;
import org.miaixz.bus.auth.metric.OIDC.JwkSet;
import org.miaixz.bus.auth.metric.OIDC.ProviderMetadata;
import org.miaixz.bus.auth.metric.shared.json.StrictJsonReader;
import org.miaixz.bus.auth.metric.shared.security.ReplayKey;
import org.miaixz.bus.auth.metric.shared.state.StateEnvelopeCodec;
import org.miaixz.bus.auth.metric.shared.validation.UriValidator;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.MediaType;

/**
 * Resolves bounded OpenID Provider JSON Web Key Sets through the injected transport and state store. Cached public keys
 * have a five-minute upper lifetime. A requested missing key identifier forces one remote refresh, enabling key
 * rotation without allowing token-controlled key type, use, or algorithm metadata to escape the trusted filters.
 *
 * @author Kimi Liu
 */
public final class JwkSetResolver {

    /**
     * Maximum cached key-set lifetime.
     */
    private static final Duration CACHE_LIFETIME = Duration.ofMinutes(5);

    /**
     * Allowed asymmetric public key types.
     */
    private static final Set<String> KEY_TYPES = Set.of("RSA", "EC", "OKP");

    /**
     * Private or symmetric JWK members that must never enter cached public metadata.
     */
    private static final Set<String> PRIVATE_MEMBERS = Set.of("d", "p", "q", "dp", "dq", "qi", "oth", "k");

    /**
     * Authentication runtime.
     */
    private final Runtime runtime;

    /**
     * Creates one key-set resolver.
     *
     * @param runtime authentication runtime
     */
    public JwkSetResolver(final Runtime runtime) {
        this.runtime = Assert.notNull(runtime, () -> new ValidateException("Authentication runtime must not be null"));
    }

    /**
     * Converts one decoded object to a safe public key.
     *
     * @param values decoded key members
     * @return validated public key
     */
    private static Jwk key(final Map<?, ?> values) {
        final String keyType = text(values, "kty");
        final String keyId = text(values, "kid");
        final String use = text(values, "use");
        final String algorithm = text(values, "alg");
        if (!KEY_TYPES.contains(keyType) || !"sig".equals(use)) {
            throw new ProtocolException(ProtocolError.INVALID_REQUEST);
        }
        try {
            TrustedAlgorithm.resolve(algorithm);
        } catch (final RuntimeException failure) {
            throw new ProtocolException(ProtocolError.INVALID_REQUEST.getKey(),
                    ProtocolError.INVALID_REQUEST.getValue(), failure);
        }
        final LinkedHashMap<String, String> parameters = new LinkedHashMap<>();
        values.forEach((name, value) -> {
            if (name instanceof String member && value instanceof String text
                    && !Set.of("kty", "kid", "use", "alg").contains(member)) {
                if (PRIVATE_MEMBERS.contains(member)) {
                    throw new ProtocolException(ProtocolError.INVALID_REQUEST);
                }
                parameters.put(member, text);
            }
        });
        if (parameters.isEmpty()) {
            throw new ProtocolException(ProtocolError.INVALID_REQUEST);
        }
        return new Jwk(keyType, keyId, use, algorithm, parameters);
    }

    /**
     * Reads one required string member.
     *
     * @param values decoded key
     * @param name   member name
     * @return exact string value
     */
    private static String text(final Map<?, ?> values, final String name) {
        final Object value = values.get(name);
        if (!(value instanceof String text) || text.isBlank() || text.length() > 8192) {
            throw new ProtocolException(ProtocolError.INVALID_REQUEST);
        }
        return text;
    }

    /**
     * Filters a key set by exact trusted metadata.
     *
     * @param source    source keys
     * @param keyId     optional exact key identifier
     * @param algorithm optional trusted algorithm
     * @return matching keys
     */
    private static List<Jwk> filter(final JwkSet source, final String keyId, final TrustedAlgorithm algorithm) {
        return source.keys().stream().filter(key -> "sig".equals(key.use()))
                .filter(key -> keyId == null || keyId.equals(key.keyId()))
                .filter(key -> algorithm == null || algorithm.identifier().equals(key.algorithm())).toList();
    }

    /**
     * Requires at least one exact key match.
     *
     * @param source    source keys
     * @param keyId     optional key identifier
     * @param algorithm optional algorithm
     * @return matching bounded key set
     */
    private static JwkSet required(final JwkSet source, final String keyId, final TrustedAlgorithm algorithm) {
        final List<Jwk> matches = filter(source, keyId, algorithm);
        if (matches.isEmpty()) {
            throw new ProtocolException(ProtocolError.INVALID_REQUEST);
        }
        return new JwkSet(matches);
    }

    /**
     * Resolves all trusted signing keys from cache or the provider.
     *
     * @param invocation operation context
     * @param metadata   validated provider metadata
     * @param policy     strict HTTPS transport policy
     * @return stage containing bounded trusted keys
     */
    public CompletionStage<JwkSet> resolve(
            final Invocation invocation,
            final ProviderMetadata metadata,
            final TransportPolicy policy) {
        return resolve(invocation, metadata, policy, null, null);
    }

    /**
     * Resolves keys matching an exact key identifier and trusted algorithm, refreshing once when rotation is detected.
     *
     * @param invocation operation context
     * @param metadata   validated provider metadata
     * @param policy     strict HTTPS transport policy
     * @param keyId      optional exact key identifier
     * @param algorithm  optional product-selected algorithm
     * @return stage containing matching trusted keys
     */
    public CompletionStage<JwkSet> resolve(
            final Invocation invocation,
            final ProviderMetadata metadata,
            final TransportPolicy policy,
            final String keyId,
            final TrustedAlgorithm algorithm) {
        final Invocation context = Assert.notNull(invocation, "Invocation must be not null!");
        final ProviderMetadata provider = Assert.notNull(metadata, "Provider metadata must be not null!");
        final TransportPolicy transportPolicy = Assert.notNull(policy, "Transport policy must be not null!");
        UriValidator.https(provider.jwksUri());
        UriValidator.transport(provider.jwksUri(), transportPolicy);
        final String cacheKey = ReplayKey.derive(
                context.tenantId(),
                "oidc",
                "jwks",
                provider.issuer() + "\n" + provider.jwksUri().toASCIIString());
        final CompletionStage<Optional<byte[]>> loaded = Assert
                .notNull(runtime.states().get(context, cacheKey), "State-store read stage must be not null!");
        return loaded.thenCompose(optional -> {
            if (optional != null && optional.isPresent()) {
                final JwkSet cached = decodeCache(optional.get());
                final List<Jwk> matches = filter(cached, keyId, algorithm);
                if (!matches.isEmpty() || keyId == null) {
                    return java.util.concurrent.CompletableFuture.completedFuture(new JwkSet(matches));
                }
                return fetch(context, provider, transportPolicy).thenCompose(
                        fresh -> replace(context, cacheKey, optional.get(), fresh)
                                .thenApply(ignored -> required(fresh, keyId, algorithm)));
            }
            return fetch(context, provider, transportPolicy).thenCompose(
                    fresh -> create(context, cacheKey, fresh).thenApply(ignored -> required(fresh, keyId, algorithm)));
        });
    }

    /**
     * Fetches and decodes a provider key set.
     *
     * @param invocation operation context
     * @param metadata   provider metadata
     * @param policy     transport policy
     * @return stage containing fresh keys
     */
    private CompletionStage<JwkSet> fetch(
            final Invocation invocation,
            final ProviderMetadata metadata,
            final TransportPolicy policy) {
        final Request request = new Request(Http.Method.GET, metadata.jwksUri(),
                Map.of(Http.Header.ACCEPT, List.of(MediaType.APPLICATION_JSON)), Map.of(), Normal.EMPTY, new byte[0]);
        final CompletionStage<Response> exchanged = Assert.notNull(
                runtime.transports().protocol().exchange(invocation, request, policy),
                "Protocol transport stage must be not null!");
        return exchanged.thenApply(this::decodeResponse);
    }

    /**
     * Validates and decodes one key-set response.
     *
     * @param response transport response
     * @return trusted public key set
     */
    private JwkSet decodeResponse(final Response response) {
        final Response source = Assert.notNull(response, "JSON Web Key Set response must be not null!");
        final List<String> contentTypes = source.headers().get(Http.Header.CONTENT_TYPE.toLowerCase(Locale.ROOT));
        if (source.status() != Http.Status.OK || contentTypes == null || contentTypes.size() != Normal._1
                || !contentTypes.get(Normal._0).toLowerCase(Locale.ROOT).startsWith(MediaType.APPLICATION_JSON)) {
            throw new ProtocolException(ProtocolError.INVALID_REQUEST);
        }
        final Object decoded = new StrictJsonReader(runtime.json(), runtime.limits()).read(source.body(), Map.class);
        if (!(decoded instanceof Map<?, ?> object) || !(object.get("keys") instanceof List<?> keys) || keys.isEmpty()
                || keys.size() > 128) {
            throw new ProtocolException(ProtocolError.INVALID_REQUEST);
        }
        final ArrayList<Jwk> result = new ArrayList<>(keys.size());
        final LinkedHashSet<String> identifiers = new LinkedHashSet<>();
        for (final Object value : keys) {
            if (!(value instanceof Map<?, ?> key)) {
                throw new ProtocolException(ProtocolError.INVALID_REQUEST);
            }
            final Jwk jwk = key(key);
            if (!identifiers.add(jwk.keyId())) {
                throw new ProtocolException(ProtocolError.INVALID_REQUEST);
            }
            result.add(jwk);
        }
        return new JwkSet(result);
    }

    /**
     * Creates one cache entry without replacing a concurrent winner.
     *
     * @param invocation operation context
     * @param key        cache key
     * @param value      key set
     * @return cache operation stage
     */
    private CompletionStage<Boolean> create(final Invocation invocation, final String key, final JwkSet value) {
        return Assert.notNull(
                runtime.states().putIfAbsent(invocation, key, encode(value), CACHE_LIFETIME),
                "State-store create stage must be not null!");
    }

    /**
     * Replaces one exact cached generation after key rotation.
     *
     * @param invocation operation context
     * @param key        cache key
     * @param expected   current encoded value
     * @param value      fresh key set
     * @return cache operation stage
     */
    private CompletionStage<Boolean> replace(
            final Invocation invocation,
            final String key,
            final byte[] expected,
            final JwkSet value) {
        return Assert.notNull(
                runtime.states().compareAndSet(invocation, key, expected, encode(value), CACHE_LIFETIME),
                "State-store replace stage must be not null!");
    }

    /**
     * Encodes one key set into an integrity-protected cache envelope.
     *
     * @param value key set
     * @return encoded envelope
     */
    private byte[] encode(final JwkSet value) {
        final List<Map<String, Object>> keys = value.keys().stream().map(key -> {
            final LinkedHashMap<String, Object> members = new LinkedHashMap<>(key.parameters());
            members.put("kty", key.keyType());
            members.put("kid", key.keyId());
            members.put("use", key.use());
            members.put("alg", key.algorithm());
            return Map.copyOf(members);
        }).toList();
        return StateEnvelopeCodec.encode(runtime.json().write(Map.of("keys", keys)));
    }

    /**
     * Decodes one integrity-protected cached key set.
     *
     * @param value encoded envelope
     * @return cached key set
     */
    private JwkSet decodeCache(final byte[] value) {
        final byte[] json = StateEnvelopeCodec.decode(value);
        return decodeResponse(
                new Response(Http.Status.OK, Map.of(Http.Header.CONTENT_TYPE, List.of(MediaType.APPLICATION_JSON)),
                        json));
    }

}
