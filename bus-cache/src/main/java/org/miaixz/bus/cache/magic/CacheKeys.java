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
