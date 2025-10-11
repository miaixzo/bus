/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.cache;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import org.miaixz.bus.cache.magic.*;
import org.miaixz.bus.cache.magic.annotation.Cached;
import org.miaixz.bus.cache.magic.annotation.CachedGet;
import org.miaixz.bus.cache.magic.annotation.Invalid;
import org.miaixz.bus.cache.reader.AbstractReader;
import org.miaixz.bus.cache.magic.MethodHolder;
import org.miaixz.bus.cache.support.CacheInfoContainer;
import org.miaixz.bus.cache.support.SpelCalculator;
import org.miaixz.bus.core.lang.EnumValue;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.proxy.invoker.ProxyChain;

/**
 * The core processing unit of the cache framework, orchestrating read, write, and invalidation operations.
 * <p>
 * This class acts as the central handler for cache-related annotations. It determines whether to apply caching logic
 * based on global and annotation-specific conditions, and then delegates the actual work to the appropriate components
 * like {@link Manage} and {@link AbstractReader}.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Complex {

    /**
     * The configuration context for the cache module.
     */
    private Context context;

    /**
     * The manager for all registered cache instances.
     */
    private Manage manage;

    /**
     * The reader responsible for handling single-key cache lookups.
     */
    private AbstractReader singleCacheReader;

    /**
     * The reader responsible for handling multi-key batch cache lookups.
     */
    private AbstractReader multiCacheReader;

    /**
     * Checks if caching is enabled for a method annotated with {@link Cached}.
     *
     * @param config The global cache configuration.
     * @param cached The {@link Cached} annotation instance.
     * @param method The annotated method.
     * @param args   The arguments passed to the method.
     * @return {@code true} if caching is active and all conditions are met, otherwise {@code false}.
     */
    public static boolean isSwitchOn(Context config, Cached cached, Method method, Object[] args) {
        return doIsSwitchOn(
                config.getCache() == EnumValue.Switch.ON,
                cached.expire(),
                cached.condition(),
                method,
                args);
    }

    /**
     * Checks if cache invalidation is enabled for a method annotated with {@link Invalid}.
     *
     * @param config  The global cache configuration.
     * @param invalid The {@link Invalid} annotation instance.
     * @param method  The annotated method.
     * @param args    The arguments passed to the method.
     * @return {@code true} if invalidation is active and all conditions are met, otherwise {@code false}.
     */
    public static boolean isSwitchOn(Context config, Invalid invalid, Method method, Object[] args) {
        return doIsSwitchOn(
                config.getCache() == EnumValue.Switch.ON,
                CacheExpire.FOREVER, // Invalidation is not subject to expiration
                invalid.condition(),
                method,
                args);
    }

    /**
     * Checks if caching is enabled for a method annotated with {@link CachedGet}.
     *
     * @param config    The global cache configuration.
     * @param cachedGet The {@link CachedGet} annotation instance.
     * @param method    The annotated method.
     * @param args      The arguments passed to the method.
     * @return {@code true} if caching is active and all conditions are met, otherwise {@code false}.
     */
    public static boolean isSwitchOn(Context config, CachedGet cachedGet, Method method, Object[] args) {
        return doIsSwitchOn(
                config.getCache() == EnumValue.Switch.ON,
                CacheExpire.FOREVER, // Read-only operations are not subject to expiration
                cachedGet.condition(),
                method,
                args);
    }

    /**
     * Executes a read-through cache operation for a method annotated with {@link CachedGet}.
     * <p>
     * If caching is disabled or conditions are not met, it proceeds with the original method invocation. Otherwise, it
     * attempts to read from the cache without performing a subsequent write on a cache miss.
     * </p>
     *
     * @param cachedGet   The {@link CachedGet} annotation from the method.
     * @param method      The target method.
     * @param baseInvoker The proxy chain invoker to proceed with the original method call.
     * @return The cached value or the result from the original method invocation.
     * @throws Throwable if the underlying method invocation throws an exception.
     */
    public Object read(CachedGet cachedGet, Method method, ProxyChain baseInvoker) throws Throwable {
        Object result;
        if (isSwitchOn(context, cachedGet, method, baseInvoker.getArguments())) {
            result = doReadWrite(method, baseInvoker, false); // false: do not write on miss
        } else {
            result = baseInvoker.proceed();
        }
        return result;
    }

    /**
     * Executes a full read-write cache operation for a method annotated with {@link Cached}.
     * <p>
     * If caching is enabled, it attempts to read from the cache. On a cache miss, it invokes the original method,
     * writes the result to the cache, and then returns the result.
     * </p>
     *
     * @param cached      The {@link Cached} annotation from the method.
     * @param method      The target method.
     * @param baseInvoker The proxy chain invoker.
     * @return The cached value or the result from the original method invocation.
     * @throws Throwable if the underlying method invocation throws an exception.
     */
    public Object readWrite(Cached cached, Method method, ProxyChain baseInvoker) throws Throwable {
        Object result;
        if (isSwitchOn(context, cached, method, baseInvoker.getArguments())) {
            result = doReadWrite(method, baseInvoker, true); // true: write on miss
        } else {
            result = baseInvoker.proceed();
        }
        return result;
    }

    /**
     * Executes a cache invalidation operation for a method annotated with {@link Invalid}.
     * <p>
     * If invalidation is enabled, it generates the cache key(s) based on the method's arguments and removes the
     * corresponding entries from the cache.
     * </p>
     *
     * @param invalid The {@link Invalid} annotation from the method.
     * @param method  The target method.
     * @param args    The arguments passed to the method.
     */
    public void remove(Invalid invalid, Method method, Object[] args) {
        if (isSwitchOn(context, invalid, method, args)) {
            long start = System.currentTimeMillis();
            AnnoHolder annoHolder = CacheInfoContainer.getCacheInfo(method).getLeft();
            if (annoHolder.isMulti()) {
                Map[] pair = Builder.generateMultiKey(annoHolder, args);
                Set<String> keys = ((Map<String, Object>) pair[1]).keySet();
                manage.remove(invalid.value(), keys.toArray(new String[0]));
                Logger.info("multi cache clear, keys: {}", keys);
            } else {
                String key = Builder.generateSingleKey(annoHolder, args);
                manage.remove(invalid.value(), key);
                Logger.info("single cache clear, key: {}", key);
            }
            Logger.debug("cache clear total cost [{}] ms", (System.currentTimeMillis() - start));
        }
    }

    /**
     * Placeholder for handling cache write operations (e.g., for {@code @CachedPut}).
     */
    public void write() {
        // TODO: Implement logic for @CachedPut
    }

    /**
     * Sets the cache context configuration.
     *
     * @param context The cache context configuration.
     */
    public void setContext(Context context) {
        this.context = context;
    }

    /**
     * Sets the cache manager.
     *
     * @param manage The cache manager.
     */
    public void setManage(Manage manage) {
        this.manage = manage;
    }

    /**
     * Sets the reader for single-key cache operations.
     *
     * @param singleCacheReader The single-key cache reader.
     */
    public void setSingleCacheReader(AbstractReader singleCacheReader) {
        this.singleCacheReader = singleCacheReader;
    }

    /**
     * Sets the reader for multi-key cache operations.
     *
     * @param multiCacheReader The multi-key cache reader.
     */
    public void setMultiCacheReader(AbstractReader multiCacheReader) {
        this.multiCacheReader = multiCacheReader;
    }

    /**
     * Internal helper to determine if caching should be active based on multiple conditions.
     *
     * @param openStat  The global cache switch status.
     * @param expire    The expiration policy for the specific operation.
     * @param condition A SpEL expression that must evaluate to {@code true}.
     * @param method    The target method.
     * @param args      The method's arguments.
     * @return {@code true} if all conditions for caching are met.
     */
    private static boolean doIsSwitchOn(boolean openStat, int expire, String condition, Method method, Object[] args) {
        if (!openStat) {
            return false;
        }
        if (expire == CacheExpire.NO) {
            return false;
        }
        // Evaluate the SpEL condition
        return (boolean) SpelCalculator.calcSpelValueWithContext(condition, Builder.getArgNames(method), args, true);
    }

    /**
     * Internal implementation of the read/write logic.
     * <p>
     * It retrieves the cached annotation information and delegates to the appropriate reader (single-key or multi-key)
     * to perform the operation.
     * </p>
     *
     * @param method      The target method.
     * @param baseInvoker The proxy chain invoker.
     * @param needWrite   If {@code true}, the result will be written to the cache on a miss.
     * @return The cached value or the result from the original method invocation.
     * @throws Throwable if the underlying method invocation throws an exception.
     */
    private Object doReadWrite(Method method, ProxyChain baseInvoker, boolean needWrite) throws Throwable {
        long start = System.currentTimeMillis();
        CachePair<AnnoHolder, MethodHolder> pair = CacheInfoContainer.getCacheInfo(method);
        AnnoHolder annoHolder = pair.getLeft();
        MethodHolder methodHolder = pair.getRight();
        Object result;
        if (annoHolder.isMulti()) {
            result = multiCacheReader.read(annoHolder, methodHolder, baseInvoker, needWrite);
        } else {
            result = singleCacheReader.read(annoHolder, methodHolder, baseInvoker, needWrite);
        }
        Logger.debug("cache read total cost [{}] ms", (System.currentTimeMillis() - start));
        return result;
    }

}
