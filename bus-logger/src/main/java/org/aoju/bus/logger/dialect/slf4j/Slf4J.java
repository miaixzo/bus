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
package org.aoju.bus.logger.dialect.slf4j;

import org.aoju.bus.core.consts.Normal;
import org.aoju.bus.core.utils.StringUtils;
import org.aoju.bus.logger.AbstractAware;
import org.aoju.bus.logger.level.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.spi.LocationAwareLogger;

/**
 * <a href="http://www.slf4j.org/">SLF4J</a> log.<br>
 * 同样无缝支持 <a href="http://logback.qos.ch/">LogBack</a>
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class Slf4J extends AbstractAware {

    private static final long serialVersionUID = -6843151523380063975L;
    private static final String FQCN = Slf4J.class.getName();

    private final transient Logger logger;

    public Slf4J(Logger logger) {
        this.logger = logger;
    }

    public Slf4J(Class<?> clazz) {
        this(null == clazz ? LoggerFactory.getLogger(Normal.EMPTY) : LoggerFactory.getLogger(clazz));
    }

    public Slf4J(String name) {
        this(LoggerFactory.getLogger(name));
    }

    @Override
    public String getName() {
        return logger.getName();
    }

    @Override
    public boolean isTraceEnabled() {
        return logger.isTraceEnabled();
    }

    @Override
    public void trace(String format, Object... arguments) {
        if (false == locationAwareLog(LocationAwareLogger.TRACE_INT, format, arguments)) {
            logger.trace(format, arguments);
        }
    }

    @Override
    public void trace(Throwable t, String format, Object... arguments) {
        if (false == locationAwareLog(LocationAwareLogger.TRACE_INT, t, format, arguments)) {
            logger.trace(StringUtils.format(format, arguments), t);
        }
    }

    @Override
    public boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }

    @Override
    public void debug(String format, Object... arguments) {
        if (false == locationAwareLog(LocationAwareLogger.DEBUG_INT, format, arguments)) {
            logger.debug(format, arguments);
        }
    }

    @Override
    public void debug(Throwable t, String format, Object... arguments) {
        if (false == locationAwareLog(LocationAwareLogger.DEBUG_INT, t, format, arguments)) {
            logger.debug(StringUtils.format(format, arguments), t);
        }
    }

    @Override
    public boolean isInfoEnabled() {
        return logger.isInfoEnabled();
    }

    @Override
    public void info(String format, Object... arguments) {
        if (false == locationAwareLog(LocationAwareLogger.INFO_INT, format, arguments)) {
            logger.info(format, arguments);
        }
    }

    @Override
    public void info(Throwable t, String format, Object... arguments) {
        if (false == locationAwareLog(LocationAwareLogger.INFO_INT, t, format, arguments)) {
            logger.info(StringUtils.format(format, arguments), t);
        }
    }

    @Override
    public boolean isWarnEnabled() {
        return logger.isWarnEnabled();
    }

    @Override
    public void warn(String format, Object... arguments) {
        if (false == locationAwareLog(LocationAwareLogger.WARN_INT, format, arguments)) {
            logger.warn(format, arguments);
        }
    }

    @Override
    public void warn(Throwable t, String format, Object... arguments) {
        if (false == locationAwareLog(LocationAwareLogger.WARN_INT, t, format, arguments)) {
            logger.warn(StringUtils.format(format, arguments), t);
        }
    }

    @Override
    public boolean isErrorEnabled() {
        return logger.isErrorEnabled();
    }

    @Override
    public void error(String format, Object... arguments) {
        if (false == locationAwareLog(LocationAwareLogger.ERROR_INT, format, arguments)) {
            logger.error(format, arguments);
        }
    }

    @Override
    public void error(Throwable t, String format, Object... arguments) {
        if (false == locationAwareLog(LocationAwareLogger.ERROR_INT, t, format, arguments)) {
            logger.error(StringUtils.format(format, arguments), t);
        }
    }

    @Override
    public void log(Level level, String format, Object... arguments) {
        this.log(level, null, format, arguments);
    }

    @Override
    public void log(Level level, Throwable t, String format, Object... arguments) {
        this.log(FQCN, level, t, format, arguments);
    }

    @Override
    public void log(String fqcn, Level level, Throwable t, String format, Object... arguments) {
        int level_int;
        switch (level) {
            case TRACE:
                level_int = LocationAwareLogger.TRACE_INT;
                break;
            case DEBUG:
                level_int = LocationAwareLogger.DEBUG_INT;
                break;
            case INFO:
                level_int = LocationAwareLogger.INFO_INT;
                break;
            case WARN:
                level_int = LocationAwareLogger.WARN_INT;
                break;
            case ERROR:
                level_int = LocationAwareLogger.ERROR_INT;
                break;
            default:
                throw new Error(StringUtils.format("Can not identify level: {}", level));
        }
        this.locationAwareLog(fqcn, level_int, t, format, arguments);
    }

    /**
     * 打印日志<br>
     * 此方法用于兼容底层日志实现，通过传入当前包装类名，以解决打印日志中行号错误问题
     *
     * @param level_int   日志级别，使用LocationAwareLogger中的常量
     * @param msgTemplate 消息模板
     * @param arguments   参数
     * @return 是否支持 LocationAwareLogger对象，如果不支持需要日志方法调用被包装类的相应方法
     */
    private boolean locationAwareLog(int level_int, String msgTemplate, Object[] arguments) {
        return locationAwareLog(level_int, null, msgTemplate, arguments);
    }

    /**
     * 打印日志<br>
     * 此方法用于兼容底层日志实现，通过传入当前包装类名，以解决打印日志中行号错误问题
     *
     * @param level_int   日志级别，使用LocationAwareLogger中的常量
     * @param msgTemplate 消息模板
     * @param arguments   参数
     * @param t           异常
     * @return 是否支持 LocationAwareLogger对象，如果不支持需要日志方法调用被包装类的相应方法
     */
    private boolean locationAwareLog(int level_int, Throwable t, String msgTemplate, Object[] arguments) {
        return locationAwareLog(FQCN, level_int, t, msgTemplate, arguments);
    }

    /**
     * 打印日志<br>
     * 此方法用于兼容底层日志实现，通过传入当前包装类名，以解决打印日志中行号错误问题
     *
     * @param fqcn        完全限定类名(Fully Qualified Class Name)，用于纠正定位错误行号
     * @param level_int   日志级别，使用LocationAwareLogger中的常量
     * @param t           异常
     * @param msgTemplate 消息模板
     * @param arguments   参数
     * @return 是否支持 LocationAwareLogger对象，如果不支持需要日志方法调用被包装类的相应方法
     */
    private boolean locationAwareLog(String fqcn, int level_int, Throwable t, String msgTemplate, Object[] arguments) {
        if (this.logger instanceof LocationAwareLogger) {
            ((LocationAwareLogger) this.logger).log(null, fqcn, level_int, StringUtils.format(msgTemplate, arguments), null, t);
            return true;
        } else {
            return false;
        }
    }

}
