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
package org.miaixz.bus.core.center.set;

import java.io.Serial;
import java.util.Collection;

import org.miaixz.bus.core.center.map.concurrent.ConcurrentLinkedHashMap;
import org.miaixz.bus.core.lang.Normal;

/**
 * 通过{@link ConcurrentLinkedHashMap}实现的线程安全HashSet
 *
 * @param <E> 元素类型
 * @author Kimi Liu
 * @since Java 17+
 */
public class ConcurrentLinkedHashSet<E> extends SetFromMap<E> {

    @Serial
    private static final long serialVersionUID = 2852237523190L;

    /**
     * 构造
     */
    public ConcurrentLinkedHashSet() {
        super(new ConcurrentLinkedHashMap.Builder<E, Boolean>().maximumWeightedCapacity(Normal._64).build());
    }

    /**
     * 构造 触发因子为默认的0.75
     *
     * @param initialCapacity 初始大小
     */
    public ConcurrentLinkedHashSet(final int initialCapacity) {
        super(new ConcurrentLinkedHashMap.Builder<E, Boolean>().initialCapacity(initialCapacity)
                .maximumWeightedCapacity(initialCapacity).build());
    }

    /**
     * 构造
     *
     * @param initialCapacity  初始大小
     * @param concurrencyLevel 线程并发度
     */
    public ConcurrentLinkedHashSet(final int initialCapacity, final int concurrencyLevel) {
        super(new ConcurrentLinkedHashMap.Builder<E, Boolean>().initialCapacity(initialCapacity)
                .maximumWeightedCapacity(initialCapacity).concurrencyLevel(concurrencyLevel).build());
    }

    /**
     * 从已有集合中构造
     *
     * @param iter {@link Iterable}
     */
    public ConcurrentLinkedHashSet(final Iterable<E> iter) {
        super(iter instanceof Collection
                ? new ConcurrentLinkedHashMap.Builder<E, Boolean>().initialCapacity(((Collection<E>) iter).size())
                        .build()
                : new ConcurrentLinkedHashMap.Builder<E, Boolean>().build());
        if (iter instanceof Collection) {
            this.addAll((Collection<E>) iter);
        } else {
            for (final E e : iter) {
                this.add(e);
            }
        }
    }

}
