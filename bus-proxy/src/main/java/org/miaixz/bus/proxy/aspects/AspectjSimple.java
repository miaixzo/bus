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
package org.miaixz.bus.proxy.aspects;

import java.io.Serializable;
import java.lang.reflect.Method;

/**
 * 简单切面类,不做任何操作
 * 可以继承此类实现自己需要的方法即可
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class AspectjSimple implements Aspectj, Serializable {

    private static final long serialVersionUID = 1L;

    @Override
    public boolean before(Object target, Method method, Object[] args) {
        return true;
    }

    /**
     * 目标方法执行后的操作
     * 如果 target.method 抛出异常且
     *
     * @param target 目标对象
     * @param method 目标方法
     * @param args   参数
     * @return 是否允许返回值(接下来的操作)
     * @see Aspectj#afterException 返回true,则不会执行此操作
     * 如果
     * @see Aspectj#afterException 返回false,则无论target.method是否抛出异常,均会执行此操作
     */
    public boolean after(Object target, Method method, Object[] args) {
        return after(target, method, args, null);
    }

    @Override
    public boolean after(Object target, Method method, Object[] args, Object returnVal) {
        return true;
    }

    @Override
    public boolean afterException(Object target, Method method, Object[] args, Throwable e) {
        return true;
    }

}
