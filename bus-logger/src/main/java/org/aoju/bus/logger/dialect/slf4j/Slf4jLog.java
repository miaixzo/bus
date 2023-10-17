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
package org.aoju.bus.logger.dialect.slf4j;

import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.core.toolkit.StringKit;
import org.aoju.bus.logger.AbstractAware;
import org.aoju.bus.logger.level.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.spi.LocationAwareLogger;

/**
 * SLF4J log.
 * 无缝支持 LogBack
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Slf4jLog extends AbstractAware {

    private final transient Logger logger;
    /**
     * 是否为 LocationAwareLogger ,用于判断是否可以传递FQCN
     */
    private final boolean isLocationAwareLogger;

    public Slf4jLog(Logger logger) {
        this.logger = logger;
        this.isLocationAwareLogger = (logger instanceof LocationAwareLogger);
    }

    public Slf4jLog(Class<?> clazz) {
        this(getSlf4jLogger(clazz));
    }

    public Slf4jLog(String name) {
        this(LoggerFactory.getLogger(name));
    }

    /**
     * 获取Slf4j Logger对象
     *
     * @param clazz 打印日志所在类,当为{@code null}时使用“null”表示
     * @return {@link Logger}
     */
    private static Logger getSlf4jLogger(Class<?> clazz) {
        return (null == clazz) ? LoggerFactory.getLogger(Normal.EMPTY) : LoggerFactory.getLogger(clazz);
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
    public void trace(String fqcn, Throwable t, String format, Object... arguments) {
        if (isTrace()) {
            if (this.isLocationAwareLogger) {
                locationAwareLog((LocationAwareLogger) this.logger, fqcn, LocationAwareLogger.TRACE_INT, t, format, arguments);
            } else {
                logger.trace(StringKit.format(format, arguments), t);
            }
        }
    }

    @Override
    public boolean isDebug() {
        return logger.isDebugEnabled();
    }

    @Override
    public void debug(String fqcn, Throwable t, String format, Object... arguments) {
        if (isDebug()) {
            if (this.isLocationAwareLogger) {
                locationAwareLog((LocationAwareLogger) this.logger, fqcn, LocationAwareLogger.DEBUG_INT, t, format, arguments);
            } else {
                logger.debug(StringKit.format(format, arguments), t);
            }
        }
    }

    @Override
    public boolean isInfo() {
        return logger.isInfoEnabled();
    }

    @Override
    public void info(String fqcn, Throwable t, String format, Object... arguments) {
        if (isInfo()) {
            if (this.isLocationAwareLogger) {
                locationAwareLog((LocationAwareLogger) this.logger, fqcn, LocationAwareLogger.INFO_INT, t, format, arguments);
            } else {
                logger.info(StringKit.format(format, arguments), t);
            }
        }
    }

    @Override
    public boolean isWarn() {
        return logger.isWarnEnabled();
    }

    @Override
    public void warn(String fqcn, Throwable t, String format, Object... arguments) {
        if (isWarn()) {
            if (this.isLocationAwareLogger) {
                locationAwareLog((LocationAwareLogger) this.logger, fqcn, LocationAwareLogger.WARN_INT, t, format, arguments);
            } else {
                logger.warn(StringKit.format(format, arguments), t);
            }
        }
    }

    @Override
    public boolean isError() {
        return logger.isErrorEnabled();
    }

    @Override
    public void error(String fqcn, Throwable t, String format, Object... arguments) {
        if (isError()) {
            if (this.isLocationAwareLogger) {
                locationAwareLog((LocationAwareLogger) this.logger, fqcn, LocationAwareLogger.ERROR_INT, t, format, arguments);
            } else {
                logger.error(StringKit.format(format, arguments), t);
            }
        }
    }

    @Override
    public void log(String fqcn, Level level, Throwable t, String format, Object... arguments) {
        switch (level) {
            case TRACE:
                trace(fqcn, t, format, arguments);
                break;
            case DEBUG:
                debug(fqcn, t, format, arguments);
                break;
            case INFO:
                info(fqcn, t, format, arguments);
                break;
            case WARN:
                warn(fqcn, t, format, arguments);
                break;
            case ERROR:
                error(fqcn, t, format, arguments);
                break;
            default:
                throw new Error(StringKit.format("Can not identify level: {}", level));
        }
    }

    /**
     * 打印日志
     * 此方法用于兼容底层日志实现,通过传入当前包装类名,以解决打印日志中行号错误问题
     *
     * @param logger      {@link LocationAwareLogger} 实现
     * @param fqcn        完全限定类名(Fully Qualified Class Name),用于纠正定位错误行号
     * @param level_int   日志级别,使用LocationAwareLogger中的常量
     * @param t           异常
     * @param msgTemplate 消息模板
     * @param arguments   参数
     */
    private void locationAwareLog(LocationAwareLogger logger, String fqcn, int level_int, Throwable t, String msgTemplate, Object[] arguments) {
        logger.log(null, fqcn, level_int, StringKit.format(msgTemplate, arguments), null, t);
    }

}
