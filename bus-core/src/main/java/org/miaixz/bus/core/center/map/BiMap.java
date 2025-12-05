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
package org.miaixz.bus.core.center.map;

import java.io.Serial;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.miaixz.bus.core.xyz.MapKit;

/**
 * A bidirectional map that maintains an inverse mapping, allowing efficient lookups from value to key. This
 * implementation wraps an existing {@link Map} and synchronizes a separate inverse map.
 * <p>
 * <strong>Value Uniqueness:</strong> While keys in a map are always unique, values are not necessarily. If multiple
 * keys are mapped to the same value, the inverse mapping will only retain the most recently added association. For
 * example, if {@code map.put(k1, v)} and {@code map.put(k2, v)} are called in succession, the inverse map will map
 * {@code v} to {@code k2}.
 *
 * @param <K> The type of keys maintained by this map.
 * @param <V> The type of mapped values.
 * @author Kimi Liu
 * @since Java 17+
 */
public class BiMap<K, V> extends MapWrapper<K, V> {

    /**
     * The serialization version identifier for this class.
     */
    @Serial
    private static final long serialVersionUID = 2852268325561L;

    /**
     * The inverse map, where values from the original map are keys and keys are values. This is lazily initialized when
     * {@link #getInverse()} is first called.
     */
    private Map<V, K> inverse;

    /**
     * Constructs a new {@code BiMap} that wraps the given raw map.
     *
     * @param raw The underlying {@link Map} to be wrapped. Must not be {@code null}.
     */
    public BiMap(final Map<K, V> raw) {
        super(raw);
    }

    /**
     * Associates the specified value with the specified key in this map. If the map previously contained a mapping for
     * the key, the old value is replaced. This operation also updates the inverse map to maintain bidirectional
     * consistency.
     *
     * @param key   The key with which the specified value is to be associated.
     * @param value The value to be associated with the specified key.
     * @return The previous value associated with {@code key}, or {@code null} if there was no mapping for {@code key}.
     */
    @Override
    public V put(final K key, final V value) {
        final V oldValue = super.put(key, value);
        if (null != this.inverse) {
            if (null != oldValue) {
                // If a key is re-mapped to a new value, the old value's inverse mapping must be removed.
                this.inverse.remove(oldValue);
            }
            this.inverse.put(value, key);
        }
        return oldValue;
    }

    /**
     * Copies all of the mappings from the specified map to this map. These mappings will replace any mappings that this
     * map had for any of the keys currently in the specified map. This operation also updates the inverse map
     * accordingly.
     *
     * @param m Mappings to be stored in this map.
     */
    @Override
    public void putAll(final Map<? extends K, ? extends V> m) {
        super.putAll(m);
        if (null != this.inverse) {
            m.forEach((key, value) -> this.inverse.put(value, key));
        }
    }

    /**
     * Removes the mapping for a key from this map if it is present. This operation also removes the corresponding entry
     * from the inverse map.
     *
     * @param key The key whose mapping is to be removed from the map.
     * @return The previous value associated with {@code key}, or {@code null} if there was no mapping for {@code key}.
     */
    @Override
    public V remove(final Object key) {
        final V v = super.remove(key);
        if (null != this.inverse && null != v) {
            this.inverse.remove(v);
        }
        return v;
    }

    /**
     * Removes the entry for the specified key only if it is currently mapped to the specified value. This operation
     * also removes the corresponding entry from the inverse map if successful.
     *
     * @param key   The key with which the specified value is associated.
     * @param value The value expected to be associated with the specified key.
     * @return {@code true} if the entry was removed, {@code false} otherwise.
     */
    @Override
    public boolean remove(final Object key, final Object value) {
        return super.remove(key, value) && null != this.inverse && this.inverse.remove(value, key);
    }

    /**
     * Removes all of the mappings from this map. The map will be empty after this call returns. The inverse map is also
     * cleared.
     */
    @Override
    public void clear() {
        super.clear();
        this.inverse = null;
    }

