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

import org.miaixz.bus.core.center.date.culture.Loops;
import org.miaixz.bus.core.center.date.culture.cn.Taboo;
import org.miaixz.bus.core.center.date.culture.cn.eightchar.EightChar;
import org.miaixz.bus.core.center.date.culture.cn.eightchar.provider.EightCharProvider;
import org.miaixz.bus.core.center.date.culture.cn.eightchar.provider.impl.DefaultEightCharProvider;
import org.miaixz.bus.core.center.date.culture.cn.ren.MinorRen;
import org.miaixz.bus.core.center.date.culture.cn.sixty.EarthBranch;
import org.miaixz.bus.core.center.date.culture.cn.sixty.HeavenStem;
import org.miaixz.bus.core.center.date.culture.cn.sixty.SixtyCycle;
import org.miaixz.bus.core.center.date.culture.cn.sixty.SixtyCycleHour;
import org.miaixz.bus.core.center.date.culture.cn.star.nine.NineStar;
import org.miaixz.bus.core.center.date.culture.cn.star.twelve.TwelveStar;
import org.miaixz.bus.core.center.date.culture.solar.SolarDay;
import org.miaixz.bus.core.center.date.culture.solar.SolarTerms;
import org.miaixz.bus.core.center.date.culture.solar.SolarTime;

