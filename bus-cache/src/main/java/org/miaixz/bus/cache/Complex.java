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
package org.miaixz.bus.cache;

import org.miaixz.bus.cache.magic.*;
import org.miaixz.bus.cache.magic.annotation.Cached;
import org.miaixz.bus.cache.magic.annotation.CachedGet;
import org.miaixz.bus.cache.magic.annotation.Invalid;
import org.miaixz.bus.cache.support.ArgNameGenerator;
import org.miaixz.bus.cache.support.CacheInfoContainer;
import org.miaixz.bus.cache.support.KeyGenerator;
import org.miaixz.bus.cache.support.SpelCalculator;
import org.miaixz.bus.core.annotation.Inject;
import org.miaixz.bus.core.annotation.Named;
import org.miaixz.bus.core.annotation.Singleton;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.proxy.invoker.ProxyChain;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
@Singleton
public class Complex {

    @Inject
    private Context config;

    @Inject
    private Manage cacheManager;

    @Inject
    @Named("singleCacheReader")
    private AbstractReader singleCacheReader;

    @Inject
    @Named("multiCacheReader")
    private AbstractReader multiCacheReader;

    public static boolean isSwitchOn(Context config, Cached cached, Method method, Object[] args) {
        return doIsSwitchOn(config.getCache() == Context.Switch.ON,
                cached.expire(), cached.condition(),
                method, args);
    }

    public static boolean isSwitchOn(Context config, Invalid invalid, Method method, Object[] args) {
        return doIsSwitchOn(config.getCache() == Context.Switch.ON,
                CacheExpire.FOREVER, invalid.condition(),
                method, args);
    }

    public static boolean isSwitchOn(Context config, CachedGet cachedGet, Method method, Object[] args) {
        return doIsSwitchOn(config.getCache() == Context.Switch.ON,
                CacheExpire.FOREVER, cachedGet.condition(),
                method, args);
    }

    private static boolean doIsSwitchOn(boolean openStat,
                                        int expire,
                                        String condition, Method method, Object[] args) {
        if (!openStat) {
            return false;
        }

        if (expire == CacheExpire.NO) {
            return false;
        }

        return (boolean) SpelCalculator.calcSpelValueWithContext(condition, ArgNameGenerator.getArgNames(method), args, true);
    }

    public Object read(CachedGet cachedGet, Method method, ProxyChain baseInvoker) throws Throwable {
        Object result;
        if (isSwitchOn(config, cachedGet, method, baseInvoker.getArguments())) {
            result = doReadWrite(method, baseInvoker, false);
        } else {
            result = baseInvoker.proceed();
        }
        return result;
    }

    public Object readWrite(Cached cached, Method method, ProxyChain baseInvoker) throws Throwable {
        Object result;
        if (isSwitchOn(config, cached, method, baseInvoker.getArguments())) {
            result = doReadWrite(method, baseInvoker, true);
        } else {
            result = baseInvoker.proceed();
        }
        return result;
    }

    public void remove(Invalid invalid, Method method, Object[] args) {
        if (isSwitchOn(config, invalid, method, args)) {

            long start = System.currentTimeMillis();

            AnnoHolder annoHolder = CacheInfoContainer.getCacheInfo(method).getLeft();
            if (annoHolder.isMulti()) {
                Map[] pair = KeyGenerator.generateMultiKey(annoHolder, args);
                Set<String> keys = ((Map<String, Object>) pair[1]).keySet();
                cacheManager.remove(invalid.value(), keys.toArray(new String[keys.size()]));

                Logger.info("multi cache clear, keys: {}", keys);
            } else {
                String key = KeyGenerator.generateSingleKey(annoHolder, args);
                cacheManager.remove(invalid.value(), key);

                Logger.info("single cache clear, key: {}", key);
            }

            Logger.debug("cache clear total cost [{}] ms", (System.currentTimeMillis() - start));
        }
    }

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

    public void write() {
        // TODO on @CachedPut
    }

}
