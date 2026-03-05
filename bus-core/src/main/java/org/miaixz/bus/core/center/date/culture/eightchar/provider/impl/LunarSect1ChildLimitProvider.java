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
package org.miaixz.bus.core.center.date.culture.eightchar.provider.impl;

import org.miaixz.bus.core.center.date.culture.eightchar.ChildLimitInfo;
import org.miaixz.bus.core.center.date.culture.solar.SolarTerms;
import org.miaixz.bus.core.center.date.culture.solar.SolarTime;

/**
 * Implementation of Child Limit calculation for Lunar Sect 1. This method calculates based on days and hours, where 3
 * days equal 1 year, 1 day equals 4 months, and 1 hour equals 10 days.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class LunarSect1ChildLimitProvider extends AbstractChildLimitProvider {

    /**
     * Calculates and returns the Child Limit information based on Lunar Sect 1 rules.
     *
     * @param birthTime The Gregorian birth time.
     * @param term      The solar term (节令) relevant to the calculation.
     * @return The {@link ChildLimitInfo} containing details about the Child Limit.
     */
    @Override
    public ChildLimitInfo getInfo(SolarTime birthTime, SolarTerms term) {
        SolarTime termTime = term.getJulianDay().getSolarTime();
        SolarTime end = termTime;
        SolarTime start = birthTime;
        if (birthTime.isAfter(termTime)) {
            end = birthTime;
            start = termTime;
        }
        int endTimeZhiIndex = (end.getHour() == 23) ? 11 : end.getLunarHour().getIndexInDay();
        int startTimeZhiIndex = (start.getHour() == 23) ? 11 : start.getLunarHour().getIndexInDay();
        // Hour difference
        int hourDiff = endTimeZhiIndex - startTimeZhiIndex;
        // Day difference
        int dayDiff = end.getSolarDay().subtract(start.getSolarDay());
        if (hourDiff < 0) {
            hourDiff += 12;
            dayDiff--;
        }
        int monthDiff = hourDiff * 10 / 30;
        int month = dayDiff * 4 + monthDiff;
        int day = hourDiff * 10 - monthDiff * 30;
        int year = month / 12;
        month = month - year * 12;

        return next(birthTime, year, month, day, 0, 0, 0);
    }

}
