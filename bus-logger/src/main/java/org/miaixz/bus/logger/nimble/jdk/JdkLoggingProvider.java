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
package org.miaixz.bus.logger.nimble.jdk;

import java.io.Serial;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.logger.magic.AbstractProvider;

/**
 * A logger provider implementation that wraps a {@link java.util.logging.Logger} instance.
 *
 * @author Kimi Liu
 */
public class JdkLoggingProvider extends AbstractProvider {

    @Serial
    private static final long serialVersionUID = 2538935000001L;

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
     * Gets the name of this logger.
     *
     * @return the name of this logger.
     */
    @Override
    public String getName() {
        return logger.getName();
    }

    /**
     * Checks if logging at the TRACE level is enabled.
     *
     * @return {@code true} if TRACE level is enabled, {@code false} otherwise.
     */
    @Override
    public boolean isTraceEnabled() {
        return logger.isLoggable(Level.FINEST);
    }

    /**
     * Logs a formatted message with a throwable at the TRACE level, specifying the fully qualified class name (FQCN).
     *
     * @param fqcn   the fully qualified class name of the logger.
     * @param t      the throwable to log.
     * @param format the message format.
     * @param args   the arguments for the message format.
     */
    @Override
    public void trace(final String fqcn, final Throwable t, final String format, final Object... args) {
        logIfEnabled(fqcn, Level.FINEST, t, format, args);
    }

    /**
     * Checks if logging at the DEBUG level is enabled.
     *
     * @return {@code true} if DEBUG level is enabled, {@code false} otherwise.
     */
    @Override
    public boolean isDebugEnabled() {
        return logger.isLoggable(Level.FINE);
    }

    /**
     * Logs a formatted message with a throwable at the DEBUG level, specifying the fully qualified class name (FQCN).
     *
     * @param fqcn   the fully qualified class name of the logger.
     * @param t      the throwable to log.
     * @param format the message format.
     * @param args   the arguments for the message format.
     */
    @Override
    public void debug(final String fqcn, final Throwable t, final String format, final Object... args) {
        logIfEnabled(fqcn, Level.FINE, t, format, args);
    }

    /**
     * Checks if logging at the INFO level is enabled.
     *
     * @return {@code true} if INFO level is enabled, {@code false} otherwise.
     */
    @Override
    public boolean isInfoEnabled() {
        return logger.isLoggable(Level.INFO);
    }

    /**
     * Logs a formatted message with a throwable at the INFO level, specifying the fully qualified class name (FQCN).
     *
     * @param fqcn   the fully qualified class name of the logger.
     * @param t      the throwable to log.
     * @param format the message format.
     * @param args   the arguments for the message format.
     */
    @Override
    public void info(final String fqcn, final Throwable t, final String format, final Object... args) {
        logIfEnabled(fqcn, Level.INFO, t, format, args);
    }

    /**
     * Checks if logging at the WARN level is enabled.
     *
     * @return {@code true} if WARN level is enabled, {@code false} otherwise.
     */
    @Override
    public boolean isWarnEnabled() {
        return logger.isLoggable(Level.WARNING);
    }

    /**
     * Logs a formatted message with a throwable at the WARN level, specifying the fully qualified class name (FQCN).
     *
     * @param fqcn   the fully qualified class name of the logger.
     * @param t      the throwable to log.
     * @param format the message format.
     * @param args   the arguments for the message format.
     */
    @Override
    public void warn(final String fqcn, final Throwable t, final String format, final Object... args) {
        logIfEnabled(fqcn, Level.WARNING, t, format, args);
    }

    /**
     * Checks if logging at the ERROR level is enabled.
     *
     * @return {@code true} if ERROR level is enabled, {@code false} otherwise.
     */
    @Override
    public boolean isErrorEnabled() {
        return logger.isLoggable(Level.SEVERE);
    }

    /**
     * Logs a formatted message with a throwable at the ERROR level, specifying the fully qualified class name (FQCN).
     *
     * @param fqcn   the fully qualified class name of the logger.
     * @param t      the throwable to log.
     * @param format the message format.
     * @param args   the arguments for the message format.
     */
    @Override
    public void error(final String fqcn, final Throwable t, final String format, final Object... args) {
        logIfEnabled(fqcn, Level.SEVERE, t, format, args);
    }

    /**
     * Logs a message with a throwable at the specified level, providing the fully qualified class name (FQCN).
     *
     * @param fqcn   the fully qualified class name of the logger.
     * @param level  the logging level.
     * @param t      the throwable to log.
     * @param format the message format.
     * @param args   the arguments for the message format.
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
     * Gets the current logging level.
     *
     * @return the current logging level, or {@link Level#OFF} if it cannot be determined.
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
