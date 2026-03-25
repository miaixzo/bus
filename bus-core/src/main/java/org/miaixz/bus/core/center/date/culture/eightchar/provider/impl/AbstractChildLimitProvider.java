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
import org.miaixz.bus.core.center.date.culture.eightchar.provider.ChildLimitProvider;
import org.miaixz.bus.core.center.date.culture.solar.SolarMonth;
import org.miaixz.bus.core.center.date.culture.solar.SolarTime;

/**
 * Abstract base class for Child Limit (童限) calculation providers. Provides a common method for calculating the end time
 * and duration of the Child Limit.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public abstract class AbstractChildLimitProvider implements ChildLimitProvider {

    /**
     * Constructs a new AbstractChildLimitProvider. Utility class constructor for static access.
     */
    protected AbstractChildLimitProvider() {
    }

    /**
     * Calculates the end time and duration of the Child Limit based on the birth time and added time components.
     *
     * @param birthTime The Gregorian birth time.
     * @param addYear   Years to add.
     * @param addMonth  Months to add.
     * @param addDay    Days to add.
     * @param addHour   Hours to add.
     * @param addMinute Minutes to add.
     * @param addSecond Seconds to add.
     * @return A {@link ChildLimitInfo} object containing the calculated Child Limit details.
     */
    protected ChildLimitInfo next(
            SolarTime birthTime,
            int addYear,
            int addMonth,
            int addDay,
            int addHour,
            int addMinute,
            int addSecond) {
        int d = birthTime.getDay() + addDay;
        int h = birthTime.getHour() + addHour;
        int mi = birthTime.getMinute() + addMinute;
        int s = birthTime.getSecond() + addSecond;
        mi += s / 60;
        s %= 60;
        h += mi / 60;
        mi %= 60;
        d += h / 24;
        h %= 24;

        SolarMonth sm = SolarMonth.fromYm(birthTime.getYear() + addYear, birthTime.getMonth()).next(addMonth);

        int dc = sm.getDayCount();
        while (d > dc) {
            d -= dc;
            sm = sm.next(1);
            dc = sm.getDayCount();
        }

        return new ChildLimitInfo(birthTime, SolarTime.fromYmdHms(sm.getYear(), sm.getMonth(), d, h, mi, s), addYear,
                addMonth, addDay, addHour, addMinute);
    }

}
