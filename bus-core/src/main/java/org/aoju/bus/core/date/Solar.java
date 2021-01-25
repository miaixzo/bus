/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2021 aoju.org and other contributors.                      *
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
package org.aoju.bus.core.date;

import org.aoju.bus.core.lang.Fields;
import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.core.toolkit.BooleanKit;
import org.aoju.bus.core.toolkit.ObjectKit;

import java.util.*;

/**
 * 阳历日期
 *
 * @author Kimi Liu
 * @version 6.1.8
 * @since JDK 1.8+
 */
public class Solar {

    /**
     * 日期对应的节日
     */
    public static final Map<String, String> FESTIVAL = new HashMap<String, String>() {
        private static final long serialVersionUID = 1L;

        {
            put("1-1", "元旦节");
            put("2-14", "情人节");
            put("3-8", "妇女节");
            put("3-12", "植树节");
            put("3-15", "消费者权益日");
            put("4-1", "愚人节");
            put("5-1", "劳动节");
            put("5-4", "青年节");
            put("6-1", "儿童节");
            put("7-1", "建党节");
            put("8-1", "建军节");
            put("9-10", "教师节");
            put("10-1", "国庆节");
            put("12-24", "平安夜");
            put("12-25", "圣诞节");
        }
    };
    /**
     * 几月第几个星期几对应的节日
     */
    public static final Map<String, String> WEEK_FESTIVAL = new HashMap<String, String>() {
        private static final long serialVersionUID = 1L;

        {
            put("5-2-0", "母亲节");
            put("6-3-0", "父亲节");
            put("11-4-4", "感恩节");
        }
    };
    /**
     * 日期对应的非正式节日
     */
    public static final Map<String, List<String>> OTHER_FESTIVAL = new HashMap<String, List<String>>() {
        private static final long serialVersionUID = 1L;

        {
            put("1-8", Collections.nCopies(1, "周恩来逝世纪念日"));
            put("1-10", Arrays.asList("中国人民警察节", "中国公安110宣传日"));
            put("1-21", Collections.nCopies(1, "列宁逝世纪念日"));
            put("1-26", Collections.nCopies(1, "国际海关日"));
            put("2-2", Collections.nCopies(1, "世界湿地日"));
            put("2-4", Collections.nCopies(1, "世界抗癌日"));
            put("2-7", Collections.nCopies(1, "京汉铁路罢工纪念"));
            put("2-10", Collections.nCopies(1, "国际气象节"));
            put("2-19", Collections.nCopies(1, "邓小平逝世纪念日"));
            put("2-21", Collections.nCopies(1, "国际母语日"));
            put("2-24", Collections.nCopies(1, "第三世界青年日"));
            put("3-1", Collections.nCopies(1, "国际海豹日"));
            put("3-3", Collections.nCopies(1, "全国爱耳日"));
            put("3-5", Arrays.asList("周恩来诞辰纪念日", "中国青年志愿者服务日"));
            put("3-6", Collections.nCopies(1, "世界青光眼日"));
            put("3-12", Collections.nCopies(1, "孙中山逝世纪念日"));
            put("3-14", Collections.nCopies(1, "马克思逝世纪念日"));
            put("3-17", Collections.nCopies(1, "国际航海日"));
            put("3-18", Collections.nCopies(1, "全国科技人才活动日"));
            put("3-21", Arrays.asList("世界森林日", "世界睡眠日"));
            put("3-22", Collections.nCopies(1, "世界水日"));
            put("3-23", Collections.nCopies(1, "世界气象日"));
            put("3-24", Collections.nCopies(1, "世界防治结核病日"));
            put("4-2", Collections.nCopies(1, "国际儿童图书日"));
            put("4-7", Collections.nCopies(1, "世界卫生日"));
            put("4-22", Collections.nCopies(1, "列宁诞辰纪念日"));
            put("4-23", Collections.nCopies(1, "世界图书和版权日"));
            put("4-26", Collections.nCopies(1, "世界知识产权日"));
            put("5-3", Collections.nCopies(1, "世界新闻自由日"));
            put("5-5", Collections.nCopies(1, "马克思诞辰纪念日"));
            put("5-8", Collections.nCopies(1, "世界红十字日"));
            put("5-11", Collections.nCopies(1, "世界肥胖日"));
            put("5-23", Collections.nCopies(1, "世界读书日"));
            put("5-27", Collections.nCopies(1, "上海解放日"));
            put("5-31", Collections.nCopies(1, "世界无烟日"));
            put("6-5", Collections.nCopies(1, "世界环境日"));
            put("6-6", Collections.nCopies(1, "全国爱眼日"));
            put("6-8", Collections.nCopies(1, "世界海洋日"));
            put("6-11", Collections.nCopies(1, "中国人口日"));
            put("6-14", Collections.nCopies(1, "世界献血日"));
            put("7-1", Collections.nCopies(1, "香港回归纪念日"));
            put("7-7", Collections.nCopies(1, "中国人民抗日战争纪念日"));
            put("7-11", Collections.nCopies(1, "世界人口日"));
            put("8-5", Collections.nCopies(1, "恩格斯逝世纪念日"));
            put("8-6", Collections.nCopies(1, "国际电影节"));
            put("8-12", Collections.nCopies(1, "国际青年日"));
            put("8-22", Collections.nCopies(1, "邓小平诞辰纪念日"));
            put("9-3", Collections.nCopies(1, "中国抗日战争胜利纪念日"));
            put("9-8", Collections.nCopies(1, "世界扫盲日"));
            put("9-9", Collections.nCopies(1, "毛泽东逝世纪念日"));
            put("9-14", Collections.nCopies(1, "世界清洁地球日"));
            put("9-18", Collections.nCopies(1, "九一八事变纪念日"));
            put("9-20", Collections.nCopies(1, "全国爱牙日"));
            put("9-21", Collections.nCopies(1, "国际和平日"));
            put("9-27", Collections.nCopies(1, "世界旅游日"));
            put("10-4", Collections.nCopies(1, "世界动物日"));
            put("10-10", Collections.nCopies(1, "辛亥革命纪念日"));
            put("10-13", Collections.nCopies(1, "中国少年先锋队诞辰日"));
            put("10-25", Collections.nCopies(1, "抗美援朝纪念日"));
            put("11-12", Collections.nCopies(1, "孙中山诞辰纪念日"));
            put("11-17", Collections.nCopies(1, "国际大学生节"));
            put("11-28", Collections.nCopies(1, "恩格斯诞辰纪念日"));
            put("12-1", Collections.nCopies(1, "世界艾滋病日"));
            put("12-12", Collections.nCopies(1, "西安事变纪念日"));
            put("12-13", Collections.nCopies(1, "南京大屠杀纪念日"));
            put("12-26", Collections.nCopies(1, "毛泽东诞辰纪念日"));
        }
    };
    /**
     * 2000年儒略日数(2000-1-1 12:00:00 UTC)
     */
    public static final double J2000 = 2451545;
    /**
     * 阳历基准年
     */
    public static final int BASE_YEAR = 1901;
    /**
     * 阳历基准月
     */
    public static final int BASE_MONTH = 1;
    /**
     * 阳历基准日
     */
    public static final int BASE_DAY = 1;
    /**
     * 年
     */
    private final int year;
    /**
     * 月
     */
    private final int month;
    /**
     * 日
     */
    private final int day;
    /**
     * 时
     */
    private final int hour;
    /**
     * 分
     */
    private final int minute;
    /**
     * 秒
     */
    private final int second;
    /**
     * 日历
     */
    private final Calendar calendar;

