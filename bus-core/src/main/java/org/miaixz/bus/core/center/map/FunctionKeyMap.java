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
import java.util.Map;
import java.util.function.Function;

/**
 * A {@link CustomKeyMap} implementation that allows for custom key transformation using a {@link Function}. This class
 * provides a flexible way to define how keys are stored and retrieved in the map, while leaving the values
 * untransformed.
 *
 * @param <K> The type of keys in the map.
 * @param <V> The type of values in the map.
 * @author Kimi Liu
 * @since Java 17+
 */
public class FunctionKeyMap<K, V> extends CustomKeyMap<K, V> {

    @Serial
    private static final long serialVersionUID = 2852273768203L;

    /**
     * The function used to transform keys.
     */
    private final Function<Object, K> keyFunc;

    /**
     * Constructs a new {@code FunctionKeyMap} with a pre-existing empty map and a custom key transformation function.
     * It is crucial that the provided map is empty, as existing entries will not have their keys transformed, which
     * could lead to inconsistent behavior.
     *
     * @param emptyMap The empty map to be wrapped. Must be empty to ensure custom key transformations are effective.
     * @param keyFunc  The function to customize keys. Must not be {@code null}.
     */
    public FunctionKeyMap(final Map<K, V> emptyMap, final Function<Object, K> keyFunc) {
        super(emptyMap);
        this.keyFunc = keyFunc;
    }

    /**
     * Transforms the given key using the provided {@link #keyFunc}. This method is called by the parent
     * {@link CustomKeyMap} for all key-based operations.
     *
     * @param key The original key.
     * @return The transformed key.
     */
    @Override
    protected K customKey(final Object key) {
        if (null != this.keyFunc) {
            return keyFunc.apply(key);
        }
        return (K) key;
    }

}
