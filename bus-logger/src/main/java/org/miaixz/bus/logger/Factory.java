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
package org.miaixz.bus.logger;

import org.miaixz.bus.core.toolkit.CallerKit;
import org.miaixz.bus.core.toolkit.ResourceKit;
import org.miaixz.bus.logger.magic.Log;
import org.miaixz.bus.logger.metric.console.ConsoleFactory;
import org.miaixz.bus.logger.metric.jdk.JdkFactory;

import java.net.URL;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 日志工厂类
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class Factory {

    /**
     * 日志对象缓存
     */
    private final Map<Object, Log> logCache;
    /**
     * 日志框架名,用于打印当前所用日志框架
     */
    protected String name;

    /**
     * 构造
     *
     * @param name 日志框架名
     */
    public Factory(String name) {
        this.name = name;
        logCache = new ConcurrentHashMap<>();
    }

    /**
     * 决定日志实现
     * 依次按照顺序检查日志库的jar是否被引入,如果未引入任何日志库,
     * 则检查ClassPath下的logging.properties,存在则使用JdkLogFactory,
     * 否则使用ConsoleLogFactory
     *
     * @return 日志实现类
     */
    public static Factory of() {
        final Factory factory = create();
        factory.getLog(Factory.class).debug("Use [{}] Logger As Default.", factory.name);
        return factory;
    }

    /**
     * 决定日志实现
     * 依次按照顺序检查日志库的jar是否被引入,如果未引入任何日志库,则检查ClassPath下的logging.properties,存在则使用JdkLogFactory,否则使用ConsoleLogFactory
     *
     * @return 日志实现类
     */
    private static Factory create() {
        final ServiceLoader<Factory> factories = ServiceLoader.load(Factory.class);
        for (Factory factory : factories) {
            return factory;
        }

        // 未找到任何可支持的日志库时判断依据：当JDK Logging的配置文件位于classpath中，使用JDK Logging，否则使用Console
        final URL url = ResourceKit.getResourceUrl("logging.properties");
        return (null != url) ? new JdkFactory() : new ConsoleFactory();
    }

    /**
     * @return 当前使用的日志工厂
     */
    public static Factory getCurrentLogFactory() {
        return Holder.get();
    }

    /**
     * 自定义日志实现
     *
     * @param logFactoryClass 日志工厂类
     * @return 自定义的日志工厂类
     */
    public static Factory setCurrentLogFactory(Class<? extends Factory> logFactoryClass) {
        return Holder.set(logFactoryClass);
    }

    /**
     * 自定义日志实现
     *
     * @param factory 日志工厂类对象
     * @return 自定义的日志工厂类
     */
    public static Factory setCurrentLogFactory(Factory factory) {
        return Holder.set(factory);
    }

    /**
     * 获得日志对象
     *
     * @param name 日志对象名
     * @return 日志对象
     */
    public static Log get(String name) {
        return getCurrentLogFactory().getLog(name);
    }

    /**
     * 获得日志对象
     *
     * @param clazz 日志对应类
     * @return 日志对象
     */
    public static Log get(Class<?> clazz) {
        return getCurrentLogFactory().getLog(clazz);
    }

    /**
     * @return 获得调用者的日志
     */
    public static Log get() {
        return get(CallerKit.getCallers());
    }

    /**
     * 获取日志框架名,用于打印当前所用日志框架
     *
     * @return 日志框架名
     */
    public String getName() {
        return this.name;
    }

    /**
     * 获得日志对象
     *
     * @param name 日志对象名
     * @return 日志对象
     */
    public Log getLog(String name) {
        Log log = logCache.get(name);
        if (null == log) {
            log = createLog(name);
            logCache.put(name, log);
        }
        return log;
    }

    /**
     * 获得日志对象
     *
     * @param clazz 日志对应类
     * @return 日志对象
     */
    public Log getLog(Class<?> clazz) {
        Log log = logCache.get(clazz);
        if (null == log) {
            log = createLog(clazz);
            logCache.put(clazz, log);
        }
        return log;
    }

    /**
     * 创建日志对象
     *
     * @param name 日志对象名
     * @return 日志对象
     */
    public abstract Log createLog(String name);

    /**
     * 创建日志对象
     *
     * @param clazz 日志对应类
     * @return 日志对象
     */
    public abstract Log createLog(Class<?> clazz);

    /**
     * 检查日志实现是否存在
     * 此方法仅用于检查所提供的日志相关类是否存在,当传入的日志类类不存在时抛出ClassNotFoundException
     * 此方法的作用是在detectLogFactory方法自动检测所用日志时,如果实现类不存在,调用此方法会自动抛出异常,从而切换到下一种日志的检测
     *
     * @param logClassName 日志实现相关类
     */
    protected void checkLogExist(Class<?> logClassName) {

    }

}
