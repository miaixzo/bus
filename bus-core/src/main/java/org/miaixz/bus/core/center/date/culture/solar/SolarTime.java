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
package org.miaixz.bus.core.center.date.culture.solar;

import org.miaixz.bus.core.center.date.culture.JulianDay;
import org.miaixz.bus.core.center.date.culture.Phase;
import org.miaixz.bus.core.center.date.culture.climate.Climate;
import org.miaixz.bus.core.center.date.culture.lunar.LunarDay;
import org.miaixz.bus.core.center.date.culture.lunar.LunarHour;
import org.miaixz.bus.core.center.date.culture.lunar.LunarMonth;
import org.miaixz.bus.core.center.date.culture.parts.SecondParts;
import org.miaixz.bus.core.center.date.culture.sixty.SixtyCycleHour;

/**
 * Represents a specific time in the Gregorian calendar (year, month, day, hour, minute, second).
 * <p>
 * This class provides precise time representation and conversions between different calendar systems including Lunar,
 * Sexagenary cycle, and moon phase calculations.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class SolarTime extends SecondParts {

    /**
     * Constructs a SolarTime instance.
     *
     * @param year   the year (1-9999)
     * @param month  the month (1-12)
     * @param day    the day of month
     * @param hour   the hour (0-23)
     * @param minute the minute (0-59)
     * @param second the second (0-59)
     * @throws IllegalArgumentException if any parameter is invalid
     */
    public SolarTime(int year, int month, int day, int hour, int minute, int second) {
        validate(year, month, day, hour, minute, second);
        this.year = year;
        this.month = month;
        this.day = day;
        this.hour = hour;
        this.minute = minute;
        this.second = second;
    }

    /**
     * Creates a SolarTime from year, month, day, hour, minute, and second.
     *
     * @param year   the year (1-9999)
     * @param month  the month (1-12)
     * @param day    the day of month
     * @param hour   the hour (0-23)
     * @param minute the minute (0-59)
     * @param second the second (0-59)
     * @return a new SolarTime instance
     */
    public static SolarTime fromYmdHms(int year, int month, int day, int hour, int minute, int second) {
        return new SolarTime(year, month, day, hour, minute, second);
    }

    /**
     * Validates the time components.
     *
     * @param year   the year
     * @param month  the month
     * @param day    the day
     * @param hour   the hour
     * @param minute the minute
     * @param second the second
     * @throws IllegalArgumentException if validation fails
     */
    public static void validate(int year, int month, int day, int hour, int minute, int second) {
        SecondParts.validate(hour, minute, second);
        SolarDay.validate(year, month, day);
    }

    /**
     * Gets the solar day containing this time.
     *
     * @return the SolarDay
     */
    public SolarDay getSolarDay() {
        return SolarDay.fromYmd(year, month, day);
    }

    /**
     * Gets the time as a formatted string (HH:mm:ss).
     *
     * @return the formatted time string
     */
    public String getName() {
        return String.format("%02d:%02d:%02d", hour, minute, second);
    }

    /**
     * Returns the string representation of this object.
     *
     * @return the string representation
     */
    @Override
    public String toString() {
        return String.format("%s %s", getSolarDay(), getName());
    }

    /**
     * Checks if this time is before the target time.
     *
     * @param target the solar time to compare with
     * @return true if this time is before the target
     */
    public boolean isBefore(SolarTime target) {
        return getCompareIndex() < target.getCompareIndex();
    }

    /**
     * Checks if this time is after the target time.
     *
     * @param target the solar time to compare with
     * @return true if this time is after the target
     */
    public boolean isAfter(SolarTime target) {
        return getCompareIndex() > target.getCompareIndex();
    }

    /**
     * Gets the solar term for this time. Determines which solar term this time falls into based on precise astronomical
     * calculations.
     *
     * @return the SolarTerms
     */
    public SolarTerms getTerm() {
        SolarTerms term = getSolarDay().getTerm();
        if (isBefore(term.getJulianDay().getSolarTime())) {
            term = term.next(-1);
        }
        return term;
    }

    /**
     * Gets the climate pentad for this time.
     *
     * @return the Climate (pentad)
     */
    public Climate getPhenology() {
        Climate p = getSolarDay().getPhenology();
        if (isBefore(p.getJulianDay().getSolarTime())) {
            p = p.next(-1);
        }
        return p;
    }

    /**
     * Gets the Julian Day for this time.
     *
     * @return the JulianDay
     */
    public JulianDay getJulianDay() {
        return JulianDay.fromYmdHms(year, month, day, hour, minute, second);
    }

    /**
     * Calculates the difference in seconds between this time and the target time.
     *
     * @param target the solar time to subtract from this time
     * @return the number of seconds difference (positive if target is earlier)
     */
    public int subtract(SolarTime target) {
        long t = getSolarDay().subtract(target.getSolarDay()) * 86400L + getSecondsInDay()
                - target.getSecondsInDay();
        if (t < Integer.MIN_VALUE || t > Integer.MAX_VALUE) {
            throw new ArithmeticException("seconds difference exceeds int range: " + t);
        }
        return (int) t;
    }

    /**
     * Moves this time forward or backward by a specified number of seconds.
     *
     * @param n the number of seconds to move (positive for forward, negative for backward)
     * @return the SolarTime n seconds from this time
     */
    public SolarTime next(int n) {
        if (n == 0) {
            return SolarTime.fromYmdHms(year, month, day, hour, minute, second);
        }
        long t = hour * 3600L + minute * 60L + second + n;
        int s = (int) Math.floorMod(t, 86400);
        SolarDay d = getSolarDay().next((int) Math.floorDiv(t, 86400));
        return SolarTime.fromYmdHms(d.getYear(), d.getMonth(), d.getDay(), s / 3600, (s % 3600) / 60, s % 60);
    }

    /**
     * Converts this solar time to a lunar hour (Shichen). Each Shichen is a 2-hour period in the traditional Chinese
     * timekeeping system.
     *
     * @return the corresponding LunarHour
     */
    public LunarHour getLunarHour() {
        LunarDay d = getSolarDay().getLunarDay();
        return LunarHour.fromYmdHms(d.getYear(), d.getMonth(), d.getDay(), hour, minute, second);
    }

    /**
     * Converts this solar time to a sexagenary cycle (Gan-Zhi) hour.
     *
     * @return the corresponding SixtyCycleHour
     */
    public SixtyCycleHour getSixtyCycleHour() {
        return SixtyCycleHour.fromSolarTime(this);
    }

    /**
     * Gets the moon phase for this time.
     *
     * @return the Phase
     */
    public Phase getPhase() {
        LunarMonth month = getLunarHour().getLunarDay().getLunarMonth().next(1);
        Phase p = Phase.fromIndex(month.getYear(), month.getMonthWithLeap(), 0);
        while (p.getSolarTime().isAfter(this)) {
            p = p.next(-1);
        }
        return p;
    }

}
