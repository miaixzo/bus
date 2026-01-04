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
package org.miaixz.bus.setting.magic;

import java.io.Serial;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import org.miaixz.bus.core.xyz.MapKit;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * A thread-safe, group-based map implementation, extending {@link LinkedHashMap}. It organizes key-value pairs into
 * named groups, where each group is a {@code LinkedHashMap<String, String>}.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class GroupedMap extends LinkedHashMap<String, LinkedHashMap<String, String>> {

    @Serial
    private static final long serialVersionUID = 2852227322018L;

    /**
     * Lock for ensuring thread-safe access.
     */
    private final ReentrantReadWriteLock cacheLock = new ReentrantReadWriteLock();
    /**
     * Read lock.
     */
    private final ReadLock readLock = cacheLock.readLock();
    /**
     * Write lock.
     */
    private final WriteLock writeLock = cacheLock.writeLock();
    /**
     * Cached size of all key-value pairs across all groups. -1 indicates it needs recalculation.
     */
    private int size = -1;

    /**
     * Gets a value for a given key within a specific group.
     *
     * @param group The group name.
     * @param key   The key within the group.
     * @return The value, or null if the group or key does not exist.
     */
    public String get(final CharSequence group, final CharSequence key) {
        readLock.lock();
        try {
            final LinkedHashMap<String, String> map = this.get(StringKit.toStringOrEmpty(group));
            if (MapKit.isNotEmpty(map)) {
                return map.get(StringKit.toStringOrNull(key));
            }
        } finally {
            readLock.unlock();
        }
        return null;
    }

    @Override
    public LinkedHashMap<String, String> get(final Object key) {
        /**
         * Returns the value to which the specified key is mapped, or {@code null} if this map contains no mapping for
         * the key. This method delegates to the parent LinkedHashMap's get method with thread safety.
         *
         * @param key the key whose associated value is to be returned
         * @return the value to which the specified key is mapped, or {@code null} if this map contains no mapping for
         *         the key
         */
        readLock.lock();
        try {
            return super.get(key);
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Returns the total number of key-value pairs across all groups.
     *
     * @return The total size.
     */
    @Override
    public int size() {
        writeLock.lock();
        try {
            if (this.size < 0) {
                this.size = 0;
                for (final LinkedHashMap<String, String> value : this.values()) {
                    this.size += value.size();
                }
            }
        } finally {
            writeLock.unlock();
        }
        return this.size;
    }

    /**
     * Puts a key-value pair into a specific group.
     *
     * @param group The group name. If null or empty, the default group is used.
     * @param key   The key.
     * @param value The value.
     * @return The previous value associated with the key, or null if there was none.
     */
    public String put(String group, final String key, final String value) {
        group = StringKit.toStringOrEmpty(group).trim();
        writeLock.lock();
        try {
            final LinkedHashMap<String, String> valueMap = this.computeIfAbsent(group, k -> new LinkedHashMap<>());
            this.size = -1; // Invalidate cached size
            return valueMap.put(key, value);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Puts all key-value pairs from a map into a specific group.
     *
     * @param group The group name.
     * @param m     The map of key-value pairs to add.
     * @return This {@code GroupedMap} instance.
     */
    public GroupedMap putAll(final String group, final Map<? extends String, ? extends String> m) {
        for (final Entry<? extends String, ? extends String> entry : m.entrySet()) {
            this.put(group, entry.getKey(), entry.getValue());
        }
        return this;
    }

    /**
     * Removes a key-value pair from a specific group.
     *
     * @param group The group name.
     * @param key   The key to remove.
     * @return The value that was removed, or null if the key was not found.
     */
    public String remove(String group, final String key) {
        group = StringKit.toStringOrEmpty(group).trim();
        writeLock.lock();
        try {
            final LinkedHashMap<String, String> valueMap = this.get(group);
            if (MapKit.isNotEmpty(valueMap)) {
                return valueMap.remove(key);
            }
        } finally {
            writeLock.unlock();
        }
        return null;
    }

    /**
     * Checks if a specific group is empty.
     *
     * @param group The group name.
     * @return {@code true} if the group does not exist or has no entries.
     */
    public boolean isEmpty(String group) {
        group = StringKit.toStringOrEmpty(group).trim();
        readLock.lock();
        try {
            final LinkedHashMap<String, String> valueMap = this.get(group);
            return MapKit.isEmpty(valueMap);
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Checks if this grouped map contains any key-value pairs across all groups.
     *
     * @return {@code true} if this map is empty.
     */
    @Override
    public boolean isEmpty() {
        return this.size() == 0;
    }

    /**
     * Checks if a specific group contains a given key.
     *
     * @param group The group name.
     * @param key   The key to check for.
     * @return {@code true} if the key exists in the group.
     */
    public boolean containsKey(String group, final String key) {
        group = StringKit.toStringOrEmpty(group).trim();
        readLock.lock();
        try {
            final LinkedHashMap<String, String> valueMap = this.get(group);
            if (MapKit.isNotEmpty(valueMap)) {
                return valueMap.containsKey(key);
            }
        } finally {
            readLock.unlock();
        }
        return false;
    }

    /**
     * Checks if a specific group contains a given value.
     *
     * @param group The group name.
     * @param value The value to check for.
     * @return {@code true} if the value exists in the group.
     */
    public boolean containsValue(String group, final String value) {
        group = StringKit.toStringOrEmpty(group).trim();
        readLock.lock();
        try {
            final LinkedHashMap<String, String> valueMap = this.get(group);
            if (MapKit.isNotEmpty(valueMap)) {
                return valueMap.containsValue(value);
            }
        } finally {
            readLock.unlock();
        }
        return false;
    }

    /**
     * Clears all key-value pairs from a specific group.
     *
     * @param group The group name to clear.
     * @return This {@code GroupedMap} instance.
     */
    public GroupedMap clear(String group) {
        group = StringKit.toStringOrEmpty(group).trim();
        writeLock.lock();
        try {
            final LinkedHashMap<String, String> valueMap = this.get(group);
            if (MapKit.isNotEmpty(valueMap)) {
                valueMap.clear();
            }
        } finally {
            writeLock.unlock();
        }
        return this;
    }

    @Override
    public Set<String> keySet() {
        readLock.lock();
        try {
            return super.keySet();
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Returns a set of all keys within a specific group.
     *
     * @param group The group name.
     * @return The set of keys.
     */
    public Set<String> keySet(String group) {
        group = StringKit.toStringOrEmpty(group).trim();
        readLock.lock();
        try {
            final LinkedHashMap<String, String> valueMap = this.get(group);
            if (MapKit.isNotEmpty(valueMap)) {
                return valueMap.keySet();
            }
        } finally {
            readLock.unlock();
        }
        return Collections.emptySet();
    }

    /**
     * Returns a collection of all values within a specific group.
     *
     * @param group The group name.
     * @return The collection of values.
     */
    public Collection<String> values(String group) {
        group = StringKit.toStringOrEmpty(group).trim();
        readLock.lock();
        try {
            final LinkedHashMap<String, String> valueMap = this.get(group);
            if (MapKit.isNotEmpty(valueMap)) {
                return valueMap.values();
            }
        } finally {
            readLock.unlock();
        }
        return Collections.emptyList();
    }

    @Override
    public Set<java.util.Map.Entry<String, LinkedHashMap<String, String>>> entrySet() {
        readLock.lock();
        try {
            return super.entrySet();
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Returns a set of all key-value entries within a specific group.
     *
     * @param group The group name.
     * @return The set of entries.
     */
    public Set<Entry<String, String>> entrySet(String group) {
        group = StringKit.toStringOrEmpty(group).trim();
        readLock.lock();
        try {
            final LinkedHashMap<String, String> valueMap = this.get(group);
            if (MapKit.isNotEmpty(valueMap)) {
                return valueMap.entrySet();
            }
        } finally {
            readLock.unlock();
        }
        return Collections.emptySet();
    }

    @Override
    public String toString() {
        readLock.lock();
        try {
            return super.toString();
        } finally {
            readLock.unlock();
        }
    }

}
