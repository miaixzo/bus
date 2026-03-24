/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.logger.metric.apache.commons;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.logger.Level;
import org.miaixz.bus.logger.magic.AbstractProvider;

import java.io.Serial;

/**
 * A logger provider implementation that wraps an {@link org.apache.commons.logging.Log} instance.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class CommonsLoggingProvider extends AbstractProvider {

    @Serial
    private static final long serialVersionUID = 2852286531003L;

    /**
     * The underlying Apache Commons Logging logger instance.
     */
    private final transient Log logger;

    /**
     * Constructs a new {@code CommonsLoggingProvider} for the specified name.
     *
     * @param name the name of the logger.
     */
    public CommonsLoggingProvider(final String name) {
        this(LogFactory.getLog(name), name);
    }

    /**
     * Constructs a new {@code CommonsLoggingProvider} for the specified class.
     *
     * @param clazz the class for which to create the logger.
     */
    public CommonsLoggingProvider(final Class<?> clazz) {
        this(LogFactory.getLog(clazz), null == clazz ? Normal.NULL : clazz.getName());
    }

    /**
     * Constructs a new {@code CommonsLoggingProvider} with the specified logger and name.
     *
     * @param logger the {@link Log} instance to use.
     * @param name   the name of the logger.
     */
    public CommonsLoggingProvider(final Log logger, final String name) {
        this.logger = logger;
        this.name = name;
    }

    /**
     * Gets the name of this logger.
     *
     * @return the name of this logger
     */
    @Override
    public String getName() {
        return this.name;
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
        if (isTraceEnabled()) {
            logger.trace(StringKit.format(format, args), t);
        }
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
        if (isDebugEnabled()) {
            logger.debug(StringKit.format(format, args), t);
        }
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
        if (isInfoEnabled()) {
            logger.info(StringKit.format(format, args), t);
        }
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
     * Logs a formatted message at WARN level.
     *
     * @param format the message format string
     * @param args   the arguments to format into the message string
     */
    @Override
    public void warn(final String format, final Object... args) {
        if (isWarnEnabled()) {
            logger.warn(StringKit.format(format, args));
        }
    }

    /**
     * Logs a formatted message at WARN level with a throwable.
     *
     * @param t      the throwable to log
     * @param format the message format string
     * @param args   the arguments to format into the message string
     */
    @Override
    public void warn(final Throwable t, final String format, final Object... args) {
        if (isWarnEnabled()) {
            logger.warn(StringKit.format(format, args), t);
        }
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
        if (isWarnEnabled()) {
            logger.warn(StringKit.format(format, args), t);
        }
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
        if (isErrorEnabled()) {
            logger.error(StringKit.format(format, args), t);
        }
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
            final Level level,
            final Throwable t,
            final String format,
            final Object... args) {
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
     * Gets the current logging level.
     *
     * @return the current logging level, or {@link Level#OFF} if it cannot be determined
     */
    @Override
    public Level getLevel() {
        // Try to check the underlying logging framework (e.g., Log4j or Logback)
        if (logger instanceof org.apache.logging.log4j.Logger log4jLogger) { // Log4j 2.x
            org.apache.logging.log4j.Level log4jLevel = log4jLogger.getLevel();
            if (log4jLevel != null) {
                return switch (log4jLevel.getStandardLevel()) {
                    case TRACE -> Level.TRACE;
                    case DEBUG -> Level.DEBUG;
                    case INFO -> Level.INFO;
                    case WARN -> Level.WARN;
                    case ERROR, FATAL -> Level.ERROR; // Map FATAL to ERROR
                    default -> Level.OFF;
                };
            }
        }
        // Fallback to the default implementation
        return super.getLevel();
    }

}
