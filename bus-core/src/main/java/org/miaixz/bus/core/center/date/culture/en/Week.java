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

import java.time.DayOfWeek;
import java.util.Calendar;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.xyz.EnumKit;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * Enumeration for weeks, corresponding to the int values of weeks in {@link Calendar}.
 *
 * @author Kimi Liu
 * @see #SUNDAY
 * @see #MONDAY
 * @see #TUESDAY
 * @see #WEDNESDAY
 * @see #THURSDAY
 * @see #FRIDAY
 * @see #SATURDAY
 * @since Java 17+
 */
public enum Week {

    /**
     * Sunday
     */
    SUNDAY(Calendar.SUNDAY, "日"),
    /**
     * Monday
     */
    MONDAY(Calendar.MONDAY, "一"),
    /**
     * Tuesday
     */
    TUESDAY(Calendar.TUESDAY, "二"),
    /**
     * Wednesday
     */
    WEDNESDAY(Calendar.WEDNESDAY, "三"),
    /**
     * Thursday
     */
    THURSDAY(Calendar.THURSDAY, "四"),
    /**
     * Friday
     */
    FRIDAY(Calendar.FRIDAY, "五"),
    /**
     * Saturday
     */
    SATURDAY(Calendar.SATURDAY, "六");

    /**
     * Array of all {@link Week} enum constants.
     */
    private static final Week[] ENUMS = Week.values();

    /**
     * The week value corresponding to {@link Calendar}.
     */
    private final int code;
    /**
     * The name of the week.
     */
    private final String name;

    /**
     * Constructor for Week enum.
     *
     * @param code The week value corresponding to {@link Calendar}.
     * @param name The name of the week.
     */
    Week(final int code, final String name) {
        this.code = code;
        this.name = name;
    }

    /**
     * Converts a {@link Calendar} week int value to a {@link Week} enum object.
     *
     * @param code The int value of the week in Calendar, 1 represents Sunday.
     * @return The corresponding {@link Week} enum.
     * @see #SUNDAY
     * @see #MONDAY
     * @see #TUESDAY
     * @see #WEDNESDAY
     * @see #THURSDAY
     * @see #FRIDAY
     * @see #SATURDAY
     */
    public static Week of(final int code) {
        if (code > ENUMS.length || code < 1) {
            return null;
        }
        return ENUMS[code - 1];
    }

    /**
     * Parses an alias into a {@link Week} object. Aliases like "sun" or "SUNDAY" are case-insensitive.
     *
     * @param name The alias value.
     * @return The {@link Week} enum, never null.
     * @throws IllegalArgumentException If the alias does not correspond to a valid enum.
     */
    public static Week of(final String name) throws IllegalArgumentException {
        if (null != name && name.length() > 1) {
            if (StringKit.startWithAny(name, "星期", "周")) {
                final char chineseNumber = name.charAt(name.length() - 1);
                switch (chineseNumber) {
                    case '一':
                        return MONDAY;

                    case '二':
                        return TUESDAY;

                    case '三':
                        return WEDNESDAY;

                    case '四':
                        return THURSDAY;

                    case '五':
                        return FRIDAY;

                    case '六':
                        return SATURDAY;

                    case '日':
                        return SUNDAY;
                }
                throw new IllegalArgumentException("Invalid week name: " + name);
            }

            switch (Character.toLowerCase(name.charAt(0))) {
                case 'm':
                    return MONDAY; // monday

                case 'w':
                    return WEDNESDAY; // wednesday

                case 'f':
                    return FRIDAY; // friday

                case 't':
                    switch (Character.toLowerCase(name.charAt(1))) {
                        case 'u':
                            return TUESDAY; // tuesday

                        case 'h':
                            return THURSDAY; // thursday
                    }
                    break;

                case 's':
                    switch (Character.toLowerCase(name.charAt(1))) {
                        case 'a':
                            return SATURDAY; // saturday

                        case 'u':
                            return SUNDAY; // sunday
                    }
                    break;
            }
        }

        throw new IllegalArgumentException("Invalid week name: " + name);
    }

    /**
     * Converts a {@link DayOfWeek} value to a {@link Week} enum object.
     *
     * @param dayOfWeek The {@link DayOfWeek} value.
     * @return The corresponding {@link Week} enum.
     * @see #SUNDAY
     * @see #MONDAY
     * @see #TUESDAY
     * @see #WEDNESDAY
     * @see #THURSDAY
     * @see #FRIDAY
     * @see #SATURDAY
     */
    public static Week of(final DayOfWeek dayOfWeek) {
        Assert.notNull(dayOfWeek);
        int week = dayOfWeek.getValue() + 1;
        if (8 == week) {
            // Sunday
            week = 1;
        }
        return of(week);
    }

    /**
     * Gets the Chinese name of the week.
     *
     * @param code   The int value of the week in Calendar, 1 represents Sunday.
     * @param prefix The prefix for the week, e.g., "星期" or "周".
     * @return The Chinese name of the week.
     */
    public static String getName(final int code, String prefix) {
        return prefix + ENUMS[code].name;
    }

    /**
     * Retrieves enum property information.
     *
     * @param fieldName The name of the field.
     * @return An array of strings representing the field values.
     */
    public static String[] get(String fieldName) {
        return EnumKit.getFieldValues(Week.class, fieldName).toArray(String[]::new);
    }

    /**
     * Gets the week value corresponding to {@link Calendar}.
     *
     * @return The week value corresponding to {@link Calendar}.
     */
    public int getCode() {
        return this.code;
    }

    /**
     * Gets the ISO8601 standard int value, from 1 (Monday) to 7 (Sunday).
     *
     * @return The ISO8601 standard int value.
     */
    public int getIsoValue() {
        int iso8601IntValue = getCode() - 1;
        if (0 == iso8601IntValue) {
            iso8601IntValue = 7;
        }
        return iso8601IntValue;
    }

    /**
     * Converts to the Chinese name.
     *
     * @return The Chinese name of the week.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Converts to the Chinese name with a specified prefix.
     *
     * @param prefix The prefix for the week, e.g., "星期" or "周".
     * @return The Chinese name of the week.
     */
    public String getName(final String prefix) {
        switch (this) {
            case SUNDAY:
                return prefix + "日";

            case MONDAY:
                return prefix + "一";

            case TUESDAY:
                return prefix + "二";

            case WEDNESDAY:
                return prefix + "三";

            case THURSDAY:
                return prefix + "四";

            case FRIDAY:
                return prefix + "五";

            case SATURDAY:
                return prefix + "六";

            default:
                return null;
        }
    }

    /**
     * Converts to {@link DayOfWeek}.
     *
     * @return The corresponding {@link DayOfWeek}.
     */
    public DayOfWeek toJdkDayOfWeek() {
        return DayOfWeek.of(getIsoValue());
    }

}
