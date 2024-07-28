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
package org.miaixz.bus.core.center.date.culture.cn.birth.provider.impl;

import org.miaixz.bus.core.center.date.culture.cn.birth.ChildLimitInfo;
import org.miaixz.bus.core.center.date.culture.cn.birth.provider.ChildLimitProvider;
import org.miaixz.bus.core.center.date.culture.solar.SolarDay;
import org.miaixz.bus.core.center.date.culture.solar.SolarMonth;
import org.miaixz.bus.core.center.date.culture.solar.SolarTerms;
import org.miaixz.bus.core.center.date.culture.solar.SolarTime;

/**
 * 默认的童限计算
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class DefaultChildLimitProvider implements ChildLimitProvider {

    @Override
    public ChildLimitInfo getInfo(SolarTime birthTime, SolarTerms term) {
        // 出生时刻和节令时刻相差的秒数
        int seconds = Math.abs(term.getJulianDay().getSolarTime().subtract(birthTime));
        // 3天 = 1年，3天=60*60*24*3秒=259200秒 = 1年
        int year = seconds / 259200;
        seconds %= 259200;
        // 1天 = 4月，1天=60*60*24秒=86400秒 = 4月，85400秒/4=21600秒 = 1月
        int month = seconds / 21600;
        seconds %= 21600;
        // 1时 = 5天，1时=60*60秒=3600秒 = 5天，3600秒/5=720秒 = 1天
        int day = seconds / 720;
        seconds %= 720;
        // 1分 = 2时，60秒 = 2时，60秒/2=30秒 = 1时
        int hour = seconds / 30;
        seconds %= 30;
        // 1秒 = 2分，1秒/2=0.5秒 = 1分
        int minute = seconds * 2;

        SolarDay birthday = birthTime.getSolarDay();

        int d = birthday.getDay() + day;
        int h = birthTime.getHour() + hour;
        int mi = birthTime.getMinute() + minute;
        h += mi / 60;
        mi %= 60;
        d += h / 24;
        h %= 24;

        SolarMonth sm = SolarMonth.fromYm(birthday.getYear() + year, birthday.getMonth()).next(month);

        int dc = sm.getDayCount();
        if (d > dc) {
            d -= dc;
            sm = sm.next(1);
        }

        return new ChildLimitInfo(birthTime,
                SolarTime.fromYmdHms(sm.getYear(), sm.getMonth(), d, h, mi, birthTime.getSecond()), year, month, day,
                hour, minute);
    }

}
