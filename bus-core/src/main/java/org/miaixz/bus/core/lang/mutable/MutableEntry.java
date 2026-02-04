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
