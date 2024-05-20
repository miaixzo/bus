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
package org.miaixz.bus.logger.metric.jdk;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.toolkit.StringKit;
import org.miaixz.bus.logger.AbstractAware;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * java.util.logging log.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class JdkLog extends AbstractAware {

    private final transient Logger logger;

    public JdkLog(Logger logger) {
        this.logger = logger;
    }

    public JdkLog(Class<?> clazz) {
        this((null == clazz) ? Normal.NULL : clazz.getName());
    }

    public JdkLog(String name) {
        this(Logger.getLogger(name));
    }

    /**
     * 传入调用日志类的信息
     *
     * @param callerFQCN 调用者全限定类名
     * @param record     The record to update
     */
    private static void fillCallerData(String callerFQCN, LogRecord record) {
        StackTraceElement[] steArray = Thread.currentThread().getStackTrace();

        int found = -1;
        String className;
        for (int i = steArray.length - 2; i > -1; i--) {
            className = steArray[i].getClassName();
            if (callerFQCN.equals(className)) {
                found = i;
                break;
            }
        }

        if (found > -1) {
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
    public boolean isTrace() {
        return logger.isLoggable(Level.FINEST);
    }

    @Override
    public void trace(String fqcn, Throwable t, String format, Object... arguments) {
        logIfEnabled(fqcn, Level.FINEST, t, format, arguments);
    }

    @Override
    public boolean isDebug() {
        return logger.isLoggable(Level.FINE);
    }

    @Override
    public void debug(String fqcn, Throwable t, String format, Object... arguments) {
        logIfEnabled(fqcn, Level.FINE, t, format, arguments);
    }

    @Override
    public boolean isInfo() {
        return logger.isLoggable(Level.INFO);
    }

    @Override
    public void info(String fqcn, Throwable t, String format, Object... arguments) {
        logIfEnabled(fqcn, Level.INFO, t, format, arguments);
    }

    @Override
    public boolean isWarn() {
        return logger.isLoggable(Level.WARNING);
    }

    @Override
    public void warn(String fqcn, Throwable t, String format, Object... arguments) {
        logIfEnabled(fqcn, Level.WARNING, t, format, arguments);
    }

    @Override
    public boolean isError() {
        return logger.isLoggable(Level.SEVERE);
    }

    @Override
    public void error(String fqcn, Throwable t, String format, Object... arguments) {
        logIfEnabled(fqcn, Level.SEVERE, t, format, arguments);
    }

    @Override
    public void log(String fqcn, org.miaixz.bus.logger.Level level, Throwable t, String format, Object... arguments) {
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
                throw new Error(StringKit.format("Can not identify level: {}", level));
        }
        logIfEnabled(fqcn, jdkLevel, t, format, arguments);
    }

    /**
     * 打印对应等级的日志
     *
     * @param callerFQCN 调用者的完全限定类名(Fully Qualified Class Name)
     * @param level      等级
     * @param throwable  异常对象
     * @param format     消息模板
     * @param arguments  参数
     */
    private void logIfEnabled(String callerFQCN, Level level, Throwable throwable, String format, Object[] arguments) {
        if (logger.isLoggable(level)) {
            LogRecord record = new LogRecord(level, StringKit.format(format, arguments));
            record.setLoggerName(getName());
            record.setThrown(throwable);
            fillCallerData(callerFQCN, record);
            logger.log(record);
        }
    }

}
