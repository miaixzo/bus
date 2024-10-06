/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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
 * Lunar的流派1童限计算（按天数和时辰数计算，3天1年，1天4个月，1时辰10天）
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class LunarSect1ChildLimitProvider extends AbstractChildLimitProvider {

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
        // 时辰差
        int hourDiff = endTimeZhiIndex - startTimeZhiIndex;
        // 天数差
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
