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

import org.miaixz.bus.core.lang.ansi.Ansi4BitColor;
import org.miaixz.bus.core.lang.ansi.AnsiEncoder;
import org.miaixz.bus.core.xyz.ClassKit;
import org.miaixz.bus.core.xyz.DateKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.logger.Level;

import java.io.Serial;
import java.util.function.Function;

/**
 * A console logger that prints colorful messages using {@code System.out.println}.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class ColorLoggingProvider extends NormalLoggingProvider {

    @Serial
    private static final long serialVersionUID = 2852286792515L;

    /**
     * The color code for the class name in the console output.
     */
    private static final Ansi4BitColor COLOR_CLASSNAME = Ansi4BitColor.CYAN;

    /**
     * The color code for the timestamp in the console output.
     */
    private static final Ansi4BitColor COLOR_TIME = Ansi4BitColor.WHITE;

    /**
     * The default color code for normal information in the console output.
     */
    private static final Ansi4BitColor COLOR_NONE = Ansi4BitColor.DEFAULT;

    /**
     * A factory function that determines the color based on the logging level.
     */
    private static Function<Level, Ansi4BitColor> colorFactory = (level -> {
        switch (level) {
            case DEBUG, INFO:
                return Ansi4BitColor.GREEN;

            case WARN:
                return Ansi4BitColor.YELLOW;

            case ERROR:
                return Ansi4BitColor.RED;

            case TRACE:
                return Ansi4BitColor.MAGENTA;

            default:
                return COLOR_NONE;
        }
    });

    /**
     * Constructs a new {@code ColorLoggingProvider} for the specified name.
     *
     * @param name the name of the logger (usually the class name).
     */
    public ColorLoggingProvider(final String name) {
        super(name);
    }

    /**
     * Constructs a new {@code ColorLoggingProvider} for the specified class.
     *
     * @param clazz the class for which to create the logger.
     */
    public ColorLoggingProvider(final Class<?> clazz) {
        super(clazz);
    }

    /**
     * Sets the color factory, which defines different colors based on the logging level.
     *
     * @param colorFactory the function that provides the color for a given level.
     */
    public static void setColorFactory(final Function<Level, Ansi4BitColor> colorFactory) {
        ColorLoggingProvider.colorFactory = colorFactory;
    }

    /**
     * Logs a message at the specified level with ANSI color formatting.
     *
     * @param fqcn   the fully qualified class name of the caller
     * @param level  the logging level
     * @param t      the throwable to log
     * @param format the message format string
     * @param args   the arguments to format into the message string
     */
    @Override
    public synchronized void log(
            final String fqcn,
            final Level level,
            final Throwable t,
            final String format,
            final Object... args) {
        if (!isEnabled(level)) {
            return;
        }

        // Format the log message with ANSI color codes.
        final String template = AnsiEncoder.encode(
                COLOR_TIME,
                "[%s]",
                colorFactory.apply(level),
                "[%-5s]%s",
                COLOR_CLASSNAME,
                "%-30s: ",
                COLOR_NONE,
                "%s%n");
        // Print the formatted message to the console.
        System.out.format(
                template,
                DateKit.formatNow(),
                level.name(),
                " - ",
                ClassKit.getShortClassName(getName()),
                StringKit.format(format, args));
        // Print the stack trace if a throwable is provided.
        if (t != null) {
            t.printStackTrace();
        }
    }

    /**
     * Gets the current logging level.
     *
     * @return the current logging level, or {@link Level#OFF} if it cannot be determined
     */
    @Override
    public Level getLevel() {
        if (isTraceEnabled()) {
            return Level.TRACE;
        } else if (isDebugEnabled()) {
            return Level.DEBUG;
        } else if (isInfoEnabled()) {
            return Level.INFO;
        } else if (isWarnEnabled()) {
            return Level.WARN;
        } else if (isErrorEnabled()) {
            return Level.ERROR;
        } else {
            return Level.OFF;
        }
    }

}
