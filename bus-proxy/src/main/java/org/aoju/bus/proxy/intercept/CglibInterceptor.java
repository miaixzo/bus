/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2023 aoju.org and other contributors.                      *
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
package org.aoju.bus.proxy.intercept;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.aoju.bus.proxy.aspects.Aspectj;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Cglib实现的动态代理切面
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CglibInterceptor implements MethodInterceptor {

    private Object target;
    private Aspectj aspectj;

    /**
     * 构造
     *
     * @param target  被代理对象
     * @param aspectj 切面实现
     */
    public CglibInterceptor(Object target, Aspectj aspectj) {
        this.target = target;
        this.aspectj = aspectj;
    }

    public Object getTarget() {
        return this.target;
    }

    @Override
    public Object intercept(Object object, Method method, Object[] args, MethodProxy proxy) throws Throwable {
        Object result = null;

        // 开始前回调
        if (aspectj.before(target, method, args)) {
            try {
                result = proxy.invokeSuper(object, args);
            } catch (InvocationTargetException e) {
                // 异常回调(只捕获业务代码导致的异常,而非反射导致的异常)
                if (aspectj.afterException(target, method, args, e.getTargetException())) {
                    throw e;
                }
            }
        }

        // 结束执行回调
        if (aspectj.after(target, method, args, result)) {
            return result;
        }
        return null;
    }

}
