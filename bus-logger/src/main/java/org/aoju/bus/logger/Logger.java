/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2023 aoju.org and other contributors.                      *
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
package org.aoju.bus.logger;

import org.aoju.bus.core.toolkit.CallerKit;
import org.aoju.bus.core.toolkit.StringKit;
import org.aoju.bus.logger.level.Level;

/**
 * 静态日志类,用于在不引入日志对象的情况下打印日志
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public final class Logger {

    private static final String FQCN = Logger.class.getName();

    private Logger() {

    }

    /**
     * Trace等级日志,小于debug
     * 由于动态获取Log,效率较低,建议在非频繁调用的情况下使用！！
     *
     * @param format    格式文本,{} 代表变量
     * @param arguments 变量对应的参数
     */
    public static void trace(String format, Object... arguments) {
        trace(LogFactory.get(CallerKit.getCallers()), format, arguments);
    }

    /**
     * Trace等级日志,小于Debug
     *
     * @param log       日志对象
     * @param format    格式文本,{} 代表变量
     * @param arguments 变量对应的参数
     */
    public static void trace(Log log, String format, Object... arguments) {
        log.trace(FQCN, null, format, arguments);
    }

    /**
     * Debug等级日志,小于Info
     * 由于动态获取Log,效率较低,建议在非频繁调用的情况下使用！！
     *
     * @param format    格式文本,{} 代表变量
     * @param arguments 变量对应的参数
     */
    public static void debug(String format, Object... arguments) {
        debug(LogFactory.get(CallerKit.getCallers()), format, arguments);
    }

    /**
     * Debug等级日志,小于Info
     *
     * @param log       日志对象
     * @param format    格式文本,{} 代表变量
     * @param arguments 变量对应的参数
     */
    public static void debug(Log log, String format, Object... arguments) {
        log.debug(FQCN, null, format, arguments);
    }

    /**
     * Info等级日志,小于Warn
     * 由于动态获取Log,效率较低,建议在非频繁调用的情况下使用！！
     *
     * @param format    格式文本,{} 代表变量
     * @param arguments 变量对应的参数
     */
    public static void info(String format, Object... arguments) {
        info(LogFactory.get(CallerKit.getCallers()), format, arguments);
    }

    /**
     * Info等级日志,小于Warn
     *
     * @param log       日志对象
     * @param format    格式文本,{} 代表变量
     * @param arguments 变量对应的参数
     */
    public static void info(Log log, String format, Object... arguments) {
        log.info(FQCN, null, format, arguments);
    }

    /**
     * Warn等级日志,小于Error
     * 由于动态获取Log,效率较低,建议在非频繁调用的情况下使用！！
     *
     * @param format    格式文本,{} 代表变量
     * @param arguments 变量对应的参数
     */
    public static void warn(String format, Object... arguments) {
        warn(LogFactory.get(CallerKit.getCallers()), format, arguments);
    }

    /**
     * Warn等级日志,小于Error
     * 由于动态获取Log,效率较低,建议在非频繁调用的情况下使用！！
     *
     * @param e         需在日志中堆栈打印的异常
     * @param format    格式文本,{} 代表变量
     * @param arguments 变量对应的参数
     */
    public static void warn(Throwable e, String format, Object... arguments) {
        warn(LogFactory.get(CallerKit.getCallers()), e, StringKit.format(format, arguments));
    }

    /**
     * Warn等级日志,小于Error
     *
     * @param log       日志对象
     * @param format    格式文本,{} 代表变量
     * @param arguments 变量对应的参数
     */
    public static void warn(Log log, String format, Object... arguments) {
        warn(log, null, format, arguments);
    }

    /**
     * Warn等级日志,小于Error
     *
     * @param log       日志对象
     * @param e         需在日志中堆栈打印的异常
     * @param format    格式文本,{} 代表变量
     * @param arguments 变量对应的参数
     */
    public static void warn(Log log, Throwable e, String format, Object... arguments) {
        log.warn(FQCN, e, format, arguments);
    }

    /**
     * Error等级日志
     * 由于动态获取Log,效率较低,建议在非频繁调用的情况下使用！！
     *
     * @param e 需在日志中堆栈打印的异常
     */
    public static void error(Throwable e) {
        error(LogFactory.get(CallerKit.getCallers()), e);
    }

    /**
     * Error等级日志
     * 由于动态获取Log,效率较低,建议在非频繁调用的情况下使用！！
     *
     * @param format    格式文本,{} 代表变量
     * @param arguments 变量对应的参数
     */
    public static void error(String format, Object... arguments) {
        error(LogFactory.get(CallerKit.getCallers()), format, arguments);
    }

    /**
     * Error等级日志
     * 由于动态获取Log,效率较低,建议在非频繁调用的情况下使用！！
     *
     * @param e         需在日志中堆栈打印的异常
     * @param format    格式文本,{} 代表变量
     * @param arguments 变量对应的参数
     */
    public static void error(Throwable e, String format, Object... arguments) {
        error(LogFactory.get(CallerKit.getCallers()), e, format, arguments);
    }

    /**
     * Error等级日志
     *
     * @param log 日志对象
     * @param e   需在日志中堆栈打印的异常
     */
    public static void error(Log log, Throwable e) {
        error(log, e, e.getMessage());
    }

    /**
     * Error等级日志
     *
     * @param log       日志对象
     * @param format    格式文本,{} 代表变量
     * @param arguments 变量对应的参数
     */
    public static void error(Log log, String format, Object... arguments) {
        error(log, null, format, arguments);
    }

    /**
     * Error等级日志
     *
     * @param log       日志对象
     * @param e         需在日志中堆栈打印的异常
     * @param format    格式文本,{} 代表变量
     * @param arguments 变量对应的参数
     */
    public static void error(Log log, Throwable e, String format, Object... arguments) {
        log.error(FQCN, e, format, arguments);
    }

    /**
     * 打印日志
     *
     * @param level     日志级别
     * @param t         需在日志中堆栈打印的异常
     * @param format    格式文本,{} 代表变量
     * @param arguments 变量对应的参数
     */
    public static void log(Level level, Throwable t, String format, Object... arguments) {
        LogFactory.get(CallerKit.getCallers()).log(FQCN, level, t, format, arguments);
    }

    /**
     * 获得Log
     *
     * @param clazz 日志发出的类
     * @return Log
     */
    public static Log get(Class<?> clazz) {
        return LogFactory.get(clazz);
    }

    /**
     * 获得Log
     *
     * @param name 自定义的日志发出者名称
     * @return Log
     */
    public static Log get(String name) {
        return LogFactory.get(name);
    }

    /**
     * @return 获得日志, 自动判定日志发出者
     */
    public static Log get() {
        return get(CallerKit.getCallers());
    }

    /**
     * Trace 等级日志否开启
     *
     * @return the true/false
     */
    public static boolean isTrace() {
        return get().isTrace();
    }

    /**
     * Debug 等级日志否开启
     *
     * @return the true/false
     */
    public static boolean isDebug() {
        return get().isDebug();
    }

    /**
     * Info 等级日志否开启
     *
     * @return the true/false
     */
    public static boolean isInfo() {
        return get().isInfo();
    }

    /**
     * Warn 等级日志否开启
     *
     * @return the true/false
     */
    public static boolean isWarn() {
        return get().isWarn();
    }

    /**
     * Error 等级日志否开启
     *
     * @return the true/false
     */
    public static boolean isError() {
        return get().isError();
    }

}
