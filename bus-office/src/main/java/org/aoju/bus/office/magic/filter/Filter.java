/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
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
 ********************************************************************************/
package org.aoju.bus.office.magic.filter;

import com.sun.star.lang.XComponent;
import org.aoju.bus.office.Context;

/**
 * 表示转换文档的步骤.
 *
 * @author Kimi Liu
 * @version 6.0.0
 * @since JDK 1.8+
 */
public interface Filter {

    /**
     * 由于转换请求，每次通过链传递文档时，都会调用过滤器的doFilter方法。
     * 传入此方法的筛选器链允许筛选器将文档传递给链中的下一个实体.
     * 此方法的一个典型实现是要么使用FilterChain对象调用链中的下一个过滤器(chain. dofilter ())，
     * 或不将文档传递给过滤器链中的下一个过滤器来阻止转换处理.
     *
     * @param context  用于沿链传递的Office Context.
     * @param document 被转换为沿链传递的XComponent.
     * @param chain    链
     * @throws Exception 如果处理过滤器时发生错误.
     */
    void doFilter(final Context context, final XComponent document, final FilterChain chain)
            throws Exception;

}
