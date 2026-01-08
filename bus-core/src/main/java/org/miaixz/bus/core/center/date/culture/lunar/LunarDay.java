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

import org.miaixz.bus.core.center.date.culture.*;
import org.miaixz.bus.core.center.date.culture.Week;
import org.miaixz.bus.core.center.date.culture.fetus.FetusDay;
import org.miaixz.bus.core.center.date.culture.parts.DayParts;
import org.miaixz.bus.core.center.date.culture.ren.MinorRen;
import org.miaixz.bus.core.center.date.culture.sixty.*;
import org.miaixz.bus.core.center.date.culture.solar.SolarTerms;
import org.miaixz.bus.core.center.date.culture.star.nine.NineStar;
import org.miaixz.bus.core.center.date.culture.star.six.SixStar;
import org.miaixz.bus.core.center.date.culture.star.twelve.TwelveStar;
import org.miaixz.bus.core.center.date.culture.star.twentyeight.TwentyEightStar;
import org.miaixz.bus.core.center.date.culture.solar.SolarDay;

/**
 * Represents a day in the Lunar calendar.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class LunarDay extends DayParts {

    /**
     * Chinese names for lunar days from 1 to 30.
     */
    public static final String[] NAMES = { "初一", "初二", "初三", "初四", "初五", "初六", "初七", "初八", "初九", "初十", "十一", "十二", "十三",
            "十四", "十五", "十六", "十七", "十八", "十九", "二十", "廿一", "廿二", "廿三", "廿四", "廿五", "廿六", "廿七", "廿八", "廿九", "三十" };

    /**
     * Constructs a LunarDay instance.
     *
     * @param year  the lunar year
     * @param month the lunar month (negative value indicates a leap month)
     * @param day   the lunar day (1-30)
     * @throws IllegalArgumentException if the day is invalid for the given month/year
     */
    public LunarDay(int year, int month, int day) {
        validate(year, month, day);
        this.year = year;
        this.month = month;
        this.day = day;
    }

    /**
     * Creates a LunarDay from year, month, and day components.
     *
     * @param year  the lunar year
     * @param month the lunar month (negative value indicates a leap month)
     * @param day   the lunar day
     * @return a new LunarDay instance
     */
    public static LunarDay fromYmd(int year, int month, int day) {
        return new LunarDay(year, month, day);
    }

    /**
     * Validates the lunar year, month, and day.
     *
     * @param year  the lunar year
     * @param month the lunar month
     * @param day   the lunar day
     * @throws IllegalArgumentException if the day is invalid
     */
    public static void validate(int year, int month, int day) {
        if (day < 1) {
            throw new IllegalArgumentException(String.format("illegal lunar day %d", day));
        }
        LunarMonth m = LunarMonth.fromYm(year, month);
        if (day > m.getDayCount()) {
            throw new IllegalArgumentException(String.format("illegal day %d in %s", day, m));
        }
    }

    /**
     * Gets the lunar month for this day.
     *
     * @return the lunar month
     */
    public LunarMonth getLunarMonth() {
        return LunarMonth.fromYm(year, month);
    }

    /**
     * Gets the Chinese name of this lunar day.
     *
     * @return the Chinese name (e.g., "初一", "十五")
     */
    public String getName() {
        return NAMES[day - 1];
    }

    /**
     * Returns the string representation of this object.
     *
     * @return the string representation
     */
    @Override
    public String toString() {
        return getLunarMonth() + getName();
    }

    /**
     * Gets the lunar day that is n days after this day.
     *
     * @param n the number of days to advance (can be negative)
     * @return the lunar day after n days
     */
    public LunarDay next(int n) {
        return getSolarDay().next(n).getLunarDay();
    }

    /**
     * Checks if this lunar day is before the target lunar day.
     *
     * @param target the lunar day to compare with
     * @return true if this day is before the target, false otherwise
     */
    public boolean isBefore(LunarDay target) {
        if (year != target.getYear()) {
            return year < target.getYear();
        }
        if (month != target.getMonth()) {
            return Math.abs(month) < Math.abs(target.getMonth());
        }
        return day < target.getDay();
    }

    /**
     * Checks if this lunar day is after the target lunar day.
     *
     * @param target the lunar day to compare with
     * @return true if this day is after the target, false otherwise
     */
    public boolean isAfter(LunarDay target) {
        if (year != target.getYear()) {
            return year > target.getYear();
        }
        if (month != target.getMonth()) {
            return Math.abs(month) >= Math.abs(target.getMonth());
        }
        return day > target.getDay();
    }

    /**
     * Gets the day of week for this lunar day.
     *
     * @return the day of week
     */
    public Week getWeek() {
        return getSolarDay().getWeek();
    }

    /**
     * Gets the year's Sixty Cycle (Gan-Zhi) for this day (changes on Lichun).
     *
     * @return the Sixty Cycle
     * @see SixtyCycleDay#getYear()
     * @deprecated Use {@link SixtyCycleDay#getYear()} instead
     */
    @Deprecated
    public SixtyCycle getYearSixtyCycle() {
        return getSixtyCycleDay().getYear();
    }

    /**
     * Gets the month's Sixty Cycle (Gan-Zhi) for this day (changes on solar terms).
     *
     * @return the Sixty Cycle
     * @see SixtyCycleDay#getMonth()
     * @deprecated Use {@link SixtyCycleDay#getMonth()} instead
     */
    @Deprecated
    public SixtyCycle getMonthSixtyCycle() {
        return getSixtyCycleDay().getMonth();
    }

    /**
     * Gets the Sixty Cycle (Gan-Zhi) for this day.
     *
     * @return the Sixty Cycle
     */
    public SixtyCycle getSixtyCycle() {
        int offset = (int) getLunarMonth().getFirstJulianDay().next(day - 12).getDay();
        return SixtyCycle.fromName(HeavenStem.fromIndex(offset).getName() + EarthBranch.fromIndex(offset).getName());
    }

    /**
     * Gets the twelve duty spirits (Jian-Chu twelve value gods).
     *
     * @return the duty spirit
     * @see SixtyCycleDay
     */
    public Duty getDuty() {
        return getSixtyCycleDay().getDuty();
    }

    /**
     * Gets the twelve stars (Huangdao-Heidao twelve gods).
     *
     * @return the twelve star spirit
     * @see SixtyCycleDay
     */
    public TwelveStar getTwelveStar() {
        return getSixtyCycleDay().getTwelveStar();
    }

    /**
     * Gets the nine stars for this day.
     *
     * @return the nine star
     */
    public NineStar getNineStar() {
        SolarDay d = getSolarDay();
        SolarTerms dongZhi = SolarTerms.fromIndex(d.getYear(), 0);
        SolarDay dongZhiSolar = dongZhi.getSolarDay();
        SolarDay xiaZhiSolar = dongZhi.next(12).getSolarDay();
        SolarDay dongZhiSolar2 = dongZhi.next(24).getSolarDay();
        int dongZhiIndex = dongZhiSolar.getLunarDay().getSixtyCycle().getIndex();
        int xiaZhiIndex = xiaZhiSolar.getLunarDay().getSixtyCycle().getIndex();
        int dongZhiIndex2 = dongZhiSolar2.getLunarDay().getSixtyCycle().getIndex();
        SolarDay solarShunBai = dongZhiSolar.next(dongZhiIndex > 29 ? 60 - dongZhiIndex : -dongZhiIndex);
        SolarDay solarShunBai2 = dongZhiSolar2.next(dongZhiIndex2 > 29 ? 60 - dongZhiIndex2 : -dongZhiIndex2);
        SolarDay solarNiZi = xiaZhiSolar.next(xiaZhiIndex > 29 ? 60 - xiaZhiIndex : -xiaZhiIndex);
        int offset = 0;
        if (!d.isBefore(solarShunBai) && d.isBefore(solarNiZi)) {
            offset = d.subtract(solarShunBai);
        } else if (!d.isBefore(solarNiZi) && d.isBefore(solarShunBai2)) {
            offset = 8 - d.subtract(solarNiZi);
        } else if (!d.isBefore(solarShunBai2)) {
            offset = d.subtract(solarShunBai2);
        } else if (d.isBefore(solarShunBai)) {
            offset = 8 + solarShunBai.subtract(d);
        }
        return NineStar.fromIndex(offset);
    }

    /**
     * Gets the Jupiter direction (Tai Sui position) for this day.
     *
     * @return the direction
     */
    public Direction getJupiterDirection() {
        int index = getSixtyCycle().getIndex();
        return index % 12 < 6 ? Element.fromIndex(index / 12).getDirection()
                : LunarYear.fromYear(year).getJupiterDirection();
    }

    /**
     * Gets the fetus spirit for this day.
     *
     * @return the fetus day
     */
    public FetusDay getFetusDay() {
        return FetusDay.fromLunarDay(this);
    }

    /**
     * Gets the moon phase day information.
     *
     * @return the moon phase day
     */
    public Phase.PhaseDay getPhaseDay() {
        SolarDay today = getSolarDay();
        LunarMonth m = getLunarMonth().next(1);
        Phase p = Phase.fromIndex(m.getYear(), m.getMonthWithLeap(), 0);
        SolarDay d = p.getSolarDay();
        while (d.isAfter(today)) {
            p = p.next(-1);
            d = p.getSolarDay();
        }
        return new Phase.PhaseDay(p, today.subtract(d));
    }

    /**
     * Gets the moon phase for this day.
     *
     * @return the moon phase
     */
    public Phase getPhase() {
        return getPhaseDay().getPhase();
    }

    /**
     * Gets the six stars for this day.
     *
     * @return the six star
     */
    public SixStar getSixStar() {
        return SixStar.fromIndex((Math.abs(month) + day - 2) % 6);
    }

    /**
     * Gets the solar day corresponding to this lunar day.
     *
     * @return the solar day
     */
    public SolarDay getSolarDay() {
        return getLunarMonth().getFirstJulianDay().next(day - 1).getSolarDay();
    }

    /**
     * Gets the Sixty Cycle day for this lunar day.
     *
     * @return the Sixty Cycle day
     */
    public SixtyCycleDay getSixtyCycleDay() {
        return getSolarDay().getSixtyCycleDay();
    }

    /**
     * Gets the twenty-eight star mansions for this day.
     *
     * @return the twenty-eight star mansion
     */
    public TwentyEightStar getTwentyEightStar() {
        return TwentyEightStar.fromIndex(new int[] { 10, 18, 26, 6, 14, 22, 2 }[getSolarDay().getWeek().getIndex()])
                .next(-7 * getSixtyCycle().getEarthBranch().getIndex());
    }

    /**
     * Gets the lunar festival for this day, if any.
     *
     * @return the lunar festival, or null if this day is not a festival
     */
    public LunarFestival getFestival() {
        return LunarFestival.fromYmd(year, month, day);
    }

    /**
     * Gets the list of lunar hours for this day.
     *
     * @return the list of 12 two-hour periods (shichen) in this day
     */
    public List<LunarHour> getHours() {
        List<LunarHour> l = new ArrayList<>();
        l.add(LunarHour.fromYmdHms(year, month, day, 0, 0, 0));
        for (int i = 0; i < 24; i += 2) {
            l.add(LunarHour.fromYmdHms(year, month, day, i + 1, 0, 0));
        }
        return l;
    }

    /**
     * Gets the list of gods and spirits (auspicious and inauspicious) for this day.
     *
     * @return the list of gods
     * @see SixtyCycleDay#getGods()
     */
    public List<God> getGods() {
        return getSixtyCycleDay().getGods();
    }

    /**
     * Gets the list of recommended activities for this day.
     *
     * @return the list of taboos representing recommended activities
     * @see SixtyCycleDay#getRecommends()
     */
    public List<Taboo> getRecommends() {
        return getSixtyCycleDay().getRecommends();
    }

    /**
     * Gets the list of activities to avoid for this day.
     *
     * @return the list of taboos representing activities to avoid
     * @see SixtyCycleDay#getAvoids()
     */
    public List<Taboo> getAvoids() {
        return getSixtyCycleDay().getAvoids();
    }

    /**
     * Gets the Minor Six Ren divination for this day.
     *
     * @return the Minor Six Ren
     */
    public MinorRen getMinorRen() {
        return getLunarMonth().getMinorRen().next(day - 1);
    }

    /**
     * Gets the three pillars (year, month, day pillars) for this day.
     *
     * @return the three pillars
     */
    public ThreePillars getThreePillars() {
        return getSixtyCycleDay().getThreePillars();
    }

}
