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
package org.miaixz.bus.core.center.date.culture.cn.eightchar;

import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.core.center.date.culture.Tradition;
import org.miaixz.bus.core.center.date.culture.cn.Duty;
import org.miaixz.bus.core.center.date.culture.cn.sixty.EarthBranch;
import org.miaixz.bus.core.center.date.culture.cn.sixty.HeavenStem;
import org.miaixz.bus.core.center.date.culture.cn.sixty.SixtyCycle;
import org.miaixz.bus.core.center.date.culture.solar.SolarDay;
import org.miaixz.bus.core.center.date.culture.solar.SolarTerms;
import org.miaixz.bus.core.center.date.culture.solar.SolarTime;

/**
 * 八字
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class EightChar extends Tradition {

    /**
     * 年柱
     */
    protected SixtyCycle year;

    /**
     * 月柱
     */
    protected SixtyCycle month;

    /**
     * 日柱
     */
    protected SixtyCycle day;

    /**
     * 时柱
     */
    protected SixtyCycle hour;

    /**
     * 初始化
     *
     * @param year  年柱
     * @param month 月柱
     * @param day   日柱
     * @param hour  时柱
     */
    public EightChar(SixtyCycle year, SixtyCycle month, SixtyCycle day, SixtyCycle hour) {
        this.year = year;
        this.month = month;
        this.day = day;
        this.hour = hour;
    }

    /**
     * 初始化
     *
     * @param year  年柱
     * @param month 月柱
     * @param day   日柱
     * @param hour  时柱
     */
    public EightChar(String year, String month, String day, String hour) {
        this(SixtyCycle.fromName(year), SixtyCycle.fromName(month), SixtyCycle.fromName(day),
                SixtyCycle.fromName(hour));
    }

    /**
     * 年柱
     *
     * @return 年柱
     */
    public SixtyCycle getYear() {
        return year;
    }

    /**
     * 月柱
     *
     * @return 月柱
     */
    public SixtyCycle getMonth() {
        return month;
    }

    /**
     * 日柱
     *
     * @return 日柱
     */
    public SixtyCycle getDay() {
        return day;
    }

    /**
     * 时柱
     *
     * @return 时柱
     */
    public SixtyCycle getHour() {
        return hour;
    }

    /**
     * 胎元
     *
     * @return 胎元
     */
    public SixtyCycle getFetalOrigin() {
        return SixtyCycle.fromName(month.getHeavenStem().next(1).getName() + month.getEarthBranch().next(3).getName());
    }

    /**
     * 胎息
     *
     * @return 胎息
     */
    public SixtyCycle getFetalBreath() {
        return SixtyCycle.fromName(day.getHeavenStem().next(5).getName()
                + EarthBranch.fromIndex(13 - day.getEarthBranch().getIndex()).getName());
    }

    /**
     * 命宫
     *
     * @return 命宫
     */
    public SixtyCycle getOwnSign() {
        int offset = month.getEarthBranch().next(-1).getIndex() + hour.getEarthBranch().next(-1).getIndex();
        offset = (offset >= 14 ? 26 : 14) - offset;
        offset -= 1;
        return SixtyCycle.fromName(HeavenStem.fromIndex((year.getHeavenStem().getIndex() + 1) * 2 + offset).getName()
                + EarthBranch.fromIndex(2 + offset).getName());
    }

    /**
     * 身宫
     *
     * @return 身宫
     */
    public SixtyCycle getBodySign() {
        int offset = month.getEarthBranch().getIndex() + hour.getEarthBranch().getIndex();
        offset %= 12;
        offset -= 1;
        return SixtyCycle.fromName(HeavenStem.fromIndex((year.getHeavenStem().getIndex() + 1) * 2 + offset).getName()
                + EarthBranch.fromIndex(2 + offset).getName());
    }

    /**
     * 建除十二值神
     *
     * @return 建除十二值神
     */
    public Duty getDuty() {
        return Duty.fromIndex(day.getEarthBranch().getIndex() - month.getEarthBranch().getIndex());
    }

    /**
     * 公历时刻列表
     *
     * @param startYear 开始年(含)，支持1-9999年
     * @param endYear   结束年(含)，支持1-9999年
     * @return 公历时刻列表
     */
    public List<SolarTime> getSolarTimes(int startYear, int endYear) {
        List<SolarTime> l = new ArrayList<>();
        // 月地支距寅月的偏移值
        int m = month.getEarthBranch().next(-2).getIndex();
        // 月天干要一致
        if (!HeavenStem.fromIndex((year.getHeavenStem().getIndex() + 1) * 2 + m).equals(month.getHeavenStem())) {
            return l;
        }
        // 1年的立春是辛酉，序号57
        int y = year.next(-57).getIndex() + 1;
        // 节令偏移值
        m *= 2;
        // 时辰地支转时刻，子时按零点算
        int h = hour.getEarthBranch().getIndex() * 2;
        int baseYear = startYear - 1;
        while (y <= endYear) {
            if (y >= baseYear) {
                // 立春为寅月的开始
                SolarTerms term = SolarTerms.fromIndex(y, 3);
                // 节令推移，年干支和月干支就都匹配上了
                if (m > 0) {
                    term = term.next(m);
                }
                SolarTime solarTime = term.getJulianDay().getSolarTime();
                if (solarTime.getYear() >= startYear) {
                    int mi = 0;
                    int s = 0;
                    // 日干支和节令干支的偏移值
                    SolarDay solarDay = solarTime.getSolarDay();
                    int d = day.next(-solarDay.getLunarDay().getSixtyCycle().getIndex()).getIndex();
                    if (d > 0) {
                        // 从节令推移天数
                        solarDay = solarDay.next(d);
                    } else if (h == solarTime.getHour()) {
                        // 如果正好是节令当天，且小时和节令的小时数相等的极端情况，把分钟和秒钟带上
                        mi = solarTime.getMinute();
                        s = solarTime.getSecond();
                    }
                    SolarTime time = SolarTime.fromYmdHms(solarDay.getYear(), solarDay.getMonth(), solarDay.getDay(), h,
                            mi, s);
                    // 验证一下
                    if (time.getLunarHour().getEightChar().equals(this)) {
                        l.add(time);
                    }
                }
            }
            y += 60;
        }
        return l;
    }

    public String getName() {
        return String.format("%s %s %s %s", year, month, day, hour);
    }

}
