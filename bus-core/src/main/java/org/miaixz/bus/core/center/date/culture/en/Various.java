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
package org.miaixz.bus.core.center.date.culture.en;

import java.util.Calendar;

/**
 * Enumeration for various parts of a date, corresponding to {@link Calendar} fields.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public enum Various {

    /**
     * Era
     *
     * @see Calendar#ERA
     */
    ERA(Calendar.ERA),
    /**
     * Year
     *
     * @see Calendar#YEAR
     */
    YEAR(Calendar.YEAR),
    /**
     * Month
     *
     * @see Calendar#MONTH
     */
    MONTH(Calendar.MONTH),
    /**
     * Week in year
     *
     * @see Calendar#WEEK_OF_YEAR
     */
    WEEK_OF_YEAR(Calendar.WEEK_OF_YEAR),
    /**
     * Week in month
     *
     * @see Calendar#WEEK_OF_MONTH
     */
    WEEK_OF_MONTH(Calendar.WEEK_OF_MONTH),
    /**
     * Day in month
     *
     * @see Calendar#DAY_OF_MONTH
     */
    DAY_OF_MONTH(Calendar.DAY_OF_MONTH),
    /**
     * Day in year
     *
     * @see Calendar#DAY_OF_YEAR
     */
    DAY_OF_YEAR(Calendar.DAY_OF_YEAR),
    /**
     * Day of week, 1 for Sunday, 2 for Monday
     *
     * @see Calendar#DAY_OF_WEEK
     */
    DAY_OF_WEEK(Calendar.DAY_OF_WEEK),
    /**
     * Day of week in month
     *
     * @see Calendar#DAY_OF_WEEK_IN_MONTH
     */
    DAY_OF_WEEK_IN_MONTH(Calendar.DAY_OF_WEEK_IN_MONTH),
    /**
     * AM or PM
     *
     * @see Calendar#AM_PM
     */
    AM_PM(Calendar.AM_PM),
    /**
     * Hour, for 12-hour clock
     *
     * @see Calendar#HOUR
     */
    HOUR(Calendar.HOUR),
    /**
     * Hour, for 24-hour clock
     *
     * @see Calendar#HOUR_OF_DAY
     */
    HOUR_OF_DAY(Calendar.HOUR_OF_DAY),
    /**
     * Minute
     *
     * @see Calendar#MINUTE
     */
    MINUTE(Calendar.MINUTE),
    /**
     * Second
     *
     * @see Calendar#SECOND
     */
    SECOND(Calendar.SECOND),
    /**
     * Millisecond
     *
     * @see Calendar#MILLISECOND
     */
    MILLISECOND(Calendar.MILLISECOND);

    /**
     * The corresponding value in {@link Calendar}.
     */
    private final int value;

    /**
     * Constructor for Various enum.
     *
     * @param value The corresponding value in {@link Calendar}.
     */
    Various(final int value) {
        this.value = value;
    }

    /**
     * Converts a {@link Calendar} field int value to a {@link Various} enum object.
     *
     * @param calendarPartIntValue The int value of the field in Calendar.
     * @return The corresponding {@link Various} enum.
     */
    public static Various of(final int calendarPartIntValue) {
        switch (calendarPartIntValue) {
            case Calendar.ERA:
                return ERA;

            case Calendar.YEAR:
                return YEAR;

            case Calendar.MONTH:
                return MONTH;

            case Calendar.WEEK_OF_YEAR:
                return WEEK_OF_YEAR;

            case Calendar.WEEK_OF_MONTH:
                return WEEK_OF_MONTH;

            case Calendar.DAY_OF_MONTH:
                return DAY_OF_MONTH;

            case Calendar.DAY_OF_YEAR:
                return DAY_OF_YEAR;

            case Calendar.DAY_OF_WEEK:
                return DAY_OF_WEEK;

            case Calendar.DAY_OF_WEEK_IN_MONTH:
                return DAY_OF_WEEK_IN_MONTH;

            case Calendar.AM_PM:
                return AM_PM;

            case Calendar.HOUR:
                return HOUR;

            case Calendar.HOUR_OF_DAY:
                return HOUR_OF_DAY;

            case Calendar.MINUTE:
                return MINUTE;

            case Calendar.SECOND:
                return SECOND;

            case Calendar.MILLISECOND:
                return MILLISECOND;

            default:
                return null;
        }
    }

    /**
     * Gets the corresponding value in {@link Calendar}.
     *
     * @return The corresponding value in {@link Calendar}.
     */
    public int getValue() {
        return this.value;
    }

}
