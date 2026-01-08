/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.core.xyz;

import org.miaixz.bus.core.cache.provider.*;

/**
 * Cache utility class.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CacheKit {

    /**
     * Constructs a new CacheKit. Utility class constructor for static access.
     */
    private CacheKit() {
    }

    /**
     * Creates a FIFO (First-In, First-Out) cache.
     *
     * @param <K>      The type of the key.
     * @param <V>      The type of the value.
     * @param capacity The cache capacity.
     * @param timeout  The timeout for each entry in milliseconds.
     * @return A new {@link FIFOCache} instance.
     */
    public static <K, V> FIFOCache<K, V> newFIFOCache(final int capacity, final long timeout) {
        return new FIFOCache<>(capacity, timeout);
    }

    /**
     * Creates a FIFO (First-In, First-Out) cache.
     *
     * @param <K>      The type of the key.
     * @param <V>      The type of the value.
     * @param capacity The cache capacity.
     * @return A new {@link FIFOCache} instance.
     */
    public static <K, V> FIFOCache<K, V> newFIFOCache(final int capacity) {
        return new FIFOCache<>(capacity);
    }

    /**
     * Creates an LFU (Least Frequently Used) cache.
     *
     * @param <K>      The type of the key.
     * @param <V>      The type of the value.
     * @param capacity The cache capacity.
     * @param timeout  The timeout for each entry in milliseconds.
     * @return A new {@link LFUCache} instance.
     */
    public static <K, V> LFUCache<K, V> newLFUCache(final int capacity, final long timeout) {
        return new LFUCache<>(capacity, timeout);
    }

    /**
     * Creates an LFU (Least Frequently Used) cache.
     *
     * @param <K>      The type of the key.
     * @param <V>      The type of the value.
     * @param capacity The cache capacity.
     * @return A new {@link LFUCache} instance.
     */
    public static <K, V> LFUCache<K, V> newLFUCache(final int capacity) {
        return new LFUCache<>(capacity);
    }

    /**
     * Creates an LRU (Least Recently Used) cache.
     *
     * @param <K>      The type of the key.
     * @param <V>      The type of the value.
     * @param capacity The cache capacity.
     * @param timeout  The timeout for each entry in milliseconds.
     * @return A new {@link LRUCache} instance.
     */
    public static <K, V> LRUCache<K, V> newLRUCache(final int capacity, final long timeout) {
        return new LRUCache<>(capacity, timeout);
    }

    /**
     * Creates an LRU (Least Recently Used) cache.
     *
     * @param <K>      The type of the key.
     * @param <V>      The type of the value.
     * @param capacity The cache capacity.
     * @return A new {@link LRUCache} instance.
     */
    public static <K, V> LRUCache<K, V> newLRUCache(final int capacity) {
        return new LRUCache<>(capacity);
    }

    /**
     * Creates a timed cache that automatically prunes expired entries via a scheduled task.
     *
     * @param <K>                The type of the key.
     * @param <V>                The type of the value.
     * @param timeout            The timeout for each entry in milliseconds.
     * @param schedulePruneDelay The interval in milliseconds to prune expired entries.
     * @return A new {@link TimedCache} instance.
     */
    public static <K, V> TimedCache<K, V> newTimedCache(final long timeout, final long schedulePruneDelay) {
        final TimedCache<K, V> cache = newTimedCache(timeout);
        return cache.schedulePrune(schedulePruneDelay);
    }

    /**
     * Creates a timed cache.
     *
     * @param <K>     The type of the key.
     * @param <V>     The type of the value.
     * @param timeout The timeout for each entry in milliseconds.
     * @return A new {@link TimedCache} instance.
     */
    public static <K, V> TimedCache<K, V> newTimedCache(final long timeout) {
        return new TimedCache<>(timeout);
    }

    /**
     * Creates a cache with weak-referenced keys.
     *
     * @param <K>     The type of the key.
     * @param <V>     The type of the value.
     * @param timeout The timeout for each entry in milliseconds.
     * @return A new {@link WeakCache} instance.
     */
    public static <K, V> WeakCache<K, V> newWeakCache(final long timeout) {
        return new WeakCache<>(timeout);
    }

    /**
     * Creates a no-op cache implementation that does not store any entries.
     *
     * @param <K> The type of the key.
     * @param <V> The type of the value.
     * @return A new {@link NoCache} instance.
     */
    public static <K, V> NoCache<K, V> newNoCache() {
        return new NoCache<>();
    }

}
