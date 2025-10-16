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
package org.miaixz.bus.logger;

import org.miaixz.bus.core.xyz.CallerKit;
import org.miaixz.bus.logger.magic.level.*;
import org.miaixz.bus.logger.magic.level.Error;

/**
 * Unified interface for logging.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface Provider extends Trace, Debug, Info, Warn, Error {

    /**
     * Gets the logger provider for the specified class.
     *
     * @param clazz the class to get the logger for.
     * @return the {@link Provider} instance.
     */
    static Provider get(final Class<?> clazz) {
        return Registry.get(clazz);
    }

    /**
     * Gets the logger provider for the specified name.
     *
     * @param name the custom name of the logger.
     * @return the {@link Provider} instance.
     */
    static Provider get(final String name) {
        return Registry.get(name);
    }

    /**
     * Gets the logger provider for the calling class.
     *
     * @return the {@link Provider} instance.
     */
    static Provider get() {
        return Registry.get(CallerKit.getCallers());
    }

    /**
     * Gets the name of this logger.
     *
     * @return the name of this logger.
     */
    String getName();

    /**
     * Checks whether this logger is enabled for the specified level.
     *
     * @param level the level to check.
     * @return {@code true} if this logger is enabled for the specified level, {@code false} otherwise.
     */
    boolean isEnabled(Level level);

    /**
     * Logs a message at the specified level.
     *
     * @param level  the logging level.
     * @param format the message format.
     * @param args   the arguments for the message format.
     */
    void log(Level level, String format, Object... args);

    /**
     * Logs a message with a throwable at the specified level.
     *
     * @param level  the logging level.
     * @param t      the throwable to log.
     * @param format the message format.
     * @param args   the arguments for the message format.
     */
    void log(Level level, Throwable t, String format, Object... args);

    /**
     * Logs a message with a throwable at the specified level, providing the fully qualified class name (FQCN).
     *
     * @param fqcn   the fully qualified class name of the logger.
     * @param level  the logging level.
     * @param t      the throwable to log.
     * @param format the message format.
     * @param args   the arguments for the message format.
     */
    void log(String fqcn, Level level, Throwable t, String format, Object... args);

    /**
     * Gets the current logging level.
     *
     * @return the current logging level, or {@link Level#OFF} if it cannot be determined.
     */
    default Level getLevel() {
        if (isEnabled(Level.TRACE)) {
            return Level.TRACE;
        } else if (isEnabled(Level.DEBUG)) {
            return Level.DEBUG;
        } else if (isEnabled(Level.INFO)) {
            return Level.INFO;
        } else if (isEnabled(Level.WARN)) {
            return Level.WARN;
        } else if (isEnabled(Level.ERROR)) {
            return Level.ERROR;
        } else {
            return Level.OFF;
        }
    }

    /**
     * Sets the logging level. Note: The default implementation is empty. Concrete implementations should override this
     * method if they support dynamic level setting.
     *
     * @param level the logging level to set.
     * @throws UnsupportedOperationException if the underlying logging framework does not support dynamic level setting.
     */
    default void setLevel(Level level) {
        // The default implementation is empty, and the specific implementation class can be overridden
    }

}
