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

import org.miaixz.bus.proxy.Builder;
import org.miaixz.bus.proxy.Factory;
import org.miaixz.bus.proxy.Interceptor;
import org.miaixz.bus.proxy.Provider;

/**
 * 一个InterceptorChain帮助创建通过一系列
 * {@link Interceptor interceptors}的代理
 *
 * @author Kimi Liu
 * @since Java 17+
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

    /**
     * 创建一个{@link  Provider}，它将返回一个代理，
     * 该代理通过这个拦截器链发送方法调用， 并最终到达提供的terminus对象。
     * 代理将支持由terminus对象实现的所有接口。 线程上下文类装入器将用于生成代理类
     *
     * @param factory 用于创建代理的{@link Factory}
     * @param object  代理对象
     * @return 它将返回一个{@link Provider}代理，该代理通过这个拦截器链发送方法调用，
     * 并最终到达提供的object对象
     */
    public Provider createProxyProvider(Factory factory, Object object) {
        return createProxyProvider(factory, object, null);
    }

    /**
     * 创建一个{@link Provider}，它将返回一个代理，该代理通过这个拦截器链发送方法调用，
     * 并最终到达提供的terminus对象。代理将只支持指定的接口/类。线程上下文类装入器将用于生成代理类.
     *
     * @param factory      用于创建代理的{@link Factory}
     * @param object       代理对象
     * @param proxyClasses 支持的接口
     * @return 它将返回一个{@link Provider}代理，该代理通过这个拦截器链发送方法调用，
     * 并最终到达提供的object对象
     */
    public Provider createProxyProvider(Factory factory, Object object, Class[] proxyClasses) {
        return createProxyProvider(factory, Thread.currentThread().getContextClassLoader(), object,
                proxyClasses);
    }

    /**
     * 创建一个{@link Provider}，它将返回一个代理，该代理通过这个拦截器链发送方法调用，
     * 并最终到达提供的terminus对象。代理将只支持指定的接口/类。指定的类装入器将用于生成代理类.
     *
     * @param factory      用于创建代理的{@link Factory}
     * @param classLoader  用于生成代理类的类加载器
     * @param object       代理对象
     * @param proxyClasses 支持的接口
     * @return 它将返回一个{@link Provider}代理，该代理通过这个拦截器链发送方法调用，
     * 并最终到达提供的object对象
     */
    public Provider createProxyProvider(Factory factory, ClassLoader classLoader, Object object,
                                        Class[] proxyClasses) {
        if (null == proxyClasses || proxyClasses.length == 0) {
            proxyClasses = Builder.getAllInterfaces(object.getClass());
        }
        return new ProxyProvider(factory, classLoader, object, proxyClasses);
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