    /**
     * 默认使用当前日期初始化
     */
    public Solar() {
        this(new Date());
    }

    /**
     * 通过日期初始化
     *
     * @param date 日期
     */
    public Solar(Date date) {
        this.calendar = Calendar.getInstance();
        this.calendar.setTime(date);
        this.year = calendar.get(Calendar.YEAR);
        this.month = calendar.get(Calendar.MONTH) + 1;
        this.day = calendar.get(Calendar.DATE);
        this.hour = calendar.get(Calendar.HOUR_OF_DAY);
        this.minute = calendar.get(Calendar.MINUTE);
        this.second = calendar.get(Calendar.SECOND);
    }

    /**
     * 通过日历初始化
     *
     * @param calendar 日历
     */
    public Solar(Calendar calendar) {
        this.calendar = calendar;
        this.year = calendar.get(Calendar.YEAR);
        this.month = calendar.get(Calendar.MONTH) + 1;
        this.day = calendar.get(Calendar.DATE);
        this.hour = calendar.get(Calendar.HOUR_OF_DAY);
        this.minute = calendar.get(Calendar.MINUTE);
        this.second = calendar.get(Calendar.SECOND);
    }

    /**
     * 通过儒略日初始化
     *
     * @param julianDay 儒略日
     */
    public Solar(double julianDay) {
        int d = (int) (julianDay + 0.5);
        double f = julianDay + 0.5 - d;
        int c;

        if (d >= 2299161) {
            c = (int) ((d - 1867216.25) / 36524.25);
            d += 1 + c - (int) (c * 1D / 4);
        }
        d += 1524;
        int year = (int) ((d - 122.1) / 365.25);
        d -= (int) (365.25 * year);
        int month = (int) (d * 1D / 30.601);
        d -= (int) (30.601 * month);
        int day = d;
        if (month > 13) {
            month -= 13;
            year -= 4715;
        } else {
            month -= 1;
            year -= 4716;
        }
        f *= 24;
        int hour = (int) f;

        f -= hour;
        f *= 60;
        int minute = (int) f;

        f -= minute;
        f *= 60;
        int second = (int) Math.round(f);

        this.calendar = Calendar.getInstance();
        this.calendar.set(year, month - 1, day, hour, minute, second);
        this.year = year;
        this.month = month;
        this.day = day;
        this.hour = hour;
        this.minute = minute;
        this.second = second;
    }

    /**
     * 通过年月日初始化
     *
     * @param year   年
     * @param month  月，1到12
     * @param day    日，1到31
     * @param hour   小时，0到23
     * @param minute 分钟，0到59
     * @param second 秒钟，0到59
     */
    public Solar(int year, int month, int day, int hour, int minute, int second) {
        this.calendar = Calendar.getInstance();
        this.calendar.set(year, month - 1, day, hour, minute, second);
        this.year = year;
        this.month = month;
        this.day = day;
        this.hour = hour;
        this.minute = minute;
        this.second = second;
    }

