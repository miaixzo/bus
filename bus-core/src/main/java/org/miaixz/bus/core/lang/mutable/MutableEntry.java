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
package org.miaixz.bus.core.lang.mutable;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

import org.miaixz.bus.core.center.map.AbstractEntry;

/**
 * A mutable {@link Map.Entry} implementation that allows modification of both the key and the value.
 *
 * @param <K> The type of the key.
 * @param <V> The type of the value.
 * @author Kimi Liu
 * @since Java 17+
 */
public class MutableEntry<K, V> extends AbstractEntry<K, V> implements Mutable<Map.Entry<K, V>>, Serializable {

    @Serial
    private static final long serialVersionUID = 2852270155831L;

    /**
     * The mutable key of this entry.
     */
    protected K key;
    /**
     * The mutable value of this entry.
     */
    protected V value;

    /**
     * Constructs a new {@code MutableEntry} with the specified key and value.
     *
     * @param key   The initial key.
     * @param value The initial value.
     */
    public MutableEntry(final K key, final V value) {
        this.key = key;
        this.value = value;
    }

    /**
     * Creates a new {@code MutableEntry}.
     *
     * @param key   The initial key.
     * @param value The initial value.
     * @param <K>   The type of the key.
     * @param <V>   The type of the value.
     * @return A new {@code MutableEntry} instance.
     */
    public static <K, V> MutableEntry<K, V> of(final K key, final V value) {
        return new MutableEntry<>(key, value);
    }

    /**
     * Returns the key of this entry.
     *
     * @return The key.
     */
    @Override
    public K getKey() {
        return this.key;
    }

    /**
     * Returns the value of this entry.
     *
     * @return The value.
     */
    @Override
    public V getValue() {
        return this.value;
    }

    /**
     * Sets the key of this entry.
     *
     * @param key The new key.
     * @return The old key.
     */
    public K setKey(final K key) {
        final K oldKey = this.key;
        this.key = key;
        return oldKey;
    }

    /**
     * Sets the value of this entry.
     *
     * @param value The new value.
     * @return The old value.
     */
    @Override
    public V setValue(final V value) {
        final V oldValue = this.value;
        this.value = value;
        return oldValue;
    }

    @Override
    public Map.Entry<K, V> get() {
        return this;
    }

    /**
     * Set method.
     */
    @Override
    public void set(final Map.Entry<K, V> pair) {
        this.key = pair.getKey();
        this.value = pair.getValue();
    }

}
