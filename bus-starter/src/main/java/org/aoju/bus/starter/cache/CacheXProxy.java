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
package org.aoju.bus.starter.cache;

import org.aoju.bus.cache.CacheX;
import org.aoju.bus.cache.Complex;
import org.aoju.bus.cache.Context;
import org.aoju.bus.cache.Module;
import org.aoju.bus.cache.annotation.Cached;
import org.aoju.bus.cache.annotation.CachedGet;
import org.aoju.bus.cache.annotation.Invalid;
import org.aoju.bus.proxy.Interceptor;
import org.aoju.bus.proxy.Invocation;
import org.aoju.bus.proxy.factory.cglib.CglibFactory;
import org.aoju.bus.proxy.invoker.JoinPointInvoker;
import org.springframework.beans.factory.FactoryBean;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class CacheXProxy<T> implements FactoryBean<T> {

    private Object target;

    private Object proxy;

    private Class<T> type;

    private Context.Switch cglib = Context.Switch.OFF;

    private Complex cacheCore;
    private Interceptor interceptor = new Interceptor() {

        @Override
        public Object intercept(Invocation invocation) throws Throwable {

            Method method = invocation.getMethod();
            Cached cached;
            if (null != (cached = method.getAnnotation(Cached.class))) {
                return cacheCore.readWrite(cached, method, new JoinPointInvoker(target, invocation));
            }

            CachedGet cachedGet;
            if (null != (cachedGet = method.getAnnotation(CachedGet.class))) {
                return cacheCore.read(cachedGet, method, new JoinPointInvoker(target, invocation));
            }

            Invalid invalid;
            if (null != (invalid = method.getAnnotation(Invalid.class))) {
                cacheCore.remove(invalid, method, invocation.getArguments());
                return null;
            }

            return invocation.proceed();
        }
    };

    public CacheXProxy(Object target, Map<String, CacheX> caches) {
        this(target, (Class<T>) target.getClass().getInterfaces()[0], caches, Context.Switch.OFF);
    }

    public CacheXProxy(Object target, Class<T> type, Map<String, CacheX> caches, Context.Switch cglib) {
        this.target = target;
        this.type = type;
        this.cglib = cglib;
        this.proxy = newProxy();
        this.cacheCore = Module.coreInstance(Context.newConfig(caches));
    }

    private Object newProxy() {
        org.aoju.bus.proxy.Factory factory;
        if (cglib == Context.Switch.ON || !this.type.isInterface()) {
            factory = new CglibFactory();
        } else {
            factory = new org.aoju.bus.proxy.Factory();
        }

        return factory.createInterceptorProxy(target, interceptor, new Class[]{type});
    }

    @Override
    public T getObject() {
        return (T) proxy;
    }

    @Override
    public Class<T> getObjectType() {
        return type;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

}
