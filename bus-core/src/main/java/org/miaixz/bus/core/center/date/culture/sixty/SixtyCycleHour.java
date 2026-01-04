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
package org.miaixz.bus.core.center.date.culture.sixty;

import java.util.List;

import org.miaixz.bus.core.center.date.culture.Loops;
import org.miaixz.bus.core.center.date.culture.Taboo;
import org.miaixz.bus.core.center.date.culture.eightchar.EightChar;
import org.miaixz.bus.core.center.date.culture.star.nine.NineStar;
import org.miaixz.bus.core.center.date.culture.star.twelve.TwelveStar;
import org.miaixz.bus.core.center.date.culture.lunar.LunarDay;
import org.miaixz.bus.core.center.date.culture.lunar.LunarHour;
import org.miaixz.bus.core.center.date.culture.lunar.LunarMonth;
import org.miaixz.bus.core.center.date.culture.lunar.LunarYear;
import org.miaixz.bus.core.center.date.culture.solar.SolarDay;
import org.miaixz.bus.core.center.date.culture.solar.SolarTerms;
import org.miaixz.bus.core.center.date.culture.solar.SolarTime;

/**
 * Represents a Sixty-Year Cycle Hour (干支时辰), a traditional Chinese timekeeping unit. The year changes at the Start of
 * Spring (立春), and the month changes at the beginning of a solar term (节令). The day changes at 23:00 (子时). This class
 * extends {@link Loops} for cyclical operations.
 *
 * @author Kimi Liu
 * @since Java 17+
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
     * The Sixty-Year Cycle for the hour (时柱).
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
     * Gets the Sixty-Year Cycle for the year (年柱).
     *
     * @return The {@link SixtyCycle} instance representing the year.
     */
    public SixtyCycle getYear() {
        return day.getYear();
    }

    /**
     * Gets the Sixty-Year Cycle for the month (月柱).
     *
     * @return The {@link SixtyCycle} instance representing the month.
     */
    public SixtyCycle getMonth() {
        return day.getMonth();
    }

    /**
     * Gets the Sixty-Year Cycle for the day (日柱).
     *
     * @return The {@link SixtyCycle} instance representing the day.
     */
    public SixtyCycle getDay() {
        return day.getSixtyCycle();
    }

    /**
     * Gets the Sixty-Year Cycle for the hour (时柱).
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
        return String.format("%s时", hour);
    }

    /**
     * Returns a string representation of this Sixty-Year Cycle Hour, including the day and hour.
     *
     * @return A string representation of the Sixty-Year Cycle Hour.
     */
    @Override
    public String toString() {
        return String.format("%s%s", day, getName());
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
     * Gets the Nine Star (九星) associated with this hour.
     *
     * @return The {@link NineStar} instance.
     */
    public NineStar getNineStar() {
        SolarDay solar = solarTime.getSolarDay();
        SolarTerms dongZhi = SolarTerms.fromIndex(solar.getYear(), 0);
        SolarTerms xiaZhi = dongZhi.next(12);
        int earthBranchIndex = getIndexInDay() % 12;
        int index = new int[] { 8, 5, 2 }[getDay().getEarthBranch().getIndex() % 3];
        if (!solar.isBefore(dongZhi.getJulianDay().getSolarDay())
                && solar.isBefore(xiaZhi.getJulianDay().getSolarDay())) {
            index = 8 + earthBranchIndex - index;
        } else {
            index -= earthBranchIndex;
        }
        return NineStar.fromIndex(index);
    }

    /**
     * Gets the Twelve Star (黄道黑道十二神) associated with this hour.
     *
     * @return The {@link TwelveStar} instance.
     */
    public TwelveStar getTwelveStar() {
        return TwelveStar
                .fromIndex(hour.getEarthBranch().getIndex() + (8 - getDay().getEarthBranch().getIndex() % 6) * 2);
    }

    /**
     * Gets the list of recommended activities (宜) for this hour.
     *
     * @return A list of {@link Taboo} objects representing recommended activities.
     */
    public List<Taboo> getRecommends() {
        return Taboo.getHourRecommends(getDay(), hour);
    }

    /**
     * Gets the list of activities to avoid (忌) for this hour.
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
     * Gets the Eight Characters (八字) for this Sixty-Year Cycle Hour.
     *
     * @return The {@link EightChar} instance.
     */
    public EightChar getEightChar() {
        return new EightChar(getYear(), getMonth(), getDay(), hour);
    }

}
