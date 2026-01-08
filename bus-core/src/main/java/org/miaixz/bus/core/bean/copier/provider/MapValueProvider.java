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
