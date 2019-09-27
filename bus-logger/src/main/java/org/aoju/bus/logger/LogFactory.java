/*
 * The MIT License
 *
 * Copyright (c) 2017 aoju.org All rights reserved.
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
package org.aoju.bus.logger;

import org.aoju.bus.core.lang.Console;
import org.aoju.bus.core.utils.CallerUtils;
import org.aoju.bus.core.utils.ResourceUtils;
import org.aoju.bus.logger.dialect.commons.ApacheCommonsLogFactory;
import org.aoju.bus.logger.dialect.console.ConsoleLogFactory;
import org.aoju.bus.logger.dialect.jboss.JbossLogFactory;
import org.aoju.bus.logger.dialect.jdk.JdkLogFactory;
import org.aoju.bus.logger.dialect.log4j.Log4jLogFactory;
import org.aoju.bus.logger.dialect.log4j2.Log4j2LogFactory;
import org.aoju.bus.logger.dialect.slf4j.Slf4jLogFactory;
import org.aoju.bus.logger.dialect.tinylog.TinyLogFactory;

import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 日志工厂类
 *
 * @author Kimi Liu
 * @version 3.6.0
 * @see Slf4jLogFactory
 * @see Log4j2LogFactory
 * @see Log4jLogFactory
 * @see ApacheCommonsLogFactory
 * @see TinyLogFactory
 * @see JbossLogFactory
 * @see ConsoleLogFactory
 * @see JdkLogFactory
 * @since JDK 1.8
 */
public abstract class LogFactory {

    /**
     * 日志框架名，用于打印当前所用日志框架
     */
    protected String name;
    /**
     * 日志对象缓存
     */
    private Map<Object, Log> logCache;

    /**
     * 构造
     *
     * @param name 日志框架名
     */
    public LogFactory(String name) {
        this.name = name;
        logCache = new ConcurrentHashMap<>();
    }

    /**
     * 决定日志实现
     * <p>
     * 依次按照顺序检查日志库的jar是否被引入，如果未引入任何日志库，则检查ClassPath下的logging.properties，存在则使用JdkLogFactory，否则使用ConsoleLogFactory
     *
     * @return 日志实现类
     * @see Slf4jLogFactory
     * @see Log4j2LogFactory
     * @see Log4jLogFactory
     * @see ApacheCommonsLogFactory
     * @see TinyLogFactory
     * @see JbossLogFactory
     * @see ConsoleLogFactory
     * @see JdkLogFactory
     */
    public static LogFactory create() {
        final LogFactory factory = doCreate();
        factory.getLog(LogFactory.class).debug("Use [{}] Logger As Default.", factory.name);
        return factory;
    }

    /**
     * 决定日志实现
     * <p>
     * 依次按照顺序检查日志库的jar是否被引入，如果未引入任何日志库，则检查ClassPath下的logging.properties，存在则使用JdkLogFactory，否则使用ConsoleLogFactory
     *
     * @return 日志实现类
     * @see Slf4jLogFactory
     * @see Log4j2LogFactory
     * @see Log4jLogFactory
     * @see ApacheCommonsLogFactory
     * @see TinyLogFactory
     * @see JbossLogFactory
     * @see ConsoleLogFactory
     * @see JdkLogFactory
     */
    private static LogFactory doCreate() {
        try {
            return new Slf4jLogFactory(true);
        } catch (NoClassDefFoundError e) {
            Console.error(e.getMessage());
        }
        try {
            return new Log4j2LogFactory();
        } catch (NoClassDefFoundError e) {
            Console.error(e.getMessage());
        }
        try {
            return new Log4jLogFactory();
        } catch (NoClassDefFoundError e) {
            Console.error(e.getMessage());
        }
        try {
            return new ApacheCommonsLogFactory();
        } catch (NoClassDefFoundError e) {
            Console.error(e.getMessage());
        }
        try {
            return new TinyLogFactory();
        } catch (NoClassDefFoundError e) {
            Console.error(e.getMessage());
        }
        try {
            return new JbossLogFactory();
        } catch (NoClassDefFoundError e) {
            Console.error(e.getMessage());
        }

        final URL url = ResourceUtils.getResource("logging.properties");
        return (null != url) ? new JdkLogFactory() : new ConsoleLogFactory();
    }

    /**
     * @return 当前使用的日志工厂
     */
    public static LogFactory getCurrentLogFactory() {
        return GlobalFactory.get();
    }

    /**
     * 自定义日志实现
     *
     * @param logFactoryClass 日志工厂类
     * @return 自定义的日志工厂类
     * @see Slf4jLogFactory
     * @see Log4j2LogFactory
     * @see Log4jLogFactory
     * @see ApacheCommonsLogFactory
     * @see TinyLogFactory
     * @see JbossLogFactory
     * @see ConsoleLogFactory
     * @see JdkLogFactory
     */
    public static LogFactory setCurrentLogFactory(Class<? extends LogFactory> logFactoryClass) {
        return GlobalFactory.set(logFactoryClass);
    }

    /**
     * 自定义日志实现
     *
     * @param logFactory 日志工厂类对象
     * @return 自定义的日志工厂类
     * @see Slf4jLogFactory
     * @see Log4j2LogFactory
     * @see Log4jLogFactory
     * @see ApacheCommonsLogFactory
     * @see TinyLogFactory
     * @see JbossLogFactory
     * @see ConsoleLogFactory
     * @see JdkLogFactory
     */
    public static LogFactory setCurrentLogFactory(LogFactory logFactory) {
        return GlobalFactory.set(logFactory);
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
        return get(CallerUtils.getCallers());
    }

    /**
     * 获取日志框架名，用于打印当前所用日志框架
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
     * 此方法仅用于检查所提供的日志相关类是否存在，当传入的日志类类不存在时抛出ClassNotFoundException
     * 此方法的作用是在detectLogFactory方法自动检测所用日志时，如果实现类不存在，调用此方法会自动抛出异常，从而切换到下一种日志的检测。
     *
     * @param logClassName 日志实现相关类
     */
    protected void checkLogExist(Class<?> logClassName) {

    }

}
