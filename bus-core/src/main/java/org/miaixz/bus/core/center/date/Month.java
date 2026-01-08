/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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

import java.time.format.TextStyle;
import java.util.Calendar;
import java.util.Locale;

import org.miaixz.bus.core.lang.Assert;

/**
 * Enumeration for months, corresponding to the int values of months in {@link Calendar}.
 *
 * @author Kimi Liu
 * @see Calendar#JANUARY
 * @see Calendar#FEBRUARY
 * @see Calendar#MARCH
 * @see Calendar#APRIL
 * @see Calendar#MAY
 * @see Calendar#JUNE
 * @see Calendar#JULY
 * @see Calendar#AUGUST
 * @see Calendar#SEPTEMBER
 * @see Calendar#OCTOBER
 * @see Calendar#NOVEMBER
 * @see Calendar#DECEMBER
 * @see Calendar#UNDECIMBER
 * @since Java 17+
 */
public enum Month {

    /**
     * January
     */
    JANUARY(Calendar.JANUARY),
    /**
     * February
     */
    FEBRUARY(Calendar.FEBRUARY),
    /**
     * March
     */
    MARCH(Calendar.MARCH),
    /**
     * April
     */
    APRIL(Calendar.APRIL),
    /**
     * May
     */
    MAY(Calendar.MAY),
    /**
     * June
     */
    JUNE(Calendar.JUNE),
    /**
     * July
     */
    JULY(Calendar.JULY),
    /**
     * August
     */
    AUGUST(Calendar.AUGUST),
    /**
     * September
     */
    SEPTEMBER(Calendar.SEPTEMBER),
    /**
     * October
     */
    OCTOBER(Calendar.OCTOBER),
    /**
     * November
     */
    NOVEMBER(Calendar.NOVEMBER),
    /**
     * December
     */
    DECEMBER(Calendar.DECEMBER),
    /**
     * Undecimber (thirteenth month), only used for the lunar calendar.
     */
    UNDECIMBER(Calendar.UNDECIMBER);

    /**
     * Array of all {@link Month} enum constants.
     */
    private static final Month[] ENUMS = Month.values();

    /**
     * The corresponding value, see {@link Calendar}.
     */
    private final int value;

    /**
     * Constructor for Month enum.
     *
     * @param value The corresponding value, see {@link Calendar}.
     */
    Month(final int value) {
        this.value = value;
    }

    /**
     * Converts a {@link Calendar} month int value to a {@link Month} enum object. Returns {@code null} if not found.
     *
     * @param calendarMonthIntValue The int value of the month in Calendar, starting from 0.
     * @return The corresponding {@link Month} enum.
     * @see Calendar#JANUARY
     * @see Calendar#FEBRUARY
     * @see Calendar#MARCH
     * @see Calendar#APRIL
     * @see Calendar#MAY
     * @see Calendar#JUNE
     * @see Calendar#JULY
     * @see Calendar#AUGUST
     * @see Calendar#SEPTEMBER
     * @see Calendar#OCTOBER
     * @see Calendar#NOVEMBER
     * @see Calendar#DECEMBER
     * @see Calendar#UNDECIMBER
     */
    public static Month of(final int calendarMonthIntValue) {
        if (calendarMonthIntValue >= ENUMS.length || calendarMonthIntValue < 0) {
            return null;
        }
        return ENUMS[calendarMonthIntValue];
    }

