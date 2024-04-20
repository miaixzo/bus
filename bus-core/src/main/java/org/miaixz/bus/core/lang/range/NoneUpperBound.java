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

/**
 * 无限大的右边界
 *
 * @param <T> 边界值类型
 * @author Kimi Liu
 * @since Java 17+
 */
class NoneUpperBound<T extends Comparable<? super T>> implements Bound<T> {

    /**
     * 无限大的右边界
     */

    static final NoneUpperBound INSTANCE = new NoneUpperBound();

    /**
     * 获取边界值
     *
     * @return 边界值
     */
    @Override
    public T getValue() {
        return null;
    }

    /**
     * 获取边界类型
     *
     * @return 边界类型
     */
    @Override
    public BoundType getType() {
        return BoundType.OPEN_UPPER_BOUND;
    }

    /**
     * 检验指定值是否在当前边界表示的范围内
     *
     * @param t 要检验的值，不允许为{@code null}
     * @return 是否
     */
    @Override
    public boolean test(final T t) {
        return true;
    }

    /**
     * <p>比较另一边界与当前边界在坐标轴上位置的先后顺序
     * 若令当前边界为<em>t1</em>，另一边界为<em>t2</em>，则有
     * <ul>
     *     <li>-1：<em>t1</em>在<em>t2</em>的左侧；</li>
     *     <li>0：<em>t1</em>与<em>t2</em>的重合；</li>
     *     <li>-1：<em>t1</em>在<em>t2</em>的右侧；</li>
     * </ul>
     *
     * @param bound 边界
     * @return 位置
     */
    @Override
    public int compareTo(final Bound<T> bound) {
        return bound instanceof NoneUpperBound ? 0 : 1;
    }

    /**
     * 获取{@code "[value"}或{@code "(value"}格式的字符串
     *
     * @return 字符串
     */
    @Override
    public String descBound() {
        return INFINITE_MAX + getType().getSymbol();
    }

    /**
     * 获得当前实例对应的{@code { x | x >= xxx}}格式的不等式字符串
     *
     * @return 字符串
     */
    @Override
    public String toString() {
        return "{x | x < +\u221e}";
    }

    /**
     * 对当前边界取反
     *
     * @return 取反后的边界
     */
    @Override
    public Bound<T> negate() {
        return this;
    }

    /**
     * 将当前实例转为一个区间
     *
     * @return 区间
     */
    @Override
    public BoundedRange<T> toRange() {
        return BoundedRange.all();
    }

}
