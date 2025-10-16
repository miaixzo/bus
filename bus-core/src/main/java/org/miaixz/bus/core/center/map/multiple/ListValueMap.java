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
package org.miaixz.bus.core.center.map.multiple;

import java.io.Serial;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * A {@code Map} implementation where each key can be associated with a {@link List} of values. This allows for storing
 * multiple values for the same key.
 *
 * @param <K> The type of the keys.
 * @param <V> The type of the values.
 * @author Kimi Liu
 * @since Java 17+
 */
public class ListValueMap<K, V> extends AbstractCollValueMap<K, V> {

    @Serial
    private static final long serialVersionUID = 2852277598332L;

    /**
     * Constructs a new {@code ListValueMap} using the specified map factory. The inner collections for values will be
     * {@link ArrayList}s.
     *
     * @param mapFactory A supplier that provides the map to be used.
     */
    public ListValueMap(final Supplier<Map<K, Collection<V>>> mapFactory) {
        super(mapFactory);
    }

    /**
     * Constructs a new {@code ListValueMap} wrapping the specified map.
     *
     * @param map The raw map to wrap.
     */
    public ListValueMap(final Map<K, Collection<V>> map) {
        super(map);
    }

    /**
     * Constructs a new {@code ListValueMap} backed by a standard {@link java.util.HashMap}.
     */
    public ListValueMap() {
    }

    /**
     * Creates a new {@link ArrayList} to store values for a key.
     *
     * @return A new, empty {@link ArrayList}.
     */
    @Override
    protected List<V> createCollection() {
        return new ArrayList<>(DEFAULT_COLLECTION_INITIAL_CAPACITY);
    }

}
