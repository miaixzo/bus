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
package org.miaixz.bus.core.cache.provider;

import java.util.Iterator;

import org.miaixz.bus.core.center.map.FixedLinkedHashMap;
import org.miaixz.bus.core.lang.mutable.Mutable;

/**
 * LRU (least recently used)最近最久未使用缓存 根据使用时间来判定对象是否被持续缓存 当对象被访问时放入缓存，当缓存满了，最久未被使用的对象将被移除。
 * 此缓存基于LinkedHashMap，因此当被缓存的对象每被访问一次，这个对象的key就到链表头部。 这个算法简单并且非常快，他比FIFO有一个显著优势是经常使用的对象不太可能被移除缓存。 缺点是当缓存满时，不能被很快的访问。
 *
 * @param <K> 键类型
 * @param <V> 值类型
 * @author Kimi Liu
 * @since Java 17+
 */
public class LRUCache<K, V> extends ReentrantCache<K, V> {

    private static final long serialVersionUID = -1L;

    /**
     * 构造 默认无超时
     *
     * @param capacity 容量
     */
    public LRUCache(final int capacity) {
        this(capacity, 0);
    }

    /**
     * 构造
     *
     * @param capacity 容量
     * @param timeout  默认超时时间，单位：毫秒
     */
    public LRUCache(int capacity, final long timeout) {
        if (Integer.MAX_VALUE == capacity) {
            capacity -= 1;
        }

        this.capacity = capacity;
        this.timeout = timeout;

        // 链表key按照访问顺序排序，调用get方法后，会将这次访问的元素移至头部
        final FixedLinkedHashMap<Mutable<K>, CacheObject<K, V>> fixedLinkedHashMap = new FixedLinkedHashMap<>(capacity);
        fixedLinkedHashMap.setRemoveListener(entry -> {
            if (null != listener) {
                listener.onRemove(entry.getKey().get(), entry.getValue().getValue());
            }
        });
        cacheMap = fixedLinkedHashMap;
    }

    /**
     * 只清理超时对象，LRU的实现会交给{@code LinkedHashMap}
     */
    @Override
    protected int pruneCache() {
        if (isPruneExpiredActive() == false) {
            return 0;
        }
        int count = 0;
        final Iterator<CacheObject<K, V>> values = cacheObjIter();
        CacheObject<K, V> co;
        while (values.hasNext()) {
            co = values.next();
            if (co.isExpired()) {
                values.remove();
                onRemove(co.key, co.obj);
                count++;
            }
        }
        return count;
    }

}
