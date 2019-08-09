/*
 * The MIT License
 *
 * Copyright (c) 2017, aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
*/
package org.aoju.bus.core.map;

import java.util.Map;

/**
 * 忽略大小写的Map<br>
 * 对KEY忽略大小写，get("Value")和get("value")获得的值相同，put进入的值也会被覆盖
 *
 * @param <K> 键类型
 * @param <V> 值类型
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class CaseInsensitiveMap<K, V> extends CustomKeyMap<K, V> {

    private static final long serialVersionUID = 4043263744224569870L;

    /**
     * 构造
     */
    public CaseInsensitiveMap() {
        super();
    }

    /**
     * 构造
     *
     * @param initialCapacity 初始大小
     * @param loadFactor      加载因子
     */
    public CaseInsensitiveMap(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    /**
     * 构造
     *
     * @param initialCapacity 初始大小
     */
    public CaseInsensitiveMap(int initialCapacity) {
        super(initialCapacity);
    }

    /**
     * 构造
     *
     * @param m Map
     */
    public CaseInsensitiveMap(Map<? extends K, ? extends V> m) {
        super(m);
    }

    /**
     * 构造
     *
     * @param loadFactor 加载因子
     * @param m          Map
     * @since 3.1.2
     */
    public CaseInsensitiveMap(float loadFactor, Map<? extends K, ? extends V> m) {
        super(loadFactor, m);
    }

    /**
     * 将Key转为小写
     *
     * @param key KEY
     * @return 小写KEY
     */
    @Override
    protected Object customKey(Object key) {
        if (null != key && key instanceof CharSequence) {
            key = key.toString().toLowerCase();
        }
        return key;
    }

}
