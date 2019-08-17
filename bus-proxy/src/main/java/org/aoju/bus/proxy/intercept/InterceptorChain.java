/*
 * The MIT License
 *
 * Copyright (c) 2017, aoju.org All rights reserved.
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

import org.aoju.bus.proxy.Builder;
import org.aoju.bus.proxy.Factory;
import org.aoju.bus.proxy.Interceptor;
import org.aoju.bus.proxy.Provider;

/**
 * @author Kimi Liu
 * @version 3.0.9
 * @since JDK 1.8
 */
public class InterceptorChain {

    private final Interceptor[] interceptors;

    public InterceptorChain(Interceptor[] interceptors) {
        this.interceptors = interceptors;
    }

    private Object createProxy(Factory factory, ClassLoader classLoader, Object terminus,
                               Class[] proxyClasses) {
        Object currentTarget = terminus;
        for (int i = interceptors.length - 1; i >= 0; --i) {
            currentTarget = factory
                    .createInterceptorProxy(classLoader, currentTarget, interceptors[i], proxyClasses);
        }
        return currentTarget;
    }

    public Provider createProxyProvider(Factory factory, Object terminus) {
        return createProxyProvider(factory, terminus, null);
    }

    public Provider createProxyProvider(Factory factory, Object terminus, Class[] proxyClasses) {
        return createProxyProvider(factory, Thread.currentThread().getContextClassLoader(), terminus,
                proxyClasses);
    }

    public Provider createProxyProvider(Factory factory, ClassLoader classLoader, Object terminus,
                                        Class[] proxyClasses) {
        if (proxyClasses == null || proxyClasses.length == 0) {
            proxyClasses = Builder.getAllInterfaces(terminus.getClass());
        }
        return new ProxyProvider(factory, classLoader, terminus, proxyClasses);
    }

    private class ProxyProvider implements Provider {
        private final ClassLoader classLoader;
        private final Class[] proxyClasses;
        private final Object terminus;
        private final Factory factory;

        public ProxyProvider(Factory factory, ClassLoader classLoader, Object terminus,
                             Class[] proxyClasses) {
            this.classLoader = classLoader;
            this.proxyClasses = proxyClasses;
            this.terminus = terminus;
            this.factory = factory;
        }

        public Object getObject() {
            return createProxy(factory, classLoader, terminus, proxyClasses);
        }
    }

}

