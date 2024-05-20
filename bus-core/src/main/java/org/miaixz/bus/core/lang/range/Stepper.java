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
 * 步进接口，此接口用于实现如何对一个对象按照指定步进增加步进
 * 步进接口可以定义以下逻辑：
 *
 * <pre>
 * 1、步进规则，即对象如何做步进
 * 2、步进大小，通过实现此接口，在实现类中定义一个对象属性，可灵活定义步进大小
 * 3、限制range个数，通过实现此接口，在实现类中定义一个对象属性，可灵活定义limit，限制range个数
 * </pre>
 *
 * @param <T> 需要增加步进的对象
 * @author Kimi Liu
 * @since Java 17+
 */
@FunctionalInterface
public interface Stepper<T> {

    /**
     * 增加步进
     * 增加步进后的返回值如果为{@code null}则表示步进结束
     * 用户需根据end参数自行定义边界，当达到边界时返回null表示结束，否则Range中边界对象无效，会导致无限循环
     *
     * @param current 上一次增加步进后的基础对象
     * @param end     结束对象
     * @param index   当前索引（步进到第几个元素），从0开始计数
     * @return 增加步进后的对象
     */
    T step(T current, T end, int index);

}
