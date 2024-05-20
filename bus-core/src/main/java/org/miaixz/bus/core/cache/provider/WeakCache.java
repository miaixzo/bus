/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
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
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.core.cache.provider;

import org.miaixz.bus.core.cache.CacheListener;
import org.miaixz.bus.core.center.map.reference.WeakConcurrentMap;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.mutable.Mutable;
import org.miaixz.bus.core.lang.ref.Ref;

/**
 * 弱引用缓存
 * 对于一个给定的键，其映射的存在并不阻止垃圾回收器对该键的丢弃，这就使该键成为可终止的，被终止，然后被回收。
 * 丢弃某个键时，其条目从映射中有效地移除。
 *
 * @param <K> 键
 * @param <V> 值
 * @author Kimi Liu
 * @since Java 17+
 */
public class WeakCache<K, V> extends TimedCache<K, V> {

    private static final long serialVersionUID = -1L;

    /**
     * 构造
     *
     * @param timeout 超时时常，单位毫秒，-1或0表示无限制
     */
    public WeakCache(final long timeout) {
        super(timeout, new WeakConcurrentMap<>());
    }

    @Override
    public WeakCache<K, V> setListener(final CacheListener<K, V> listener) {
        super.setListener(listener);

        final WeakConcurrentMap<Mutable<K>, CacheObject<K, V>> map = (WeakConcurrentMap<Mutable<K>, CacheObject<K, V>>) this.cacheMap;
        // WeakKey回收之后，key对应的值已经是null了，因此此处的key也为null
        map.setPurgeListener((key, value) -> listener.onRemove(
                Optional.ofNullable(key).map(Ref::get).map(Mutable::get).get(),
                Optional.ofNullable(value).map(Ref::get).map(CacheObject::getValue).get()));

        return this;
    }

}
