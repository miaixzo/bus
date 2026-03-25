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
package org.miaixz.bus.logger.metric.jboss;

import org.jboss.logging.Logger;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.logger.Level;
import org.miaixz.bus.logger.magic.AbstractProvider;

import java.io.Serial;

/**
 * A logger provider implementation that wraps a {@link org.jboss.logging.Logger} instance.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class JbossLoggingProvider extends AbstractProvider {

    @Serial
    private static final long serialVersionUID = 2852287115767L;

    /**
     * The underlying JBoss Logging logger instance.
     */
    private final transient Logger logger;

    /**
     * Constructs a new {@code JbossLoggingProvider} with the specified logger.
     *
     * @param logger the {@link Logger} instance to use.
     */
    public JbossLoggingProvider(final Logger logger) {
        this.logger = logger;
    }

    /**
     * Constructs a new {@code JbossLoggingProvider} for the specified class.
     *
     * @param clazz the class for which to create the logger.
     */
    public JbossLoggingProvider(final Class<?> clazz) {
        this((null == clazz) ? Normal.NULL : clazz.getName());
    }

    /**
     * Constructs a new {@code JbossLoggingProvider} for the specified name.
     *
     * @param name the name of the logger.
     */
    public JbossLoggingProvider(final String name) {
        this(Logger.getLogger(name));
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
        if (isTraceEnabled()) {
            logger.trace(fqcn, StringKit.format(format, args), t);
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
            logger.debug(fqcn, StringKit.format(format, args), t);
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
            logger.info(fqcn, StringKit.format(format, args), t);
        }
    }

    /**
     * Checks whether WARN level logging is enabled.
     *
     * @return {@code true} if WARN level logging is enabled
     */
    @Override
    public boolean isWarnEnabled() {
        return logger.isEnabled(Logger.Level.WARN);
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
            logger.warn(fqcn, StringKit.format(format, args), t);
        }
    }

    /**
     * Checks whether ERROR level logging is enabled.
     *
     * @return {@code true} if ERROR level logging is enabled
     */
    @Override
    public boolean isErrorEnabled() {
        return logger.isEnabled(Logger.Level.ERROR);
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
            logger.error(fqcn, StringKit.format(format, args), t);
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
        // JBoss Logging does not directly provide a getLevel() method, try checking the underlying implementation
        if (logger.isTraceEnabled()) {
            return Level.TRACE;
        } else if (logger.isDebugEnabled()) {
            return Level.DEBUG;
        } else if (logger.isInfoEnabled()) {
            return Level.INFO;
        } else if (logger.isEnabled(Logger.Level.WARN)) {
            return Level.WARN;
        } else if (logger.isEnabled(Logger.Level.ERROR)) {
            return Level.ERROR;
        }
        // Fallback to the default implementation
        return super.getLevel();
    }

}
