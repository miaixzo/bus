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
package org.miaixz.bus.cron;

import java.util.TimeZone;

/**
 * Cron task configuration.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Configure {

    /**
     * Time zone
     */
    private TimeZone timezone = TimeZone.getDefault();
    /**
     * Whether to support second matching
     */
    private boolean matchSecond;
    /**
     * Whether to use daemon thread
     */
    private boolean daemon;

    /**
     * Default constructor.
     */
    public Configure() {

    }

    /**
     * Gets the time zone.
     *
     * @return The time zone, defaulting to {@link TimeZone#getDefault()}.
     */
    public TimeZone getTimeZone() {
        return this.timezone;
    }

    /**
     * Sets the time zone.
     *
     * @param timezone The time zone.
     * @return this {@link Configure} instance for chaining.
     */
    public Configure setTimeZone(final TimeZone timezone) {
        this.timezone = timezone;
        return this;
    }

    /**
     * Checks if second matching is enabled.
     *
     * @return {@code true} if enabled, {@code false} otherwise.
     */
    public boolean isMatchSecond() {
        return this.matchSecond;
    }

    /**
     * Sets whether to support second matching in cron expressions. Defaults to false.
     *
     * @param isMatchSecond {@code true} to enable, {@code false} to disable.
     * @return this {@link Configure} instance for chaining.
     */
    public Configure setMatchSecond(final boolean isMatchSecond) {
        this.matchSecond = isMatchSecond;
        return this;
    }

    /**
     * Checks if the thread is a daemon thread.
     *
     * @return {@code true} if daemon thread, {@code false} if non-daemon thread
     */
    public boolean isDaemon() {
        return this.daemon;
    }

    /**
     * Sets whether to use a daemon thread.
     *
     * @param daemon {@code true} for daemon thread, {@code false} for non-daemon thread
     * @return this {@link Configure} instance for chaining.
     */
    public Configure setDaemon(final boolean daemon) {
        this.daemon = daemon;
        return this;
    }

}
