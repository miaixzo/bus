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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.miaixz.bus.core.center.date.culture.Galaxy;
import org.miaixz.bus.core.center.date.culture.Loops;
import org.miaixz.bus.core.center.date.culture.cn.Direction;
import org.miaixz.bus.core.center.date.culture.cn.JulianDay;
import org.miaixz.bus.core.center.date.culture.cn.fetus.FetusMonth;
import org.miaixz.bus.core.center.date.culture.cn.ren.MinorRen;
import org.miaixz.bus.core.center.date.culture.cn.sixty.EarthBranch;
import org.miaixz.bus.core.center.date.culture.cn.sixty.HeavenStem;
import org.miaixz.bus.core.center.date.culture.cn.sixty.SixtyCycle;
import org.miaixz.bus.core.center.date.culture.cn.star.nine.NineStar;
import org.miaixz.bus.core.center.date.culture.solar.SolarTerms;

/**
 * Represents a month in the Lunar calendar.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class LunarMonth extends Loops {

    /**
     * Names of lunar months.
     */
    public static final String[] NAMES = { "正月", "二月", "三月", "四月", "五月", "六月", "七月", "八月", "九月", "十月", "十一月", "腊月" };
    /**
     * Cache for lunar month data.
     */
    private static final Map<String, Object[]> cache = new HashMap<>();
    /**
     * The lunar year this month belongs to.
     */
    protected LunarYear year;

    /**
     * The month number (1-12).
     */
    protected int month;

    /**
     * Indicates if this is a leap month.
     */
    protected boolean leap;

    /**
     * The number of days in this month.
     */
    protected int dayCount;

    /**
     * The index of this month within the year (0-12).
     */
    protected int indexInYear;

    /**
     * The Julian day of the first day of this month.
     */
    protected JulianDay firstJulianDay;

    /**
     * Initializes a new LunarMonth instance from cached data.
     *
     * @param cache Cached data containing lunar year, month (negative for leap), day count, index in year, and Julian
     *              day of the first day.
     */
    protected LunarMonth(Object[] cache) {
        int m = (int) cache[1];
        this.year = LunarYear.fromYear((int) cache[0]);
        this.month = Math.abs(m);
        this.leap = m < 0;
        this.dayCount = (int) cache[2];
        this.indexInYear = (int) cache[3];
        this.firstJulianDay = JulianDay.fromJulianDay((double) cache[4]);
    }

    /**
     * Initializes a new LunarMonth instance from lunar year and month.
     *
     * @param year  The lunar year.
     * @param month The lunar month, negative for leap months.
     * @throws IllegalArgumentException if the month is out of valid range or if a leap month is specified incorrectly.
     */
    public LunarMonth(int year, int month) {
        LunarYear currentYear = LunarYear.fromYear(year);
        int currentLeapMonth = currentYear.getLeapMonth();
        if (month == 0 || month > 12 || month < -12) {
            throw new IllegalArgumentException(String.format("illegal lunar month: %d", month));
        }
        boolean leap = month < 0;
        int m = Math.abs(month);
        if (leap && m != currentLeapMonth) {
            throw new IllegalArgumentException(String.format("illegal leap month %d in lunar year %d", m, year));
        }

        // Winter Solstice
        double dongZhiJd = SolarTerms.fromIndex(year, 0).getCursoryJulianDay();

        // The difference in ecliptic longitude between the first new moon before the Winter Solstice and the current
        // year's first new moon.
        double w = Galaxy.calcShuo(dongZhiJd);
        if (w > dongZhiJd) {
            w -= 29.53;
        }

        // Normally, the first day of the first lunar month is the 3rd new moon, but there are special cases.
        int offset = 2;
        if (year > 8 && year < 24) {
            offset = 1;
        } else if (LunarYear.fromYear(year - 1).getLeapMonth() > 10 && year != 239 && year != 240) {
            offset = 3;
        }

        // Index within the current year
        int index = m - 1;
        if (leap || (currentLeapMonth > 0 && m > currentLeapMonth)) {
            index += 1;
        }
        indexInYear = index;

        // First day of this month
        w += 29.5306 * (offset + index);
        double firstDay = Galaxy.calcShuo(w);
        firstJulianDay = JulianDay.fromJulianDay(JulianDay.J2000 + firstDay);
        // Number of days in this month = Julian day of next month's first day - Julian day of this month's first day
        dayCount = (int) (Galaxy.calcShuo(w + 29.5306) - firstDay);
        this.year = currentYear;
        this.month = m;
        this.leap = leap;
    }

    /**
     * Creates a new LunarMonth instance from lunar year and month.
     *
     * @param year  The lunar year.
     * @param month The lunar month, negative for leap months.
     * @return A new {@link LunarMonth} instance.
     */
    public static LunarMonth fromYm(int year, int month) {
        LunarMonth m;
        String key = String.format("%d%d", year, month);
        Object[] c = cache.get(key);
        if (null != c) {
            m = new LunarMonth(c);
        } else {
            m = new LunarMonth(year, month);
            cache.put(
                    key,
                    new Object[] { m.getYear(), m.getMonthWithLeap(), m.getDayCount(), m.getIndexInYear(),
                            m.getFirstJulianDay().getDay() });
        }
        return m;
    }

    /**
     * Gets the lunar year this month belongs to.
     *
     * @return The {@link LunarYear}.
     */
    public LunarYear getLunarYear() {
        return year;
    }

    /**
     * Gets the year number.
     *
     * @return The year number.
     */
    public int getYear() {
        return year.getYear();
    }

    /**
     * Gets the month number (1-12).
     *
     * @return The month number.
     */
    public int getMonth() {
        return month;
    }

    /**
     * Gets the month number, returning a negative value if it's a leap month.
     *
     * @return The month number, negative for leap months.
     */
    public int getMonthWithLeap() {
        return leap ? -month : month;
    }

    /**
     * Gets the number of days in this lunar month (either 29 or 30).
     *
     * @return The number of days.
     */
    public int getDayCount() {
        return dayCount;
    }

    /**
     * Gets the index of this month within the year (0-12).
     *
     * @return The index.
     */
    public int getIndexInYear() {
        return indexInYear;
    }

    /**
     * Gets the lunar season this month belongs to.
     *
     * @return The {@link LunarSeason}.
     */
    public LunarSeason getSeason() {
        return LunarSeason.fromIndex(month - 1);
    }

    /**
     * Gets the Julian day of the first day of this month.
     *
     * @return The {@link JulianDay} of the first day.
     */
    public JulianDay getFirstJulianDay() {
        return firstJulianDay;
    }

    /**
     * Checks if this is a leap month.
     *
     * @return {@code true} if it's a leap month, {@code false} otherwise.
     */
    public boolean isLeap() {
        return leap;
    }

    /**
     * Gets the number of weeks in this month.
     *
     * @param start The starting day of the week, 1-7 (1 for Monday, 7 for Sunday).
     * @return The number of weeks.
     */
    public int getWeekCount(int start) {
        return (int) Math.ceil((indexOf(firstJulianDay.getWeek().getIndex() - start, 7) + getDayCount()) / 7D);
    }

    /**
     * Gets the name of this lunar month, according to the national standard "Compilation and Promulgation of the Lunar
     * Calendar" GB/T 33661-2017.
     *
     * @return The name of the lunar month.
     */
    public String getName() {
        return (leap ? "闰" : "") + NAMES[month - 1];
    }

    @Override
    public String toString() {
        return year + getName();
    }

    /**
     * Gets the lunar month after a specified number of months.
     *
     * @param n The number of months to add.
     * @return The {@link LunarMonth} after {@code n} months.
     */
    public LunarMonth next(int n) {
        if (n == 0) {
            return fromYm(getYear(), getMonthWithLeap());
        }
        int m = indexInYear + 1 + n;
        LunarYear y = year;
        if (n > 0) {
            int monthCount = y.getMonthCount();
            while (m > monthCount) {
                m -= monthCount;
                y = y.next(1);
                monthCount = y.getMonthCount();
            }
        } else {
            while (m <= 0) {
                y = y.next(-1);
                m += y.getMonthCount();
            }
        }
        boolean leap = false;
        int leapMonth = y.getLeapMonth();
        if (leapMonth > 0) {
            if (m == leapMonth + 1) {
                leap = true;
            }
            if (m > leapMonth) {
                m--;
            }
        }
        return fromYm(y.getYear(), leap ? -m : m);
    }

    /**
     * Gets a list of all lunar days in this month.
     *
     * @return A list of {@link LunarDay} objects for this month.
     */
    public List<LunarDay> getDays() {
        int size = getDayCount();
        int y = getYear();
        int m = getMonthWithLeap();
        List<LunarDay> l = new ArrayList<>(size);
        for (int i = 1; i <= size; i++) {
            l.add(LunarDay.fromYmd(y, m, i));
        }
        return l;
    }

    /**
     * Gets a list of all lunar weeks in this month.
     *
     * @param start The starting day of the week, 1-7 (1 for Monday, 7 for Sunday).
     * @return A list of {@link LunarWeek} objects for this month.
     */
    public List<LunarWeek> getWeeks(int start) {
        int size = getWeekCount(start);
        int y = getYear();
        int m = getMonthWithLeap();
        List<LunarWeek> l = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            l.add(LunarWeek.fromYm(y, m, i, start));
        }
        return l;
    }

    /**
     * Gets the Sixty Cycle (GanZhi) of this lunar month.
     *
     * @return The {@link SixtyCycle} of this month.
     */
    public SixtyCycle getSixtyCycle() {
        return SixtyCycle.fromName(
                HeavenStem.fromIndex((year.getSixtyCycle().getHeavenStem().getIndex() + 1) * 2 + indexInYear).getName()
                        + EarthBranch.fromIndex(indexInYear + 2).getName());
    }

    /**
     * Gets the Nine Star (JiuXing) for this lunar month.
     *
     * @return The {@link NineStar} for this month.
     */
    public NineStar getNineStar() {
        int index = getSixtyCycle().getEarthBranch().getIndex();
        if (index < 2) {
            index += 3;
        }
        return NineStar.fromIndex(27 - year.getSixtyCycle().getEarthBranch().getIndex() % 3 * 3 - index);
    }

    /**
     * Gets the Jupiter Direction (TaiSui方位) for this lunar month.
     *
     * @return The {@link Direction} of Jupiter.
     */
    public Direction getJupiterDirection() {
        SixtyCycle sixtyCycle = getSixtyCycle();
        int n = new int[] { 7, -1, 1, 3 }[sixtyCycle.getEarthBranch().next(-2).getIndex() % 4];
        return n != -1 ? Direction.fromIndex(n) : sixtyCycle.getHeavenStem().getDirection();
    }

    /**
     * Gets the Fetus Month (ZhuYueTaiShen) for this lunar month.
     *
     * @return The {@link FetusMonth} of this month.
     */
    public FetusMonth getFetus() {
        return FetusMonth.fromLunarMonth(this);
    }

    /**
     * Gets the Minor Ren (XiaoLiuRen) for this lunar month.
     *
     * @return The {@link MinorRen} for this month.
     */
    public MinorRen getMinorRen() {
        return MinorRen.fromIndex((month - 1) % 6);
    }

}
