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
package org.miaixz.bus.cache.metric;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import org.miaixz.bus.cache.CacheX;

/**
 * A no-operation (No-Op) implementation of the {@link CacheX} interface, used to effectively disable caching.
 * <p>
 * This implementation performs no actual caching. All read operations return {@code null} or empty collections, and all
 * write/delete operations do nothing. It is useful for disabling caching in certain environments or for testing
 * purposes without changing application code.
 * </p>
 *
 * @param <K> The type of keys.
 * @param <V> The type of values.
 * @author Kimi Liu
 * @since Java 21+
 */
public class NoOpCache<K, V> implements CacheX<K, V> {

    /**
     * Performs no operation and always returns {@code null}.
     *
     * @param key The cache key.
     * @return Always {@code null}.
     */
    @Override
    public V read(K key) {
        return null;
    }

    /**
     * Performs no operation.
     *
     * @param key    The cache key.
     * @param value  The cache value.
     * @param expire The expiration time in milliseconds.
     */
    @Override
    public void write(K key, V value, long expire) {
        // No-op
    }

    /**
     * Performs no operation and always returns an empty map.
     *
     * @param keys A collection of cache keys.
     * @return Always an empty, unmodifiable map.
     */
    @Override
    public Map<K, V> read(Collection<K> keys) {
        return Collections.emptyMap();
    }

    /**
     * Performs no operation.
     *
     * @param keyValueMap A map of key-value pairs.
     * @param expire      The expiration time in milliseconds.
     */
    @Override
    public void write(Map<K, V> keyValueMap, long expire) {
        // No-op
    }

    /**
     * Performs no operation.
     *
     * @param keys The keys to be removed.
     */
    @Override
    public void remove(K... keys) {
        // No-op
    }

    /**
     * Performs no operation.
     */
    @Override
    public void clear() {
        // No-op
    }

}
