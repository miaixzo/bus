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
package org.miaixz.bus.logger.metric.slf4j;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.logger.Level;
import org.miaixz.bus.logger.magic.AbstractProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.spi.LocationAwareLogger;

/**
 * A logger provider implementation that wraps an {@link org.slf4j.Logger} instance.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Slf4jLoggingProvider extends AbstractProvider {

    private static final long serialVersionUID = -1L;

    /**
     * The underlying SLF4J logger instance.
     */
    private final transient Logger logger;
    /**
     * Whether the underlying logger is a {@link LocationAwareLogger}. This is used to determine if the FQCN can be
     * passed.
     */
    private final boolean isLocationAwareLogger;

    /**
     * Constructs a new {@code Slf4jLoggingProvider} with the specified logger.
     *
     * @param logger the {@link Logger} instance to use.
     */
    public Slf4jLoggingProvider(final Logger logger) {
        this.logger = logger;
        this.isLocationAwareLogger = (logger instanceof LocationAwareLogger);
    }

    /**
     * Constructs a new {@code Slf4jLoggingProvider} for the specified class.
     *
     * @param clazz the class for which to create the logger.
     */
    public Slf4jLoggingProvider(final Class<?> clazz) {
        this(getSlf4jLogger(clazz));
    }

    /**
     * Constructs a new {@code Slf4jLoggingProvider} for the specified name.
     *
     * @param name the name of the logger.
     */
    public Slf4jLoggingProvider(final String name) {
        this(LoggerFactory.getLogger(name));
    }

    /**
     * Gets the SLF4J logger for the specified class.
     *
     * @param clazz the class for which to get the logger. If {@code null}, a logger named "null" is returned.
     * @return the {@link Logger} instance.
     */
    private static Logger getSlf4jLogger(final Class<?> clazz) {
        return (null == clazz) ? LoggerFactory.getLogger(Normal.EMPTY) : LoggerFactory.getLogger(clazz);
    }

    @Override
    public String getName() {
        return logger.getName();
    }

    @Override
    public boolean isTraceEnabled() {
        return logger.isTraceEnabled();
    }

    @Override
    public void trace(final String fqcn, final Throwable t, final String format, final Object... args) {
        if (isTraceEnabled()) {
            if (this.isLocationAwareLogger) {
                locationAwareLog(
                        (LocationAwareLogger) this.logger,
                        fqcn,
                        LocationAwareLogger.TRACE_INT,
                        t,
                        format,
                        args);
            } else {
                logger.trace(StringKit.format(format, args), t);
            }
        }
    }

    @Override
    public boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }

    @Override
    public void debug(final String fqcn, final Throwable t, final String format, final Object... args) {
        if (isDebugEnabled()) {
            if (this.isLocationAwareLogger) {
                locationAwareLog(
                        (LocationAwareLogger) this.logger,
                        fqcn,
                        LocationAwareLogger.DEBUG_INT,
                        t,
                        format,
                        args);
            } else {
                logger.debug(StringKit.format(format, args), t);
            }
        }
    }

    @Override
    public boolean isInfoEnabled() {
        return logger.isInfoEnabled();
    }

    @Override
    public void info(final String fqcn, final Throwable t, final String format, final Object... args) {
        if (isInfoEnabled()) {
            if (this.isLocationAwareLogger) {
                locationAwareLog(
                        (LocationAwareLogger) this.logger,
                        fqcn,
                        LocationAwareLogger.INFO_INT,
                        t,
                        format,
                        args);
            } else {
                logger.info(StringKit.format(format, args), t);
            }
        }
    }

    @Override
    public boolean isWarnEnabled() {
        return logger.isWarnEnabled();
    }

    @Override
    public void warn(final String fqcn, final Throwable t, final String format, final Object... args) {
        if (isWarnEnabled()) {
            if (this.isLocationAwareLogger) {
                locationAwareLog(
                        (LocationAwareLogger) this.logger,
                        fqcn,
                        LocationAwareLogger.WARN_INT,
                        t,
                        format,
                        args);
            } else {
                logger.warn(StringKit.format(format, args), t);
            }
        }
    }

    @Override
    public boolean isErrorEnabled() {
        return logger.isErrorEnabled();
    }

    @Override
    public void error(final String fqcn, final Throwable t, final String format, final Object... args) {
        if (isErrorEnabled()) {
            if (this.isLocationAwareLogger) {
                locationAwareLog(
                        (LocationAwareLogger) this.logger,
                        fqcn,
                        LocationAwareLogger.ERROR_INT,
                        t,
                        format,
                        args);
            } else {
                logger.error(StringKit.format(format, args), t);
            }
        }
    }

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
     * Logs a message. This method is used to support underlying logging implementations by passing the fully qualified
     * class name of the caller, which helps in correcting the line number in the log output.
     *
     * @param logger    the {@link LocationAwareLogger} instance.
     * @param fqcn      the fully qualified class name of the caller.
     * @param level_int the logging level, using the constants from {@link LocationAwareLogger}.
     * @param t         the throwable to log.
     * @param format    the message format.
     * @param args      the arguments for the message format.
     */
    private void locationAwareLog(
            final LocationAwareLogger logger,
            final String fqcn,
            final int level_int,
            final Throwable t,
            final String format,
            final Object[] args) {
        // The implementation of this method in slf4j-log4j12 has a bug,
        // so the parameters are concatenated here.
        logger.log(null, fqcn, level_int, StringKit.format(format, args), null, t);
    }

    @Override
    public Level getLevel() {
        // Try to check if it is a Logback Logger
        if (logger instanceof ch.qos.logback.classic.Logger logbackLogger) {
            ch.qos.logback.classic.Level logbackLevel = logbackLogger.getLevel();
            if (logbackLevel != null) {
                return switch (logbackLevel.toString()) {
                    case "TRACE" -> Level.TRACE;
                    case "DEBUG" -> Level.DEBUG;
                    case "INFO" -> Level.INFO;
                    case "WARN" -> Level.WARN;
                    case "ERROR" -> Level.ERROR;
                    default -> Level.OFF;
                };
            }
        }
        // Fallback to inference based on isEnabled()
        if (logger.isTraceEnabled()) {
            return Level.TRACE;
        } else if (logger.isDebugEnabled()) {
            return Level.DEBUG;
        } else if (logger.isInfoEnabled()) {
            return Level.INFO;
        } else if (logger.isWarnEnabled()) {
            return Level.WARN;
        } else if (logger.isErrorEnabled()) {
            return Level.ERROR;
        }
        return Level.OFF;
    }

}
