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
package org.miaixz.bus.core.center.date.culture.lunar;

import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.core.center.date.culture.Direction;
import org.miaixz.bus.core.center.date.Galaxy;
import org.miaixz.bus.core.center.date.culture.JulianDay;
import org.miaixz.bus.core.center.date.culture.fetus.FetusMonth;
import org.miaixz.bus.core.center.date.culture.parts.MonthParts;
import org.miaixz.bus.core.center.date.culture.ren.MinorRen;
import org.miaixz.bus.core.center.date.culture.sixty.EarthBranch;
import org.miaixz.bus.core.center.date.culture.sixty.HeavenStem;
import org.miaixz.bus.core.center.date.culture.sixty.SixtyCycle;
import org.miaixz.bus.core.center.date.culture.solar.SolarTerms;
import org.miaixz.bus.core.center.date.culture.star.nine.NineStar;

/**
 * Represents a month in the Lunar calendar.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class LunarMonth extends MonthParts {

    /**
     * Chinese names for lunar months.
     */
    public static final String[] NAMES = { "正月", "二月", "三月", "四月", "五月", "六月", "七月", "八月", "九月", "十月", "十一月", "十二月" };

    /**
     * Indicates whether this is a leap month.
     */
    protected boolean leap;

    /**
     * Constructs a LunarMonth instance.
     *
     * @param year  the lunar year
     * @param month the lunar month (negative value indicates a leap month)
     */
    public LunarMonth(int year, int month) {
        validate(year, month);
        this.year = year;
        this.month = Math.abs(month);
        this.leap = month < 0;
    }

    /**
     * Creates a LunarMonth from year and month.
     *
     * @param year  the lunar year
     * @param month the lunar month (negative value indicates a leap month)
     * @return a new LunarMonth instance
     */
    public static LunarMonth fromYm(int year, int month) {
        return new LunarMonth(year, month);
    }

    /**
     * Validates the lunar year and month.
     *
     * @param year  the lunar year
     * @param month the lunar month
     * @throws IllegalArgumentException if the month is invalid
     */
    public static void validate(int year, int month) {
        if (month == 0 || month > 12 || month < -12) {
            throw new IllegalArgumentException(String.format("illegal lunar month: %d", month));
        }
        // Leap month validation
        if (month < 0 && -month != LunarYear.fromYear(year).getLeapMonth()) {
            throw new IllegalArgumentException(String.format("illegal leap month %d in lunar year %d", -month, year));
        }
    }

    /**
     * Gets the lunar year for this month.
     *
     * @return the lunar year
     */
    public LunarYear getLunarYear() {
        return LunarYear.fromYear(year);
    }

    /**
     * Gets the month with leap indicator.
     *
     * @return the month number (negative if this is a leap month)
     */
    public int getMonthWithLeap() {
        return leap ? -month : month;
    }

    /**
     * Calculates the Julian Day of the new moon for this month.
     *
     * @return the Julian Day
     */
    protected double getNewMoon() {
        // Winter solstice
        double dongZhiJd = SolarTerms.fromIndex(year, 0).getCursoryJulianDay();

        // The first day of lunar month before winter solstice, this year's first sun-moon longitude difference
        double w = Galaxy.calcShuo(dongZhiJd);
        if (w > dongZhiJd) {
            w -= 29.53;
        }

        // Normally the first day of the first lunar month is the 3rd new moon, but there are special cases
        int offset = 2;
        if (year > 8 && year < 24) {
            offset = 1;
        } else if (LunarYear.fromYear(year - 1).getLeapMonth() > 10 && year != 239 && year != 240) {
            offset = 3;
        }

        // The first day of this month
        return w + 29.5306 * (offset + getIndexInYear());
    }

    /**
     * Gets the number of days in this month (30 days for large month, 29 days for small month).
     *
     * @return the number of days
     */
    public int getDayCount() {
        double w = getNewMoon();
        // Days in this month = first day of next month - first day of this month
        return (int) (Galaxy.calcShuo(w + 29.5306) - Galaxy.calcShuo(w));
    }

    /**
     * Gets the index of this month within the year (0-12).
     *
     * @return the index
     */
    public int getIndexInYear() {
        int index = month - 1;
        if (isLeap()) {
            index += 1;
        } else {
            int leapMonth = LunarYear.fromYear(year).getLeapMonth();
            if (leapMonth > 0 && month > leapMonth) {
                index += 1;
            }
        }
        return index;
    }

    /**
     * Gets the lunar quarter for this month.
     *
     * @return the lunar quarter
     */
    public LunarQuarter getQuarter() {
        return LunarQuarter.fromIndex(month - 1);
    }

    /**
     * Gets the Julian Day of the first day of this month.
     *
     * @return the Julian Day
     */
    public JulianDay getFirstJulianDay() {
        return JulianDay.fromJulianDay(JulianDay.J2000 + Galaxy.calcShuo(getNewMoon()));
    }

    /**
     * Checks if this is a leap month.
     *
     * @return true if this is a leap month, false otherwise
     */
    public boolean isLeap() {
        return leap;
    }

    /**
     * Gets the number of weeks in this month.
     *
     * @param start the starting day of week (1-7 for Monday-Sunday, 0 for Sunday)
     * @return the number of weeks
     */
    public int getWeekCount(int start) {
        return (int) Math.ceil((indexOf(getFirstJulianDay().getWeek().getIndex() - start, 7) + getDayCount()) / 7D);
    }

    /**
     * Gets the name of this month following the Chinese national standard "Compilation and Promulgation of the Lunar
     * Calendar" GB/T 33661-2017.
     *
     * @return the name
     */
    public String getName() {
        return (leap ? "闰" : "") + NAMES[month - 1];
    }

    /**
     * Returns the string representation of this object.
     *
     * @return the string representation
     */
    @Override
    public String toString() {
        return getLunarYear() + getName();
    }

    /**
     * Gets the lunar month that is n months after this month.
     *
     * @param n the number of months to advance (can be negative)
     * @return the lunar month after n months
     */
    public LunarMonth next(int n) {
        if (n == 0) {
            return fromYm(year, getMonthWithLeap());
        }
        int m = getIndexInYear() + 1 + n;
        LunarYear y = getLunarYear();
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
     * Gets the list of lunar days in this month.
     *
     * @return the list of lunar days
     */
    public List<LunarDay> getDays() {
        int size = getDayCount();
        int m = getMonthWithLeap();
        List<LunarDay> l = new ArrayList<>(size);
        for (int i = 1; i <= size; i++) {
            l.add(LunarDay.fromYmd(year, m, i));
        }
        return l;
    }

    /**
     * Gets the first day of this month.
     *
     * @return the first lunar day
     */
    public LunarDay getFirstDay() {
        return LunarDay.fromYmd(year, getMonthWithLeap(), 1);
    }

    /**
     * Gets the list of lunar weeks in this month.
     *
     * @param start the starting day of week (1-7 for Monday-Sunday, 0 for Sunday)
     * @return the list of lunar weeks
     */
    public List<LunarWeek> getWeeks(int start) {
        int size = getWeekCount(start);
        int m = getMonthWithLeap();
        List<LunarWeek> l = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            l.add(LunarWeek.fromYm(year, m, i, start));
        }
        return l;
    }

    /**
     * Gets the Sixty Cycle (Gan-Zhi) for this month.
     *
     * @return the Sixty Cycle
     */
    public SixtyCycle getSixtyCycle() {
        return SixtyCycle.fromName(
                HeavenStem.fromIndex(getLunarYear().getSixtyCycle().getHeavenStem().getIndex() * 2 + month + 1)
                        .getName() + EarthBranch.fromIndex(month + 1).getName());
    }

    /**
     * Gets the nine stars for this month.
     *
     * @return the nine star
     */
    public NineStar getNineStar() {
        int index = getSixtyCycle().getEarthBranch().getIndex();
        if (index < 2) {
            index += 3;
        }
        return NineStar.fromIndex(27 - getLunarYear().getSixtyCycle().getEarthBranch().getIndex() % 3 * 3 - index);
    }

    /**
     * Gets the Jupiter direction (Tai Sui position) for this month.
     *
     * @return the direction
     */
    public Direction getJupiterDirection() {
        SixtyCycle sixtyCycle = getSixtyCycle();
        int n = new int[] { 7, -1, 1, 3 }[sixtyCycle.getEarthBranch().next(-2).getIndex() % 4];
        return n != -1 ? Direction.fromIndex(n) : sixtyCycle.getHeavenStem().getDirection();
    }

    /**
     * Gets the fetus spirit for this month.
     *
     * @return the fetus month
     */
    public FetusMonth getFetus() {
        return FetusMonth.fromLunarMonth(this);
    }

    /**
     * Gets the Minor Six Ren divination for this month.
     *
     * @return the Minor Six Ren
     */
    public MinorRen getMinorRen() {
        return MinorRen.fromIndex((month - 1) % 6);
    }

}
