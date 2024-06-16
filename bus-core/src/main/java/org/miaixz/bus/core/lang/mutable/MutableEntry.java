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
package org.miaixz.bus.core.lang.mutable;

import org.miaixz.bus.core.center.map.AbstractEntry;

import java.io.Serializable;
import java.util.Map;

/**
 * 可变键和值的{@link Map.Entry}实现，可以修改键和值
 *
 * @param <K> 键类型
 * @param <V> 值类型
 * @author Kimi Liu
 * @since Java 17+
 */
public class MutableEntry<K, V> extends AbstractEntry<K, V> implements Mutable<Map.Entry<K, V>>, Serializable {

    private static final long serialVersionUID = -1L;

    protected K key;
    protected V value;

    /**
     * 构造
     *
     * @param key   键
     * @param value 值
     */
    public MutableEntry(final K key, final V value) {
        this.key = key;
        this.value = value;
    }

    /**
     * 创建{@code MutableEntry}
     *
     * @param key   键
     * @param value 值
     * @param <K>   键类型
     * @param <V>   值类型
     * @return {@code MutableEntry}
     */
    public static <K, V> MutableEntry<K, V> of(final K key, final V value) {
        return new MutableEntry<>(key, value);
    }

    /**
     * 获取键
     *
     * @return 键
     */
    @Override
    public K getKey() {
        return this.key;
    }

    /**
     * 获取值
     *
     * @return 值
     */
    @Override
    public V getValue() {
        return this.value;
    }

    /**
     * 设置键
     *
     * @param key 新键
     * @return old data
     */
    public K setKey(final K key) {
        final K oldKey = this.key;
        this.key = key;
        return oldKey;
    }

    /**
     * 设置值
     *
     * @param value 新值
     * @return old value
     */
    @Override
    public V setValue(final V value) {
        final V oldValue = this.value;
        this.value = value;
        return oldValue;
    }

    @Override
    public Map.Entry<K, V> get() {
        return this;
    }

    @Override
    public void set(final Map.Entry<K, V> pair) {
        this.key = pair.getKey();
        this.value = pair.getValue();
    }

}
