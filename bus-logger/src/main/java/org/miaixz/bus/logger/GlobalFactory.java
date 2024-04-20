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

import org.miaixz.bus.logger.dialect.commons.ApacheCommonsLogFactory;
import org.miaixz.bus.logger.dialect.console.ConsoleLogFactory;
import org.miaixz.bus.logger.dialect.jdk.JdkLogFactory;
import org.miaixz.bus.logger.dialect.log4j2.Log4j2LogFactory;
import org.miaixz.bus.logger.dialect.slf4j.Slf4jLogFactory;

/**
 * 全局日志工厂类
 * 用于减少日志工厂创建,减少日志库探测
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class GlobalFactory {

    private static final Object lock = new Object();
    private static volatile LogFactory currentLogFactory;

    /**
     * 获取单例日志工厂类,如果不存在创建之
     *
     * @return 当前使用的日志工厂
     */
    public static LogFactory get() {
        if (null == currentLogFactory) {
            synchronized (lock) {
                if (null == currentLogFactory) {
                    currentLogFactory = LogFactory.of();
                }
            }
        }
        return currentLogFactory;
    }

    /**
     * 自定义日志实现
     *
     * @param logFactoryClass 日志工厂类
     * @return 自定义的日志工厂类
     * @see Slf4jLogFactory
     * @see Log4j2LogFactory
     * @see ApacheCommonsLogFactory
     * @see JdkLogFactory
     * @see ConsoleLogFactory
     */
    public static LogFactory set(Class<? extends LogFactory> logFactoryClass) {
        try {
            return set(logFactoryClass.getConstructor().newInstance());
        } catch (Exception e) {
            throw new IllegalArgumentException("Can not instance LogFactory class!", e);
        }
    }

    /**
     * 自定义日志实现
     *
     * @param logFactory 日志工厂类对象
     * @return 自定义的日志工厂类
     * @see Slf4jLogFactory
     * @see Log4j2LogFactory
     * @see ApacheCommonsLogFactory
     * @see JdkLogFactory
     * @see ConsoleLogFactory
     */
    public static LogFactory set(LogFactory logFactory) {
        logFactory.getLog(GlobalFactory.class).debug("Custom Use [{}] Logger.", logFactory.name);
        currentLogFactory = logFactory;
        return currentLogFactory;
    }

}
