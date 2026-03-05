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
package org.miaixz.bus.core.center.map;

import java.io.Serial;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * A fixed-size {@link LinkedHashMap} that implements a Least Recently Used (LRU) cache policy. When the map's size
 * exceeds its capacity, the eldest (least recently accessed or inserted) entry is removed.
 * <p>
 * <strong>Note:</strong> This implementation is not thread-safe. Since {@link #get(Object)} operations modify the
 * internal linked list order, even read operations are not safe for concurrent use without external synchronization.
 *
 * @param <K> The type of keys maintained by this map.
 * @param <V> The type of mapped values.
 * @author Kimi Liu
 * @since Java 17+
 */
public class FixedLinkedHashMap<K, V> extends LinkedHashMap<K, V> {

    @Serial
    private static final long serialVersionUID = 2852273311867L;

    /**
     * The maximum capacity of the map. When the size exceeds this, the eldest entry is removed.
     */
    private int capacity;
    /**
     * An optional listener that is called when an entry is removed due to the capacity being exceeded.
     */
    private Consumer<java.util.Map.Entry<K, V>> removeListener;

    /**
     * Constructs a {@code FixedLinkedHashMap} with the specified capacity.
     *
     * @param capacity The maximum number of entries the map can hold.
     */
    public FixedLinkedHashMap(final int capacity) {
        super(capacity + 1, 1.0f, true);
        this.capacity = capacity;
    }

    /**
     * Returns the maximum capacity of this map.
     *
     * @return The capacity.
     */
    public int getCapacity() {
        return this.capacity;
    }

    /**
     * Sets the maximum capacity of this map.
     *
     * @param capacity The new capacity.
     */
    public void setCapacity(final int capacity) {
        this.capacity = capacity;
    }

    /**
     * Sets a custom listener to be notified when an entry is removed.
     *
     * @param removeListener The consumer to be called with the removed entry.
     */
    public void setRemoveListener(final Consumer<Map.Entry<K, V>> removeListener) {
        this.removeListener = removeListener;
    }

    /**
     * Determines whether the eldest entry should be removed. This method is called by {@code put} and {@code putAll}
     * after inserting a new entry into the map.
     *
     * @param eldest The least recently inserted or accessed entry in the map.
     * @return {@code true} if the map's size is greater than its capacity, indicating the eldest entry should be
     *         removed.
     */
    @Override
    protected boolean removeEldestEntry(final java.util.Map.Entry<K, V> eldest) {
        if (size() > this.capacity) {
            if (null != removeListener) {
                removeListener.accept(eldest);
            }
            return true;
        }
        return false;
    }

}
