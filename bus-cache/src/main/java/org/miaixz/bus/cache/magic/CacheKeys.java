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
package org.miaixz.bus.cache.magic;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * A container for the results of a batch cache read operation.
 * <p>
 * This class is used to store the outcome of a multi-key lookup, separating the keys that were found in the cache
 * (hits) from those that were not (misses).
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CacheKeys {

    /**
     * A map of key-value pairs for the keys that were found in the cache.
     */
    private Map<String, Object> hitKeyMap;

    /**
     * A set of keys that were not found in the cache.
     */
    private Set<String> missKeySet;

    /**
     * Constructs a new, empty {@code CacheKeys} instance.
     * <p>
     * Both the hit map and miss set will be {@code null} initially.
     * </p>
     */
    public CacheKeys() {
    }

    /**
     * Constructs a new {@code CacheKeys} instance with the specified hit and miss collections.
     *
     * @param hitKeyMap  A map of keys and their corresponding values that were found in the cache.
     * @param missKeySet A set of keys that were not found in the cache.
     */
    public CacheKeys(Map<String, Object> hitKeyMap, Set<String> missKeySet) {
        this.hitKeyMap = hitKeyMap;
        this.missKeySet = missKeySet;
    }

    /**
     * Gets the map of key-value pairs for cache hits.
     * <p>
     * If the hit map is {@code null}, this method returns an unmodifiable empty map.
     * </p>
     *
     * @return A map of cache hits; never {@code null}.
     */
    public Map<String, Object> getHitKeyMap() {
        return null == hitKeyMap ? Collections.emptyMap() : hitKeyMap;
    }

    /**
     * Gets the set of keys that were missed in the cache lookup.
     * <p>
     * If the miss set is {@code null}, this method returns an unmodifiable empty set.
     * </p>
     *
     * @return A set of cache misses; never {@code null}.
     */
    public Set<String> getMissKeySet() {
        return null == missKeySet ? Collections.emptySet() : missKeySet;
    }

}
