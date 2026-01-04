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
public enum Units {

    /**
     * One millisecond.
     */
    MS(1),
    /**
     * Number of milliseconds in one second.
     */
    SECOND(1000),
    /**
     * Number of milliseconds in one minute.
     */
    MINUTE(SECOND.getMillis() * 60),
    /**
     * Number of milliseconds in one hour.
     */
    HOUR(MINUTE.getMillis() * 60),
    /**
     * Number of milliseconds in one day.
     */
    DAY(HOUR.getMillis() * 24),
    /**
     * Number of milliseconds in one week.
     */
    WEEK(DAY.getMillis() * 7);

    /**
     * The number of milliseconds for this unit.
     */
    private final long millis;

    /**
     * Constructor for Units enum.
     *
     * @param millis The number of milliseconds for this unit.
     */
    Units(final long millis) {
        this.millis = millis;
    }

    /**
     * Converts {@link ChronoUnit} to the corresponding {@link Units}.
     *
     * @param unit The {@link ChronoUnit} to convert.
     * @return The corresponding {@link Units}, or {@code null} if the unit is not supported.
     */
    public static Units of(final ChronoUnit unit) {
        switch (unit) {
            case MICROS:
                return Units.MS;

            case SECONDS:
                return Units.SECOND;

            case MINUTES:
                return Units.MINUTE;

            case HOURS:
                return Units.HOUR;

            case DAYS:
                return Units.DAY;

            case WEEKS:
                return Units.WEEK;
        }
        return null;
    }

    /**
     * Converts {@link Units} to the corresponding {@link ChronoUnit}.
     *
     * @param unit The {@link Units} to convert.
     * @return The corresponding {@link ChronoUnit}.
     */
    public static ChronoUnit toChronoUnit(final Units unit) {
        switch (unit) {
            case MS:
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

    /**
     * Gets the number of milliseconds corresponding to this unit.
     *
     * @return The number of milliseconds.
     */
    public long getMillis() {
        return this.millis;
    }

    /**
     * Converts this {@link Units} to the corresponding {@link ChronoUnit}.
     *
     * @return The corresponding {@link ChronoUnit}.
     */
    public ChronoUnit toChronoUnit() {
        return Units.toChronoUnit(this);
    }

}
