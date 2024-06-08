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
 ********************************************************************************//*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2023 miaixz.org and other contributors.                    *
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
package org.miaixz.bus.proxy.invoker;


import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.MethodSignature;
import org.miaixz.bus.core.xyz.ObjectKit;

import java.lang.reflect.Method;

/**
 * 切面实现类
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class InvocationInvoker implements ProxyChain {

    /**
     * 切点
     */
    private final ProceedingJoinPoint joinPoint;
    /**
     * 方法
     */
    private Method method;

    public InvocationInvoker(ProceedingJoinPoint joinPoint) {
        this.joinPoint = joinPoint;
    }


    /**
     * 获取拦截的方法的参数列表
     *
     * @return 返回参数列表
     */
    @Override
    public Object[] getNames() {
        Signature signature = joinPoint.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        return methodSignature.getParameterNames();
    }

    /**
     * 获取拦截的方法的参数列表
     *
     * @return 返回参数列表
     */
    @Override
    public Object[] getArguments() {
        return joinPoint.getArgs();
    }

    /**
     * 获取要拦截的目标实例
     *
     * @return 返回目标实例
     */
    @Override
    public Object getProxy() {
        return joinPoint.getTarget();
    }

    /**
     * 获取拦截的方法
     *
     * @return 获取拦截的方法
     */
    @Override
    public Method getMethod() {
        if (ObjectKit.isEmpty(method)) {
            Signature signature = joinPoint.getSignature();
            MethodSignature methodSignature = (MethodSignature) signature;
            this.method = methodSignature.getMethod();
        }
        return method;
    }

    /**
     * 执行方法
     *
     * @return 返回对象
     * @throws Throwable 抛出异常
     */
    @Override
    public Object proceed() throws Throwable {
        return joinPoint.proceed();
    }

    /**
     * 执行方法
     *
     * @param arguments 参数
     * @return 返回对象
     * @throws Throwable 抛出异常
     */
    @Override
    public Object proceed(Object[] arguments) throws Throwable {
        return joinPoint.proceed(arguments);
    }

}
