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
package org.miaixz.bus.core.center.date.culture.lunar;

import java.util.List;

import org.miaixz.bus.core.center.date.culture.Loops;
import org.miaixz.bus.core.center.date.culture.cn.Taboo;
import org.miaixz.bus.core.center.date.culture.cn.eightchar.EightChar;
import org.miaixz.bus.core.center.date.culture.cn.eightchar.provider.EightCharProvider;
import org.miaixz.bus.core.center.date.culture.cn.eightchar.provider.impl.DefaultEightCharProvider;
import org.miaixz.bus.core.center.date.culture.cn.ren.MinorRen;
import org.miaixz.bus.core.center.date.culture.cn.sixty.EarthBranch;
import org.miaixz.bus.core.center.date.culture.cn.sixty.HeavenStem;
import org.miaixz.bus.core.center.date.culture.cn.sixty.SixtyCycle;
import org.miaixz.bus.core.center.date.culture.cn.sixty.SixtyCycleHour;
import org.miaixz.bus.core.center.date.culture.cn.star.nine.NineStar;
import org.miaixz.bus.core.center.date.culture.cn.star.twelve.TwelveStar;
import org.miaixz.bus.core.center.date.culture.solar.SolarDay;
import org.miaixz.bus.core.center.date.culture.solar.SolarTerms;
import org.miaixz.bus.core.center.date.culture.solar.SolarTime;

