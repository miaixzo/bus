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
package org.miaixz.bus.core.center.map;

import java.io.Serial;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * An abstract map implementation that automatically transforms keys and values. Subclasses must implement the
 * {@link #customKey(Object)} and {@link #customValue(Object)} methods to define the transformation logic. All map
 * operations that involve keys or values will apply these transformations before interacting with the underlying map.
 *
 * @param <K> The type of keys maintained by this map.
 * @param <V> The type of mapped values.
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class TransMap<K, V> extends MapWrapper<K, V> {

    @Serial
    private static final long serialVersionUID = 2852276161213L;

    /**
     * Constructs a {@code TransMap} using a map factory to create the underlying map instance. The factory should
     * provide a new, empty map.
     *
     * @param mapFactory A {@link Supplier} that creates an empty map for internal use.
     */
    public TransMap(final Supplier<Map<K, V>> mapFactory) {
        super(mapFactory);
    }

    /**
     * Constructs a {@code TransMap} by wrapping a provided empty map instance. It is crucial that the provided map is
     * empty, as existing entries will not be transformed, which could lead to inconsistent behavior.
     *
     * @param emptyMap The map to be wrapped. Must be an empty map.
     */
    public TransMap(final Map<K, V> emptyMap) {
        super(emptyMap);
    }

    /**
     * Description inherited from parent class or interface. The key is transformed before being used for the lookup.
     */
    @Override
    public V get(final Object key) {
        return super.get(customKey(key));
    }

    /**
     * Description inherited from parent class or interface. The key and value are transformed before being put into the
     * map.
     */
    @Override
    public V put(final K key, final V value) {
        return super.put(customKey(key), customValue(value));
    }

    /**
     * Description inherited from parent class or interface. Each key and value from the source map are transformed
     * before being put into this map.
     */
    @Override
    public void putAll(final Map<? extends K, ? extends V> m) {
        m.forEach(this::put);
    }

    /**
     * Description inherited from parent class or interface. The key is transformed before the contains check is
     * performed.
     */
    @Override
    public boolean containsKey(final Object key) {
        return super.containsKey(customKey(key));
    }

    /**
     * Description inherited from parent class or interface. The key is transformed before the removal operation.
     */
    @Override
    public V remove(final Object key) {
        return super.remove(customKey(key));
    }

    /**
     * Description inherited from parent class or interface. The key and value are transformed before the removal
     * operation.
     */
    @Override
    public boolean remove(final Object key, final Object value) {
        return super.remove(customKey(key), customValue(value));
    }

    /**
     * Description inherited from parent class or interface. The key and values are transformed before the replacement
     * operation.
     */
    @Override
    public boolean replace(final K key, final V oldValue, final V newValue) {
        return super.replace(customKey(key), customValue(oldValue), customValue(newValue));
    }

    /**
     * Description inherited from parent class or interface. The key and value are transformed before the replacement
     * operation.
     */
    @Override
    public V replace(final K key, final V value) {
        return super.replace(customKey(key), customValue(value));
    }

    /**
     * Description inherited from parent class or interface. The key and default value are transformed before the
     * operation.
     */
    @Override
    public V getOrDefault(final Object key, final V defaultValue) {
        return super.getOrDefault(customKey(key), customValue(defaultValue));
    }

    /**
     * Description inherited from parent class or interface. The key is transformed, and the values passed to and
     * returned from the remapping function are also transformed.
     */
    @Override
    public V computeIfPresent(final K key, final BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return super.computeIfPresent(customKey(key), (k, v) -> remappingFunction.apply(customKey(k), customValue(v)));
    }

    /**
     * Description inherited from parent class or interface. The key is transformed, and the values passed to and
     * returned from the remapping function are also transformed.
     */
    @Override
    public V compute(final K key, final BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return super.compute(customKey(key), (k, v) -> remappingFunction.apply(customKey(k), customValue(v)));
    }

    /**
     * Description inherited from parent class or interface. The key and value are transformed, and the values passed to
     * the remapping function are also transformed.
     */
    @Override
    public V merge(final K key, final V value, final BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        return super.merge(
                customKey(key),
                customValue(value),
                (v1, v2) -> remappingFunction.apply(customValue(v1), customValue(v2)));
    }

    /**
     * Description inherited from parent class or interface. The key and value are transformed before being put into the
     * map.
     */
    @Override
    public V putIfAbsent(final K key, final V value) {
        return super.putIfAbsent(customKey(key), customValue(value));
    }

    /**
     * Description inherited from parent class or interface. The key is transformed before the operation.
     */
    @Override
    public V computeIfAbsent(final K key, final Function<? super K, ? extends V> mappingFunction) {
        return super.computeIfAbsent(customKey(key), mappingFunction);
    }

    /**
     * Transforms a key before it is used in any map operation. Subclasses must implement this method to define their
     * key conversion logic. For example, to create a case-insensitive map, this method could convert the key to
     * lowercase.
     *
     * @param key The original key.
     * @return The transformed key.
     */
    protected abstract K customKey(Object key);

    /**
     * Transforms a value before it is stored in the map. Subclasses must implement this method to define their value
     * conversion logic. For example, this method could trim whitespace from string values.
     *
     * @param value The original value.
     * @return The transformed value.
     */
    protected abstract V customValue(Object value);

}
