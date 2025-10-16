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
import java.util.LinkedHashMap;
import java.util.Map;

import org.miaixz.bus.core.lang.Normal;

/**
 * A {@link LinkedHashMap} implementation that automatically converts keys to camel case. This map preserves insertion
 * order while allowing flexible key access, where keys like "int_value" and "intValue" can refer to the same entry.
 * When a value is {@code put} into the map, its key is converted to camel case. If a camel case version of the key
 * already exists, its value will be overwritten.
 *
 * @param <K> The type of keys in the map (typically {@code String}).
 * @param <V> The type of values in the map.
 * @author Kimi Liu
 * @since Java 17+
 */
public class CamelCaseLinkedMap<K, V> extends CamelCaseMap<K, V> {

    @Serial
    private static final long serialVersionUID = 2852268579885L;

    /**
     * Constructs an empty {@code CamelCaseLinkedMap} with the default initial capacity (16).
     */
    public CamelCaseLinkedMap() {
        this(Normal._16);
    }

    /**
     * Constructs an empty {@code CamelCaseLinkedMap} with the specified initial capacity.
     *
     * @param initialCapacity The initial capacity of the map.
     */
    public CamelCaseLinkedMap(final int initialCapacity) {
        this(initialCapacity, Normal.DEFAULT_LOAD_FACTOR);
    }

    /**
     * Constructs a new {@code CamelCaseLinkedMap} with the same mappings as the specified map. Keys from the input map
     * will be converted to camel case upon insertion.
     *
     * @param m The map whose mappings are to be placed in this map.
     */
    public CamelCaseLinkedMap(final Map<? extends K, ? extends V> m) {
        this(Normal.DEFAULT_LOAD_FACTOR, m);
    }

    /**
     * Constructs a new {@code CamelCaseLinkedMap} with the specified load factor and the same mappings as the specified
     * map. Keys from the input map will be converted to camel case upon insertion.
     *
     * @param loadFactor The load factor for the map.
     * @param m          The map whose mappings are to be placed in this map. Its data will be copied into a new
     *                   {@link LinkedHashMap}.
     */
    public CamelCaseLinkedMap(final float loadFactor, final Map<? extends K, ? extends V> m) {
        this(m.size(), loadFactor);
        this.putAll(m);
    }

    /**
     * Constructs an empty {@link CamelCaseLinkedMap} with the specified initial capacity and load factor.
     *
     * @param initialCapacity The initial capacity of the map.
     * @param loadFactor      The load factor for the map.
     */
    public CamelCaseLinkedMap(final int initialCapacity, final float loadFactor) {
        super(new LinkedHashMap<>(initialCapacity, loadFactor));
    }

}
