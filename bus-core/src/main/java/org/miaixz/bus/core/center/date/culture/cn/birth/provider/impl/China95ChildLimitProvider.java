/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org 6tail and other contributors.              *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.core.center.date.culture.cn.birth.provider.impl;

import org.miaixz.bus.core.center.date.culture.cn.birth.ChildLimitInfo;
import org.miaixz.bus.core.center.date.culture.cn.birth.provider.ChildLimitProvider;
import org.miaixz.bus.core.center.date.culture.solar.SolarDay;
import org.miaixz.bus.core.center.date.culture.solar.SolarMonth;
import org.miaixz.bus.core.center.date.culture.solar.SolarTerms;
import org.miaixz.bus.core.center.date.culture.solar.SolarTime;

/**
 * 元亨利贞的童限计算
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class China95ChildLimitProvider implements ChildLimitProvider {

    @Override
    public ChildLimitInfo getInfo(SolarTime birthTime, SolarTerms term) {
        // 出生时刻和节令时刻相差的分钟数
        int minutes = Math.abs(term.getJulianDay().getSolarTime().subtract(birthTime)) / 60;
        int year = minutes / 4320;
        minutes %= 4320;
        int month = minutes / 360;
        minutes %= 360;
        int day = minutes / 12;

        SolarDay birthday = birthTime.getDay();
        SolarMonth birthMonth = birthday.getMonth();
        SolarMonth sm = SolarMonth.fromYm(birthMonth.getYear().getYear() + year, birthMonth.getMonth()).next(month);

        int d = birthday.getDay() + day;
        int dc = sm.getDayCount();
        if (d > dc) {
            d -= dc;
            sm = sm.next(1);
        }

        return new ChildLimitInfo(birthTime, SolarTime.fromYmdHms(sm.getYear().getYear(), sm.getMonth(), d, birthTime.getHour(), birthTime.getMinute(), birthTime.getSecond()), year, month, day, 0, 0);
    }

}
