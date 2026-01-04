/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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

import java.io.Serial;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * A logger provider implementation that wraps a {@link java.util.logging.Logger} instance.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class JdkLoggingProvider extends AbstractProvider {

    @Serial
    private static final long serialVersionUID = 2852287167223L;

    /**
     * The underlying {@link java.util.logging.Logger} instance.
     */
    private final transient Logger logger;

    /**
     * Constructs a new {@code JdkLoggingProvider} with the specified logger.
     *
     * @param logger the {@link Logger} instance to use.
     */
    public JdkLoggingProvider(final Logger logger) {
        this.logger = logger;
    }

    /**
     * Constructs a new {@code JdkLoggingProvider} for the specified class.
     *
     * @param clazz the class for which to create the logger.
     */
    public JdkLoggingProvider(final Class<?> clazz) {
        this((null == clazz) ? Normal.NULL : clazz.getName());
    }

    /**
     * Constructs a new {@code JdkLoggingProvider} for the specified name.
     *
     * @param name the name of the logger.
     */
    public JdkLoggingProvider(final String name) {
        this(Logger.getLogger(name));
    }

    /**
     * Fills the {@link LogRecord} with the source class name and method name of the caller.
     *
     * @param fqcn   the fully qualified class name of the caller.
     * @param record the {@link LogRecord} to update.
     */
    private static void fill(final String fqcn, final LogRecord record) {
        final StackTraceElement[] steArray = Thread.currentThread().getStackTrace();

        int found = -1;
        String className;
        for (int i = steArray.length - 2; i > -1; i--) {
            // The initial value here is length-2, which means that the check starts from the penultimate stack.
            // If it is the last one, the caller will not be able to get it.
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

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return logger.getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isTraceEnabled() {
        return logger.isLoggable(Level.FINEST);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trace(final String fqcn, final Throwable t, final String format, final Object... args) {
        logIfEnabled(fqcn, Level.FINEST, t, format, args);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isDebugEnabled() {
        return logger.isLoggable(Level.FINE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void debug(final String fqcn, final Throwable t, final String format, final Object... args) {
        logIfEnabled(fqcn, Level.FINE, t, format, args);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isInfoEnabled() {
        return logger.isLoggable(Level.INFO);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void info(final String fqcn, final Throwable t, final String format, final Object... args) {
        logIfEnabled(fqcn, Level.INFO, t, format, args);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isWarnEnabled() {
        return logger.isLoggable(Level.WARNING);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void warn(final String fqcn, final Throwable t, final String format, final Object... args) {
        logIfEnabled(fqcn, Level.WARNING, t, format, args);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isErrorEnabled() {
        return logger.isLoggable(Level.SEVERE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void error(final String fqcn, final Throwable t, final String format, final Object... args) {
        logIfEnabled(fqcn, Level.SEVERE, t, format, args);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void log(
            final String fqcn,
            final org.miaixz.bus.logger.Level level,
            final Throwable t,
            final String format,
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
     * Logs a message at the specified level if it is enabled.
     *
     * @param fqcn      the fully qualified class name of the caller.
     * @param level     the logging level.
     * @param throwable the throwable to log.
     * @param format    the message format.
     * @param args      the arguments for the message format.
     */
    private void logIfEnabled(
            final String fqcn,
            final Level level,
            final Throwable throwable,
            final String format,
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
     * {@inheritDoc}
     */
    @Override
    public org.miaixz.bus.logger.Level getLevel() {
        Level jdkLevel = logger.getLevel();
        if (jdkLevel == null) {
            return org.miaixz.bus.logger.Level.OFF;
        }
        return switch (jdkLevel.getName()) {
            case "FINEST" -> org.miaixz.bus.logger.Level.TRACE;
            case "FINE", "FINER" -> org.miaixz.bus.logger.Level.DEBUG;
            case "INFO" -> org.miaixz.bus.logger.Level.INFO;
            case "WARNING" -> org.miaixz.bus.logger.Level.WARN;
            case "SEVERE" -> org.miaixz.bus.logger.Level.ERROR;
            default -> org.miaixz.bus.logger.Level.OFF;
        };
    }

}