    /**
     * Parses an alias into a {@link Month} object. Aliases like "jan" or "JANUARY" are case-insensitive.
     *
     * @param name The alias value.
     * @return The {@link Month} enum, non-null.
     * @throws IllegalArgumentException If the alias does not correspond to a valid enum.
     */
    public static Month of(final String name) throws IllegalArgumentException {
        if (null != name && name.length() > 1) {
            switch (Character.toLowerCase(name.charAt(0))) {
                case 'a':
                    switch (Character.toLowerCase(name.charAt(1))) {
                        case 'p':
                            return APRIL; // april

                        case 'u':
                            return AUGUST; // august
                    }
                    break;

                case 'j':
                    if (Character.toLowerCase(name.charAt(1)) == 'a') {
                        return JANUARY; // january
                    }
                    switch (Character.toLowerCase(name.charAt(2))) {
                        case 'n':
                            return JUNE; // june

                        case 'l':
                            return JULY; // july
                    }
                    break;

                case 'f':
                    return FEBRUARY; // february

                case 'm':
                    switch (Character.toLowerCase(name.charAt(2))) {
                        case 'r':
                            return MARCH; // march

                        case 'y':
                            return MAY; // may
                    }
                    break;

                case 's':
                    return SEPTEMBER; // september

                case 'o':
                    return OCTOBER; // october

                case 'n':
                    return NOVEMBER; // november

                case 'd':
                    return DECEMBER; // december

                case '一':
                    return JANUARY;

                case '二':
                    return FEBRUARY;

                case '三':
                    return MARCH;

                case '四':
                    return APRIL;

                case '五':
                    return MAY;

                case '六':
                    return JUNE;

                case '七':
                    return JULY;

                case '八':
                    return AUGUST;

                case '九':
                    return SEPTEMBER;

                case '十':
                    switch (Character.toLowerCase(name.charAt(1))) {
                        case '一':
                            return NOVEMBER;

                        case '二':
                            return DECEMBER;
                    }
                    return OCTOBER;
            }
        }

        throw new IllegalArgumentException("Invalid Month name: " + name);
    }

    /**
     * Converts a {@link java.time.Month} object to a {@link Month} enum object.
     *
     * @param month The {@link java.time.Month} object.
     * @return The corresponding {@link Month} enum.
     */
    public static Month of(final java.time.Month month) {
        return of(month.ordinal());
    }

    /**
     * Gets the last day of the specified month.
     *
     * @param month      The month, starting from 0 (0 for January).
     * @param isLeapYear Whether it is a leap year. Leap year only affects February.
     * @return The last day of the month, which can be 28, 29, 30, or 31.
     */
    public static int getLastDay(final int month, final boolean isLeapYear) {
        final Month of = of(month);
        Assert.notNull(of, "Invalid Month base 0: " + month);
        return of.getLastDay(isLeapYear);
    }

    /**
     * Gets the corresponding value in {@link Calendar}. This value starts from 0, where 0 represents January.
     *
     * @return The corresponding month value in {@link Calendar}, counted from 0.
     */
    public int getValue() {
        return this.value;
    }

    /**
     * Gets the month value, which corresponds to {@link java.time.Month}. This value starts from 1, where 1 represents
     * January.
     *
     * @return The month value, corresponding to {@link java.time.Month}, counted from 1.
     * @throws IllegalArgumentException if {@link #UNDECIMBER} is used, as it is unsupported.
     */
    public int getIsoValue() {
        Assert.isFalse(this == UNDECIMBER, "Unsupported Undecimber field");
        return getValue() + 1;
    }

    /**
     * Gets the value of the last day of this month. Does not support {@link #UNDECIMBER}.
     *
     * @param isLeapYear True if it is a leap year, false otherwise.
     * @return The value of the last day of this month.
     */
    public int getLastDay(final boolean isLeapYear) {
        switch (this) {
            case FEBRUARY:
                return isLeapYear ? 29 : 28;

            case APRIL:
            case JUNE:
            case SEPTEMBER:
            case NOVEMBER:
                return 30;

            default:
                return 31;
        }
    }

    /**
     * Converts this {@link Month} enum to a {@link java.time.Month} object.
     *
     * @return The corresponding {@link java.time.Month} object.
     */
    public java.time.Month toJdkMonth() {
        return java.time.Month.of(getIsoValue());
    }

    /**
     * Gets the display name of the month.
     *
     * @param style The style of the name (e.g., full, short).
     * @return The display name.
     */
    public String getDisplayName(final TextStyle style) {
        return getDisplayName(style, Locale.getDefault());
    }

    /**
     * Gets the display name of the month with a specified locale.
     *
     * @param style  The style of the name (e.g., full, short).
     * @param locale The {@link Locale} to use.
     * @return The display name.
     */
    public String getDisplayName(final TextStyle style, final Locale locale) {
        return toJdkMonth().getDisplayName(style, locale);
    }

}
