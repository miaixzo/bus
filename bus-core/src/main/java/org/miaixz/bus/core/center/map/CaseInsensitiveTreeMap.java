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
package org.miaixz.bus.core.center.map;

import java.io.Serial;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

/**
 * A {@link TreeMap} implementation that treats keys as case-insensitive. All keys are internally converted to lowercase
 * strings for storage and retrieval, while maintaining the sorted order of a {@code TreeMap}. This means that
 * {@code get("Value")} and {@code get("value")} will retrieve the same entry.
 * <p>
 * When a key is {@code put} into the map, it is converted to lowercase. If a lowercase version of the key already
 * exists, its value will be overwritten. This map does not preserve the original casing of keys.
 *
 * @param <K> The type of keys in the map (typically {@code String} or a type convertible to {@code String}).
 * @param <V> The type of values in the map.
 * @author Kimi Liu
 * @since Java 17+
 */
public class CaseInsensitiveTreeMap<K, V> extends CaseInsensitiveMap<K, V> {

    @Serial
    private static final long serialVersionUID = 2852272639786L;

    /**
     * Constructs an empty {@code CaseInsensitiveTreeMap} that orders its keys according to their natural ordering,
     * treating them as case-insensitive.
     */
    public CaseInsensitiveTreeMap() {
        this((Comparator<? super K>) null);
    }

    /**
     * Constructs a new {@code CaseInsensitiveTreeMap} with the same mappings as the specified map. Keys from the input
     * map will be converted to lowercase for internal storage and ordered naturally.
     *
     * @param m The map whose mappings are to be placed in this map.
     */
    public CaseInsensitiveTreeMap(final Map<? extends K, ? extends V> m) {
        this();
        this.putAll(m);
    }

    /**
     * Constructs an empty {@code CaseInsensitiveTreeMap} with the specified comparator. The comparator will be used to
     * order the keys, after they have been converted to lowercase.
     *
     * @param comparator The comparator that will be used to order this map. If {@code null}, the natural ordering of
     *                   the keys (after lowercase conversion) will be used.
     */
    public CaseInsensitiveTreeMap(final Comparator<? super K> comparator) {
        super(MapBuilder.of(new TreeMap<>(comparator)));
    }

}
