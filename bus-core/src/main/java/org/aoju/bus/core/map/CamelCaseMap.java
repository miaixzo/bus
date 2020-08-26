/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 ********************************************************************************/
package org.aoju.bus.core.map;

import org.aoju.bus.core.toolkit.StringKit;

import java.util.HashMap;
import java.util.Map;

/**
 * 驼峰Key风格的Map
 * 对KEY转换为驼峰,get("int_value")和get("intValue")获得的值相同,put进入的值也会被覆盖
 *
 * @param <K> 键类型
 * @param <V> 值类型
 * @author Kimi Liu
 * @version 6.0.8
 * @since JDK 1.8+
 */
public class CamelCaseMap<K, V> extends CustomKeyMap<K, V> {

    /**
     * 构造
     */
    public CamelCaseMap() {
        this(DEFAULT_INITIAL_CAPACITY);
    }

    /**
     * 构造
     *
     * @param initialCapacity 初始大小
     */
    public CamelCaseMap(int initialCapacity) {
        this(initialCapacity, DEFAULT_LOAD_FACTOR);
    }

    /**
     * 构造
     *
     * @param map Map
     */
    public CamelCaseMap(Map<? extends K, ? extends V> map) {
        this(DEFAULT_LOAD_FACTOR, map);
    }

    /**
     * 构造
     *
     * @param loadFactor 加载因子
     * @param map        Map
     */
    public CamelCaseMap(float loadFactor, Map<? extends K, ? extends V> map) {
        this(map.size(), loadFactor);
        this.putAll(map);
    }

    /**
     * 构造
     *
     * @param initialCapacity 初始大小
     * @param loadFactor      加载因子
     */
    public CamelCaseMap(int initialCapacity, float loadFactor) {
        super(new HashMap<>(initialCapacity, loadFactor));
    }

    /**
     * 将Key转为驼峰风格,如果key为字符串的话
     *
     * @param key KEY
     * @return 驼峰Key
     */
    @Override
    protected Object customKey(Object key) {
        if (null != key && key instanceof CharSequence) {
            key = StringKit.toCamelCase(key.toString());
        }
        return key;
    }

}
