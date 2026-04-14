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
package org.miaixz.bus.core.center.date.culture.solar;

import org.miaixz.bus.core.center.date.culture.festival.AbstractFestival;
import org.miaixz.bus.core.center.date.culture.festival.Festival;
import org.miaixz.bus.core.center.date.culture.festival.FestivalRegistry;

/**
 * Represents modern Gregorian festivals.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class SolarFestival extends AbstractFestival {

    /**
     * Names of solar festivals.
     */
    public static final String[] NAMES = { "元旦", "妇女节", "植树节", "劳动节", "青年节", "儿童节", "建党节", "建军节", "教师节", "国庆节" };

    /**
     * Encoded solar festival data.
     *
     * @see FestivalRegistry#DATA
     */
    public static String DATA = "0VV__0Ux0Xc__0Ux0Xg__0_Q0ZV__0Ux0ZY__0Ux0aV__0Ux0bV__0Uo0cV__0Ug0de__0_V0eV__0Ux";

    /**
     * Constructs a solar festival instance.
     *
     * @param index festival index within {@link #NAMES}
     * @param event festival definition
     * @param day   matched solar day
     */
    public SolarFestival(int index, Festival event, SolarDay day) {
        super(index, event, day);
    }

    /**
     * Creates a solar festival by festival index for a specific year.
     *
     * @param year  target year
     * @param index festival index
     * @return matching solar festival, or {@code null} if none exists
     */
    public static SolarFestival fromIndex(int year, int index) {
        if (index < 0 || index >= NAMES.length) {
            return null;
        }
        int start = index * 8;
        Festival e = new Festival(NAMES[index], "@" + DATA.substring(start, start + 8));
        if (year < e.getStartYear()) {
            return null;
        }
        return new SolarFestival(index, e, SolarDay.fromYmd(year, e.getValue(2), e.getValue(3)));
    }

    /**
     * Creates a solar festival by concrete calendar date.
     *
     * @param year  target year
     * @param month target month
     * @param day   target day of month
     * @return matching solar festival, or {@code null} if none exists
     */
    public static SolarFestival fromYmd(int year, int month, int day) {
        SolarDay d = SolarDay.fromYmd(year, month, day);
        for (int i = 0, j = SolarFestival.NAMES.length; i < j; i++) {
            int start = i * 8;
            Festival e = new Festival(SolarFestival.NAMES[i], "@" + SolarFestival.DATA.substring(start, start + 8));
            if (d.getYear() >= e.getStartYear() && d.getMonth() == e.getValue(2) && d.getDay() == e.getValue(3)) {
                return new SolarFestival(i, e, d);
            }
        }
        return null;
    }

    /**
     * Gets the festival that is {@code n} positions away in the ordered solar festival sequence.
     *
     * @param n number of festival steps to move
     * @return target solar festival
     */
    public SolarFestival next(int n) {
        int size = NAMES.length;
        int i = index + n;
        return fromIndex((day.getYear() * size + i) / size, indexOf(i, size));
    }

    /**
     * Gets the start year from which this festival is observed.
     *
     * @return The start year.
     */
    public int getStartYear() {
        return event.getStartYear();
    }

    /**
     * Gets the solar day of the festival.
     *
     * @return The {@link SolarDay} of the festival.
     */
    public SolarDay getDay() {
        return (SolarDay) super.getDay();
    }

}