/**
 * 农历时辰
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class LunarHour extends Loops {

    /**
     * 八字计算接口
     */
    public static EightCharProvider provider = new DefaultEightCharProvider();
    /**
     * 农历日
     */
    protected LunarDay day;
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
     * 公历时刻（第一次使用时才会初始化）
     */
    protected SolarTime solarTime;

    /**
     * 干支时辰（第一次使用时才会初始化）
     */
    protected SixtyCycleHour sixtyCycleHour;

    /**
     * 初始化
     *
     * @param year   农历年
     * @param month  农历月，闰月为负
     * @param day    农历日
     * @param hour   时
     * @param minute 分
     * @param second 秒
     */
    public LunarHour(int year, int month, int day, int hour, int minute, int second) {
        if (hour < 0 || hour > 23) {
            throw new IllegalArgumentException(String.format("illegal hour: %d", hour));
        }
        if (minute < 0 || minute > 59) {
            throw new IllegalArgumentException(String.format("illegal minute: %d", minute));
        }
        if (second < 0 || second > 59) {
            throw new IllegalArgumentException(String.format("illegal second: %d", second));
        }
        this.day = LunarDay.fromYmd(year, month, day);
        this.hour = hour;
        this.minute = minute;
        this.second = second;
    }

    /**
     * 从农历年月日时分秒初始化
     *
     * @param year   农历年
     * @param month  农历月，闰月为负
     * @param day    农历日
     * @param hour   时
     * @param minute 分
     * @param second 秒
     */
    public static LunarHour fromYmdHms(int year, int month, int day, int hour, int minute, int second) {
        return new LunarHour(year, month, day, hour, minute, second);
    }

    /**
     * 农历日
     *
     * @return 农历日
     */
    public LunarDay getLunarDay() {
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
        return EarthBranch.fromIndex(getIndexInDay()).getName() + "时";
    }

    @Override
    public String toString() {
        return day + getSixtyCycle().getName() + "时";
    }

    /**
     * 位于当天的索引
     *
     * @return 索引
     */
    public int getIndexInDay() {
        return (hour + 1) / 2;
    }

    public LunarHour next(int n) {
        if (n == 0) {
            return fromYmdHms(getYear(), getMonth(), getDay(), hour, minute, second);
        }
        int h = hour + n * 2;
        int diff = h < 0 ? -1 : 1;
        int hour = Math.abs(h);
        int days = hour / 24 * diff;
        hour = (hour % 24) * diff;
        if (hour < 0) {
            hour += 24;
            days--;
        }
        LunarDay d = day.next(days);
        return fromYmdHms(d.getYear(), d.getMonth(), d.getDay(), hour, minute, second);
    }

    /**
     * 是否在指定农历时辰之前
     *
     * @param target 农历时辰
     * @return true/false
     */
    public boolean isBefore(LunarHour target) {
        if (!day.equals(target.getLunarDay())) {
            return day.isBefore(target.getLunarDay());
        }
        if (hour != target.getHour()) {
            return hour < target.getHour();
        }
        return minute != target.getMinute() ? minute < target.getMinute() : second < target.getSecond();
    }

    /**
     * 是否在指定农历时辰之后
     *
     * @param target 农历时辰
     * @return true/false
     */
    public boolean isAfter(LunarHour target) {
        if (!day.equals(target.getLunarDay())) {
            return day.isAfter(target.getLunarDay());
        }
        if (hour != target.getHour()) {
            return hour > target.getHour();
        }
        return minute != target.getMinute() ? minute > target.getMinute() : second > target.getSecond();
    }

    /**
     * 干支
     *
     * @return 干支
     */
    public SixtyCycle getSixtyCycle() {
        int earthBranchIndex = getIndexInDay() % 12;
        SixtyCycle d = day.getSixtyCycle();
        if (hour >= 23) {
            d = d.next(1);
        }
        int heavenStemIndex = d.getHeavenStem().getIndex() % 5 * 2 + earthBranchIndex;
        return SixtyCycle.fromName(
                HeavenStem.fromIndex(heavenStemIndex).getName() + EarthBranch.fromIndex(earthBranchIndex).getName());
    }

    /**
     * 黄道黑道十二神
     *
     * @return 黄道黑道十二神
     */
    public TwelveStar getTwelveStar() {
        return TwelveStar.fromIndex(getSixtyCycle().getEarthBranch().getIndex()
                + (8 - getSixtyCycleHour().getDay().getEarthBranch().getIndex() % 6) * 2);
    }

    /**
     * 九星（时家紫白星歌诀：三元时白最为佳，冬至阳生顺莫差，孟日七宫仲一白，季日四绿发萌芽，每把时辰起甲子，本时星耀照光华，时星移入中宫去，顺飞八方逐细查。夏至阴生逆回首，孟归三碧季加六，仲在九宫时起甲，依然掌中逆轮跨。）
     *
     * @return 九星
     */
    public NineStar getNineStar() {
        SolarDay solar = day.getSolarDay();
        SolarTerms dongZhi = SolarTerms.fromIndex(solar.getYear(), 0);
        SolarTerms xiaZhi = dongZhi.next(12);
        boolean asc = !solar.isBefore(dongZhi.getJulianDay().getSolarDay())
                && solar.isBefore(xiaZhi.getJulianDay().getSolarDay());
        int start = new int[] { 8, 5, 2 }[day.getSixtyCycle().getEarthBranch().getIndex() % 3];
        if (asc) {
            start = 8 - start;
        }
        int earthBranchIndex = getIndexInDay() % 12;
        return NineStar.fromIndex(start + (asc ? earthBranchIndex : -earthBranchIndex));
    }

    /**
     * 公历时刻
     *
     * @return 公历时刻
     */
    public SolarTime getSolarTime() {
        if (null == solarTime) {
            SolarDay d = day.getSolarDay();
            solarTime = SolarTime.fromYmdHms(d.getYear(), d.getMonth(), d.getDay(), hour, minute, second);
        }
        return solarTime;
    }

    /**
     * 八字
     *
     * @return 八字
     */
    public EightChar getEightChar() {
        return provider.getEightChar(this);
    }

    /**
     * 干支时辰
     *
     * @return 干支时辰
     */
    public SixtyCycleHour getSixtyCycleHour() {
        if (null == sixtyCycleHour) {
            sixtyCycleHour = getSolarTime().getSixtyCycleHour();
        }
        return sixtyCycleHour;
    }

    /**
     * 宜
     *
     * @return 宜忌列表
     */
    public List<Taboo> getRecommends() {
        return Taboo.getHourRecommends(getSixtyCycleHour().getDay(), getSixtyCycle());
    }

    /**
     * 忌
     *
     * @return 宜忌列表
     */
    public List<Taboo> getAvoids() {
        return Taboo.getHourAvoids(getSixtyCycleHour().getDay(), getSixtyCycle());
    }

    /**
     * 小六壬
     *
     * @return 小六壬
     */
    public MinorRen getMinorRen() {
        return getLunarDay().getMinorRen().next(getIndexInDay());
    }

}
