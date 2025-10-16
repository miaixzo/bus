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

import org.miaixz.bus.core.center.date.culture.Loops;
import org.miaixz.bus.core.center.date.culture.cn.JulianDay;
import org.miaixz.bus.core.center.date.culture.cn.climate.Climate;
import org.miaixz.bus.core.center.date.culture.cn.sixty.SixtyCycleHour;
import org.miaixz.bus.core.center.date.culture.lunar.LunarDay;
import org.miaixz.bus.core.center.date.culture.lunar.LunarHour;

/**
 * Represents a specific time in the Gregorian calendar.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SolarTime extends Loops {

    /**
     * The solar day this time belongs to.
     */
    protected SolarDay day;

    /**
     * The hour of the day (0-23).
     */
    protected int hour;

    /**
     * The minute of the hour (0-59).
     */
    protected int minute;

    /**
     * The second of the minute (0-59).
     */
    protected int second;

    /**
     * Constructs a {@code SolarTime} with the given year, month, day, hour, minute, and second.
     *
     * @param year   The year.
     * @param month  The month.
     * @param day    The day.
     * @param hour   The hour (0-23).
     * @param minute The minute (0-59).
     * @param second The second (0-59).
     * @throws IllegalArgumentException if hour, minute, or second are out of valid range.
     */
    public SolarTime(int year, int month, int day, int hour, int minute, int second) {
        if (hour < 0 || hour > 23) {
            throw new IllegalArgumentException(String.format("illegal hour: %d", hour));
        }
        if (minute < 0 || minute > 59) {
            throw new IllegalArgumentException(String.format("illegal minute: %d", minute));
        }
        if (second < 0 || second > 59) {
            throw new IllegalArgumentException(String.format("illegal second: %d", second));
        }
        this.day = SolarDay.fromYmd(year, month, day);
        this.hour = hour;
        this.minute = minute;
        this.second = second;
    }

    /**
     * Creates a {@code SolarTime} instance from the given year, month, day, hour, minute, and second.
     *
     * @param year   The year.
     * @param month  The month.
     * @param day    The day.
     * @param hour   The hour.
     * @param minute The minute.
     * @param second The second.
     * @return A new {@link SolarTime} instance.
     */
    public static SolarTime fromYmdHms(int year, int month, int day, int hour, int minute, int second) {
        return new SolarTime(year, month, day, hour, minute, second);
    }

    /**
     * Gets the solar day this time belongs to.
     *
     * @return The {@link SolarDay}.
     */
    public SolarDay getSolarDay() {
        return day;
    }

    /**
     * Gets the year of this solar time.
     *
     * @return The year.
     */
    public int getYear() {
        return day.getYear();
    }

    /**
     * Gets the month of this solar time.
     *
     * @return The month.
     */
    public int getMonth() {
        return day.getMonth();
    }

    /**
     * Gets the day of this solar time.
     *
     * @return The day.
     */
    public int getDay() {
        return day.getDay();
    }

    /**
     * Gets the hour of this solar time.
     *
     * @return The hour.
     */
    public int getHour() {
        return hour;
    }

    /**
     * Gets the minute of this solar time.
     *
     * @return The minute.
     */
    public int getMinute() {
        return minute;
    }

    /**
     * Gets the second of this solar time.
     *
     * @return The second.
     */
    public int getSecond() {
        return second;
    }

    /**
     * Gets the formatted time string (HH:mm:ss).
     *
     * @return The formatted time string.
     */
    public String getName() {
        return String.format("%02d:%02d:%02d", hour, minute, second);
    }

    @Override
    public String toString() {
        return String.format("%s %s", day, getName());
    }

    /**
     * Checks if this solar time is before the target solar time.
     *
     * @param target The target solar time.
     * @return {@code true} if this time is before the target, {@code false} otherwise.
     */
    public boolean isBefore(SolarTime target) {
        if (!day.equals(target.getSolarDay())) {
            return day.isBefore(target.getSolarDay());
        }
        if (hour != target.getHour()) {
            return hour < target.getHour();
        }
        return minute != target.getMinute() ? minute < target.getMinute() : second < target.getSecond();
    }

    /**
     * Checks if this solar time is after the target solar time.
     *
     * @param target The target solar time.
     * @return {@code true} if this time is after the target, {@code false} otherwise.
     */
    public boolean isAfter(SolarTime target) {
        if (!day.equals(target.getSolarDay())) {
            return day.isAfter(target.getSolarDay());
        }
        if (hour != target.getHour()) {
            return hour > target.getHour();
        }
        return minute != target.getMinute() ? minute > target.getMinute() : second > target.getSecond();
    }

    /**
     * Gets the solar term for this solar time.
     *
     * @return The {@link SolarTerms} for this time.
     */
    public SolarTerms getTerm() {
        SolarTerms term = day.getTerm();
        if (isBefore(term.getJulianDay().getSolarTime())) {
            term = term.next(-1);
        }
        return term;
    }

    /**
     * Gets the phenology (Hou) for this solar time.
     *
     * @return The {@link Climate} for this time.
     */
    public Climate getPhenology() {
        Climate p = day.getPhenology();
        if (isBefore(p.getJulianDay().getSolarTime())) {
            p = p.next(-1);
        }
        return p;
    }

    /**
     * Gets the Julian day corresponding to this solar time.
     *
     * @return The {@link JulianDay} for this solar time.
     */
    public JulianDay getJulianDay() {
        return JulianDay.fromYmdHms(getYear(), getMonth(), getDay(), hour, minute, second);
    }

    /**
     * Subtracts a target {@code SolarTime} from this {@code SolarTime}, returning the difference in seconds.
     *
     * @param target The target {@code SolarTime}.
     * @return The number of seconds difference.
     */
    public int subtract(SolarTime target) {
        int days = day.subtract(target.getSolarDay());
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
     * Advances this solar time by a specified number of seconds.
     *
     * @param n The number of seconds to advance.
     * @return The {@link SolarTime} after advancing by {@code n} seconds.
     */
    public SolarTime next(int n) {
        if (n == 0) {
            return SolarTime.fromYmdHms(getYear(), getMonth(), getDay(), hour, minute, second);
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

        SolarDay d = day.next(td);
        return SolarTime.fromYmdHms(d.getYear(), d.getMonth(), d.getDay(), th, tm, ts);
    }

    /**
     * Gets the lunar hour corresponding to this solar time.
     *
     * @return The {@link LunarHour} for this solar time.
     */
    public LunarHour getLunarHour() {
        LunarDay d = day.getLunarDay();
        return LunarHour.fromYmdHms(d.getYear(), d.getMonth(), d.getDay(), hour, minute, second);
    }

    /**
     * Gets the Sixty Cycle Hour (GanZhi hour) corresponding to this solar time.
     *
     * @return The {@link SixtyCycleHour} for this solar time.
     */
    public SixtyCycleHour getSixtyCycleHour() {
        return SixtyCycleHour.fromSolarTime(this);
    }

}
