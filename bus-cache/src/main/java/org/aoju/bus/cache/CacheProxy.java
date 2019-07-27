package org.aoju.bus.cache;

import org.aoju.bus.cache.annotation.Cached;
import org.aoju.bus.cache.annotation.CachedGet;
import org.aoju.bus.cache.annotation.Invalid;
import org.aoju.bus.cache.invoker.InvocationBaseInvoker;
import org.aoju.bus.cache.support.cache.Cache;
import org.apache.commons.proxy.Interceptor;
import org.apache.commons.proxy.Invocation;
import org.apache.commons.proxy.ProxyFactory;
import org.apache.commons.proxy.factory.cglib.CglibProxyFactory;
import org.springframework.beans.factory.FactoryBean;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
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
        ProxyFactory factory;
        if (cglib == CacheConfig.Switch.ON || !this.type.isInterface()) {
            factory = new CglibProxyFactory();
        } else {
            factory = new ProxyFactory();
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