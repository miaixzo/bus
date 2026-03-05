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

import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.core.center.date.culture.Tradition;
import org.miaixz.bus.core.center.date.culture.solar.SolarDay;
import org.miaixz.bus.core.center.date.culture.solar.SolarTerms;

/**
 * Three Pillars (Year, Month, Day).
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ThreePillars extends Tradition {

    /**
     * The Year Pillar.
     */
    protected SixtyCycle year;

    /**
     * The Month Pillar.
     */
    protected SixtyCycle month;

    /**
     * The Day Pillar.
     */
    protected SixtyCycle day;

    /**
     * Initializes the ThreePillars.
     *
     * @param year  The Year Pillar.
     * @param month The Month Pillar.
     * @param day   The Day Pillar.
     */
    public ThreePillars(SixtyCycle year, SixtyCycle month, SixtyCycle day) {
        this.year = year;
        this.month = month;
        this.day = day;
    }

    /**
     * Initializes the ThreePillars.
     *
     * @param year  The Year Pillar (as a string).
     * @param month The Month Pillar (as a string).
     * @param day   The Day Pillar (as a string).
     */
    public ThreePillars(String year, String month, String day) {
        this(SixtyCycle.fromName(year), SixtyCycle.fromName(month), SixtyCycle.fromName(day));
    }

    /**
     * Gets the Year Pillar.
     *
     * @return The Year Pillar.
     */
    public SixtyCycle getYear() {
        return year;
    }

    /**
     * Gets the Month Pillar.
     *
     * @return The Month Pillar.
     */
    public SixtyCycle getMonth() {
        return month;
    }

    /**
     * Gets the Day Pillar.
     *
     * @return The Day Pillar.
     */
    public SixtyCycle getDay() {
        return day;
    }

    /**
     * Gets a list of Gregorian (Solar) days that match these Three Pillars.
     *
     * @param startYear The inclusive start year (supports 1-9999).
     * @param endYear   The inclusive end year (supports 1-9999).
     * @return A list of {@link SolarDay} objects.
     */
    public List<SolarDay> getSolarDays(int startYear, int endYear) {
        List<SolarDay> l = new ArrayList<>();
        // Offset of the month's Earthly Branch from Yin month
        int m = month.getEarthBranch().next(-2).getIndex();
        // The month's Heavenly Stem must match
        if (!HeavenStem.fromIndex((year.getHeavenStem().getIndex() + 1) * 2 + m).equals(month.getHeavenStem())) {
            return l;
        }
        // The Start of Spring in year 1 is Xin-You, index 57
        int y = year.next(-57).getIndex() + 1;
        // Solar term offset
        m *= 2;
        int baseYear = startYear - 1;
        if (baseYear > y) {
            y += 60 * (int) Math.ceil((baseYear - y) / 60D);
        }
        while (y <= endYear) {
            // Start of Spring is the beginning of Yin month
            SolarTerms term = SolarTerms.fromIndex(y, 3);
            // Shift by solar terms, the year and month pillars will match
            if (m > 0) {
                term = term.next(m);
            }
            SolarDay solarDay = term.getSolarDay();
            if (solarDay.getYear() >= startYear) {
                // Offset between the day's SixtyCycle and the solar term's SixtyCycle
                int d = day.next(-solarDay.getLunarDay().getSixtyCycle().getIndex()).getIndex();
                if (d > 0) {
                    // Shift days from the solar term
                    solarDay = solarDay.next(d);
                }
                // Verify
                if (solarDay.getSixtyCycleDay().getThreePillars().equals(this)) {
                    l.add(solarDay);
                }
            }
            y += 60;
        }
        return l;
    }

    public String getName() {
        return String.format("%s %s %s", year, month, day);
    }

}
