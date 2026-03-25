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
package org.miaixz.bus.core.center.date.culture.fetus;

import org.miaixz.bus.core.center.date.culture.Samsara;
import org.miaixz.bus.core.center.date.culture.lunar.LunarMonth;

/**
 * Represents the monthly Fetus God (逐月胎神), a concept in Chinese traditional culture related to pregnancy and auspicious
 * locations. The names indicate the location where the Fetus God resides each month. (Reference:
 * 正十二月在床房，二三九十门户中，四六十一灶勿犯，五甲七子八厕凶。) This class extends {@link Samsara} to manage a cyclical list of these entities.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class FetusMonth extends Samsara {

    /**
     * Array of names for the monthly Fetus God locations.
     */
    public static final String[] NAMES = { "占房床", "占户窗", "占门堂", "占厨灶", "占房床", "占床仓", "占碓磨", "占厕户", "占门房", "占房床", "占灶炉",
            "占房床" };

    /**
     * Constructs a {@code FetusMonth} instance with the specified index.
     *
     * @param index The index of the Fetus God location in the {@link #NAMES} array.
     */
    public FetusMonth(int index) {
        super(NAMES, index);
    }

    /**
     * Creates a {@code FetusMonth} instance from a {@link LunarMonth}.
     *
     * @param lunarMonth The LunarMonth.
     * @return A new {@code FetusMonth} instance, or {@code null} if the month is a leap month.
     */
    public static FetusMonth fromLunarMonth(LunarMonth lunarMonth) {
        return lunarMonth.isLeap() ? null : new FetusMonth(lunarMonth.getMonth() - 1);
    }

    /**
     * Gets the next {@code FetusMonth} in the cycle.
     *
     * @param n The number of steps to move forward or backward in the cycle.
     * @return The next {@code FetusMonth} instance.
     */
    public FetusMonth next(int n) {
        return new FetusMonth(nextIndex(n));
    }

}
