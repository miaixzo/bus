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
package org.miaixz.bus.logger.magic.level;

/**
 * This interface defines methods for logging at the WARN level.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface Warn {

    /**
     * Checks if logging at the WARN level is enabled.
     *
     * @return {@code true} if WARN level is enabled, {@code false} otherwise.
     */
    boolean isWarnEnabled();

    /**
     * Logs a throwable at the WARN level.
     *
     * @param t the throwable to log.
     */
    void warn(Throwable t);

    /**
     * Logs a formatted message at the WARN level.
     *
     * @param format the message format.
     * @param args   the arguments for the message format.
     */
    void warn(String format, Object... args);

    /**
     * Logs a formatted message with a throwable at the WARN level.
     *
     * @param t      the throwable to log.
     * @param format the message format.
     * @param args   the arguments for the message format.
     */
    void warn(Throwable t, String format, Object... args);

    /**
     * Logs a formatted message with a throwable at the WARN level, specifying the fully qualified class name (FQCN).
     *
     * @param fqcn   the fully qualified class name of the logger.
     * @param t      the throwable to log.
     * @param format the message format.
     * @param args   the arguments for the message format.
     */
    void warn(String fqcn, Throwable t, String format, Object... args);

}
