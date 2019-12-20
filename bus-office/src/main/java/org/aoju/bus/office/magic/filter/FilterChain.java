/*
 * The MIT License
 *
 * Copyright (c) 2017 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.office.magic.filter;

import com.sun.star.lang.XComponent;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.office.Context;

/**
 * FilterChain是负责管理过滤器调用链的对象.
 * 过滤器使用FilterChain来调用链中的下一个过滤器，
 * 或者如果调用过滤器是链中的最后一个过滤器，则结束调用链。
 *
 * @author Kimi Liu
 * @version 5.3.6
 * @since JDK 1.8+
 */
public interface FilterChain {

    /**
     * 向链中添加一个过滤器.
     *
     * @param filter 过滤器添加在链的末端.
     */
    void addFilter(Filter filter);

    /**
     * 导致调用链中的下一个过滤器，或者如果调用的过滤器是链中的最后一个过滤器，则不执行任何操作.
     *
     * @param context  用于沿链传递的OfficeContext.
     * @param document 被转换为沿链传递的XComponent.
     * @throws InstrumentException 如果处理过滤器时发生错误.
     */
    void doFilter(final Context context, final XComponent document) throws InstrumentException;

    /**
     * 创建并返回此对象的副本。"copy"的确切含义可能取决于链的类别
     *
     * @return 这个链的拷贝.
     */
    FilterChain copy();

}
