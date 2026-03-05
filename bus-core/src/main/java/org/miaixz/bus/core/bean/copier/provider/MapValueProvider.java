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
package org.miaixz.bus.core.bean.copier.provider;

import java.lang.reflect.Type;
import java.util.Map;

import org.miaixz.bus.core.bean.copier.ValueProvider;
import org.miaixz.bus.core.convert.Convert;

/**
 * A {@link ValueProvider} implementation that retrieves values from a {@link Map}. This provider is used when the
 * source of properties for bean copying is a map.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class MapValueProvider implements ValueProvider<String> {

    /**
     * The map from which values are retrieved.
     */
    private final Map map;

    /**
     * Constructs a new {@code MapValueProvider} with the given map.
     *
     * @param map The map from which to retrieve values. Must not be {@code null}.
     */
    public MapValueProvider(final Map map) {
        this.map = map;
    }

    /**
     * Retrieves the value associated with the given key from the map. The value is converted to the specified
     * {@code valueType} if necessary.
     *
     * @param key       The key to look up in the map.
     * @param valueType The type to which the retrieved value should be converted.
     * @return The value from the map, converted to {@code valueType}, or {@code null} if the key is not found.
     */
    @Override
    public Object value(final String key, final Type valueType) {
        return Convert.convert(valueType, map.get(key));
    }

    /**
     * Checks if the map contains the specified key.
     *
     * @param key The key to check for existence in the map.
     * @return {@code true} if the map contains the key, {@code false} otherwise.
     */
    @Override
    public boolean containsKey(final String key) {
        return map.containsKey(key);
    }

}
