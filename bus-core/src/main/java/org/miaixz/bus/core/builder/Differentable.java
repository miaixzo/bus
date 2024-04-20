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
package org.miaixz.bus.core.builder;

/**
 * 可以将{@code Diffable}类与其他对象进行比较，以了解它们之间的差异。
 * 检索到的{@link DifferentResult}对象可以查询差异列表，也可以使用{@link DifferentResult#toString()}打印
 *
 * <p>
 * 当且仅当{@code d1.equals(d2)}表示{@code d1.diff(d2) == ""}时，差异的计算与= 一致
 * 强烈建议实现与equals一致，以避免混淆。注意，{@code null}不是任何类的实例，
 * {@code d1.diff(null)}应该抛出{@code NullPointerException}
 * </p>
 *
 * <pre>
 * Assert.assertEquals(expected.diff(result), expected, result);
 * </pre>
 *
 * @param <T> 这个对象可能被区分的对象类型
 * @author Kimi Liu
 * @since Java 17+
 */
public interface Differentable<T> {

    /**
     * 检索此对象与提供的对象之间的差异列表
     *
     * @param object diff的对象可以是{@code null}
     * @return 差异列表
     */
    DifferentResult diff(T object);

}
