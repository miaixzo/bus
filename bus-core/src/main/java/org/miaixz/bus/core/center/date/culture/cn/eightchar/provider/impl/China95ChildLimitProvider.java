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
package org.miaixz.bus.core.center.date.culture.cn.eightchar.provider.impl;

import org.miaixz.bus.core.center.date.culture.cn.eightchar.ChildLimitInfo;
import org.miaixz.bus.core.center.date.culture.solar.SolarTerms;
import org.miaixz.bus.core.center.date.culture.solar.SolarTime;

/**
 * Implementation of Child Limit calculation based on the "Yuan Heng Li Zhen" (元亨利贞) method.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class China95ChildLimitProvider extends AbstractChildLimitProvider {

    /**
     * Constructs a new China95ChildLimitProvider. Utility class constructor for static access.
     */
    protected China95ChildLimitProvider() {
    }

    /**
     * Calculates and returns the Child Limit information based on the "Yuan Heng Li Zhen" method.
     *
     * @param birthTime The Gregorian birth time.
     * @param term      The solar term (节令) relevant to the calculation.
     * @return The {@link ChildLimitInfo} containing details about the Child Limit.
     */
    @Override
    public ChildLimitInfo getInfo(SolarTime birthTime, SolarTerms term) {
        // Minutes difference between birth time and solar term time
        int minutes = Math.abs(term.getJulianDay().getSolarTime().subtract(birthTime)) / 60;
        int year = minutes / 4320;
        minutes %= 4320;
        int month = minutes / 360;
        minutes %= 360;
        int day = minutes / 12;

        return next(birthTime, year, month, day, 0, 0, 0);
    }

}
