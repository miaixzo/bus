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
package org.miaixz.bus.logger.magic.level;

/**
 * This interface defines methods for logging at the INFO level.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface Info {

    /**
     * Checks if logging at the INFO level is enabled.
     *
     * @return {@code true} if INFO level is enabled, {@code false} otherwise.
     */
    boolean isInfoEnabled();

    /**
     * Logs a throwable at the INFO level.
     *
     * @param t the throwable to log.
     */
    void info(Throwable t);

    /**
     * Logs a formatted message at the INFO level.
     *
     * @param format the message format.
     * @param args   the arguments for the message format.
     */
    void info(String format, Object... args);

    /**
     * Logs a formatted message with a throwable at the INFO level.
     *
     * @param t      the throwable to log.
     * @param format the message format.
     * @param args   the arguments for the message format.
     */
    void info(Throwable t, String format, Object... args);

    /**
     * Logs a formatted message with a throwable at the INFO level, specifying the fully qualified class name (FQCN).
     *
     * @param fqcn   the fully qualified class name of the logger.
     * @param t      the throwable to log.
     * @param format the message format.
     * @param args   the arguments for the message format.
     */
    void info(String fqcn, Throwable t, String format, Object... args);

}
