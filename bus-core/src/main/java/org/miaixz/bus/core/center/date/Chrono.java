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
package org.miaixz.bus.core.center.date;

import java.time.temporal.ChronoUnit;

/**
 * Date and time units, each unit is based on milliseconds.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public enum Chrono {

    /**
     * One millisecond.
     */
    MILLISECOND(1, "毫秒"),
    /**
     * Number of milliseconds in one second.
     */
    SECOND(1000, "秒"),
    /**
     * Number of milliseconds in one minute.
     */
    MINUTE(SECOND.getMillis() * 60, "分"),
    /**
     * Number of milliseconds in one hour.
     */
    HOUR(MINUTE.getMillis() * 60, "小时"),
    /**
     * Number of milliseconds in one day.
     */
    DAY(HOUR.getMillis() * 24, "天"),
    /**
     * Number of milliseconds in one week.
     */
    WEEK(DAY.getMillis() * 7, "周");

    /**
     * The number of milliseconds for this unit.
     */
    private final long millis;

    /**
     * The name of the level.
     */
    private final String name;

    /**
     * Constructor for Chrono enum.
     *
     * @param millis The number of milliseconds for this unit.
     */
    Chrono(final long millis, final String name) {
        this.millis = millis;
        this.name = name;
    }

    /**
     * Gets the number of milliseconds corresponding to this unit.
     *
     * @return The number of milliseconds.
     */
    public long getMillis() {
        return this.millis;
    }

    /**
     * Gets the name of the level.
     *
     * @return The name of the level.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Converts {@link ChronoUnit} to the corresponding {@link Chrono}.
     *
     * @param chrono The {@link ChronoUnit} to convert.
     * @return The corresponding {@link Chrono}, or {@code null} if the unit is not supported.
     */
    public static Chrono of(final ChronoUnit chrono) {
        switch (chrono) {
            case MICROS:
                return Chrono.MILLISECOND;

            case SECONDS:
                return Chrono.SECOND;

            case MINUTES:
                return Chrono.MINUTE;

            case HOURS:
                return Chrono.HOUR;

            case DAYS:
                return Chrono.DAY;

            case WEEKS:
                return Chrono.WEEK;
        }
        return null;
    }

    /**
     * Converts this {@link Chrono} to the corresponding {@link ChronoUnit}.
     *
     * @return The corresponding {@link ChronoUnit}.
     */
    public ChronoUnit of() {
        return Chrono.of(this);
    }

    /**
     * Converts {@link Chrono} to the corresponding {@link ChronoUnit}.
     *
     * @param chrono The {@link Chrono} to convert.
     * @return The corresponding {@link ChronoUnit}.
     */
    public static ChronoUnit of(final Chrono chrono) {
        switch (chrono) {
            case MILLISECOND:
                return ChronoUnit.MICROS;

            case SECOND:
                return ChronoUnit.SECONDS;

            case MINUTE:
                return ChronoUnit.MINUTES;

            case HOUR:
                return ChronoUnit.HOURS;

            case DAY:
                return ChronoUnit.DAYS;

            case WEEK:
                return ChronoUnit.WEEKS;
        }
        return null;
    }

}
