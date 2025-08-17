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
package org.miaixz.bus.logger;

import org.miaixz.bus.core.xyz.CallerKit;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * 静态日志类，用于在不引入日志对象的情况下打印日志
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Logger {

    /**
     * 完全限定类名(Fully Qualified Class Name)，用于纠正定位错误行号
     */
    private static final String FQCN = Logger.class.getName();

    /**
     * 默认构造
     */
    public Logger() {

    }

    /**
     * Trace等级日志，小于debug 由于动态获取Log，效率较低，建议在非频繁调用的情况下使用！
     *
     * @param format 格式文本，{}代表变量
     * @param args   变量对应的参数
     */
    public static void trace(final String format, final Object... args) {
        trace(Registry.get(CallerKit.getCallers()), format, args);
    }

    /**
     * Trace等级日志，小于Debug
     *
     * @param provider 日志对象
     * @param format   格式文本，{}代表变量
     * @param args     变量对应的参数
     */
    public static void trace(final Provider provider, final String format, final Object... args) {
        provider.trace(FQCN, null, format, args);
    }

    /**
     * Debug等级日志，小于Info 由于动态获取Log，效率较低，建议在非频繁调用的情况下使用！
     *
     * @param format 格式文本，{}代表变量
     * @param args   变量对应的参数
     */
    public static void debug(final String format, final Object... args) {
        debug(Registry.get(CallerKit.getCallers()), format, args);
    }

    /**
     * Debug等级日志，小于Info
     *
     * @param provider 日志对象
     * @param format   格式文本，{}代表变量
     * @param args     变量对应的参数
     */
    public static void debug(final Provider provider, final String format, final Object... args) {
        provider.debug(FQCN, null, format, args);
    }

    /**
     * Info等级日志，小于Warn 由于动态获取Log，效率较低，建议在非频繁调用的情况下使用！
     *
     * @param format 格式文本，{}代表变量
     * @param args   变量对应的参数
     */
    public static void info(final String format, final Object... args) {
        info(Registry.get(CallerKit.getCallers()), format, args);
    }

    /**
     * Info等级日志，小于Warn
     *
     * @param provider 日志对象
     * @param format   格式文本，{}代表变量
     * @param args     变量对应的参数
     */
    public static void info(final Provider provider, final String format, final Object... args) {
        provider.info(FQCN, null, format, args);
    }

    /**
     * Warn等级日志，小于Error 由于动态获取Log，效率较低，建议在非频繁调用的情况下使用！
     *
     * @param format 格式文本，{} 代表变量
     * @param args   变量对应的参数
     */
    public static void warn(final String format, final Object... args) {
        warn(Registry.get(CallerKit.getCallers()), format, args);
    }

    /**
     * Warn等级日志，小于Error 由于动态获取Log，效率较低，建议在非频繁调用的情况下使用！
     *
     * @param e      需在日志中堆栈打印的异常
     * @param format 格式文本，{}代表变量
     * @param args   变量对应的参数
     */
    public static void warn(final Throwable e, final String format, final Object... args) {
        warn(Registry.get(CallerKit.getCallers()), e, StringKit.format(format, args));
    }

    /**
     * Warn等级日志，小于Error
     *
     * @param provider 日志对象
     * @param format   格式文本，{}代表变量
     * @param args     变量对应的参数
     */
    public static void warn(final Provider provider, final String format, final Object... args) {
        warn(provider, null, format, args);
    }

    /**
     * Warn等级日志，小于Error
     *
     * @param provider 日志对象
     * @param e        需在日志中堆栈打印的异常
     * @param format   格式文本，{}代表变量
     * @param args     变量对应的参数
     */
    public static void warn(final Provider provider, final Throwable e, final String format, final Object... args) {
        provider.warn(FQCN, e, format, args);
    }

    /**
     * Error等级日志 由于动态获取Log，效率较低，建议在非频繁调用的情况下使用！
     *
     * @param e 需在日志中堆栈打印的异常
     */
    public static void error(final Throwable e) {
        error(Registry.get(CallerKit.getCallers()), e);
    }

    /**
     * Error等级日志 由于动态获取Log，效率较低，建议在非频繁调用的情况下使用！
     *
     * @param format 格式文本，{} 代表变量
     * @param args   变量对应的参数
     */
    public static void error(final String format, final Object... args) {
        error(Registry.get(CallerKit.getCallers()), format, args);
    }

    /**
     * Error等级日志 由于动态获取Log，效率较低，建议在非频繁调用的情况下使用！
     *
     * @param e      需在日志中堆栈打印的异常
     * @param format 格式文本，{}代表变量
     * @param args   变量对应的参数
     */
    public static void error(final Throwable e, final String format, final Object... args) {
        error(Registry.get(CallerKit.getCallers()), e, format, args);
    }

    /**
     * Error等级日志
     *
     * @param provider 日志对象
     * @param e        需在日志中堆栈打印的异常
     */
    public static void error(final Provider provider, final Throwable e) {
        error(provider, e, e.getMessage());
    }

    /**
     * Error等级日志
     *
     * @param provider 日志对象
     * @param format   格式文本，{}代表变量
     * @param args     变量对应的参数
     */
    public static void error(final Provider provider, final String format, final Object... args) {
        error(provider, null, format, args);
    }

    /**
     * Error等级日志
     *
     * @param provider 日志对象
     * @param e        需在日志中堆栈打印的异常
     * @param format   格式文本，{}代表变量
     * @param args     变量对应的参数
     */
    public static void error(final Provider provider, final Throwable e, final String format, final Object... args) {
        provider.error(FQCN, e, format, args);
    }

    /**
     * 打印日志
     *
     * @param level  日志级别
     * @param t      需在日志中堆栈打印的异常
     * @param format 格式文本，{}代表变量
     * @param args   变量对应的参数
     */
    public static void log(final Level level, final Throwable t, final String format, final Object... args) {
        Registry.get(CallerKit.getCallers()).log(FQCN, level, t, format, args);
    }

    /**
     * 获取当前日志级别
     *
     * @return 当前日志级别，如果无法获取则返回 Level.OFF
     */
    public static Level getLevel() {
        Provider provider = Registry.get(CallerKit.getCallers());
        return provider != null ? provider.getLevel() : Level.OFF;
    }

    /**
     * 设置日志级别
     *
     * @param level 日志级别
     * @throws UnsupportedOperationException 如果底层日志框架不支持动态级别设置
     */
    public static void setLevel(Level level) {
        Provider provider = Registry.get(CallerKit.getCallers());
        if (provider != null) {
            provider.setLevel(level);
        }
    }

    /**
     * 获取日志实现
     *
     * @return 日志实现类，例如org.jboss.logging.Logger
     */
    public static Class<?> getFactory() {
        Factory factory = Holder.getFactory();
        if (factory == null) {
            return null;
        }

        // 如果无法直接获取，尝试通过工厂名称推断
        String factoryName = factory.getName();
        if (factoryName.contains("org.jboss.logging.Logger")) {
            try {
                return Class.forName("org.jboss.logging.Logger");
            } catch (ClassNotFoundException ex) {
                // 忽略异常，继续尝试其他方式
            }
        } else if (factoryName.contains("org.slf4j.Logger")) {
            try {
                return Class.forName("org.slf4j.Logger");
            } catch (ClassNotFoundException ex) {
                // 忽略异常，继续尝试其他方式
            }
        } else if (factoryName.contains("org.apache.logging.log4j.Logger")) {
            try {
                return Class.forName("org.apache.logging.log4j.Logger");
            } catch (ClassNotFoundException ex) {
                // 忽略异常，继续尝试其他方式
            }
        } else if (factoryName.contains("java.util.logging.Logger")) {
            try {
                return Class.forName("java.util.logging.Logger");
            } catch (ClassNotFoundException ex) {
                // 忽略异常，继续尝试其他方式
            }
        } else if (factoryName.contains("org.apache.commons.logging.Log")) {
            try {
                return Class.forName("org.apache.commons.logging.Log");
            } catch (ClassNotFoundException ex) {
                // 忽略异常，继续尝试其他方式
            }
        } else if (factoryName.contains("org.tinylog.Logger")) {
            try {
                return Class.forName("org.tinylog.Logger");
            } catch (ClassNotFoundException ex) {
                // 忽略异常，继续尝试其他方式
            }
        }

        return null;
    }

    /**
     * 获得日志对象
     *
     * @return 当前日志提供者，可能为 null
     */
    public static Provider getProvider() {
        return Registry.get(CallerKit.getCallers());
    }

    /**
     * 检查指定日志级别是否启用
     *
     * @param level 日志级别
     * @return 是否启用
     */
    public static boolean isEnabled(Level level) {
        return getProvider().isEnabled(level);
    }

    /**
     * Trace 等级日志否开启
     *
     * @return the true/false
     */
    public static boolean isTraceEnabled() {
        return getProvider().isTraceEnabled();
    }

    /**
     * Debug 等级日志否开启
     *
     * @return the true/false
     */
    public static boolean isDebugEnabled() {
        return getProvider().isDebugEnabled();
    }

    /**
     * Info 等级日志否开启
     *
     * @return the true/false
     */
    public static boolean isInfoEnabled() {
        return getProvider().isInfoEnabled();
    }

    /**
     * Warn 等级日志否开启
     *
     * @return the true/false
     */
    public static boolean isWarnEnabled() {
        return getProvider().isWarnEnabled();
    }

    /**
     * Error 等级日志否开启
     *
     * @return the true/false
     */
    public static boolean isErrorEnabled() {
        return getProvider().isErrorEnabled();
    }

}
