/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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

import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 自定义键值函数风格的Map
 *
 * @param <K> 键类型
 * @param <V> 值类型
 * @author Kimi Liu
 * @since Java 17+
 */
public class FunctionMap<K, V> extends TransMap<K, V> {

    private static final long serialVersionUID = -1L;

    private final Function<Object, K> keyFunc;
    private final Function<Object, V> valueFunc;

    /**
     * 构造
     * 注意提供的Map中不能有键值对，否则可能导致自定义key失效
     *
     * @param mapFactory Map，提供的空map
     * @param keyFunc    自定义KEY的函数
     * @param valueFunc  自定义value函数
     */
    public FunctionMap(final Supplier<Map<K, V>> mapFactory, final Function<Object, K> keyFunc, final Function<Object, V> valueFunc) {
        this(mapFactory.get(), keyFunc, valueFunc);
    }

    /**
     * 构造
     * 注意提供的Map中不能有键值对，否则可能导致自定义key失效
     *
     * @param emptyMap  Map，提供的空map
     * @param keyFunc   自定义KEY的函数
     * @param valueFunc 自定义value函数
     */
    public FunctionMap(final Map<K, V> emptyMap, final Function<Object, K> keyFunc, final Function<Object, V> valueFunc) {
        super(emptyMap);
        this.keyFunc = keyFunc;
        this.valueFunc = valueFunc;
    }

    /**
     * 根据函数自定义键
     *
     * @param key KEY
     * @return 驼峰Key
     */
    @Override
    protected K customKey(final Object key) {
        if (null != this.keyFunc) {
            return keyFunc.apply(key);
        }
        return (K) key;
    }


    @Override
    protected V customValue(final Object value) {
        if (null != this.valueFunc) {
            return valueFunc.apply(value);
        }
        return (V) value;
    }

}
