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
package org.miaixz.bus.core.center.date.culture.sixty;

import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.core.center.date.culture.*;
import org.miaixz.bus.core.center.date.culture.fetus.FetusDay;
import org.miaixz.bus.core.center.date.culture.star.nine.NineStar;
import org.miaixz.bus.core.center.date.culture.star.twelve.TwelveStar;
import org.miaixz.bus.core.center.date.culture.star.twentyeight.TwentyEightStar;
import org.miaixz.bus.core.center.date.culture.lunar.LunarDay;
import org.miaixz.bus.core.center.date.culture.lunar.LunarMonth;
import org.miaixz.bus.core.center.date.culture.lunar.LunarYear;
import org.miaixz.bus.core.center.date.culture.solar.SolarDay;
import org.miaixz.bus.core.center.date.culture.solar.SolarTerms;
import org.miaixz.bus.core.center.date.culture.solar.SolarTime;

/**
 * Represents a Sixty-Year Cycle Day (干支日), a traditional Chinese calendar unit. The year changes at the Start of Spring
 * (立春), and the month changes at the beginning of a solar term (节令). This class extends {@link Loops} for cyclical
 * operations.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SixtyCycleDay extends Loops {

    /**
     * The Gregorian day associated with this Sixty-Year Cycle Day.
     */
    protected SolarDay solarDay;

    /**
     * The Sixty-Year Cycle Month associated with this day.
     */
    protected SixtyCycleMonth month;

    /**
     * The Sixty-Year Cycle for the day (日柱).
     */
    protected SixtyCycle day;

    /**
     * Constructs a {@code SixtyCycleDay} instance with the specified Gregorian day, Sixty-Year Cycle Month, and day
     * pillar.
     *
     * @param solarDay The Gregorian day.
     * @param month    The Sixty-Year Cycle Month.
     * @param day      The Sixty-Year Cycle for the day.
     */
    SixtyCycleDay(SolarDay solarDay, SixtyCycleMonth month, SixtyCycle day) {
        this.solarDay = solarDay;
        this.month = month;
        this.day = day;
    }

    /**
     * Constructs a {@code SixtyCycleDay} instance based on a {@link SolarDay}.
     *
     * @param solarDay The Gregorian day.
     */
    public SixtyCycleDay(SolarDay solarDay) {
        int solarYear = solarDay.getYear();
        SolarDay springSolarDay = SolarTerms.fromIndex(solarYear, 3).getSolarDay();
        LunarDay lunarDay = solarDay.getLunarDay();
        LunarYear lunarYear = lunarDay.getLunarMonth().getLunarYear();
        if (lunarYear.getYear() == solarYear) {
            if (solarDay.isBefore(springSolarDay)) {
                lunarYear = lunarYear.next(-1);
            }
        } else if (lunarYear.getYear() < solarYear) {
            if (!solarDay.isBefore(springSolarDay)) {
                lunarYear = lunarYear.next(1);
            }
        }
        SolarTerms term = solarDay.getTerm();
        int index = term.getIndex() - 3;
        if (index < 0 && term.getSolarDay().isAfter(springSolarDay)) {
            index += 24;
        }
        this.solarDay = solarDay;
        this.month = new SixtyCycleMonth(SixtyCycleYear.fromYear(lunarYear.getYear()),
                LunarMonth.fromYm(solarYear, 1).getSixtyCycle().next((int) Math.floor(index * 0.5)));
        this.day = lunarDay.getSixtyCycle();
    }

    /**
     * Creates a {@code SixtyCycleDay} instance from a {@link SolarDay}.
     *
     * @param solarDay The Gregorian day.
     * @return A new {@code SixtyCycleDay} instance.
     */
    public static SixtyCycleDay fromSolarDay(SolarDay solarDay) {
        return new SixtyCycleDay(solarDay);
    }

    /**
     * Gets the Gregorian day associated with this Sixty-Year Cycle Day.
     *
     * @return The {@link SolarDay} instance.
     */
    public SolarDay getSolarDay() {
        return solarDay;
    }

    /**
     * Gets the Sixty-Year Cycle Month associated with this day.
     *
     * @return The {@link SixtyCycleMonth} instance.
     */
    public SixtyCycleMonth getSixtyCycleMonth() {
        return month;
    }

    /**
     * Gets the Sixty-Year Cycle for the year (年柱).
     *
     * @return The {@link SixtyCycle} instance representing the year pillar.
     */
    public SixtyCycle getYear() {
        return month.getYear();
    }

    /**
     * Gets the Sixty-Year Cycle for the month (月柱).
     *
     * @return The {@link SixtyCycle} instance representing the month pillar.
     */
    public SixtyCycle getMonth() {
        return month.getSixtyCycle();
    }

    /**
     * Gets the Sixty-Year Cycle for the day (日柱).
     *
     * @return The {@link SixtyCycle} instance representing the day pillar.
     */
    public SixtyCycle getSixtyCycle() {
        return day;
    }

    /**
     * Gets the name of this Sixty-Year Cycle Day.
     *
     * @return The name of the day as a formatted string.
     */
    public String getName() {
        return String.format("%s日", day);
    }

    /**
     * Returns a string representation of this Sixty-Year Cycle Day, including the month and day.
     *
     * @return A string representation of the Sixty-Year Cycle Day.
     */
    @Override
    public String toString() {
        return String.format("%s%s", month, getName());
    }

    /**
     * Gets the Duty (建除十二值神) associated with this day.
     *
     * @return The {@link Duty} instance.
     */
    public Duty getDuty() {
        return Duty.fromIndex(day.getEarthBranch().getIndex() - getMonth().getEarthBranch().getIndex());
    }

    /**
     * Gets the Twelve Star (黄道黑道十二神) associated with this day.
     *
     * @return The {@link TwelveStar} instance.
     */
    public TwelveStar getTwelveStar() {
        return TwelveStar
                .fromIndex(day.getEarthBranch().getIndex() + (8 - getMonth().getEarthBranch().getIndex() % 6) * 2);
    }

    /**
     * Gets the Nine Star (九星) associated with this day.
     *
     * @return The {@link NineStar} instance.
     */
    public NineStar getNineStar() {
        SolarTerms dongZhi = SolarTerms.fromIndex(solarDay.getYear(), 0);
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
        if (!solarDay.isBefore(solarShunBai) && solarDay.isBefore(solarNiZi)) {
            offset = solarDay.subtract(solarShunBai);
        } else if (!solarDay.isBefore(solarNiZi) && solarDay.isBefore(solarShunBai2)) {
            offset = 8 - solarDay.subtract(solarNiZi);
        } else if (!solarDay.isBefore(solarShunBai2)) {
            offset = solarDay.subtract(solarShunBai2);
        } else if (solarDay.isBefore(solarShunBai)) {
            offset = 8 + solarShunBai.subtract(solarDay);
        }
        return NineStar.fromIndex(offset);
    }

    /**
     * Gets the direction of Jupiter (太岁方位) for this day.
     *
     * @return The {@link Direction} of Jupiter.
     */
    public Direction getJupiterDirection() {
        int index = day.getIndex();
        return index % 12 < 6 ? Element.fromIndex(index / 12).getDirection()
                : month.getSixtyCycleYear().getJupiterDirection();
    }

    /**
     * Gets the daily Fetus God (逐日胎神) for this day.
     *
     * @return The {@link FetusDay} instance.
     */
    public FetusDay getFetusDay() {
        return FetusDay.fromSixtyCycleDay(this);
    }

    /**
     * Gets the Twenty-Eight Mansions (二十八宿) associated with this day.
     *
     * @return The {@link TwentyEightStar} instance.
     */
    public TwentyEightStar getTwentyEightStar() {
        return TwentyEightStar.fromIndex(new int[] { 10, 18, 26, 6, 14, 22, 2 }[solarDay.getWeek().getIndex()])
                .next(-7 * day.getEarthBranch().getIndex());
    }

    /**
     * Gets the list of Gods and Evils (神煞列表) for this day, indicating auspicious (吉神宜趋) and inauspicious (凶神宜忌) ones.
     *
     * @return A list of {@link God} objects.
     */
    public List<God> getGods() {
        return God.getDayGods(getMonth(), day);
    }

    /**
     * Gets the list of recommended activities (宜) for this day.
     *
     * @return A list of {@link Taboo} objects representing recommended activities.
     */
    public List<Taboo> getRecommends() {
        return Taboo.getDayRecommends(getMonth(), day);
    }

    /**
     * Gets the list of activities to avoid (忌) for this day.
     *
     * @return A list of {@link Taboo} objects representing activities to avoid.
     */
    public List<Taboo> getAvoids() {
        return Taboo.getDayAvoids(getMonth(), day);
    }

    /**
     * Gets the next {@code SixtyCycleDay} by adding a specified number of days.
     *
     * @param n The number of days to add.
     * @return The next {@code SixtyCycleDay} instance.
     */
    @Override
    public SixtyCycleDay next(int n) {
        return fromSolarDay(solarDay.next(n));
    }

    /**
     * Gets a list of all Sixty-Year Cycle Hours (干支时辰列表) for this day.
     *
     * @return A list of {@link SixtyCycleHour} objects.
     */
    public List<SixtyCycleHour> getHours() {
        List<SixtyCycleHour> l = new ArrayList<>();
        SolarDay d = solarDay.next(-1);
        SixtyCycleHour h = SixtyCycleHour
                .fromSolarTime(SolarTime.fromYmdHms(d.getYear(), d.getMonth(), d.getDay(), 23, 0, 0));
        l.add(h);
        for (int i = 0; i < 11; i++) {
            h = h.next(7200);
            l.add(h);
        }
        return l;
    }

    /**
     * The Three Pillars (Year, Month, Day).
     * 
     * @return the {@link ThreePillars}
     */
    public ThreePillars getThreePillars() {
        return new ThreePillars(getYear(), getMonth(), getSixtyCycle());
    }

}
