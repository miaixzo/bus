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
package org.miaixz.bus.proxy;

import org.miaixz.bus.proxy.factory.cglib.CglibFactory;
import org.miaixz.bus.proxy.factory.javassist.JavassistFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * 这个类使用Java反射。对于更有效的代理， 请尝试使用其中之一
 * {@link  CglibFactory}或 {@link  JavassistFactory}代替
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Factory {

    public boolean canProxy(Class[] proxyClasses) {
        for (int i = 0; i < proxyClasses.length; i++) {
            Class proxyClass = proxyClasses[i];
            if (!proxyClass.isInterface()) {
                return false;
            }
        }
        return true;
    }

    public Object createDelegatorProxy(Provider delegateProvider, Class[] proxyClasses) {
        return createDelegatorProxy(Thread.currentThread().getContextClassLoader(), delegateProvider, proxyClasses);
    }

    public Object createDelegatorProxy(ClassLoader classLoader, Provider delegateProvider,
                                       Class[] proxyClasses) {
        return Proxy.newProxyInstance(classLoader, proxyClasses,
                new DelegatorInvocationHandler(delegateProvider));
    }

    public Object createInterceptorProxy(Object target, Interceptor interceptor,
                                         Class[] proxyClasses) {
        return createInterceptorProxy(Thread.currentThread().getContextClassLoader(), target, interceptor,
                proxyClasses);
    }

    public Object createInterceptorProxy(ClassLoader classLoader, Object target, Interceptor interceptor,
                                         Class[] proxyClasses) {
        return Proxy
                .newProxyInstance(classLoader, proxyClasses, new InterceptorInvocationHandler(target, interceptor));
    }

    public Object createInvokerProxy(Invoker invoker, Class[] proxyClasses) {
        return createInvokerProxy(Thread.currentThread().getContextClassLoader(), invoker,
                proxyClasses);
    }

    public Object createInvokerProxy(ClassLoader classLoader, Invoker invoker,
                                     Class[] proxyClasses) {
        return Proxy.newProxyInstance(classLoader, proxyClasses, new InvokerInvocationHandler(invoker));
    }

    private static class DelegatorInvocationHandler implements InvocationHandler {
        private final Provider delegateProvider;

        protected DelegatorInvocationHandler(Provider delegateProvider) {
            this.delegateProvider = delegateProvider;
        }

        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            try {
                return method.invoke(delegateProvider.getObject(), args);
            } catch (InvocationTargetException e) {
                throw e.getTargetException();
            }
        }
    }

    private static class InterceptorInvocationHandler implements InvocationHandler {
        private final Object target;
        private final Interceptor methodInterceptor;

        public InterceptorInvocationHandler(Object target, Interceptor methodInterceptor) {
            this.target = target;
            this.methodInterceptor = methodInterceptor;
        }

        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            final ReflectionInvocation invocation = new ReflectionInvocation(target, method, args);
            return methodInterceptor.intercept(invocation);
        }
    }

    private static class ReflectionInvocation implements Invocation {
        private final Method method;
        private final Object[] arguments;
        private final Object target;

        public ReflectionInvocation(Object target, Method method, Object[] arguments) {
            this.method = method;
            this.arguments = (null == arguments ? Builder.EMPTY_ARGUMENTS : arguments);
            this.target = target;
        }

        public Object[] getArguments() {
            return arguments;
        }

        public Method getMethod() {
            return method;
        }

        public Object getProxy() {
            return target;
        }

        public Object proceed() throws Throwable {
            try {
                return method.invoke(target, arguments);
            } catch (InvocationTargetException e) {
                throw e.getTargetException();
            }
        }

    }

    private static class InvokerInvocationHandler implements InvocationHandler {
        private final Invoker invoker;

        public InvokerInvocationHandler(Invoker invoker) {
            this.invoker = invoker;
        }

        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            return invoker.invoke(proxy, method, args);
        }
    }

}

