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
package org.aoju.bus.proxy.intercept;

import org.aoju.bus.proxy.Interceptor;
import org.aoju.bus.proxy.Invocation;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;

/**
 * @author Kimi Liu
 * @version 3.5.2
 * @since JDK 1.8
 */
public class MethodInterceptorAdapter implements Interceptor {

    private final MethodInterceptor methodInterceptor;

    public MethodInterceptorAdapter(MethodInterceptor methodInterceptor) {
        this.methodInterceptor = methodInterceptor;
    }

    public Object intercept(Invocation invocation) throws Throwable {
        return methodInterceptor.invoke(new MethodInvocationAdapter(invocation));
    }

    private static class MethodInvocationAdapter implements MethodInvocation {
        private final Invocation invocation;

        public MethodInvocationAdapter(Invocation invocation) {
            this.invocation = invocation;
        }

        public Method getMethod() {
            return invocation.getMethod();
        }

        public Object[] getArguments() {
            return invocation.getArguments();
        }

        public Object proceed() throws Throwable {
            return invocation.proceed();
        }

        public Object getThis() {
            return invocation.getProxy();
        }

        public AccessibleObject getStaticPart() {
            return invocation.getMethod();
        }
    }

}
