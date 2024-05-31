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

import org.miaixz.bus.core.xyz.CallerKit;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * 静态日志类，用于在不引入日志对象的情况下打印日志
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Logger {

    private static final String FQCN = Logger.class.getName();

    /**
     * 默认构造
     */
    public Logger() {

    }

    /**
     * Trace等级日志，小于debug
     * 由于动态获取Log，效率较低，建议在非频繁调用的情况下使用
     *
     * @param format 格式文本，{} 代表变量
     * @param args   变量对应的参数
     */
    public static void trace(final String format, final Object... args) {
        trace(get(), format, args);
    }

    /**
     * Trace等级日志，小于Debug
     *
     * @param supplier 日志对象
     * @param format   格式文本，{} 代表变量
     * @param args     变量对应的参数
     */
    public static void trace(final Supplier supplier, final String format, final Object... args) {
        supplier.trace(FQCN, null, format, args);
    }

    /**
     * Debug等级日志，小于Info
     * 由于动态获取Log，效率较低，建议在非频繁调用的情况下使用
     *
     * @param format 格式文本，{} 代表变量
     * @param args   变量对应的参数
     */
    public static void debug(final String format, final Object... args) {
        debug(get(), format, args);
    }

    /**
     * Debug等级日志，小于Info
     *
     * @param supplier 日志对象
     * @param format   格式文本，{} 代表变量
     * @param args     变量对应的参数
     */
    public static void debug(final Supplier supplier, final String format, final Object... args) {
        supplier.debug(FQCN, null, format, args);
    }

    /**
     * Info等级日志，小于Warn
     * 由于动态获取Log，效率较低，建议在非频繁调用的情况下使用
     *
     * @param format 格式文本，{} 代表变量
     * @param args   变量对应的参数
     */
    public static void info(final String format, final Object... args) {
        info(get(), format, args);
    }

    /**
     * Info等级日志，小于Warn
     *
     * @param supplier 日志对象
     * @param format   格式文本，{} 代表变量
     * @param args     变量对应的参数
     */
    public static void info(final Supplier supplier, final String format, final Object... args) {
        supplier.info(FQCN, null, format, args);
    }

    /**
     * Warn等级日志，小于Error
     * 由于动态获取Log，效率较低，建议在非频繁调用的情况下使用
     *
     * @param format 格式文本，{} 代表变量
     * @param args   变量对应的参数
     */
    public static void warn(final String format, final Object... args) {
        warn(get(), format, args);
    }

    /**
     * Warn等级日志，小于Error
     * 由于动态获取Log，效率较低，建议在非频繁调用的情况下使用
     *
     * @param e      需在日志中堆栈打印的异常
     * @param format 格式文本，{} 代表变量
     * @param args   变量对应的参数
     */
    public static void warn(final Throwable e, final String format, final Object... args) {
        warn(get(), e, StringKit.format(format, args));
    }

    /**
     * Warn等级日志，小于Error
     *
     * @param supplier 日志对象
     * @param format   格式文本，{} 代表变量
     * @param args     变量对应的参数
     */
    public static void warn(final Supplier supplier, final String format, final Object... args) {
        warn(supplier, null, format, args);
    }

    /**
     * Warn等级日志，小于Error
     *
     * @param supplier 日志对象
     * @param e        需在日志中堆栈打印的异常
     * @param format   格式文本，{} 代表变量
     * @param args     变量对应的参数
     */
    public static void warn(final Supplier supplier, final Throwable e, final String format, final Object... args) {
        supplier.warn(FQCN, e, format, args);
    }

    /**
     * Error等级日志
     * 由于动态获取Log，效率较低，建议在非频繁调用的情况下使用
     *
     * @param e 需在日志中堆栈打印的异常
     */
    public static void error(final Throwable e) {
        error(get(), e);
    }

    /**
     * Error等级日志
     * 由于动态获取Log，效率较低，建议在非频繁调用的情况下使用
     *
     * @param format 格式文本，{} 代表变量
     * @param args   变量对应的参数
     */
    public static void error(final String format, final Object... args) {
        error(get(), format, args);
    }

    /**
     * Error等级日志
     * 由于动态获取Log，效率较低，建议在非频繁调用的情况下使用
     *
     * @param e      需在日志中堆栈打印的异常
     * @param format 格式文本，{} 代表变量
     * @param args   变量对应的参数
     */
    public static void error(final Throwable e, final String format, final Object... args) {
        error(get(), e, format, args);
    }

    /**
     * Error等级日志
     *
     * @param supplier 日志对象
     * @param e        需在日志中堆栈打印的异常
     */
    public static void error(final Supplier supplier, final Throwable e) {
        error(supplier, e, e.getMessage());
    }

    /**
     * Error等级日志
     *
     * @param supplier 日志对象
     * @param format   格式文本，{} 代表变量
     * @param args     变量对应的参数
     */
    public static void error(final Supplier supplier, final String format, final Object... args) {
        error(supplier, null, format, args);
    }

    /**
     * Error等级日志
     *
     * @param supplier 日志对象
     * @param e        需在日志中堆栈打印的异常
     * @param format   格式文本，{} 代表变量
     * @param args     变量对应的参数
     */
    public static void error(final Supplier supplier, final Throwable e, final String format, final Object... args) {
        supplier.error(FQCN, e, format, args);
    }

    /**
     * 打印日志
     *
     * @param level  日志级别
     * @param t      需在日志中堆栈打印的异常
     * @param format 格式文本，{} 代表变量
     * @param args   变量对应的参数
     */
    public static void log(final Level level, final Throwable t, final String format, final Object... args) {
        get().log(FQCN, level, t, format, args);
    }

    /**
     * 获得日志, 自动判定日志发出者
     *
     * @return the supplier
     */
    public static Supplier get() {
        return get(CallerKit.getCallers());
    }

    /**
     * 获得日志
     *
     * @param clazz 日志发出的类
     * @return the supplier
     */
    public static Supplier get(Class<?> clazz) {
        return Registry.get(clazz);
    }

    /**
     * 获得日志
     *
     * @param name 日志发出者名称
     * @return the supplier
     */
    public static Supplier get(String name) {
        return Registry.get(name);
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
