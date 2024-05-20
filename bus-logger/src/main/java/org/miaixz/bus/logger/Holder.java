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

import org.miaixz.bus.logger.metric.commons.ApacheCommonsFactory;
import org.miaixz.bus.logger.metric.console.ConsoleFactory;
import org.miaixz.bus.logger.metric.jdk.JdkFactory;
import org.miaixz.bus.logger.metric.log4j2.Log4J2Factory;
import org.miaixz.bus.logger.metric.slf4j.Slf4JFactory;

/**
 * 全局日志提供者
 * 用于减少日志提供者创建,减少日志库探测
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Holder {

    private static final Object lock = new Object();
    private static volatile Factory currentFactory;

    /**
     * 获取单例日志提供者类,如果不存在创建之
     *
     * @return 当前使用的日志提供者
     */
    public static Factory get() {
        if (null == currentFactory) {
            synchronized (lock) {
                if (null == currentFactory) {
                    currentFactory = Factory.of();
                }
            }
        }
        return currentFactory;
    }

    /**
     * 自定义日志实现
     *
     * @param logFactoryClass 日志提供者类
     * @return 自定义的日志提供者类
     * @see Slf4JFactory
     * @see Log4J2Factory
     * @see ApacheCommonsFactory
     * @see JdkFactory
     * @see ConsoleFactory
     */
    public static Factory set(Class<? extends Factory> logFactoryClass) {
        try {
            return set(logFactoryClass.getConstructor().newInstance());
        } catch (Exception e) {
            throw new IllegalArgumentException("Can not instance LogFactory class!", e);
        }
    }

    /**
     * 自定义日志实现
     *
     * @param factory 日志提供者类对象
     * @return 自定义的日志提供者类
     * @see Slf4JFactory
     * @see Log4J2Factory
     * @see ApacheCommonsFactory
     * @see JdkFactory
     * @see ConsoleFactory
     */
    public static Factory set(Factory factory) {
        factory.getLog(Holder.class).debug("Custom Use [{}] Logger.", factory.name);
        currentFactory = factory;
        return currentFactory;
    }

}
