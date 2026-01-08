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
package org.miaixz.bus.logger;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.CallerKit;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * Static logger class for logging without introducing a logger instance.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Logger {

    /**
     * The fully qualified class name, used to correct the location of the wrong line number.
     */
    private static final String FQCN = Logger.class.getName();

    /**
     * The default width for the aligned log prefix.
     */
    private static final int WIDTH = Normal._10 + Normal._5;

    /**
     * Default constructor.
     */
    public Logger() {

    }

    /**
     * Logs a message at the TRACE level.
     *
     * @param format the format string
     * @param args   the arguments to be substituted into the format string
     */
    public static void trace(final String format, final Object... args) {
        trace(Registry.get(CallerKit.getCallers()), format, args);
    }

    /**
     * Logs a message at the TRACE level. (Provider-specific)
     *
     * @param provider the logger provider
     * @param format   the format string
     * @param args     the arguments to be substituted into the format string
     */
    public static void trace(final Provider provider, final String format, final Object... args) {
        provider.trace(FQCN, null, format, args);
    }

    /**
     * Logs an aligned message at the TRACE level using the default prefix width.
     *
     * @param isEntry The direction: true (==&gt;) for entry/inbound, false (&lt;==) for exit/outbound.
     * @param tag     The log tag (e.g., "Vetting").
     * @param message The log message with SLF4J placeholders ({}).
     * @param args    The arguments for the placeholders.
     */
    public static void trace(boolean isEntry, String tag, String message, Object... args) {
        // Must NOT call the other overload to avoid breaking the stack trace
        String formattedMessage = build(WIDTH, tag, isEntry) + Symbol.SPACE + message;
        trace(Registry.get(CallerKit.getCallers()), formattedMessage, args);
    }

    /**
     * Logs an aligned message at the TRACE level.
     *
     * @param isEntry The direction: true (==&gt;) for entry/inbound, false (&lt;==) for exit/outbound.
     * @param tag     The log tag (e.g., "Vetting").
     * @param width   The desired total width of the prefix.
     * @param message The log message with SLF4J placeholders ({}).
     * @param args    The arguments for the placeholders.
     */
    public static void trace(boolean isEntry, String tag, int width, String message, Object... args) {
        String formattedMessage = build(width, tag, isEntry) + Symbol.SPACE + message;
        trace(Registry.get(CallerKit.getCallers()), formattedMessage, args);
    }

    /**
     * Logs a message at the DEBUG level.
     *
     * @param format the format string
     * @param args   the arguments to be substituted into the format string
     */
    public static void debug(final String format, final Object... args) {
        debug(Registry.get(CallerKit.getCallers()), format, args);
    }

    /**
     * Logs a message at the DEBUG level. (Provider-specific)
     *
     * @param provider the logger provider
     * @param format   the format string
     * @param args     the arguments to be substituted into the format string
     */
    public static void debug(final Provider provider, final String format, final Object... args) {
        provider.debug(FQCN, null, format, args);
    }

    /**
     * Logs an aligned message at the DEBUG level using the default prefix width.
     *
     * @param isEntry The direction: true (==&gt;) for entry/inbound, false (&lt;==) for exit/outbound.
     * @param tag     The log tag (e.g., "Vetting").
     * @param message The log message with SLF4J placeholders ({}).
     * @param args    The arguments for the placeholders.
     */
    public static void debug(boolean isEntry, String tag, String message, Object... args) {
        // Must NOT call the other overload to avoid breaking the stack trace
        String formattedMessage = build(WIDTH, tag, isEntry) + Symbol.SPACE + message;
        debug(Registry.get(CallerKit.getCallers()), formattedMessage, args);
    }

    /**
     * Logs an aligned message at the DEBUG level.
     *
     * @param isEntry The direction: true (==&gt;) for entry/inbound, false (&lt;==) for exit/outbound.
     * @param tag     The log tag (e.g., "Vetting").
     * @param width   The desired total width of the prefix.
     * @param message The log message with SLF4J placeholders ({}).
     * @param args    The arguments for the placeholders.
     */
    public static void debug(boolean isEntry, String tag, int width, String message, Object... args) {
        String formattedMessage = build(width, tag, isEntry) + Symbol.SPACE + message;
        debug(Registry.get(CallerKit.getCallers()), formattedMessage, args);
    }

    /**
     * Logs a message at the INFO level.
     *
     * @param format the format string
     * @param args   the arguments to be substituted into the format string
     */
    public static void info(final String format, final Object... args) {
        info(Registry.get(CallerKit.getCallers()), format, args);
    }

    /**
     * Logs a message at the INFO level. (Provider-specific)
     *
     * @param provider the logger provider
     * @param format   the format string
     * @param args     the arguments to be substituted into the format string
     */
    public static void info(final Provider provider, final String format, final Object... args) {
        provider.info(FQCN, null, format, args);
    }

    /**
     * Logs an aligned message at the INFO level using the default prefix width.
     *
     * @param isEntry The direction: true (==&gt;) for entry/inbound, false (&lt;==) for exit/outbound.
     * @param tag     The log tag (e.g., "Vetting").
     * @param message The log message with SLF4J placeholders ({}).
     * @param args    The arguments for the placeholders.
     */
    public static void info(boolean isEntry, String tag, String message, Object... args) {
        // Must NOT call the other overload to avoid breaking the stack trace
        String formattedMessage = build(WIDTH, tag, isEntry) + Symbol.SPACE + message;
        info(Registry.get(CallerKit.getCallers()), formattedMessage, args);
    }

    /**
     * Logs an aligned message at the INFO level.
     *
     * @param isEntry The direction: true (==&gt;) for entry/inbound, false (&lt;==) for exit/outbound.
     * @param tag     The log tag (e.g., "Vetting").
     * @param width   The desired total width of the prefix.
     * @param message The log message with SLF4J placeholders ({}).
     * @param args    The arguments for the placeholders.
     */
    public static void info(boolean isEntry, String tag, int width, String message, Object... args) {
        String formattedMessage = build(width, tag, isEntry) + Symbol.SPACE + message;
        info(Registry.get(CallerKit.getCallers()), formattedMessage, args);
    }

    /**
     * Logs a message at the WARN level.
     *
     * @param format the format string
     * @param args   the arguments to be substituted into the format string
     */
    public static void warn(final String format, final Object... args) {
        warn(Registry.get(CallerKit.getCallers()), format, args);
    }

    /**
     * Logs a message at the WARN level.
     *
     * @param e      the exception to log
     * @param format the format string
     * @param args   the arguments to be substituted into the format string
     */
    public static void warn(final Throwable e, final String format, final Object... args) {
        warn(Registry.get(CallerKit.getCallers()), e, StringKit.format(format, args));
    }

    /**
     * Logs a message at the WARN level. (Provider-specific)
     *
     * @param provider the logger provider
     * @param format   the format string
     * @param args     the arguments to be substituted into the format string
     */
    public static void warn(final Provider provider, final String format, final Object... args) {
        warn(provider, null, format, args);
    }

    /**
     * Logs a message at the WARN level. (Provider-specific)
     *
     * @param provider the logger provider
     * @param e        the exception to log
     * @param format   the format string
     * @param args     the arguments to be substituted into the format string
     */
    public static void warn(final Provider provider, final Throwable e, final String format, final Object... args) {
        provider.warn(FQCN, e, format, args);
    }

    /**
     * Logs an aligned message at the WARN level using the default prefix width.
     *
     * @param isEntry The direction: true (==&gt;) for entry/inbound, false (&lt;==) for exit/outbound.
     * @param tag     The log tag (e.g., "Vetting").
     * @param message The log message with SLF4J placeholders ({}).
     * @param args    The arguments for the placeholders.
     */
    public static void warn(boolean isEntry, String tag, String message, Object... args) {
        // Must NOT call the other overload to avoid breaking the stack trace
        String formattedMessage = build(WIDTH, tag, isEntry) + Symbol.SPACE + message;
        warn(Registry.get(CallerKit.getCallers()), formattedMessage, args);
    }

    /**
     * Logs an aligned message at the WARN level.
     *
     * @param isEntry The direction: true (==&gt;) for entry/inbound, false (&lt;==) for exit/outbound.
     * @param tag     The log tag (e.g., "Vetting").
     * @param width   The desired total width of the prefix.
     * @param message The log message with SLF4J placeholders ({}).
     * @param args    The arguments for the placeholders.
     */
    public static void warn(boolean isEntry, String tag, int width, String message, Object... args) {
        String formattedMessage = build(width, tag, isEntry) + Symbol.SPACE + message;
        warn(Registry.get(CallerKit.getCallers()), formattedMessage, args);
    }

    /**
     * Logs a message at the ERROR level.
     *
     * @param e the exception to log
     */
    public static void error(final Throwable e) {
        error(Registry.get(CallerKit.getCallers()), e);
    }

    /**
     * Logs a message at the ERROR level.
     *
     * @param format the format string
     * @param args   the arguments to be substituted into the format string
     */
    public static void error(final String format, final Object... args) {
        error(Registry.get(CallerKit.getCallers()), format, args);
    }

    /**
     * Logs a message at the ERROR level.
     *
     * @param e      the exception to log
     * @param format the format string
     * @param args   the arguments to be substituted into the format string
     */
    public static void error(final Throwable e, final String format, final Object... args) {
        error(Registry.get(CallerKit.getCallers()), e, format, args);
    }

    /**
     * Logs a message at the ERROR level. (Provider-specific)
     *
     * @param provider the logger provider
     * @param e        the exception to log
     */
    public static void error(final Provider provider, final Throwable e) {
        error(provider, e, e.getMessage());
    }

    /**
     * Logs a message at the ERROR level. (Provider-specific)
     *
     * @param provider the logger provider
     * @param format   the format string
     * @param args     the arguments to be substituted into the format string
     */
    public static void error(final Provider provider, final String format, final Object... args) {
        error(provider, null, format, args);
    }

    /**
     * Logs a message at the ERROR level. (Provider-specific)
     *
     * @param provider the logger provider
     * @param e        the exception to log
     * @param format   the format string
     * @param args     the arguments to be substituted into the format string
     */
    public static void error(final Provider provider, final Throwable e, final String format, final Object... args) {
        provider.error(FQCN, e, format, args);
    }

    /**
     * Logs an aligned message at the ERROR level using the default prefix width.
     *
     * @param isEntry The direction: true (==&gt;) for entry/inbound, false (&lt;==) for exit/outbound.
     * @param tag     The log tag (e.g., "Vetting").
     * @param message The log message with SLF4J placeholders ({}).
     * @param args    The arguments for the placeholders.
     */
    public static void error(boolean isEntry, String tag, String message, Object... args) {
        // Must NOT call the other overload to avoid breaking the stack trace
        String formattedMessage = build(WIDTH, tag, isEntry) + Symbol.SPACE + message;
        error(Registry.get(CallerKit.getCallers()), formattedMessage, args);
    }

    /**
     * Logs an aligned message at the ERROR level.
     *
     * @param isEntry The direction: true (==&gt;) for entry/inbound, false (&lt;==) for exit/outbound.
     * @param tag     The log tag (e.g., "Vetting").
     * @param width   The desired total width of the prefix.
     * @param message The log message with SLF4J placeholders ({}).
     * @param args    The arguments for the placeholders.
     */
    public static void error(boolean isEntry, String tag, int width, String message, Object... args) {
        String formattedMessage = build(width, tag, isEntry) + Symbol.SPACE + message;
        error(Registry.get(CallerKit.getCallers()), formattedMessage, args);
    }

    /**
     * Logs a message with the given level.
     *
     * @param level  the logging level
     * @param t      the throwable to log
     * @param format the format string
     * @param args   the arguments to be substituted into the format string
     */
    public static void log(final Level level, final Throwable t, final String format, final Object... args) {
        Registry.get(CallerKit.getCallers()).log(FQCN, level, t, format, args);
    }

    /**
     * Gets the current logging level.
     *
     * @return the current logging level, or {@link Level#OFF} if it cannot be determined
     */
    public static Level getLevel() {
        Provider provider = Registry.get(CallerKit.getCallers());
        return provider != null ? provider.getLevel() : Level.OFF;
    }

    /**
     * Sets the logging level.
     *
     * @param level the logging level to set
     * @throws UnsupportedOperationException if the underlying logging framework does not support dynamic level setting
     */
    public static void setLevel(Level level) {
        Provider provider = Registry.get(CallerKit.getCallers());
        if (provider != null) {
            provider.setLevel(level);
        }
    }

    /**
     * Gets the underlying logger factory class.
     *
     * @return the logger factory class, e.g., {@code org.jboss.logging.Logger}, or {@code null} if it cannot be
     *         determined
     */
    public static Class<?> getFactory() {
        Factory factory = Holder.getFactory();
        if (factory == null) {
            return null;
        }

        // If it cannot be obtained directly, try to infer it from the factory name
        String factoryName = factory.getName();
        if (factoryName.contains("org.jboss.logging.Logger")) {
            try {
                return Class.forName("org.jboss.logging.Logger");
            } catch (ClassNotFoundException ex) {
                // Ignore exceptions and continue trying other methods
            }
        } else if (factoryName.contains("org.slf4j.Logger")) {
            try {
                return Class.forName("org.slf4j.Logger");
            } catch (ClassNotFoundException ex) {
                // Ignore exceptions and continue trying other methods
            }
        } else if (factoryName.contains("org.apache.logging.log4j.Logger")) {
            try {
                return Class.forName("org.apache.logging.log4j.Logger");
            } catch (ClassNotFoundException ex) {
                // Ignore exceptions and continue trying other methods
            }
        } else if (factoryName.contains("java.util.logging.Logger")) {
            try {
                return Class.forName("java.util.logging.Logger");
            } catch (ClassNotFoundException ex) {
                // Ignore exceptions and continue trying other methods
            }
        } else if (factoryName.contains("org.apache.commons.logging.Log")) {
            try {
                return Class.forName("org.apache.commons.logging.Log");
            } catch (ClassNotFoundException ex) {
                // Ignore exceptions and continue trying other methods
            }
        } else if (factoryName.contains("org.tinylog.Logger")) {
            try {
                return Class.forName("org.tinylog.Logger");
            } catch (ClassNotFoundException ex) {
                // Ignore exceptions and continue trying other methods
            }
        }

        return null;
    }

    /**
     * Gets the current logger provider.
     *
     * @return the current logger provider, or {@code null} if none is available
     */
    public static Provider getProvider() {
        return Registry.get(CallerKit.getCallers());
    }

    /**
     * Checks if the specified logging level is enabled.
     *
     * @param level the logging level to check
     * @return {@code true} if the specified level is enabled, {@code false} otherwise
     */
    public static boolean isEnabled(Level level) {
        return getProvider().isEnabled(level);
    }

    /**
     * Checks if the TRACE level is enabled.
     *
     * @return {@code true} if TRACE is enabled, {@code false} otherwise
     */
    public static boolean isTraceEnabled() {
        return getProvider().isTraceEnabled();
    }

    /**
     * Checks if the DEBUG level is enabled.
     *
     * @return {@code true} if DEBUG is enabled, {@code false} otherwise
     */
    public static boolean isDebugEnabled() {
        return getProvider().isDebugEnabled();
    }

    /**
     * Checks if the INFO level is enabled.
     *
     * @return {@code true} if INFO is enabled, {@code false} otherwise
     */
    public static boolean isInfoEnabled() {
        return getProvider().isInfoEnabled();
    }

    /**
     * Checks if the WARN level is enabled.
     *
     * @return {@code true} if WARN is enabled, {@code false} otherwise
     */
    public static boolean isWarnEnabled() {
        return getProvider().isWarnEnabled();
    }

    /**
     * Checks if the ERROR level is enabled.
     *
     * @return {@code true} if ERROR is enabled, {@code false} otherwise
     */
    public static boolean isErrorEnabled() {
        return getProvider().isErrorEnabled();
    }

    /**
     * Builds a standardized, padded log prefix.
     *
     * @param width   The desired total width of the prefix (e.g., 20).
     * @param tag     The log tag (e.g., "Vetting", "Limiter").
     * @param isEntry The direction: true for '==&gt;' (entry), false for '&lt;==' (exit).
     * @return The formatted prefix string, e.g., "==&gt; Vetting:"
     */
    private static String build(int width, String tag, boolean isEntry) {
        String arrow = isEntry ? Symbol.EQUAL + Symbol.EQUAL + Symbol.GT : Symbol.LT + Symbol.EQUAL + Symbol.EQUAL;

        // Calculate the length of non-padded content: "==>" + " " (space between arrow and tag) + "Tag" + ":"
        int fixedContentLength = arrow.length() + 1 + tag.length() + 1;

        int paddingSize = width - fixedContentLength;

        if (paddingSize <= 0) {
            // If the total length is insufficient, ensure at least one space
            //
            // *** FIX: Use {} placeholders for StringKit.format ***
            return StringKit.format("{} {}:", arrow, tag);
        }

        // " ".repeat(paddingSize) will create the required number of spaces
        String padding = Symbol.SPACE.repeat(paddingSize);

        // Format as: "==>" + "[...padding...]" + " " + "Tag" + ":"
        // Note: Padding is placed between the arrow and the tag to align the colons
        //
        // *** FIX: Use {} placeholders for StringKit.format ***
        return StringKit.format("{}{} {}:", arrow, padding, tag);
    }

}
