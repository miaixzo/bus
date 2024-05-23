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
package org.miaixz.bus.logger.metric.slf4j;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.logger.Level;
import org.miaixz.bus.logger.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.spi.LocationAwareLogger;

/**
 * slf4j and logback
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Slf4jProvider extends Provider {

    private static final long serialVersionUID = -1L;

    /**
     * 日志门面
     */
    private final transient Logger logger;
    /**
     * 是否为 LocationAwareLogger ，用于判断是否可以传递FQCN
     */
    private final boolean isLocationAwareLogger;

    /**
     * 构造
     *
     * @param logger 日志对象
     */
    public Slf4jProvider(final Logger logger) {
        this.logger = logger;
        this.isLocationAwareLogger = (logger instanceof LocationAwareLogger);
    }

    /**
     * 构造
     *
     * @param clazz 日志实现类
     */
    public Slf4jProvider(final Class<?> clazz) {
        this(null == clazz ? LoggerFactory.getLogger(Normal.EMPTY) : LoggerFactory.getLogger(clazz));
    }

    /**
     * 构造
     *
     * @param name 日志实现类名
     */
    public Slf4jProvider(final String name) {
        this(LoggerFactory.getLogger(name));
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
        if (isTrace()) {
            if (this.isLocationAwareLogger) {
                log((LocationAwareLogger) this.logger, fqcn, LocationAwareLogger.TRACE_INT, t, format, args);
            } else {
                logger.trace(StringKit.format(format, args), t);
            }
        }
    }

    @Override
    public boolean isDebug() {
        return logger.isDebugEnabled();
    }

    @Override
    public void debug(final String fqcn, final Throwable t, final String format, final Object... args) {
        if (isDebug()) {
            if (this.isLocationAwareLogger) {
                log((LocationAwareLogger) this.logger, fqcn, LocationAwareLogger.DEBUG_INT, t, format, args);
            } else {
                logger.debug(StringKit.format(format, args), t);
            }
        }
    }

    @Override
    public boolean isInfo() {
        return logger.isInfoEnabled();
    }

    @Override
    public void info(final String fqcn, final Throwable t, final String format, final Object... args) {
        if (isInfo()) {
            if (this.isLocationAwareLogger) {
                log((LocationAwareLogger) this.logger, fqcn, LocationAwareLogger.INFO_INT, t, format, args);
            } else {
                logger.info(StringKit.format(format, args), t);
            }
        }
    }

    @Override
    public boolean isWarn() {
        return logger.isWarnEnabled();
    }

    @Override
    public void warn(final String fqcn, final Throwable t, final String format, final Object... args) {
        if (isWarn()) {
            if (this.isLocationAwareLogger) {
                log((LocationAwareLogger) this.logger, fqcn, LocationAwareLogger.WARN_INT, t, format, args);
            } else {
                logger.warn(StringKit.format(format, args), t);
            }
        }
    }

    @Override
    public boolean isError() {
        return logger.isErrorEnabled();
    }

    @Override
    public void error(final String fqcn, final Throwable t, final String format, final Object... args) {
        if (isError()) {
            if (this.isLocationAwareLogger) {
                log((LocationAwareLogger) this.logger, fqcn, LocationAwareLogger.ERROR_INT, t, format, args);
            } else {
                logger.error(StringKit.format(format, args), t);
            }
        }
    }

    @Override
    public void log(final String fqcn, final Level level, final Throwable t, final String format, final Object... args) {
        switch (level) {
            case TRACE:
                trace(fqcn, t, format, args);
                break;
            case DEBUG:
                debug(fqcn, t, format, args);
                break;
            case INFO:
                info(fqcn, t, format, args);
                break;
            case WARN:
                warn(fqcn, t, format, args);
                break;
            case ERROR:
                error(fqcn, t, format, args);
                break;
            default:
                throw new Error(StringKit.format("Can not identify level: {}", level));
        }
    }

    /**
     * 打印日志
     * 此方法用于兼容底层日志实现，通过传入当前包装类名，以解决打印日志中行号错误问题
     *
     * @param logger {@link LocationAwareLogger} 实现
     * @param fqcn   完全限定类名(Fully Qualified Class Name)，用于纠正定位错误行号
     * @param level  日志级别，使用LocationAwareLogger中的常量
     * @param t      异常
     * @param format 消息格式
     * @param args   参数
     */
    private void log(final LocationAwareLogger logger, final String fqcn, final int level, final Throwable t, final String format, final Object[] args) {
        logger.log(null, fqcn, level, StringKit.format(format, args), null, t);
    }

}