    /**
     * Retrieves the inverse view of this map, where values map to keys. The inverse map is lazily initialized upon the
     * first call to this method.
     *
     * @return A {@link Map} representing the inverse of this map.
     */
    public Map<V, K> getInverse() {
        if (null == this.inverse) {
            inverse = MapKit.inverse(getRaw());
        }
        return this.inverse;
    }

    /**
     * Retrieves the key associated with the specified value from the inverse map.
     *
     * @param value The value whose associated key is to be returned.
     * @return The key to which the specified value is mapped, or {@code null} if this map contains no mapping for the
     *         value.
     */
    public K getKey(final V value) {
        return getInverse().get(value);
    }

    /**
     * If the specified key is not already associated with a value (or is mapped to {@code null}), associates it with
     * the given value and returns {@code null}, else returns the current value. This operation also updates the inverse
     * map.
     *
     * @param key   The key with which the specified value is to be associated.
     * @param value The value to be associated with the specified key.
     * @return The previous value associated with the key, or {@code null} if there was no mapping.
     */
    @Override
    public V putIfAbsent(final K key, final V value) {
        final V oldValue = super.putIfAbsent(key, value);
        if (null == oldValue && null != this.inverse) {
            this.inverse.put(value, key);
        }
        return oldValue;
    }

    /**
     * Computes a mapping for the specified key and its current mapped value. This operation is complex and may
     * invalidate the existing inverse map, so the inverse map is reset and will be rebuilt on next access.
     *
     * @param key             The key with which the specified value is to be associated.
     * @param mappingFunction The function to compute a value.
     * @return The new value associated with the specified key, or {@code null} if none.
     */
    @Override
    public V computeIfAbsent(final K key, final Function<? super K, ? extends V> mappingFunction) {
        final V result = super.computeIfAbsent(key, mappingFunction);
        resetInverseMap();
        return result;
    }

    /**
     * Computes a new mapping for the specified key if it is present and non-null. This operation is complex and may
     * invalidate the existing inverse map, so the inverse map is reset and will be rebuilt on next access.
     *
     * @param key               The key with which the specified value is to be associated.
     * @param remappingFunction The function to compute a replacement value.
     * @return The new value associated with the specified key, or {@code null} if none.
     */
    @Override
    public V computeIfPresent(final K key, final BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        final V result = super.computeIfPresent(key, remappingFunction);
        resetInverseMap();
        return result;
    }

    /**
     * Computes a mapping for the specified key and its current value. This operation is complex and may invalidate the
     * existing inverse map, so the inverse map is reset and will be rebuilt on next access.
     *
     * @param key               The key with which the specified value is to be associated.
     * @param remappingFunction The function to compute a value.
     * @return The new value associated with the specified key, or {@code null} if none.
     */
    @Override
    public V compute(final K key, final BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        final V result = super.compute(key, remappingFunction);
        resetInverseMap();
        return result;
    }

    /**
     * Merges the specified key and value into the map. This operation is complex and may invalidate the existing
     * inverse map, so the inverse map is reset and will be rebuilt on next access.
     *
     * @param key               The key with which the specified value is to be associated.
     * @param value             The value to be merged with the existing value.
     * @param remappingFunction The function to resolve conflicts between existing and new values.
     * @return The new value associated with the key, or {@code null} if no value is associated.
     */
    @Override
    public V merge(final K key, final V value, final BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        final V result = super.merge(key, value, remappingFunction);
        resetInverseMap();
        return result;
    }

    /**
     * Resets the inverse map by setting it to {@code null}. This forces the inverse map to be rebuilt from the primary
     * map's current state upon its next access via {@link #getInverse()}. This method is called after complex
     * operations like {@code compute} or {@code merge} that make incremental updates to the inverse map difficult.
     */
    private void resetInverseMap() {
        if (null != this.inverse) {
            inverse = null;
        }
    }

}
