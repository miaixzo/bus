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
package org.miaixz.bus.core.center.date.culture.lunar;

import java.util.List;

import org.miaixz.bus.core.center.date.culture.Taboo;
import org.miaixz.bus.core.center.date.culture.eightchar.EightChar;
import org.miaixz.bus.core.center.date.culture.eightchar.provider.EightCharProvider;
import org.miaixz.bus.core.center.date.culture.eightchar.provider.impl.DefaultEightCharProvider;
import org.miaixz.bus.core.center.date.culture.parts.SecondParts;
import org.miaixz.bus.core.center.date.culture.ren.MinorRen;
import org.miaixz.bus.core.center.date.culture.sixty.EarthBranch;
import org.miaixz.bus.core.center.date.culture.sixty.HeavenStem;
import org.miaixz.bus.core.center.date.culture.sixty.SixtyCycle;
import org.miaixz.bus.core.center.date.culture.sixty.SixtyCycleHour;
import org.miaixz.bus.core.center.date.culture.solar.SolarTerms;
import org.miaixz.bus.core.center.date.culture.star.nine.NineStar;
import org.miaixz.bus.core.center.date.culture.star.twelve.TwelveStar;
import org.miaixz.bus.core.center.date.culture.solar.SolarDay;
import org.miaixz.bus.core.center.date.culture.solar.SolarTime;

