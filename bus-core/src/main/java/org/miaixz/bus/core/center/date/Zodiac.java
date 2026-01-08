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

import java.time.LocalDate;
import java.time.MonthDay;
import java.util.Date;

import org.miaixz.bus.core.xyz.EnumKit;

/**
 * Enumeration for zodiacs (zodiac signs).
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public enum Zodiac {

    /**
     * Aries
     */
    ARIES(0, "白羊", MonthDay.of(3, 21), MonthDay.of(4, 19)),
    /**
     * Taurus
     */
    TAURUS(1, "金牛", MonthDay.of(4, 20), MonthDay.of(5, 20)),
    /**
     * Gemini
     */
    GEMINI(2, "双子", MonthDay.of(5, 21), MonthDay.of(6, 21)),
    /**
     * Cancer
     */
    CANCER(3, "巨蟹", MonthDay.of(6, 22), MonthDay.of(7, 22)),
    /**
     * Leo
     */
    LEO(4, "狮子", MonthDay.of(7, 23), MonthDay.of(8, 22)),
    /**
     * Virgo
     */
    VIRGO(5, "处女", MonthDay.of(8, 23), MonthDay.of(9, 22)),
    /**
     * Libra
     */
    LIBRA(6, "天秤", MonthDay.of(9, 23), MonthDay.of(10, 23)),
    /**
     * Scorpio
     */
    SCORPIO(7, "天蝎", MonthDay.of(10, 24), MonthDay.of(11, 22)),
    /**
     * Sagittarius
     */
    SAGITTARIUS(8, "射手", MonthDay.of(11, 23), MonthDay.of(12, 21)),
    /**
     * Capricorn
     */
    CAPRICORN(9, "摩羯", MonthDay.of(12, 22), MonthDay.of(1, 19)),
    /**
     * Aquarius
     */
    AQUARIUS(10, "水瓶", MonthDay.of(1, 20), MonthDay.of(2, 18)),
    /**
     * Pisces
     */
    PISCES(11, "双鱼", MonthDay.of(2, 19), MonthDay.of(3, 20));

    /**
     * Array of all {@link Zodiac} enum constants.
     */
    private static final Zodiac[] ENUMS = Zodiac.values();

    /**
     * The code of the zodiac.
     */
    private final long code;
    /**
     * The name of the zodiac.
     */
    private final String name;

    /**
     * The start month and day (inclusive) of the zodiac.
     */
    private final MonthDay begin;

    /**
     * The end month and day (inclusive) of the zodiac.
     */
    private final MonthDay end;

    /**
     * Constructor for zodiac enum.
     *
     * @param code  The code of the zodiac.
     * @param name  The name of the zodiac.
     * @param begin The start month and day (inclusive) of the zodiac.
     * @param end   The end month and day (inclusive) of the zodiac.
     */
    Zodiac(long code, String name, MonthDay begin, MonthDay end) {
        this.code = code;
        this.name = name;
        this.begin = begin;
        this.end = end;
    }

    /**
     * Gets the zodiac by its code (palace position).
     *
     * @param code The code of the zodiac.
     * @return The corresponding {@link Zodiac}.
     * @throws IllegalArgumentException if the code is out of range (1-12).
     */
    public static Zodiac get(int code) {
        if (code < 1 || code > 12) {
            throw new IllegalArgumentException();
        }
        return ENUMS[(code + 2) % 12];
    }

    /**
     * Gets the zodiac by date.
     *
     * @param date The date.
     * @return The corresponding {@link Zodiac}.
     */
    public static Zodiac get(LocalDate date) {
        return get(date.getMonthValue(), date.getDayOfMonth());
    }

    /**
     * Gets the zodiac by month and day.
     *
     * @param month The month.
     * @param day   The day.
     * @return The corresponding {@link Zodiac}.
     */
    public static Zodiac get(int month, int day) {
        return get(MonthDay.of(month, day));
    }

    /**
     * Gets the zodiac by {@link MonthDay}.
     *
     * @param monthDay The {@link MonthDay}.
     * @return The corresponding {@link Zodiac}.
     */
    public static Zodiac get(MonthDay monthDay) {
        int month = monthDay.getMonthValue();
        int day = monthDay.getDayOfMonth();
        Zodiac zodiac = ENUMS[month - 1];
        return day <= zodiac.end.getDayOfMonth() ? zodiac : ENUMS[month % 12];
    }

    /**
     * Calculates the zodiac based on the birth date.
     *
     * @param date The birth date.
     * @return The name of the zodiac.
     */
    public static String getName(final Date date) {
        return getName(Calendar.calendar(date));
    }

    /**
     * Calculates the zodiac based on the birth date.
     *
     * @param calendar The birth date {@link java.util.Calendar}.
     * @return The name of the zodiac.
     */
    public static String getName(final java.util.Calendar calendar) {
        if (null == calendar) {
            return null;
        }
        return getName(calendar.get(java.util.Calendar.MONTH), calendar.get(java.util.Calendar.DAY_OF_MONTH));
    }

    /**
     * Calculates the zodiac based on the birth month and day.
     *
     * @param month The month, starting from 0.
     * @param day   The day.
     * @return The name of the zodiac.
     */
    public static String getName(final Month month, final int day) {
        return getName(month.getValue(), day);
    }

    /**
     * Calculates the zodiac based on the birth month and day.
     *
     * @param month The month, starting from 0, see {@link Month#getValue()}.
     * @param day   The day.
     * @return The name of the zodiac.
     */
    public static String getName(final int month, final int day) {
        return get(month, day).name;
    }

    /**
     * Retrieves enum property information.
     *
     * @param fieldName The name of the field.
     * @return An array of strings representing the field values.
     */
    public static String[] get(String fieldName) {
        return EnumKit.getFieldValues(Zodiac.class, fieldName).toArray(String[]::new);
    }

    /**
     * Gets the name corresponding to the given code.
     *
     * @param code The code of the zodiac.
     * @return The corresponding name.
     */
    public String getName(final int code) {
        return ENUMS[code].name;
    }

    /**
     * Gets the code corresponding to this unit.
     *
     * @return The code of the zodiac.
     */
    public long getCode() {
        return this.code;
    }

    /**
     * Gets the name of the zodiac.
     *
     * @return The name of the zodiac.
     */
    public String getName() {
        return this.name;
    }

}
