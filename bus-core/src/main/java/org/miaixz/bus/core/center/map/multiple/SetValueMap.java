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
package org.miaixz.bus.core.center.map.multiple;

import java.io.Serial;
import java.util.*;
import java.util.function.Supplier;

/**
 * A {@link MultipleValueMap} implementation where the values associated with each key are stored in a {@link Set}.
 * Specifically, it uses a {@link LinkedHashSet} to maintain the insertion order of values within each set and ensure
 * uniqueness.
 * <p>
 * By calling {@code putValue}, multiple values can be added for the same key, and these values are represented as a
 * set.
 * 
 *
 * @param <K> The type of keys in the map.
 * @param <V> The type of values stored in the sets.
 * @author Kimi Liu
 * @since Java 17+
 */
public class SetValueMap<K, V> extends AbstractCollValueMap<K, V> {

    @Serial
    private static final long serialVersionUID = 2852277962550L;

    /**
     * Constructs a new {@code SetValueMap} using a provided factory for the underlying map. The values for each key
     * will be stored in a {@link Set} (specifically a {@link LinkedHashSet}).
     *
     * @param mapFactory A factory method that supplies a {@link Map} to store the key-set pairs.
     */
    public SetValueMap(final Supplier<Map<K, Collection<V>>> mapFactory) {
        super(mapFactory);
    }

    /**
     * Constructs a new {@code SetValueMap} with initial data from the provided map. The values for each key will be
     * stored in a {@link Set} (specifically a {@link LinkedHashSet}).
     *
     * @param map The map providing initial key-collection pairs.
     */
    public SetValueMap(final Map<K, Collection<V>> map) {
        super(map);
    }

    /**
     * Constructs an empty {@code SetValueMap} with a default {@link HashMap} as its underlying storage. The values for
     * each key will be stored in a {@link LinkedHashSet}.
     */
    public SetValueMap() {
    }

    /**
     * Creates a new {@link LinkedHashSet} instance to hold multiple values for a key. This method is called internally
     * when a new collection of values needs to be created for a key.
     *
     * @return A new, empty {@link LinkedHashSet} instance.
     */
    @Override
    protected Set<V> createCollection() {
        return new LinkedHashSet<>(DEFAULT_COLLECTION_INITIAL_CAPACITY);
    }

}