/**
 * Represents a specific two-hour block (shichen) in the Lunar calendar.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class LunarHour extends SecondParts {

    /**
     * Provider for Eight Character (Ba Zi) calculations.
     */
    public static EightCharProvider provider = new DefaultEightCharProvider();

    /**
     * Validates lunar year, month, day, hour, minute, and second.
     *
     * @param year   the lunar year
     * @param month  the lunar month
     * @param day    the lunar day
     * @param hour   the hour (0-23)
     * @param minute the minute (0-59)
     * @param second the second (0-59)
     */
    public static void validate(int year, int month, int day, int hour, int minute, int second) {
        SecondParts.validate(hour, minute, second);
        LunarDay.validate(year, month, day);
    }

    /**
     * Constructs a LunarHour instance.
     *
     * @param year   the lunar year
     * @param month  the lunar month (negative value indicates a leap month)
     * @param day    the lunar day
     * @param hour   the hour (0-23)
     * @param minute the minute (0-59)
     * @param second the second (0-59)
     */
    public LunarHour(int year, int month, int day, int hour, int minute, int second) {
        this.year = year;
        this.month = month;
        this.day = day;
        this.hour = hour;
        this.minute = minute;
        this.second = second;
    }

    /**
     * Creates a LunarHour from year, month, day, hour, minute, and second components.
     *
     * @param year   the lunar year
     * @param month  the lunar month (negative value indicates a leap month)
     * @param day    the lunar day
     * @param hour   the hour (0-23)
     * @param minute the minute (0-59)
     * @param second the second (0-59)
     * @return a new LunarHour instance
     */
    public static LunarHour fromYmdHms(int year, int month, int day, int hour, int minute, int second) {
        return new LunarHour(year, month, day, hour, minute, second);
    }

    /**
     * Gets the lunar day for this hour.
     *
     * @return the lunar day
     */
    public LunarDay getLunarDay() {
        return LunarDay.fromYmd(year, month, day);
    }

    /**
     * Gets the Chinese name of this two-hour period (shichen).
     *
     * @return the name (e.g., "子时", "丑时")
     */
    public String getName() {
        return EarthBranch.fromIndex(getIndexInDay()).getName() + "时";
    }

    /**
     * Returns the string representation of this object.
     *
     * @return the string representation
     */
    @Override
    public String toString() {
        return getLunarDay() + getSixtyCycle().getName() + "时";
    }

    /**
     * Gets the index of this hour within the day (0-11).
     *
     * @return the index (0 for Zi hour, 1 for Chou hour, etc.)
     */
    public int getIndexInDay() {
        return (hour + 1) / 2;
    }

    /**
     * Gets the lunar hour that is n two-hour periods after this hour.
     *
     * @param n the number of two-hour periods to advance (can be negative)
     * @return the lunar hour after n periods
     */
    public LunarHour next(int n) {
        if (n == 0) {
            return fromYmdHms(year, month, day, hour, minute, second);
        }
        int h = hour + n * 2;
        int diff = h < 0 ? -1 : 1;
        int hour = Math.abs(h);
        int days = hour / 24 * diff;
        hour = (hour % 24) * diff;
        if (hour < 0) {
            hour += 24;
            days--;
        }
        LunarDay d = getLunarDay().next(days);
        return fromYmdHms(d.getYear(), d.getMonth(), d.getDay(), hour, minute, second);
    }

    /**
     * Checks if this lunar hour is before the target lunar hour.
     *
     * @param target the lunar hour to compare with
     * @return true if this hour is before the target, false otherwise
     */
    public boolean isBefore(LunarHour target) {
        LunarDay d = getLunarDay();
        if (!d.equals(target.getLunarDay())) {
            return d.isBefore(target.getLunarDay());
        }
        if (hour != target.getHour()) {
            return hour < target.getHour();
        }
        return minute != target.getMinute() ? minute < target.getMinute() : second < target.getSecond();
    }

    /**
     * Checks if this lunar hour is after the target lunar hour.
     *
     * @param target the lunar hour to compare with
     * @return true if this hour is after the target, false otherwise
     */
    public boolean isAfter(LunarHour target) {
        LunarDay d = getLunarDay();
        if (!d.equals(target.getLunarDay())) {
            return d.isAfter(target.getLunarDay());
        }
        if (hour != target.getHour()) {
            return hour > target.getHour();
        }
        return minute != target.getMinute() ? minute > target.getMinute() : second > target.getSecond();
    }

    /**
     * Gets the year's Sixty Cycle (Gan-Zhi) for this hour (changes on Lichun).
     *
     * @return the Sixty Cycle
     * @see SixtyCycleHour#getYear()
     * @deprecated Use {@link SixtyCycleHour#getYear()} instead
     */
    @Deprecated
    public SixtyCycle getYearSixtyCycle() {
        return getSixtyCycleHour().getYear();
    }

    /**
     * Gets the month's Sixty Cycle (Gan-Zhi) for this hour (changes on solar terms).
     *
     * @return the Sixty Cycle
     * @see SixtyCycleHour#getMonth()
     * @deprecated Use {@link SixtyCycleHour#getMonth()} instead
     */
    @Deprecated
    public SixtyCycle getMonthSixtyCycle() {
        return getSixtyCycleHour().getMonth();
    }

    /**
     * Gets the day's Sixty Cycle (Gan-Zhi) for this hour (23:00 counts as the next day).
     *
     * @return the Sixty Cycle
     * @see SixtyCycleHour#getDay()
     * @deprecated Use {@link SixtyCycleHour#getDay()} instead
     */
    @Deprecated
    public SixtyCycle getDaySixtyCycle() {
        return getSixtyCycleHour().getDay();
    }

    /**
     * Gets the Sixty Cycle (Gan-Zhi) for this hour.
     *
     * @return the Sixty Cycle
     */
    public SixtyCycle getSixtyCycle() {
        int earthBranchIndex = getIndexInDay() % 12;
        SixtyCycle d = getLunarDay().getSixtyCycle();
        if (hour >= 23) {
            d = d.next(1);
        }
        return SixtyCycle.fromName(
                HeavenStem.fromIndex(d.getHeavenStem().getIndex() % 5 * 2 + earthBranchIndex).getName()
                        + EarthBranch.fromIndex(earthBranchIndex).getName());
    }

    /**
     * Gets the twelve stars (Huangdao-Heidao twelve gods) for this hour.
     *
     * @return the twelve star spirit
     */
    public TwelveStar getTwelveStar() {
        return TwelveStar.fromIndex(
                getSixtyCycle().getEarthBranch().getIndex()
                        + (8 - getSixtyCycleHour().getDay().getEarthBranch().getIndex() % 6) * 2);
    }

    /**
     * Gets the nine stars for this hour.
     * <p>
     * Based on the Time Family Purple-White Star Song: "The three yuan time white is most excellent, yang grows from
     * winter solstice and moves forward without error. Meng day seventh palace, zhong day one white, ji day four green
     * sprouts. Starting from Jiazi for each time period, the star of this time shines with glory. When the time star
     * moves into the central palace, flying forward in eight directions for careful inspection. From summer solstice
     * yin grows and reverses direction, meng returns to three green, ji adds six, zhong at nine palace starting from
     * Jia, still crossing the reverse wheel in the palm."
     *
     * @return the nine star
     */
    public NineStar getNineStar() {
        LunarDay d = getLunarDay();
        SolarDay solar = d.getSolarDay();
        SolarTerms dongZhi = SolarTerms.fromIndex(solar.getYear(), 0);
        int earthBranchIndex = getIndexInDay() % 12;
        int index = new int[] { 8, 5, 2 }[d.getSixtyCycle().getEarthBranch().getIndex() % 3];
        if (!solar.isBefore(dongZhi.getJulianDay().getSolarDay())
                && solar.isBefore(dongZhi.next(12).getJulianDay().getSolarDay())) {
            index = 8 + earthBranchIndex - index;
        } else {
            index -= earthBranchIndex;
        }
        return NineStar.fromIndex(index);
    }

    /**
     * Gets the solar time corresponding to this lunar hour.
     *
     * @return the solar time
     */
    public SolarTime getSolarTime() {
        SolarDay d = getLunarDay().getSolarDay();
        return SolarTime.fromYmdHms(d.getYear(), d.getMonth(), d.getDay(), hour, minute, second);
    }

    /**
     * Gets the Eight Characters (Ba Zi) for this hour.
     *
     * @return the eight characters
     */
    public EightChar getEightChar() {
        return provider.getEightChar(this);
    }

    /**
     * Gets the Sixty Cycle hour for this lunar hour.
     *
     * @return the Sixty Cycle hour
     */
    public SixtyCycleHour getSixtyCycleHour() {
        return getSolarTime().getSixtyCycleHour();
    }

    /**
     * Gets the list of recommended activities for this hour.
     *
     * @return the list of taboos representing recommended activities
     */
    public List<Taboo> getRecommends() {
        return Taboo.getHourRecommends(getSixtyCycleHour().getDay(), getSixtyCycle());
    }

    /**
     * Gets the list of activities to avoid for this hour.
     *
     * @return the list of taboos representing activities to avoid
     */
    public List<Taboo> getAvoids() {
        return Taboo.getHourAvoids(getSixtyCycleHour().getDay(), getSixtyCycle());
    }

    /**
     * Gets the Minor Six Ren divination for this hour.
     *
     * @return the Minor Six Ren
     */
    public MinorRen getMinorRen() {
        return getLunarDay().getMinorRen().next(getIndexInDay());
    }
}
