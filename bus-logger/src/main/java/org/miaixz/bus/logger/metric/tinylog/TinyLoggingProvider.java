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
package org.miaixz.bus.logger.metric.tinylog;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.xyz.ArrayKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.logger.magic.AbstractProvider;
import org.tinylog.Level;
import org.tinylog.configuration.Configuration;
import org.tinylog.format.AdvancedMessageFormatter;
import org.tinylog.format.MessageFormatter;
import org.tinylog.provider.LoggingProvider;
import org.tinylog.provider.ProviderRegistry;

import java.io.Serial;

/**
 * A logger provider implementation that wraps the tinylog logging framework.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class TinyLoggingProvider extends AbstractProvider {

    @Serial
    private static final long serialVersionUID = 2852287656777L;

    /**
     * The stack depth, which is increased by two layers due to encapsulation. This value is used to correctly obtain
     * the current class name.
     */
    private static final int DEPTH = 5;
    /**
     * The underlying tinylog logging provider.
     */
    private static final LoggingProvider provider = ProviderRegistry.getLoggingProvider();
    /**
     * The message formatter for formatting log messages.
     */
    private static final MessageFormatter formatter = new AdvancedMessageFormatter(Configuration.getLocale(),
            Configuration.isEscapingEnabled());
    /**
     * The minimum logging level.
     */
    private final int level;

    /**
     * Constructs a new {@code TinyLoggingProvider} for the specified class.
     *
     * @param clazz the class for which to create the logger.
     */
    public TinyLoggingProvider(final Class<?> clazz) {
        this(null == clazz ? Normal.NULL : clazz.getName());
    }

    /**
     * Constructs a new {@code TinyLoggingProvider} for the specified name.
     *
     * @param name the name of the logger.
     */
    public TinyLoggingProvider(final String name) {
        this.name = name;
        this.level = provider.getMinimumLevel().ordinal();
    }

    /**
     * Gets the last argument if it is a throwable, otherwise returns null.
     *
     * @param args the arguments.
     * @return the last throwable argument, or {@code null} if none is found.
     */
    private static Throwable getLastArgumentIfThrowable(final Object... args) {
        if (ArrayKit.isNotEmpty(args) && args[args.length - 1] instanceof Throwable) {
            return (Throwable) args[args.length - 1];
        } else {
            return null;
        }
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
        return this.level <= Level.TRACE.ordinal();
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
        return this.level <= Level.DEBUG.ordinal();
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
        return this.level <= Level.INFO.ordinal();
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
        return this.level <= Level.WARN.ordinal();
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
        return this.level <= Level.ERROR.ordinal();
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
        logIfEnabled(fqcn, toTinyLevel(level), t, format, args);
    }

    /**
     * Checks whether logging is enabled for the specified level.
     *
     * @param level the logging level to check
     * @return {@code true} if logging is enabled for the specified level
     */
    @Override
    public boolean isEnabled(final org.miaixz.bus.logger.Level level) {
        return this.level <= toTinyLevel(level).ordinal();
    }

    /**
     * Logs a message if the specified level is enabled.
     *
     * @param fqcn   the fully qualified class name of the caller.
     * @param level  the logging level.
     * @param t      the throwable to log. If null, it checks if the last argument is a throwable.
     * @param format the message format.
     * @param args   the arguments for the message format.
     */
    private void logIfEnabled(
            final String fqcn,
            final Level level,
            Throwable t,
            final String format,
            final Object... args) {
        if (null == t) {
            t = getLastArgumentIfThrowable(args);
        }
        provider.log(DEPTH, null, level, t, formatter, StringKit.toString(format), args);
    }

    /**
     * Converts a {@link org.miaixz.bus.logger.Level} to a {@link org.tinylog.Level}.
     *
     * @param level the level to convert.
     * @return the corresponding tinylog level.
     */
    private Level toTinyLevel(final org.miaixz.bus.logger.Level level) {
        return switch (level) {
            case TRACE -> Level.TRACE;
            case DEBUG -> Level.DEBUG;
            case INFO -> Level.INFO;
            case WARN -> Level.WARN;
            case ERROR, FATAL -> Level.ERROR;
            case OFF -> Level.OFF;
            default -> throw new Error(StringKit.format("Can not identify level: {}", level));
        };
    }

    /**
     * Gets the current logging level.
     *
     * @return the current logging level, or {@link org.miaixz.bus.logger.Level#OFF} if it cannot be determined
     */
    @Override
    public org.miaixz.bus.logger.Level getLevel() {
        Level tinyLevel = provider.getMinimumLevel();
        if (tinyLevel == null) {
            return org.miaixz.bus.logger.Level.OFF;
        }
        return switch (tinyLevel) {
            case TRACE -> org.miaixz.bus.logger.Level.TRACE;
            case DEBUG -> org.miaixz.bus.logger.Level.DEBUG;
            case INFO -> org.miaixz.bus.logger.Level.INFO;
            case WARN -> org.miaixz.bus.logger.Level.WARN;
            case ERROR -> org.miaixz.bus.logger.Level.ERROR;
            case OFF -> org.miaixz.bus.logger.Level.OFF;
        };
    }

}
