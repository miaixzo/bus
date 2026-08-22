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
package org.miaixz.bus.auth.cache;

import java.io.Serial;
import java.io.Serializable;
import java.time.Duration;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.Builder;
import org.miaixz.bus.auth.FabricX.Clock;
import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Typed authentication-state view over one bus-cache backend.
 * <p>
 * This class owns no cache implementation, connection, serialization, expiration scheduler, or atomic algorithm. It
 * only isolates one authentication purpose with a fixed key prefix and delegates every operation to {@link CacheX}.
 * Purpose-specific public caches expose this view with their exact immutable value type. Every value is stored inside a
 * versioned {@link Envelope}; the complete envelope graph must be serializable by the configured bus-cache serializer
 * and must produce a stable representation when the same immutable value is encoded again, because distributed
 * {@link CacheX#replace(Object, Object, Object, long)} implementations compare the encoded expected value atomically. A
 * runtime-scoped view includes an irreversible Source identifier and the complete Registry snapshot revision in its
 * prefix. A successful explicit runtime reload therefore invalidates state for every Source in that runtime by
 * switching key prefixes instead of requiring backend key scans or non-atomic bulk deletion. The ordinary and scoped
 * prefixes intentionally contain no Redis hash tag: every operation is single-key atomic, so forcing a complete
 * deployment into one cluster slot would create a hotspot without providing a stronger transaction boundary.
 * </p>
 *
 * @param <V> immutable authentication value type
 * @author Kimi Liu
 */
public abstract class AuthCache<V> {

    /**
     * Shared bus-cache backend selected and maintained outside bus-auth.
     */
    private final CacheX<String, Object> cache;

    /**
     * Fixed authentication-purpose key prefix.
     */
    private final String keyPrefix;

    /**
     * Runtime value class expected after backend decoding.
     */
    private final Class<V> valueType;

    /**
     * Shared runtime clock used to convert absolute expiry instants into backend TTL values.
     */
    private final Clock clock;

    /**
     * Creates one prefix-isolated typed view over a bus-cache backend.
     *
     * @param cache      shared bus-cache backend
     * @param deployment deployment-unique cache scope
     * @param purpose    authentication-state purpose within the deployment
     * @param valueType  exact outer value class stored for this purpose
     * @param clock      shared runtime clock used to derive backend TTL
     */
    public AuthCache(final CacheX<String, Object> cache, final String deployment, final String purpose,
            final Class<V> valueType, final Clock clock) {
        this.cache = Assert.notNull(cache, "Authentication cache must not be null");
        this.keyPrefix = keyPrefix(deployment, purpose, null, 0L);
        this.valueType = Assert.notNull(valueType, "Authentication cache value type must not be null");
        this.clock = Assert.notNull(clock, "Authentication cache clock must not be null");
    }

    /**
     * Creates one Source-generation-scoped typed view over a bus-cache backend.
     * <p>
     * The Source identifier is hashed before it enters the backend key. The generation must be the revision of the
     * complete Registry snapshot from which the Source was compiled.
     * </p>
     *
     * @param cache      shared bus-cache backend
     * @param deployment deployment-unique cache scope
     * @param purpose    authentication-state purpose within the deployment
     * @param valueType  exact outer value class stored for this purpose
     * @param sourceId   exact Source registration identifier
     * @param generation non-negative Source configuration generation
     * @param clock      shared runtime clock used to derive backend TTL
     */
    protected AuthCache(final CacheX<String, Object> cache, final String deployment, final String purpose,
            final Class<V> valueType, final String sourceId, final long generation, final Clock clock) {
        this.cache = Assert.notNull(cache, "Authentication cache must not be null");
        this.keyPrefix = keyPrefix(deployment, purpose, sourceId, generation);
        this.valueType = Assert.notNull(valueType, "Authentication cache value type must not be null");
        this.clock = Assert.notNull(clock, "Authentication cache clock must not be null");
    }

    /**
     * Builds either the public purpose prefix or a Source-generation-isolated runtime prefix.
     *
     * @param deployment deployment-unique cache scope
     * @param purpose    authentication-state purpose
     * @param sourceId   optional Source identifier for a runtime-scoped view
     * @param generation Source configuration generation
     * @return validated authentication cache prefix
     */
    private static String keyPrefix(
            final String deployment,
            final String purpose,
            final String sourceId,
            final long generation) {
        final String deploymentName = Assert
                .notBlank(deployment, "Authentication cache deployment scope must not be blank");
        Assert.isFalse(
                deploymentName.indexOf(Symbol.C_BRACE_LEFT) >= 0 || deploymentName.indexOf(Symbol.C_BRACE_RIGHT) >= 0,
                "Authentication cache deployment scope must not contain Redis hash-tag braces");
        final String purposeName = Assert.notBlank(purpose, "Authentication cache purpose must not be blank");
        Assert.isFalse(
                purposeName.indexOf(Symbol.C_BRACE_LEFT) >= 0 || purposeName.indexOf(Symbol.C_BRACE_RIGHT) >= 0,
                "Authentication cache purpose must not contain Redis hash-tag braces");
        if (sourceId == null) {
            return deploymentName + ":auth:" + purposeName + ":";
        }
        final String source = Assert.notBlank(sourceId, "Authentication cache Source id must not be blank");
        Assert.isTrue(generation >= 0L, "Authentication cache Source generation must not be negative");
        final String sourceKey = Builder.sha256Hex(source);
        return deploymentName + ":auth:source:" + sourceKey + ":generation:" + generation + ":" + purposeName + ":";
    }

    /**
     * Delegates atomic create-if-absent to bus-cache.
     *
     * @param key   purpose-local cache key
     * @param value immutable authentication value
     * @return stage containing whether the value was created
     */
    protected CompletionStage<Boolean> doIssue(final String key, final ExpiringValue<V> value) {
        return cache.create(key(key), envelope(value), ttl(value));
    }

    /**
     * Delegates one linearizable read to bus-cache.
     *
     * @param key purpose-local cache key
     * @return stage containing the stored value or {@code null}
     */
    protected CompletionStage<ExpiringValue<V>> doFind(final String key) {
        return cache.get(key(key)).thenApply(this::value);
    }

    /**
     * Delegates atomic read-and-remove to bus-cache.
     *
     * @param key purpose-local one-time cache key
     * @return stage containing the consumed value or {@code null}
     */
    protected CompletionStage<ExpiringValue<V>> doConsume(final String key) {
        return cache.take(key(key)).thenApply(this::value);
    }

    /**
     * Delegates atomic compare-and-replace to bus-cache.
     *
     * @param key      purpose-local cache key
     * @param expected exact value required for replacement
     * @param update   replacement value
     * @return stage containing whether the value was replaced
     */
    protected CompletionStage<Boolean> doUpdate(
            final String key,
            final ExpiringValue<V> expected,
            final ExpiringValue<V> update) {
        return cache.replace(key(key), envelope(expected), envelope(update), ttl(update));
    }

    /**
     * Delegates atomic removal to bus-cache.
     *
     * @param key purpose-local cache key
     * @return stage containing whether a value was removed
     */
    protected CompletionStage<Boolean> doRevoke(final String key) {
        return cache.delete(key(key));
    }

    /**
     * Adds the immutable authentication-purpose prefix to a caller key.
     *
     * @param key purpose-local key
     * @return isolated bus-cache key
     */
    private String key(final String key) {
        final String localKey = Assert.notBlank(key, "Authentication cache key must not be blank");
        Assert.isFalse(
                localKey.indexOf(Symbol.C_BRACE_LEFT) >= 0 || localKey.indexOf(Symbol.C_BRACE_RIGHT) >= 0,
                "Authentication cache key must not contain Redis hash-tag braces");
        return keyPrefix + localKey;
    }

    /**
     * Restores the exact value type owned by the purpose-specific wrapper.
     *
     * @param value value returned by the shared object cache
     * @return typed value or {@code null}
     */
    private Envelope envelope(final ExpiringValue<V> value) {
        Assert.notNull(value, "Authentication cache value must not be null");
        Assert.isTrue(valueType.isInstance(value.value()), "Authentication cache entry has an incompatible type");
        return new Envelope(Normal._1, keyPrefix, valueType.getName(),
                Assert.notNull(value, "Authentication cache value must not be null"));
    }

    /**
     * Calculates the positive backend TTL for an expiring authentication value.
     *
     * @param value immutable value carrying an absolute expiry instant
     * @return remaining lifetime in milliseconds
     * @throws ValidateException if the value has already expired
     */
    private long ttl(final ExpiringValue<V> value) {
        final long millis = Duration.between(clock.now(), value.expiresAt()).toMillis();
        if (millis <= 0L) {
            throw new ValidateException("Authentication cache value is already expired");
        }
        return millis;
    }

    /**
     * Validates a backend envelope and restores its purpose-specific value.
     *
     * @param stored object returned by the shared cache backend
     * @return typed expiring value, or {@code null} when no entry exists
     * @throws ValidateException if the stored envelope has an incompatible version, purpose, or value type
     */
    private ExpiringValue<V> value(final Object stored) {
        if (stored == null) {
            return null;
        }
        if (!(stored instanceof Envelope envelope) || envelope.version() != Normal._1
                || !keyPrefix.equals(envelope.purpose()) || !valueType.getName().equals(envelope.valueType())
                || !(envelope.value() instanceof ExpiringValue<?> expiring)
                || !valueType.isInstance(expiring.value())) {
            throw new ValidateException("Authentication cache value has an incompatible type or version");
        }
        return new ExpiringValue<>(valueType.cast(expiring.value()), expiring.expiresAt());
    }

    /**
     * Stable versioned boundary stored through the bus-cache serializer.
     *
     * @param version   envelope schema version
     * @param purpose   complete purpose prefix that owns the entry
     * @param valueType fully qualified immutable value type name
     * @param value     expiring authentication value serialized by bus-cache
     */
    public record Envelope(int version, String purpose, String valueType, Object value) implements Serializable {

        @Serial
        private static final long serialVersionUID = 2898166305821L;

    }

}
