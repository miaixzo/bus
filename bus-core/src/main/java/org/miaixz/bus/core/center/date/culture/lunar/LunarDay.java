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
import java.util.List;

import org.miaixz.bus.core.center.date.culture.Loops;
import org.miaixz.bus.core.center.date.culture.cn.*;
import org.miaixz.bus.core.center.date.culture.cn.fetus.FetusDay;
import org.miaixz.bus.core.center.date.culture.cn.ren.MinorRen;
import org.miaixz.bus.core.center.date.culture.cn.sixty.*;
import org.miaixz.bus.core.center.date.culture.cn.star.nine.NineStar;
import org.miaixz.bus.core.center.date.culture.cn.star.six.SixStar;
import org.miaixz.bus.core.center.date.culture.cn.star.twelve.TwelveStar;
import org.miaixz.bus.core.center.date.culture.cn.star.twentyeight.TwentyEightStar;
import org.miaixz.bus.core.center.date.culture.solar.SolarDay;
import org.miaixz.bus.core.center.date.culture.solar.SolarTerms;

/**
 * Represents a day in the Lunar calendar.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class LunarDay extends Loops {

    /**
     * Names of the days in a lunar month.
     */
    public static final String[] NAMES = { "初一", "初二", "初三", "初四", "初五", "初六", "初七", "初八", "初九", "初十", "十一", "十二", "十三",
            "十四", "十五", "十六", "十七", "十八", "十九", "二十", "廿一", "廿二", "廿三", "廿四", "廿五", "廿六", "廿七", "廿八", "廿九", "三十" };

    /**
     * The lunar month this day belongs to.
     */
    protected LunarMonth month;

    /**
     * The day of the month (1-30).
     */
    protected int day;

    /**
     * The corresponding Gregorian (Solar) day, lazily initialized.
     */
    protected SolarDay solarDay;

    /**
     * The corresponding Sixty Cycle (Ganzhi) day, lazily initialized.
     */
    protected SixtyCycleDay sixtyCycleDay;

    /**
     * Constructs a {@code LunarDay} from a year, month, and day.
     *
     * @param year  The lunar year.
     * @param month The lunar month (a negative value indicates a leap month).
     * @param day   The lunar day.
     */
    public LunarDay(int year, int month, int day) {
        LunarMonth m = LunarMonth.fromYm(year, month);
        if (day < 1 || day > m.getDayCount()) {
            throw new IllegalArgumentException(String.format("illegal day %d in %s", day, m));
        }
        this.month = m;
        this.day = day;
    }

    /**
     * Creates a {@code LunarDay} from a year, month, and day.
     *
     * @param year  The lunar year.
     * @param month The lunar month (a negative value indicates a leap month).
     * @param day   The lunar day.
     * @return a new {@code LunarDay} instance.
     */
    public static LunarDay fromYmd(int year, int month, int day) {
        return new LunarDay(year, month, day);
    }

    /**
     * Gets the lunar month object.
     *
     * @return The {@link LunarMonth}.
     */
    public LunarMonth getLunarMonth() {
        return month;
    }

    /**
     * Gets the year.
     *
     * @return The lunar year.
     */
    public int getYear() {
        return month.getYear();
    }

    /**
     * Gets the month.
     *
     * @return The lunar month (a negative value indicates a leap month).
     */
    public int getMonth() {
        return month.getMonthWithLeap();
    }

    /**
     * Gets the day.
     *
     * @return The day of the month (1-30).
     */
    public int getDay() {
        return day;
    }

    /**
     * Gets the name of the day (e.g., "初一").
     *
     * @return The name of the day.
     */
    public String getName() {
        return NAMES[day - 1];
    }

    @Override
    public String toString() {
        return month + getName();
    }

    @Override
    public LunarDay next(int n) {
        return getSolarDay().next(n).getLunarDay();
    }

    /**
     * Checks if this lunar day is before another.
     *
     * @param target The other lunar day.
     * @return {@code true} if this day is before the target day.
     */
    public boolean isBefore(LunarDay target) {
        int aYear = getYear();
        int bYear = target.getYear();
        if (aYear != bYear) {
            return aYear < bYear;
        }
        int aMonth = getMonth();
        int bMonth = target.getMonth();
        if (aMonth != bMonth) {
            return Math.abs(aMonth) < Math.abs(bMonth);
        }
        return day < target.getDay();
    }

    /**
     * Checks if this lunar day is after another.
     *
     * @param target The other lunar day.
     * @return {@code true} if this day is after the target day.
     */
    public boolean isAfter(LunarDay target) {
        int aYear = getYear();
        int bYear = target.getYear();
        if (aYear != bYear) {
            return aYear > bYear;
        }
        int aMonth = getMonth();
        int bMonth = target.getMonth();
        if (aMonth != bMonth) {
            return Math.abs(aMonth) >= Math.abs(bMonth);
        }
        return day > target.getDay();
    }

    /**
     * Gets the day of the week.
     *
     * @return The {@link Week}.
     */
    public Week getWeek() {
        return getSolarDay().getWeek();
    }

    /**
     * Gets the Sixty Cycle (Ganzhi) of this day.
     *
     * @return The {@link SixtyCycle}.
     */
    public SixtyCycle getSixtyCycle() {
        int offset = (int) month.getFirstJulianDay().next(day - 12).getDay();
        return SixtyCycle.fromName(HeavenStem.fromIndex(offset).getName() + EarthBranch.fromIndex(offset).getName());
    }

    /**
     * Gets the Duty (Jian Chu) of this day.
     *
     * @return The {@link Duty}.
     */
    public Duty getDuty() {
        return getSixtyCycleDay().getDuty();
    }

    /**
     * Gets the Twelve Star (Huang Dao Hei Dao) of this day.
     *
     * @return The {@link TwelveStar}.
     */
    public TwelveStar getTwelveStar() {
        return getSixtyCycleDay().getTwelveStar();
    }

    /**
     * Gets the Nine Star (Jiu Xing) of this day.
     *
     * @return The {@link NineStar}.
     */
    public NineStar getNineStar() {
        SolarDay d = getSolarDay();
        SolarTerms dongZhi = SolarTerms.fromIndex(d.getYear(), 0);
        SolarDay dongZhiSolar = dongZhi.getJulianDay().getSolarDay();
        SolarDay xiaZhiSolar = dongZhi.next(12).getJulianDay().getSolarDay();
        SolarDay dongZhiSolar2 = dongZhi.next(24).getJulianDay().getSolarDay();
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
     * Gets the direction of Jupiter (Tai Sui) for this day.
     *
     * @return The {@link Direction}.
     */
    public Direction getJupiterDirection() {
        int index = getSixtyCycle().getIndex();
        return index % 12 < 6 ? Element.fromIndex(index / 12).getDirection()
                : month.getLunarYear().getJupiterDirection();
    }

    /**
     * Gets the Daily Fetus Spirit (Tai Shen).
     *
     * @return The {@link FetusDay}.
     */
    public FetusDay getFetusDay() {
        return FetusDay.fromLunarDay(this);
    }

    /**
     * Gets the day index within the lunar phase.
     *
     * @return The day index within the lunar phase.
     */
    public Phase.PhaseDay getPhaseDay() {
        SolarDay today = getSolarDay();
        LunarMonth m = month.next(1);
        Phase p = Phase.fromIndex(m.getYear(), m.getMonthWithLeap(), 0);
        SolarDay d = p.getSolarDay();
        while (d.isAfter(today)) {
            p = p.next(-1);
            d = p.getSolarDay();
        }
        return new Phase.PhaseDay(p, today.subtract(d));
    }

    /**
     * Gets the lunar phase (Moon Phase).
     *
     * @return The {@link Phase}.
     */
    public Phase getPhase() {
        return getPhaseDay().getPhase();
    }

    /**
     * Gets the Six Star (Liu Yao).
     *
     * @return The {@link SixStar}.
     */
    public SixStar getSixStar() {
        return SixStar.fromIndex((month.getMonth() + day - 2) % 6);
    }

    /**
     * Gets the corresponding Gregorian (Solar) day.
     *
     * @return The {@link SolarDay}.
     */
    public SolarDay getSolarDay() {
        if (null == solarDay) {
            solarDay = month.getFirstJulianDay().next(day - 1).getSolarDay();
        }
        return solarDay;
    }

    /**
     * Gets the corresponding Sixty Cycle (Ganzhi) day object.
     *
     * @return The {@link SixtyCycleDay}.
     */
    public SixtyCycleDay getSixtyCycleDay() {
        if (null == sixtyCycleDay) {
            sixtyCycleDay = getSolarDay().getSixtyCycleDay();
        }
        return sixtyCycleDay;
    }

    /**
     * Gets the Twenty-Eight Mansions (Xiu) for this day.
     *
     * @return The {@link TwentyEightStar}.
     */
    public TwentyEightStar getTwentyEightStar() {
        return TwentyEightStar.fromIndex(new int[] { 10, 18, 26, 6, 14, 22, 2 }[getSolarDay().getWeek().getIndex()])
                .next(-7 * getSixtyCycle().getEarthBranch().getIndex());
    }

    /**
     * Gets the traditional lunar festival for this day, if any.
     *
     * @return The {@link LunarFestival}, or null if this day is not a festival.
     */
    public LunarFestival getFestival() {
        return LunarFestival.fromYmd(getYear(), getMonth(), day);
    }

    /**
     * Gets the list of {@code LunarHour} objects for this day.
     *
     * @return A list of {@link LunarHour} objects.
     */
    public List<LunarHour> getHours() {
        List<LunarHour> l = new ArrayList<>();
        int y = getYear();
        int m = getMonth();
        l.add(LunarHour.fromYmdHms(y, m, day, 0, 0, 0));
        for (int i = 0; i < 24; i += 2) {
            l.add(LunarHour.fromYmdHms(y, m, day, i + 1, 0, 0));
        }
        return l;
    }

    /**
     * Gets the list of Gods (Shen Sha) for this day.
     *
     * @return A list of {@link God}s.
     */
    public List<God> getGods() {
        return getSixtyCycleDay().getGods();
    }

    /**
     * Gets the list of recommended activities for this day.
     *
     * @return A list of recommended {@link Taboo}s.
     */
    public List<Taboo> getRecommends() {
        return getSixtyCycleDay().getRecommends();
    }

    /**
     * Gets the list of avoided activities for this day.
     *
     * @return A list of avoided {@link Taboo}s.
     */
    public List<Taboo> getAvoids() {
        return getSixtyCycleDay().getAvoids();
    }

    /**
     * Gets the Minor Liu Ren for this day.
     *
     * @return The {@link MinorRen}.
     */
    public MinorRen getMinorRen() {
        return getLunarMonth().getMinorRen().next(day - 1);
    }

    /**
     * Gets the Three Pillars (Year, Month, Day).
     *
     * @return The {@link ThreePillars}.
     */
    public ThreePillars getThreePillars() {
        return getSixtyCycleDay().getThreePillars();
    }

}
