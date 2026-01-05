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
 * @since Java 17+
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
        SolarDay d = getSolarDay();
        if (!d.equals(target.getSolarDay())) {
            return d.isBefore(target.getSolarDay());
        }
        if (hour != target.getHour()) {
            return hour < target.getHour();
        }
        return minute != target.getMinute() ? minute < target.getMinute() : second < target.getSecond();
    }

    /**
     * Checks if this time is after the target time.
     *
     * @param target the solar time to compare with
     * @return true if this time is after the target
     */
    public boolean isAfter(SolarTime target) {
        SolarDay d = getSolarDay();
        if (!d.equals(target.getSolarDay())) {
            return d.isAfter(target.getSolarDay());
        }
        if (hour != target.getHour()) {
            return hour > target.getHour();
        }
        return minute != target.getMinute() ? minute > target.getMinute() : second > target.getSecond();
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
        int days = getSolarDay().subtract(target.getSolarDay());
        int cs = hour * 3600 + minute * 60 + second;
        int ts = target.getHour() * 3600 + target.getMinute() * 60 + target.getSecond();
        int seconds = cs - ts;
        if (seconds < 0) {
            seconds += 86400;
            days--;
        }
        seconds += days * 86400;
        return seconds;
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
        int ts = second + n;
        int tm = minute + ts / 60;
        ts %= 60;
        if (ts < 0) {
            ts += 60;
            tm -= 1;
        }
        int th = hour + tm / 60;
        tm %= 60;
        if (tm < 0) {
            tm += 60;
            th -= 1;
        }
        int td = th / 24;
        th %= 24;
        if (th < 0) {
            th += 24;
            td -= 1;
        }

        SolarDay d = getSolarDay().next(td);
        return SolarTime.fromYmdHms(d.getYear(), d.getMonth(), d.getDay(), th, tm, ts);
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
