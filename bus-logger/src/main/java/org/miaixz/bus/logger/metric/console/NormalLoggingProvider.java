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
package org.miaixz.bus.logger.metric.console;

import org.miaixz.bus.core.center.map.Dictionary;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Console;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.xyz.DateKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.logger.Level;
import org.miaixz.bus.logger.magic.AbstractProvider;

import java.io.Serial;

/**
 * A logger provider that prints messages to the console using {@code System.out.println}.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class NormalLoggingProvider extends AbstractProvider {

    @Serial
    private static final long serialVersionUID = 2852287011503L;

    /**
     * The current logging level.
     */
    private static Level _level = Level.DEBUG;

    /**
     * Constructs a new {@code NormalLoggingProvider} for the specified class.
     *
     * @param clazz the class for which to create the logger.
     */
    public NormalLoggingProvider(final Class<?> clazz) {
        this.name = (null == clazz) ? Normal.NULL : clazz.getName();
    }

    /**
     * Constructs a new {@code NormalLoggingProvider} for the specified name.
     *
     * @param name the name of the logger (usually the class name).
     */
    public NormalLoggingProvider(final String name) {
        this.name = name;
    }

    /**
     * Sets the logging level.
     *
     * @param level the logging level to set.
     */
    public void setLevel(final Level level) {
        Assert.notNull(level);
        _level = level;
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
        return isEnabled(Level.TRACE);
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
        log(fqcn, Level.TRACE, t, format, args);
    }

    /**
     * Checks whether DEBUG level logging is enabled.
     *
     * @return {@code true} if DEBUG level logging is enabled
     */
    @Override
    public boolean isDebugEnabled() {
        return isEnabled(Level.DEBUG);
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
        log(fqcn, Level.DEBUG, t, format, args);
    }

    /**
     * Checks whether INFO level logging is enabled.
     *
     * @return {@code true} if INFO level logging is enabled
     */
    @Override
    public boolean isInfoEnabled() {
        return isEnabled(Level.INFO);
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
        log(fqcn, Level.INFO, t, format, args);
    }

    /**
     * Checks whether WARN level logging is enabled.
     *
     * @return {@code true} if WARN level logging is enabled
     */
    @Override
    public boolean isWarnEnabled() {
        return isEnabled(Level.WARN);
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
        log(fqcn, Level.WARN, t, format, args);
    }

    /**
     * Checks whether ERROR level logging is enabled.
     *
     * @return {@code true} if ERROR level logging is enabled
     */
    @Override
    public boolean isErrorEnabled() {
        return isEnabled(Level.ERROR);
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
        log(fqcn, Level.ERROR, t, format, args);
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
        if (!isEnabled(level)) {
            return;
        }

        // Create a dictionary to hold log message components.
        final Dictionary dict = Dictionary.of().set("date", DateKit.formatNow()).set("level", level.toString())
                .set("name", this.name).set("msg", StringKit.format(format, args));

        // Format the log message using the dictionary.
        final String logMsg = StringKit.formatByMap("[{date}] [{level}] {name}: {msg}", dict);

        // Print messages of WARN level or higher to System.err.
        if (level.ordinal() >= Level.WARN.ordinal()) {
            Console.error(t, logMsg);
        } else {
            // Print messages of lower levels to System.out.
            Console.log(t, logMsg);
        }
    }

    /**
     * Checks whether logging is enabled for the specified level.
     *
     * @param level the logging level to check
     * @return {@code true} if logging is enabled for the specified level
     */
    @Override
    public boolean isEnabled(final Level level) {
        return _level.compareTo(level) <= 0;
    }

    /**
     * Gets the current logging level.
     *
     * @return the current logging level, or {@link Level#OFF} if it cannot be determined
     */
    @Override
    public Level getLevel() {
        return _level != null ? _level : Level.OFF;
    }

}
