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
package org.miaixz.bus.core.center.date.culture.sixty;

import java.util.List;

import org.miaixz.bus.core.center.date.culture.Loops;
import org.miaixz.bus.core.center.date.culture.Taboo;
import org.miaixz.bus.core.center.date.culture.eightchar.EightChar;
import org.miaixz.bus.core.center.date.culture.lunar.LunarDay;
import org.miaixz.bus.core.center.date.culture.lunar.LunarHour;
import org.miaixz.bus.core.center.date.culture.lunar.LunarMonth;
import org.miaixz.bus.core.center.date.culture.lunar.LunarYear;
import org.miaixz.bus.core.center.date.culture.solar.SolarDay;
import org.miaixz.bus.core.center.date.culture.solar.SolarTerms;
import org.miaixz.bus.core.center.date.culture.solar.SolarTime;
import org.miaixz.bus.core.center.date.culture.star.nine.NineStar;
import org.miaixz.bus.core.center.date.culture.star.twelve.TwelveStar;

/**
 * Represents a Sixty-Year Cycle hour, a traditional Chinese timekeeping unit. The year changes at the Start of
 * Spring, the month changes at the beginning of a solar term, and the day changes at 23:00. This class
 * extends {@link Loops} for cyclical operations.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class SixtyCycleHour extends Loops {

    /**
     * The Gregorian time associated with this Sixty-Year Cycle Hour.
     */
    protected SolarTime solarTime;

    /**
     * The Sixty-Year Cycle Day associated with this hour.
     */
    protected SixtyCycleDay day;

    /**
     * The hour pillar within the Sixty-Year Cycle.
     */
    protected SixtyCycle hour;

    /**
     * Constructs a {@code SixtyCycleHour} instance with the specified Gregorian time.
     *
     * @param solarTime The Gregorian time.
     */
    public SixtyCycleHour(SolarTime solarTime) {
        int solarYear = solarTime.getYear();
        SolarTime springSolarTime = SolarTerms.fromIndex(solarYear, 3).getJulianDay().getSolarTime();
        LunarHour lunarHour = solarTime.getLunarHour();
        LunarDay lunarDay = lunarHour.getLunarDay();
        LunarYear lunarYear = lunarDay.getLunarMonth().getLunarYear();
        if (lunarYear.getYear() == solarYear) {
            if (solarTime.isBefore(springSolarTime)) {
                lunarYear = lunarYear.next(-1);
            }
        } else if (lunarYear.getYear() < solarYear) {
            if (!solarTime.isBefore(springSolarTime)) {
                lunarYear = lunarYear.next(1);
            }
        }

        SolarTerms term = solarTime.getTerm();
        int index = term.getIndex() - 3;
        if (index < 0 && term.getJulianDay().getSolarTime()
                .isAfter(SolarTerms.fromIndex(solarYear, 3).getJulianDay().getSolarTime())) {
            index += 24;
        }
        SixtyCycle d = lunarDay.getSixtyCycle();
        this.solarTime = solarTime;
        this.day = new SixtyCycleDay(solarTime.getSolarDay(),
                new SixtyCycleMonth(SixtyCycleYear.fromYear(lunarYear.getYear()),
                        LunarMonth.fromYm(solarYear, 1).getSixtyCycle().next((int) Math.floor(index * 0.5))),
                solarTime.getHour() < 23 ? d : d.next(1));
        this.hour = lunarHour.getSixtyCycle();
    }

    /**
     * Creates a {@code SixtyCycleHour} instance from a {@link SolarTime}.
     *
     * @param solarTime The Gregorian time.
     * @return A new {@code SixtyCycleHour} instance.
     */
    public static SixtyCycleHour fromSolarTime(SolarTime solarTime) {
        return new SixtyCycleHour(solarTime);
    }

    /**
     * Gets the year pillar.
     *
     * @return The {@link SixtyCycle} instance representing the year.
     */
    public SixtyCycle getYear() {
        return day.getYear();
    }

    /**
     * Gets the month pillar.
     *
     * @return The {@link SixtyCycle} instance representing the month.
     */
    public SixtyCycle getMonth() {
        return day.getMonth();
    }

    /**
     * Gets the day pillar.
     *
     * @return The {@link SixtyCycle} instance representing the day.
     */
    public SixtyCycle getDay() {
        return day.getSixtyCycle();
    }

    /**
     * Gets the hour pillar.
     *
     * @return The {@link SixtyCycle} instance representing the hour.
     */
    public SixtyCycle getSixtyCycle() {
        return hour;
    }

    /**
     * Gets the Sixty-Year Cycle Day associated with this hour.
     *
     * @return The {@link SixtyCycleDay} instance.
     */
    public SixtyCycleDay getSixtyCycleDay() {
        return day;
    }

    /**
     * Gets the Gregorian time associated with this Sixty-Year Cycle Hour.
     *
     * @return The {@link SolarTime} instance.
     */
    public SolarTime getSolarTime() {
        return solarTime;
    }

    /**
     * Gets the name of this Sixty-Year Cycle Hour.
     *
     * @return The name of the hour as a formatted string.
     */
    public String getName() {
        return hour + "时";
    }

    /**
     * Returns a string representation of this Sixty-Year Cycle Hour, including the day and hour.
     *
     * @return A string representation of the Sixty-Year Cycle Hour.
     */
    @Override
    public String toString() {
        return day + getName();
    }

    /**
     * Gets the index of this hour within the day (0-11).
     *
     * @return The index of the hour in the day.
     */
    public int getIndexInDay() {
        int h = solarTime.getHour();
        return h == 23 ? 0 : (h + 1) / 2;
    }

    /**
     * Gets the nine-star marker associated with this hour.
     *
     * @return The {@link NineStar} instance.
     */
    public NineStar getNineStar() {
        SolarDay solar = solarTime.getSolarDay();
        SolarTerms dongZhi = SolarTerms.fromIndex(solar.getYear(), 0);
        int earthBranchIndex = getIndexInDay() % 12;
        int index = 8 - 3 * (getDay().getEarthBranch().getIndex() % 3);
        if (!solar.isBefore(dongZhi.getJulianDay().getSolarDay())
                && solar.isBefore(dongZhi.next(12).getJulianDay().getSolarDay())) {
            index = 8 + earthBranchIndex - index;
        } else {
            index -= earthBranchIndex;
        }
        return NineStar.fromIndex(index);
    }

    /**
     * Gets the twelve-star marker associated with this hour.
     *
     * @return The {@link TwelveStar} instance.
     */
    public TwelveStar getTwelveStar() {
        return TwelveStar
                .fromIndex(hour.getEarthBranch().getIndex() + (8 - getDay().getEarthBranch().getIndex() % 6) * 2);
    }

    /**
     * Gets the list of recommended activities for this hour.
     *
     * @return A list of {@link Taboo} objects representing recommended activities.
     */
    public List<Taboo> getRecommends() {
        return Taboo.getHourRecommends(getDay(), hour);
    }

    /**
     * Gets the list of activities to avoid for this hour.
     *
     * @return A list of {@link Taboo} objects representing activities to avoid.
     */
    public List<Taboo> getAvoids() {
        return Taboo.getHourAvoids(getDay(), hour);
    }

    /**
     * Gets the next {@code SixtyCycleHour} by adding a specified number of seconds.
     *
     * @param n The number of seconds to add.
     * @return The next {@code SixtyCycleHour} instance.
     */
    @Override
    public SixtyCycleHour next(int n) {
        return fromSolarTime(solarTime.next(n));
    }

    /**
     * Gets the Eight Characters for this Sixty-Year Cycle hour.
     *
     * @return The {@link EightChar} instance.
     */
    public EightChar getEightChar() {
        return new EightChar(getYear(), getMonth(), getDay(), hour);
    }

}
