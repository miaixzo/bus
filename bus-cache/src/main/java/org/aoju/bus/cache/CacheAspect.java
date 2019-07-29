package org.aoju.bus.cache;

import org.aoju.bus.cache.annotation.Cached;
import org.aoju.bus.cache.annotation.CachedGet;
import org.aoju.bus.cache.annotation.Invalid;
import org.aoju.bus.cache.invoker.JoinPointBaseInvoker;
import org.aoju.bus.cache.support.cache.Cache;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
@Aspect
public class CacheAspect {

    private CacheCore core;

    public CacheAspect(Map<String, Cache> caches) {
        this(CacheConfig.newConfig(caches));
    }

    public CacheAspect(CacheConfig config) {
        core = CacheModule.coreInstance(config);
    }

    @Around("@annotation(org.aoju.bus.cache.annotation.CachedGet)")
    public Object read(ProceedingJoinPoint point) throws Throwable {
        Method method = getMethod(point);
        CachedGet cachedGet = method.getAnnotation(CachedGet.class);
        return core.read(cachedGet, method, new JoinPointBaseInvoker(point));
    }

    @Around("@annotation(org.aoju.bus.cache.annotation.Cached)")
    public Object readWrite(ProceedingJoinPoint point) throws Throwable {
        Method method = getMethod(point);
        Cached cached = method.getAnnotation(Cached.class);

        return core.readWrite(cached, method, new JoinPointBaseInvoker(point));
    }

    @After("@annotation(org.aoju.bus.cache.annotation.Invalid)")
    public void remove(JoinPoint point) throws Throwable {
        Method method = getMethod(point);
        Invalid invalid = method.getAnnotation(Invalid.class);
        core.remove(invalid, method, point.getArgs());
    }

    private Method getMethod(JoinPoint point) throws NoSuchMethodException {
        MethodSignature ms = (MethodSignature) point.getSignature();
        Method method = ms.getMethod();
        if (method.getDeclaringClass().isInterface()) {
            method = point.getTarget().getClass().getDeclaredMethod(ms.getName(), method.getParameterTypes());
        }

        return method;
    }
}
