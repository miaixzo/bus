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
package org.aoju.bus.logger.dialect.jdk;

import org.aoju.bus.core.utils.StringUtils;
import org.aoju.bus.logger.AbstractAware;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * <a href="http://java.sun.com/javase/6/docs/technotes/guides/logging/index.html">java.util.logging</a> log.
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class Jdk extends AbstractAware {

    private static final long serialVersionUID = -6843151523380063975L;

    /**
     * 本类的全限定类名
     */
    private static final String FQCN_SELF = Jdk.class.getName();

    private final transient Logger logger;

    public Jdk(Logger logger) {
        this.logger = logger;
    }

    public Jdk(Class<?> clazz) {
        this(clazz.getName());
    }

    public Jdk(String name) {
        this(Logger.getLogger(name));
    }

    /**
     * 传入调用日志类的信息
     *
     * @param caller 调用者全限定类名
     * @param record The record to update
     */
    private static void fillCallerData(String caller, LogRecord record) {
        StackTraceElement[] steArray = new Throwable().getStackTrace();

        int found = -1;
        String className;
        for (int i = 0; i < steArray.length; i++) {
            className = steArray[i].getClassName();
            if (className.equals(caller)) {
                found = i;
                break;
            }
        }

        if (found > -1 && found < steArray.length - 1) {
            StackTraceElement ste = steArray[found + 1];
            record.setSourceClassName(ste.getClassName());
            record.setSourceMethodName(ste.getMethodName());
        }
    }

    @Override
    public String getName() {
        return logger.getName();
    }

    @Override
    public boolean isTraceEnabled() {
        return logger.isLoggable(Level.FINEST);
    }

    @Override
    public void trace(String format, Object... arguments) {
        logIfEnabled(Level.FINEST, null, format, arguments);
    }

    @Override
    public void trace(Throwable t, String format, Object... arguments) {
        logIfEnabled(Level.FINEST, t, format, arguments);
    }

    @Override
    public boolean isDebugEnabled() {
        return logger.isLoggable(Level.FINE);
    }

    @Override
    public void debug(String format, Object... arguments) {
        logIfEnabled(Level.FINE, null, format, arguments);
    }

    @Override
    public void debug(Throwable t, String format, Object... arguments) {
        logIfEnabled(Level.FINE, t, format, arguments);
    }

    @Override
    public boolean isInfoEnabled() {
        return logger.isLoggable(Level.INFO);
    }

    @Override
    public void info(String format, Object... arguments) {
        logIfEnabled(Level.INFO, null, format, arguments);
    }

    @Override
    public void info(Throwable t, String format, Object... arguments) {
        logIfEnabled(Level.INFO, t, format, arguments);
    }

    @Override
    public boolean isWarnEnabled() {
        return logger.isLoggable(Level.WARNING);
    }

    @Override
    public void warn(String format, Object... arguments) {
        logIfEnabled(Level.WARNING, null, format, arguments);
    }

    @Override
    public void warn(Throwable t, String format, Object... arguments) {
        logIfEnabled(Level.WARNING, t, format, arguments);
    }

    @Override
    public boolean isErrorEnabled() {
        return logger.isLoggable(Level.SEVERE);
    }

    @Override
    public void error(String format, Object... arguments) {
        logIfEnabled(Level.SEVERE, null, format, arguments);
    }

    @Override
    public void error(Throwable t, String format, Object... arguments) {
        logIfEnabled(Level.SEVERE, t, format, arguments);
    }

    @Override
    public void log(org.aoju.bus.logger.level.Level level, String format, Object... arguments) {
        this.log(level, null, format, arguments);
    }

    @Override
    public void log(org.aoju.bus.logger.level.Level level, Throwable t, String format, Object... arguments) {
        this.log(FQCN_SELF, level, t, format, arguments);
    }

    @Override
    public void log(String fqcn, org.aoju.bus.logger.level.Level level, Throwable t, String format, Object... arguments) {
        Level jdkLevel;
        switch (level) {
            case TRACE:
                jdkLevel = Level.FINEST;
                break;
            case DEBUG:
                jdkLevel = Level.FINE;
                break;
            case INFO:
                jdkLevel = Level.INFO;
                break;
            case WARN:
                jdkLevel = Level.WARNING;
                break;
            case ERROR:
                jdkLevel = Level.SEVERE;
                break;
            default:
                throw new Error(StringUtils.format("Can not identify level: {}", level));
        }
        logIfEnabled(fqcn, jdkLevel, t, format, arguments);
    }

    /**
     * 打印对应等级的日志
     *
     * @param level     等级
     * @param throwable 异常对象
     * @param format    消息模板
     * @param arguments 参数
     */
    private void logIfEnabled(Level level, Throwable throwable, String format, Object[] arguments) {
        this.logIfEnabled(FQCN_SELF, level, throwable, format, arguments);
    }

    /**
     * 打印对应等级的日志
     *
     * @param callerFQCN
     * @param level      等级
     * @param throwable  异常对象
     * @param format     消息模板
     * @param arguments  参数
     */
    private void logIfEnabled(String callerFQCN, Level level, Throwable throwable, String format, Object[] arguments) {
        if (logger.isLoggable(level)) {
            LogRecord record = new LogRecord(level, StringUtils.format(format, arguments));
            record.setLoggerName(getName());
            record.setThrown(throwable);
            fillCallerData(callerFQCN, record);
            logger.log(record);
        }
    }

}
