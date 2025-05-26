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
import java.util.concurrent.ConcurrentHashMap;

/**
 * 通过{@link ConcurrentHashMap}实现的线程安全HashSet
 *
 * @param <E> 元素类型
 * @author Kimi Liu
 * @since Java 17+
 */
public class ConcurrentHashSet<E> extends SetFromMap<E> {

    @Serial
    private static final long serialVersionUID = 2852237331635L;

    /**
     * 构造 触发因子为默认的0.75
     */
    public ConcurrentHashSet() {
        super(new ConcurrentHashMap<>());
    }

    /**
     * 构造 触发因子为默认的0.75
     *
     * @param initialCapacity 初始大小
     */
    public ConcurrentHashSet(final int initialCapacity) {
        super(new ConcurrentHashMap<>(initialCapacity));
    }

    /**
     * 构造
     *
     * @param initialCapacity 初始大小
     * @param loadFactor      加载因子。此参数决定数据增长时触发的百分比
     */
    public ConcurrentHashSet(final int initialCapacity, final float loadFactor) {
        super(new ConcurrentHashMap<>(initialCapacity, loadFactor));
    }

    /**
     * 构造
     *
     * @param initialCapacity  初始大小
     * @param loadFactor       触发因子。此参数决定数据增长时触发的百分比
     * @param concurrencyLevel 线程并发度
     */
    public ConcurrentHashSet(final int initialCapacity, final float loadFactor, final int concurrencyLevel) {
        super(new ConcurrentHashMap<>(initialCapacity, loadFactor, concurrencyLevel));
    }

    /**
     * 从已有集合中构造
     *
     * @param iter {@link Iterable}
     */
    public ConcurrentHashSet(final Iterable<E> iter) {
        super(iter instanceof Collection ? new ConcurrentHashMap<>(((Collection<E>) iter).size())
                : new ConcurrentHashMap<>());
        if (iter instanceof Collection) {
            this.addAll((Collection<E>) iter);
        } else {
            for (final E e : iter) {
                this.add(e);
            }
        }
    }

}
