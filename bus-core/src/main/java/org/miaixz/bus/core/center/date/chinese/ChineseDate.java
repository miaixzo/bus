/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
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
package org.miaixz.bus.core.center.date.chinese;

import org.miaixz.bus.core.center.date.Calendars;
import org.miaixz.bus.core.center.date.Resolver;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.math.ChineseNumberFormatter;
import org.miaixz.bus.core.xyz.DateKit;
import org.miaixz.bus.core.xyz.StringKit;

import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

/**
 * 农历日期工具，最大支持到2099年，支持：
 *
 * <ul>
 *     <li>通过公历日期构造获取对应农历</li>
 *     <li>通过农历日期直接构造</li>
 * </ul>
 * <p>
 * 规范参考：<a href="https://openstd.samr.gov.cn/bzgk/gb/newGbInfo?hcno=E107EA4DE9725EDF819F33C60A44B296">GB/T 33661-2017</a>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ChineseDate {

    //农历年
    private final int year;
    //农历月，润N月这个值就是N+1，其他月按照显示月份赋值
    private final int month;
    // 当前月份是否闰月
    private final boolean isLeapMonth;
    //农历日
    private final int day;

    //公历年
    private final int gyear;
    //公历月，从1开始计数
    private final int gmonthBase1;
    //公历日
    private final int gday;

    /**
     * 通过公历日期构造
     *
     * @param date 公历日期
     */
    public ChineseDate(final Date date) {
        this(Resolver.ofDate(Assert.notNull(date.toInstant())));
    }

    /**
     * 通过公历日期构造
     *
     * @param localDate 公历日期
     */
    public ChineseDate(final LocalDate localDate) {
        // 公历
        gyear = localDate.getYear();
        gmonthBase1 = localDate.getMonthValue();
        gday = localDate.getDayOfMonth();

        // 求出和1900年1月31日相差的天数
        int offset = (int) (localDate.toEpochDay() - LunarInfo.BASE_DAY);

        // 计算农历年份
        // 用offset减去每农历年的天数，计算当天是农历第几天，offset是当年的第几天
        int daysOfYear;
        int iYear;
        for (iYear = LunarInfo.BASE_YEAR; iYear <= LunarInfo.MAX_YEAR; iYear++) {
            daysOfYear = LunarInfo.yearDays(iYear);
            if (offset < daysOfYear) {
                break;
            }
            offset -= daysOfYear;
        }

        year = iYear;
        // 计算农历月份
        final int leapMonth = LunarInfo.leapMonth(iYear); // 闰哪个月,1-12
        // 用当年的天数offset,逐个减去每月（农历）的天数，求出当天是本月的第几天
        int month;
        int daysOfMonth;
        boolean hasLeapMonth = false;
        for (month = 1; month < 13; month++) {
            // 闰月，如润的是五月，则5表示五月，6表示润五月
            if (leapMonth > 0 && month == (leapMonth + 1)) {
                daysOfMonth = LunarInfo.leapDays(year);
                hasLeapMonth = true;
            } else {
                // 普通月，当前面的月份存在闰月时，普通月份要-1，递补闰月的数字
                // 如2月是闰月，此时3月实际是第四个月
                daysOfMonth = LunarInfo.monthDays(year, hasLeapMonth ? month - 1 : month);
            }

            if (offset < daysOfMonth) {
                // offset不足月，结束
                break;
            }
            offset -= daysOfMonth;
        }

        this.isLeapMonth = leapMonth > 0 && (month == (leapMonth + 1));
        if (hasLeapMonth && !this.isLeapMonth) {
            // 当前月份前有闰月，则月份显示要-1，除非当前月份就是润月
            month--;
        }
        this.month = month;
        this.day = offset + 1;
    }

    /**
     * 构造方法传入日期
     * 此方法自动判断闰月，如果chineseMonth为本年的闰月，则按照闰月计算
     *
     * @param chineseYear  农历年
     * @param chineseMonth 农历月，1表示一月（正月）
     * @param chineseDay   农历日，1表示初一
     */
    public ChineseDate(final int chineseYear, final int chineseMonth, final int chineseDay) {
        this(chineseYear, chineseMonth, chineseDay, chineseMonth == LunarInfo.leapMonth(chineseYear));
    }

    /**
     * 构造方法传入日期
     * 通过isLeapMonth参数区分是否闰月，如五月是闰月，当isLeapMonth为{@code true}时，表示润五月，{@code false}表示五月
     *
     * @param chineseYear  农历年
     * @param chineseMonth 农历月，1表示一月（正月），如果isLeapMonth为{@code true}，1表示润一月
     * @param chineseDay   农历日，1表示初一
     * @param isLeapMonth  当前月份是否闰月
     */
    public ChineseDate(final int chineseYear, final int chineseMonth, final int chineseDay, boolean isLeapMonth) {
        if (chineseMonth != LunarInfo.leapMonth(chineseYear)) {
            // 用户传入的月份可能非闰月，此时此参数无效。
            isLeapMonth = false;
        }

        this.day = chineseDay;
        // 当月是闰月的后边的月定义为闰月，如润的是五月，则5表示五月，6表示润五月
        this.isLeapMonth = isLeapMonth;
        // 闰月时，农历月份+1，如6表示润五月
        this.month = isLeapMonth ? chineseMonth + 1 : chineseMonth;
        this.year = chineseYear;

        final org.miaixz.bus.core.center.date.DateTime dateTime = lunar2solar(chineseYear, chineseMonth, chineseDay, isLeapMonth);
        if (null != dateTime) {
            //初始化公历年
            this.gday = dateTime.dayOfMonth();
            //初始化公历月
            this.gmonthBase1 = dateTime.month() + 1;
            //初始化公历日
            this.gyear = dateTime.year();
        } else {
            //初始化公历年
            this.gday = -1;
            //初始化公历月
            this.gmonthBase1 = -1;
            //初始化公历日
            this.gyear = -1;
        }
    }

    /**
     * 获得农历年份
     *
     * @return 返回农历年份
     */
    public int getChineseYear() {
        return this.year;
    }

    /**
     * 获取公历的年
     *
     * @return 公历年
     */
    public int getGregorianYear() {
        return this.gyear;
    }

    /**
     * 获取农历的月，从1开始计数
     * 此方法返回实际的月序号，如一月是闰月，则一月返回1，润一月返回2
     *
     * @return 农历的月
     */
    public int getMonth() {
        return this.month;
    }

    /**
     * 获取公历的月，从1开始计数
     *
     * @return 公历月
     */
    public int getGregorianMonthBase1() {
        return this.gmonthBase1;
    }

    /**
     * 获取公历的月，从0开始计数
     *
     * @return 公历月
     */
    public int getGregorianMonth() {
        return this.gmonthBase1 - 1;
    }

    /**
     * 当前农历月份是否为闰月
     *
     * @return 是否为闰月
     */
    public boolean isLeapMonth() {
        return this.isLeapMonth;
    }


    /**
     * 获得农历月份（中文，例如二月，十二月，或者润一月）
     *
     * @return 返回农历月份
     */
    public String getChineseMonth() {
        return getChineseMonth(false);
    }

    /**
     * 获得农历月称呼（中文，例如二月，腊月，或者润正月）
     *
     * @return 返回农历月份称呼
     */
    public String getChineseMonthName() {
        return getChineseMonth(true);
    }

    /**
     * 获得农历月份（中文，例如二月，十二月，或者润一月）
     *
     * @param isTraditional 是否传统表示，例如一月传统表示为正月
     * @return 返回农历月份
     */
    public String getChineseMonth(final boolean isTraditional) {
        return ChineseMonth.getChineseMonthName(isLeapMonth(),
                isLeapMonth() ? this.month - 1 : this.month, isTraditional);
    }

    /**
     * 获取农历的日，从1开始计数
     *
     * @return 农历的日，从1开始计数
     */
    public int getDay() {
        return this.day;
    }

    /**
     * 获取公历的日
     *
     * @return 公历日
     */
    public int getGregorianDay() {
        return this.gday;
    }

    /**
     * 获得农历日
     *
     * @return 获得农历日
     */
    public String getChineseDay() {
        final String[] chineseTen = {"初", "十", "廿", "卅"};
        final int n = (day % 10 == 0) ? 9 : (day % 10 - 1);
        if (day > 30) {
            return "";
        }
        switch (day) {
            case 10:
                return "初十";
            case 20:
                return "二十";
            case 30:
                return "三十";
            default:
                return chineseTen[day / 10] + ChineseNumberFormatter.of().format(n + 1);
        }
    }

    /**
     * 获取公历的Date
     *
     * @return 公历Date
     */
    public Date getGregorianDate() {
        return DateKit.date(getGregorianCalendar());
    }

    /**
     * 获取公历的Calendar
     *
     * @return 公历Calendar
     */
    public Calendar getGregorianCalendar() {
        final Calendar calendar = Calendars.calendar();
        //noinspection MagicConstant
        calendar.set(this.gyear, getGregorianMonth(), this.gday, 0, 0, 0);
        return calendar;
    }

    /**
     * 获得节日，闰月不计入节日中
     *
     * @return 获得农历节日
     */
    public String getFestivals() {
        return StringKit.join(",", LunarFestival.getFestivals(this.year, this.month, day));
    }

    /**
     * 获得年份生肖
     *
     * @return 获得年份生肖
     */
    public String getChineseZodiac() {
        return org.miaixz.bus.core.center.date.Zodiac.getChineseZodiac(this.year);
    }


    /**
     * 获得年的天干地支
     *
     * @return 获得天干地支
     */
    public String getCyclical() {
        return GanZhi.getGanzhiOfYear(this.year);
    }

    /**
     * 获得节气
     *
     * @return 获得节气
     */
    public String getTerm() {
        return SolarTerms.getTerm(gyear, gmonthBase1, gday);
    }

    /**
     * 转换为标准的日期格式来表示农历日期
     * 例如2020-01-13 如果存在闰月，显示闰月月份，如润二月显示2
     *
     * @return 标准的日期格式
     */
    public String toStringNormal() {
        return String.format("%04d-%02d-%02d", this.year,
                isLeapMonth() ? this.month - 1 : this.month, this.day);
    }

    @Override
    public String toString() {
        return toString(ChineseDateFormat.GXSS);
    }

    /**
     * 获取标准化农历日期
     * 支持格式
     * <ol>
     *   <li>{@link ChineseDateFormat#GSS} 干支纪年 数序纪月 数序纪日</li>
     *   <li>{@link ChineseDateFormat#XSS} 生肖纪年 数序纪月 数序纪日</li>
     *   <li>{@link ChineseDateFormat#GXSS} 干支生肖纪年 数序纪月（传统表示） 数序纪日日</li>
     *   <li>{@link ChineseDateFormat#GSG} 干支纪年 数序纪月 干支纪日</li>
     *   <li>{@link ChineseDateFormat#GGG} 干支纪年 干支纪月 干支纪日</li>
     *   <li>{@link ChineseDateFormat#MIX} 农历年年首所在的公历年份 干支纪年 数序纪月 数序纪日</li>
     * </ol>
     *
     * @param format 选择输出的标准格式
     * @return 获取的标准化农历日期
     */
    public String toString(ChineseDateFormat format) {
        if (null == format) {
            format = ChineseDateFormat.MIX;
        }

        final int year = this.year;
        CharSequence dateTemplate = "农历{}年{}{}";
        String normalizedYear = GanZhi.getGanzhiOfYear(year);
        String normalizedMonth = getChineseMonth();
        String normalizedDay = getChineseDay();
        switch (format) {
            case GXSS:
                dateTemplate = "农历{}" + getChineseZodiac() + "年{}{}";
                normalizedMonth = getChineseMonthName();
                break;
            case XSS:
                normalizedYear = getChineseZodiac();
                break;
            case GSG:
                dateTemplate = "农历{}年{}{}日";
                normalizedDay = GanZhi.getGanzhiOfDay(this.gyear, this.gmonthBase1, this.gday);
                break;
            case GGG:
                dateTemplate = "农历{}年{}月{}日";
                normalizedMonth = GanZhi.getGanzhiOfMonth(this.gyear, this.gmonthBase1, this.gday);
                normalizedDay = GanZhi.getGanzhiOfDay(this.gyear, this.gmonthBase1, this.gday);
                break;
            case MIX:
                //根据选择的格式返回不同标准化日期输出，默认为Mix
                dateTemplate = "公元" + this.year + "年农历{}年{}{}";
            case GSS:
                break;
            default:
                throw new IllegalArgumentException("Unsupported format: " + format);
        }

        return StringKit.format(dateTemplate,
                normalizedYear,
                normalizedMonth,
                normalizedDay);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ChineseDate that = (ChineseDate) o;
        return year == that.year && month == that.month && day == that.day && isLeapMonth == that.isLeapMonth;
    }

    @Override
    public int hashCode() {
        return Objects.hash(year, month, day, isLeapMonth);
    }

    /**
     * 这里同步处理年月日的天干地支信息
     *
     * @param year  公历年
     * @param month 公历月，从1开始
     * @param day   公历日
     * @return 天干地支信息
     */
    private String cyclicalm(final int year, final int month, final int day) {
        return StringKit.format("{}年{}月{}日",
                GanZhi.getGanzhiOfYear(this.year),
                GanZhi.getGanzhiOfMonth(year, month, day),
                GanZhi.getGanzhiOfDay(year, month, day));
    }

    /**
     * 通过农历年月日信息 返回公历信息 提供给构造函数
     *
     * @param chineseYear  农历年
     * @param chineseMonth 农历月
     * @param chineseDay   农历日
     * @param isLeapMonth  传入的月是不是闰月
     * @return 公历信息
     */
    private org.miaixz.bus.core.center.date.DateTime lunar2solar(final int chineseYear, final int chineseMonth, final int chineseDay, final boolean isLeapMonth) {
        //超出了最大极限值
        if ((chineseYear == 2100 && chineseMonth == 12 && chineseDay > 1) ||
                (chineseYear == LunarInfo.BASE_YEAR && chineseMonth == 1 && chineseDay < 31)) {
            return null;
        }
        final int day = LunarInfo.monthDays(chineseYear, chineseMonth);
        int _day = day;
        if (isLeapMonth) {
            _day = LunarInfo.leapDays(chineseYear);
        }
        //参数合法性效验
        if (chineseYear < LunarInfo.BASE_YEAR || chineseYear > 2100 || chineseDay > _day) {
            return null;
        }
        //计算农历的时间差
        int offset = 0;
        for (int i = LunarInfo.BASE_YEAR; i < chineseYear; i++) {
            offset += LunarInfo.yearDays(i);
        }
        int leap;
        boolean isAdd = false;
        for (int i = 1; i < chineseMonth; i++) {
            leap = LunarInfo.leapMonth(chineseYear);
            if (!isAdd) {//处理闰月
                if (leap <= i && leap > 0) {
                    offset += LunarInfo.leapDays(chineseYear);
                    isAdd = true;
                }
            }
            offset += LunarInfo.monthDays(chineseYear, i);
        }
        //转换闰月农历 需补充该年闰月的前一个月的时差
        if (isLeapMonth) {
            offset += day;
        }
        //1900年农历正月一日的公历时间为1900年1月30日0时0分0秒(该时间也是本农历的最开始起始点) -2203804800000
        return DateKit.date(((offset + chineseDay - 31) * 86400000L) - 2203804800000L);
    }


}
