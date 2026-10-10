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
package org.miaixz.bus.core.center.date.culture.lunar;

import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.core.center.date.Galaxy;
import org.miaixz.bus.core.center.date.culture.Direction;
import org.miaixz.bus.core.center.date.culture.JulianDay;
import org.miaixz.bus.core.center.date.culture.fetus.FetusMonth;
import org.miaixz.bus.core.center.date.culture.parts.AbstractLeapMonth;
import org.miaixz.bus.core.center.date.culture.parts.AbstractYear;
import org.miaixz.bus.core.center.date.culture.ren.MinorRen;
import org.miaixz.bus.core.center.date.culture.sixty.SixtyCycle;
import org.miaixz.bus.core.center.date.culture.solar.SolarTerms;
import org.miaixz.bus.core.center.date.culture.star.nine.NineStar;
import org.miaixz.bus.core.lang.Normal;

/**
 * Represents a month in the Lunar calendar.
 *
 * @author Kimi Liu
 */
public class LunarMonth extends AbstractLeapMonth {

    /**
     * Chinese names for lunar months.
     */
    public static final String[] NAMES = { "正月", "二月", "三月", "四月", "五月", "六月", "七月", "八月", "九月", "十月", "十一月", "十二月" };

    /**
     * Constructs a LunarMonth instance.
     *
     * @param year  the lunar year
     * @param month the lunar month (negative value indicates a leap month)
     */
    public LunarMonth(int year, int month) {
        super(year, month);
        validate(year, month);
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
            throw new IllegalArgumentException("illegal lunar month: " + month);
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
     * Gets the abstract year for this month.
     *
     * @return the abstract year
     */
    @Override
    public AbstractYear getAbstractYear() {
        return getLunarYear();
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
     * Gets the name of this month following the Chinese national standard "Compilation and Promulgation of the Lunar
     * Calendar" GB/T 33661-2017.
     *
     * @return the name
     */
    public String getName() {
        return (leap ? "闰" : Normal.EMPTY) + NAMES[month - 1];
    }

    /**
     * Gets the lunar month that is n months after this month.
     *
     * @param n the number of months to advance (can be negative)
     * @return the lunar month after n months
     */
    public LunarMonth next(int n) {
        AbstractLeapMonth m = super.next(n);
        return fromYm(m.getYear(), m.getMonthValue());
    }

    /**
     * Gets the list of lunar days in this month.
     *
     * @return the list of lunar days
     */
    public List<LunarDay> getDays() {
        int size = getDayCount();
        int m = getMonthValue();
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
        return LunarDay.fromYmd(year, getMonthValue(), 1);
    }

    /**
     * Gets the list of lunar weeks in this month.
     *
     * @param start the starting day of week (1-7 for Monday-Sunday, 0 for Sunday)
     * @return the list of lunar weeks
     */
    public List<LunarWeek> getWeeks(int start) {
        int size = getWeekCount(start);
        int m = getMonthValue();
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
        return SixtyCycle.fromIndex(year * 12 + month - 47);
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
