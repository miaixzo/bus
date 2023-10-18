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
package org.aoju.bus.proxy.factory.cglib;

import net.sf.cglib.proxy.*;
import org.aoju.bus.proxy.Interceptor;
import org.aoju.bus.proxy.Invocation;
import org.aoju.bus.proxy.Invoker;
import org.aoju.bus.proxy.Provider;
import org.aoju.bus.proxy.aspects.Aspectj;
import org.aoju.bus.proxy.factory.AbstractFactory;
import org.aoju.bus.proxy.intercept.CglibInterceptor;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class CglibFactory extends AbstractFactory {

    private static CallbackFilter callbackFilter = new PublicCallbackFilter();

    @Override
    public <T> T proxy(T target, Aspectj aspectj) {
        final Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(target.getClass());
        enhancer.setCallback(new CglibInterceptor(target, aspectj));
        return (T) enhancer.create();
    }

    public Object createDelegatorProxy(ClassLoader classLoader, Provider targetProvider,
                                       Class[] proxyClasses) {
        final Enhancer enhancer = new Enhancer();
        enhancer.setClassLoader(classLoader);
        enhancer.setInterfaces(toInterfaces(proxyClasses));
        enhancer.setSuperclass(getSuperclass(proxyClasses));
        enhancer.setCallbackFilter(callbackFilter);
        enhancer.setCallbacks(new Callback[]{new ObjectProviderDispatcher(targetProvider), NoOp.INSTANCE});
        return enhancer.create();
    }

    public Object createInterceptorProxy(ClassLoader classLoader, Object target, Interceptor interceptor,
                                         Class[] proxyClasses) {
        final Enhancer enhancer = new Enhancer();
        enhancer.setClassLoader(classLoader);
        enhancer.setInterfaces(toInterfaces(proxyClasses));
        enhancer.setSuperclass(getSuperclass(proxyClasses));
        enhancer.setCallbackFilter(callbackFilter);
        enhancer.setCallbacks(new Callback[]{new InterceptorBridge(target, interceptor), NoOp.INSTANCE});
        return enhancer.create();
    }

    public Object createInvokerProxy(ClassLoader classLoader, Invoker invoker,
                                     Class[] proxyClasses) {
        final Enhancer enhancer = new Enhancer();
        enhancer.setClassLoader(classLoader);
        enhancer.setInterfaces(toInterfaces(proxyClasses));
        enhancer.setSuperclass(getSuperclass(proxyClasses));
        enhancer.setCallbackFilter(callbackFilter);
        enhancer.setCallbacks(new Callback[]{new InvokerBridge(invoker), NoOp.INSTANCE});
        return enhancer.create();
    }

    private static class PublicCallbackFilter implements CallbackFilter {

        public int accept(Method method) {
            return Modifier.isPublic(method.getModifiers()) ? 0 : 1;
        }

    }

    private class InvokerBridge implements InvocationHandler {

        private final Invoker original;

        public InvokerBridge(Invoker original) {
            this.original = original;
        }

        public Object invoke(Object object, Method method, Object[] objects) throws Throwable {
            return original.invoke(object, method, objects);
        }

    }

    private class InterceptorBridge implements net.sf.cglib.proxy.MethodInterceptor {

        private final Interceptor inner;
        private final Object target;

        public InterceptorBridge(Object target, Interceptor inner) {
            this.inner = inner;
            this.target = target;
        }

        public Object intercept(Object object, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
            return inner.intercept(new MethodProxyInvocation(target, method, args, methodProxy));
        }

    }

    private class MethodProxyInvocation implements Invocation {

        private final MethodProxy methodProxy;
        private final Method method;
        private final Object[] args;
        private final Object target;

        public MethodProxyInvocation(Object target, Method method, Object[] args, MethodProxy methodProxy) {
            this.target = target;
            this.method = method;
            this.methodProxy = methodProxy;
            this.args = args;
        }

        public Method getMethod() {
            return method;
        }

        public Object[] getArguments() {
            return args;
        }

        public Object proceed() throws Throwable {
            return methodProxy.invoke(target, args);
        }

        public Object getProxy() {
            return target;
        }
    }

    private class ObjectProviderDispatcher implements Dispatcher {

        private final Provider delegateProvider;

        public ObjectProviderDispatcher(Provider delegateProvider) {
            this.delegateProvider = delegateProvider;
        }

        public Object loadObject() {
            return delegateProvider.getObject();
        }
    }

}