    /**
     * 通过年月日初始化
     *
     * @param year  年
     * @param month 月，1到12
     * @param day   日，1到31
     */
    public Solar(int year, int month, int day) {
        this(year, month, day, 0, 0, 0);
    }

    /**
     * 通过指定日期获取阳历
     *
     * @param date 日期
     * @return 阳历
     */
    public static Solar from(Date date) {
        return new Solar(date);
    }

    /**
     * 通过指定日历获取阳历
     *
     * @param calendar 日历
     * @return 阳历
     */
    public static Solar from(Calendar calendar) {
        return new Solar(calendar);
    }

    /**
     * 通过指定儒略日获取阳历
     *
     * @param julianDay 儒略日
     * @return 阳历
     */
    public static Solar from(double julianDay) {
        return new Solar(julianDay);
    }

    /**
     * 通过指定年月日获取阳历
     *
     * @param year  年
     * @param month 月，1到12
     * @param day   日，1到31
     * @return 阳历
     */
    public static Solar from(int year, int month, int day) {
        return new Solar(year, month, day);
    }

    /**
     * 通过指定年月日时分获取阳历
     *
     * @param year   年
     * @param month  月，1到12
     * @param day    日，1到31
     * @param hour   小时，0到23
     * @param minute 分钟，0到59
     * @param second 秒钟，0到59
     * @return 阳历
     */
    public static Solar from(int year, int month, int day, int hour, int minute, int second) {
        return new Solar(year, month, day, hour, minute, second);
    }

    /**
     * 通过八字获取阳历列表（晚子时日柱按当天）
     *
     * @param yearGanZhi  年柱
     * @param monthGanZhi 月柱
     * @param dayGanZhi   日柱
     * @param timeGanZhi  时柱
     * @return 符合的阳历列表
     */
    public static List<Solar> from(String yearGanZhi, String monthGanZhi, String dayGanZhi, String timeGanZhi) {
        return from(yearGanZhi, monthGanZhi, dayGanZhi, timeGanZhi, 2);
    }

    /**
     * 通过八字获取阳历列表
     *
     * @param yearGanZhi  年柱
     * @param monthGanZhi 月柱
     * @param dayGanZhi   日柱
     * @param timeGanZhi  时柱
     * @param sect        流派，2晚子时日柱按当天，1晚子时日柱按明天
     * @return 符合的阳历列表
     */
    public static List<Solar> from(String yearGanZhi, String monthGanZhi, String dayGanZhi, String timeGanZhi, int sect) {
        sect = (1 == sect) ? 1 : 2;
        Solar today = new Solar();
        Lunar lunar = today.getLunar();
        int offsetYear = Lunar.getJiaZiIndex(lunar.getYearInGanZhiExact()) - Lunar.getJiaZiIndex(yearGanZhi);
        if (offsetYear < 0) {
            offsetYear = offsetYear + 60;
        }
        int startYear = today.getYear() - offsetYear;
        int hour = 0;
        String timeZhi = timeGanZhi.substring(1);
        for (int i = 0, j = Fields.CN_ZHI.length; i < j; i++) {
            if (Fields.CN_ZHI[i].equals(timeZhi)) {
                hour = (i - 1) * 2;
            }
        }
        List<Solar> list = new ArrayList<>();
        while (startYear >= Solar.BASE_YEAR - 1) {
            int year = startYear - 1;
            int counter = 0;
            int month = 12;
            int day;
            boolean found = false;
            while (counter < 15) {
                if (year >= Solar.BASE_YEAR) {
                    day = 1;
                    if (year == Solar.BASE_YEAR && month == Solar.BASE_MONTH) {
                        day = Solar.BASE_DAY;
                    }
                    Solar solar = new Solar(year, month, day, hour, 0, 0);
                    lunar = solar.getLunar();
                    if (lunar.getYearInGanZhiExact().equals(yearGanZhi) && lunar.getMonthInGanZhiExact().equals(monthGanZhi)) {
                        found = true;
                        break;
                    }
                }
                month++;
                if (month > 12) {
                    month = 1;
                    year++;
                }
                counter++;
            }
            if (found) {
                counter = 0;
                month--;
                if (month < 1) {
                    month = 12;
                    year--;
                }
                day = 1;
                if (year == Solar.BASE_YEAR && month == Solar.BASE_MONTH) {
                    day = Solar.BASE_DAY;
                }
                Solar solar = new Solar(year, month, day, hour, 0, 0);
                while (counter < 61) {
                    lunar = solar.getLunar();
                    String dgz = (2 == sect) ? lunar.getDayInGanZhiExact2() : lunar.getDayInGanZhiExact();
                    if (lunar.getYearInGanZhiExact().equals(yearGanZhi) && lunar.getMonthInGanZhiExact().equals(monthGanZhi) && dgz.equals(dayGanZhi) && lunar.getTimeInGanZhi().equals(timeGanZhi)) {
                        list.add(solar);
                        break;
                    }
                    solar = solar.next(1);
                    counter++;
                }
            }
            startYear -= 60;
        }
        return list;
    }

    /**
     * 是否闰年
     *
     * @param year 年
     * @return true/false 闰年/非闰年
     */
    public static boolean isLeapYear(int year) {
        boolean leap = false;
        if (year % 4 == 0) {
            leap = true;
        }
        if (year % 100 == 0) {
            leap = false;
        }
        if (year % 400 == 0) {
            leap = true;
        }
        return leap;
    }

