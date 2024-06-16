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
package org.miaixz.bus.core.center.map.concurrent;

import org.miaixz.bus.core.lang.Keys;
import org.miaixz.bus.core.xyz.MapKit;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * 安全的ConcurrentHashMap实现
 * 此类用于解决在JDK8中调用{@link ConcurrentHashMap#computeIfAbsent(Object, Function)}可能造成的死循环问题。
 * <p>
 * 相关bug见：@see <a href="https://bugs.openjdk.java.net/browse/JDK-8161372">https://bugs.openjdk.java.net/browse/JDK-8161372</a>
 * </p>
 *
 * @param <K> 键类型
 * @param <V> 值类型
 * @author Kimi Liu
 * @since Java 17+
 */
public class SafeConcurrentHashMap<K, V> extends ConcurrentHashMap<K, V> {

    private static final long serialVersionUID = -1L;

    /**
     * 构造，默认初始大小（16）
     */
    public SafeConcurrentHashMap() {
        super();
    }

    /**
     * 构造
     *
     * @param initialCapacity 预估初始大小
     */
    public SafeConcurrentHashMap(final int initialCapacity) {
        super(initialCapacity);
    }

    /**
     * 构造
     *
     * @param m 初始键值对
     */
    public SafeConcurrentHashMap(final Map<? extends K, ? extends V> m) {
        super(m);
    }

    /**
     * 构造
     *
     * @param initialCapacity 初始容量
     * @param loadFactor      增长系数
     */
    public SafeConcurrentHashMap(final int initialCapacity, final float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    /**
     * 构造
     *
     * @param initialCapacity  初始容量
     * @param loadFactor       增长系数
     * @param concurrencyLevel 并发级别，即Segment的个数
     */
    public SafeConcurrentHashMap(final int initialCapacity,
                                 final float loadFactor, final int concurrencyLevel) {
        super(initialCapacity, loadFactor, concurrencyLevel);
    }

    @Override
    public V computeIfAbsent(final K key, final Function<? super K, ? extends V> mappingFunction) {
        if (Keys.IS_JDK8) {
            return MapKit.computeIfAbsentForJdk8(this, key, mappingFunction);
        } else {
            return super.computeIfAbsent(key, mappingFunction);
        }
    }

}
