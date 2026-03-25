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
package org.miaixz.bus.core.center.date.culture.rabjung;

import org.miaixz.bus.core.center.date.culture.parts.DayParts;
import org.miaixz.bus.core.center.date.culture.solar.SolarDay;

/**
 * Represents a day in the Tibetan calendar. Only supports Tibetan calendar from the 1st day of the 12th month of 1950
 * (Gregorian January 8, 1951) to the 30th day of the 12th month of 2050 (Gregorian February 11, 2051).
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class RabjungDay extends DayParts {

    /**
     * Names of Tibetan calendar days.
     */
    public static final String[] NAMES = { "初一", "初二", "初三", "初四", "初五", "初六", "初七", "初八", "初九", "初十", "十一", "十二", "十三",
            "十四", "十五", "十六", "十七", "十八", "十九", "二十", "廿一", "廿二", "廿三", "廿四", "廿五", "廿六", "廿七", "廿八", "廿九", "三十" };

    /**
     * Indicates if this is a leap day.
     */
    protected boolean leap;

    /**
     * Initializes a new RabjungDay instance.
     *
     * @param year  The Tibetan year.
     * @param month The Tibetan month, negative for leap months.
     * @param day   The Tibetan day, negative for leap days.
     */
    public RabjungDay(int year, int month, int day) {
        validate(year, month, day);
        this.year = year;
        this.month = month;
        this.day = Math.abs(day);
        this.leap = day < 0;
    }

    /**
     * Creates a new RabjungDay instance from Tibetan year, month, and day.
     *
     * @param year  The Tibetan year.
     * @param month The Tibetan month, negative for leap months.
     * @param day   The Tibetan day, negative for leap days.
     * @return A new {@link RabjungDay} instance.
     */
    public static RabjungDay fromYmd(int year, int month, int day) {
        return new RabjungDay(year, month, day);
    }

    /**
     * Validates the given Rabjung day parameters.
     *
     * @param year  Gregorian year
     * @param month Rabjung month (negative for leap month)
     * @param day   Rabjung day (negative for leap day, valid range -30 to 30, excluding 0)
     * @throws IllegalArgumentException if the day is out of valid range or does not exist
     */
    public static void validate(int year, int month, int day) {
        if (day == 0 || day < -30 || day > 30) {
            throw new IllegalArgumentException(String.format("illegal day %d in %s", day, month));
        }
        RabjungMonth m = RabjungMonth.fromYm(year, month);
        boolean leap = day < 0;
        int d = Math.abs(day);
        if (leap && !m.getLeapDays().contains(d)) {
            throw new IllegalArgumentException(String.format("illegal leap day %d in %s", d, m));
        }
        if (!leap && m.getMissDays().contains(d)) {
            throw new IllegalArgumentException(String.format("illegal day %d in %s", d, m));
        }
    }

    /**
     * Creates a new RabjungDay instance from a {@link SolarDay}.
     *
     * @param solarDay The solar day.
     * @return A new {@link RabjungDay} instance.
     */
    public static RabjungDay fromSolarDay(SolarDay solarDay) {
        int days = solarDay.subtract(SolarDay.fromYmd(1951, 1, 8));
        RabjungMonth m = RabjungMonth.fromYm(1950, 12);
        int count = m.getDayCount();
        while (days >= count) {
            days -= count;
            m = m.next(1);
            count = m.getDayCount();
        }
        int day = days + 1;
        for (int d : m.getSpecialDays()) {
            if (d < 0) {
                if (day >= -d) {
                    day++;
                }
            } else if (d > 0) {
                if (day == d + 1) {
                    day = -d;
                    break;
                } else if (day > d + 1) {
                    day--;
                }
            }
        }
        return new RabjungDay(m.getYear(), m.getMonthWithLeap(), day);
    }

    /**
     * Gets the Tibetan month this day belongs to.
     *
     * @return The {@link RabjungMonth}.
     */
    public RabjungMonth getRabByungMonth() {
        return RabjungMonth.fromYm(year, month);
    }

    /**
     * Checks if this is a leap day.
     *
     * @return {@code true} if it's a leap day, {@code false} otherwise.
     */
    public boolean isLeap() {
        return leap;
    }

    /**
     * Gets the day of the Tibetan month, returning a negative value if it's a leap day.
     *
     * @return The day, negative for leap days.
     */
    public int getDayWithLeap() {
        return leap ? -day : day;
    }

    /**
     * Gets the Chinese name of the Tibetan day.
     *
     * @return The name of the Tibetan day.
     */
    public String getName() {
        return (leap ? "闰" : "") + NAMES[day - 1];
    }

    /**
     * Returns the string representation of this object.
     *
     * @return the string representation
     */
    @Override
    public String toString() {
        return getRabByungMonth() + getName();
    }

    /**
     * Subtracts a target {@code RabjungDay} from this {@code RabjungDay}.
     *
     * @param target The target {@code RabjungDay}.
     * @return The number of days difference.
     */
    public int subtract(RabjungDay target) {
        return getSolarDay().subtract(target.getSolarDay());
    }

    /**
     * Gets the corresponding solar day.
     *
     * @return The {@link SolarDay} corresponding to this Tibetan day.
     */
    public SolarDay getSolarDay() {
        RabjungMonth m = RabjungMonth.fromYm(1950, 12);
        RabjungMonth cm = getRabByungMonth();
        int n = 0;
        while (!m.equals(cm)) {
            n += m.getDayCount();
            m = m.next(1);
        }
        int t = day;
        for (int d : m.getSpecialDays()) {
            if (d < 0) {
                if (t > -d) {
                    t--;
                }
            } else if (d > 0) {
                if (t > d) {
                    t++;
                }
            }
        }
        if (leap) {
            t++;
        }
        return SolarDay.fromYmd(1951, 1, 7).next(n + t);
    }

    /**
     * Gets the Tibetan day after a specified number of days.
     *
     * @param n The number of days to add.
     * @return The {@link RabjungDay} after {@code n} days.
     */
    public RabjungDay next(int n) {
        return getSolarDay().next(n).getRabByungDay();
    }

}
