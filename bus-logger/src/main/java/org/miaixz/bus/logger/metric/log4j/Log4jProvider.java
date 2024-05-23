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
package org.miaixz.bus.logger.metric.log4j;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.spi.AbstractLogger;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.logger.Provider;

/**
 * log4j
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Log4jProvider extends Provider {

    private static final long serialVersionUID = -1L;

    /**
     * 日志门面
     */
    private final transient Logger logger;

    public Log4jProvider(final Logger logger) {
        this.logger = logger;
    }

    public Log4jProvider(final Class<?> clazz) {
        this(LogManager.getLogger(clazz));
    }

    public Log4jProvider(final String name) {
        this(LogManager.getLogger(name));
    }

    @Override
    public String getName() {
        return logger.getName();
    }

    @Override
    public boolean isTrace() {
        return logger.isTraceEnabled();
    }

    @Override
    public void trace(final String fqcn, final Throwable t, final String format, final Object... args) {
        log(fqcn, Level.TRACE, t, format, args);
    }

    @Override
    public boolean isDebug() {
        return logger.isDebugEnabled();
    }

    @Override
    public void debug(final String fqcn, final Throwable t, final String format, final Object... args) {
        log(fqcn, Level.DEBUG, t, format, args);
    }

    @Override
    public boolean isInfo() {
        return logger.isInfoEnabled();
    }

    @Override
    public void info(final String fqcn, final Throwable t, final String format, final Object... args) {
        log(fqcn, Level.INFO, t, format, args);
    }

    @Override
    public boolean isWarn() {
        return logger.isWarnEnabled();
    }

    @Override
    public void warn(final String fqcn, final Throwable t, final String format, final Object... args) {
        log(fqcn, Level.WARN, t, format, args);
    }

    @Override
    public boolean isError() {
        return logger.isErrorEnabled();
    }

    @Override
    public void error(final String fqcn, final Throwable t, final String format, final Object... args) {
        log(fqcn, Level.ERROR, t, format, args);
    }

    @Override
    public void log(final String fqcn, final org.miaixz.bus.logger.Level level, final Throwable t, final String format, final Object... args) {
        final Level log4j2Level;
        switch (level) {
            case TRACE:
                log4j2Level = Level.TRACE;
                break;
            case DEBUG:
                log4j2Level = Level.DEBUG;
                break;
            case INFO:
                log4j2Level = Level.INFO;
                break;
            case WARN:
                log4j2Level = Level.WARN;
                break;
            case ERROR:
                log4j2Level = Level.ERROR;
                break;
            default:
                throw new Error(StringKit.format("Can not identify level: {}", level));
        }
        log(fqcn, log4j2Level, t, format, args);
    }

    /**
     * 打印日志
     * 此方法用于兼容底层日志实现，通过传入当前包装类名，以解决打印日志中行号错误问题
     *
     * @param fqcn   完全限定类名(Fully Qualified Class Name)，用于纠正定位错误行号
     * @param level  日志级别，使用org.apache.logging.log4j.Level中的常量
     * @param t      异常
     * @param format 消息格式
     * @param args   参数
     */
    private void log(final String fqcn, final Level level, final Throwable t, final String format, final Object... args) {
        if (this.logger.isEnabled(level)) {
            if (this.logger instanceof AbstractLogger) {
                ((AbstractLogger) this.logger).logIfEnabled(fqcn, level, null, StringKit.format(format, args), t);
            } else {
                this.logger.log(level, StringKit.format(format, args), t);
            }
        }
    }

}
