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
package org.aoju.bus.cache;

import org.aoju.bus.cache.annotation.Cached;
import org.aoju.bus.cache.annotation.CachedGet;
import org.aoju.bus.cache.annotation.Invalid;
import org.aoju.bus.cache.invoker.InvocationBaseInvoker;
import org.aoju.bus.cache.support.cache.Cache;
import org.aoju.bus.proxy.Factory;
import org.aoju.bus.proxy.Interceptor;
import org.aoju.bus.proxy.Invocation;
import org.aoju.bus.proxy.factory.cglib.CglibFactory;
import org.springframework.beans.factory.FactoryBean;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * @author Kimi Liu
 * @version 3.5.0
 * @since JDK 1.8
 */
public class CacheProxy<T> implements FactoryBean<T> {

    private Object target;

    private Object proxy;

    private Class<T> type;

    private CacheConfig.Switch cglib = CacheConfig.Switch.OFF;

    private CacheCore cacheCore;
    private Interceptor interceptor = new Interceptor() {

        @Override
        public Object intercept(Invocation invocation) throws Throwable {

            Method method = invocation.getMethod();
            Cached cached;
            if ((cached = method.getAnnotation(Cached.class)) != null) {
                return cacheCore.readWrite(cached, method, new InvocationBaseInvoker(target, invocation));
            }

            CachedGet cachedGet;
            if ((cachedGet = method.getAnnotation(CachedGet.class)) != null) {
                return cacheCore.read(cachedGet, method, new InvocationBaseInvoker(target, invocation));
            }

            Invalid invalid;
            if ((invalid = method.getAnnotation(Invalid.class)) != null) {
                cacheCore.remove(invalid, method, invocation.getArguments());
                return null;
            }

            return invocation.proceed();
        }
    };

    public CacheProxy(Object target, Map<String, Cache> caches) {
        this(target, (Class<T>) target.getClass().getInterfaces()[0], caches, CacheConfig.Switch.OFF);
    }

    public CacheProxy(Object target, Class<T> type, Map<String, Cache> caches, CacheConfig.Switch cglib) {
        this.target = target;
        this.type = type;
        this.cglib = cglib;
        this.proxy = newProxy();
        this.cacheCore = CacheModule.coreInstance(CacheConfig.newConfig(caches));
    }

    private Object newProxy() {
        Factory factory;
        if (cglib == CacheConfig.Switch.ON || !this.type.isInterface()) {
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
