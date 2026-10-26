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
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.miaixz.bus.core.center.function.ConsumerX;

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
 */
public class FixedLinkedHashMap<K, V> extends LinkedHashMap<K, V> {

    @Serial
    private static final long serialVersionUID = 2535836359801L;

    /**
     * The maximum capacity of the map. When the size exceeds this, the eldest entry is removed.
     */
    private int capacity;

    /**
     * An optional listener that is called when an entry is removed due to the capacity being exceeded.
     */
    private ConsumerX<Entry<K, V>> removeListener;

    /**
     * Constructs a {@code FixedLinkedHashMap} with the specified capacity.
     *
     * @param capacity The maximum number of entries the map can hold.
     * @throws IllegalArgumentException if the capacity is negative
     */
    public FixedLinkedHashMap(final int capacity) {
        super(capacity == Integer.MAX_VALUE ? capacity : capacity + 1, 1.0f, true);
        if (capacity < 0) {
            throw new IllegalArgumentException("capacity must not be negative");
        }
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
     * Sets the maximum capacity and immediately removes excess entries in least-recently-used order. The bound is
     * restored before removal listeners are notified.
     *
     * @param capacity The new capacity.
     * @throws IllegalArgumentException if capacity is negative
     * @throws RuntimeException         if a removal listener fails after the bound has been restored and remaining
     *                                  notifications have been attempted
     */
    public void setCapacity(final int capacity) {
        if (capacity < 0) {
            throw new IllegalArgumentException("capacity must not be negative");
        }
        this.capacity = capacity;
        removeExcessEntries();
    }

    /**
     * Sets the listener for capacity-driven removals. Explicit removal and clearing do not invoke this listener.
     *
     * @param removeListener the consumer receiving detached removed-entry snapshots, or {@code null} to disable it
     */
    public void setRemoveListener(final ConsumerX<Map.Entry<K, V>> removeListener) {
        this.removeListener = removeListener;
    }

    /**
     * Determines whether the eldest entry should be removed. This method is called by {@code put} and {@code putAll}
     * after inserting a new entry into the map.
     *
     * @param eldest The least recently inserted or accessed entry in the map.
     * @return {@code false}, because excess entries are removed directly before listeners are notified
     */
    @Override
    protected boolean removeEldestEntry(final Entry<K, V> eldest) {
        removeExcessEntries();
        return false;
    }

    /**
     * Restores the configured bound before invoking removal callbacks. Callback failures do not leave unremoved excess
     * entries, and later callbacks are still delivered when an earlier callback throws a runtime exception.
     *
     * @throws RuntimeException if at least one listener callback fails; later failures are suppressed on the first
     */
    private void removeExcessEntries() {
        if (size() <= capacity) {
            return;
        }
        final ConsumerX<Entry<K, V>> listener = removeListener;
        final List<Entry<K, V>> removed = listener == null ? null : new ArrayList<>();
        final Iterator<Entry<K, V>> iterator = entrySet().iterator();
        while (size() > capacity && iterator.hasNext()) {
            final Entry<K, V> entry = iterator.next();
            if (removed != null) {
                removed.add(new AbstractMap.SimpleImmutableEntry<>(entry));
            }
            iterator.remove();
        }
        RuntimeException failure = null;
        if (removed != null) {
            for (final Entry<K, V> entry : removed) {
                try {
                    listener.accept(entry);
                } catch (final RuntimeException exception) {
                    if (failure == null) {
                        failure = exception;
                    } else if (failure != exception) {
                        failure.addSuppressed(exception);
                    }
                }
            }
        }
        if (failure != null) {
            throw failure;
        }
    }

}
