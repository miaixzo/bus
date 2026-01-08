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
package org.miaixz.bus.core.center.map.multiple;

import java.io.Serial;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.miaixz.bus.core.center.function.SupplierX;

/**
 * A generic implementation of {@link MultipleValueMap} where the values associated with each key are stored in a
 * {@link Collection}. This class allows specifying custom factories for both the underlying map and the type of
 * collection used to hold multiple values.
 * <p>
 * When methods like {@code putValue} or {@code putAllValues} are called, a value collection will be created for the key
 * (if one doesn't exist), and new values will be appended to this collection.
 * 
 *
 * @param <K> The type of keys in the map.
 * @param <V> The type of values stored in the collections.
 * @author Kimi Liu
 * @since Java 17+
 */
public class CollectionValueMap<K, V> extends AbstractCollValueMap<K, V> {

    @Serial
    private static final long serialVersionUID = 2852277301182L;

    /**
     * The supplier for creating new {@link Collection} instances to hold values for a key.
     */
    private final SupplierX<Collection<V>> collFactory;

    /**
     * Constructs a new {@code CollectionValueMap} using provided factories for the underlying map and value
     * collections.
     *
     * @param mapFactory  A factory method that supplies a {@link Map} to store the key-collection pairs.
     * @param collFactory A factory method that supplies a {@link Collection} to store the values for each key.
     */
    public CollectionValueMap(final Supplier<Map<K, Collection<V>>> mapFactory,
            final SupplierX<Collection<V>> collFactory) {
        super(mapFactory);
        this.collFactory = collFactory;
    }

    /**
     * Constructs a new {@code CollectionValueMap} with a default {@link HashMap} as the underlying map and a custom
     * factory for value collections.
     *
     * @param collFactory A factory method that supplies a {@link Collection} to store the values for each key.
     */
    public CollectionValueMap(final SupplierX<Collection<V>> collFactory) {
        this.collFactory = collFactory;
    }

    /**
     * Constructs a new {@code CollectionValueMap} with a default {@link HashMap} as the underlying map and an
     * {@link ArrayList} as the default value collection type.
     */
    public CollectionValueMap() {
        this.collFactory = ArrayList::new;
    }

    /**
     * Constructs a new {@code CollectionValueMap} with initial data from the provided map. A new {@link HashMap} is
     * created to store the data, and {@link ArrayList} is used for value collections.
     *
     * @param map The map providing initial key-collection pairs.
     */
    public CollectionValueMap(final Map<K, Collection<V>> map) {
        super(map);
        this.collFactory = ArrayList::new;
    }

    /**
     * Creates a new {@link Collection} instance using the configured {@link #collFactory}. This method is called
     * internally when a new collection of values needs to be created for a key.
     *
     * @return A new {@link Collection} instance.
     */
    @Override
    protected Collection<V> createCollection() {
        return collFactory.get();
    }

}
