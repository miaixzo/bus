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
package org.miaixz.bus.core.lang.range;

import org.miaixz.bus.core.convert.Convert;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.toolkit.MathKit;

import java.lang.reflect.Type;

/**
 * 片段表示，用于表示文本、集合等数据结构的一个区间。
 *
 * @param <T> 数字类型，用于表示位置index
 * @author Kimi Liu
 * @since Java 17+
 */
public interface Segment<T extends Number> {

    /**
     * 获取起始位置
     *
     * @return 起始位置
     */
    T getBeginIndex();

    /**
     * 获取结束位置
     *
     * @return 结束位置
     */
    T getEndIndex();

    /**
     * 片段长度，默认计算方法为abs({@link #getEndIndex()} - {@link #getEndIndex()})
     *
     * @return 片段长度
     */
    default T length() {
        final T start = Assert.notNull(getBeginIndex(), "Start index must be not null!");
        final T end = Assert.notNull(getEndIndex(), "End index must be not null!");
        return Convert.convert((Type) start.getClass(), MathKit.sub(end, start).abs());
    }

}
