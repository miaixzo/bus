/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.logger.magic;

import org.miaixz.bus.core.xyz.ExceptionKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.logger.Level;
import org.miaixz.bus.logger.Provider;

import java.io.Serial;
import java.io.Serializable;

/**
 * Abstract base class for logger providers, implementing common functionality.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class AbstractProvider implements Provider, Serializable {

    @Serial
    private static final long serialVersionUID = 2852286301053L;

    /**
     * The fully qualified class name of this abstract provider.
     */
    private static final String FQCN = AbstractProvider.class.getName();

    /**
     * The name of the logger.
     */
    protected String name;

    /**
     * Checks if logging is enabled for the specified level.
     *
     * @param level the logging level to check
     * @return {@code true} if logging is enabled for the specified level
     */
    @Override
    public boolean isEnabled(final Level level) {
        switch (level) {
            case TRACE:
                return isTraceEnabled();

            case DEBUG:
                return isDebugEnabled();

            case INFO:
                return isInfoEnabled();

            case WARN:
                return isWarnEnabled();

            case ERROR:
                return isErrorEnabled();

            default:
                throw new Error(StringKit.format("Can not identify level: {}", level));
        }
    }

    /**
     * Logs a message at TRACE level with a throwable.
     *
     * @param t the throwable to log
     */
    @Override
    public void trace(final Throwable t) {
        trace(t, ExceptionKit.getSimpleMessage(t));
    }

    /**
     * Logs a formatted message at TRACE level.
     *
     * @param format the message format string
     * @param args   the arguments to format into the message string
     */
    @Override
    public void trace(final String format, final Object... args) {
        trace(null, format, args);
    }

    /**
     * Logs a formatted message at TRACE level with a throwable.
     *
     * @param t      the throwable to log
     * @param format the message format string
     * @param args   the arguments to format into the message string
     */
    @Override
    public void trace(final Throwable t, final String format, final Object... args) {
        trace(FQCN, t, format, args);
    }

    /**
     * Logs a message at DEBUG level with a throwable.
     *
     * @param t the throwable to log
     */
    @Override
    public void debug(final Throwable t) {
        debug(t, ExceptionKit.getSimpleMessage(t));
    }

    /**
     * Logs a formatted message at DEBUG level.
     *
     * @param format the message format string
     * @param args   the arguments to format into the message string
     */
    @Override
    public void debug(final String format, final Object... args) {
        if (null != args && 1 == args.length && args[0] instanceof Throwable) {
            // Compatible with xxx(String message, Throwable e) in Slf4j
            debug((Throwable) args[0], format);
        } else {
            debug(null, format, args);
        }
    }

    /**
     * Logs a formatted message at DEBUG level with a throwable.
     *
     * @param t      the throwable to log
     * @param format the message format string
     * @param args   the arguments to format into the message string
     */
    @Override
    public void debug(final Throwable t, final String format, final Object... args) {
        debug(FQCN, t, format, args);
    }

    /**
     * Logs a message at INFO level with a throwable.
     *
     * @param t the throwable to log
     */
    @Override
    public void info(final Throwable t) {
        info(t, ExceptionKit.getSimpleMessage(t));
    }

    /**
     * Logs a formatted message at INFO level.
     *
     * @param format the message format string
     * @param args   the arguments to format into the message string
     */
    @Override
    public void info(final String format, final Object... args) {
        if (null != args && 1 == args.length && args[0] instanceof Throwable) {
            // Compatible with xxx(String message, Throwable e) in Slf4j
            info((Throwable) args[0], format);
        } else {
            info(null, format, args);
        }
    }

    /**
     * Logs a formatted message at INFO level with a throwable.
     *
     * @param t      the throwable to log
     * @param format the message format string
     * @param args   the arguments to format into the message string
     */
    @Override
    public void info(final Throwable t, final String format, final Object... args) {
        info(FQCN, t, format, args);
    }

    /**
     * Logs a message at WARN level with a throwable.
     *
     * @param t the throwable to log
     */
    @Override
    public void warn(final Throwable t) {
        warn(t, ExceptionKit.getSimpleMessage(t));
    }

    /**
     * Logs a formatted message at WARN level.
     *
     * @param format the message format string
     * @param args   the arguments to format into the message string
     */
    @Override
    public void warn(final String format, final Object... args) {
        if (null != args && 1 == args.length && args[0] instanceof Throwable) {
            // Compatible with xxx(String message, Throwable e) in Slf4j
            warn((Throwable) args[0], format);
        } else {
            warn(null, format, args);
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
        warn(FQCN, t, format, args);
    }

    /**
     * Logs a message at ERROR level with a throwable.
     *
     * @param t the throwable to log
     */
    @Override
    public void error(final Throwable t) {
        this.error(t, ExceptionKit.getSimpleMessage(t));
    }

    /**
     * Logs a formatted message at ERROR level.
     *
     * @param format the message format string
     * @param args   the arguments to format into the message string
     */
    @Override
    public void error(final String format, final Object... args) {
        if (null != args && 1 == args.length && args[0] instanceof Throwable) {
            // Compatible with xxx(String message, Throwable e) in Slf4j
            error((Throwable) args[0], format);
        } else {
            error(null, format, args);
        }
    }

    /**
     * Logs a formatted message at ERROR level with a throwable.
     *
     * @param t      the throwable to log
     * @param format the message format string
     * @param args   the arguments to format into the message string
     */
    @Override
    public void error(final Throwable t, final String format, final Object... args) {
        error(FQCN, t, format, args);
    }

    /**
     * Logs a formatted message at the specified level.
     *
     * @param level  the logging level
     * @param format the message format string
     * @param args   the arguments to format into the message string
     */
    @Override
    public void log(final Level level, final String format, final Object... args) {
        if (null != args && 1 == args.length && args[0] instanceof Throwable) {
            // Compatible with xxx(String message, Throwable e) in Slf4j
            log(level, (Throwable) args[0], format);
        } else {
            log(level, null, format, args);
        }
    }

    /**
     * Logs a formatted message at the specified level with a throwable.
     *
     * @param level  the logging level
     * @param t      the throwable to log
     * @param format the message format string
     * @param args   the arguments to format into the message string
     */
    @Override
    public void log(final Level level, final Throwable t, final String format, final Object... args) {
        this.log(FQCN, level, t, format, args);
    }

}