    /**
     * 获取某年某月有多少天
     *
     * @param year  年
     * @param month 月
     * @return 天数
     */
    public static int getDaysOfMonth(int year, int month) {
        int m = month - 1;
        int d = Fields.DAYS_OF_MONTH[m];
        // 公历闰年2月多一天
        if (m == Calendar.FEBRUARY && isLeapYear(year)) {
            d++;
        }
        return d;
    }

    /**
     * 获取某年某月有多少周
     *
     * @param year  年
     * @param month 月
     * @param start 星期几作为一周的开始，默认星期日
     * @return 周数
     */
    public static int getWeeksOfMonth(int year, int month, int start) {
        int days = getDaysOfMonth(year, month);
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, 1);
        int week = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        return (int) Math.ceil((days + week - start) * 1D / 7);
    }

    /**
     * 是否闰年
     *
     * @return true/false 闰年/非闰年
     */
    public boolean isLeapYear() {
        return isLeapYear(this.year);
    }

    /**
     * 获取星期，1代表周日
     *
     * @return 123456
     */
    public int getWeek() {
        return calendar.get(Calendar.DAY_OF_WEEK);
    }

    /**
     * 获取星期的中文
     *
     * @return 星期x
     */
    public String getWeekInChinese() {
        return Fields.Week.getCnNameByCode(getWeek());
    }

    /**
     * 获取节日，有可能一天会有多个节日
     *
     * @return 劳动节等
     */
    public List<String> getFestivals() {
        List<String> list = new ArrayList<>();
        //获取几月几日对应的节日
        String festival = Solar.FESTIVAL.get(month + Symbol.HYPHEN + day);
        if (null != festival) {
            list.add(festival);
        }
        //计算几月第几个星期几对应的节日
        int weeks = (int) Math.ceil(day / 7D);
        //星期几，0代表星期天
        int week = getWeek();
        festival = Solar.WEEK_FESTIVAL.get(month + Symbol.HYPHEN + weeks + Symbol.HYPHEN + week);
        if (null != festival) {
            list.add(festival);
        }
        return list;
    }

    /**
     * 获取非正式的节日，有可能一天会有多个节日
     *
     * @return 非正式的节日列表，如中元节
     */
    public List<String> getOtherFestivals() {
        List<String> list = new ArrayList<>();
        List<String> fs = Solar.OTHER_FESTIVAL.get(month + Symbol.HYPHEN + day);
        if (null != fs) {
            list.addAll(fs);
        }
        return list;
    }

    /**
     * 获取星座
     *
     * @return 星座
     */
    public String getZodiac() {
        int index = 11;
        int y = month * 100 + day;
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
        return Fields.ZODIAC[index];
    }

    /**
     * 获取儒略日
     *
     * @return 儒略日
     */
    public double getJulianDay() {
        int y = this.year;
        int m = this.month;
        double d = this.day + ((this.second * 1D / 60 + this.minute) / 60 + this.hour) / 24;
        int n = 0;
        boolean g = false;
        if (y * 372 + m * 31 + (int) d >= 588829) {
            g = true;
        }
        if (m <= 2) {
            m += 12;
            y--;
        }
        if (g) {
            n = (int) (y * 1D / 100);
            n = 2 - n + (int) (n * 1D / 4);
        }
        return (int) (365.25 * (y + 4716)) + (int) (30.6001 * (m + 1)) + d + n - 1524.5;
    }

    /**
     * 获取往后推几天的阳历日期，如果要往前推，则天数用负数
     *
     * @param days 天数
     * @return {@link Solar}
     */
    public Solar next(int days) {
        return next(days, false);
    }

    /**
     * 获取往后推几天的阳历日期，如果要往前推，则天数用负数
     *
     * @param days        天数
     * @param onlyWorkday 是否仅工作日
     * @return {@link Solar}
     */
    public Solar next(int days, boolean onlyWorkday) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, day, hour, minute, second);
        if (0 != days) {
            if (!onlyWorkday) {
                calendar.add(Calendar.DATE, days);
            } else {
                int rest = Math.abs(days);
                int add = days < 1 ? -1 : 1;
                while (rest > 0) {
                    calendar.add(Calendar.DATE, add);
                    boolean work = true;
                    Holiday holiday = Holiday.getHoliday(
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH) + 1,
                            calendar.get(Calendar.DAY_OF_MONTH)
                    );
                    if (null == holiday) {
                        int week = calendar.get(Calendar.DAY_OF_WEEK);
                        if (1 == week || 7 == week) {
                            work = false;
                        }
                    } else {
                        work = holiday.isWork();
                    }
                    if (work) {
                        rest--;
                    }
                }
            }
        }
        return new Solar(calendar);
    }

    /**
     * 获取农历
     *
     * @return 农历
     */
    public Lunar getLunar() {
        return new Lunar(calendar.getTime());
    }

    /**
     * 获取年份
     *
     * @return 如2020
     */
    public int getYear() {
        return year;
    }

    /**
     * 获取月份
     *
     * @return 1到12
     */
    public int getMonth() {
        return month;
    }

    /**
     * 获取日期
     *
     * @return 1到31之间的数字
     */
    public int getDay() {
        return day;
    }

    /**
     * 获取小时
     *
     * @return 0到23之间的数字
     */
    public int getHour() {
        return hour;
    }

    /**
     * 获取分钟
     *
     * @return 0到59之间的数字
     */
    public int getMinute() {
        return minute;
    }

    /**
     * 获取秒钟
     *
     * @return 0到59之间的数字
     */
    public int getSecond() {
        return second;
    }

    /**
     * 获取日历
     *
     * @return 日历
     */
    public Calendar getCalendar() {
        return calendar;
    }

    /**
     * 构建字符串内容
     *
     * @param args 可选参数-简化输出
     * @return 字符串内容
     */
    public String build(boolean... args) {
        // 年月日
        String strYmd = this.year + Symbol.HYPHEN
                + (this.month < 10 ? "0" : Normal.EMPTY) + this.month + Symbol.HYPHEN
                + (this.day < 10 ? "0" : Normal.EMPTY) + this.day;

        // 年月日时分秒
        String strYmdHms = strYmd + Symbol.SPACE
                + (hour < 10 ? "0" : Normal.EMPTY) + hour + Symbol.C_COLON
                + (minute < 10 ? "0" : "") + minute + Symbol.C_COLON
                + (second < 10 ? "0" : Normal.EMPTY) + second;

        if (ObjectKit.isNotEmpty(args)) {
            if (BooleanKit.and(args)) {
                StringBuilder s = new StringBuilder();
                s.append(strYmdHms);
                if (isLeapYear()) {
                    s.append(Symbol.SPACE);
                    s.append("闰年");
                }
                s.append(Symbol.SPACE);
                s.append(getWeekInChinese());
                for (String f : getFestivals()) {
                    s.append(" (");
                    s.append(f);
                    s.append(")");
                }
                for (String f : getOtherFestivals()) {
                    s.append(" (");
                    s.append(f);
                    s.append(")");
                }
                s.append(Symbol.SPACE);
                s.append(getZodiac());
                return s.toString();
            }
            return strYmd;
        }
        return strYmdHms;
    }

    /**
     * 阳历年
     */
    public static class Year {

        /**
         * 一年的月数
         */
        public static final int MONTH_COUNT = 12;
        /**
         * 年
         */
        private final int year;

        /**
         * 默认当年
         */
        public Year() {
            this(new Date());
        }

        /**
         * 通过日期初始化
         *
         * @param date 日期
         */
        public Year(Date date) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            this.year = calendar.get(Calendar.YEAR);
        }

        /**
         * 通过日历初始化
         *
         * @param calendar 日历
         */
        public Year(Calendar calendar) {
            this.year = calendar.get(Calendar.YEAR);
        }

        /**
         * 通过年初始化
         *
         * @param year 年
         */
        public Year(int year) {
            this.year = year;
        }

        /**
         * 通过指定日期获取阳历年
         *
         * @param date 日期
         * @return 阳历年
         */
        public static Year from(Date date) {
            return new Year(date);
        }

        /**
         * 通过指定日历获取阳历年
         *
         * @param calendar 日历
         * @return 阳历年
         */
        public static Year from(Calendar calendar) {
            return new Year(calendar);
        }

        /**
         * 通过指定年份获取阳历年
         *
         * @param year 年
         * @return 阳历年
         */
        public static Year from(int year) {
            return new Year(year);
        }

        /**
         * 获取年
         *
         * @return 年
         */
        public int getYear() {
            return this.year;
        }

        /**
         * 获取本年的阳历月列表
         *
         * @return 阳历月列表
         */
        public List<Month> getMonths() {
            List<Month> list = new ArrayList<>(MONTH_COUNT);
            Month month = new Month(this.year, 1);
            list.add(month);
            for (int i = 1; i < MONTH_COUNT; i++) {
                list.add(month.next(i));
            }
            return list;
        }

        /**
         * 获取往后推几年的阳历年，如果要往前推，则年数用负数
         *
         * @param years 年数
         * @return 阳历年
         */
        public Year next(int years) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(this.year, Calendar.JANUARY, 1);
            calendar.add(Calendar.YEAR, years);
            return new Year(calendar);
        }

        /**
         * 构建字符串内容
         *
         * @param args 可选参数-简化输出
         * @return 字符串内容
         */
        public String build(boolean... args) {
            if (ObjectKit.isNotEmpty(args) && BooleanKit.and(args)) {
                return this.year + "年";
            }
            return this.year + Normal.EMPTY;
        }

    }

    /**
     * 阳历半年
     */
    public static class Semester {

        /**
         * 半年的月数
         */
        public static final int MONTH_COUNT = 6;
        /**
         * 年
         */
        private final int year;
        /**
         * 月
         */
        private final int month;

        /**
         * 默认当月
         */
        public Semester() {
            this(new Date());
        }

        /**
         * 通过日期初始化
         *
         * @param date 日期
         */
        public Semester(Date date) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            this.year = calendar.get(Calendar.YEAR);
            this.month = calendar.get(Calendar.MONTH) + 1;
        }

        /**
         * 通过日历初始化
         *
         * @param calendar 日历
         */
        public Semester(Calendar calendar) {
            this.year = calendar.get(Calendar.YEAR);
            this.month = calendar.get(Calendar.MONTH) + 1;
        }

        /**
         * 通过年月初始化
         *
         * @param year  年
         * @param month 月
         */
        public Semester(int year, int month) {
            this.year = year;
            this.month = month;
        }

        /**
         * 通过指定日期获取阳历半年
         *
         * @param date 日期
         * @return 阳历半年
         */
        public static Semester from(Date date) {
            return new Semester(date);
        }

        /**
         * 通过指定日历获取阳历半年
         *
         * @param calendar 日历
         * @return 阳历半年
         */
        public static Semester from(Calendar calendar) {
            return new Semester(calendar);
        }

        /**
         * 通过指定年月获取阳历半年
         *
         * @param year  年
         * @param month 月
         * @return 阳历半年
         */
        public static Semester from(int year, int month) {
            return new Semester(year, month);
        }

        /**
         * 获取年
         *
         * @return 年
         */
        public int getYear() {
            return this.year;
        }

        /**
         * 获取月
         *
         * @return 月
         */
        public int getMonth() {
            return this.month;
        }

        /**
         * 获取当月是第几半年
         *
         * @return 半年序号，从1开始
         */
        public int getIndex() {
            return (int) Math.ceil(this.month * 1D / MONTH_COUNT);
        }

        /**
         * 半年推移
         *
         * @param halfYears 推移的半年数，负数为倒推
         * @return 推移后的半年
         */
        public Semester next(int halfYears) {
            if (0 == halfYears) {
                return new Semester(this.year, this.month);
            }
            Calendar calendar = Calendar.getInstance();
            calendar.set(this.year, this.month - 1, 1);
            calendar.add(Calendar.MONTH, MONTH_COUNT * halfYears);
            return new Semester(calendar);
        }

        /**
         * 获取本半年的月份
         *
         * @return 本半年的月份列表
         */
        public List<Month> getMonths() {
            List<Month> list = new ArrayList<>();
            int index = getIndex() - 1;
            for (int i = 0; i < MONTH_COUNT; i++) {
                list.add(new Month(this.year, MONTH_COUNT * index + i + 1));
            }
            return list;
        }

        /**
         * 构建字符串内容
         *
         * @param args 可选参数-简化输出
         * @return 字符串内容
         */
        public String build(boolean... args) {
            if (ObjectKit.isNotEmpty(args) && BooleanKit.and(args)) {
                return this.year + "年" + (getIndex() == 1 ? "上" : "下") + "半年";
            }
            return this.year + Symbol.DOT + getIndex();
        }

    }

    /**
     * 阳历季度
     */
    public static class Quarter {

        /**
         * 一个季度的月数
         */
        public static final int MONTH_COUNT = 3;
        /**
         * 年
         */
        private final int year;
        /**
         * 月
         */
        private final int month;

        /**
         * 默认当月
         */
        public Quarter() {
            this(new Date());
        }

        /**
         * 通过日期初始化
         *
         * @param date 日期
         */
        public Quarter(Date date) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            this.year = calendar.get(Calendar.YEAR);
            this.month = calendar.get(Calendar.MONTH) + 1;
        }

        /**
         * 通过日历初始化
         *
         * @param calendar 日历
         */
        public Quarter(Calendar calendar) {
            this.year = calendar.get(Calendar.YEAR);
            this.month = calendar.get(Calendar.MONTH) + 1;
        }

        /**
         * 通过年月初始化
         *
         * @param year  年
         * @param month 月
         */
        public Quarter(int year, int month) {
            this.year = year;
            this.month = month;
        }

        /**
         * 通过指定日期获取阳历季度
         *
         * @param date 日期
         * @return 阳历季度
         */
        public static Quarter from(Date date) {
            return new Quarter(date);
        }

        /**
         * 通过指定日历获取阳历季度
         *
         * @param calendar 日历
         * @return 阳历季度
         */
        public static Quarter from(Calendar calendar) {
            return new Quarter(calendar);
        }

        /**
         * 通过指定年月获取阳历季度
         *
         * @param year  年
         * @param month 月
         * @return 阳历季度
         */
        public static Quarter from(int year, int month) {
            return new Quarter(year, month);
        }

        /**
         * 获取年
         *
         * @return 年
         */
        public int getYear() {
            return this.year;
        }

        /**
         * 获取月
         *
         * @return 月
         */
        public int getMonth() {
            return this.month;
        }

        /**
         * 获取当月是第几季度
         *
         * @return 季度序号，从1开始
         */
        public int getIndex() {
            return (int) Math.ceil(this.month * 1D / MONTH_COUNT);
        }

        /**
         * 季度推移
         *
         * @param seasons 推移的季度数，负数为倒推
         * @return 推移后的季度
         */
        public Quarter next(int seasons) {
            if (0 == seasons) {
                return new Quarter(this.year, this.month);
            }
            Calendar calendar = Calendar.getInstance();
            calendar.set(this.year, this.month - 1, 1);
            calendar.add(Calendar.MONTH, MONTH_COUNT * seasons);
            return new Quarter(calendar);
        }

        /**
         * 获取本季度的月份
         *
         * @return 本季度的月份
         */
        public List<Month> getMonths() {
            List<Month> list = new ArrayList<>();
            int index = getIndex() - 1;
            for (int i = 0; i < MONTH_COUNT; i++) {
                list.add(new Month(this.year, MONTH_COUNT * index + i + 1));
            }
            return list;
        }

        /**
         * 构建字符串内容
         *
         * @param args 可选参数-简化输出
         * @return 字符串内容
         */
        public String build(boolean... args) {
            if (ObjectKit.isNotEmpty(args) && BooleanKit.and(args)) {
                return this.year + "年" + getIndex() + "季度";
            }
            return this.year + Symbol.DOT + getIndex();
        }

    }

    /**
     * 阳历月
     */
    public static class Month {

        /**
         * 年
         */
        private final int year;
        /**
         * 月
         */
        private final int month;

        /**
         * 默认当月
         */
        public Month() {
            this(new Date());
        }

        /**
         * 通过日期初始化
         *
         * @param date 日期
         */
        public Month(Date date) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            this.year = calendar.get(Calendar.YEAR);
            this.month = calendar.get(Calendar.MONTH) + 1;
        }

        /**
         * 通过日历初始化
         *
         * @param calendar 日历
         */
        public Month(Calendar calendar) {
            this.year = calendar.get(Calendar.YEAR);
            this.month = calendar.get(Calendar.MONTH) + 1;
        }

        /**
         * 通过年月初始化
         *
         * @param year  年
         * @param month 月
         */
        public Month(int year, int month) {
            this.year = year;
            this.month = month;
        }

        /**
         * 通过指定日期获取阳历月
         *
         * @param date 日期
         * @return 阳历月
         */
        public static Month from(Date date) {
            return new Month(date);
        }

        /**
         * 通过指定日历获取阳历月
         *
         * @param calendar 日历
         * @return 阳历月
         */
        public static Month from(Calendar calendar) {
            return new Month(calendar);
        }

        /**
         * 通过指定年月获取阳历月
         *
         * @param year  年
         * @param month 月
         * @return 阳历月
         */
        public static Month from(int year, int month) {
            return new Month(year, month);
        }

        /**
         * 获取年
         *
         * @return 年
         */
        public int getYear() {
            return this.year;
        }

        /**
         * 获取月
         *
         * @return 月
         */
        public int getMonth() {
            return this.month;
        }

        /**
         * 获取本月的阳历日期列表
         *
         * @return 阳历日期列表
         */
        public List<Solar> getDays() {
            List<Solar> list = new ArrayList<>(31);
            Solar solar = new Solar(this.year, this.month, 1);
            list.add(solar);
            int days = getDaysOfMonth(this.year, this.month);
            for (int i = 1; i < days; i++) {
                list.add(solar.next(i));
            }
            return list;
        }

        /**
         * 获取往后推几个月的阳历月，如果要往前推，则月数用负数
         *
         * @param months 月数
         * @return 阳历月
         */
        public Month next(int months) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(this.year, this.month - 1, 1);
            calendar.add(Calendar.MONTH, months);
            return new Month(calendar);
        }

        /**
         * 构建字符串内容
         *
         * @param args 可选参数-简化输出
         * @return 字符串内容
         */
        public String build(boolean... args) {
            if (ObjectKit.isNotEmpty(args) && BooleanKit.and(args)) {
                return this.year + "年" + this.month + "月";
            }
            return this.year + Symbol.HYPHEN + this.month;
        }

    }

    /**
     * 阳历周
     */
    public static class Week {

        /**
         * 年
         */
        private final int year;
        /**
         * 月
         */
        private final int month;
        /**
         * 日
         */
        private final int day;
        /**
         * 星期几作为一周的开始，1234560分别代表星期一至星期天
         */
        private final int start;

        /**
         * 默认当月
         *
         * @param start 星期几作为一周的开始，1234560分别代表星期一至星期天
         */
        public Week(int start) {
            this(new Date(), start);
        }

        /**
         * 通过日期初始化
         *
         * @param date  日期
         * @param start 星期几作为一周的开始，1234560分别代表星期一至星期天
         */
        public Week(Date date, int start) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            this.year = calendar.get(Calendar.YEAR);
            this.month = calendar.get(Calendar.MONTH) + 1;
            this.day = calendar.get(Calendar.DATE);
            this.start = start;
        }

        /**
         * 通过日历初始化
         *
         * @param calendar 日历
         * @param start    星期几作为一周的开始，1234560分别代表星期一至星期天
         */
        public Week(Calendar calendar, int start) {
            this.year = calendar.get(Calendar.YEAR);
            this.month = calendar.get(Calendar.MONTH) + 1;
            this.day = calendar.get(Calendar.DATE);
            this.start = start;
        }

        /**
         * 通过年月初始化
         *
         * @param year  年
         * @param month 月，1到12
         * @param day   日，1到31
         * @param start 星期几作为一周的开始，1234560分别代表星期一至星期天
         */
        public Week(int year, int month, int day, int start) {
            this.year = year;
            this.month = month;
            this.day = day;
            this.start = start;
        }

        /**
         * 通过指定日期获取阳历周
         *
         * @param date  日期
         * @param start 星期几作为一周的开始，1234560分别代表星期一至星期天
         * @return 阳历周
         */
        public static Week from(Date date, int start) {
            return new Week(date, start);
        }

        /**
         * 通过指定日历获取阳历周
         *
         * @param calendar 日历
         * @param start    星期几作为一周的开始，1234560分别代表星期一至星期天
         * @return 阳历周
         */
        public static Week from(Calendar calendar, int start) {
            return new Week(calendar, start);
        }

        /**
         * 通过指定年月日获取阳历周
         *
         * @param year  年
         * @param month 月，1到12
         * @param day   日，1到31
         * @param start 星期几作为一周的开始，1234560分别代表星期一至星期天
         * @return 阳历周
         */
        public static Week from(int year, int month, int day, int start) {
            return new Week(year, month, day, start);
        }

        /**
         * 获取年
         *
         * @return 年
         */
        public int getYear() {
            return this.year;
        }

        /**
         * 获取月
         *
         * @return 1到12
         */
        public int getMonth() {
            return this.month;
        }

        /**
         * 获取日期
         *
         * @return 1到31之间的数字
         */
        public int getDay() {
            return this.day;
        }

        /**
         * 获取星期几作为一周的开始，1234560分别代表星期一至星期天
         *
         * @return 1234560分别代表星期一至星期天
         */
        public int getStart() {
            return this.start;
        }

        /**
         * 获取当前日期是在当月第几周
         *
         * @return 周序号，从1开始
         */
        public int getIndex() {
            Calendar calendar = Calendar.getInstance();
            calendar.set(this.year, this.month - 1, 1);
            int firstDayWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1;
            if (firstDayWeek == 0) {
                firstDayWeek = 7;
            }
            return (int) Math.ceil((this.day + firstDayWeek - this.start) / 7D);
        }

        /**
         * 周推移
         *
         * @param weeks         推移的周数，负数为倒推
         * @param separateMonth 是否按月单独计算
         * @return 推移后的阳历周
         */
        public Week next(int weeks, boolean separateMonth) {
            if (0 == weeks) {
                return new Week(this.year, this.month, this.day, this.start);
            }
            if (separateMonth) {
                int n = weeks;
                Calendar calendar = Calendar.getInstance();
                calendar.set(year, this.month - 1, this.day);
                Week week = new Week(calendar, this.start);
                int month = this.month;
                boolean plus = n > 0;
                while (0 != n) {
                    calendar.add(Calendar.DATE, plus ? 7 : -7);
                    week = new Week(calendar, this.start);
                    int weekMonth = week.getMonth();
                    if (month != weekMonth) {
                        int index = week.getIndex();
                        if (plus) {
                            if (1 == index) {
                                Solar firstDay = week.getFirstDay();
                                week = new Week(firstDay.getYear(), firstDay.getMonth(), firstDay.getDay(), this.start);
                                weekMonth = week.getMonth();
                            } else {
                                calendar.set(week.getYear(), week.getMonth() - 1, 1);
                                week = new Week(calendar, this.start);
                            }
                        } else {
                            int size = getWeeksOfMonth(week.getYear(), week.getMonth(), this.start);
                            if (size == index) {
                                Solar firstDay = week.getFirstDay();
                                Solar lastDay = firstDay.next(6);
                                week = new Week(lastDay.getYear(), lastDay.getMonth(), lastDay.getDay(), this.start);
                                weekMonth = week.getMonth();
                            } else {
                                calendar.set(week.getYear(), week.getMonth() - 1, getDaysOfMonth(week.getYear(), week.getMonth()));
                                week = new Week(calendar, this.start);
                            }
                        }
                        month = weekMonth;
                    }
                    n -= plus ? 1 : -1;
                }
                return week;
            } else {
                Calendar calendar = Calendar.getInstance();
                calendar.set(this.year, this.month - 1, this.day);
                calendar.add(Calendar.DATE, weeks * 7);
                return new Week(calendar, this.start);
            }
        }

        /**
         * 获取本周第一天的阳历日期（可能跨月）
         *
         * @return 本周第一天的阳历日期
         */
        public Solar getFirstDay() {
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, this.month - 1, this.day);
            int week = calendar.get(Calendar.DAY_OF_WEEK) - 1;
            int prev = week - this.start;
            if (prev < 0) {
                prev += 7;
            }
            calendar.add(Calendar.DATE, -prev);
            return new Solar(calendar);
        }

        /**
         * 获取本周第一天的阳历日期（仅限当月）
         *
         * @return 本周第一天的阳历日期
         */
        public Solar getFirstDayInMonth() {
            List<Solar> days = getDays();
            for (Solar day : days) {
                if (this.month == day.getMonth()) {
                    return day;
                }
            }
            return null;
        }

        /**
         * 获取本周的阳历日期列表（可能跨月）
         *
         * @return 本周的阳历日期列表
         */
        public List<Solar> getDays() {
            Solar firstDay = getFirstDay();
            List<Solar> l = new ArrayList<>();
            l.add(firstDay);
            for (int i = 1; i < 7; i++) {
                l.add(firstDay.next(i));
            }
            return l;
        }

        /**
         * 获取本周的阳历日期列表（仅限当月）
         *
         * @return 本周的阳历日期列表（仅限当月）
         */
        public List<Solar> getDaysInMonth() {
            List<Solar> days = this.getDays();
            List<Solar> list = new ArrayList<>();
            for (Solar day : days) {
                if (this.month != day.getMonth()) {
                    continue;
                }
                list.add(day);
            }
            return list;
        }

        /**
         * 构建字符串内容
         *
         * @param args 可选参数-简化输出
         * @return 字符串内容
         */
        public String build(boolean... args) {
            if (ObjectKit.isNotEmpty(args) && BooleanKit.and(args)) {
                return year + "年" + month + "月第" + getIndex() + "周";
            }
            return year + Symbol.DOT + month + Symbol.DOT + getIndex();
        }

    }

}
