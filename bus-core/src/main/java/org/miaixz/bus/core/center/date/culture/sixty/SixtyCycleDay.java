/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.core.center.date.culture.sixty;

import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.core.center.date.culture.*;
import org.miaixz.bus.core.center.date.culture.fetus.FetusDay;
import org.miaixz.bus.core.center.date.culture.solar.SolarDay;
import org.miaixz.bus.core.center.date.culture.solar.SolarTerms;
import org.miaixz.bus.core.center.date.culture.solar.SolarTime;
import org.miaixz.bus.core.center.date.culture.star.nine.NineStar;
import org.miaixz.bus.core.center.date.culture.star.twelve.TwelveStar;
import org.miaixz.bus.core.center.date.culture.star.twentyeight.TwentyEightStar;

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
        SolarTerms term = solarDay.getTerm();
        int index = term.getIndex();
        int offset = index < 3 ? (index == 0 ? -2 : -1) : ((index - 3) / 2);
        this.solarDay = solarDay;
        this.month = SixtyCycleYear.fromYear(term.getYear()).getFirstMonth().next(offset);
        this.day = SixtyCycle.fromIndex(solarDay.subtract(SolarDay.fromYmd(2000, 1, 7)));
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
