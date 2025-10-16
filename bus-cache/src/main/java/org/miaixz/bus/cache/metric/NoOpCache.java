/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
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
 * @since Java 17+
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
