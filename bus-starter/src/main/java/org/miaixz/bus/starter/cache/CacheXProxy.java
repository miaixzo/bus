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
package org.miaixz.bus.starter.cache;

import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.cache.Complex;
import org.miaixz.bus.cache.Context;
import org.miaixz.bus.cache.Module;
import org.miaixz.bus.cache.annotation.Cached;
import org.miaixz.bus.cache.annotation.CachedGet;
import org.miaixz.bus.cache.annotation.Invalid;
import org.miaixz.bus.proxy.Factory;
import org.miaixz.bus.proxy.Interceptor;
import org.miaixz.bus.proxy.Invocation;
import org.miaixz.bus.proxy.factory.cglib.CglibFactory;
import org.miaixz.bus.proxy.invoker.JoinPointInvoker;
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
        Factory factory;
        if (cglib == Context.Switch.ON || !this.type.isInterface()) {
            factory = new CglibFactory();
        } else {
            factory = new Factory();
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
