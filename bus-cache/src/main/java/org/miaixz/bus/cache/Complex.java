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
 * 缓存复合操作类，提供缓存读写、删除等核心功能
 * <p>
 * 该类是缓存框架的核心处理器，负责协调缓存读取、写入和删除操作， 支持单键和多键缓存操作，并提供条件判断和SpEL表达式解析功能。
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Complex {

    /**
     * 缓存上下文配置
     */
    private Context context;

    /**
     * 缓存管理器
     */
    private Manage manage;

    /**
     * 单键缓存读取器
     */
    private AbstractReader singleCacheReader;

    /**
     * 多键缓存读取器
     */
    private AbstractReader multiCacheReader;

    /**
     * 判断缓存开关是否打开
     *
     * @param config 缓存配置
     * @param cached 缓存注解
     * @param method 方法对象
     * @param args   方法参数
     * @return 如果缓存开关打开且满足条件则返回true，否则返回false
     */
    public static boolean isSwitchOn(Context config, Cached cached, Method method, Object[] args) {
        return doIsSwitchOn(config.getCache() == EnumValue.Switch.ON, cached.expire(), cached.condition(), method,
                args);
    }

    /**
     * 判断缓存开关是否打开
     *
     * @param config  缓存配置
     * @param invalid 失效注解
     * @param method  方法对象
     * @param args    方法参数
     * @return 如果缓存开关打开且满足条件则返回true，否则返回false
     */
    public static boolean isSwitchOn(Context config, Invalid invalid, Method method, Object[] args) {
        return doIsSwitchOn(config.getCache() == EnumValue.Switch.ON, CacheExpire.FOREVER, invalid.condition(), method,
                args);
    }

    /**
     * 判断缓存开关是否打开
     *
     * @param config    缓存配置
     * @param cachedGet 获取缓存注解
     * @param method    方法对象
     * @param args      方法参数
     * @return 如果缓存开关打开且满足条件则返回true，否则返回false
     */
    public static boolean isSwitchOn(Context config, CachedGet cachedGet, Method method, Object[] args) {
        return doIsSwitchOn(config.getCache() == EnumValue.Switch.ON, CacheExpire.FOREVER, cachedGet.condition(),
                method, args);
    }

    /**
     * 执行缓存读取操作
     *
     * @param cachedGet   获取缓存注解
     * @param method      方法对象
     * @param baseInvoker 代理调用链
     * @return 缓存值或方法执行结果
     * @throws Throwable 可能抛出的异常
     */
    public Object read(CachedGet cachedGet, Method method, ProxyChain baseInvoker) throws Throwable {
        Object result;
        if (isSwitchOn(context, cachedGet, method, baseInvoker.getArguments())) {
            result = doReadWrite(method, baseInvoker, false);
        } else {
            result = baseInvoker.proceed();
        }
        return result;
    }

    /**
     * 执行缓存读写操作
     *
     * @param cached      缓存注解
     * @param method      方法对象
     * @param baseInvoker 代理调用链
     * @return 缓存值或方法执行结果
     * @throws Throwable 可能抛出的异常
     */
    public Object readWrite(Cached cached, Method method, ProxyChain baseInvoker) throws Throwable {
        Object result;
        if (isSwitchOn(context, cached, method, baseInvoker.getArguments())) {
            result = doReadWrite(method, baseInvoker, true);
        } else {
            result = baseInvoker.proceed();
        }
        return result;
    }

    /**
     * 执行缓存删除操作
     *
     * @param invalid 失效注解
     * @param method  方法对象
     * @param args    方法参数
     */
    public void remove(Invalid invalid, Method method, Object[] args) {
        if (isSwitchOn(context, invalid, method, args)) {
            long start = System.currentTimeMillis();
            AnnoHolder annoHolder = CacheInfoContainer.getCacheInfo(method).getLeft();
            if (annoHolder.isMulti()) {
                Map[] pair = Builder.generateMultiKey(annoHolder, args);
                Set<String> keys = ((Map<String, Object>) pair[1]).keySet();
                manage.remove(invalid.value(), keys.toArray(new String[keys.size()]));
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
     * 执行缓存写入操作（待实现）
     */
    public void write() {
        // TODO on @CachedPut
    }

    /**
     * 设置缓存上下文配置
     *
     * @param context 缓存上下文配置
     */
    public void setContext(Context context) {
        this.context = context;
    }

    /**
     * 设置缓存管理器
     *
     * @param manage 缓存管理器
     */
    public void setManage(Manage manage) {
        this.manage = manage;
    }

    /**
     * 设置单键缓存读取器
     *
     * @param singleCacheReader 单键缓存读取器
     */
    public void setSingleCacheReader(AbstractReader singleCacheReader) {
        this.singleCacheReader = singleCacheReader;
    }

    /**
     * 设置多键缓存读取器
     *
     * @param multiCacheReader 多键缓存读取器
     */
    public void setMultiCacheReader(AbstractReader multiCacheReader) {
        this.multiCacheReader = multiCacheReader;
    }

    /**
     * 判断缓存开关是否打开的内部实现
     *
     * @param openStat  缓存开关状态
     * @param expire    过期时间
     * @param condition 条件表达式
     * @param method    方法对象
     * @param args      方法参数
     * @return 如果缓存开关打开且满足条件则返回true，否则返回false
     */
    private static boolean doIsSwitchOn(boolean openStat, int expire, String condition, Method method, Object[] args) {
        if (!openStat) {
            return false;
        }
        if (expire == CacheExpire.NO) {
            return false;
        }
        return (boolean) SpelCalculator.calcSpelValueWithContext(condition, Builder.getArgNames(method), args, true);
    }

    /**
     * 执行缓存读写操作的内部实现
     *
     * @param method      方法对象
     * @param baseInvoker 代理调用链
     * @param needWrite   是否需要写入
     * @return 缓存值或方法执行结果
     * @throws Throwable 可能抛出的异常
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