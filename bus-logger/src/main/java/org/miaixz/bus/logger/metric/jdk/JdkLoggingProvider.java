/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.logger.metric.jdk;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.logger.magic.AbstractProvider;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * java.util.logging
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class JdkLoggingProvider extends AbstractProvider {

    private static final long serialVersionUID = -1L;

    /**
     * 日志门面
     */
    private final transient Logger logger;

    /**
     * 构造
     *
     * @param logger 日志对象
     */
    public JdkLoggingProvider(final Logger logger) {
        this.logger = logger;
    }

    /**
     * 构造
     *
     * @param clazz 日志实现类
     */
    public JdkLoggingProvider(final Class<?> clazz) {
        this((null == clazz) ? Normal.NULL : clazz.getName());
    }

    /**
     * 构造
     *
     * @param name 日志实现类名
     */
    public JdkLoggingProvider(final String name) {
        this(Logger.getLogger(name));
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
    public void trace(final String fqcn, final Throwable t, final String format, final Object... args) {
        logIfEnabled(fqcn, Level.FINEST, t, format, args);
    }

    @Override
    public boolean isDebugEnabled() {
        return logger.isLoggable(Level.FINE);
    }

    @Override
    public void debug(final String fqcn, final Throwable t, final String format, final Object... args) {
        logIfEnabled(fqcn, Level.FINE, t, format, args);
    }

    @Override
    public boolean isInfoEnabled() {
        return logger.isLoggable(Level.INFO);
    }

    @Override
    public void info(final String fqcn, final Throwable t, final String format, final Object... args) {
        logIfEnabled(fqcn, Level.INFO, t, format, args);
    }

    @Override
    public boolean isWarnEnabled() {
        return logger.isLoggable(Level.WARNING);
    }

    @Override
    public void warn(final String fqcn, final Throwable t, final String format, final Object... args) {
        logIfEnabled(fqcn, Level.WARNING, t, format, args);
    }

    @Override
    public boolean isErrorEnabled() {
        return logger.isLoggable(Level.SEVERE);
    }

    @Override
    public void error(final String fqcn, final Throwable t, final String format, final Object... args) {
        logIfEnabled(fqcn, Level.SEVERE, t, format, args);
    }

    @Override
    public void log(final String fqcn, final org.miaixz.bus.logger.Level level, final Throwable t, final String format,
            final Object... args) {
        final Level jdkLevel;
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
        logIfEnabled(fqcn, jdkLevel, t, format, args);
    }

    /**
     * 打印对应等级的日志
     *
     * @param fqcn      调用者的完全限定类名(Fully Qualified Class Name)
     * @param level     等级
     * @param throwable 异常对象
     * @param format    消息模板
     * @param args      参数
     */
    private void logIfEnabled(final String fqcn, final Level level, final Throwable throwable, final String format,
            final Object[] args) {
        if (logger.isLoggable(level)) {
            final LogRecord record = new LogRecord(level, StringKit.format(format, args));
            record.setLoggerName(getName());
            record.setThrown(throwable);
            fill(fqcn, record);
            logger.log(record);
        }
    }

    /**
     * 传入调用日志类的信息
     *
     * @param fqcn   调用者全限定类名
     * @param record 要更新的记录
     */
    private static void fill(final String fqcn, final LogRecord record) {
        final StackTraceElement[] steArray = Thread.currentThread().getStackTrace();

        int found = -1;
        String className;
        for (int i = steArray.length - 2; i > -1; i--) {
            // 此处初始值为length-2，表示从倒数第二个堆栈开始检查，如果是倒数第一个，那调用者就获取不到
            className = steArray[i].getClassName();
            if (fqcn.equals(className)) {
                found = i;
                break;
            }
        }

        if (found > -1) {
            final StackTraceElement ste = steArray[found + 1];
            record.setSourceClassName(ste.getClassName());
            record.setSourceMethodName(ste.getMethodName());
        }
    }

}
