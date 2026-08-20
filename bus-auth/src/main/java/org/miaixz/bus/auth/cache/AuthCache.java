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

import java.util.concurrent.CompletionStage;

import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.core.lang.Assert;

/**
 * Package-private typed view over one bus-cache backend.
 * <p>
 * This class owns no cache implementation, connection, serialization, expiration scheduler, or atomic algorithm. It
 * only isolates one authentication purpose with a fixed key namespace and delegates every operation to {@link CacheX}.
 * Purpose-specific public caches expose this view with their exact immutable value type.
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
    private final String namespace;

    /**
     * Creates one namespaced typed view over a bus-cache backend.
     *
     * @param cache     shared bus-cache backend
     * @param namespace non-empty authentication-purpose key prefix
     */
    protected AuthCache(final CacheX<String, Object> cache, final String namespace) {
        this.cache = Assert.notNull(cache, "Authentication cache must not be null");
        this.namespace = Assert.notBlank(namespace, "Authentication cache namespace must not be blank");
    }

    /**
     * Delegates atomic create-if-absent to bus-cache.
     *
     * @param key       purpose-local cache key
     * @param value     immutable authentication value
     * @param ttlMillis positive time to live in milliseconds
     * @return stage containing whether the value was created
     */
    public final CompletionStage<Boolean> create(final String key, final V value, final long ttlMillis) {
        return cache.create(key(key), Assert.notNull(value, "Authentication cache value must not be null"), ttlMillis);
    }

    /**
     * Delegates one linearizable read to bus-cache.
     *
     * @param key purpose-local cache key
     * @return stage containing the stored value or {@code null}
     */
    public final CompletionStage<V> get(final String key) {
        return cache.get(key(key)).thenApply(this::value);
    }

    /**
     * Delegates atomic read-and-remove to bus-cache.
     *
     * @param key purpose-local one-time cache key
     * @return stage containing the consumed value or {@code null}
     */
    public final CompletionStage<V> take(final String key) {
        return cache.take(key(key)).thenApply(this::value);
    }

    /**
     * Delegates atomic compare-and-replace to bus-cache.
     *
     * @param key       purpose-local cache key
     * @param expected  exact value required for replacement
     * @param update    replacement value
     * @param ttlMillis positive replacement time to live in milliseconds
     * @return stage containing whether the value was replaced
     */
    public final CompletionStage<Boolean> replace(
            final String key,
            final V expected,
            final V update,
            final long ttlMillis) {
        return cache.replace(
                key(key),
                Assert.notNull(expected, "Expected authentication cache value must not be null"),
                Assert.notNull(update, "Updated authentication cache value must not be null"),
                ttlMillis);
    }

    /**
     * Delegates atomic removal to bus-cache.
     *
     * @param key purpose-local cache key
     * @return stage containing whether a value was removed
     */
    public final CompletionStage<Boolean> delete(final String key) {
        return cache.delete(key(key));
    }

    /**
     * Adds the immutable authentication-purpose namespace to a caller key.
     *
     * @param key purpose-local key
     * @return isolated bus-cache key
     */
    private String key(final String key) {
        return namespace + Assert.notBlank(key, "Authentication cache key must not be blank");
    }

    /**
     * Restores the exact value type owned by the purpose-specific wrapper.
     *
     * @param value value returned by the shared object cache
     * @return typed value or {@code null}
     */
    private V value(final Object value) {
        return (V) value;
    }

}
