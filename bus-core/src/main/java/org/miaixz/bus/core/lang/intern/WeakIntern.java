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
package org.miaixz.bus.core.lang.intern;

import org.miaixz.bus.core.center.map.reference.WeakConcurrentMap;

import java.lang.ref.WeakReference;

/**
 * 使用WeakHashMap(线程安全)存储对象的规范化对象，注意此对象需单例使用！
 *
 * @param <T> data 类型
 * @author Kimi Liu
 * @since Java 17+
 */
public class WeakIntern<T> implements Intern<T> {

    private final WeakConcurrentMap<T, WeakReference<T>> cache = new WeakConcurrentMap<>();

    @Override
    public T intern(final T sample) {
        if (null == sample) {
            return null;
        }
        T val;
        // 循环避免刚创建就被回收的情况
        do {
            val = this.cache.computeIfAbsent(sample, WeakReference::new).get();
        } while (val == null);
        return val;
    }

}
