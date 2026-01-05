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

import java.io.*;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Wrapper;
import org.miaixz.bus.core.xyz.ObjectKit;

/**
 * A decorator for a {@link Map} that delegates all its methods to an underlying map instance. This class serves as a
 * base for creating customized map implementations by overriding specific methods, such as those for transforming keys
 * or values (e.g., {@link TransMap}).
 *
 * @param <K> The type of keys maintained by this map.
 * @param <V> The type of mapped values.
 * @author Kimi Liu
 * @since Java 17+
 */
public class MapWrapper<K, V>
        implements Map<K, V>, Iterable<Map.Entry<K, V>>, Wrapper<Map<K, V>>, Serializable, Cloneable {

    @Serial
    private static final long serialVersionUID = 2852267777760L;
    /**
     * The underlying (raw) map that all operations are delegated to.
     */
    private Map<K, V> raw;

    /**
     * Constructs a {@code MapWrapper} using a factory to create the underlying map. This is useful for subclasses that
     * need to initialize with a specific map type (e.g., {@code LinkedHashMap}).
     *
     * @param mapFactory A supplier that provides an empty {@link Map} instance.
     */
    public MapWrapper(final Supplier<Map<K, V>> mapFactory) {
        this(mapFactory.get());
    }

    /**
     * Constructs a {@code MapWrapper} that delegates to the given raw map.
     *
     * @param raw The underlying map to be wrapped. Must not be {@code null}.
     */
    public MapWrapper(final Map<K, V> raw) {
        Assert.notNull(raw, "Raw map must not be null");
        this.raw = raw;
    }

    /**
     * Retrieves the underlying raw map that this wrapper delegates to.
     *
     * @return The raw {@link Map} instance.
     */
    @Override
    public Map<K, V> getRaw() {
        return this.raw;
    }

    /**
     * Description inherited from parent class or interface.
     * <p>
     * Returns the number of key-value mappings in this map.
     *
     * @return the number of key-value mappings in this map
     */
    @Override
    public int size() {
        return raw.size();
    }

    /**
     * Description inherited from parent class or interface.
     * <p>
     * Returns {@code true} if this map contains no key-value mappings.
     *
     * @return {@code true} if this map contains no key-value mappings
     */
    @Override
    public boolean isEmpty() {
        return raw.isEmpty();
    }

    /**
     * Description inherited from parent class or interface.
     * <p>
     * Returns {@code true} if this map contains a mapping for the specified key.
     *
     * @param key the key whose presence in this map is to be tested
     * @return {@code true} if this map contains a mapping for the specified key
     */
    @Override
    public boolean containsKey(final Object key) {
        return raw.containsKey(key);
    }

    /**
     * Description inherited from parent class or interface.
     * <p>
     * Returns {@code true} if this map maps one or more keys to the specified value.
     *
     * @param value value whose presence in this map is to be tested
     * @return {@code true} if this map maps one or more keys to the specified value
     */
    @Override
    public boolean containsValue(final Object value) {
        return raw.containsValue(value);
    }

    /**
     * Description inherited from parent class or interface.
     * <p>
     * Returns the value to which the specified key is mapped, or {@code null} if this map contains no mapping for the
     * key.
     *
     * @param key the key whose associated value is to be returned
     * @return the value to which the specified key is mapped, or {@code null} if this map contains no mapping for the
     *         key
     */
    @Override
    public V get(final Object key) {
        return raw.get(key);
    }

    /**
     * Description inherited from parent class or interface.
     * <p>
     * Associates the specified value with the specified key in this map.
     *
     * @param key   key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     * @return the previous value associated with {@code key}, or {@code null} if there was no mapping for {@code key}
     */
    @Override
    public V put(final K key, final V value) {
        return raw.put(key, value);
    }

    /**
     * Description inherited from parent class or interface.
     * <p>
     * Removes the mapping for a key from this map if it is present.
     *
     * @param key key whose mapping is to be removed from the map
     * @return the previous value associated with {@code key}, or {@code null} if there was no mapping for {@code key}
     */
    @Override
    public V remove(final Object key) {
        return raw.remove(key);
    }

    /**
     * Description inherited from parent class or interface.
     * <p>
     * Copies all of the mappings from the specified map to this map.
     *
     * @param m mappings to be stored in this map
     */
    @Override
    public void putAll(final Map<? extends K, ? extends V> m) {
        raw.putAll(m);
    }

    /**
     * Description inherited from parent class or interface.
     * <p>
     * Removes all of the mappings from this map. The map will be empty after this call returns.
     */
    @Override
    public void clear() {
        raw.clear();
    }

    /**
     * Description inherited from parent class or interface.
     * <p>
     * Returns a {@link Collection} view of the values contained in this map.
     *
     * @return a collection view of the values contained in this map
     */
    @Override
    public Collection<V> values() {
        return raw.values();
    }

    /**
     * Description inherited from parent class or interface.
     * <p>
     * Returns a {@link Set} view of the keys contained in this map.
     *
     * @return a set view of the keys contained in this map
     */
    @Override
    public Set<K> keySet() {
        return raw.keySet();
    }

    /**
     * Description inherited from parent class or interface.
     * <p>
     * Returns a {@link Set} view of the mappings contained in this map.
     *
     * @return a set view of the mappings contained in this map
     */
    @Override
    public Set<Entry<K, V>> entrySet() {
        return raw.entrySet();
    }

    /**
     * Description inherited from parent class or interface.
     * <p>
     * Returns an iterator over the entries in this map.
     *
     * @return an iterator over the entries in this map
     */
    @Override
    public Iterator<Entry<K, V>> iterator() {
        return this.entrySet().iterator();
    }

    /**
     * Description inherited from parent class or interface.
     * <p>
     * Compares the specified object with this map for equality.
     *
     * @param o the object to be compared for equality with this map
     * @return {@code true} if the specified object is equal to this map
     */
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final MapWrapper<?, ?> that = (MapWrapper<?, ?>) o;
        return Objects.equals(raw, that.raw);
    }

    /**
     * Description inherited from parent class or interface.
     * <p>
     * Returns the hash code value for this map.
     *
     * @return the hash code value for this map
     */
    @Override
    public int hashCode() {
        return Objects.hash(raw);
    }

    /**
     * Description inherited from parent class or interface.
     * <p>
     * Returns the string representation of this map.
     *
     * @return the string representation of this map
     */
    @Override
    public String toString() {
        return raw.toString();
    }

    /**
     * Description inherited from parent class or interface.
     * <p>
     * Performs the given action for each entry in this map until all entries have been processed or the action throws
     * an exception.
     *
     * @param action The action to be performed for each entry
     */
    @Override
    public void forEach(final BiConsumer<? super K, ? super V> action) {
        raw.forEach(action);
    }

    /**
     * Description inherited from parent class or interface.
     * <p>
     * Replaces each entry's value with the result of invoking the given function on that entry until all entries have
     * been processed or the function throws an exception.
     *
     * @param function the function to apply to each entry
     */
    @Override
    public void replaceAll(final BiFunction<? super K, ? super V, ? extends V> function) {
        raw.replaceAll(function);
    }

    /**
     * Description inherited from parent class or interface.
     * <p>
     * If the specified key is not already associated with a value, associates it with the given value and returns
     * {@code null}, else returns the current value.
     *
     * @param key   key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     * @return the previous value associated with the specified key, or {@code null} if there was no mapping for the key
     */
    @Override
    public V putIfAbsent(final K key, final V value) {
        return raw.putIfAbsent(key, value);
    }

    /**
     * Description inherited from parent class or interface.
     * <p>
     * Removes the entry for the specified key only if it is currently mapped to the specified value.
     *
     * @param key   key with which the specified value is associated
     * @param value value expected to be associated with the specified key
     * @return {@code true} if the value was removed
     */
    @Override
    public boolean remove(final Object key, final Object value) {
        return raw.remove(key, value);
    }

    /**
     * Description inherited from parent class or interface.
     * <p>
     * Replaces the entry for the specified key only if it is currently mapped to the specified value.
     *
     * @param key      key with which the specified value is associated
     * @param oldValue value expected to be associated with the specified key
     * @param newValue value to be associated with the specified key
     * @return {@code true} if the value was replaced
     */
    @Override
    public boolean replace(final K key, final V oldValue, final V newValue) {
        return raw.replace(key, oldValue, newValue);
    }

    /**
     * Description inherited from parent class or interface.
     * <p>
     * Replaces the entry for the specified key only if it is currently mapped to some value.
     *
     * @param key   key with which the specified value is associated
     * @param value value to be associated with the specified key
     * @return the previous value associated with the specified key, or {@code null} if there was no mapping for the key
     */
    @Override
    public V replace(final K key, final V value) {
        return raw.replace(key, value);
    }

    /**
     * Description inherited from parent class or interface.
     * <p>
     * If the specified key is not already associated with a value, attempts to compute its value using the given
     * mapping function and enters it into this map unless {@code null}.
     *
     * @param key             key with which the specified value is to be associated
     * @param mappingFunction the function to compute a value
     * @return the current (existing or computed) value associated with the specified key, or {@code null} if the
     *         computed value is {@code null}
     */
    @Override
    public V computeIfAbsent(final K key, final Function<? super K, ? extends V> mappingFunction) {
        return raw.computeIfAbsent(key, mappingFunction);
    }

    /**
     * Description inherited from parent class or interface.
     * <p>
     * Returns the value to which the specified key is mapped, or {@code defaultValue} if this map contains no mapping
     * for the key.
     *
     * @param key          the key whose associated value is to be returned
     * @param defaultValue the default mapping of the key
     * @return the value to which the specified key is mapped, or {@code defaultValue} if this map contains no mapping
     *         for the key
     */
    @Override
    public V getOrDefault(final Object key, final V defaultValue) {
        return raw.getOrDefault(key, defaultValue);
    }

    /**
     * Description inherited from parent class or interface.
     * <p>
     * If the value for the specified key is present and non-null, attempts to compute a new mapping given the key and
     * its current mapped value.
     *
     * @param key               key with which the specified value is to be associated
     * @param remappingFunction the function to compute a value
     * @return the new value associated with the specified key, or {@code null} if none
     */
    @Override
    public V computeIfPresent(final K key, final BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return raw.computeIfPresent(key, remappingFunction);
    }

    /**
     * Description inherited from parent class or interface.
     * <p>
     * Attempts to compute a mapping for the specified key and its current mapped value (or {@code null} if there is no
     * current mapping).
     *
     * @param key               key with which the specified value is to be associated
     * @param remappingFunction the function to compute a value
     * @return the new value associated with the specified key, or {@code null} if none
     */
    @Override
    public V compute(final K key, final BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return raw.compute(key, remappingFunction);
    }

    /**
     * Description inherited from parent class or interface.
     * <p>
     * If the specified key is not already associated with a value or is associated with {@code null}, associates it
     * with the given non-null value.
     *
     * @param key               key with which the resulting value is to be associated
     * @param value             the non-null value to be merged with the existing value associated with the key or, if
     *                          no existing value or a null value is associated with the key, to be associated with the
     *                          key
     * @param remappingFunction the function to reapply a value to the key if it exists
     * @return the new value associated with the specified key, or {@code null} if none
     */
    @Override
    public V merge(final K key, final V value, final BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        return raw.merge(key, value, remappingFunction);
    }

    /**
     * Creates and returns a shallow copy of this {@code MapWrapper}. The underlying map is also cloned. The keys and
     * values themselves are not cloned.
     *
     * @return A shallow copy of this instance.
     * @throws CloneNotSupportedException If the underlying map is not cloneable.
     */
    @Override
    public MapWrapper<K, V> clone() throws CloneNotSupportedException {
        final MapWrapper<K, V> clone = (MapWrapper<K, V>) super.clone();
        clone.raw = ObjectKit.clone(raw);
        return clone;
    }

    /**
     * Serializes this {@code MapWrapper} instance.
     *
     * @param out The {@link ObjectOutputStream} to write to.
     * @throws IOException if an I/O error occurs.
     */
    private void writeObject(final ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        out.writeObject(this.raw);
    }

    /**
     * Deserializes this {@code MapWrapper} instance.
     *
     * @param in The {@link ObjectInputStream} to read from.
     * @throws IOException            if an I/O error occurs.
     * @throws ClassNotFoundException if the class of a serialized object could not be found.
     */
    private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        raw = (Map<K, V>) in.readObject();
    }

}
