/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
