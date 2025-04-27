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
import org.miaixz.bus.core.center.date.culture.cn.eightchar.provider.ChildLimitProvider;
import org.miaixz.bus.core.center.date.culture.solar.SolarMonth;
import org.miaixz.bus.core.center.date.culture.solar.SolarTime;

/**
 * 童限计算抽象
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class AbstractChildLimitProvider implements ChildLimitProvider {

    protected ChildLimitInfo next(SolarTime birthTime, int addYear, int addMonth, int addDay, int addHour,
            int addMinute, int addSecond) {
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
