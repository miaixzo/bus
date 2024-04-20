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
package org.miaixz.bus.core.date;

import org.miaixz.bus.core.lang.Fields;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.toolkit.BooleanKit;
import org.miaixz.bus.core.toolkit.ObjectKit;

import java.util.*;

/**
 * 阳历日期
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Solar {

    /**
     * 日期对应的节日
     */
    public static final Map<String, String> FESTIVAL = new HashMap<>() {
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
            put("10-31", "万圣节前夜");
            put("11-1", "万圣节");
            put("12-24", "平安夜");
            put("12-25", "圣诞节");
        }
    };
    /**
     * 几月第几个星期几对应的节日
     */
    public static final Map<String, String> WEEK_FESTIVAL = new HashMap<>() {
        private static final long serialVersionUID = 1L;

        {
            put("3-0-1", "全国中小学生安全教育日");
            put("5-2-0", "母亲节");
            put("6-3-0", "父亲节");
            put("11-4-4", "感恩节");
        }
    };
    /**
     * 日期对应的非正式节日
     */
    public static final Map<String, List<String>> OTHER_FESTIVAL = new HashMap<>() {
        private static final long serialVersionUID = 1L;

        {
            put("1-8", Collections.nCopies(1, "周恩来逝世纪念日"));
            put("1-10", Collections.nCopies(1, "中国人民警察节"));
            put("1-14", Collections.nCopies(1, "日记情人节"));
            put("1-21", Collections.nCopies(1, "列宁逝世纪念日"));
            put("1-26", Collections.nCopies(1, "国际海关日"));
            put("1-27", Collections.nCopies(1, "国际大屠杀纪念日"));
            put("2-2", Collections.nCopies(1, "世界湿地日"));
            put("2-4", Collections.nCopies(1, "世界抗癌日"));
            put("2-7", Collections.nCopies(1, "京汉铁路罢工纪念日"));
            put("2-10", Collections.nCopies(1, "国际气象节"));
            put("2-19", Collections.nCopies(1, "邓小平逝世纪念日"));
            put("2-20", Collections.nCopies(1, "世界社会公正日"));
            put("2-21", Collections.nCopies(1, "国际母语日"));
            put("2-24", Collections.nCopies(1, "第三世界青年日"));
            put("3-1", Collections.nCopies(1, "国际海豹日"));
            put("3-3", Arrays.asList("世界野生动植物日", "全国爱耳日"));
            put("3-5", Arrays.asList("周恩来诞辰纪念日", "中国青年志愿者服务日"));
            put("3-6", Collections.nCopies(1, "世界青光眼日"));
            put("3-7", Collections.nCopies(1, "女生节"));
            put("3-12", Collections.nCopies(1, "孙中山逝世纪念日"));
            put("3-14", Arrays.asList("马克思逝世纪念日", "白色情人节"));
            put("3-17", Collections.nCopies(1, "国际航海日"));
            put("3-18", Arrays.asList("全国科技人才活动日", "全国爱肝日"));
            put("3-20", Collections.nCopies(1, "国际幸福日"));
            put("3-21", Arrays.asList("世界森林日", "世界睡眠日", "国际消除种族歧视日"));
            put("3-22", Collections.nCopies(1, "世界水日"));
            put("3-23", Collections.nCopies(1, "世界气象日"));
            put("3-24", Collections.nCopies(1, "世界防治结核病日"));
            put("3-29", Collections.nCopies(1, "中国黄花岗七十二烈士殉难纪念日"));
            put("4-2", Arrays.asList("国际儿童图书日", "世界自闭症日"));
            put("4-4", Collections.nCopies(1, "国际地雷行动日"));
            put("4-7", Collections.nCopies(1, "世界卫生日"));
            put("4-8", Collections.nCopies(1, "国际珍稀动物保护日"));
            put("4-12", Collections.nCopies(1, "世界航天日"));
            put("4-14", Collections.nCopies(1, "黑色情人节"));
            put("4-15", Collections.nCopies(1, "全民国家安全教育日"));
            put("4-22", Arrays.asList("世界地球日", "列宁诞辰纪念日"));
            put("4-23", Collections.nCopies(1, "世界读书日"));
            put("4-24", Collections.nCopies(1, "中国航天日"));
            put("4-25", Collections.nCopies(1, "儿童预防接种宣传日"));
            put("4-26", Arrays.asList("世界知识产权日", "全国疟疾日"));
            put("4-28", Collections.nCopies(1, "世界安全生产与健康日"));
            put("4-30", Collections.nCopies(1, "全国交通安全反思日"));
            put("5-2", Collections.nCopies(1, "世界金枪鱼日"));
            put("5-3", Collections.nCopies(1, "世界新闻自由日"));
            put("5-5", Collections.nCopies(1, "马克思诞辰纪念日"));
            put("5-8", Collections.nCopies(1, "世界红十字日"));
            put("5-11", Collections.nCopies(1, "世界肥胖日"));
            put("5-12", Arrays.asList("全国防灾减灾日", "护士节"));
            put("5-14", Collections.nCopies(1, "玫瑰情人节"));
            put("5-15", Collections.nCopies(1, "国际家庭日"));
            put("5-19", Collections.nCopies(1, "中国旅游日"));
            put("5-20", Collections.nCopies(1, "网络情人节"));
            put("5-22", Collections.nCopies(1, "国际生物多样性日"));
            put("5-25", Collections.nCopies(1, "525心理健康节"));
            put("5-27", Collections.nCopies(1, "上海解放日"));
            put("5-29", Collections.nCopies(1, "国际维和人员日"));
            put("5-30", Collections.nCopies(1, "中国五卅运动纪念日"));
            put("5-31", Collections.nCopies(1, "世界无烟日"));
            put("6-3", Collections.nCopies(1, "世界自行车日"));
            put("6-5", Collections.nCopies(1, "世界环境日"));
            put("6-6", Collections.nCopies(1, "全国爱眼日"));
            put("6-8", Collections.nCopies(1, "世界海洋日"));
            put("6-11", Collections.nCopies(1, "中国人口日"));
            put("6-14", Arrays.asList("世界献血日", "亲亲情人节"));
            put("6-17", Collections.nCopies(1, "世界防治荒漠化与干旱日"));
            put("6-20", Collections.nCopies(1, "世界难民日"));
            put("6-21", Collections.nCopies(1, "国际瑜伽日"));
            put("6-25", Collections.nCopies(1, "全国土地日"));
            put("6-26", Arrays.asList("国际禁毒日", "联合国宪章日"));
            put("7-1", Collections.nCopies(1, "香港回归纪念日"));
            put("7-6", Arrays.asList("国际接吻日", "朱德逝世纪念日"));
            put("7-7", Collections.nCopies(1, "七七事变纪念日"));
            put("7-11", Arrays.asList("世界人口日", "中国航海日"));
            put("7-14", Collections.nCopies(1, "银色情人节"));
            put("7-18", Collections.nCopies(1, "曼德拉国际日"));
            put("7-30", Collections.nCopies(1, "国际友谊日"));
            put("8-3", Collections.nCopies(1, "男人节"));
            put("8-5", Collections.nCopies(1, "恩格斯逝世纪念日"));
            put("8-6", Collections.nCopies(1, "国际电影节"));
            put("8-8", Collections.nCopies(1, "全民健身日"));
            put("8-9", Collections.nCopies(1, "国际土著人日"));
            put("8-12", Collections.nCopies(1, "国际青年节"));
            put("8-14", Collections.nCopies(1, "绿色情人节"));
            put("8-19", Arrays.asList("世界人道主义日", "中国医师节"));
            put("8-22", Collections.nCopies(1, "邓小平诞辰纪念日"));
            put("8-29", Collections.nCopies(1, "全国测绘法宣传日"));
            put("9-3", Collections.nCopies(1, "中国抗日战争胜利纪念日"));
            put("9-5", Collections.nCopies(1, "中华慈善日"));
            put("9-8", Collections.nCopies(1, "世界扫盲日"));
            put("9-9", Arrays.asList("毛泽东逝世纪念日", "全国拒绝酒驾日"));
            put("9-14", Arrays.asList("世界清洁地球日", "相片情人节"));
            put("9-15", Collections.nCopies(1, "国际民主日"));
            put("9-16", Collections.nCopies(1, "国际臭氧层保护日"));
            put("9-17", Collections.nCopies(1, "世界骑行日"));
            put("9-18", Collections.nCopies(1, "九一八事变纪念日"));
            put("9-20", Collections.nCopies(1, "全国爱牙日"));
            put("9-21", Collections.nCopies(1, "国际和平日"));
            put("9-27", Collections.nCopies(1, "世界旅游日"));
            put("9-30", Collections.nCopies(1, "中国烈士纪念日"));
            put("10-1", Collections.nCopies(1, "国际老年人日"));
            put("10-2", Collections.nCopies(1, "国际非暴力日"));
            put("10-4", Collections.nCopies(1, "世界动物日"));
            put("10-11", Collections.nCopies(1, "国际女童日"));
            put("10-10", Collections.nCopies(1, "辛亥革命纪念日"));
            put("10-13", Arrays.asList("国际减轻自然灾害日", "中国少年先锋队诞辰日"));
            put("10-14", Collections.nCopies(1, "葡萄酒情人节"));
            put("10-16", Collections.nCopies(1, "世界粮食日"));
            put("10-17", Collections.nCopies(1, "全国扶贫日"));
            put("10-20", Collections.nCopies(1, "世界统计日"));
            put("10-24", Arrays.asList("世界发展信息日", "程序员节"));
            put("10-25", Collections.nCopies(1, "抗美援朝纪念日"));
            put("11-5", Collections.nCopies(1, "世界海啸日"));
            put("11-8", Collections.nCopies(1, "记者节"));
            put("11-9", Collections.nCopies(1, "全国消防日"));
            put("11-11", Collections.nCopies(1, "光棍节"));
            put("11-12", Collections.nCopies(1, "孙中山诞辰纪念日"));
            put("11-14", Collections.nCopies(1, "电影情人节"));
            put("11-16", Collections.nCopies(1, "国际宽容日"));
            put("11-17", Collections.nCopies(1, "国际大学生节"));
            put("11-19", Collections.nCopies(1, "世界厕所日"));
            put("11-28", Collections.nCopies(1, "恩格斯诞辰纪念日"));
            put("11-29", Collections.nCopies(1, "国际声援巴勒斯坦人民日"));
            put("12-1", Collections.nCopies(1, "世界艾滋病日"));
            put("12-2", Collections.nCopies(1, "全国交通安全日"));
            put("12-3", Collections.nCopies(1, "世界残疾人日"));
            put("12-4", Collections.nCopies(1, "全国法制宣传日"));
            put("12-5", Arrays.asList("世界弱能人士日", "国际志愿人员日"));
            put("12-7", Collections.nCopies(1, "国际民航日"));
            put("12-9", Arrays.asList("世界足球日", "国际反腐败日"));
            put("12-10", Collections.nCopies(1, "世界人权日"));
            put("12-11", Collections.nCopies(1, "国际山岳日"));
            put("12-12", Collections.nCopies(1, "西安事变纪念日"));
            put("12-13", Collections.nCopies(1, "国家公祭日"));
            put("12-14", Collections.nCopies(1, "拥抱情人节"));
            put("12-18", Collections.nCopies(1, "国际移徙者日"));
            put("12-26", Collections.nCopies(1, "毛泽东诞辰纪念日"));
        }
    };
    /**
     * 2000年儒略日数(2000-1-1 12:00:00 UTC)
     */
    public static final double J2000 = 2451545;
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
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT+8"));
        c.setTime(date);
        c.set(Calendar.MILLISECOND, 0);
        year = c.get(Calendar.YEAR);
        month = c.get(Calendar.MONTH) + 1;
        day = c.get(Calendar.DATE);
        hour = c.get(Calendar.HOUR_OF_DAY);
        minute = c.get(Calendar.MINUTE);
        second = c.get(Calendar.SECOND);
    }

    /**
     * 通过日历初始化
     *
     * @param calendar 日历
     */
    public Solar(Calendar calendar) {
        calendar.set(Calendar.MILLISECOND, 0);
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH) + 1;
        day = calendar.get(Calendar.DATE);
        hour = calendar.get(Calendar.HOUR_OF_DAY);
        minute = calendar.get(Calendar.MINUTE);
        second = calendar.get(Calendar.SECOND);
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
        if (second > 59) {
            second -= 60;
            minute++;
        }
        if (minute > 59) {
            minute -= 60;
            hour++;
        }
        if (hour > 23) {
            hour -= 24;
            day += 1;
        }

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
        if (1582 == year && 10 == month) {
            if (day > 4 && day < 15) {
                throw new IllegalArgumentException(String.format("wrong solar year %d month %d day %d", year, month, day));
            }
        }
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException(String.format("wrong month %d", month));
        }
        if (day < 1 || day > 31) {
            throw new IllegalArgumentException(String.format("wrong day %d", day));
        }
        if (hour < 0 || hour > 23) {
            throw new IllegalArgumentException(String.format("wrong hour %d", hour));
        }
        if (minute < 0 || minute > 59) {
            throw new IllegalArgumentException(String.format("wrong minute %d", minute));
        }
        if (second < 0 || second > 59) {
            throw new IllegalArgumentException(String.format("wrong second %d", second));
        }
        this.year = year;
        this.month = month;
        this.day = day;
        this.hour = hour;
        this.minute = minute;
        this.second = second;
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
     * 通过八字获取阳历列表（晚子时日柱按当天，起始年为1900）
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
     * 通过八字获取阳历列表（起始年为1900）
     *
     * @param yearGanZhi  年柱
     * @param monthGanZhi 月柱
     * @param dayGanZhi   日柱
     * @param timeGanZhi  时柱
     * @param sect        流派，2晚子时日柱按当天，1晚子时日柱按明天
     * @return 符合的阳历列表
     */
    public static List<Solar> from(String yearGanZhi, String monthGanZhi, String dayGanZhi, String timeGanZhi, int sect) {
        return fromBaZi(yearGanZhi, monthGanZhi, dayGanZhi, timeGanZhi, sect, 1900);
    }

    /**
     * 通过八字获取阳历列表（起始年为1900）
     *
     * @param yearGanZhi  年柱
     * @param monthGanZhi 月柱
     * @param dayGanZhi   日柱
     * @param timeGanZhi  时柱
     * @param sect        流派，2晚子时日柱按当天，1晚子时日柱按明天
     * @param baseYear    起始年
     * @return 符合的阳历列表
     */
    public static List<Solar> fromBaZi(String yearGanZhi, String monthGanZhi, String dayGanZhi, String timeGanZhi, int sect, int baseYear) {
        sect = (1 == sect) ? 1 : 2;
        List<Solar> list = new ArrayList<>();
        List<Integer> years = new ArrayList<>();
        Solar today = from(new Date());
        int offsetYear = Lunar.getJiaZiIndex(today.getLunar().getYearInGanZhiExact()) - Lunar.getJiaZiIndex(yearGanZhi);
        if (offsetYear < 0) {
            offsetYear += 60;
        }
        int startYear = today.getYear() - offsetYear - 1;
        int minYear = baseYear - 2;
        while (startYear >= minYear) {
            years.add(startYear);
            startYear -= 60;
        }
        List<Integer> hours = new ArrayList<>(2);
        String timeZhi = timeGanZhi.substring(1);
        for (int i = 1, j = Fields.CN_ZHI.length; i < j; i++) {
            if (Fields.CN_ZHI[i].equals(timeZhi)) {
                hours.add((i - 1) * 2);
                break;
            }
        }
        if ("子".equals(timeZhi)) {
            hours.add(23);
        }
        for (int hour : hours) {
            for (Integer y : years) {
                int maxYear = y + 3;
                int year = y;
                int month = 11;
                if (year < baseYear) {
                    year = baseYear;
                    month = 1;
                }
                Solar solar = from(year, month, 1, hour, 0, 0);
                while (solar.getYear() <= maxYear) {
                    Lunar lunar = solar.getLunar();
                    String dgz = (2 == sect) ? lunar.getDayInGanZhiExact2() : lunar.getDayInGanZhiExact();
                    if (lunar.getYearInGanZhiExact().equals(yearGanZhi) && lunar.getMonthInGanZhiExact().equals(monthGanZhi) && dgz.equals(dayGanZhi) && lunar.getTimeInGanZhi().equals(timeGanZhi)) {
                        list.add(solar);
                        break;
                    }
                    solar = solar.next(1);
                }
            }
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
        if (year < 1600) {
            return year % 4 == 0;
        }
        return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0);
    }

    /**
     * 获取某年有多少天（平年365天，闰年366天）
     *
     * @param year 年
     * @return 天数
     */
    public static int getDaysOfYear(int year) {
        if (1582 == year) {
            return 355;
        }
        return isLeapYear(year) ? 366 : 365;
    }

    /**
     * 获取某年某月有多少天
     *
     * @param year  年
     * @param month 月
     * @return 天数
     */
    public static int getDaysOfMonth(int year, int month) {
        if (1582 == year && 10 == month) {
            return 21;
        }
        int m = month - 1;
        int d = Fields.DAYS_OF_MONTH[m];
        // 公历闰年2月多一天
        if (m == Calendar.FEBRUARY && isLeapYear(year)) {
            d++;
        }
        return d;
    }

    /**
     * 获取某天为当年的第几天
     *
     * @param year  年
     * @param month 月
     * @param day   日
     * @return 第几天
     */
    public static int getDaysInYear(int year, int month, int day) {
        int days = 0;
        for (int i = 1; i < month; i++) {
            days += getDaysOfMonth(year, i);
        }
        int d = day;
        if (1582 == year && 10 == month) {
            if (day >= 15) {
                d -= 10;
            } else if (day > 4) {
                throw new IllegalArgumentException(String.format("wrong solar year %d month %d day %d", year, month, day));
            }
        }
        days += d;
        return days;
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
        return (int) Math.ceil((getDaysOfMonth(year, month) + Solar.from(year, month, 1).getWeek() - start) * 1D / Fields.CN_WEEK.length);
    }

    /**
     * 获取两个日期之间相差的天数（如果日期a比日期b小，天数为正，如果日期a比日期b大，天数为负）
     *
     * @param ay 年a
     * @param am 月a
     * @param ad 日a
     * @param by 年b
     * @param bm 月b
     * @param bd 日b
     * @return 天数
     */
    public static int getDays(int ay, int am, int ad, int by, int bm, int bd) {
        int n;
        int days;
        int i;
        if (ay == by) {
            n = getDaysInYear(by, bm, bd) - getDaysInYear(ay, am, ad);
        } else if (ay > by) {
            days = getDaysOfYear(by) - getDaysInYear(by, bm, bd);
            for (i = by + 1; i < ay; i++) {
                days += getDaysOfYear(i);
            }
            days += getDaysInYear(ay, am, ad);
            n = -days;
        } else {
            days = getDaysOfYear(ay) - getDaysInYear(ay, am, ad);
            for (i = ay + 1; i < by; i++) {
                days += getDaysOfYear(i);
            }
            days += getDaysInYear(by, bm, bd);
            n = days;
        }
        return n;
    }

    /**
     * 获取两个日期之间相差的天数（如果日期a比日期b小，天数为正，如果日期a比日期b大，天数为负）
     *
     * @param calendar0 日期a
     * @param calendar1 日期b
     * @return 天数
     */
    public static int getDays(Calendar calendar0, Calendar calendar1) {
        return getDays(calendar0.get(Calendar.YEAR)
                , calendar0.get(Calendar.MONTH) + 1
                , calendar0.get(Calendar.DATE)
                , calendar1.get(Calendar.YEAR)
                , calendar1.get(Calendar.MONTH) + 1
                , calendar1.get(Calendar.DATE));
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
     * 获取星期，0代表周日
     *
     * @return 1234567
     */
    public int getWeek() {
        return ((int) (getJulianDay() + 0.5) + 7000001) % 7;
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
        String festival = Solar.FESTIVAL.get(month + Symbol.MINUS + day);
        if (null != festival) {
            list.add(festival);
        }
        //计算几月第几个星期几对应的节日
        int weeks = (int) Math.ceil(day / 7D);
        //星期几，1代表星期天
        int week = getWeek();
        festival = Solar.WEEK_FESTIVAL.get(month + Symbol.MINUS + weeks + Symbol.MINUS + week);
        if (null != festival) {
            list.add(festival);
        }
        if (day + 7 > Solar.getDaysOfMonth(year, month)) {
            festival = Solar.WEEK_FESTIVAL.get(month + "-0-" + week);
            if (null != festival) {
                list.add(festival);
            }
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
        List<String> fs = Solar.OTHER_FESTIVAL.get(month + Symbol.MINUS + day);
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
        boolean g = y * 372 + m * 31 + (int) d >= 588829;
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
     * 获取农历
     *
     * @return 农历
     */
    public Lunar getLunar() {
        return new Lunar(this);
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

    public String toYmd() {
        return String.format("%04d-%02d-%02d", year, month, day);
    }

    /**
     * 阳历日期相减，获得相差天数
     *
     * @param solar 阳历
     * @return 天数
     */
    public int subtract(Solar solar) {
        return getDays(solar.getYear(), solar.getMonth(), solar.getDay(), year, month, day);
    }

    /**
     * 阳历日期相减，获得相差分钟数
     *
     * @param solar 阳历
     * @return 分钟数
     */
    public int subtractMinute(Solar solar) {
        int days = subtract(solar);
        int cm = hour * 60 + minute;
        int sm = solar.getHour() * 60 + solar.getMinute();
        int m = cm - sm;
        if (m < 0) {
            m += 1440;
            days--;
        }
        m += days * 1440;
        return m;
    }

    /**
     * 是否在指定日期之后
     *
     * @param solar 阳历
     * @return true/false
     */
    public boolean isAfter(Solar solar) {
        if (year > solar.getYear()) {
            return true;
        }
        if (year < solar.getYear()) {
            return false;
        }
        if (month > solar.getMonth()) {
            return true;
        }
        if (month < solar.getMonth()) {
            return false;
        }
        if (day > solar.getDay()) {
            return true;
        }
        if (day < solar.getDay()) {
            return false;
        }
        if (hour > solar.getHour()) {
            return true;
        }
        if (hour < solar.getHour()) {
            return false;
        }
        if (minute > solar.getMinute()) {
            return true;
        }
        if (minute < solar.getMinute()) {
            return false;
        }
        return second > solar.getSecond();
    }

    /**
     * 是否在指定日期之前
     *
     * @param solar 阳历
     * @return true/false
     */
    public boolean isBefore(Solar solar) {
        if (year > solar.getYear()) {
            return false;
        }
        if (year < solar.getYear()) {
            return true;
        }
        if (month > solar.getMonth()) {
            return false;
        }
        if (month < solar.getMonth()) {
            return true;
        }
        if (day > solar.getDay()) {
            return false;
        }
        if (day < solar.getDay()) {
            return true;
        }
        if (hour > solar.getHour()) {
            return false;
        }
        if (hour < solar.getHour()) {
            return true;
        }
        if (minute > solar.getMinute()) {
            return false;
        }
        if (minute < solar.getMinute()) {
            return true;
        }
        return second < solar.getSecond();
    }

    /**
     * 年推移
     *
     * @param years 年数
     * @return 阳历
     */
    public Solar nextYear(int years) {
        int y = year + years;
        int m = month;
        int d = day;
        // 2月处理
        if (2 == m) {
            if (d > 28) {
                if (!isLeapYear(y)) {
                    d = 28;
                }
            }
        }
        if (1582 == y && 10 == m) {
            if (d > 4 && d < 15) {
                d += 10;
            }
        }
        return from(y, m, d, hour, minute, second);
    }

    /**
     * 月推移
     *
     * @param months 月数
     * @return 阳历
     */
    public Solar nextMonth(int months) {
        Month month = Month.from(year, this.month).next(months);
        month = month.next(months);
        int y = month.getYear();
        int m = month.getMonth();
        int d = day;
        // 2月处理
        if (2 == m) {
            if (d > 28) {
                if (!isLeapYear(y)) {
                    d = 28;
                }
            }
        }
        if (1582 == y && 10 == m) {
            if (d > 4 && d < 15) {
                d += 10;
            }
        }
        return from(y, m, d, hour, minute, second);
    }

    /**
     * 小时推移
     *
     * @param hours 小时数
     * @return 阳历
     */
    public Solar nextHour(int hours) {
        int h = hour + hours;
        int n = h < 0 ? -1 : 1;
        int hour = Math.abs(h);
        int days = hour / 24 * n;
        hour = (hour % 24) * n;
        if (hour < 0) {
            hour += 24;
            days--;
        }
        Solar solar = next(days);
        return from(solar.getYear(), solar.getMonth(), solar.getDay(), hour, solar.getMinute(), solar.getSecond());
    }

    /**
     * 获取往后推几天的阳历日期，如果要往前推，则天数用负数
     *
     * @param days 天数
     * @return {@link Solar}
     */
    public Solar next(int days) {
        int y = year;
        int m = month;
        int d = day;
        if (1582 == y && 10 == m) {
            if (d > 4) {
                d -= 10;
            }
        }
        if (days > 0) {
            d += days;
            int daysInMonth = getDaysOfMonth(y, m);
            while (d > daysInMonth) {
                d -= daysInMonth;
                m++;
                if (m > 12) {
                    m = 1;
                    y++;
                }
                daysInMonth = getDaysOfMonth(y, m);
            }
        } else if (days < 0) {
            while (d + days <= 0) {
                m--;
                if (m < 1) {
                    m = 12;
                    y--;
                }
                d += getDaysOfMonth(y, m);
            }
            d += days;
        }
        if (1582 == y && 10 == m) {
            if (d > 4) {
                d += 10;
            }
        }
        return from(y, m, d, hour, minute, second);
    }

    /**
     * 取往后推几天的阳历日期，如果要往前推，则天数用负数
     *
     * @param days        天数
     * @param onlyWorkday 是否仅限工作日
     * @return {@link Solar}
     */
    public Solar next(int days, boolean onlyWorkday) {
        if (!onlyWorkday) {
            return next(days);
        }
        Solar solar = from(year, month, day, hour, minute, second);
        if (days != 0) {
            int rest = Math.abs(days);
            int add = days < 0 ? -1 : 1;
            while (rest > 0) {
                solar = solar.next(add);
                boolean work = true;
                Holiday holiday = Holiday.getHoliday(solar.getYear(), solar.getMonth(), solar.getDay());
                if (null == holiday) {
                    int week = solar.getWeek();
                    if (0 == week || 6 == week) {
                        work = false;
                    }
                } else {
                    work = holiday.isWork();
                }
                if (work) {
                    rest -= 1;
                }
            }
        }
        return solar;
    }

    /**
     * 构建字符串内容
     *
     * @param args 可选参数-简化输出
     * @return 字符串内容
     */
    public String build(boolean... args) {
        // 年月日
        String strYmd = this.year + Symbol.MINUS
                + (this.month < 10 ? "0" : Normal.EMPTY) + this.month + Symbol.MINUS
                + (this.day < 10 ? "0" : Normal.EMPTY) + this.day;

        // 年月日时分秒
        String strYmdHms = strYmd + Symbol.SPACE
                + (hour < 10 ? "0" : Normal.EMPTY) + hour + Symbol.C_COLON
                + (minute < 10 ? "0" : Normal.EMPTY) + minute + Symbol.C_COLON
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
                    s.append(Symbol.PARENTHESE_RIGHT);
                }
                for (String f : getOtherFestivals()) {
                    s.append(" (");
                    s.append(f);
                    s.append(Symbol.PARENTHESE_RIGHT);
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
            year = Solar.from(date).getYear();
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
            return new Year(year + years);
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
            Solar solar = Solar.from(date);
            this.year = solar.getYear();
            this.month = solar.getMonth();
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
            Month m = Month.from(year, month).next(MONTH_COUNT * halfYears);
            return new Semester(m.getYear(), m.getMonth());
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
            Solar solar = Solar.from(date);
            this.year = solar.getYear();
            this.month = solar.getMonth();
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
            Month m = Month.from(year, month).next(MONTH_COUNT * seasons);
            return new Quarter(m.getYear(), m.getMonth());
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
            Solar solar = Solar.from(date);
            this.year = solar.getYear();
            this.month = solar.getMonth();
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
         * 获取本月的阳历周列表
         *
         * @param start 星期几作为一周的开始，1234560分别代表星期一至星期天
         * @return 周列表
         */
        public List<Week> getWeeks(int start) {
            List<Week> l = new ArrayList<>();
            Week week = new Week(year, month, 1, start);
            while (true) {
                l.add(week);
                week = week.next(1, false);
                Solar firstDay = week.getFirstDay();
                if (firstDay.getYear() > year || firstDay.getMonth() > month) {
                    break;
                }
            }
            return l;
        }

        /**
         * 获取往后推几个月的阳历月，如果要往前推，则月数用负数
         *
         * @param months 月数
         * @return 阳历月
         */
        public Month next(int months) {
            int n = months < 0 ? -1 : 1;
            int m = Math.abs(months);
            int y = year + m / 12 * n;
            m = month + m % 12 * n;
            if (m > 12) {
                m -= 12;
                y++;
            } else if (m < 1) {
                m += 12;
                y--;
            }
            return new Month(y, m);
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
            return this.year + Symbol.MINUS + this.month;
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
            Solar solar = Solar.from(date);
            this.year = solar.getYear();
            this.month = solar.getMonth();
            this.day = solar.getDay();
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
            int offset = Solar.from(year, month, 1).getWeek() - start;
            if (offset < 0) {
                offset += 7;
            }
            return (int) Math.ceil((day + offset) / 7D);
        }

        /**
         * 获取当前日期是在当年第几周
         *
         * @return 周序号，从1开始
         */
        public int getIndexInYear() {
            int offset = Solar.from(year, 1, 1).getWeek() - start;
            if (offset < 0) {
                offset += 7;
            }
            return (int) Math.ceil((Solar.getDaysInYear(year, month, day) + offset) / 7D);
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
                return new Week(year, month, day, start);
            }
            Solar solar = Solar.from(year, month, day);
            if (separateMonth) {
                int n = weeks;
                Week week = new Week(solar.getYear(), solar.getMonth(), solar.getDay(), start);
                int month = this.month;
                boolean plus = n > 0;
                while (0 != n) {
                    solar = solar.next(plus ? 7 : -7);
                    week = new Week(solar.getYear(), solar.getMonth(), solar.getDay(), start);
                    int weekMonth = week.getMonth();
                    if (month != weekMonth) {
                        int index = week.getIndex();
                        if (plus) {
                            if (1 == index) {
                                Solar firstDay = week.getFirstDay();
                                week = new Week(firstDay.getYear(), firstDay.getMonth(), firstDay.getDay(), start);
                                weekMonth = week.getMonth();
                            } else {
                                solar = Solar.from(week.getYear(), week.getMonth(), 1);
                                week = new Week(solar.getYear(), solar.getMonth(), solar.getDay(), start);
                            }
                        } else {
                            if (Solar.getWeeksOfMonth(week.getYear(), week.getMonth(), start) == index) {
                                Solar lastDay = week.getFirstDay().next(6);
                                week = new Week(lastDay.getYear(), lastDay.getMonth(), lastDay.getDay(), start);
                                weekMonth = week.getMonth();
                            } else {
                                solar = Solar.from(week.getYear(), week.getMonth(), Solar.getDaysOfMonth(week.getYear(), week.getMonth()));
                                week = new Week(solar.getYear(), solar.getMonth(), solar.getDay(), start);
                            }
                        }
                        month = weekMonth;
                    }
                    n -= plus ? 1 : -1;
                }
                return week;
            } else {
                solar = solar.next(weeks * 7);
                return new Week(solar.getYear(), solar.getMonth(), solar.getDay(), start);
            }
        }

        /**
         * 获取本周第一天的阳历日期（可能跨月）
         *
         * @return 本周第一天的阳历日期
         */
        public Solar getFirstDay() {
            Solar solar = Solar.from(year, month, day);
            int prev = solar.getWeek() - start;
            if (prev < 0) {
                prev += 7;
            }
            return solar.next(-prev);
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
