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
package org.miaixz.bus.core.center.date.culture.hijri;

import org.miaixz.bus.core.center.date.culture.JulianDay;
import org.miaixz.bus.core.center.date.culture.parts.DayParts;
import org.miaixz.bus.core.center.date.culture.solar.SolarDay;

/**
 * Represents a day in the tabular Hijri calendar. The epoch used here is 622-07-16 in the Gregorian/Julian conversion
 * model used by the surrounding calendar classes.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class HijriDay extends DayParts {

    /**
     * Localized day labels from 1 to 30.
     */
    public static final String[] NAMES = { "1日", "2日", "3日", "4日", "5日", "6日", "7日", "8日", "9日", "10日", "11日", "12日",
            "13日", "14日", "15日", "16日", "17日", "18日", "19日", "20日", "21日", "22日", "23日", "24日", "25日", "26日", "27日",
            "28日", "29日", "30日" };

    /**
     * Validates a Hijri date.
     *
     * @param year  Hijri year
     * @param month Hijri month
     * @param day   Hijri day
     * @throws IllegalArgumentException if the date is invalid
     */
    public static void validate(int year, int month, int day) {
        if (day < 1) {
            throw new IllegalArgumentException(String.format("illegal hijri day: %d-%d-%d", year, month, day));
        }
        if (day > HijriMonth.fromYm(year, month).getDayCount()) {
            throw new IllegalArgumentException(String.format("illegal hijri day: %d-%d-%d", year, month, day));
        }
    }

    /**
     * Constructs a Hijri day.
     *
     * @param year  Hijri year
     * @param month Hijri month
     * @param day   Hijri day
     */
    public HijriDay(int year, int month, int day) {
        validate(year, month, day);
        this.year = year;
        this.month = month;
        this.day = day;
    }

    /**
     * Creates a Hijri day.
     *
     * @param year  Hijri year
     * @param month Hijri month
     * @param day   Hijri day
     * @return a new {@link HijriDay}
     */
    public static HijriDay fromYmd(int year, int month, int day) {
        return new HijriDay(year, month, day);
    }

    /**
     * Gets the Hijri month containing this day.
     *
     * @return Hijri month
     */
    public HijriMonth getHijriMonth() {
        return HijriMonth.fromYm(year, month);
    }

    /**
     * Gets the localized day name.
     *
     * @return day name
     */
    public String getName() {
        return NAMES[day - 1];
    }

    /**
     * Returns the display text of this day.
     *
     * @return display text
     */
    @Override
    public String toString() {
        return getHijriMonth() + getName();
    }

    /**
     * Gets the day that is {@code n} days away.
     *
     * @param n number of days to move
     * @return target Hijri day
     */
    public HijriDay next(int n) {
        return getSolarDay().next(n).getHijriDay();
    }

    /**
     * Checks whether this day is before the target day.
     *
     * @param target target day
     * @return {@code true} if this day is before target
     */
    public boolean isBefore(HijriDay target) {
        if (year != target.year) {
            return year < target.year;
        }
        return month != target.month ? month < target.month : day < target.day;
    }

    /**
     * Checks whether this day is after the target day.
     *
     * @param target target day
     * @return {@code true} if this day is after target
     */
    public boolean isAfter(HijriDay target) {
        if (year != target.year) {
            return year > target.year;
        }
        return month != target.month ? month > target.month : day > target.day;
    }

    /**
     * Gets this day's index within the year.
     *
     * @return index from 0
     */
    public int getIndexInYear() {
        int n = 0;
        for (int i = 1; i < month; i++) {
            n += HijriMonth.fromYm(year, i).getDayCount();
        }
        return n + day - 1;
    }

    /**
     * Calculates the day difference between this day and the target.
     *
     * @param target target day
     * @return day difference
     */
    public int subtract(HijriDay target) {
        return (int) getJulianDay().subtract(target.getJulianDay());
    }

    /**
     * Gets the Julian day.
     *
     * @return Julian day
     */
    public JulianDay getJulianDay() {
        return JulianDay.fromJulianDay(
                Math.floorDiv(11 * year + 3, 30) + 354 * year + 30 * month - Math.floorDiv(month - 1, 2) + day
                        + 1948055);
    }

    /**
     * Converts this Hijri day to a solar day.
     *
     * @return corresponding solar day
     */
    public SolarDay getSolarDay() {
        return SolarDay.fromYmd(622, 7, 16).next(subtract(HijriDay.fromYmd(1, 1, 1)));
    }

}
