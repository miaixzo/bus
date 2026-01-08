/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.logger.metric.apache.log4j;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.spi.AbstractLogger;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.logger.magic.AbstractProvider;

import java.io.Serial;

/**
 * A logger provider implementation that wraps an {@link org.apache.logging.log4j.Logger} instance.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Log4jLoggingProvider extends AbstractProvider {

    @Serial
    private static final long serialVersionUID = 2852286601223L;

    /**
     * The underlying Log4j 2 logger instance.
     */
    private final transient Logger logger;

    /**
     * Constructs a new {@code Log4jLoggingProvider} with the specified logger.
     *
     * @param logger the {@link Logger} instance to use.
     */
    public Log4jLoggingProvider(final Logger logger) {
        this.logger = logger;
    }

    /**
     * Constructs a new {@code Log4jLoggingProvider} for the specified class.
     *
     * @param clazz the class for which to create the logger.
     */
    public Log4jLoggingProvider(final Class<?> clazz) {
        this(LogManager.getLogger(clazz));
    }

    /**
     * Constructs a new {@code Log4jLoggingProvider} for the specified name.
     *
     * @param name the name of the logger.
     */
    public Log4jLoggingProvider(final String name) {
        this(LogManager.getLogger(name));
    }

    /**
     * Gets the name of this logger.
     *
     * @return the name of this logger
     */
    @Override
    public String getName() {
        return logger.getName();
    }

    /**
     * Checks whether TRACE level logging is enabled.
     *
     * @return {@code true} if TRACE level logging is enabled
     */
    @Override
    public boolean isTraceEnabled() {
        return logger.isTraceEnabled();
    }

    /**
     * Logs a message at TRACE level with full context.
     *
     * @param fqcn   the fully qualified class name of the caller
     * @param t      the throwable to log
     * @param format the message format string
     * @param args   the arguments to format into the message string
     */
    @Override
    public void trace(final String fqcn, final Throwable t, final String format, final Object... args) {
        logIfEnabled(fqcn, Level.TRACE, t, format, args);
    }

    /**
     * Checks whether DEBUG level logging is enabled.
     *
     * @return {@code true} if DEBUG level logging is enabled
     */
    @Override
    public boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }

    /**
     * Logs a message at DEBUG level with full context.
     *
     * @param fqcn   the fully qualified class name of the caller
     * @param t      the throwable to log
     * @param format the message format string
     * @param args   the arguments to format into the message string
     */
    @Override
    public void debug(final String fqcn, final Throwable t, final String format, final Object... args) {
        logIfEnabled(fqcn, Level.DEBUG, t, format, args);
    }

    /**
     * Checks whether INFO level logging is enabled.
     *
     * @return {@code true} if INFO level logging is enabled
     */
    @Override
    public boolean isInfoEnabled() {
        return logger.isInfoEnabled();
    }

    /**
     * Logs a message at INFO level with full context.
     *
     * @param fqcn   the fully qualified class name of the caller
     * @param t      the throwable to log
     * @param format the message format string
     * @param args   the arguments to format into the message string
     */
    @Override
    public void info(final String fqcn, final Throwable t, final String format, final Object... args) {
        logIfEnabled(fqcn, Level.INFO, t, format, args);
    }

    /**
     * Checks whether WARN level logging is enabled.
     *
     * @return {@code true} if WARN level logging is enabled
     */
    @Override
    public boolean isWarnEnabled() {
        return logger.isWarnEnabled();
    }

    /**
     * Logs a message at WARN level with full context.
     *
     * @param fqcn   the fully qualified class name of the caller
     * @param t      the throwable to log
     * @param format the message format string
     * @param args   the arguments to format into the message string
     */
    @Override
    public void warn(final String fqcn, final Throwable t, final String format, final Object... args) {
        logIfEnabled(fqcn, Level.WARN, t, format, args);
    }

    /**
     * Checks whether ERROR level logging is enabled.
     *
     * @return {@code true} if ERROR level logging is enabled
     */
    @Override
    public boolean isErrorEnabled() {
        return logger.isErrorEnabled();
    }

    /**
     * Logs a message at ERROR level with full context.
     *
     * @param fqcn   the fully qualified class name of the caller
     * @param t      the throwable to log
     * @param format the message format string
     * @param args   the arguments to format into the message string
     */
    @Override
    public void error(final String fqcn, final Throwable t, final String format, final Object... args) {
        logIfEnabled(fqcn, Level.ERROR, t, format, args);
    }

    /**
     * Logs a message at the specified level with full context.
     *
     * @param fqcn   the fully qualified class name of the caller
     * @param level  the logging level
     * @param t      the throwable to log
     * @param format the message format string
     * @param args   the arguments to format into the message string
     */
    @Override
    public void log(
            final String fqcn,
            final org.miaixz.bus.logger.Level level,
            final Throwable t,
            final String format,
            final Object... args) {
        final Level log4j2Level = switch (level) {
            case TRACE -> Level.TRACE;
            case DEBUG -> Level.DEBUG;
            case INFO -> Level.INFO;
            case WARN -> Level.WARN;
            case ERROR -> Level.ERROR;
            default -> throw new Error(StringKit.format("Can not identify level: {}", level));
        };
        logIfEnabled(fqcn, log4j2Level, t, format, args);
    }

    /**
     * Logs a message. This method is used to support underlying logging implementations by passing the fully qualified
     * class name of the caller, which helps in correcting the line number in the log output.
     *
     * @param fqcn   the fully qualified class name of the caller.
     * @param level  the logging level, using the constants from {@link org.apache.logging.log4j.Level}.
     * @param t      the throwable to log.
     * @param format the message format.
     * @param args   the arguments for the message format.
     */
    private void logIfEnabled(
            final String fqcn,
            final Level level,
            final Throwable t,
            final String format,
            final Object... args) {
        if (this.logger.isEnabled(level)) {
            if (this.logger instanceof AbstractLogger) {
                ((AbstractLogger) this.logger).logIfEnabled(fqcn, level, null, StringKit.format(format, args), t);
            } else {
                this.logger.log(level, StringKit.format(format, args), t);
            }
        }
    }

    /**
     * Gets the current logging level.
     *
     * @return the current logging level, or {@link org.miaixz.bus.logger.Level#OFF} if it cannot be determined
     */
    @Override
    public org.miaixz.bus.logger.Level getLevel() {
        Level log4jLevel = logger.getLevel();
        if (log4jLevel == null) {
            return org.miaixz.bus.logger.Level.OFF;
        }
        return switch (log4jLevel.getStandardLevel()) {
            case TRACE -> org.miaixz.bus.logger.Level.TRACE;
            case DEBUG -> org.miaixz.bus.logger.Level.DEBUG;
            case INFO -> org.miaixz.bus.logger.Level.INFO;
            case WARN -> org.miaixz.bus.logger.Level.WARN;
            case ERROR, FATAL -> org.miaixz.bus.logger.Level.ERROR; // Map FATAL to ERROR
            default -> org.miaixz.bus.logger.Level.OFF;
        };
    }

}
