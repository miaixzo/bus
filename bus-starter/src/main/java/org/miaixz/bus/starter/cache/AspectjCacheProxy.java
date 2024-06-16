/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 */
package org.miaixz.bus.starter.cache;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.cache.Complex;
import org.miaixz.bus.cache.Context;
import org.miaixz.bus.cache.Module;
import org.miaixz.bus.cache.magic.annotation.Cached;
import org.miaixz.bus.cache.magic.annotation.CachedGet;
import org.miaixz.bus.cache.magic.annotation.Invalid;
import org.miaixz.bus.proxy.invoker.InvocationInvoker;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
@Aspect
public class AspectjCacheProxy {

    private Complex core;

    public AspectjCacheProxy(Map<String, CacheX> caches) {
        this(Context.newConfig(caches));
    }

    public AspectjCacheProxy(Context config) {
        core = Module.coreInstance(config);
    }

    @Around("@annotation(org.miaixz.bus.cache.magic.annotation.CachedGet)")
    public Object read(ProceedingJoinPoint point) throws Throwable {
        Method method = getMethod(point);
        CachedGet cachedGet = method.getAnnotation(CachedGet.class);
        return core.read(cachedGet, method, new InvocationInvoker(point));
    }

    @Around("@annotation(org.miaixz.bus.cache.magic.annotation.Cached)")
    public Object readWrite(ProceedingJoinPoint point) throws Throwable {
        Method method = getMethod(point);
        Cached cached = method.getAnnotation(Cached.class);
        return core.readWrite(cached, method, new InvocationInvoker(point));
    }

    @After("@annotation(org.miaixz.bus.cache.magic.annotation.Invalid)")
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
