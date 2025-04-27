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
package org.miaixz.bus.core.cache.provider;

import java.util.Iterator;

import org.miaixz.bus.core.cache.Cache;
import org.miaixz.bus.core.center.function.SupplierX;

/**
 * 无缓存实现，用于快速关闭缓存
 *
 * @param <K> 键类型
 * @param <V> 值类型
 * @author Kimi Liu
 * @since Java 17+
 */
public class NoCache<K, V> implements Cache<K, V> {

    private static final long serialVersionUID = -1L;

    @Override
    public int capacity() {
        return 0;
    }

    @Override
    public long timeout() {
        return 0;
    }

    @Override
    public void put(final K key, final V object) {
        // 跳过
    }

    @Override
    public void put(final K key, final V object, final long timeout) {
        // 跳过
    }

    @Override
    public boolean containsKey(final K key) {
        return false;
    }

    @Override
    public V get(final K key) {
        return null;
    }

    @Override
    public V get(final K key, final boolean isUpdateLastAccess) {
        return null;
    }

    @Override
    public V get(final K key, final SupplierX<V> supplier) {
        return get(key, true, supplier);
    }

    @Override
    public V get(final K key, final boolean isUpdateLastAccess, final SupplierX<V> supplier) {
        return get(key, isUpdateLastAccess, 0, supplier);
    }

    @Override
    public V get(final K key, final boolean isUpdateLastAccess, final long timeout, final SupplierX<V> supplier) {
        return (null == supplier) ? null : supplier.get();
    }

    @Override
    public Iterator<V> iterator() {
        return new Iterator<>() {
            @Override
            public boolean hasNext() {
                return false;
            }

            @Override
            public V next() {
                return null;
            }
        };
    }

    @Override
    public Iterator<CacheObject<K, V>> cacheObjIterator() {
        return null;
    }

    @Override
    public int prune() {
        return 0;
    }

    @Override
    public boolean isFull() {
        return false;
    }

    @Override
    public void remove(final K key) {
        // 跳过
    }

    @Override
    public void clear() {
        // 跳过
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

}
