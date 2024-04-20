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
package org.miaixz.bus.proxy.intercept;

import org.miaixz.bus.proxy.Interceptor;
import org.miaixz.bus.proxy.Invocation;

/**
 * 修饰另一个MethodInterceptor，
 * 只在方法被提供的MethodFilter接受的情况下调用它
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class FilteredInterceptor implements Interceptor {

    private final Interceptor inner;
    private final MethodFilter filter;

    public FilteredInterceptor(Interceptor inner, MethodFilter filter) {
        this.inner = inner;
        this.filter = filter;
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        if (filter.accepts(invocation.getMethod())) {
            return inner.intercept(invocation);
        } else {
            return invocation.proceed();
        }
    }

}

