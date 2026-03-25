/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
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
 * @since Java 21+
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
