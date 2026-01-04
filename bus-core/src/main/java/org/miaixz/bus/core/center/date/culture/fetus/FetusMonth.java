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
package org.miaixz.bus.core.center.date.culture.fetus;

import org.miaixz.bus.core.center.date.culture.Samsara;
import org.miaixz.bus.core.center.date.culture.lunar.LunarMonth;

/**
 * Represents the monthly Fetus God (逐月胎神), a concept in Chinese traditional culture related to pregnancy and auspicious
 * locations. The names indicate the location where the Fetus God resides each month. (Reference:
 * 正十二月在床房，二三九十门户中，四六十一灶勿犯，五甲七子八厕凶。) This class extends {@link Samsara} to manage a cyclical list of these entities.
 *
 * @author Kimi Liu
 * @since Java 17+
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
