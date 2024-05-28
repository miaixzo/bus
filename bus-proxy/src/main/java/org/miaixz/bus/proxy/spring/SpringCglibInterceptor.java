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
package org.miaixz.bus.proxy.spring;

import org.miaixz.bus.proxy.Aspect;
import org.miaixz.bus.proxy.invoker.Interceptor;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Spring-cglib实现的动态代理切面
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SpringCglibInterceptor extends Interceptor implements MethodInterceptor {

    private static final long serialVersionUID = -1L;

    /**
     * 构造
     *
     * @param target 被代理对象
     * @param aspect 切面实现
     */
    public SpringCglibInterceptor(final Object target, final Aspect aspect) {
        super(target, aspect);
    }

    @Override
    public Object intercept(final Object obj, final Method method, final Object[] args, final MethodProxy proxy) throws Throwable {
        final Object target = this.target;
        Object result = null;
        // 开始前回调
        if (aspect.before(target, method, args)) {
            try {
                result = proxy.invoke(target, args);
            } catch (final Throwable e) {
                Throwable throwable = e;
                if (throwable instanceof InvocationTargetException) {
                    throwable = ((InvocationTargetException) throwable).getTargetException();
                }

                // 异常回调（只捕获业务代码导致的异常，而非反射导致的异常）
                if (aspect.afterException(target, method, args, throwable)) {
                    throw throwable;
                }
            }
        }

        // 结束执行回调
        if (aspect.after(target, method, args, result)) {
            return result;
        }
        return null;
    }

}
