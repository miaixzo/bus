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
 * @since Java 17+
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
