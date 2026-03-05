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
 * Default implementation for calculating "Child Limit" (童限) information.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class DefaultChildLimitProvider extends AbstractChildLimitProvider {

    /**
     * Constructs a new DefaultChildLimitProvider. Utility class constructor for static access.
     */
    public DefaultChildLimitProvider() {
    }

    /**
     * Calculates and returns the Child Limit information based on default rules.
     *
     * @param birthTime The Gregorian birth time.
     * @param term      The solar term (节令) relevant to the calculation.
     * @return The {@link ChildLimitInfo} containing details about the Child Limit.
     */
    @Override
    public ChildLimitInfo getInfo(SolarTime birthTime, SolarTerms term) {
        // Seconds difference between birth time and solar term time
        int seconds = Math.abs(term.getJulianDay().getSolarTime().subtract(birthTime));
        // 3 days = 1 year, 3 days = 60*60*24*3 seconds = 259200 seconds = 1 year
        int year = seconds / 259200;
        seconds %= 259200;
        // 1 day = 4 months, 1 day = 60*60*24 seconds = 86400 seconds = 4 months, 86400 seconds / 4 = 21600 seconds = 1
        // month
        int month = seconds / 21600;
        seconds %= 21600;
        // 1 hour = 5 days, 1 hour = 60*60 seconds = 3600 seconds = 5 days, 3600 seconds / 5 = 720 seconds = 1 day
        int day = seconds / 720;
        seconds %= 720;
        // 1 minute = 2 hours, 60 seconds = 2 hours, 60 seconds / 2 = 30 seconds = 1 hour
        int hour = seconds / 30;
        seconds %= 30;
        // 1 second = 2 minutes, 1 second / 2 = 0.5 seconds = 1 minute
        int minute = seconds * 2;

        return next(birthTime, year, month, day, hour, minute, 0);
    }

}
