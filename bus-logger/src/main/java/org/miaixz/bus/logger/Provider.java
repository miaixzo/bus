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
