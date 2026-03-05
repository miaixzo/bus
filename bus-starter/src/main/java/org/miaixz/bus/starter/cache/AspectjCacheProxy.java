/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
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
 * An AOP aspect that provides caching functionality by intercepting method calls annotated with {@link CachedGet},
 * {@link Cached}, and {@link Invalid}.
 * <p>
 * This class acts as a proxy to the core caching logic, delegating the actual cache operations (read, write,
 * invalidate) to the underlying caching module.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Aspect
public class AspectjCacheProxy {

    private final Complex core;

    /**
     * Constructs a new cache proxy with a map of cache configurations.
     *
     * @param caches A map where the key is the cache name and the value is the {@link CacheX} instance.
     */
    public AspectjCacheProxy(Map<String, CacheX> caches) {
        this(Context.newConfig(caches));
    }

    /**
     * Constructs a new cache proxy with a pre-configured caching context.
     *
     * @param context The caching {@link Context} to be used.
     */
    public AspectjCacheProxy(Context context) {
        this.core = Module.instance(context);
    }

    /**
     * Around advice for methods annotated with {@link CachedGet}.
     * <p>
     * This advice intercepts the method call, attempts to retrieve the result from the cache, and if not found,
     * proceeds with the original method execution.
     * </p>
     *
     * @param point The proceeding join point.
     * @return The cached or newly computed result.
     * @throws Throwable if the intercepted method throws an exception.
     */
    @Around("@annotation(org.miaixz.bus.cache.magic.annotation.CachedGet)")
    public Object read(ProceedingJoinPoint point) throws Throwable {
        Method method = getMethod(point);
        CachedGet cachedGet = method.getAnnotation(CachedGet.class);
        return core.read(cachedGet, method, new InvocationInvoker(point));
    }

    /**
     * Around advice for methods annotated with {@link Cached}.
     * <p>
     * This advice implements read-through/write-through caching. It first attempts to retrieve the result from the
     * cache. If the result is not found, it proceeds with the original method execution, caches the result, and then
     * returns it.
     * </p>
     *
     * @param point The proceeding join point.
     * @return The cached or newly computed result.
     * @throws Throwable if the intercepted method throws an exception.
     */
    @Around("@annotation(org.miaixz.bus.cache.magic.annotation.Cached)")
    public Object readWrite(ProceedingJoinPoint point) throws Throwable {
        Method method = getMethod(point);
        Cached cached = method.getAnnotation(Cached.class);
        return core.readWrite(cached, method, new InvocationInvoker(point));
    }

    /**
     * After advice for methods annotated with {@link Invalid}.
     * <p>
     * This advice invalidates or removes one or more entries from the cache after the successful execution of the
     * annotated method.
     * </p>
     *
     * @param point The join point.
     * @throws Throwable if there is an error during cache invalidation.
     */
    @After("@annotation(org.miaixz.bus.cache.magic.annotation.Invalid)")
    public void remove(JoinPoint point) throws Throwable {
        Method method = getMethod(point);
        Invalid invalid = method.getAnnotation(Invalid.class);
        core.remove(invalid, method, point.getArgs());
    }

    /**
     * Retrieves the actual {@link Method} from a {@link JoinPoint}.
     * <p>
     * This helper method is necessary to handle cases where the intercepted method is defined in an interface. It
     * ensures that the method from the actual target class is retrieved, which is required for annotation processing.
     * </p>
     *
     * @param point The join point.
     * @return The resolved {@link Method}.
     * @throws NoSuchMethodException if the method cannot be found on the target class.
     */
    private Method getMethod(JoinPoint point) throws NoSuchMethodException {
        MethodSignature ms = (MethodSignature) point.getSignature();
        Method method = ms.getMethod();
        if (method.getDeclaringClass().isInterface()) {
            method = point.getTarget().getClass().getDeclaredMethod(ms.getName(), method.getParameterTypes());
        }
        return method;
    }

}
