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
package org.miaixz.bus.auth.protocol.oidc;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.bridge.TransportPolicy;
import org.miaixz.bus.auth.cache.StateStore;
import org.miaixz.bus.auth.codec.http.HttpValues;
import org.miaixz.bus.auth.codec.json.JsonValues;
import org.miaixz.bus.auth.codec.json.StrictJsonReader;
import org.miaixz.bus.auth.codec.state.StateJsonCodec;
import org.miaixz.bus.auth.guard.ReplayKey;
import org.miaixz.bus.auth.guard.UriValidator;
import org.miaixz.bus.auth.protocol.jwt.JWT.TrustedAlgorithm;
import org.miaixz.bus.auth.protocol.oauth2.OAuth2.ProtocolError;
import org.miaixz.bus.auth.protocol.oidc.OIDC.Jwk;
import org.miaixz.bus.auth.protocol.oidc.OIDC.JwkSet;
import org.miaixz.bus.auth.protocol.oidc.OIDC.ProviderMetadata;
import org.miaixz.bus.auth.runtime.Limits;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.extra.json.JsonProvider;
import org.miaixz.bus.fabric.protocol.http.HttpResponse;
import org.miaixz.bus.fabric.protocol.http.HttpX;

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
     * Explicit Fabric context that owns outbound HTTP resources.
     */
    private final org.miaixz.bus.fabric.Context fabric;

    /**
     * Atomic tenant-scoped cache for bounded public key sets.
     */
    private final StateStore states;

    /**
     * JSON provider used only through strict bounded codecs.
     */
    private final JsonProvider json;

    /**
     * Closed parser and allocation limits for HTTP and JSON input.
     */
    private final Limits limits;

    /**
     * Creates one key-set resolver.
     *
     * @param fabric non-null Fabric context for outbound HTTP
     * @param states non-null atomic key-set cache
     * @param json   non-null JSON provider
     * @param limits non-null parser and allocation limits
     * @throws ValidateException if any collaborator is {@code null}
     */
    public JwkSetResolver(final org.miaixz.bus.fabric.Context fabric, final StateStore states, final JsonProvider json,
            final Limits limits) {
        this.fabric = Assert.notNull(fabric, () -> new ValidateException("Fabric context must not be null"));
        this.states = Assert.notNull(states, () -> new ValidateException("State store must not be null"));
        this.json = Assert.notNull(json, () -> new ValidateException("JSON provider must not be null"));
        this.limits = Assert.notNull(limits, () -> new ValidateException("Limits must not be null"));
    }

    /**
     * Converts one decoded object to a safe public key.
     *
     * @param values decoded key members
     * @return validated public key
     * @throws ProtocolException if required metadata, public parameters, key type, use, or algorithm is invalid
     */
    private static Jwk key(final Map<?, ?> values) {
        final String keyType = JsonValues.requiredText(values, "kty", 8192, JwkSetResolver::invalidRequest);
        final String keyId = JsonValues.requiredText(values, "kid", 8192, JwkSetResolver::invalidRequest);
        final String use = JsonValues.requiredText(values, "use", 8192, JwkSetResolver::invalidRequest);
        final String algorithm = JsonValues.requiredText(values, "alg", 8192, JwkSetResolver::invalidRequest);
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
     * Creates the fixed invalid-request failure.
     *
     * @return new OAuth invalid-request protocol failure
     */
    private static RuntimeException invalidRequest() {
        return new ProtocolException(ProtocolError.INVALID_REQUEST);
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
     * @throws ProtocolException if no key matches the trusted metadata
     */
    private static JwkSet required(final JwkSet source, final String keyId, final TrustedAlgorithm algorithm) {
        final List<Jwk> matches = filter(source, keyId, algorithm);
        if (matches.isEmpty()) {
            throw new ProtocolException(ProtocolError.INVALID_REQUEST);
        }
        return new JwkSet(matches);
    }

    /**
     * Decodes one already validated key-set object.
     *
     * @param object decoded JSON object containing the {@code keys} array
     * @return immutable bounded public key set
     * @throws ProtocolException if the array, a member, or a key identifier is invalid
     */
    private static JwkSet decodeValues(final Map<?, ?> object) {
        if (!(object.get("keys") instanceof List<?> keys) || keys.isEmpty() || keys.size() > 128) {
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
     * Resolves all trusted signing keys from cache or the provider.
     *
     * @param invocation operation context
     * @param metadata   validated provider metadata
     * @param policy     strict HTTPS transport policy
     * @return stage containing bounded trusted keys
     * @throws IllegalArgumentException if a required input or state-store stage is {@code null}
     * @throws ProtocolException        if the key-set URI violates the HTTPS transport policy
     */
    public CompletionStage<JwkSet> resolve(
            final Context invocation,
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
     * @throws IllegalArgumentException if a required input or state-store stage is {@code null}
     * @throws ProtocolException        if the endpoint, payload, or requested key metadata is invalid
     */
    public CompletionStage<JwkSet> resolve(
            final Context invocation,
            final ProviderMetadata metadata,
            final TransportPolicy policy,
            final String keyId,
            final TrustedAlgorithm algorithm) {
        final Context context = Assert.notNull(invocation, "Context must be not null!");
        final ProviderMetadata provider = Assert.notNull(metadata, "Provider metadata must be not null!");
        final TransportPolicy transportPolicy = Assert.notNull(policy, "Transport policy must be not null!");
        try {
            UriValidator.https(provider.jwksUri());
            UriValidator.transport(provider.jwksUri(), transportPolicy.addressPolicy());
        } catch (final RuntimeException failure) {
            throw new ProtocolException(ProtocolError.INVALID_REQUEST.getKey(),
                    ProtocolError.INVALID_REQUEST.getValue(), failure);
        }
        final String cacheKey = ReplayKey.derive(
                context.tenantId(),
                "oidc",
                "jwks",
                provider.issuer() + "\n" + provider.jwksUri().toASCIIString());
        final CompletionStage<Optional<byte[]>> loaded = Assert
                .notNull(states.get(context, cacheKey), "State-store read stage must be not null!");
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
            final Context invocation,
            final ProviderMetadata metadata,
            final TransportPolicy policy) {
        return java.util.concurrent.CompletableFuture
                .supplyAsync(
                        () -> HttpX.builder(fabric).get(metadata.jwksUri().toASCIIString())
                                .header(Http.Header.ACCEPT, MediaType.APPLICATION_JSON)
                                .addressPolicy(policy.addressPolicy()).timeout(policy.timeout()).build().execute())
                .thenApply(this::decodeResponse);
    }

    /**
     * Validates and decodes one key-set response.
     *
     * @param response transport response
     * @return trusted public key set
     * @throws ProtocolException if status, media type, bounds, JSON, or JWK metadata is invalid
     */
    private JwkSet decodeResponse(final HttpResponse response) {
        try (HttpResponse source = Assert.notNull(response, "JSON Web Key Set response must be not null!")) {
            if (source.code() != Http.Status.OK
                    || !HttpValues.json(source.headers().asMap(), JwkSetResolver::invalidRequest)) {
                throw new ProtocolException(ProtocolError.INVALID_REQUEST);
            }
            final Object decoded = new StrictJsonReader(json, limits.maxJsonBytes(), limits.maxJsonDepth())
                    .read(source.bytes(limits.maxJsonBytes()), Map.class);
            if (!(decoded instanceof Map<?, ?> object)) {
                throw new ProtocolException(ProtocolError.INVALID_REQUEST);
            }
            return decodeValues(object);
        }
    }

    /**
     * Creates one cache entry without replacing a concurrent winner.
     *
     * @param invocation operation context
     * @param key        cache key
     * @param value      key set
     * @return cache operation stage
     */
    private CompletionStage<Boolean> create(final Context invocation, final String key, final JwkSet value) {
        return Assert.notNull(
                states.putIfAbsent(invocation, key, encode(value), CACHE_LIFETIME),
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
            final Context invocation,
            final String key,
            final byte[] expected,
            final JwkSet value) {
        return Assert.notNull(
                states.compareAndSet(invocation, key, expected, encode(value), CACHE_LIFETIME),
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
        return new StateJsonCodec(json, limits.maxJsonBytes(), limits.maxJsonDepth()).encode(Map.of("keys", keys));
    }

    /**
     * Decodes one integrity-protected cached key set.
     *
     * @param value encoded envelope
     * @return cached key set
     * @throws ProtocolException if the integrity-protected state does not contain a valid key set
     */
    private JwkSet decodeCache(final byte[] value) {
        return decodeValues(new StateJsonCodec(json, limits.maxJsonBytes(), limits.maxJsonDepth()).decode(value));
    }

}
