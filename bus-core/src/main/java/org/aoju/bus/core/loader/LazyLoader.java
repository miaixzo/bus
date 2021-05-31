/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2021 aoju.org and other contributors.                      *
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
package org.aoju.bus.core.loader;

import java.io.Serializable;
import java.util.function.Supplier;

/**
 * 持有一个计算延迟的值
 *
 * @author Kimi Liu
 * @version 6.2.3
 * @since JDK 1.8+
 */
public class LazyLoader<T> implements Supplier<T>, Serializable {

    private transient volatile Supplier<? extends T> supplier;

    private T value;

    private LazyLoader(final Supplier<T> supplier) {
        this.supplier = supplier;
    }

    /**
     * 创建惰性的新实例
     *
     * @param supplier 供应者
     * @param <T>      泛型标记
     * @return Lazy
     */
    public static <T> LazyLoader<T> of(final Supplier<T> supplier) {
        return new LazyLoader<>(supplier);
    }

    /**
     * 返回值。值将在第一次调用时计算
     *
     * @return 懒惰的值
     */
    @Override
    public T get() {
        return null == supplier ? value : computeValue();
    }

    private synchronized T computeValue() {
        final Supplier<? extends T> s = supplier;
        if (null != s) {
            value = s.get();
            supplier = null;
        }
        return value;
    }

}