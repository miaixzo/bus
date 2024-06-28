/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org 6tail and other contributors.              ~
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

import org.miaixz.bus.core.center.date.culture.Holiday;
import org.miaixz.bus.core.center.date.culture.Loops;
import org.miaixz.bus.core.center.date.culture.cn.JulianDay;
import org.miaixz.bus.core.center.date.culture.cn.Week;
import org.miaixz.bus.core.center.date.culture.cn.climate.Climate;
import org.miaixz.bus.core.center.date.culture.cn.climate.ClimateDay;
import org.miaixz.bus.core.center.date.culture.cn.dog.Dog;
import org.miaixz.bus.core.center.date.culture.cn.dog.DogDay;
import org.miaixz.bus.core.center.date.culture.cn.nine.Nine;
import org.miaixz.bus.core.center.date.culture.cn.nine.NineDay;
import org.miaixz.bus.core.center.date.culture.cn.plumrain.PlumRain;
import org.miaixz.bus.core.center.date.culture.cn.plumrain.PlumRainDay;
import org.miaixz.bus.core.center.date.culture.en.Constellation;
import org.miaixz.bus.core.center.date.culture.lunar.LunarDay;
import org.miaixz.bus.core.center.date.culture.lunar.LunarMonth;

/**
 * 公历日
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SolarDay extends Loops {

    public static final String[] NAMES = {
            "1日", "2日", "3日", "4日", "5日", "6日", "7日", "8日", "9日", "10日", "11日",
            "12日", "13日", "14日", "15日", "16日", "17日", "18日", "19日", "20日", "21日",
            "22日", "23日", "24日", "25日", "26日", "27日", "28日", "29日", "30日", "31日"
    };

    /**
     * 公历月
     */
    protected SolarMonth month;

    /**
     * 日
     */
    protected int day;

    /**
     * 初始化
     *
     * @param year  年
     * @param month 月
     * @param day   日
     */
    public SolarDay(int year, int month, int day) {
        if (day < 1) {
            throw new IllegalArgumentException(String.format("illegal solar day: %d-%d-%d", year, month, day));
        }
        SolarMonth m = SolarMonth.fromYm(year, month);
        if (1582 == year && 10 == month) {
            if ((day > 4 && day < 15) || day > 31) {
                throw new IllegalArgumentException(String.format("illegal solar day: %d-%d-%d", year, month, day));
            }
        } else if (day > m.getDayCount()) {
            throw new IllegalArgumentException(String.format("illegal solar day: %d-%d-%d", year, month, day));
        }
        this.month = m;
        this.day = day;
    }

    public static SolarDay fromYmd(int year, int month, int day) {
        return new SolarDay(year, month, day);
    }

    /**
     * 月
     *
     * @return 月
     */
    public SolarMonth getMonth() {
        return month;
    }

    /**
     * 日
     *
     * @return 日
     */
    public int getDay() {
        return day;
    }

    /**
     * 星期
     *
     * @return 星期
     */
    public Week getWeek() {
        return getJulianDay().getWeek();
    }

    /**
     * 星座
     *
     * @return 星座
     */
    public Constellation getConstellation() {
        int index = 11;
        int y = month.getMonth() * 100 + day;
        if (y >= 321 && y <= 419) {
            index = 0;
        } else if (y >= 420 && y <= 520) {
            index = 1;
        } else if (y >= 521 && y <= 621) {
            index = 2;
        } else if (y >= 622 && y <= 722) {
            index = 3;
        } else if (y >= 723 && y <= 822) {
            index = 4;
        } else if (y >= 823 && y <= 922) {
            index = 5;
        } else if (y >= 923 && y <= 1023) {
            index = 6;
        } else if (y >= 1024 && y <= 1122) {
            index = 7;
        } else if (y >= 1123 && y <= 1221) {
            index = 8;
        } else if (y >= 1222 || y <= 119) {
            index = 9;
        } else if (y <= 218) {
            index = 10;
        }
        return Constellation.get(index);
    }

    public String getName() {
        return NAMES[day - 1];
    }

    @Override
    public String toString() {
        return month + getName();
    }

    public SolarDay next(int n) {
        return getJulianDay().next(n).getSolarDay();
    }

    /**
     * 是否在指定公历日之前
     *
     * @param target 公历日
     * @return true/false
     */
    public boolean isBefore(SolarDay target) {
        SolarMonth bMonth = target.getMonth();
        int aYear = month.getYear().getYear();
        int bYear = bMonth.getYear().getYear();
        if (aYear != bYear) {
            return aYear < bYear;
        }
        return month.getMonth() != bMonth.getMonth() ? month.getMonth() < bMonth.getMonth() : day < target.getDay();
    }

    /**
     * 是否在指定公历日之后
     *
     * @param target 公历日
     * @return true/false
     */
    public boolean isAfter(SolarDay target) {
        SolarMonth bMonth = target.getMonth();
        int aYear = month.getYear().getYear();
        int bYear = bMonth.getYear().getYear();
        if (aYear != bYear) {
            return aYear > bYear;
        }
        return month.getMonth() != bMonth.getMonth() ? month.getMonth() > bMonth.getMonth() : day > target.getDay();
    }

    /**
     * 节气
     *
     * @return 节气
     */
    public SolarTerms getTerm() {
        return getTermDay().getSolarTerm();
    }

    /**
     * 节气第几天
     *
     * @return 节气第几天
     */
    public SolarTermDay getTermDay() {
        SolarTerms term = SolarTerms.fromIndex(month.getYear().getYear() + 1, 0);
        SolarDay day = term.getJulianDay().getSolarDay();
        while (isBefore(day)) {
            term = term.next(-1);
            day = term.getJulianDay().getSolarDay();
        }
        return new SolarTermDay(term, subtract(day));
    }

    /**
     * 公历周
     *
     * @param start 起始星期，1234560分别代表星期一至星期天
     * @return 公历周
     */
    public SolarWeek getSolarWeek(int start) {
        int y = month.getYear().getYear();
        int m = month.getMonth();
        return SolarWeek.fromYm(y, m, (int) Math.ceil((day + fromYmd(y, m, 1).getWeek().next(-start).getIndex()) / 7D) - 1, start);
    }

    /**
     * 七十二候
     *
     * @return 七十二候
     */
    public ClimateDay getClimateDay() {
        SolarTerms term = getTerm();
        int dayIndex = subtract(term.getJulianDay().getSolarDay());
        int index = dayIndex / 5;
        if (index > 2) {
            index = 2;
        }
        dayIndex -= index * 5;
        return new ClimateDay(Climate.fromIndex(term.getIndex() * 3 + index), dayIndex);
    }

    /**
     * 三伏天
     *
     * @return 三伏天
     */
    public DogDay getDogDay() {
        SolarTerms xiaZhi = SolarTerms.fromIndex(month.getYear().getYear(), 12);
        // 第1个庚日
        SolarDay start = xiaZhi.getJulianDay().getSolarDay();
        int add = 6 - start.getLunarDay().getSixtyCycle().getHeavenStem().getIndex();
        if (add < 0) {
            add += 10;
        }
        // 第3个庚日，即初伏第1天
        add += 20;
        start = start.next(add);
        int days = subtract(start);
        // 初伏以前
        if (days < 0) {
            return null;
        }
        if (days < 10) {
            return new DogDay(Dog.fromIndex(0), days);
        }
        // 第4个庚日，中伏第1天
        start = start.next(10);
        days = subtract(start);
        if (days < 10) {
            return new DogDay(Dog.fromIndex(1), days);
        }
        // 第5个庚日，中伏第11天或末伏第1天
        start = start.next(10);
        days = subtract(start);
        // 立秋
        if (xiaZhi.next(3).getJulianDay().getSolarDay().isAfter(start)) {
            if (days < 10) {
                return new DogDay(Dog.fromIndex(1), days + 10);
            }
            start = start.next(10);
            days = subtract(start);
        }
        return days >= 10 ? null : new DogDay(Dog.fromIndex(2), days);
    }

    /**
     * 数九天
     *
     * @return 数九天
     */
    public NineDay getNineDay() {
        int year = month.getYear().getYear();
        SolarDay start = SolarTerms.fromIndex(year + 1, 0).getJulianDay().getSolarDay();
        if (isBefore(start)) {
            start = SolarTerms.fromIndex(year, 0).getJulianDay().getSolarDay();
        }
        SolarDay end = start.next(81);
        if (isBefore(start) || !isBefore(end)) {
            return null;
        }
        int days = subtract(start);
        return new NineDay(Nine.fromIndex(days / 9), days % 9);
    }

    /**
     * 梅雨天（芒种后的第1个丙日入梅，小暑后的第1个未日出梅）
     *
     * @return 梅雨天
     */
    public PlumRainDay getPlumRainDay() {
        // 芒种
        SolarTerms grainInEar = SolarTerms.fromIndex(month.getYear().getYear(), 11);
        SolarDay start = grainInEar.getJulianDay().getSolarDay();
        int add = 2 - start.getLunarDay().getSixtyCycle().getHeavenStem().getIndex();
        if (add < 0) {
            add += 10;
        }
        // 芒种后的第1个丙日
        start = start.next(add);

        // 小暑
        SolarTerms slightHeat = grainInEar.next(2);
        SolarDay end = slightHeat.getJulianDay().getSolarDay();
        add = 7 - end.getLunarDay().getSixtyCycle().getEarthBranch().getIndex();
        if (add < 0) {
            add += 12;
        }
        // 小暑后的第1个未日
        end = end.next(add);

        if (isBefore(start) || isAfter(end)) {
            return null;
        }
        return equals(end) ? new PlumRainDay(PlumRain.fromIndex(1), 0) : new PlumRainDay(PlumRain.fromIndex(0), subtract(start));
    }

    /**
     * 位于当年的索引
     *
     * @return 索引
     */
    public int getIndexInYear() {
        return subtract(fromYmd(month.getYear().getYear(), 1, 1));
    }

    /**
     * 公历日期相减，获得相差天数
     *
     * @param target 公历
     * @return 天数
     */
    public int subtract(SolarDay target) {
        return (int) (getJulianDay().getDay() - target.getJulianDay().getDay());
    }

    /**
     * 儒略日
     *
     * @return 儒略日
     */
    public JulianDay getJulianDay() {
        return JulianDay.fromYmdHms(month.getYear().getYear(), month.getMonth(), day, 0, 0, 0);
    }

    /**
     * 农历日
     *
     * @return 农历日
     */
    public LunarDay getLunarDay() {
        LunarMonth m = LunarMonth.fromYm(month.getYear().getYear(), month.getMonth()).next(-3);
        int days = subtract(m.getFirstJulianDay().getSolarDay());
        while (days >= m.getDayCount()) {
            m = m.next(1);
            days = subtract(m.getFirstJulianDay().getSolarDay());
        }
        return LunarDay.fromYmd(m.getYear().getYear(), m.getMonthWithLeap(), days + 1);
    }

    /**
     * 法定假日，如果当天不是法定假日，返回null
     *
     * @return 法定假日
     */
    public Holiday getHoliday() {
        return Holiday.fromYmd(month.getYear().getYear(), month.getMonth(), day);
    }

    /**
     * 公历现代节日，如果当天不是公历现代节日，返回null
     *
     * @return 公历现代节日
     */
    public SolarFestival getFestival() {
        return SolarFestival.fromYmd(month.getYear().getYear(), month.getMonth(), day);
    }

}
