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
package org.miaixz.bus.core.center.date.culture.solar;

import org.miaixz.bus.core.center.date.culture.Loops;
import org.miaixz.bus.core.center.date.culture.cn.JulianDay;
import org.miaixz.bus.core.center.date.culture.lunar.LunarDay;
import org.miaixz.bus.core.center.date.culture.lunar.LunarHour;

/**
 * 公历时刻
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SolarTime extends Loops {

    /**
     * 公历日
     */
    protected SolarDay day;

    /**
     * 时
     */
    protected int hour;

    /**
     * 分
     */
    protected int minute;

    /**
     * 秒
     */
    protected int second;

    /**
     * 初始化
     *
     * @param year   年
     * @param month  月
     * @param day    日
     * @param hour   时
     * @param minute 分
     * @param second 秒
     */
    public SolarTime(int year, int month, int day, int hour, int minute, int second) {
        if (hour < 0 || hour > 23) {
            throw new IllegalArgumentException(String.format("illegal hour: %d", hour));
        }
        if (minute < 0 || minute > 59) {
            throw new IllegalArgumentException(String.format("illegal minute: %d", minute));
        }
        if (second < 0 || second > 59) {
            throw new IllegalArgumentException(String.format("illegal second: %d", second));
        }
        this.day = SolarDay.fromYmd(year, month, day);
        this.hour = hour;
        this.minute = minute;
        this.second = second;
    }

    public static SolarTime fromYmdHms(int year, int month, int day, int hour, int minute, int second) {
        return new SolarTime(year, month, day, hour, minute, second);
    }

    /**
     * 公历日
     *
     * @return 公历日
     */
    public SolarDay getSolarDay() {
        return day;
    }

    /**
     * 年
     *
     * @return 年
     */
    public int getYear() {
        return day.getYear();
    }

    /**
     * 月
     *
     * @return 月
     */
    public int getMonth() {
        return day.getMonth();
    }

    /**
     * 日
     *
     * @return 日
     */
    public int getDay() {
        return day.getDay();
    }

    /**
     * 时
     *
     * @return 时
     */
    public int getHour() {
        return hour;
    }

    /**
     * 分
     *
     * @return 分
     */
    public int getMinute() {
        return minute;
    }

    /**
     * 秒
     *
     * @return 秒
     */
    public int getSecond() {
        return second;
    }

    public String getName() {
        return String.format("%02d:%02d:%02d", hour, minute, second);
    }

    @Override
    public String toString() {
        return String.format("%s %s", day, getName());
    }

    /**
     * 是否在指定公历时刻之前
     *
     * @param target 公历时刻
     * @return true/false
     */
    public boolean isBefore(SolarTime target) {
        if (!day.equals(target.getSolarDay())) {
            return day.isBefore(target.getSolarDay());
        }
        if (hour != target.getHour()) {
            return hour < target.getHour();
        }
        return minute != target.getMinute() ? minute < target.getMinute() : second < target.getSecond();
    }

    /**
     * 是否在指定公历时刻之后
     *
     * @param target 公历时刻
     * @return true/false
     */
    public boolean isAfter(SolarTime target) {
        if (!day.equals(target.getSolarDay())) {
            return day.isAfter(target.getSolarDay());
        }
        if (hour != target.getHour()) {
            return hour > target.getHour();
        }
        return minute != target.getMinute() ? minute > target.getMinute() : second > target.getSecond();
    }

    /**
     * 节气
     *
     * @return 节气
     */
    public SolarTerms getTerm() {
        int y = getYear();
        int i = getMonth() * 2;
        if (i == 24) {
            y += 1;
            i = 0;
        }
        SolarTerms term = SolarTerms.fromIndex(y, i);
        while (isBefore(term.getJulianDay().getSolarTime())) {
            term = term.next(-1);
        }
        return term;
    }

    /**
     * 儒略日
     *
     * @return 儒略日
     */
    public JulianDay getJulianDay() {
        return JulianDay.fromYmdHms(getYear(), getMonth(), getDay(), hour, minute, second);
    }

    /**
     * 公历时刻相减，获得相差秒数
     *
     * @param target 公历时刻
     * @return 秒数
     */
    public int subtract(SolarTime target) {
        int days = day.subtract(target.getSolarDay());
        int cs = hour * 3600 + minute * 60 + second;
        int ts = target.getHour() * 3600 + target.getMinute() * 60 + target.getSecond();
        int seconds = cs - ts;
        if (seconds < 0) {
            seconds += 86400;
            days--;
        }
        seconds += days * 86400;
        return seconds;
    }

    /**
     * 推移
     *
     * @param n 推移秒数
     * @return 公历时刻
     */
    public SolarTime next(int n) {
        if (n == 0) {
            return SolarTime.fromYmdHms(getYear(), getMonth(), getDay(), hour, minute, second);
        }
        int ts = second + n;
        int tm = minute + ts / 60;
        ts %= 60;
        if (ts < 0) {
            ts += 60;
            tm -= 1;
        }
        int th = hour + tm / 60;
        tm %= 60;
        if (tm < 0) {
            tm += 60;
            th -= 1;
        }
        int td = th / 24;
        th %= 24;
        if (th < 0) {
            th += 24;
            td -= 1;
        }

        SolarDay d = day.next(td);
        return SolarTime.fromYmdHms(d.getYear(), d.getMonth(), d.getDay(), th, tm, ts);
    }

    /**
     * 时辰
     *
     * @return 农历时辰
     */
    public LunarHour getLunarHour() {
        LunarDay d = day.getLunarDay();
        return LunarHour.fromYmdHms(d.getYear(), d.getMonth(), d.getDay(), hour, minute, second);
    }

}
