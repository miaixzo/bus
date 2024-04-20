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

import org.miaixz.bus.core.exception.InternalException;
import org.miaixz.bus.core.toolkit.ObjectKit;

import java.util.function.Function;

/**
 * 指定函数排序器
 *
 * @param <T> 被比较的对象
 * @author Kimi Liu
 * @since Java 17+
 */
public class FuncCompare<T> extends NullCompare<T> {

    private static final long serialVersionUID = 1L;

    private final Function<T, Comparable<?>> func;

    /**
     * 构造
     *
     * @param nullGreater 是否{@code null}在后
     * @param func        比较项获取函数
     */
    public FuncCompare(boolean nullGreater, Function<T, Comparable<?>> func) {
        super(nullGreater, null);
        this.func = func;
    }

    @Override
    protected int doCompare(T a, T b) {
        Comparable<?> v1;
        Comparable<?> v2;
        try {
            v1 = func.apply(a);
            v2 = func.apply(b);
        } catch (Exception e) {
            throw new InternalException(e);
        }

        return compare(a, b, v1, v2);
    }

    /**
     * 对象及对应比较的值的综合比较
     * 考虑到如果对象对应的比较值相同，如对象的字段值相同，则返回相同结果，此时在TreeMap等容器比较去重时会去重
     * 因此需要比较下对象本身以避免去重
     *
     * @param o1 对象1
     * @param o2 对象2
     * @param v1 被比较的值1
     * @param v2 被比较的值2
     * @return 比较结果
     */
    private int compare(T o1, T o2, Comparable v1, Comparable v2) {
        int result = ObjectKit.compare(v1, v2);
        if (0 == result) {
            //避免TreeSet / TreeMap 过滤掉排序字段相同但是对象不相同的情况
            result = ObjectKit.compare(o1, o2, this.nullGreater);
        }
        return result;
    }

}
