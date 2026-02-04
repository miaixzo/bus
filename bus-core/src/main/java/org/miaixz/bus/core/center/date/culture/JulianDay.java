/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.core.center.date.culture;

import org.miaixz.bus.core.center.date.culture.solar.SolarDay;
import org.miaixz.bus.core.center.date.culture.solar.SolarTime;

/**
 * Represents a Julian Day. The Julian Day is the continuous count of days since the beginning of the Julian period and
 * is used primarily in astronomical calculations.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class JulianDay extends Loops {

    /**
     * The Julian Day for the standard epoch J2000.0, which corresponds to 2000-01-01 12:00:00 UTC.
     */
    public static final double J2000 = 2451545;

    /**
     * The Julian Day number.
     */
    protected double day;

    /**
     * Constructs a JulianDay from a numeric value.
     *
     * @param day The Julian Day number.
     */
    public JulianDay(double day) {
        this.day = day;
    }

    /**
     * Creates a {@code JulianDay} instance from a numeric value.
     *
     * @param day The Julian Day number.
     * @return a new {@code JulianDay} instance.
     */
    public static JulianDay fromJulianDay(double day) {
        return new JulianDay(day);
    }

    /**
     * Creates a {@code JulianDay} instance from Gregorian calendar components.
     *
     * @param year   The year.
     * @param month  The month (1-12).
     * @param day    The day.
     * @param hour   The hour.
     * @param minute The minute.
     * @param second The second.
     * @return a new {@code JulianDay} instance.
     */
    public static JulianDay fromYmdHms(int year, int month, int day, int hour, int minute, int second) {
        double d = day + ((second / 60 + minute) / 60 + hour) / 24;
        int n = 0;
        // Determine if the date is Gregorian
        boolean g = year * 372 + month * 31 + (int) d >= 588829;
        if (month <= 2) {
            month += 12;
            year--;
        }
        if (g) {
            n = (int) (year * 0.01);
            n = 2 - n + (int) (n * 0.25);
        }
        return fromJulianDay((int) (365.25 * (year + 4716)) + (int) (30.6001 * (month + 1)) + d + n - 1524.5);
    }

    /**
     * Gets the Julian Day number.
     *
     * @return The Julian Day number.
     */
    public double getDay() {
        return day;
    }

    /**
     * Gets the name of this object.
     *
     * @return the name
     */
    @Override
    public String getName() {
        return String.valueOf(day);
    }

    /**
     * Returns the next element in the iteration.
     *
     * @return the next element
     */
    @Override
    public JulianDay next(int n) {
        return fromJulianDay(day + n);
    }

    /**
     * Converts this Julian Day to a {@link SolarDay} (Gregorian calendar day).
     *
     * @return The corresponding {@code SolarDay}.
     */
    public SolarDay getSolarDay() {
        return getSolarTime().getSolarDay();
    }

    /**
     * Converts this Julian Day to a {@link SolarTime} (Gregorian calendar date and time).
     *
     * @return The corresponding {@code SolarTime}.
     */
    public SolarTime getSolarTime() {
        int n = (int) (day + 0.5);
        double f = day + 0.5 - n;

        if (n >= 2299161) {
            int c = (int) ((n - 1867216.25) / 36524.25);
            n += 1 + c - (int) (c * 0.25);
        }
        n += 1524;
        int y = (int) ((n - 122.1) / 365.25);
        n -= (int) (365.25 * y);
        int m = (int) (n / 30.601);
        n -= (int) (30.601 * m);
        int d = n;
        if (m > 13) {
            m -= 12;
        } else {
            y -= 1;
        }
        m -= 1;
        y -= 4715;
        f *= 24;
        int hour = (int) f;

        f -= hour;
        f *= 60;
        int minute = (int) f;

        f -= minute;
        f *= 60;
        int second = (int) Math.round(f);

        // Handle leap second overflow
        return second < 60 ? SolarTime.fromYmdHms(y, m, d, hour, minute, second)
                : SolarTime.fromYmdHms(y, m, d, hour, minute, second - 60).next(60);
    }

    /**
     * Calculates the day of the week for this Julian Day.
     *
     * @return The {@link Week}.
     */
    public Week getWeek() {
        return Week.fromIndex((int) (day + 0.5) + 7000001);
    }

    /**
     * Subtracts another {@code JulianDay} from this one to find the difference in days.
     *
     * @param target The {@code JulianDay} to subtract.
     * @return The difference in days.
     */
    public double subtract(JulianDay target) {
        return day - target.getDay();
    }

}
