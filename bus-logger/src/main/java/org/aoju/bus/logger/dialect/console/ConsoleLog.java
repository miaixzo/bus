/*
 * The MIT License
 *
 * Copyright (c) 2017, aoju.org All rights reserved.
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
package org.aoju.bus.logger.dialect.console;

import org.aoju.bus.core.lang.Assert;
import org.aoju.bus.core.lang.Console;
import org.aoju.bus.core.lang.Dict;
import org.aoju.bus.core.utils.DateUtils;
import org.aoju.bus.core.utils.StringUtils;
import org.aoju.bus.logger.AbstractLog;
import org.aoju.bus.logger.level.Level;

/**
 * 打印日志
 *
 * @author Kimi Liu
 * @version 3.0.5
 * @since JDK 1.8
 */
public class ConsoleLog extends AbstractLog {

    private static final long serialVersionUID = -6843151523380063975L;

    private static String logFormat = "[{date}] [{level}] {name}: {msg}";
    private static Level level = Level.DEBUG;

    private String name;

    /**
     * 构造
     *
     * @param clazz 类
     */
    public ConsoleLog(Class<?> clazz) {
        this.name = clazz.getName();
    }

    /**
     * 构造
     *
     * @param name 类名
     */
    public ConsoleLog(String name) {
        this.name = name;
    }

    /**
     * 设置自定义的日志显示级别
     *
     * @param customLevel 自定义级别
     * @since 4.1.10
     */
    public static void setLevel(Level customLevel) {
        Assert.notNull(customLevel);
        level = customLevel;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public boolean isTraceEnabled() {
        return level.compareTo(Level.TRACE) <= 0;
    }

    @Override
    public void trace(String format, Object... arguments) {
        log(Level.TRACE, format, arguments);
    }

    @Override
    public void trace(Throwable t, String format, Object... arguments) {
        log(Level.TRACE, t, format, arguments);
    }

    @Override
    public boolean isDebugEnabled() {
        return level.compareTo(Level.DEBUG) <= 0;
    }

    @Override
    public void debug(String format, Object... arguments) {
        log(Level.DEBUG, format, arguments);
    }

    @Override
    public void debug(Throwable t, String format, Object... arguments) {
        log(Level.DEBUG, t, format, arguments);
    }

    @Override
    public boolean isInfoEnabled() {
        return level.compareTo(Level.INFO) <= 0;
    }

    @Override
    public void info(String format, Object... arguments) {
        log(Level.INFO, format, arguments);
    }

    @Override
    public void info(Throwable t, String format, Object... arguments) {
        log(Level.INFO, t, format, arguments);
    }

    @Override
    public boolean isWarnEnabled() {
        return level.compareTo(Level.WARN) <= 0;
    }

    @Override
    public void warn(String format, Object... arguments) {
        log(Level.WARN, format, arguments);
    }

    @Override
    public void warn(Throwable t, String format, Object... arguments) {
        log(Level.WARN, t, format, arguments);
    }

    @Override
    public boolean isErrorEnabled() {
        return level.compareTo(Level.ERROR) <= 0;
    }

    @Override
    public void error(String format, Object... arguments) {
        log(Level.ERROR, format, arguments);
    }

    @Override
    public void error(Throwable t, String format, Object... arguments) {
        log(Level.ERROR, t, format, arguments);
    }

    @Override
    public void log(Level level, String format, Object... arguments) {
        this.log(level, null, format, arguments);
    }

    @Override
    public void log(Level level, Throwable t, String format, Object... arguments) {
        if (false == isEnabled(level)) {
            return;
        }

        final Dict dict = Dict.create()
                .set("date", DateUtils.now())
                .set("level", level.toString())
                .set("name", this.name)
                .set("msg", StringUtils.format(format, arguments));

        final String logMsg = StringUtils.format(logFormat, dict);

        //WARN以上级别打印至System.err
        if (level.ordinal() >= Level.WARN.ordinal()) {
            Console.error(t, logMsg);
        } else {
            Console.log(t, logMsg);
        }

    }
}