/**
 * Represents a specific two-hour block (shichen) in the Lunar calendar.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class LunarHour extends Loops {

    /**
     * The provider for Eight Characters (Bazi) calculations.
     */
    public static EightCharProvider provider = new DefaultEightCharProvider();
    /**
     * The lunar day this hour belongs to.
     */
    protected LunarDay day;
    /**
     * The hour (0-23).
     */
    protected int hour;
    /**
     * The minute (0-59).
     */
    protected int minute;
    /**
     * The second (0-59).
     */
    protected int second;

    /**
     * The corresponding Gregorian (Solar) time, lazily initialized.
     */
    protected SolarTime solarTime;

    /**
     * The corresponding Sixty Cycle (Ganzhi) hour, lazily initialized.
     */
    protected SixtyCycleHour sixtyCycleHour;

    /**
     * Constructs a {@code LunarHour} from its components.
     *
     * @param year   The lunar year.
     * @param month  The lunar month (a negative value indicates a leap month).
     * @param day    The lunar day.
     * @param hour   The hour (0-23).
     * @param minute The minute (0-59).
     * @param second The second (0-59).
     */
    public LunarHour(int year, int month, int day, int hour, int minute, int second) {
        if (hour < 0 || hour > 23) {
            throw new IllegalArgumentException(String.format("illegal hour: %d", hour));
        }
        if (minute < 0 || minute > 59) {
            throw new IllegalArgumentException(String.format("illegal minute: %d", minute));
        }
        if (second < 0 || second > 59) {
            throw new IllegalArgumentException(String.format("illegal second: %d", second));
        }
        this.day = LunarDay.fromYmd(year, month, day);
        this.hour = hour;
        this.minute = minute;
        this.second = second;
    }

    /**
     * Creates a {@code LunarHour} from its components.
     *
     * @param year   The lunar year.
     * @param month  The lunar month (a negative value indicates a leap month).
     * @param day    The lunar day.
     * @param hour   The hour (0-23).
     * @param minute The minute (0-59).
     * @param second The second (0-59).
     * @return a new {@code LunarHour} instance.
     */
    public static LunarHour fromYmdHms(int year, int month, int day, int hour, int minute, int second) {
        return new LunarHour(year, month, day, hour, minute, second);
    }

    /**
     * Gets the lunar day object.
     *
     * @return The {@link LunarDay}.
     */
    public LunarDay getLunarDay() {
        return day;
    }

    /**
     * Gets the year.
     *
     * @return The lunar year.
     */
    public int getYear() {
        return day.getYear();
    }

    /**
     * Gets the month.
     *
     * @return The lunar month.
     */
    public int getMonth() {
        return day.getMonth();
    }

    /**
     * Gets the day.
     *
     * @return The lunar day.
     */
    public int getDay() {
        return day.getDay();
    }

    /**
     * Gets the hour.
     *
     * @return The hour (0-23).
     */
    public int getHour() {
        return hour;
    }

    /**
     * Gets the minute.
     *
     * @return The minute (0-59).
     */
    public int getMinute() {
        return minute;
    }

    /**
     * Gets the second.
     *
     * @return The second (0-59).
     */
    public int getSecond() {
        return second;
    }

    /**
     * Gets the name of the two-hour block (e.g., "子时").
     *
     * @return The name of the hour.
     */
    public String getName() {
        return EarthBranch.fromIndex(getIndexInDay()).getName() + "时";
    }

    @Override
    public String toString() {
        return day + getSixtyCycle().getName() + "时";
    }

    /**
     * Gets the index of this hour within the day (0-11 for the 12 two-hour blocks). For example, 23:00-00:59 is index 0
     * (子时).
     *
     * @return The index of the hour block.
     */
    public int getIndexInDay() {
        return (hour + 1) / 2;
    }

    @Override
    public LunarHour next(int n) {
        if (n == 0) {
            return fromYmdHms(getYear(), getMonth(), getDay(), hour, minute, second);
        }
        // Each step is 2 hours.
        int h = hour + n * 2;
        int diff = h < 0 ? -1 : 1;
        int totalHours = Math.abs(h);
        int days = (totalHours / 24) * diff;
        int remainingHours = (totalHours % 24) * diff;
        if (remainingHours < 0) {
            remainingHours += 24;
            days--;
        }
        LunarDay d = day.next(days);
        return fromYmdHms(d.getYear(), d.getMonth(), d.getDay(), remainingHours, minute, second);
    }

    /**
     * Checks if this lunar hour is before another.
     *
     * @param target The other lunar hour.
     * @return {@code true} if this hour is before the target.
     */
    public boolean isBefore(LunarHour target) {
        if (!day.equals(target.getLunarDay())) {
            return day.isBefore(target.getLunarDay());
        }
        if (hour != target.getHour()) {
            return hour < target.getHour();
        }
        return minute != target.getMinute() ? minute < target.getMinute() : second < target.getSecond();
    }

    /**
     * Checks if this lunar hour is after another.
     *
     * @param target The other lunar hour.
     * @return {@code true} if this hour is after the target.
     */
    public boolean isAfter(LunarHour target) {
        if (!day.equals(target.getLunarDay())) {
            return day.isAfter(target.getLunarDay());
        }
        if (hour != target.getHour()) {
            return hour > target.getHour();
        }
        return minute != target.getMinute() ? minute > target.getMinute() : second > target.getSecond();
    }

    /**
     * Gets the Sixty Cycle (Ganzhi) of this hour.
     *
     * @return The {@link SixtyCycle}.
     */
    public SixtyCycle getSixtyCycle() {
        int earthBranchIndex = getIndexInDay() % 12;
        SixtyCycle d = day.getSixtyCycle();
        // The day's stem changes for the first hour block (23:00-00:59).
        if (hour >= 23) {
            d = d.next(1);
        }
        return SixtyCycle.fromName(
                HeavenStem.fromIndex(d.getHeavenStem().getIndex() % 5 * 2 + earthBranchIndex).getName()
                        + EarthBranch.fromIndex(earthBranchIndex).getName());
    }

    /**
     * Gets the Twelve Star (Huang Dao Hei Dao) for this hour.
     *
     * @return The {@link TwelveStar}.
     */
    public TwelveStar getTwelveStar() {
        return TwelveStar.fromIndex(
                getSixtyCycle().getEarthBranch().getIndex()
                        + (8 - getSixtyCycleHour().getDay().getEarthBranch().getIndex() % 6) * 2);
    }

    /**
     * Gets the Nine Star (Jiu Xing) for this hour.
     *
     * @return The {@link NineStar}.
     */
    public NineStar getNineStar() {
        SolarDay solar = day.getSolarDay();
        SolarTerms dongZhi = SolarTerms.fromIndex(solar.getYear(), 0);
        boolean asc = !solar.isBefore(dongZhi.getJulianDay().getSolarDay())
                && solar.isBefore(dongZhi.next(12).getJulianDay().getSolarDay());
        int start = new int[] { 8, 5, 2 }[day.getSixtyCycle().getEarthBranch().getIndex() % 3];
        if (asc) {
            start = 8 - start;
        }
        int earthBranchIndex = getIndexInDay() % 12;
        return NineStar.fromIndex(start + (asc ? earthBranchIndex : -earthBranchIndex));
    }

    /**
     * Gets the corresponding Gregorian (Solar) time.
     *
     * @return The {@link SolarTime}.
     */
    public SolarTime getSolarTime() {
        if (null == solarTime) {
            SolarDay d = day.getSolarDay();
            solarTime = SolarTime.fromYmdHms(d.getYear(), d.getMonth(), d.getDay(), hour, minute, second);
        }
        return solarTime;
    }

    /**
     * Gets the Eight Characters (Bazi) for this specific time.
     *
     * @return The {@link EightChar}.
     */
    public EightChar getEightChar() {
        return provider.getEightChar(this);
    }

    /**
     * Gets the corresponding Sixty Cycle (Ganzhi) hour object.
     *
     * @return The {@link SixtyCycleHour}.
     */
    public SixtyCycleHour getSixtyCycleHour() {
        if (null == sixtyCycleHour) {
            sixtyCycleHour = getSolarTime().getSixtyCycleHour();
        }
        return sixtyCycleHour;
    }

    /**
     * Gets the list of recommended activities for this hour.
     *
     * @return A list of recommended {@link Taboo}s.
     */
    public List<Taboo> getRecommends() {
        return Taboo.getHourRecommends(getSixtyCycleHour().getDay(), getSixtyCycle());
    }

    /**
     * Gets the list of avoided activities for this hour.
     *
     * @return A list of avoided {@link Taboo}s.
     */
    public List<Taboo> getAvoids() {
        return Taboo.getHourAvoids(getSixtyCycleHour().getDay(), getSixtyCycle());
    }

    /**
     * Gets the Minor Liu Ren for this hour.
     *
     * @return The {@link MinorRen}.
     */
    public MinorRen getMinorRen() {
        return getLunarDay().getMinorRen().next(getIndexInDay());
    }

}
