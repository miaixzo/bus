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
     * Trace 等级日志否开启
     *
     * @return the true/false
     */
    public static boolean isTraceEnabled() {
        return Registry.get(CallerKit.getCallers()).isTraceEnabled();
    }

    /**
     * Debug 等级日志否开启
     *
     * @return the true/false
     */
    public static boolean isDebugEnabled() {
        return Registry.get(CallerKit.getCallers()).isDebugEnabled();
    }

    /**
     * Info 等级日志否开启
     *
     * @return the true/false
     */
    public static boolean isInfoEnabled() {
        return Registry.get(CallerKit.getCallers()).isInfoEnabled();
    }

    /**
     * Warn 等级日志否开启
     *
     * @return the true/false
     */
    public static boolean isWarnEnabled() {
        return Registry.get(CallerKit.getCallers()).isWarnEnabled();
    }

    /**
     * Error 等级日志否开启
     *
     * @return the true/false
     */
    public static boolean isErrorEnabled() {
        return Registry.get(CallerKit.getCallers()).isErrorEnabled();
    }

}
