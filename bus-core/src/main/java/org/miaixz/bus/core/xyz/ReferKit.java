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
package org.miaixz.bus.core.xyz;

import org.miaixz.bus.core.lang.ref.Ref;
import org.miaixz.bus.core.lang.ref.ReferenceType;

import java.lang.ref.*;

/**
 * 引用工具类，主要针对{@link Reference} 工具化封装 主要封装包括：
 * 
 * <pre>
 * 1. {@link SoftReference} 软引用，在GC报告内存不足时会被GC回收
 * 2. {@link WeakReference} 弱引用，在GC时发现弱引用会回收其对象
 * 3. {@link PhantomReference} 虚引用，在GC时发现虚引用对象，会将{@link PhantomReference}插入{@link ReferenceQueue}。 此时对象未被真正回收，要等到{@link ReferenceQueue}被真正处理后才会被回收。
 * </pre>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ReferKit {

    /**
     * 获得引用
     *
     * @param <T>      被引用对象类型
     * @param type     引用类型枚举
     * @param referent 被引用对象
     * @return {@link Reference}
     */
    public static <T> Reference<T> of(final ReferenceType type, final T referent) {
        return of(type, referent, null);
    }

    /**
     * 获得引用
     *
     * @param <T>      被引用对象类型
     * @param type     引用类型枚举
     * @param referent 被引用对象
     * @param queue    引用队列
     * @return {@link Reference}
     */
    public static <T> Reference<T> of(final ReferenceType type, final T referent, final ReferenceQueue<T> queue) {
        switch (type) {
        case SOFT:
            return new SoftReference<>(referent, queue);
        case WEAK:
            return new WeakReference<>(referent, queue);
        case PHANTOM:
            return new PhantomReference<>(referent, queue);
        default:
            return null;
        }
    }

    /**
     * {@code null}全的解包获取原始对象
     *
     * @param <T> 对象类型
     * @param obj Reference对象
     * @return 原始对象 or {@code null}
     */
    public static <T> T get(final Reference<T> obj) {
        return ObjectKit.apply(obj, Reference::get);
    }

    /**
     * {@code null}全的解包获取原始对象
     *
     * @param <T> 对象类型
     * @param obj Ref对象
     * @return 原始对象 or {@code null}
     */
    public static <T> T get(final Ref<T> obj) {
        return ObjectKit.apply(obj, Ref::get);
    }

}
