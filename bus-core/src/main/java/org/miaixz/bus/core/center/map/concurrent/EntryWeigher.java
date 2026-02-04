/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.core.center.map.concurrent;

/**
 * A strategy interface that determines the "weight" or cost of a map entry (key-value pair). This is typically used in
 * size-bounded caches to manage eviction policies based on the combined weight of stored entries, rather than just the
 * count of entries.
 *
 * @param <K> The type of the key in the map entry.
 * @param <V> The type of the value in the map entry.
 * @author Kimi Liu
 * @see <a href="http://code.google.com/p/concurrentlinkedhashmap/">ConcurrentLinkedHashMap Project</a>
 * @since Java 17+
 */
public interface EntryWeigher<K, V> {

    /**
     * Measures the weight of a given map entry (key-value pair). The returned weight must be non-negative. A weight of
     * zero means the entry consumes no capacity. An entry must consume a minimum of one unit of capacity if it is to be
     * stored in the map.
     *
     * @param key   The key of the entry to weigh.
     * @param value The value of the entry to weigh.
     * @return The non-negative weight of the entry.
     */
    int weightOf(K key, V value);

}
