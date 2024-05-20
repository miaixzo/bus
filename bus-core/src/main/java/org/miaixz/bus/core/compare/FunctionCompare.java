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
package org.miaixz.bus.core.compare;

import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.toolkit.CompareKit;

import java.util.function.Function;

/**
 * 指定函数排序器
 *
 * @param <T> 被比较的对象
 * @author Kimi Liu
 * @since Java 17+
 */
public class FunctionCompare<T> extends NullCompare<T> {

    private static final long serialVersionUID = -1L;

    /**
     * 构造
     *
     * @param nullGreater 是否{@code null}在后
     * @param compareSelf 在字段值相同情况下，是否比较对象本身。
     *                    如果此项为{@code false}，字段值比较后为0会导致对象被认为相同，可能导致被去重。
     * @param func        比较项获取函数
     */
    public FunctionCompare(final boolean nullGreater, final boolean compareSelf, final Function<T, Comparable<?>> func) {
        super(nullGreater, (a, b) -> {
            // 通过给定函数转换对象为指定规则的可比较对象
            final Comparable<?> v1;
            final Comparable<?> v2;
            try {
                v1 = func.apply(a);
                v2 = func.apply(b);
            } catch (final Exception e) {
                throw new InternalException(e);
            }

            // 首先比较用户自定义的转换结果，如果为0，根据compareSelf参数决定是否比较对象本身。
            // compareSelf为false时，主要用于多规则比较，比如多字段比较的情况
            int result = CompareKit.compare(v1, v2, nullGreater);
            if (compareSelf && 0 == result) {
                // 避免TreeSet / TreeMap 过滤掉排序字段相同但是对象不相同的情况
                result = CompareKit.compare(a, b, nullGreater);
            }
            return result;
        });
    }

}
