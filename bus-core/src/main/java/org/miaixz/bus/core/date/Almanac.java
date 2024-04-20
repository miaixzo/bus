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
package org.miaixz.bus.core.date;

import org.miaixz.bus.core.exception.InternalException;
import org.miaixz.bus.core.lang.Fields;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.RegEx;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.toolkit.ArrayKit;
import org.miaixz.bus.core.toolkit.DateKit;
import org.miaixz.bus.core.toolkit.StringKit;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.chrono.ChronoLocalDateTime;
import java.time.temporal.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 日期计算类
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Almanac extends Converter {

    /**
     * 获取年，比如2020
     *
     * @param date Date
     * @return int
     */
    public static int getYear(Date date) {
        return toLocalDateTime(date).getYear();
    }

    /**
     * 获取年，比如2020
     *
     * @param instant Instant
     * @return int
     */
    public static int getYear(Instant instant) {
        return toLocalDateTime(instant).getYear();
    }

    /**
     * 获取年，比如2020
     * LocalDateTime LocalDate ZonedDateTime 可以直接getYear()
     *
     * @param localDateTime LocalDateTime
     * @return int
     */
    public static int getYear(LocalDateTime localDateTime) {
        return localDateTime.getYear();
    }

    /**
     * 获取年，比如2020
     * LocalDateTime LocalDate ZonedDateTime 可以直接getYear()
     *
     * @param localDate localDate
     * @return int
     */
    public static int getYear(LocalDate localDate) {
        return localDate.getYear();
    }

    /**
     * 获取年份时间段内的所有年
     *
     * @param startDate 开始时间
     * @param endDate   截止时间
     * @return the list
     */
    public static List<String> getYear(String startDate, String endDate) {
        List<String> list = new ArrayList<>();
        try {
            DateFormat df = new SimpleDateFormat(Fields.NORM_YEAR_PATTERN);
            Date date1 = df.parse(startDate);
            Date date2 = df.parse(endDate);
            Calendar c1 = Calendar.getInstance();
            Calendar c2 = Calendar.getInstance();

            list.add(df.format(date1));
            c1.setTime(date1);
            c2.setTime(date2);
            while (c1.compareTo(c2) < 0) {
                c1.add(Calendar.YEAR, 1);
                Date ss = c1.getTime();
                String text = df.format(ss);
                list.add(text);
            }
        } catch (ParseException e) {
            throw new InternalException(e);
        }
        return list;
    }

    /**
     * 获得指定日期年份和季节
     * 格式：[20131]表示2013年第一季度
     *
     * @param cal 日期
     */
    private static String getQuarter(Calendar cal) {
        return new StringBuilder().append(cal.get(Calendar.YEAR)).append(cal.get(Calendar.MONTH) / 3 + 1).toString();
    }

    /**
     * 获得指定日期区间内的年份和季节
     *
     * @param startDate 起始日期(包含)
     * @param endDate   结束日期(包含)
     * @return 季度列表 ，元素类似于 20132
     */
    public static LinkedHashSet<String> getQuarter(Date startDate, Date endDate) {
        if (null == startDate || null == endDate) {
            return new LinkedHashSet<>(0);
        }
        return getQuarter(startDate.getTime(), endDate.getTime());
    }

    /**
     * 获得指定日期区间内的年份和季节
     *
     * @param startDate 起始日期(包含)
     * @param endDate   结束日期(包含)
     * @return 季度列表 ，元素类似于 20132
     */
    public static LinkedHashSet<String> getQuarter(long startDate, long endDate) {
        LinkedHashSet<String> quarters = new LinkedHashSet<>();
        final Calendar cal = toCalendar(startDate);
        while (startDate <= endDate) {
            // 如果开始时间超出结束时间，让结束时间为开始时间，处理完后结束循环
            quarters.add(getQuarter(cal));

            cal.add(Calendar.MONTH, 3);
            startDate = cal.getTimeInMillis();
        }

        return quarters;
    }

    /**
     * (季度) 计算本期的上期起止时间和同期的起止时间 返回的mao key 时间起止：beginkey endkey 季度起止： beginWkey
     * endWkey 本期的时间起止：begin end 季度：beginW endW type 0本期 1上期 2去年同期 季度
     *
     * @param type      计算上期
     * @param beginkey  开始时间key
     * @param endkey    截止时间key
     * @param beginWkey 开始周key
     * @param endWkey   截止周key
     * @param begin     开始时间
     * @param end       截止时间
     * @param beginW    开始周
     * @param endW      截止周
     * @return the map
     */
    public static Map<String, String> getQuarter(int type,
                                                 String beginkey,
                                                 String endkey,
                                                 String beginWkey,
                                                 String endWkey,
                                                 String begin,
                                                 String end,
                                                 String beginW,
                                                 String endW) {
        Map<String, String> map = new HashMap<>();
        try {
            DateFormat sdf = new SimpleDateFormat(Fields.NORM_YEAR_PATTERN);
            Date date1 = sdf.parse(begin);
            Date dEnd = sdf.parse(end);
            Calendar calBegin = Calendar.getInstance();
            calBegin.setTime(date1);
            calBegin.set(Calendar.MONTH,
                    getMonthOfQuarter(Integer.parseInt(beginW)));
            Calendar calEnd = Calendar.getInstance();
            calEnd.setTime(dEnd);
            calEnd.set(Calendar.MONTH,
                    getMonthOfQuarter(Integer.parseInt(endW)));

            if (type == 1) {
                int quarter = ((Integer.parseInt(end) - Integer.parseInt(begin))
                        * 4
                        + (Integer.parseInt(endW) - Integer.parseInt(beginW)) + 1) * 3;

                calBegin.add(Calendar.MONTH, -quarter);
                calEnd.add(Calendar.MONTH, -quarter);
                map.put(beginWkey, String.valueOf(getQuarterOfMonth(calBegin
                        .get(Calendar.MONTH))));
                map.put(endWkey, String.valueOf(getQuarterOfMonth(calEnd
                        .get(Calendar.MONTH))));
            } else if (type == 2) {
                calBegin.add(Calendar.YEAR, -1);
                calEnd.add(Calendar.YEAR, -1);

                map.put(beginWkey, beginW);
                map.put(endWkey, endW);
            }
            map.put(beginkey,
                    calBegin.get((Calendar.YEAR))
                            + Symbol.MINUS
                            + getMonthOfQuarter(0, Integer.parseInt(map.get(beginWkey))));
            map.put(endkey,
                    calEnd.get((Calendar.YEAR))
                            + Symbol.MINUS
                            + getMonthOfQuarter(1, Integer.parseInt(map.get(endWkey))));
        } catch (ParseException e) {
            throw new InternalException(e);
        }
        return map;
    }

    /**
     * (季度)获取季度份时间段内的所有季度
     *
     * @param StartDate 开始日期
     * @param beginQ    开始季度
     * @param endDate   截止日期
     * @param endQ      结束季度
     * @return the list
     */
    public static List<String> getQuarter(String StartDate,
                                          String beginQ,
                                          String endDate,
                                          String endQ) {
        try {
            DateFormat sdf = new SimpleDateFormat(Fields.NORM_YEAR_MTOTH_PATTERN);
            Date date1 = sdf.parse(StartDate);
            Date dEnd = sdf.parse(endDate);

            Calendar calBegin = Calendar.getInstance();
            calBegin.setTime(date1);
            Calendar calEnd = Calendar.getInstance();
            calEnd.setTime(dEnd);
            List<String> list = new ArrayList<>();
            int beginY = calBegin.get(Calendar.YEAR);
            int beginYQ = Integer.parseInt(beginQ);
            int endY = calEnd.get(Calendar.YEAR);
            int endYQ = Integer.parseInt(endQ);
            do {
                list.add(beginY + "年第" + beginYQ + "季度");
                if (beginY == endY && beginYQ == endYQ) {
                    return list;
                }
                beginYQ++;
                if (beginYQ > 4) {
                    beginYQ = 1;
                    beginY++;
                }
            } while (true);

        } catch (ParseException e) {
            throw new InternalException(e);
        }
    }

    /**
     * 获取农历月份
     *
     * @param month 　农历月数值表示
     * @return 农历月传统字符表示
     */
    public static String getMonth(int month) {
        return Fields.CN_MONTH[month - 1];
    }

    /**
     * 获取月， 比如 1
     *
     * @param date Date
     * @return int
     */
    public static int getMonth(Date date) {
        return toLocalDateTime(date).getMonthValue();
    }

    /**
     * 获取月， 比如 1
     *
     * @param instant Instant
     * @return int
     */
    public static int getMonth(Instant instant) {
        return toLocalDateTime(instant).getMonthValue();
    }

    /**
     * 获取月， 比如 1
     * LocalDateTime LocalDate ZonedDateTime 可以直接getMonthValue()
     *
     * @param localDateTime LocalDateTime
     * @return int
     */
    public static int getMonth(LocalDateTime localDateTime) {
        return localDateTime.getMonthValue();
    }

    /**
     * 获取月， 比如 1
     * LocalDateTime LocalDate ZonedDateTime 可以直接getMonthValue()
     *
     * @param localDate LocalDate
     * @return int
     */
    public static int getMonth(LocalDate localDate) {
        return localDate.getMonthValue();
    }

    /**
     * 当时间段内的所有月份
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return the list
     */
    public static List<String> getMonth(String startDate, String endDate) {
        List<String> list = new ArrayList<>();
        try {
            DateFormat df = new SimpleDateFormat(Fields.NORM_YEAR_MTOTH_PATTERN);

            Date date1 = df.parse(startDate);
            Date date2 = df.parse(endDate);
            Calendar c1 = Calendar.getInstance();
            Calendar c2 = Calendar.getInstance();

            list.add(df.format(date1));
            c1.setTime(date1);
            c2.setTime(date2);
            while (c1.compareTo(c2) < 0) {
                c1.add(Calendar.MONTH, 1);
                Date ss = c1.getTime();
                String text = df.format(ss);
                list.add(text);
            }
        } catch (ParseException e) {
            throw new InternalException(e);
        }
        return list;
    }

    /**
     * (月)计算本期的上期和去年同期 1 上期 2同期 返回的mapkay beginkey endkey 本期起止：begin end
     * 计算上期的起止时间 和去年同期 type 0本期 1上期 2去年同期
     *
     * @param type     计算上期
     * @param beginkey 开始时间key
     * @param endkey   截止时间key
     * @param begin    开始时间
     * @param end      截止时间
     * @return the map
     */
    public static Map<String, String> getMonth(int type,
                                               String beginkey,
                                               String endkey,
                                               String begin,
                                               String end) {
        Map<String, String> map = new HashMap<>();
        try {
            DateFormat sdf = new SimpleDateFormat(Fields.NORM_YEAR_MTOTH_PATTERN);
            Date date1 = sdf.parse(begin);
            Date dEnd = sdf.parse(end);
            Calendar calBegin = Calendar.getInstance();
            calBegin.setTime(date1);
            Calendar calEnd = Calendar.getInstance();
            calEnd.setTime(dEnd);
            if (type == 1) {
                int year = calBegin.get(Calendar.YEAR);
                int month = calBegin.get(Calendar.MONTH);

                int year1 = calEnd.get(Calendar.YEAR);
                int month1 = calEnd.get(Calendar.MONTH);
                int result;
                if (year == year1) {
                    result = month1 - month;
                } else {
                    result = 12 * (year1 - year) + month1 - month;
                }
                result++;
                calBegin.add(Calendar.MONTH, -result);
                calEnd.add(Calendar.MONTH, -result);
            } else if (type == 2) {
                calBegin.add(Calendar.YEAR, -1);
                calEnd.add(Calendar.YEAR, -1);
            }
            map.put(beginkey, sdf.format(calBegin.getTime()));
            map.put(endkey, sdf.format(calEnd.getTime()));
        } catch (ParseException e) {
            throw new InternalException(e);
        }
        return map;
    }

    /**
     * 获取月英文全称， 比如 January
     *
     * @param date Date
     * @return String
     */
    public static String getMonthEnLong(Date date) {
        return Fields.Month.getFullNameEnByCode(getMonth(date));
    }

    /**
     * 获取月英文全称， 比如 January
     *
     * @param instant Instant
     * @return String
     */
    public static String getMonthEnLong(Instant instant) {
        return Fields.Month.getFullNameEnByCode(getMonth(instant));
    }

    /**
     * 获取月英文全称， 比如 January
     *
     * @param localDateTime LocalDateTime
     * @return String
     */
    public static String getMonthEnLong(LocalDateTime localDateTime) {
        return Fields.Month.getFullNameEnByCode(getMonth(localDateTime));
    }

    /**
     * 获取月英文全称， 比如 January
     *
     * @param localDate LocalDate
     * @return String
     */
    public static String getMonthEnLong(LocalDate localDate) {
        return Fields.Month.getFullNameEnByCode(getMonth(localDate));
    }

    /**
     * 获取月英文简称， 比如 Jan
     *
     * @param date Date
     * @return String
     */
    public static String getMonthEnShort(Date date) {
        return Fields.Month.getFullNameEnByCode(getMonth(date));
    }

    /**
     * 获取月英文简称， 比如 Jan
     *
     * @param instant Instant
     * @return String
     */
    public static String getMonthEnShort(Instant instant) {
        return Fields.Month.getShortNameEnByCode(getMonth(instant));
    }

    /**
     * 获取月英文简称， 比如 Jan
     *
     * @param localDateTime LocalDateTime
     * @return String
     */
    public static String getMonthEnShort(LocalDateTime localDateTime) {
        return Fields.Month.getShortNameEnByCode(getMonth(localDateTime));
    }

    /**
     * 获取月英文简称， 比如 Jan
     *
     * @param localDate LocalDate
     * @return String
     */
    public static String getMonthEnShort(LocalDate localDate) {
        return Fields.Month.getShortNameEnByCode(localDate.getMonthValue());
    }

    /**
     * 获取月英文简称大写， 比如 JAN
     *
     * @param date Date
     * @return String
     */
    public static String getMonthEnShortUpper(Date date) {
        return Fields.Month.getShortNameEnByCode(getMonth(date)).toUpperCase();
    }

    /**
     * 获取月英文简称大写， 比如 JAN
     *
     * @param instant Instant
     * @return String
     */
    public static String getMonthEnShortUpper(Instant instant) {
        return Fields.Month.getShortNameEnByCode(getMonth(instant)).toUpperCase();
    }

    /**
     * 获取月英文简称大写， 比如 JAN
     *
     * @param localDateTime LocalDateTime
     * @return String
     */
    public static String getMonthEnShortUpper(LocalDateTime localDateTime) {
        return Fields.Month.getShortNameEnByCode(getMonth(localDateTime)).toUpperCase();
    }

    /**
     * 获取月英文简称大写， 比如 JAN
     *
     * @param localDate LocalDate
     * @return String
     */
    public static String getMonthEnShortUpper(LocalDate localDate) {
        return Fields.Month.getShortNameEnByCode(localDate.getMonthValue()).toUpperCase();
    }

    /**
     * 获取月份中文全称， 比如一月
     *
     * @param date Date
     * @return String
     */
    public static String getMonthCnLong(Date date) {
        return Fields.Month.getFullNameCnByCode(getMonth(date));
    }

    /**
     * 获取月份中文全称， 比如一月
     *
     * @param instant Instant
     * @return String
     */
    public static String getMonthCnLong(Instant instant) {
        return Fields.Month.getFullNameCnByCode(getMonth(instant));
    }

    /**
     * 获取月份中文全称， 比如一月
     *
     * @param localDateTime LocalDateTime
     * @return String
     */
    public static String getMonthCnLong(LocalDateTime localDateTime) {
        return Fields.Month.getFullNameCnByCode(getMonth(localDateTime));
    }

    /**
     * 获取月份中文全称， 比如一月
     *
     * @param localDate LocalDate
     * @return String
     */
    public static String getMonthCnLong(LocalDate localDate) {
        return Fields.Month.getFullNameCnByCode(localDate.getMonthValue());
    }

    /**
     * 获取月份中文简称， 比如一
     *
     * @param date Date
     * @return String
     */
    public static String getMonthCnShort(Date date) {
        return Fields.Month.getShortNameCnByCode(getMonth(date));
    }

    /**
     * 获取月份中文简称， 比如一
     *
     * @param instant Instant
     * @return String
     */
    public static String getMonthCnShort(Instant instant) {
        return Fields.Month.getShortNameCnByCode(getMonth(instant));
    }

    /**
     * 获取月份中文简称， 比如一
     *
     * @param localDateTime LocalDateTime
     * @return String
     */
    public static String getMonthCnShort(LocalDateTime localDateTime) {
        return Fields.Month.getShortNameCnByCode(getMonth(localDateTime));
    }

    /**
     * 获取月份中文简称， 比如一
     *
     * @param localDate LocalDate
     * @return String
     */
    public static String getMonthCnShort(LocalDate localDate) {
        return Fields.Month.getShortNameCnByCode(localDate.getMonthValue());
    }

    /**
     * 根据季度返回季度第一月
     *
     * @param quarter 季度
     * @return 月份
     */
    public static int getMonthOfQuarter(int quarter) {
        if (quarter == 1) {
            return 1;
        }
        if (quarter == 2) {
            return 4;
        }
        if (quarter == 3) {
            return 7;
        }
        if (quarter == 4) {
            return 10;
        }
        return 1;
    }

    /**
     * 根据季度返回季度第一月或最后一月 0 起始月 1截止月
     *
     * @param type    第一个月份
     * @param quarter 季度
     * @return 月份
     */
    public static String getMonthOfQuarter(int type, int quarter) {
        if (quarter == 1) {
            if (type == 1) {
                return "03";
            }
            return "01";
        }
        if (quarter == 2) {
            if (type == 1) {
                return "06";
            }
            return "04";
        }
        if (quarter == 3) {
            if (type == 1) {
                return "09";
            }
            return "07";
        }
        if (quarter == 4) {
            if (type == 1) {
                return "12";
            }
            return "10";
        }
        return "01";
    }

    /**
     * 根据月份获取所在季度
     *
     * @param month 月份
     * @return 季度
     */
    public static int getQuarterOfMonth(int month) {
        int quarter = 1;
        if (month >= 1 && month <= 3) {
            return 1;
        }
        if (month >= 4 && month <= 6) {
            return 2;
        }
        if (month >= 7 && month <= 9) {
            return 3;
        }
        if (month >= 10 && month <= 12) {
            return 4;
        }
        return quarter;
    }

    /**
     * (周)返回起止时间内的所有自然周
     *
     * @param begin  时间起
     * @param end    时间止
     * @param startw 周起
     * @param endW   周止
     * @return the list
     */
    public static List<String> getWeek(String begin, String end, String startw, String endW) {
        List<String> lDate = new ArrayList<>();
        try {
            DateFormat sdf = new SimpleDateFormat(Fields.NORM_YEAR_PATTERN);
            Date date1 = sdf.parse(begin);
            Date dEnd = sdf.parse(end);
            Calendar calBegin = Calendar.getInstance();
            calBegin.setTime(date1);
            Calendar calEnd = Calendar.getInstance();
            calEnd.setTime(dEnd);
            calBegin.setFirstDayOfWeek(Calendar.MONDAY);
            int beginww = Integer.parseInt(startw);
            int endww = Integer.parseInt(endW);

            int beginY = calBegin.get(Calendar.YEAR);
            int endY = calEnd.get(Calendar.YEAR);

            int weekall = getWeek(beginY + Normal.EMPTY);
            do {
                lDate.add(beginY + "年第" + beginww + "周");
                if (beginww == weekall) {
                    beginww = 0;
                    beginY++;
                    weekall = getWeek(beginY + Normal.EMPTY);
                }
                if (beginY == endY && beginww == endww) {
                    break;
                }
                beginww++;
            } while (beginY <= endY);
        } catch (ParseException e) {
            throw new InternalException(e);
        }
        return lDate;
    }

    /**
     * 返回该年有多少个自然周
     *
     * @param year 最多53 一般52 如果12月月末今天在本年53周(属于第二年第一周) 那么按照当年52周算
     * @return the int
     */
    public static int getWeek(String year) {
        Calendar calendar = Calendar.getInstance();
        try {
            calendar.setTime(Fields.PURE_DATETIME_FORMAT.parse(year + "-12-31"));
        } catch (ParseException e) {
            throw new InternalException(e);
        }
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        int week = calendar.get(Calendar.WEEK_OF_YEAR);
        if (week != 53) {
            week = 52;
        }
        return week;
    }

    /**
     * 获取两个日期段相差的周数
     *
     * @param start 日期
     * @param end   日期
     * @return the int
     */
    public static int getWeek(Date start, Date end) {
        Calendar c_begin = Calendar.getInstance();
        c_begin.setTime(start);
        Calendar c_end = Calendar.getInstance();
        c_end.setTime(end);
        int count = 0;
        c_begin.setFirstDayOfWeek(Calendar.MONDAY);
        c_end.setFirstDayOfWeek(Calendar.MONDAY);
        while (c_begin.before(c_end)) {
            if (c_begin.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                count++;
            }
            c_begin.add(Calendar.DAY_OF_YEAR, 1);
        }
        return count;
    }

    /**
     * 获取农历日的表示
     *
     * @param lunarDay 　农历日数值表示
     * @return 农历日传统字符表示
     */
    public static String getDayOfMonth(int lunarDay) {
        return Fields.CN_DAY[lunarDay];
    }

    /**
     * 获得指定月份的总天数
     *
     * @param month      月份
     * @param isLeapYear 是否闰年
     * @return 天
     */
    public static int getDayOfMonth(int month, boolean isLeapYear) {
        return java.time.Month.of(month).length(isLeapYear);
    }

    /**
     * 获取天
     *
     * @param date Date
     * @return int
     */
    public static int getDayOfMonth(Date date) {
        return toLocalDateTime(date).getDayOfMonth();
    }

    /**
     * 获取天
     *
     * @param instant Instant
     * @return int
     */
    public static int getDayOfMonth(Instant instant) {
        return toLocalDateTime(instant).getDayOfMonth();
    }

    /**
     * 获取天
     * LocalDateTime LocalDate ZonedDateTime 可以直接.getDayOfMonth()
     *
     * @param localDateTime LocalDateTime
     * @return int
     */
    public static int getDayOfMonth(LocalDateTime localDateTime) {
        return localDateTime.getDayOfMonth();
    }

    /**
     * 获取天
     * LocalDateTime LocalDate ZonedDateTime 可以直接.getDayOfMonth()
     *
     * @param localDate localDate
     * @return int
     */
    public static int getDayOfMonth(LocalDate localDate) {
        return localDate.getDayOfMonth();
    }

    /**
     * 获取天（一年中）
     *
     * @param date Date
     * @return int
     */
    public static int getDayOfYear(Date date) {
        return toLocalDateTime(date).getDayOfYear();
    }

    /**
     * 获取天（一年中）
     *
     * @param instant Instant
     * @return int
     */
    public static int getDayOfYear(Instant instant) {
        return toLocalDateTime(instant).getDayOfYear();
    }

    /**
     * 获取天（一年中）
     * LocalDateTime LocalDate ZonedDateTime 可以直接.getDayOfYear()获取
     *
     * @param localDateTime LocalDateTime
     * @return int
     */
    public static int getDayOfYear(LocalDateTime localDateTime) {
        return localDateTime.getDayOfYear();
    }

    /**
     * 获取天（一年中）
     * LocalDateTime LocalDate ZonedDateTime 可以直接.getDayOfYear()获取
     *
     * @param localDate localDate
     * @return int
     */
    public static int getDayOfYear(LocalDate localDate) {
        return localDate.getDayOfYear();
    }

    /**
     * 获取某年的总天数
     *
     * @param year 年
     * @return int
     */
    public static int getLengthOfYear(int year) {
        return java.time.Year.of(year).length();
    }

    /**
     * 获取当前时间在一年中的第几天
     *
     * @return int
     */
    public static int getDayOfYear() {
        return getDayOfYear(new Date());
    }

    /**
     * 获取小时
     *
     * @param date Date
     * @return int
     */
    public static int getHour(Date date) {
        return toLocalDateTime(date).getHour();
    }

    /**
     * 获取小时
     *
     * @param instant Instant
     * @return int
     */
    public static int getHour(Instant instant) {
        return toLocalDateTime(instant).getHour();
    }

    /**
     * 获取小时
     * LocalDateTime LocalTime ZonedDateTime 可以直接.getHour()获取
     *
     * @param localDateTime LocalDateTime
     * @return int
     */
    public static int getHour(LocalDateTime localDateTime) {
        return localDateTime.getHour();
    }

    /**
     * 获取小时
     * LocalDateTime LocalTime ZonedDateTime 可以直接.getHour()获取
     *
     * @param localTime localTime
     * @return int
     */
    public static int getHour(LocalTime localTime) {
        return localTime.getHour();
    }

    /**
     * 获取分钟
     *
     * @param date Date
     * @return int
     */
    public static int getMinute(Date date) {
        return toLocalDateTime(date).getMinute();
    }

    /**
     * 获取分钟
     *
     * @param instant Instant
     * @return int
     */
    public static int getMinute(Instant instant) {
        return toLocalDateTime(instant).getMinute();
    }

    /**
     * 获取分钟
     * LocalDateTime LocalTime ZonedDateTime 可以直接.getMinute()获取
     *
     * @param localDateTime LocalDateTime
     * @return int
     */
    public static int getMinute(LocalDateTime localDateTime) {
        return localDateTime.getMinute();
    }

    /**
     * 获取分钟
     * LocalDateTime LocalTime ZonedDateTime 可以直接.getMinute()获取
     *
     * @param localTime localTime
     * @return int
     */
    public static int getMinute(LocalTime localTime) {
        return localTime.getMinute();
    }

    /**
     * 获取秒
     *
     * @param date Date
     * @return int
     */
    public static int getSecond(Date date) {
        return toLocalDateTime(date).getSecond();
    }

    /**
     * 获取秒
     *
     * @param instant Instant
     * @return int
     */
    public static int getSecond(Instant instant) {
        return toLocalDateTime(instant).getSecond();
    }

    /**
     * 获取秒
     * LocalDateTime LocalTime ZonedDateTime 可以直接.getSecond()获取
     *
     * @param localDateTime LocalDateTime
     * @return int
     */
    public static int getSecond(LocalDateTime localDateTime) {
        return localDateTime.getSecond();
    }

    /**
     * 获取秒
     * LocalDateTime LocalTime ZonedDateTime 可以直接.getSecond()获取
     *
     * @param localTime localTime
     * @return int
     */
    public static int getSecond(LocalTime localTime) {
        return localTime.getSecond();
    }

    /**
     * 获取毫秒
     *
     * @param date Date
     * @return int
     */
    public static int getMillisecond(Date date) {
        return toLocalDateTime(date).getNano() / 1_000_000;
    }

    /**
     * 获取毫秒
     *
     * @param instant Instant
     * @return int
     */
    public static int getMillisecond(Instant instant) {
        return toLocalDateTime(instant).getNano() / 1_000_000;
    }

    /**
     * 获取毫秒
     *
     * @param localDateTime LocalDateTime
     * @return int
     */
    public static int getMillisecond(LocalDateTime localDateTime) {
        return localDateTime.getNano() / 1_000_000;
    }

    /**
     * 获取毫秒
     *
     * @param localTime localTime
     * @return int
     */
    public static int getMillisecond(LocalTime localTime) {
        return localTime.getNano() / 1_000_000;
    }

    /**
     * 获取毫秒
     *
     * @param zonedDateTime zonedDateTime
     * @return int
     */
    public static int getMillisecond(ZonedDateTime zonedDateTime) {
        return zonedDateTime.getNano() / 1_000_000;
    }

    /**
     * 获取时间戳
     *
     * @return long
     */
    public static long getEpochMilli() {
        return System.currentTimeMillis();
    }

    /**
     * 获取时间戳（到秒）
     *
     * @return long
     */
    public static long getEpochSecond() {
        return System.currentTimeMillis() / 1000;
    }

    /**
     * 获取格式化时间戳字符串
     *
     * @return String 格式： yyyy-MM-dd HH:mm:ss
     */
    public static String getEpochMilliFormat() {
        return format(new Date());
    }

    /**
     * 获取格式化时间戳字符串，带毫秒
     *
     * @return String 格式： yyyy-MM-dd HH:mm:ss.SSS
     */
    public static String getEpochMilliFormatFull() {
        return format(new Date(), Fields.NORM_DATETIME_MS_PATTERN);
    }

    /**
     * 获取格式化时间戳字符串 iso格式 2020-02-18T22:37:55+0800
     *
     * @return String 格式： yyyy-MM-ddTHH:mm:ssZ
     */
    public static String getEpochMilliIsoNotFormatNoColon() {
        return format(new Date(), Fields.MSEC_PATTERN);
    }

    /**
     * 获取格式化时间戳字符串 iso格式 带毫秒 2020-02-18T22:37:55.991+0800
     *
     * @return String 格式： yyyy-MM-dd'T'HH:mm:ss.SSSZ
     */
    public static String getEpochMilliIsoFormatFullNoColon() {
        return format(new Date(), Fields.MSEC_PATTERN);
    }

    /**
     * 根据年月日创建Date，时间部分为：00:00:00
     *
     * @param year       年
     * @param month      月
     * @param dayOfMonth 日
     * @return Date
     */
    public static Date getDate(int year, int month, int dayOfMonth) {
        return toDate(LocalDate.of(year, month, dayOfMonth));
    }

    /**
     * 根据年月日时分秒创建Date
     *
     * @param year       年
     * @param month      月
     * @param dayOfMonth 日
     * @param hour       时
     * @param minute     分
     * @param second     秒
     * @return Date
     */
    public static Date getDate(int year, int month, int dayOfMonth, int hour, int minute, int second) {
        return toDate(LocalDateTime.of(year, month, dayOfMonth, hour, minute, second));
    }

    /**
     * 根据年月日时分秒毫秒创建Date
     *
     * @param year          年
     * @param month         月
     * @param dayOfMonth    日
     * @param hour          时
     * @param minute        分
     * @param second        秒
     * @param milliOfSecond 毫秒
     * @return Date
     */
    public static Date getDate(int year, int month, int dayOfMonth, int hour, int minute, int second, int milliOfSecond) {
        return toDate(LocalDateTime.of(year, month, dayOfMonth, hour, minute, second, milliOfSecond * 1000_000));
    }

    /**
     * 获取指定月第一天
     *
     * @param year  年
     * @param month 月
     * @return Date
     */
    public static Date getDateStartOfMonth(int year, int month) {
        return toDateStartOfMonth(YearMonth.of(year, month));
    }

    /**
     * 获取指定月最后一天
     *
     * @param year  年
     * @param month 月
     * @return Date
     */
    public static Date getDateEndOfMonth(int year, int month) {
        return toDateEndOfMonth(YearMonth.of(year, month));
    }

    /**
     * 计算年龄
     *
     * @param birthDay 生日
     * @return int 年龄
     */
    public static int getAge(LocalDate birthDay) {
        Period period = periodBetween(birthDay, LocalDate.now());
        if (period.getYears() < 0) {
            throw new DateTimeException("birthDay is after now!");
        } else {
            return period.getYears();
        }
    }

    /**
     * 计算年龄
     *
     * @param birthDay 生日
     * @return int 年龄
     */
    public static int getAge(Date birthDay) {
        return getAge(toLocalDate(birthDay));
    }

    /**
     * 计算年龄
     *
     * @param birthDay 生日
     * @return int 年龄
     */
    public static int getAge(LocalDateTime birthDay) {
        return getAge(toLocalDate(birthDay));
    }

    /**
     * 出生日期转年龄
     *
     * @param birthday 时间戳字符串
     * @return int 年龄
     */
    public static int getAge(String birthday) {
        return getAge(Long.parseLong(birthday), Calendar.getInstance().getTimeInMillis());
    }

    /**
     * 计算相对于dateToCompare的年龄,长用于计算指定生日在某年的年龄
     *
     * @param birthday      生日
     * @param dateToCompare 需要对比的日期
     * @return 年龄
     */
    public static int getAge(Date birthday, Date dateToCompare) {
        if (null == dateToCompare) {
            dateToCompare = date();
        }
        return getAge(birthday.getTime(), dateToCompare.getTime());
    }

    /**
     * 计算相对于dateToCompare的年龄,长用于计算指定生日在某年的年龄
     *
     * @param birthday      生日
     * @param dateToCompare 需要对比的日期
     * @return 年龄
     */
    public static int getAge(Calendar birthday, Calendar dateToCompare) {
        return getAge(birthday.getTimeInMillis(), dateToCompare.getTimeInMillis());
    }

    /**
     * 计算相对于dateToCompare的年龄,长用于计算指定生日在某年的年龄
     *
     * @param birthDay      生日
     * @param dateToCompare 需要对比的日期
     * @return 年龄
     */
    public static int getAge(long birthDay, long dateToCompare) {
        if (birthDay > dateToCompare) {
            throw new IllegalArgumentException("Birthday is after dateToCompare!");
        }

        final Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(dateToCompare);

        final int year = cal.get(Calendar.YEAR);
        final int month = cal.get(Calendar.MONTH);
        final int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
        final boolean isLastDayOfMonth = dayOfMonth == cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        cal.setTimeInMillis(birthDay);
        int age = year - cal.get(Calendar.YEAR);
        // 当前日期，则为0岁
        if (age == 0) {
            return 0;
        }

        final int monthBirth = cal.get(Calendar.MONTH);
        if (month == monthBirth) {

            final int dayOfMonthBirth = cal.get(Calendar.DAY_OF_MONTH);
            final boolean isLastDayOfMonthBirth = dayOfMonthBirth == cal.getActualMaximum(Calendar.DAY_OF_MONTH);
            // 法定生日当天不算年龄，从第二天开始计算
            if ((false == isLastDayOfMonth || false == isLastDayOfMonthBirth) && dayOfMonth <= dayOfMonthBirth) {
                // 如果生日在当月，但是未达到生日当天的日期，年龄减一
                age--;
            }
        } else if (month < monthBirth) {
            // 如果当前月份未达到生日的月份，年龄计算减一
            age--;
        }

        return age;
    }

    /**
     * 计算年龄, 返回几周岁几个月几天
     *
     * @param birthDay      出生日期:yyyy-MM-dd
     * @param dateToCompare 对比日期: yyyy-MM-dd, 默认当前系统日期
     * @return java.lang.String 返回几周岁几个月几天: 如 6 岁 3 个月 2 天
     */
    public static String getAge(String birthDay, String dateToCompare) {
        if (null == birthDay || birthDay.trim().length() == 0) {
            throw new IllegalArgumentException("birthDay must not be null !");
        }
        if (null == dateToCompare || dateToCompare.trim().length() == 0) {
            dateToCompare = Fields.NORM_DATE_FORMAT.format(new Date());
        }
        String[] data = StringKit.splitToArray(birthDay, Symbol.MINUS);
        if (data.length < 3) {
            throw new IllegalArgumentException("Please confirm the date format !");
        }

        Calendar birthday = new GregorianCalendar(Integer.valueOf(data[0]),
                Integer.valueOf(data[1]), Integer.valueOf(data[2]));
        Calendar now = Calendar.getInstance();
        now.setTime(parse(dateToCompare));

        // 1.日相减
        int day = now.get(Calendar.DAY_OF_MONTH) - birthday.get(Calendar.DAY_OF_MONTH);
        // 2.月相减
        int month = now.get(Calendar.MONTH) + 1 - birthday.get(Calendar.MONTH);
        // 3.年相减
        int year = now.get(Calendar.YEAR) - birthday.get(Calendar.YEAR);
        if (day < 0) {
            month -= 1;
            // 获取上个月，用来计算上个月的天数
            now.add(Calendar.MONTH, -1);
            day = day + now.getActualMaximum(Calendar.DAY_OF_MONTH);
        }
        if (month < 0) {
            month = (month + 12) % 12;
            year--;
        }

        return (year > 0 ? (year + "岁") : Normal.EMPTY)
                + (month > 0 ? (month + "个月") : Normal.EMPTY)
                + (day > 0 ? (day + "天") : "今日出生");
    }

    /**
     * 获得季度值
     *
     * @param localDateTime LocalDateTime
     * @return int 季度 1,2,3,4
     */
    public static int getQuarter(LocalDateTime localDateTime) {
        return (localDateTime.getMonthValue() + 2) / 3;
    }

    /**
     * 获得季度值
     *
     * @param localDate LocalDate
     * @return int 季度 1,2,3,4
     */
    public static int getQuarter(LocalDate localDate) {
        return (localDate.getMonthValue() + 2) / 3;
    }

    /**
     * 获得季度值
     *
     * @param date Date
     * @return int 季度 1,2,3,4
     */
    public static int getQuarter(Date date) {
        return (getMonth(date) + 2) / 3;
    }

    /**
     * 获得当前季度值
     *
     * @return int 季度 1,2,3,4
     */
    public static int getQuarter() {
        return (LocalDate.now().getMonthValue() + 2) / 3;
    }


    /**
     * 计算去年同期和上期的起止时间
     *
     * @param type     计算上期
     * @param beginkey 开始时间key
     * @param endkey   截止时间key
     * @param begin    开始时间
     * @param end      截止时间
     * @return the map
     */
    public static Map<String, String> getLast(int type,
                                              String beginkey,
                                              String endkey,
                                              String begin,
                                              String end) {
        Map<String, String> map = new HashMap<>();
        try {
            Date dBegin = Fields.PURE_DATETIME_FORMAT.parse(begin);
            Date dEnd = Fields.PURE_DATETIME_FORMAT.parse(end);
            Calendar calBegin = Calendar.getInstance();

            calBegin.setTime(dBegin);
            Calendar calEnd = Calendar.getInstance();

            calEnd.setTime(dEnd);
            if (type == 1) {

                long beginTime = dBegin.getTime();
                long endTime = dEnd.getTime();
                long inter = endTime - beginTime;
                if (inter < 0) {
                    inter = inter * (-1);
                }
                long dateMillSec = 24 * 60 * 60 * 1000;
                long dateCnt = inter / dateMillSec;
                long remainder = inter % dateMillSec;
                if (remainder != 0) {
                    dateCnt++;
                }
                int day = Integer.parseInt(String.valueOf(dateCnt)) + 1;
                calBegin.add(Calendar.DATE, -day);
                calEnd.add(Calendar.DATE, -day);
            } else if (type == 2) {
                calBegin.add(Calendar.YEAR, -1);
                calEnd.add(Calendar.YEAR, -1);
            }
            map.put(beginkey, Fields.PURE_DATETIME_FORMAT.format(calBegin.getTime()));
            map.put(endkey, Fields.PURE_DATETIME_FORMAT.format(calEnd.getTime()));
        } catch (ParseException e) {
            throw new InternalException(e);
        }
        return map;
    }

    /**
     * 计算时间段内的所有的天
     * type:0本期1上期2去年同期
     *
     * @param begin 起始日期
     * @param end   截止日期
     * @return the list
     */
    public static List<String> getLast(String begin, String end) {
        List<String> lDate = new ArrayList<>();
        Date date1;
        Date dEnd;
        try {
            date1 = Fields.PURE_DATETIME_FORMAT.parse(begin);
            dEnd = Fields.PURE_DATETIME_FORMAT.parse(end);
            Calendar calBegin = Calendar.getInstance();
            calBegin.setTime(date1);
            Calendar calEnd = Calendar.getInstance();
            calEnd.setTime(dEnd);
            lDate.add(Fields.PURE_DATETIME_FORMAT.format(calBegin.getTime()));
            while (calBegin.compareTo(calEnd) < 0) {
                calBegin.add(Calendar.DAY_OF_MONTH, 1);
                lDate.add(Fields.PURE_DATETIME_FORMAT.format(calBegin.getTime()));
            }
        } catch (ParseException e) {
            throw new InternalException(e);
        }
        return lDate;
    }

    /**
     * (周)计算(周) 上期和去年同期的起止日期和起止周 计算上期的起止时间 和去年同期 type 0本期 1上期 2去年同期 起始日期key
     * beginkey endkey 起始日期的起止周key beginWkey endWkey 本期：begin end 本期起止周
     * beginW、endW
     *
     * @param type      计算上期
     * @param beginkey  开始时间key
     * @param endkey    截止时间key
     * @param beginWkey 开始周key
     * @param endWkey   截止周key
     * @param begin     开始时间
     * @param end       截止时间
     * @param beginW    开始周
     * @param endW      截止周
     * @return the map
     */
    public static Map<String, String> getLast(int type, String beginkey,
                                              String endkey, String beginWkey, String endWkey, String begin,
                                              String end, String beginW, String endW) {
        Map<String, String> map = new HashMap<>();
        try {
            Date date1 = Fields.PURE_DATETIME_FORMAT.parse(begin);
            Date dEnd = Fields.PURE_DATETIME_FORMAT.parse(end);
            Calendar calBegin = Calendar.getInstance();

            calBegin.setTime(date1);
            Calendar calEnd = Calendar.getInstance();
            calEnd.setTime(dEnd);
            calBegin.setFirstDayOfWeek(Calendar.MONDAY);
            calEnd.setFirstDayOfWeek(Calendar.MONDAY);
            if (type == 1) {
                int week = getInterval(date1, dEnd);
                calBegin.add(Calendar.WEEK_OF_YEAR, -week);
                calEnd.add(Calendar.WEEK_OF_YEAR, -week);
                map.put(beginWkey,
                        String.valueOf(calBegin.get(Calendar.WEEK_OF_YEAR)));
                map.put(endWkey,
                        String.valueOf(calEnd.get(Calendar.WEEK_OF_YEAR)));
                int day_of_week = calBegin.get(Calendar.DAY_OF_WEEK) - 1;
                if (day_of_week == 0)
                    day_of_week = 7;
                calBegin.add(Calendar.DATE, -day_of_week + 1);
                int day_of_week_end = calEnd.get(Calendar.DAY_OF_WEEK) - 1;
                if (day_of_week_end == 0)
                    day_of_week_end = 7;
                calEnd.add(Calendar.DATE, -day_of_week_end + 7);
            } else if (type == 2) {
                calBegin.add(Calendar.YEAR, -1);
                calEnd.add(Calendar.YEAR, -1);

                calBegin.set(Calendar.WEEK_OF_YEAR, Integer.parseInt(beginW));
                calEnd.set(Calendar.WEEK_OF_YEAR, Integer.parseInt(endW));
                map.put(beginWkey, beginW);
                map.put(endWkey, endW);

                int day_of_week = calBegin.get(Calendar.DAY_OF_WEEK) - 1;
                if (day_of_week == 0)
                    day_of_week = 7;
                calBegin.add(Calendar.DATE, -day_of_week + 1);

                int day_of_week_end = calEnd.get(Calendar.DAY_OF_WEEK) - 1;
                if (day_of_week_end == 0)
                    day_of_week_end = 7;
                calEnd.add(Calendar.DATE, -day_of_week_end + 7);
            }
            map.put(beginkey, Fields.PURE_DATETIME_FORMAT.format(calBegin.getTime()));
            map.put(endkey, Fields.PURE_DATETIME_FORMAT.format(calEnd.getTime()));
        } catch (ParseException e) {
            throw new InternalException(e);
        }
        return map;
    }

    /**
     * (年)计算本期(年)的上期
     *
     * @param beginkey 开始时间key
     * @param endkey   截止时间key
     * @param begin    开始时间
     * @param end      截止时间
     * @return the map
     */
    public static Map<String, String> getLast(String beginkey,
                                              String endkey,
                                              String begin,
                                              String end) {
        Map<String, String> map = new HashMap<>();
        try {
            Calendar calBegin = Calendar.getInstance();
            calBegin.setTime(Fields.NORM_YEAR_FORMAT.parse(begin));
            Calendar calEnd = Calendar.getInstance();
            calEnd.setTime(Fields.NORM_YEAR_FORMAT.parse(end));
            int year = calBegin.get(Calendar.YEAR);
            int year1 = calEnd.get(Calendar.YEAR);
            int result;
            result = year1 - year + 1;
            calBegin.add(Calendar.YEAR, -result);
            calEnd.add(Calendar.YEAR, -result);
            map.put(beginkey, Fields.NORM_YEAR_FORMAT.format(calBegin.getTime()));
            map.put(endkey, Fields.NORM_YEAR_FORMAT.format(calEnd.getTime()));
        } catch (ParseException e) {
            throw new InternalException(e);
        }
        return map;
    }

    /**
     * 获取两个日期段相差的周数
     *
     * @param start 日期
     * @param end   日期
     * @return the int
     */
    public static int getInterval(Date start, Date end) {
        Calendar c_begin = Calendar.getInstance();
        c_begin.setTime(start);
        Calendar c_end = Calendar.getInstance();
        c_end.setTime(end);

        int count = 0;
        c_begin.setFirstDayOfWeek(Calendar.SUNDAY);
        c_end.setFirstDayOfWeek(Calendar.SUNDAY);
        while (c_begin.before(c_end)) {
            if (c_begin.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
                count++;
            }
            c_begin.add(Calendar.DAY_OF_YEAR, 1);
        }
        return count;
    }

    /**
     * 计算指定指定时间区间内的周数
     *
     * @param start 开始时间
     * @param end   结束时间
     * @return 周数
     */
    public static int getWeeks(Date start, Date end) {
        final Calendar startCalendar = Calendar.getInstance();
        startCalendar.setTime(start);
        final Calendar endCalendar = Calendar.getInstance();
        endCalendar.setTime(end);

        final int startWeekofYear = startCalendar.get(Calendar.WEEK_OF_YEAR);
        final int endWeekofYear = endCalendar.get(Calendar.WEEK_OF_YEAR);

        int count = endWeekofYear - startWeekofYear + 1;

        if (Calendar.SATURDAY != startCalendar.get(Calendar.DAY_OF_WEEK)) {
            count--;
        }

        return count;
    }

    /**
     * 修改年
     *
     * @param date     Date
     * @param newValue 新值
     * @return Date
     */
    public static Date withYear(Date date, long newValue) {
        return with(date, ChronoField.YEAR, newValue);
    }

    /**
     * 修改年
     *
     * @param localDateTime LocalDateTime
     * @param newValue      新值
     * @return LocalDateTime
     */
    public static LocalDateTime withYear(LocalDateTime localDateTime, long newValue) {
        return localDateTime.withYear((int) newValue);
    }

    /**
     * 修改年
     *
     * @param localDate LocalDate
     * @param newValue  新值
     * @return LocalDate
     */
    public static LocalDate withYear(LocalDate localDate, long newValue) {
        return localDate.withYear((int) newValue);
    }

    /**
     * 原有时间基础上,加上/减去(负数)N年
     *
     * @param date     日期
     * @param newValue 年份
     * @return 操作后的时间
     */
    public static Date withYear(Date date, int newValue) {
        return with(date, Calendar.YEAR, newValue);
    }

    /**
     * 修改月
     *
     * @param localDateTime LocalDateTime
     * @param newValue      新值
     * @return LocalDateTime
     */
    public static LocalDateTime withMonth(LocalDateTime localDateTime, long newValue) {
        return localDateTime.withMonth((int) newValue);
    }

    /**
     * 修改月
     *
     * @param localDate LocalDate
     * @param newValue  新值
     * @return LocalDate
     */
    public static LocalDate withMonth(LocalDate localDate, long newValue) {
        return localDate.withMonth((int) newValue);
    }

    /**
     * 原有时间基础上,加上/减去(负数)N月
     *
     * @param date     日期
     * @param newValue 月份
     * @return 操作后的时间
     */
    public static Date withMonth(Date date, int newValue) {
        return with(date, Calendar.MONTH, newValue);
    }

    /**
     * 原有时间基础上,加上/减去(负数)N周
     *
     * @param date     日期
     * @param newValue 周
     * @return 操作后的时间
     */
    public static Date withWeek(Date date, int newValue) {
        return with(date, Calendar.WEEK_OF_YEAR, newValue);
    }

    /**
     * 原有时间基础上,加上/减去(负数)N天
     *
     * @param date     日期
     * @param newValue 天
     * @return 操作后的时间
     */
    public static Date withDay(Date date, int newValue) {
        return with(date, Calendar.DAY_OF_MONTH, newValue);
    }

    /**
     * 修改小时
     *
     * @param date     Date
     * @param newValue 新值
     * @return Date
     */
    public static Date withHour(Date date, long newValue) {
        return with(date, ChronoField.HOUR_OF_DAY, newValue);
    }

    /**
     * 修改小时
     *
     * @param localDateTime LocalDateTime
     * @param newValue      新值
     * @return LocalDateTime
     */
    public static LocalDateTime withHour(LocalDateTime localDateTime, long newValue) {
        return localDateTime.withHour((int) newValue);
    }

    /**
     * 修改小时
     *
     * @param localTime LocalTime
     * @param newValue  新值
     * @return LocalTime
     */
    public static LocalTime withHour(LocalTime localTime, long newValue) {
        return localTime.withHour((int) newValue);
    }

    /**
     * 原有时间基础上,加上/减去(负数)N小时
     *
     * @param date     日期
     * @param newValue 小时
     * @return 操作后的时间
     */
    public static Date withHour(Date date, int newValue) {
        return with(date, Calendar.HOUR_OF_DAY, newValue);
    }

    /**
     * 修改分钟
     *
     * @param date     Date
     * @param newValue 新值
     * @return Date
     */
    public static Date withMinute(Date date, long newValue) {
        return with(date, ChronoField.MINUTE_OF_HOUR, newValue);
    }

    /**
     * 修改分钟
     *
     * @param localDateTime LocalDateTime
     * @param newValue      新值
     * @return LocalDateTime
     */
    public static LocalDateTime withMinute(LocalDateTime localDateTime, long newValue) {
        return localDateTime.withMinute((int) newValue);
    }

    /**
     * 修改分钟
     *
     * @param localTime LocalTime
     * @param newValue  新值
     * @return LocalTime
     */
    public static LocalTime withMinute(LocalTime localTime, long newValue) {
        return localTime.withMinute((int) newValue);
    }


    /**
     * 原有时间基础上,加上/减去(负数)N分钟
     *
     * @param date     日期
     * @param newValue 分钟
     * @return 操作后的时间
     */
    public static Date withMinute(Date date, int newValue) {
        return with(date, Calendar.MINUTE, newValue);
    }

    /**
     * 修改秒
     *
     * @param date     Date
     * @param newValue 新值
     * @return Date
     */
    public static Date withSecond(Date date, long newValue) {
        return with(date, ChronoField.SECOND_OF_MINUTE, newValue);
    }

    /**
     * 修改秒
     *
     * @param localDateTime LocalDateTime
     * @param newValue      新值
     * @return LocalDateTime
     */
    public static LocalDateTime withSecond(LocalDateTime localDateTime, long newValue) {
        return localDateTime.withSecond((int) newValue);
    }

    /**
     * 修改秒
     *
     * @param localTime LocalTime
     * @param newValue  新值
     * @return LocalTime
     */
    public static LocalTime withSecond(LocalTime localTime, long newValue) {
        return localTime.withSecond((int) newValue);
    }

    /**
     * 原有时间基础上,加上/减去(负数)N秒
     *
     * @param date     日期
     * @param newValue 秒
     * @return 操作后的时间
     */
    public static Date withSecond(Date date, int newValue) {
        return with(date, Calendar.SECOND, newValue);
    }

    /**
     * 修改毫秒
     *
     * @param date     Date
     * @param newValue 新值
     * @return Date
     */
    public static Date withMilli(Date date, long newValue) {
        return with(date, ChronoField.MILLI_OF_SECOND, newValue);
    }

    /**
     * 修改毫秒
     *
     * @param localDateTime LocalDateTime
     * @param newValue      新值
     * @return LocalDateTime
     */
    public static LocalDateTime withMilli(LocalDateTime localDateTime, long newValue) {
        return (LocalDateTime) with(localDateTime, ChronoField.MILLI_OF_SECOND, newValue);
    }

    /**
     * 修改毫秒
     *
     * @param localTime LocalTime
     * @param newValue  新值
     * @return LocalTime
     */
    public static LocalTime withMilli(LocalTime localTime, long newValue) {
        return (LocalTime) with(localTime, ChronoField.MILLI_OF_SECOND, newValue);
    }

    /**
     * 原有时间基础上,加上/减去(负数)N毫秒
     *
     * @param date     日期
     * @param newValue 毫秒
     * @return 操作后的时间
     */
    public static Date withMilli(Date date, int newValue) {
        return with(date, Calendar.MILLISECOND, newValue);
    }

    /**
     * 修改天
     *
     * @param date     Date
     * @param newValue 新值
     * @return Date
     */
    public static Date withDayOfMonth(Date date, long newValue) {
        return with(date, ChronoField.DAY_OF_MONTH, newValue);
    }

    /**
     * 修改天
     *
     * @param localDateTime LocalDateTime
     * @param newValue      新值
     * @return LocalDateTime
     */
    public static LocalDateTime withDayOfMonth(LocalDateTime localDateTime, long newValue) {
        return localDateTime.withDayOfMonth((int) newValue);
    }

    /**
     * 修改天
     *
     * @param localDate LocalDate
     * @param newValue  新值
     * @return LocalDate
     */
    public static LocalDate withDayOfMonth(LocalDate localDate, long newValue) {
        return localDate.withDayOfMonth((int) newValue);
    }

    /**
     * 修改一年中的天
     *
     * @param date     Date
     * @param newValue 新值
     * @return Date
     */
    public static Date withDayOfYear(Date date, long newValue) {
        return with(date, ChronoField.DAY_OF_YEAR, newValue);
    }

    /**
     * 修改一年中的天
     *
     * @param localDateTime LocalDateTime
     * @param newValue      新值
     * @return LocalDateTime
     */
    public static LocalDateTime withDayOfYear(LocalDateTime localDateTime, long newValue) {
        return localDateTime.withDayOfYear((int) newValue);
    }

    /**
     * 修改一年中的天
     *
     * @param localDate LocalDate
     * @param newValue  新值
     * @return LocalDate
     */
    public static LocalDate withDayOfYear(LocalDate localDate, long newValue) {
        return localDate.withDayOfYear((int) newValue);
    }

    /**
     * 修改星期
     *
     * @param date     Date
     * @param newValue 新值 1-7
     * @return Date
     */
    public static Date withDayOfWeek(Date date, long newValue) {
        return with(date, ChronoField.DAY_OF_WEEK, newValue);
    }

    /**
     * 修改星期
     *
     * @param localDateTime LocalDateTime
     * @param newValue      新值 1-7
     * @return LocalDateTime
     */
    public static LocalDateTime withDayOfWeek(LocalDateTime localDateTime, long newValue) {
        return (LocalDateTime) with(localDateTime, ChronoField.DAY_OF_WEEK, newValue);
    }

    /**
     * 修改星期
     *
     * @param localDate LocalDate
     * @param newValue  新值 1-7
     * @return LocalDateTime
     */
    public static LocalDate withDayOfWeek(LocalDate localDate, long newValue) {
        return (LocalDate) with(localDate, ChronoField.DAY_OF_WEEK, newValue);
    }

    /**
     * 获取2个日期的相差年月天的年数
     * 比如2020-02-29 2021-03-07，返回1
     *
     * @param startInclusive 开始时间
     * @param endExclusive   结束时间
     * @return long
     */
    public static long betweenYears(LocalDateTime startInclusive, LocalDateTime endExclusive) {
        return Period.between(toLocalDate(startInclusive),
                toLocalDate(endExclusive)).getYears();
    }

    /**
     * 获取2个日期的相差年月天的年数
     * 比如2020-02-29 2021-03-07，返回1
     *
     * @param startInclusive 开始时间
     * @param endExclusive   结束时间
     * @return long
     */
    public static long betweenYears(Date startInclusive, Date endExclusive) {
        return Period.between(toLocalDate(startInclusive),
                toLocalDate(endExclusive)).getYears();
    }

    /**
     * 获取2个日期的相差年月天的年数
     * 比如2020-02-29 2021-03-07，返回1
     *
     * @param startInclusive 开始时间
     * @param endExclusive   结束时间
     * @return long
     */
    public static long betweenYears(LocalDate startInclusive, LocalDate endExclusive) {
        return Period.between(startInclusive, endExclusive).getYears();
    }

    /**
     * 获取2个日期的相差年月天的月数
     * 比如2020-02-29 2021-03-07，返回0
     *
     * @param startInclusive 开始时间
     * @param endExclusive   结束时间
     * @return long
     */
    public static long betweenMonths(LocalDateTime startInclusive, LocalDateTime endExclusive) {
        return Period.between(toLocalDate(startInclusive),
                toLocalDate(endExclusive)).getMonths();
    }

    /**
     * 获取2个日期的相差年月天的月数
     * 比如2020-02-29 2021-03-07，返回0
     *
     * @param startInclusive 开始时间
     * @param endExclusive   结束时间
     * @return long
     */
    public static long betweenMonths(Date startInclusive, Date endExclusive) {
        return Period.between(toLocalDate(startInclusive),
                toLocalDate(endExclusive)).getMonths();
    }

    /**
     * 获取2个日期的相差年月天的月数
     * 比如2020-02-29 2021-03-07，返回0
     *
     * @param startInclusive 开始时间
     * @param endExclusive   结束时间
     * @return long
     */
    public static long betweenMonths(LocalDate startInclusive, LocalDate endExclusive) {
        return Period.between(startInclusive, endExclusive).getMonths();
    }

    /**
     * 获取2个日期的相差年月天的天数
     * 比如2020-02-29 2021-03-07，返回7
     *
     * @param startInclusive 开始时间
     * @param endExclusive   结束时间
     * @return long
     */
    public static long betweenDays(LocalDateTime startInclusive, LocalDateTime endExclusive) {
        return Period.between(toLocalDate(startInclusive),
                toLocalDate(endExclusive)).getDays();
    }

    /**
     * 获取2个日期的相差年月天的天数
     * 比如2020-02-29 2021-03-06，返回6
     *
     * @param startInclusive 开始时间
     * @param endExclusive   结束时间
     * @return long
     */
    public static long betweenDays(Date startInclusive, Date endExclusive) {
        return Period.between(toLocalDate(startInclusive),
                toLocalDate(endExclusive)).getDays();
    }

    /**
     * 获取2个日期的相差年月天的天数
     * 比如2020-02-29 2021-03-06，返回6
     *
     * @param startInclusive 开始时间
     * @param endExclusive   结束时间
     * @return long
     */
    public static long betweenDays(LocalDate startInclusive, LocalDate endExclusive) {
        return Period.between(startInclusive, endExclusive).getDays();
    }

    /**
     * 获取2个日期的相差总天数
     *
     * @param startInclusive 开始时间
     * @param endExclusive   结束时间
     * @return long
     */
    public static long betweenTotalDays(LocalDateTime startInclusive, LocalDateTime endExclusive) {
        return Duration.between(startInclusive, endExclusive).toDays();
    }

    /**
     * 获取2个日期的相差总天数
     *
     * @param startInclusive 开始时间
     * @param endExclusive   结束时间
     * @return long
     */
    public static long betweenTotalDays(Date startInclusive, Date endExclusive) {
        return durationBetween(toLocalDateTime(startInclusive), toLocalDateTime(endExclusive)).toDays();
    }

    /**
     * 获取2个日期的相差总小时数
     *
     * @param startInclusive 开始时间
     * @param endExclusive   结束时间
     * @return long
     */
    public static long betweenTotalHours(LocalDateTime startInclusive, LocalDateTime endExclusive) {
        return Duration.between(startInclusive, endExclusive).toHours();
    }

    /**
     * 获取2个日期的相差总小时数
     *
     * @param startInclusive 开始时间
     * @param endExclusive   结束时间
     * @return long
     */
    public static long betweenTotalHours(LocalTime startInclusive, LocalTime endExclusive) {
        return Duration.between(startInclusive, endExclusive).toHours();
    }

    /**
     * 获取2个日期的相差总小时数
     *
     * @param startInclusive 开始时间
     * @param endExclusive   结束时间
     * @return long
     */
    public static long betweenTotalHours(Date startInclusive, Date endExclusive) {
        return durationBetween(toLocalDateTime(startInclusive), toLocalDateTime(endExclusive)).toHours();
    }

    /**
     * 获取2个日期的相差总分钟数
     *
     * @param startInclusive 开始时间
     * @param endExclusive   结束时间
     * @return long
     */
    public static long betweenTotalMinutes(LocalDateTime startInclusive, LocalDateTime endExclusive) {
        return Duration.between(startInclusive, endExclusive).toMinutes();
    }

    /**
     * 获取2个日期的相差总分钟数
     *
     * @param startInclusive 开始时间
     * @param endExclusive   结束时间
     * @return long
     */
    public static long betweenTotalMinutes(LocalTime startInclusive, LocalTime endExclusive) {
        return Duration.between(startInclusive, endExclusive).toMinutes();
    }

    /**
     * 获取2个日期的相差总分钟数
     *
     * @param startInclusive 开始时间
     * @param endExclusive   结束时间
     * @return long
     */
    public static long betweenTotalMinutes(Date startInclusive, Date endExclusive) {
        return durationBetween(toLocalDateTime(startInclusive), toLocalDateTime(endExclusive)).toMinutes();
    }

    /**
     * 获取2个日期的相差总秒数
     *
     * @param startInclusive 开始时间
     * @param endExclusive   结束时间
     * @return long
     */
    public static long betweenTotalSeconds(LocalDateTime startInclusive, LocalDateTime endExclusive) {
        return Duration.between(startInclusive, endExclusive).getSeconds();
    }

    /**
     * 获取2个日期的相差总秒数
     *
     * @param startInclusive 开始时间
     * @param endExclusive   结束时间
     * @return long
     */
    public static long betweenTotalSeconds(LocalTime startInclusive, LocalTime endExclusive) {
        return Duration.between(startInclusive, endExclusive).getSeconds();
    }

    /**
     * 获取2个日期的相差总秒数
     *
     * @param startInclusive 开始时间
     * @param endExclusive   结束时间
     * @return long
     */
    public static long betweenTotalSeconds(Date startInclusive, Date endExclusive) {
        return durationBetween(toLocalDateTime(startInclusive), toLocalDateTime(endExclusive)).getSeconds();
    }

    /**
     * 获取2个日期的相差总毫秒数
     *
     * @param startInclusive 开始时间
     * @param endExclusive   结束时间
     * @return long
     */
    public static long betweenTotalMillis(LocalDateTime startInclusive, LocalDateTime endExclusive) {
        return Duration.between(startInclusive, endExclusive).toMillis();
    }

    /**
     * 获取2个日期的相差总毫秒数
     *
     * @param startInclusive 开始时间
     * @param endExclusive   结束时间
     * @return long
     */
    public static long betweenTotalMillis(LocalTime startInclusive, LocalTime endExclusive) {
        return Duration.between(startInclusive, endExclusive).toMillis();
    }

    /**
     * 获取2个日期的相差总毫秒数
     *
     * @param startInclusive 开始时间
     * @param endExclusive   结束时间
     * @return long
     */
    public static long betweenTotalMillis(Date startInclusive, Date endExclusive) {
        return durationBetween(toLocalDateTime(startInclusive), toLocalDateTime(endExclusive)).toMillis();
    }

    /**
     * 获取2个日期的相差总纳秒数
     *
     * @param startInclusive 开始时间
     * @param endExclusive   结束时间
     * @return long
     */
    public static long betweenTotalNanos(LocalDateTime startInclusive, LocalDateTime endExclusive) {
        return Duration.between(startInclusive, endExclusive).toNanos();
    }

    /**
     * 获取2个日期的相差总纳秒数
     *
     * @param startInclusive 开始时间
     * @param endExclusive   结束时间
     * @return long
     */
    public static long betweenTotalNanos(LocalTime startInclusive, LocalTime endExclusive) {
        return Duration.between(startInclusive, endExclusive).toNanos();
    }

    /**
     * 获取2个日期的相差总纳秒数
     *
     * @param startInclusive 开始时间
     * @param endExclusive   结束时间
     * @return long
     */
    public static long betweenTotalNanos(Date startInclusive, Date endExclusive) {
        return durationBetween(toLocalDateTime(startInclusive), toLocalDateTime(endExclusive)).toNanos();
    }

    /**
     * 获取星期值 1-7，星期一到星期日
     *
     * @param date Date
     * @return int
     */
    public static int getDayOfWeek(Date date) {
        return toLocalDateTime(date).getDayOfWeek().getValue();
    }

    /**
     * 获取星期值 1-7，星期一到星期日
     *
     * @param localDateTime LocalDateTime
     * @return int
     */
    public static int getDayOfWeek(LocalDateTime localDateTime) {
        return localDateTime.getDayOfWeek().getValue();
    }

    /**
     * 获取星期值 1-7，星期一到星期日
     *
     * @param localDate LocalDate
     * @return int
     */
    public static int getDayOfWeek(LocalDate localDate) {
        return localDate.getDayOfWeek().getValue();
    }

    /**
     * 获取星期值 1-7，星期一到星期日
     *
     * @param instant Instant
     * @return int
     */
    public static int getDayOfWeek(Instant instant) {
        return toLocalDateTime(instant).getDayOfWeek().getValue();
    }

    /**
     * 获取星期英文全称，比如Monday, Tuesday, Wednesday, Thursday, Friday, Saturday and Sunday
     *
     * @param date Date
     * @return String
     */
    public static String getDayOfWeekEnLong(Date date) {
        return Fields.Week.getEnNameByCode(getDayOfWeek(date));
    }

    /**
     * 获取星期英文全称，比如Monday, Tuesday, Wednesday, Thursday, Friday, Saturday and Sunday
     *
     * @param localDateTime LocalDateTime
     * @return String
     */
    public static String getDayOfWeekEnLong(LocalDateTime localDateTime) {
        return Fields.Week.getEnNameByCode(getDayOfWeek(localDateTime));
    }

    /**
     * 获取星期英文全称，比如Monday, Tuesday, Wednesday, Thursday, Friday, Saturday and Sunday
     *
     * @param localDate LocalDate
     * @return String
     */
    public static String getDayOfWeekEnLong(LocalDate localDate) {
        return Fields.Week.getEnNameByCode(getDayOfWeek(localDate));
    }

    /**
     * 获取星期英文全称，比如Monday, Tuesday, Wednesday, Thursday, Friday, Saturday and Sunday
     *
     * @param instant Instant
     * @return String
     */
    public static String getDayOfWeekEnLong(Instant instant) {
        return Fields.Week.getEnNameByCode(getDayOfWeek(instant));
    }

    /**
     * 获取星期英文简称，比如Mon
     *
     * @param date Date
     * @return String
     */
    public static String getDayOfWeekEnShort(Date date) {
        return Fields.Week.getShortNameByCode(getDayOfWeek(date));
    }

    /**
     * 获取星期英文简称，比如Mon
     *
     * @param localDateTime LocalDateTime
     * @return String
     */
    public static String getDayOfWeekEnShort(LocalDateTime localDateTime) {
        return Fields.Week.getShortNameByCode(getDayOfWeek(localDateTime));
    }

    /**
     * 获取星期英文简称，比如Mon
     *
     * @param localDate LocalDate
     * @return String
     */
    public static String getDayOfWeekEnShort(LocalDate localDate) {
        return Fields.Week.getShortNameByCode(getDayOfWeek(localDate));
    }

    /**
     * 获取星期英文简称，比如Mon
     *
     * @param instant Instant
     * @return String
     */
    public static String getDayOfWeekEnShort(Instant instant) {
        return Fields.Week.getShortNameByCode(getDayOfWeek(instant));
    }

    /**
     * 获取星期英文简称大写，比如MON
     *
     * @param date Date
     * @return String
     */
    public static String getDayOfWeekEnShortUpper(Date date) {
        return Fields.Week.getShortNameByCode(getDayOfWeek(date)).toUpperCase();
    }

    /**
     * 获取星期英文简称大写，比如MON
     *
     * @param localDateTime LocalDateTime
     * @return String
     */
    public static String getDayOfWeekEnShortUpper(LocalDateTime localDateTime) {
        return Fields.Week.getShortNameByCode(getDayOfWeek(localDateTime)).toUpperCase();
    }

    /**
     * 获取星期英文简称大写，比如MON
     *
     * @param localDate LocalDate
     * @return String
     */
    public static String getDayOfWeekEnShortUpper(LocalDate localDate) {
        return Fields.Week.getShortNameByCode(getDayOfWeek(localDate)).toUpperCase();
    }

    /**
     * 获取星期英文简称大写，比如MON
     *
     * @param instant Instant
     * @return String
     */
    public static String getDayOfWeekEnShortUpper(Instant instant) {
        return Fields.Week.getShortNameByCode(getDayOfWeek(instant)).toUpperCase();
    }

    /**
     * 获取星期中文，比如星期一
     *
     * @param date Date
     * @return String
     */
    public static String getDayOfWeekCn(Date date) {
        return Fields.Week.getCnNameByCode(getDayOfWeek(date));
    }

    /**
     * 获取星期中文，比如星期一
     *
     * @param localDateTime LocalDateTime
     * @return String
     */
    public static String getDayOfWeekCn(LocalDateTime localDateTime) {
        return Fields.Week.getCnNameByCode(getDayOfWeek(localDateTime));
    }

    /**
     * 获取星期中文，比如星期一
     *
     * @param localDate LocalDate
     * @return String
     */
    public static String getDayOfWeekCn(LocalDate localDate) {
        return Fields.Week.getCnNameByCode(getDayOfWeek(localDate));
    }

    /**
     * 获取星期中文，比如星期一
     *
     * @param instant Instant
     * @return String
     */
    public static String getDayOfWeekCn(Instant instant) {
        return Fields.Week.getCnNameByCode(getDayOfWeek(instant));
    }

    /**
     * 获取星期中文简称，比如星期一为一
     *
     * @param date Date
     * @return String
     */
    public static String getDayOfWeekCnShort(Date date) {
        return Fields.Week.getCnNameByCode(getDayOfWeek(date)).substring(2);
    }

    /**
     * 获取星期中文简称，比如星期一为一
     *
     * @param localDateTime LocalDateTime
     * @return String
     */
    public static String getDayOfWeekCnShort(LocalDateTime localDateTime) {
        return Fields.Week.getCnNameByCode(getDayOfWeek(localDateTime)).substring(2);
    }

    /**
     * 获取星期中文简称，比如星期一为一
     *
     * @param localDate LocalDate
     * @return String
     */
    public static String getDayOfWeekCnShort(LocalDate localDate) {
        return Fields.Week.getCnNameByCode(getDayOfWeek(localDate)).substring(2);
    }

    /**
     * 获取星期中文简称，比如星期一为一
     *
     * @param instant Instant
     * @return String
     */
    public static String getDayOfWeekCnShort(Instant instant) {
        return Fields.Week.getCnNameByCode(getDayOfWeek(instant)).substring(2);
    }

    /**
     * 获取当前月的第一天
     *
     * @param localDate LocalDate
     * @return LocalDate
     */
    public static LocalDate firstDayOfMonth(LocalDate localDate) {
        return localDate.with(TemporalAdjusters.firstDayOfMonth());
    }

    /**
     * 获取当前月的第一天
     *
     * @param localDateTime LocalDateTime
     * @return LocalDateTime
     */
    public static LocalDateTime firstDayOfMonth(LocalDateTime localDateTime) {
        return localDateTime.with(TemporalAdjusters.firstDayOfMonth());
    }

    /**
     * 获取当前月的第一天
     *
     * @param date Date
     * @return Date
     */
    public static Date firstDayOfMonth(Date date) {
        return toDate(toLocalDateTime(date).with(TemporalAdjusters.firstDayOfMonth()));
    }

    /**
     * 获取当前月的最后一天
     *
     * @param localDate LocalDate
     * @return LocalDate
     */
    public static LocalDate lastDayOfMonth(LocalDate localDate) {
        return localDate.with(TemporalAdjusters.lastDayOfMonth());
    }

    /**
     * 获取当前月的最后一天
     *
     * @param localDateTime LocalDateTime
     * @return LocalDateTime
     */
    public static LocalDateTime lastDayOfMonth(LocalDateTime localDateTime) {
        return localDateTime.with(TemporalAdjusters.lastDayOfMonth());
    }

    /**
     * 获取当前月的最后一天
     *
     * @param date Date
     * @return Date
     */
    public static Date lastDayOfMonth(Date date) {
        return toDate(toLocalDateTime(date).with(TemporalAdjusters.lastDayOfMonth()));
    }


    /**
     * 获取秒级别的开始时间，即忽略毫秒部分
     *
     * @param date 日期
     * @return {@link DateTime}
     */
    public static DateTime beginOfSecond(Date date) {
        return new DateTime(beginOfSecond(toCalendar(date)));
    }

    /**
     * 获取秒级别的结束时间，即毫秒设置为999
     *
     * @param date 日期
     * @return {@link DateTime}
     */
    public static DateTime endOfSecond(Date date) {
        return new DateTime(endOfSecond(toCalendar(date)));
    }

    /**
     * 修改秒级别的开始时间，即忽略毫秒部分
     *
     * @param calendar 日期 {@link Calendar}
     * @return {@link Calendar}
     */
    public static Calendar beginOfSecond(Calendar calendar) {
        return truncate(calendar, Fields.Type.SECOND);
    }

    /**
     * 修改秒级别的结束时间，即毫秒设置为999
     *
     * @param calendar 日期 {@link Calendar}
     * @return {@link Calendar}
     */
    public static Calendar endOfSecond(Calendar calendar) {
        return ceiling(calendar, Fields.Type.SECOND);
    }

    /**
     * 获取某分钟的开始时间
     *
     * @param calendar 日期 {@link Calendar}
     * @return {@link Calendar}
     */
    public static Calendar beginOfMinute(Calendar calendar) {
        return truncate(calendar, Fields.Type.MINUTE);
    }

    /**
     * 获取某分钟的结束时间
     *
     * @param calendar 日期 {@link Calendar}
     * @return {@link Calendar}
     */
    public static Calendar endOfMinute(Calendar calendar) {
        return ceiling(calendar, Fields.Type.MINUTE);
    }

    /**
     * 获取某分钟的开始时间
     *
     * @param date 日期
     * @return {@link DateTime}
     */
    public static DateTime beginOfMinute(Date date) {
        return new DateTime(beginOfMinute(toCalendar(date)));
    }

    /**
     * 获取某分钟的结束时间
     *
     * @param date 日期
     * @return {@link DateTime}
     */
    public static DateTime endOfMinute(Date date) {
        return new DateTime(endOfMinute(toCalendar(date)));
    }

    /**
     * 修改某小时的开始时间
     *
     * @param calendar 日期 {@link Calendar}
     * @return {@link Calendar}
     */
    public static Calendar beginOfHour(Calendar calendar) {
        return truncate(calendar, Fields.Type.HOUR_OF_DAY);
    }

    /**
     * 修改某小时的结束时间
     *
     * @param calendar 日期 {@link Calendar}
     * @return {@link Calendar}
     */
    public static Calendar endOfHour(Calendar calendar) {
        return ceiling(calendar, Fields.Type.HOUR_OF_DAY);
    }

    /**
     * 获取某天的开始时间
     *
     * @param date 日期
     * @return {@link DateTime}
     */
    public static DateTime beginOfDay(Date date) {
        return new DateTime(beginOfDay(toCalendar(date)));
    }

    /**
     * 获取某天的结束时间
     *
     * @param date 日期
     * @return {@link DateTime}
     */
    public static DateTime endOfDay(Date date) {
        return new DateTime(endOfDay(toCalendar(date)));
    }

    /**
     * 获取某小时的开始时间
     *
     * @param date 日期
     * @return {@link DateTime}
     */
    public static DateTime beginOfHour(Date date) {
        return new DateTime(beginOfHour(toCalendar(date)));
    }

    /**
     * 获取某小时的结束时间
     *
     * @param date 日期
     * @return {@link DateTime}
     */
    public static DateTime endOfHour(Date date) {
        return new DateTime(endOfHour(toCalendar(date)));
    }

    /**
     * 修改某天的开始时间
     *
     * @param calendar 日期 {@link Calendar}
     * @return {@link Calendar}
     */
    public static Calendar beginOfDay(Calendar calendar) {
        return truncate(calendar, Fields.Type.DAY_OF_MONTH);
    }

    /**
     * 修改某天的结束时间
     *
     * @param calendar 日期 {@link Calendar}
     * @return {@link Calendar}
     */
    public static Calendar endOfDay(Calendar calendar) {
        return ceiling(calendar, Fields.Type.DAY_OF_MONTH);
    }

    /**
     * 获取某周的开始时间，周一定为一周的开始时间
     *
     * @param date 日期
     * @return {@link DateTime}
     */
    public static DateTime beginOfWeek(Date date) {
        return new DateTime(beginOfWeek(toCalendar(date)));
    }

    /**
     * 获取某周的结束时间
     *
     * @param date 日期
     * @return {@link DateTime}
     */
    public static DateTime endOfWeek(Date date) {
        return new DateTime(endOfWeek(toCalendar(date)));
    }

    /**
     * 修改给定日期当前周的开始时间，周一定为一周的开始时间
     *
     * @param calendar 日期 {@link Calendar}
     * @return {@link Calendar}
     */
    public static Calendar beginOfWeek(Calendar calendar) {
        return beginOfWeek(calendar, true);
    }

    /**
     * 修改给定日期当前周的开始时间
     *
     * @param calendar           日期 {@link Calendar}
     * @param isMondayAsFirstDay 是否周一做为一周的第一天(false表示周日做为第一天)
     * @return {@link Calendar}
     */
    public static Calendar beginOfWeek(Calendar calendar, boolean isMondayAsFirstDay) {
        calendar.setFirstDayOfWeek(isMondayAsFirstDay ? Calendar.MONDAY : Calendar.SUNDAY);
        // WEEK_OF_MONTH为上限的字段(不包括)，实际调整的为DAY_OF_MONTH
        return truncate(calendar, Fields.Type.WEEK_OF_MONTH);
    }

    /**
     * 修改某周的结束时间，周日定为一周的结束
     *
     * @param calendar 日期 {@link Calendar}
     * @return {@link Calendar}
     */
    public static Calendar endOfWeek(Calendar calendar) {
        return endOfWeek(calendar, true);
    }

    /**
     * 修改某周的结束时间
     *
     * @param calendar          日期 {@link Calendar}
     * @param isSundayAsLastDay 是否周日做为一周的最后一天(false表示周六做为最后一天)
     * @return {@link Calendar}
     */
    public static Calendar endOfWeek(Calendar calendar, boolean isSundayAsLastDay) {
        calendar.setFirstDayOfWeek(isSundayAsLastDay ? Calendar.MONDAY : Calendar.SUNDAY);
        return ceiling(calendar, Fields.Type.WEEK_OF_MONTH);
    }

    /**
     * 获取某月的开始时间
     *
     * @param date 日期
     * @return {@link DateTime}
     */
    public static DateTime beginOfMonth(Date date) {
        return new DateTime(beginOfMonth(toCalendar(date)));
    }

    /**
     * 获取某月的结束时间
     *
     * @param date 日期
     * @return {@link DateTime}
     */
    public static DateTime endOfMonth(Date date) {
        return new DateTime(endOfMonth(toCalendar(date)));
    }

    /**
     * 修改某月的开始时间
     *
     * @param calendar 日期 {@link Calendar}
     * @return {@link Calendar}
     */
    public static Calendar beginOfMonth(Calendar calendar) {
        return truncate(calendar, Fields.Type.MONTH);
    }

    /**
     * 修改某月的结束时间
     *
     * @param calendar 日期 {@link Calendar}
     * @return {@link Calendar}
     */
    public static Calendar endOfMonth(Calendar calendar) {
        return ceiling(calendar, Fields.Type.MONTH);
    }

    /**
     * 获取某季度的开始时间
     *
     * @param date 日期
     * @return {@link DateTime}
     */
    public static DateTime beginOfQuarter(Date date) {
        return new DateTime(beginOfQuarter(toCalendar(date)));
    }

    /**
     * 获取某季度的结束时间
     *
     * @param date 日期
     * @return {@link DateTime}
     */
    public static DateTime endOfQuarter(Date date) {
        return new DateTime(endOfQuarter(toCalendar(date)));
    }

    /**
     * 修改某季度的开始时间
     *
     * @param calendar 日期 {@link Calendar}
     * @return {@link Calendar}
     */
    public static Calendar beginOfQuarter(Calendar calendar) {
        calendar.set(Calendar.MONTH, calendar.get(Fields.Type.MONTH.getValue()) / 3 * 3);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        return beginOfDay(calendar);
    }

    /**
     * 修改某季度的结束时间
     *
     * @param calendar 日期 {@link Calendar}
     * @return {@link Calendar}
     */
    public static Calendar endOfQuarter(Calendar calendar) {
        calendar.set(Calendar.MONTH, calendar.get(Fields.Type.MONTH.getValue()) / 3 * 3 + 2);
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        return endOfDay(calendar);
    }

    /**
     * 获取某年的开始时间
     *
     * @param date 日期
     * @return {@link DateTime}
     */
    public static DateTime beginOfYear(Date date) {
        return new DateTime(beginOfYear(toCalendar(date)));
    }

    /**
     * 获取某年的结束时间
     *
     * @param date 日期
     * @return {@link DateTime}
     */
    public static DateTime endOfYear(Date date) {
        return new DateTime(endOfYear(toCalendar(date)));
    }

    /**
     * 修改某年的开始时间
     *
     * @param calendar 日期 {@link Calendar}
     * @return {@link Calendar}
     */
    public static Calendar beginOfYear(Calendar calendar) {
        return truncate(calendar, Fields.Type.YEAR);
    }

    /**
     * 修改某年的结束时间
     *
     * @param calendar 日期 {@link Calendar}
     * @return {@link Calendar}
     */
    public static Calendar endOfYear(Calendar calendar) {
        return ceiling(calendar, Fields.Type.YEAR);
    }

    /**
     * 判断是否闰年
     *
     * @param localDate LocalDate
     * @return boolean
     */
    public static boolean isLeapYear(LocalDate localDate) {
        return localDate.isLeapYear();
    }

    /**
     * 判断是否闰年
     *
     * @param localDateTime LocalDateTime
     * @return boolean
     */
    public static boolean isLeapYear(LocalDateTime localDateTime) {
        return localDateTime.toLocalDate().isLeapYear();
    }

    /**
     * 判断是否闰年
     *
     * @param date Date
     * @return boolean
     */
    public static boolean isLeapYear(Date date) {
        return toLocalDateTime(date).toLocalDate().isLeapYear();
    }

    /**
     * 判断是否闰年
     *
     * @param year 年
     * @return boolean
     */
    public static boolean isLeapYear(int year) {
        return Solar.isLeapYear(year);
    }

    /**
     * 下一个闰年
     *
     * @param year 年
     * @return int
     */
    public static int nextLeapYear(int year) {
        for (int i = 0; i < 8; i++) {
            year++;
            if (isLeapYear(year)) {
                return year;
            }
        }
        return -1;
    }

    /**
     * 下一个闰年
     *
     * @param localDateTime LocalDateTime
     * @return LocalDateTime
     */
    public static LocalDateTime nextLeapYear(LocalDateTime localDateTime) {
        return localDateTime.withYear(nextLeapYear(localDateTime.getYear()));
    }

    /**
     * 下一个闰年
     *
     * @param localDate LocalDate
     * @return LocalDate
     */
    public static LocalDate nextLeapYear(LocalDate localDate) {
        return localDate.withYear(nextLeapYear(localDate.getYear()));
    }

    /**
     * 下一个闰年
     *
     * @param date Date
     * @return Date
     */
    public static Date nextLeapYear(Date date) {
        return toDate(nextLeapYear(toLocalDateTime(date)));
    }

    /**
     * 判断是否工作日 （周一到周五）
     *
     * @param date Date
     * @return boolean
     */
    public static boolean isWorkDay(Date date) {
        int dayOfWeek = getDayOfWeek(date);
        return dayOfWeek != 6 && dayOfWeek != 7;
    }

    /**
     * 判断是否工作日 （周一到周五）
     *
     * @param localDateTime LocalDateTime
     * @return boolean
     */
    public static boolean isWorkDay(LocalDateTime localDateTime) {
        int dayOfWeek = getDayOfWeek(localDateTime);
        return dayOfWeek != 6 && dayOfWeek != 7;
    }

    /**
     * 判断是否工作日 （周一到周五）
     *
     * @param localDate LocalDate
     * @return boolean
     */
    public static boolean isWorkDay(LocalDate localDate) {
        int dayOfWeek = getDayOfWeek(localDate);
        return dayOfWeek != 6 && dayOfWeek != 7;
    }

    /**
     * 判断是否周末（周六周日）
     *
     * @param date Date
     * @return boolean
     */
    public static boolean isWeekend(Date date) {
        return !isWorkDay(date);
    }

    /**
     * 判断是否周末（周六周日）
     *
     * @param localDateTime LocalDateTime
     * @return boolean
     */
    public static boolean isWeekend(LocalDateTime localDateTime) {
        return !isWorkDay(localDateTime);
    }

    /**
     * 判断是否周末（周六周日）
     *
     * @param localDate LocalDate
     * @return boolean
     */
    public static boolean isWeekend(LocalDate localDate) {
        return !isWorkDay(localDate);
    }

    /**
     * 获取月的天数
     *
     * @param localDate LocalDate
     * @return int
     */
    public static int lengthOfMonth(LocalDate localDate) {
        return localDate.lengthOfMonth();
    }

    /**
     * 获取月的天数
     *
     * @param localDateTime LocalDateTime
     * @return int
     */
    public static int lengthOfMonth(LocalDateTime localDateTime) {
        return localDateTime.toLocalDate().lengthOfMonth();
    }

    /**
     * 获取月的天数
     *
     * @param date Date
     * @return int
     */
    public static int lengthOfMonth(Date date) {
        return toLocalDateTime(date).toLocalDate().lengthOfMonth();
    }

    /**
     * 获取年的天数
     *
     * @param localDate LocalDate
     * @return int
     */
    public static int lengthOfYear(LocalDate localDate) {
        return localDate.lengthOfYear();
    }

    /**
     * 获取年的天数
     *
     * @param localDateTime LocalDateTime
     * @return int
     */
    public static int lengthOfYear(LocalDateTime localDateTime) {
        return localDateTime.toLocalDate().lengthOfYear();
    }

    /**
     * 获取年的天数
     *
     * @param date Date
     * @return int
     */
    public static int lengthOfYear(Date date) {
        return toLocalDateTime(date).toLocalDate().lengthOfYear();
    }

    /**
     * 下一个星期几
     *
     * @param localDate 日期
     * @param dayOfWeek 星期
     * @return LocalDate
     */
    public static LocalDate next(LocalDate localDate, DayOfWeek dayOfWeek) {
        return localDate.with(TemporalAdjusters.next(dayOfWeek));
    }

    /**
     * 下一个星期几
     *
     * @param localDateTime 日期
     * @param dayOfWeek     星期
     * @return LocalDateTime
     */
    public static LocalDateTime next(LocalDateTime localDateTime, DayOfWeek dayOfWeek) {
        return localDateTime.with(TemporalAdjusters.next(dayOfWeek));
    }

    /**
     * 下一个星期几
     *
     * @param date      日期
     * @param dayOfWeek 星期
     * @return Date
     */
    public static Date next(Date date, DayOfWeek dayOfWeek) {
        return toDate(toLocalDateTime(date).with(TemporalAdjusters.next(dayOfWeek)));
    }


    /**
     * 上一个星期几
     *
     * @param localDate 日期
     * @param dayOfWeek 星期
     * @return LocalDate
     */
    public static LocalDate previous(LocalDate localDate, DayOfWeek dayOfWeek) {
        return localDate.with(TemporalAdjusters.previous(dayOfWeek));
    }

    /**
     * 上一个星期几
     *
     * @param localDateTime 日期
     * @param dayOfWeek     星期
     * @return LocalDateTime
     */
    public static LocalDateTime previous(LocalDateTime localDateTime, DayOfWeek dayOfWeek) {
        return localDateTime.with(TemporalAdjusters.previous(dayOfWeek));
    }

    /**
     * 上一个星期几
     *
     * @param date      日期
     * @param dayOfWeek 星期
     * @return Date
     */
    public static Date previous(Date date, DayOfWeek dayOfWeek) {
        return toDate(toLocalDateTime(date).with(TemporalAdjusters.previous(dayOfWeek)));
    }

    /**
     * 获下一个工作日
     *
     * @param localDate LocalDate
     * @return LocalDate
     */
    public static LocalDate nextWorkDay(LocalDate localDate) {
        return localDate.with(nextWorkDay());
    }

    /**
     * 获下一个工作日
     *
     * @param localDateTime LocalDateTime
     * @return LocalDateTime
     */
    public static LocalDateTime nextWorkDay(LocalDateTime localDateTime) {
        return localDateTime.with(nextWorkDay());
    }

    /**
     * 获下一个工作日
     *
     * @param date Date
     * @return Date
     */
    public static Date nextWorkDay(Date date) {
        return toDate(toLocalDateTime(date).with(nextWorkDay()));
    }

    /**
     * 下一个工作日
     *
     * @return TemporalAdjuster
     */
    public static TemporalAdjuster nextWorkDay() {
        return (temporal) -> {
            DayOfWeek dayOfWeek = DayOfWeek.of(temporal.get(ChronoField.DAY_OF_WEEK));
            int add = 1;
            if (dayOfWeek == DayOfWeek.FRIDAY) {
                add = 3;
            }
            if (dayOfWeek == DayOfWeek.SATURDAY) {
                add = 2;
            }
            return temporal.plus(add, ChronoUnit.DAYS);
        };
    }

    /**
     * 获取当前系统当前时区时间
     *
     * @return ZonedDateTime
     */
    public static ZonedDateTime getZonedDateTimeNowOfDefault() {
        return ZonedDateTime.now(java.time.ZoneId.systemDefault());
    }

    /**
     * 获取当前上海时区时间（北京时间）
     *
     * @return ZonedDateTime
     */
    public static ZonedDateTime getZonedDateTimeNowOfCTT() {
        return ZonedDateTime.now(ZoneId.of(org.miaixz.bus.core.lang.ZoneId.CTT.getEnName()));
    }

    /**
     * 获取当前巴黎时区时间
     *
     * @return ZonedDateTime
     */
    public static ZonedDateTime getZonedDateTimeNowOfECT() {
        return ZonedDateTime.now(ZoneId.of(org.miaixz.bus.core.lang.ZoneId.ECT.getEnName()));
    }

    /**
     * 获取当前美国东部标准时区
     *
     * @return ZonedDateTime
     */
    public static ZonedDateTime getZonedDateTimeNowOfEST() {
        return ZonedDateTime.now(ZoneId.of(org.miaixz.bus.core.lang.ZoneId.EST.getEnName()));
    }

    /**
     * 获取当前东京时区时间
     *
     * @return ZonedDateTime
     */
    public static ZonedDateTime getZonedDateTimeNowOfJST() {
        return ZonedDateTime.now(ZoneId.of(org.miaixz.bus.core.lang.ZoneId.JST.getEnName()));
    }

    /**
     * 根据field修改属性
     *
     * @param temporal Temporal
     * @param field    属性
     * @param newValue 新值
     * @return Temporal
     */
    public static Temporal with(Temporal temporal, TemporalField field, long newValue) {
        return temporal.with(field, newValue);
    }

    /**
     * 根据field修改属性
     *
     * @param date     日期
     * @param field    属性
     * @param newValue 新值
     * @return Date
     */
    public static Date with(Date date, TemporalField field, long newValue) {
        return toDate(toLocalDateTime(date).with(field, newValue));
    }

    /**
     * 返回添加指定规则后的日期
     *
     * @param date   日期
     * @param field  规则
     * @param amount 数量
     * @return 操作后的日期 {@code Date}
     */
    private static Date with(final Date date, final int field, final int amount) {
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(field, amount);
        return calendar.getTime();
    }

    /**
     * 获取2个日期的总的天时分秒毫秒纳秒
     *
     * @param startInclusive 开始时间
     * @param endExclusive   结束时间
     * @return Duration
     */
    public static Duration durationBetween(Temporal startInclusive, Temporal endExclusive) {
        return Duration.between(startInclusive, endExclusive);
    }

    /**
     * 获取2个日期的相差年月日部分属性
     *
     * @param startDateInclusive 开始时间
     * @param endDateExclusive   结束时间
     * @return Period
     */
    public static Period periodBetween(LocalDate startDateInclusive, LocalDate endDateExclusive) {
        return Period.between(startDateInclusive, endDateExclusive);
    }

    /**
     * 获取时区当前时间
     *
     * @param zoneId 时区
     * @return ZonedDateTime
     */
    public static ZonedDateTime getZonedDateTimeNow(String zoneId) {
        return ZonedDateTime.now(java.time.ZoneId.of(zoneId));
    }

    /**
     * 时区转换计算
     *
     * @param zonedDateTime ZonedDateTime
     * @param zoneId        例如 Asia/Shanghai
     * @return ZonedDateTime
     */
    public static ZonedDateTime transform(ZonedDateTime zonedDateTime, String zoneId) {
        return transform(zonedDateTime, java.time.ZoneId.of(zoneId));
    }

    /**
     * 时区转换计算
     *
     * @param zonedDateTime ZonedDateTime
     * @param zone          时区
     * @return ZonedDateTime
     */
    public static ZonedDateTime transform(ZonedDateTime zonedDateTime, java.time.ZoneId zone) {
        return zonedDateTime.withZoneSameInstant(zone);
    }

    /**
     * 时区转换计算
     *
     * @param date   Date
     * @param zoneId 目标时区
     * @return 日期 yyyy-MM-dd HH:mm:ss
     */
    public static String transform(Date date, String zoneId) {
        return transform(date, java.time.ZoneId.of(zoneId));
    }

    /**
     * 时区转换计算
     *
     * @param date Date
     * @param zone 目标时区
     * @return 日期 yyyy-MM-dd HH:mm:ss
     */
    public static String transform(Date date, java.time.ZoneId zone) {
        return format(date, zone.toString());
    }

    /**
     * 比较2个时间Date
     *
     * @param date1 时间1
     * @param date2 时间2
     * @return int date1 大于 date2 返回1， date1 小于 date2 返回-1，date1 等于date2 返回0
     */
    public static int compare(Date date1, Date date2) {
        return compare(toLocalDateTime(date1), toLocalDateTime(date2));
    }

    /**
     * 比较2个时间,可用于LocalDateTime,LocalDate,LocalTime,Instant
     *
     * @param temporal1 时间1
     * @param temporal2 时间2
     * @return int temporal1 大于 temporal2 返回1， temporal1 小于 temporal2 返回-1，temporal1 等于temporal2 返回0
     */
    public static int compare(Temporal temporal1, Temporal temporal2) {
        if (temporal1 instanceof LocalDateTime && temporal2 instanceof LocalDateTime) {
            LocalDateTime localDateTimeA = (LocalDateTime) temporal1;
            LocalDateTime localDateTimeB = (LocalDateTime) temporal2;
            return localDateTimeA.compareTo(localDateTimeB);
        } else if (temporal1 instanceof LocalDate && temporal2 instanceof LocalDate) {
            LocalDate localDateA = (LocalDate) temporal1;
            LocalDate localDateB = (LocalDate) temporal2;
            return localDateA.compareTo(localDateB);
        } else if (temporal1 instanceof LocalTime && temporal2 instanceof LocalTime) {
            LocalTime localTimeA = (LocalTime) temporal1;
            LocalTime localTimeB = (LocalTime) temporal2;
            return localTimeA.compareTo(localTimeB);
        } else if (temporal1 instanceof Instant && temporal2 instanceof Instant) {
            Instant instantA = (Instant) temporal1;
            Instant instantB = (Instant) temporal2;
            return instantA.compareTo(instantB);
        }

        throw new DateTimeException("Unsupported Temporal, must be LocalDateTime,LocalDate,LocalTime,Instant");
    }

    /**
     * 两个时间比较
     *
     * @param date 日期
     * @return 时间差
     */
    public static int compare(Date date) {
        return date.compareTo(new Date());
    }

    /**
     * 两个时间比较(时间戳比较)
     *
     * @param date 日期
     * @return 时间差
     */
    public static int compare(long date) {
        long now = timestamp();
        return Long.compare(date, now);
    }

    /**
     * 两个时间比较(时间戳比较)
     *
     * @param object 字符串日期
     * @return the boolean
     */
    public static boolean compare(String object) {
        long expired = timestamp() - (Long.parseLong(object) * 1000);
        return expired <= 900000 && expired >= -900000;
    }

    /**
     * 一天开始时间 00:00:00
     *
     * @return LocalTime
     */
    public static LocalTime startTimeOfDay() {
        return LocalTime.MIN;
    }

    /**
     * 一天开始时间 23:59:59
     *
     * @return LocalTime
     */
    public static LocalTime endTimeOfDay() {
        return LocalTime.of(23, 59, 59);
    }

    /**
     * 一天结束时间 精确时间到纳秒 23:59:59.999999999
     *
     * @return LocalTime
     */
    public static LocalTime endAccuracyTimeOfDay() {
        return LocalTime.MAX;
    }

    /**
     * 昨天起始时间 即：昨天日期+00:00:00
     *
     * @return Date
     */
    public static Date startTimeOfYesterday() {
        return toDate(LocalDate.now().minusDays(1).atTime(startTimeOfDay()));
    }

    /**
     * 昨天结束时间即：昨天日期+23:59:59
     *
     * @return Date
     */
    public static Date endTimeOfYesterday() {
        return toDate(LocalDate.now().minusDays(1).atTime(endTimeOfDay()));
    }

    /**
     * 明天起始时间 即：明天日期+00:00:00
     *
     * @return Date
     */
    public static Date startTimeOfTomorrow() {
        return toDate(LocalDate.now().plusDays(1).atTime(startTimeOfDay()));
    }

    /**
     * 明天结束时间即：明天日期+23:59:59
     *
     * @return Date
     */
    public static Date endTimeOfTomorrow() {
        return toDate(LocalDate.now().plusDays(1).atTime(endTimeOfDay()));
    }

    /**
     * 今天起始时间 即：今天日期+00:00:00
     *
     * @return Date
     */
    public static Date startTimeOfToday() {
        return toDate(LocalDate.now().atTime(startTimeOfDay()));
    }

    /**
     * 今天结束时间即：今天日期+23:59:59
     *
     * @return Date
     */
    public static Date endTimeOfToday() {
        return toDate(LocalDate.now().atTime(endTimeOfDay()));
    }

    /**
     * 上个月第一天起始时间 即：上个月第一天日期+00:00:00
     *
     * @return Date
     */
    public static Date startTimeOfLastMonth() {
        return toDate(firstDayOfMonth(LocalDate.now().minusMonths(1)).atTime(startTimeOfDay()));
    }

    /**
     * 上个月最后一天结束时间 即：上个月最后一天日期+23:59:59
     *
     * @return Date
     */
    public static Date endTimeOfLastMonth() {
        return toDate(lastDayOfMonth(LocalDate.now().minusMonths(1)).atTime(endTimeOfDay()));
    }

    /**
     * 当月第一天起始时间 即：当月第一天日期+00:00:00
     *
     * @return Date
     */
    public static Date startTimeOfMonth() {
        return toDate(firstDayOfMonth(LocalDate.now()).atTime(startTimeOfDay()));
    }

    /**
     * 当月最后一天结束时间即：当月最后一天日期+23:59:59
     *
     * @return Date
     */
    public static Date endTimeOfMonth() {
        return toDate(lastDayOfMonth(LocalDate.now()).atTime(endTimeOfDay()));
    }

    /**
     * 获date起始时间
     *
     * @param date Date
     * @return Date
     */
    public static Date startTimeOfDate(Date date) {
        return toDate(toLocalDate(date).atTime(startTimeOfDay()));
    }

    /**
     * 获取date结束时间 精确到秒 23:59:59
     *
     * @param date Date
     * @return Date
     */
    public static Date endTimeOfDate(Date date) {
        return toDate(toLocalDate(date).atTime(endTimeOfDay()));
    }


    /**
     * 获localDateTime结束时间，精确时间到纳秒 23:59:59.999000000 （转换为Date会丢失毫秒以后数据）
     *
     * @param date Date
     * @return Date
     */
    public static Date endAccuracyTimeOfDate(Date date) {
        return toDate(toLocalDate(date).atTime(endAccuracyTimeOfDay()));
    }

    /**
     * 获localDateTime起始时间
     *
     * @param localDateTime LocalDateTime
     * @return LocalDateTime
     */
    public static LocalDateTime startTimeOfLocalDateTime(LocalDateTime localDateTime) {
        return localDateTime.with(startTimeOfDay());
    }

    /**
     * 获取localDateTime结束时间，精确时间到纳秒 23:59:59.999999999
     *
     * @param localDateTime LocalDateTime
     * @return LocalDateTime
     */
    public static LocalDateTime endAccuracyTimeOfLocalDateTime(LocalDateTime localDateTime) {
        return localDateTime.with(endAccuracyTimeOfDay());
    }


    /**
     * 获取指定年月的第一天起始时间
     *
     * @param year  年
     * @param month 月
     * @return Date
     */
    public static Date startTimeOfSpecialMonth(int year, int month) {
        return toDate(LocalDate.of(year, month, 1).atTime(startTimeOfDay()));
    }

    /**
     * 获取指定年月的最后一天结束时间
     *
     * @param year  年
     * @param month 月
     * @return Date
     */
    public static Date endTimeOfSpecialMonth(int year, int month) {
        return toDate(lastDayOfMonth(LocalDate.of(year, month, 1)).atTime(endTimeOfDay()));
    }

    /**
     * 获取指定日期的起始时间
     *
     * @param year       年
     * @param month      月
     * @param dayOfMonth 日
     * @return Date
     */
    public static Date startTimeOfDate(int year, int month, int dayOfMonth) {
        return toDate(LocalDate.of(year, month, dayOfMonth).atTime(startTimeOfDay()));
    }

    /**
     * 获取指定日期的结束时间
     *
     * @param year       年
     * @param month      月
     * @param dayOfMonth 日
     * @return Date
     */
    public static Date endTimeOfDate(int year, int month, int dayOfMonth) {
        return toDate(LocalDate.of(year, month, dayOfMonth).atTime(endTimeOfDay()));
    }

    /**
     * 获取第一季度起始日期的开始时间
     *
     * @param year 年
     * @return Date
     */
    public static Date startTimeOfFirstQuarter(int year) {
        return startTimeOfSpecialMonth(year, 1);
    }

    /**
     * 获取第二季度起始日期的开始时间
     *
     * @param year 年
     * @return Date
     */
    public static Date startTimeOfSecondQuarter(int year) {
        return startTimeOfSpecialMonth(year, 4);
    }

    /**
     * 获取第三季度起始日期的开始时间
     *
     * @param year 年
     * @return Date
     */
    public static Date startTimeOfThirdQuarter(int year) {
        return startTimeOfSpecialMonth(year, 7);
    }

    /**
     * 获取第四季度起始日期的开始时间
     *
     * @param year 年
     * @return Date
     */
    public static Date startTimeOfFourthQuarter(int year) {
        return startTimeOfSpecialMonth(year, 10);
    }

    /**
     * 获取第一季度结束日期的开始时间
     *
     * @param year 年
     * @return Date
     */
    public static Date endTimeOfFirstQuarter(int year) {
        return endTimeOfSpecialMonth(year, 3);
    }

    /**
     * 获取第二季度结束日期的开始时间
     *
     * @param year 年
     * @return Date
     */
    public static Date endTimeOfSecondQuarter(int year) {
        return endTimeOfSpecialMonth(year, 6);
    }

    /**
     * 获取第三季度结束日期的开始时间
     *
     * @param year 年
     * @return Date
     */
    public static Date endTimeOfThirdQuarter(int year) {
        return endTimeOfSpecialMonth(year, 9);
    }

    /**
     * 获取第四季度结束日期的开始时间
     *
     * @param year 年
     * @return Date
     */
    public static Date endTimeOfFourthQuarter(int year) {
        return endTimeOfSpecialMonth(year, 12);
    }

    /**
     * 获取当前季度起始日期的开始时间
     *
     * @return Date
     */
    public static Date startTimeOfCurrentQuarter() {
        LocalDate now = LocalDate.now();
        int year = now.getYear();
        int firstMonthOfQuarter = now.getMonth().firstMonthOfQuarter().getValue();
        return startTimeOfSpecialMonth(year, firstMonthOfQuarter);
    }

    /**
     * 获取当前季度结束日期的时间
     *
     * @return Date
     */
    public static Date endTimeOfCurrentQuarter() {
        LocalDate now = LocalDate.now();
        int year = now.getYear();
        int endMonthOfQuarter = now.getMonth().firstMonthOfQuarter().getValue() + 2;
        return endTimeOfSpecialMonth(year, endMonthOfQuarter);
    }

    /**
     * 获取指定年起始日期的开始时间
     *
     * @param year 年
     * @return Date
     */
    public static Date startTimeOfYear(int year) {
        return startTimeOfSpecialMonth(year, 1);
    }

    /**
     * 获取指定年结束日期的开始时间
     *
     * @param year 年
     * @return Date
     */
    public static Date endTimeOfYear(int year) {
        return endTimeOfSpecialMonth(year, 12);
    }

    /**
     * 获取当前年起始日期的开始时间
     *
     * @return Date
     */
    public static Date startTimeOfCurrentYear() {
        return startTimeOfYear(LocalDate.now().getYear());
    }

    /**
     * 获取当前年结束日期的时间
     *
     * @return Date
     */
    public static Date endTimeOfCurrentYear() {
        return endTimeOfYear(LocalDate.now().getYear());
    }

    /**
     * 检查两个Calendar时间戳是否相同
     * 此方法检查两个Calendar的毫秒数时间戳是否相同
     *
     * @param date1 时间1
     * @param date2 时间2
     * @return 两个Calendar时间戳是否相同。如果两个时间都为
     * {@code null}返回true，否则有{@code null}返回false
     */
    public static boolean isSameTime(Calendar date1, Calendar date2) {
        if (null == date1) {
            return null == date2;
        }
        if (null == date2) {
            return false;
        }

        return date1.getTimeInMillis() == date2.getTimeInMillis();
    }

    /**
     * 是否为相同时间
     * 此方法比较两个日期的时间戳是否相同
     *
     * @param date1 日期1
     * @param date2 日期2
     * @return 是否为相同时间
     */
    public static boolean isSameTime(Date date1, Date date2) {
        return date1.compareTo(date2) == 0;
    }

    /**
     * 比较两个日期是否为同一天
     *
     * @param cal1 日期1
     * @param cal2 日期2
     * @return 是否为同一天
     */
    public static boolean isSameDay(Calendar cal1, Calendar cal2) {
        if (null == cal1 || null == cal2) {
            throw new IllegalArgumentException("The date must not be null");
        }
        return cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR) &&
                cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA);
    }

    /**
     * 比较两个日期是否为同一天
     *
     * @param date1 日期1
     * @param date2 日期2
     * @return 是否为同一天
     */
    public static boolean isSameDay(final Date date1, final Date date2) {
        if (date1 == null || date2 == null) {
            throw new IllegalArgumentException("The date must not be null");
        }
        return isSameDay(toCalendar(date1), toCalendar(date2));
    }

    /**
     * 比较两个日期是否为同一天
     *
     * @param date1 日期1
     * @param date2 日期2
     * @return 是否为同一天
     */
    public static boolean isSameDay(final LocalDate date1, final LocalDate date2) {
        return date1 != null && date2 != null && date1.isEqual(date2);
    }

    /**
     * 比较两个日期是否为同一天
     *
     * @param date1 日期1
     * @param date2 日期2
     * @return 是否为同一天
     */
    public static boolean isSameDay(final LocalDateTime date1, final LocalDateTime date2) {
        return date1 != null && date2 != null && date1.toLocalDate().isEqual(date2.toLocalDate());
    }

    /**
     * 比较两个日期是否为同一周
     *
     * @param cal1  日期1
     * @param cal2  日期2
     * @param isMon 是否为周一。国内第一天为星期一，国外第一天为星期日
     * @return 是否为同一周
     */
    public static boolean isSameWeek(Calendar cal1, Calendar cal2, boolean isMon) {
        if (cal1 == null || cal2 == null) {
            throw new IllegalArgumentException("The date must not be null");
        }
        // 防止比较前修改原始Calendar对象
        cal1 = (Calendar) cal1.clone();
        cal2 = (Calendar) cal2.clone();
        // 把所传日期设置为其当前周的第一天
        // 比较设置后的两个日期是否是同一天：true 代表同一周
        if (isMon) {
            cal1.setFirstDayOfWeek(Calendar.MONDAY);
            cal1.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
            cal2.setFirstDayOfWeek(Calendar.MONDAY);
            cal2.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        } else {
            cal1.setFirstDayOfWeek(Calendar.SUNDAY);
            cal1.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
            cal2.setFirstDayOfWeek(Calendar.SUNDAY);
            cal2.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        }
        return isSameDay(cal1, cal2);
    }

    /**
     * 比较两个日期是否为同一周
     *
     * @param date1 日期1
     * @param date2 日期2
     * @param isMon 是否为周一。国内第一天为星期一，国外第一天为星期日
     * @return 是否为同一周
     */
    public static boolean isSameWeek(final Date date1, final Date date2, boolean isMon) {
        if (date1 == null || date2 == null) {
            throw new IllegalArgumentException("The date must not be null");
        }
        return isSameWeek(toCalendar(date1), toCalendar(date2), isMon);
    }

    /**
     * 比较两个日期是否为同一月
     *
     * @param date1 日期1
     * @param date2 日期2
     * @return 是否为同一月
     */
    public static boolean isSameMonth(final Date date1, final Date date2) {
        if (date1 == null || date2 == null) {
            throw new IllegalArgumentException("The date must not be null");
        }
        return isSameMonth(toCalendar(date1), toCalendar(date2));
    }

    /**
     * 比较两个日期是否为同一月
     *
     * @param cal1 日期1
     * @param cal2 日期2
     * @return 是否为同一月
     */
    public static boolean isSameMonth(Calendar cal1, Calendar cal2) {
        if (null == cal1 || null == cal2) {
            throw new IllegalArgumentException("The date must not be null");
        }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH);
    }

    /**
     * 相同月日比较判断，用于生日，节日等周期性的日期比较判断
     *
     * @param localDate1 日期1
     * @param monthDay   月日
     * @return boolean
     */
    public static boolean isSameMonthDay(LocalDate localDate1, MonthDay monthDay) {
        return MonthDay.of(localDate1.getMonthValue(), localDate1.getDayOfMonth()).equals(monthDay);
    }

    /**
     * 相同月日比较判断，用于生日，节日等周期性的日期比较判断
     *
     * @param localDate1  日期1
     * @param monthDayStr 月日字符串 MM-dd格式
     * @return boolean
     */
    public static boolean isSameMonthDay(LocalDate localDate1, String monthDayStr) {
        return isSameMonthDay(localDate1, MonthDay.parse(Symbol.MINUS + Symbol.MINUS + monthDayStr));
    }

    /**
     * 相同月日比较判断，用于生日，节日等周期性的日期比较判断。
     *
     * @param localDate1 日期1
     * @param localDate2 日期2
     * @return boolean
     */
    public static boolean isSameMonthDay(LocalDate localDate1, LocalDate localDate2) {
        return isSameMonthDay(localDate1, MonthDay.of(localDate2.getMonthValue(), localDate2.getDayOfMonth()));
    }

    /**
     * 相同月日比较判断，用于生日，节日等周期性的日期比较判断。
     *
     * @param date        日期
     * @param monthDayStr 月日字符串，MM-dd格式
     * @return boolean
     */
    public static boolean isSameMonthDay(Date date, String monthDayStr) {
        return isSameMonthDay(toLocalDate(date), monthDayStr);
    }

    /**
     * 相同月日比较判断，用于生日，节日等周期性的日期比较判断。
     *
     * @param date1 日期1
     * @param date2 日期2
     * @return boolean
     */
    public static boolean isSameMonthDay(Date date1, Date date2) {
        return isSameMonthDay(toLocalDate(date1), toLocalDate(date2));
    }

    /**
     * 相同月日比较判断，与当前日期对比，用于生日，节日等周期性的日期比较判断
     *
     * @param monthDayStr 月日字符串，MM-dd格式
     * @return boolean
     */
    public static boolean isSameMonthDayOfNow(String monthDayStr) {
        return isSameMonthDay(LocalDate.now(), monthDayStr);
    }

    /**
     * 下个固定月日相差天数，用于生日，节日等周期性的日期推算
     *
     * @param localDate1 日期1
     * @param month      月
     * @param dayOfMonth 日
     * @return long
     */
    public static long betweenNextSameMonthDay(LocalDate localDate1, int month, int dayOfMonth) {
        MonthDay monthDay1 = MonthDay.of(localDate1.getMonthValue(), localDate1.getDayOfMonth());
        MonthDay monthDay2 = MonthDay.of(month, dayOfMonth);

        // localDate1 月日 小于 month dayOfMonth
        if (monthDay1.compareTo(monthDay2) == -1) {
            return betweenTotalDays(localDate1.atStartOfDay(),
                    localDate1.withMonth(month).withDayOfMonth(dayOfMonth).atStartOfDay());
        } else {
            // 闰年2月29
            MonthDay leapMonthDay = MonthDay.of(2, 29);
            if (leapMonthDay.equals(monthDay2)) {
                LocalDate nextLeapYear = nextLeapYear(localDate1);
                return betweenTotalDays(localDate1.atStartOfDay(),
                        nextLeapYear.withMonth(month).withDayOfMonth(dayOfMonth).atStartOfDay());
            } else {
                LocalDate next = localDate1.plusYears(1);
                return betweenTotalDays(localDate1.atStartOfDay(),
                        next.withMonth(month).withDayOfMonth(dayOfMonth).atStartOfDay());
            }
        }
    }

    /**
     * 下个固定月日相差天数，用于生日，节日等周期性的日期推算
     *
     * @param localDate   日期
     * @param monthDayStr 月日字符串，MM-dd格式
     * @return long
     */
    public static long betweenNextSameMonthDay(LocalDate localDate, String monthDayStr) {
        MonthDay monthDay2 = MonthDay.parse(Symbol.MINUS + Symbol.MINUS + monthDayStr);
        return betweenNextSameMonthDay(localDate, monthDay2.getMonthValue(), monthDay2.getDayOfMonth());
    }

    /**
     * 下个固定月日相差天数，用于生日，节日等周期性的日期推算
     *
     * @param date        日期
     * @param monthDayStr 月日字符串，MM-dd格式
     * @return long
     */
    public static long betweenNextSameMonthDay(Date date, String monthDayStr) {
        MonthDay monthDay2 = MonthDay.parse(Symbol.MINUS + Symbol.MINUS + monthDayStr);
        return betweenNextSameMonthDay(toLocalDate(date), monthDay2.getMonthValue(),
                monthDay2.getDayOfMonth());
    }

    /**
     * 下个固定月日相差天数，与当前日期对比，用于生日，节日等周期性的日期推算
     *
     * @param monthDayStr 月日字符串，MM-dd格式
     * @return long
     */
    public static long betweenNextSameMonthDayOfNow(String monthDayStr) {
        MonthDay monthDay2 = MonthDay.parse(Symbol.MINUS + Symbol.MINUS + monthDayStr);
        return betweenNextSameMonthDay(LocalDate.now(), monthDay2.getMonthValue(),
                monthDay2.getDayOfMonth());
    }

    /**
     * 下个固定月日相差日期，用于生日，节日等周期性的日期推算
     *
     * @param localDate   日期
     * @param monthDayStr 月日字符串，MM-dd格式
     * @return LocalDate
     */
    public static LocalDate nextSameMonthDay(LocalDate localDate, String monthDayStr) {
        return localDate.plusDays(betweenNextSameMonthDay(localDate, monthDayStr));
    }

    /**
     * 下个固定月日相差日期，用于生日，节日等周期性的日期推算
     *
     * @param date        日期
     * @param monthDayStr 月日字符串，MM-dd格式
     * @return Date
     */
    public static Date nextSameMonthDay(Date date, String monthDayStr) {
        return toDate(nextSameMonthDay(toLocalDate(date), monthDayStr));
    }

    /**
     * 下个固定月日相差日期，与当前日期对比，用于生日，节日等周期性的日期推算
     *
     * @param monthDayStr 月日字符串，MM-dd格式
     * @return Date
     */
    public static Date nextSameMonthDayOfNow(String monthDayStr) {
        return nextSameMonthDay(new Date(), monthDayStr);
    }

    /**
     * 根据日期查询星座中文名称
     *
     * @param monthDay 月日字符串，MM-dd格式
     * @return String
     */
    public static String getZodiacCnName(String monthDay) {
        return Fields.Zodiac.getCnNameByMonthDay(monthDay);
    }

    /**
     * 根据日期查询星座中文名称
     *
     * @param date Date
     * @return String
     */
    public static String getZodiacCnName(Date date) {
        return Fields.Zodiac.getCnNameByMonthDay(format(date));
    }

    /**
     * 根据日期查询星座英文名称
     *
     * @param monthDay 月日字符串，MM-dd格式
     * @return String
     */
    public static String getZodiacEnName(String monthDay) {
        return Fields.Zodiac.getEnNameByMonthDay(monthDay);
    }

    /**
     * 根据日期查询星座中文名称
     *
     * @param date Date
     * @return String
     */
    public static String getZodiacEnName(Date date) {
        return Fields.Zodiac.getEnNameByMonthDay(format(date));
    }

    /**
     * 通过生日计算星座
     *
     * @param date 出生日期
     * @return 星座名
     */
    public static String getZodiac(Date date) {
        return getZodiac(toCalendar(date));
    }

    /**
     * 通过生日计算星座
     *
     * @param calendar 出生日期
     * @return 星座名
     */
    public static String getZodiac(Calendar calendar) {
        if (null == calendar) {
            return null;
        }
        return getZodiac(calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
    }

    /**
     * 通过生日计算星座
     *
     * @param month 月,从0开始计数
     * @param day   天
     * @return 星座名
     */
    public static String getZodiac(int month, int day) {
        return day < Fields.SLICED[month] ? Fields.ZODIAC[month] : Fields.ZODIAC[month + 1];
    }

    /**
     * 获取指定区间的时间列表，包含起始
     *
     * @param startInclusive 开始时间
     * @param endInclusive   结束时间
     * @return 时间列表
     */
    public static List<LocalDateTime> getLocalDateTimeList(LocalDateTime startInclusive, LocalDateTime endInclusive) {
        if (startInclusive.isAfter(endInclusive)) {
            throw new DateTimeException("startInclusive must before or equal endInclusive!");
        }
        List<LocalDateTime> localDateTimeList = new ArrayList<>();
        long days = betweenTotalDays(startInclusive, endInclusive) + 1;
        for (long i = 0; i < days; i++) {
            localDateTimeList.add(startInclusive.plusDays(i));
        }
        return localDateTimeList;
    }

    /**
     * 获取指定区间的时间列表，包含起始
     *
     * @param startInclusive 开始时间
     * @param endInclusive   结束时间
     * @return 时间列表
     */
    public static List<LocalDate> getLocalDateList(LocalDate startInclusive, LocalDate endInclusive) {
        return getLocalDateTimeList(toLocalDateTime(startInclusive),
                toLocalDateTime(endInclusive)).stream()
                .map(localDateTime -> localDateTime.toLocalDate()).collect(Collectors.toList());
    }

    /**
     * 获取指定年月的所有日期列表
     *
     * @param yearMonth 年月
     * @return 时间列表
     */
    public static List<LocalDate> getLocalDateList(YearMonth yearMonth) {
        List<LocalDate> localDateList = new ArrayList<>();
        long days = yearMonth.lengthOfMonth();
        LocalDate localDate = toLocalDateStartOfMonth(yearMonth);
        for (long i = 0; i < days; i++) {
            localDateList.add(localDate.plusDays(i));
        }
        return localDateList;
    }

    /**
     * 获取指定年月的所有日期列表
     *
     * @param yearMonthStr 年月字符串，格式： yyyy-MM
     * @return 时间列表
     */
    public static List<LocalDate> getLocalDateList(String yearMonthStr) {
        YearMonth yearMonth = YearMonth.parse(yearMonthStr);
        return getLocalDateList(yearMonth);
    }

    /**
     * 获取指定年月的所有日期列表
     *
     * @param year  年
     * @param month 月
     * @return 时间列表
     */
    public static List<LocalDate> getLocalDateList(int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        return getLocalDateList(yearMonth);
    }

    /**
     * 获取指定年月的所有日期列表
     *
     * @param yearMonth 年月
     * @return 时间列表
     */
    public static List<LocalDateTime> getLocalDateTimeList(YearMonth yearMonth) {
        return getLocalDateList(yearMonth).stream()
                .map(localDate -> toLocalDateTime(localDate)).collect(Collectors.toList());
    }

    /**
     * 获取指定年月的所有日期列表
     *
     * @param yearMonthStr 年月字符串，格式： yyyy-MM
     * @return 时间列表
     */
    public static List<LocalDateTime> getLocalDateTimeList(String yearMonthStr) {
        return getLocalDateList(yearMonthStr).stream()
                .map(localDate -> toLocalDateTime(localDate)).collect(Collectors.toList());
    }

    /**
     * 获取指定年月的所有日期列表
     *
     * @param year  年
     * @param month 月
     * @return 时间列表
     */
    public static List<LocalDateTime> getLocalDateTimeList(int year, int month) {
        return getLocalDateList(YearMonth.of(year, month)).stream()
                .map(localDate -> toLocalDateTime(localDate)).collect(Collectors.toList());
    }

    /**
     * 获取指定年月的所有日期列表
     *
     * @param yearMonthStr 年月字符串，格式： yyyy-MM
     * @return 时间列表
     */
    public static List<Date> getDateList(String yearMonthStr) {
        return getLocalDateList(yearMonthStr).stream().map(localDate -> toDate(localDate))
                .collect(Collectors.toList());
    }

    /**
     * 获取指定年月的所有日期列表
     *
     * @param year  年
     * @param month 月
     * @return 时间列表
     */
    public static List<Date> getDateList(int year, int month) {
        return getLocalDateList(YearMonth.of(year, month)).stream().map(localDate -> toDate(localDate))
                .collect(Collectors.toList());
    }

    /**
     * 获取指定区间的时间列表，包含起始
     *
     * @param startInclusive 开始时间
     * @param endInclusive   结束时间
     * @return 时间列表
     */
    public static List<Date> getDateList(Date startInclusive, Date endInclusive) {
        return getLocalDateTimeList(toLocalDateTime(startInclusive),
                toLocalDateTime(endInclusive)).stream()
                .map(localDateTime -> toDate(localDateTime)).collect(Collectors.toList());
    }

    /**
     * 判断是否过期，（输入年月小于当前年月）
     *
     * @param yearMonth 年月
     * @return boolean
     */
    public static boolean isExpiry(YearMonth yearMonth) {
        return yearMonth.isBefore(YearMonth.now());
    }

    /**
     * 判断是否过期，（输入年月小于当前年月）
     *
     * @param value 年月字符串，格式： yyyy-MM
     * @return boolean
     */
    public static boolean isExpiry(String value) {
        YearMonth yearMonth = YearMonth.parse(value);
        return isExpiry(yearMonth);
    }

    /**
     * 验证是否为生日
     * 只支持以下几种格式：
     * <ul>
     * <li>yyyyMMdd</li>
     * <li>yyyy-MM-dd</li>
     * <li>yyyy/MM/dd</li>
     * <li>yyyy.MM.dd</li>
     * <li>yyyy年MM月dd日</li>
     * </ul>
     *
     * @param value 值
     * @return 是否为生日
     */
    public static boolean isBirthday(CharSequence value) {
        final Matcher matcher = RegEx.BIRTHDAY.matcher(value);
        if (matcher.find()) {
            return DateKit.isBirthday(Integer.parseInt(matcher.group(1)),
                    Integer.parseInt(matcher.group(3)),
                    Integer.parseInt(matcher.group(5)));
        }
        return false;
    }

    /**
     * 是否为生日
     *
     * @param date 生日
     * @return boolean
     */
    public static boolean isBirthDay(Date date) {
        return isBirthDay(toLocalDate(date));
    }

    /**
     * 是否为生日
     *
     * @param localDate 生日
     * @return boolean
     */
    public static boolean isBirthDay(LocalDate localDate) {
        return isSameMonthDay(localDate, LocalDate.now());
    }

    /**
     * 是否为生日
     *
     * @param localDateTime 生日
     * @return boolean
     */
    public static boolean isBirthDay(LocalDateTime localDateTime) {
        return isBirthDay(toLocalDate(localDateTime));
    }

    /**
     * 验证是否为生日
     *
     * @param year  年，从1900年开始计算
     * @param month 月，从1开始计数
     * @param day   日，从1开始计数
     * @return 是否为生日
     */
    public static boolean isBirthday(int year, int month, int day) {
        // 验证年
        int thisYear = DateKit.thisYear();
        if (year < 1900 || year > thisYear) {
            return false;
        }

        // 验证月
        if (month < 1 || month > 12) {
            return false;
        }

        // 验证日
        if (day < 1 || day > 31) {
            return false;
        }
        // 检查几个特殊月的最大天数
        if (day == 31 && (month == 4 || month == 6 || month == 9 || month == 11)) {
            return false;
        }
        if (month == 2) {
            // 在2月，非闰年最大28，闰年最大29
            return day < 29 || (day == 29 && DateKit.isLeapYear(year));
        }
        return true;
    }

    /**
     * 减少时间精度到秒，其他补0，返回如，2020-04-23 15:18:13
     *
     * @param localDateTime LocalDateTime
     * @return LocalDateTime
     */
    public static LocalDateTime reduceAccuracyToSecond(LocalDateTime localDateTime) {
        return LocalDateTime.of(localDateTime.getYear(), localDateTime.getMonthValue(),
                localDateTime.getDayOfMonth(), localDateTime.getHour(), localDateTime.getMinute(),
                localDateTime.getSecond());
    }

    /**
     * 减少时间精度到秒，其他补0，返回如，2020-04-23 15:18:13
     *
     * @param date Date
     * @return Date
     */
    public static Date reduceAccuracyToSecond(Date date) {
        return toDate(reduceAccuracyToSecond(toLocalDateTime(date)));
    }

    /**
     * 减少时间精度到分，其他补0，返回如，2020-04-23 15:18:00
     *
     * @param localDateTime LocalDateTime
     * @return LocalDateTime
     */
    public static LocalDateTime reduceAccuracyToMinute(LocalDateTime localDateTime) {
        return LocalDateTime.of(localDateTime.getYear(), localDateTime.getMonthValue(),
                localDateTime.getDayOfMonth(), localDateTime.getHour(), localDateTime.getMinute(),
                0);
    }

    /**
     * 减少时间精度到分，其他补0，返回如，2020-04-23 15:18:00
     *
     * @param date Date
     * @return Date
     */
    public static Date reduceAccuracyToMinute(Date date) {
        return toDate(reduceAccuracyToMinute(toLocalDateTime(date)));
    }

    /**
     * 减少时间精度到小时，其他补0，返回如，2020-04-23 15:00:00
     *
     * @param localDateTime LocalDateTime
     * @return LocalDateTime
     */
    public static LocalDateTime reduceAccuracyToHour(LocalDateTime localDateTime) {
        return LocalDateTime.of(localDateTime.getYear(), localDateTime.getMonthValue(),
                localDateTime.getDayOfMonth(), localDateTime.getHour(), 0, 0);
    }

    /**
     * 减少时间精度到小时，其他补0，返回如，2020-04-23 15:00:00
     *
     * @param date Date
     * @return Date
     */
    public static Date reduceAccuracyToHour(Date date) {
        return toDate(reduceAccuracyToHour(toLocalDateTime(date)));
    }

    /**
     * 减少时间精度到天，其他补0，返回如，2020-04-23 00:00:00
     *
     * @param localDateTime LocalDateTime
     * @return LocalDateTime
     */
    public static LocalDateTime reduceAccuracyToDay(LocalDateTime localDateTime) {
        return LocalDateTime.of(localDateTime.getYear(), localDateTime.getMonthValue(),
                localDateTime.getDayOfMonth(), 0, 0, 0);
    }

    /**
     * 减少时间精度到天，其他补0，返回如，2020-04-23 00:00:00
     *
     * @param date Date
     * @return Date
     */
    public static Date reduceAccuracyToDay(Date date) {
        return toDate(reduceAccuracyToDay(toLocalDateTime(date)));
    }

    /**
     * 日期所在月中第几周
     *
     * @param localDate LocalDate
     * @param locale    地区 为null 是取系统默认地区
     * @return 周数
     */
    public static int weekOfMonth(LocalDate localDate, Locale locale) {
        WeekFields weekFields = null == locale ? WeekFields.of(Locale.getDefault()) : WeekFields.of(locale);
        return (int) weekFields.weekOfMonth().getFrom(localDate);
    }

    /**
     * 日期所在月中第几周
     *
     * @param localDate LocalDate
     * @return 周数
     */
    public static int weekOfMonth(LocalDate localDate) {
        return weekOfMonth(localDate, null);
    }

    /**
     * 日期所在月中第几周
     *
     * @param localDateTime LocalDateTime
     * @return 周数
     */
    public static int weekOfMonth(LocalDateTime localDateTime) {
        return weekOfMonth(toLocalDate(localDateTime), null);
    }

    /**
     * 日期所在月中第几周
     *
     * @param date Date
     * @return 周数
     */
    public static int weekOfMonth(Date date) {
        return weekOfMonth(toLocalDate(date), null);
    }

    /**
     * 当前日期所在月中第几周
     *
     * @return 周数
     */
    public static int weekOfMonth() {
        return weekOfMonth(LocalDate.now());
    }

    /**
     * 日期所在年中第几周
     *
     * @param localDate LocalDate
     * @param locale    地区 为null 是取系统默认地区
     * @return 周数
     */
    public static int weekOfYear(LocalDate localDate, Locale locale) {
        WeekFields weekFields = null == locale ? WeekFields.of(Locale.getDefault()) : WeekFields.of(locale);
        return (int) weekFields.weekOfYear().getFrom(localDate);
    }

    /**
     * 日期所在年中第几周
     *
     * @param localDate LocalDate
     * @return 周数
     */
    public static int weekOfYear(LocalDate localDate) {
        return weekOfYear(localDate, null);
    }

    /**
     * 日期所在年中第几周
     *
     * @param localDateTime LocalDateTime
     * @return 周数
     */
    public static int weekOfYear(LocalDateTime localDateTime) {
        return weekOfYear(toLocalDate(localDateTime), null);
    }

    /**
     * 日期所在年中第几周
     *
     * @param date Date
     * @return 周数
     */
    public static int weekOfYear(Date date) {
        return weekOfYear(toLocalDate(date), null);
    }

    /**
     * 当前日期所在年中第几周
     *
     * @return 周数
     */
    public static int weekOfYear() {
        return weekOfYear(LocalDate.now());
    }

    /**
     * 是否为周一
     *
     * @param localDate LocalDate
     * @return 是 true 否 false
     */
    public static boolean isMonday(LocalDate localDate) {
        return Fields.Week.Mon.getKey() == localDate.getDayOfWeek().getValue();
    }

    /**
     * 是否为周一
     *
     * @param date Date
     * @return 是 true 否 false
     */
    public static boolean isMonday(Date date) {
        return isMonday(toLocalDate(date));
    }

    /**
     * 是否为周五
     *
     * @param localDate LocalDate
     * @return 是 true 否 false
     */
    public static boolean isFriday(LocalDate localDate) {
        return Fields.Week.Fri.getKey() == localDate.getDayOfWeek().getValue();
    }

    /**
     * 是否为周五
     *
     * @param date Date
     * @return 是 true 否 false
     */
    public static boolean isFriday(Date date) {
        return isFriday(toLocalDate(date));
    }

    /**
     * 校验日期格式,日期不能早于当前天
     *
     * @param dptDate 日期, 仅需包含年月日
     * @param pattern 日期转移格式
     * @return true/false
     */
    public static boolean isDate(String dptDate, String pattern) {
        if (null == dptDate || dptDate.isEmpty()) {
            return false;
        }
        String formatDate = format(dptDate, pattern, pattern);
        return formatDate.equals(dptDate);
    }

    /**
     * 校验日期格式,日期不能早于当前天, 默认日期转义格式：yyyy-MM-dd
     *
     * @param dptDate 日期,仅需包含年月日
     * @return true/false
     */
    public static boolean isDate(String dptDate) {
        return isDate(dptDate, Fields.NORM_DATE_PATTERN);
    }

    /**
     * 校验前面的日期go,是否早于或者等于后面的日期back
     *
     * @param go      日期1
     * @param back    日期2
     * @param pattern 日期正则表达式
     * @return true/false
     */
    public static boolean isBefore(String go, String back, String pattern) {
        if (null == go || null == back || go.isEmpty() || back.isEmpty())
            return false;

        Date goDate = offsetDay(parse(go, pattern), -1);
        Date backDate = parse(back, pattern);
        if (null != goDate && null != backDate) {
            return goDate.before(backDate);
        }
        return false;
    }

    /**
     * 校验前面的日期go,是否早于或者等于后面的日期back
     *
     * @param go   日期1
     * @param back 日期2
     * @return true/false
     */
    public static boolean isBefore(String go, String back) {
        return isBefore(go, back, Fields.NORM_DATE_PATTERN);
    }

    /**
     * 验证长日期格式yyyy-MM-dd HH:mm:ss
     *
     * @param datetime 日期
     * @return true/false
     */
    public static boolean isDatetime(String datetime) {
        return isDate(datetime, Fields.NORM_DATETIME_PATTERN);
    }

    /**
     * 校验短日期格式[yyyyMMdd]
     *
     * @param date 短日期
     * @return true/false
     */
    public static boolean isShortDate(String date) {
        if (null == date || Normal.EMPTY.equals(date))
            return false;
        String regex = "^([\\d]{4}(((0[13578]|1[02])((0[1-9])|([12][0-9])|(3[01])))|(((0[469])|11)((0[1-9])|([12][1-9])|30))|(02((0[1-9])|(1[0-9])|(2[1-8])))))|((((([02468][048])|([13579][26]))00)|([0-9]{2}(([02468][048])|([13579][26]))))(((0[13578]|1[02])((0[1-9])|([12][0-9])|(3[01])))|(((0[469])|11)((0[1-9])|([12][1-9])|30))|(02((0[1-9])|(1[0-9])|(2[1-9])))))$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(date);
        return matcher.matches();
    }

    /**
     * 检查两个时间段是否有时间重叠
     * 重叠指两个时间段是否有交集，注意此方法时间段重合时如：
     * <ul>
     *     <li>此方法未纠正开始时间小于结束时间</li>
     *     <li>当realStartTime和realEndTime或startTime和endTime相等时,退化为判断区间是否包含点</li>
     *     <li>当realStartTime和realEndTime和startTime和endTime相等时,退化为判断点与点是否相等</li>
     * </ul>
     * See <a href="https://www.ics.uci.edu/~alspaugh/cls/shr/allen.html">准确的区间关系参考:艾伦区间代数</a>
     *
     * @param realStartTime 第一个时间段的开始时间
     * @param realEndTime   第一个时间段的结束时间
     * @param startTime     第二个时间段的开始时间
     * @param endTime       第二个时间段的结束时间
     * @return true 表示时间有重合或包含或相等
     */
    public static boolean isOverlap(final Date realStartTime, final Date realEndTime,
                                    final Date startTime, final Date endTime) {

        // x>b||a>y 无交集
        // 则有交集的逻辑为 !(x>b||a>y)
        // 根据德摩根公式，可化简为 x<=b && a<=y 即 realStartTime<=endTime && startTime<=realEndTime
        return realStartTime.compareTo(endTime) <= 0 && startTime.compareTo(realEndTime) <= 0;
    }

    /**
     * 判断传入的日期是否 &gt;=今天
     *
     * @param date 待判断的日期
     * @return true/false
     */
    public static boolean isNotLessThanToday(String date) {
        return isNotLessThanToday(date, Fields.NORM_DATE_PATTERN);
    }

    /**
     * 判断传入的日期是否&gt;=今天
     *
     * @param date   待判断的日期
     * @param format 格式
     * @return true/false
     */
    public static boolean isNotLessThanToday(String date, String format) {
        if (null == date || date.isEmpty())
            return false;
        Date cmpDate = offsetDay(new Date(), -1);
        Date srcDate = new DateTime(date, format);
        return srcDate.after(cmpDate);
    }

    /**
     * 是否为上午
     *
     * @param date 日期
     * @return 是否为上午
     */
    public static boolean isAM(Date date) {
        return DateTime.of(date).isAM();
    }

    /**
     * 是否为上午
     *
     * @param calendar {@link Calendar}
     * @return 是否为上午
     */
    public static boolean isAM(Calendar calendar) {
        return Calendar.AM == calendar.get(Calendar.AM_PM);
    }

    /**
     * 是否为下午
     *
     * @param date 日期
     * @return 是否为下午
     */
    public static boolean isPM(Date date) {
        return DateTime.of(date).isPM();
    }

    /**
     * 是否为下午
     *
     * @param calendar {@link Calendar}
     * @return 是否为下午
     */
    public static boolean isPM(Calendar calendar) {
        return Calendar.PM == calendar.get(Calendar.AM_PM);
    }

    /**
     * 判断当前时间（默认时区）是否在指定范围内
     * 起始日期和结束日期可以互换
     *
     * @param beginDate 起始日期
     * @param endDate   结束日期
     * @return 是否在范围内
     */
    public static boolean isIn(Date beginDate, Date endDate) {
        return isIn(new Date(), beginDate, endDate);
    }

    /**
     * 判定在指定检查时间是否过期
     * 起始日期和结束日期可以互换
     *
     * @param date      被检查的日期
     * @param beginDate 起始日期
     * @param endDate   结束日期
     * @return 是否在范围内
     */
    public static boolean isIn(Date date, Date beginDate, Date endDate) {
        if (date instanceof DateTime) {
            return ((DateTime) date).isIn(beginDate, endDate);
        } else {
            return new DateTime(date).isIn(beginDate, endDate);
        }
    }

    /**
     * 判断当前时间（默认时区）是否在指定范围内
     * 起始时间和结束时间可以互换
     *
     * @param beginDate 起始时间（包含）
     * @param endDate   结束时间（包含）
     * @return 是否在范围内
     */
    public static boolean isIn(ChronoLocalDateTime<?> beginDate, ChronoLocalDateTime<?> endDate) {
        return isIn(LocalDateTime.now(), beginDate, endDate);
    }

    /**
     * 判断指定时间是否在指定范围内
     * 起始日期和结束日期可以互换
     *
     * @param date      被检查的日期
     * @param beginDate 起始日期（包含）
     * @param endDate   结束日期（包含）
     * @return 是否在范围内
     */
    public static boolean isIn(TemporalAccessor date, TemporalAccessor beginDate, TemporalAccessor endDate) {
        return isIn(date, beginDate, endDate, true, true);
    }

    /**
     * 判断指定时间是否在指定范围内
     * 起始日期和结束日期可以互换
     * 通过includeBegin, includeEnd参数控制日期范围区间是否为开区间，
     * 例如：传入参数：includeBegin=true, includeEnd=false，
     * 则本方法会判断 date (beginDate, endDate) 是否成立
     *
     * @param date         被检查的日期
     * @param beginDate    起始日期
     * @param endDate      结束日期
     * @param includeBegin 时间范围是否包含起始日期
     * @param includeEnd   时间范围是否包含结束日期
     * @return 是否在范围内
     */
    public static boolean isIn(TemporalAccessor date, TemporalAccessor beginDate, TemporalAccessor endDate,
                               boolean includeBegin, boolean includeEnd) {
        if (date == null || beginDate == null || endDate == null) {
            throw new IllegalArgumentException("参数不可为null");
        }

        final long thisMills = toEpochMilli(date);
        final long beginMills = toEpochMilli(beginDate);
        final long endMills = toEpochMilli(endDate);
        final long rangeMin = Math.min(beginMills, endMills);
        final long rangeMax = Math.max(beginMills, endMills);

        // 先判断是否满足 date (beginDate, endDate)
        boolean isIn = rangeMin < thisMills && thisMills < rangeMax;

        // 若不满足，则再判断是否在时间范围的边界上
        if (!isIn && includeBegin) {
            isIn = thisMills == rangeMin;
        }

        if (!isIn && includeEnd) {
            isIn = thisMills == rangeMax;
        }

        return isIn;
    }

    /**
     * 获取指定时间对应的十二时辰
     *
     * @param localTime LocalTime
     * @return 十二时辰名称
     */
    public static String getChrono(LocalTime localTime) {
        return Fields.Chrono.getName(localTime);
    }

    /**
     * 获取指定时间对应的十二时辰
     *
     * @param localDateTime LocalDateTime
     * @return 十二时辰名称
     */
    public static String getChrono(LocalDateTime localDateTime) {
        return Fields.Chrono.getName(toLocalTime(localDateTime));
    }

    /**
     * 获取指定时间对应的十二时辰
     *
     * @param date Date
     * @return 十二时辰名称
     */
    public static String getChrono(Date date) {
        return Fields.Chrono.getName(date);
    }

    /**
     * 获取当前时间对应的十二时辰
     *
     * @return 十二时辰名称
     */
    public static String getChrono() {
        return Fields.Chrono.getName(LocalTime.now());
    }

    /**
     * 通过生日计算生肖,只计算1900年后出生的人
     *
     * @param date 出生日期(年需农历)
     * @return 星座名
     */
    public static String getAnimal(Date date) {
        return getAnimal(toCalendar(date));
    }

    /**
     * 通过生日计算生肖,只计算1900年后出生的人
     *
     * @param calendar 出生日期(年需农历)
     * @return 星座名
     */
    public static String getAnimal(Calendar calendar) {
        if (null == calendar) {
            return null;
        }
        return getAnimal(calendar.get(Calendar.YEAR));
    }

    /**
     * 获取生肖名称
     *
     * @param year 农历年
     * @return 生肖名
     */
    public static String getAnimal(int year) {
        return Fields.CN_ANIMAL[(year - 4) % 12];
    }

    /**
     * 获取指定日期字段的最小值，例如分钟的最小值是0
     *
     * @param calendar  {@link Calendar}
     * @param dateField {@link Fields.Type}
     * @return 字段最小值
     * @see Calendar#getActualMinimum(int)
     */
    public static int getBeginValue(Calendar calendar, int dateField) {
        if (Calendar.DAY_OF_WEEK == dateField) {
            return calendar.getFirstDayOfWeek();
        }
        return calendar.getActualMinimum(dateField);
    }

    /**
     * 获取指定日期字段的最大值，例如分钟的最大值是59
     *
     * @param calendar  {@link Calendar}
     * @param dateField {@link Fields.Type}
     * @return 字段最大值
     * @see Calendar#getActualMaximum(int)
     */
    public static int getEndValue(Calendar calendar, int dateField) {
        if (Calendar.DAY_OF_WEEK == dateField) {
            return (calendar.getFirstDayOfWeek() + 6) % 7;
        }
        return calendar.getActualMaximum(dateField);
    }

    /**
     * 修改日期为某个时间字段结束时间
     *
     * @param date {@link Date}
     * @param type 时间字段
     * @return {@link DateTime}
     */
    public static DateTime ceiling(Date date, Fields.Type type) {
        return new DateTime(ceiling(toCalendar(date), type));
    }

    /**
     * 修改日期为某个时间字段结束时间
     *
     * @param calendar {@link Calendar}
     * @param type     时间字段
     * @return 原{@link Calendar}
     */
    public static Calendar ceiling(Calendar calendar, Fields.Type type) {
        return ceiling(calendar, type.getValue(), Fields.Modify.CEILING);
    }

    /**
     * 修改日期
     *
     * @param calendar  {@link Calendar}
     * @param dateField 日期字段，即保留到哪个日期字段
     * @param modify    修改类型，包括舍去、四舍五入、进一等
     * @return 修改后的{@link Calendar}
     */
    public static Calendar ceiling(Calendar calendar, int dateField, Fields.Modify modify) {
        return ceiling(calendar, dateField, modify, false);
    }

    /**
     * 修改日期，取起始值或者结束值可选是否归零毫秒。
     * 在{@link  Fields.Modify#TRUNCATE}模式下，毫秒始终要归零,
     * 但是在{@link  Fields.Modify#CEILING}和{@link  Fields.Modify#ROUND}模式下
     * 有时候由于毫秒部分必须为0（如MySQL数据库中），因此在此加上选项
     *
     * @param calendar            {@link Calendar}
     * @param field               日期字段，即保留到哪个日期字段
     * @param modify              修改类型，包括舍去、四舍五入、进一等
     * @param truncateMillisecond 是否归零毫秒
     * @return 修改后的{@link Calendar}
     */
    public static Calendar ceiling(Calendar calendar, int field, Fields.Modify modify, boolean truncateMillisecond) {
        // AM_PM上下午特殊处理
        if (Calendar.AM_PM == field) {
            boolean isAM = isAM(calendar);
            switch (modify) {
                case TRUNCATE:
                    calendar.set(Calendar.HOUR_OF_DAY, isAM ? 0 : 12);
                    break;
                case CEILING:
                    calendar.set(Calendar.HOUR_OF_DAY, isAM ? 11 : 23);
                    break;
                case ROUND:
                    int min = isAM ? 0 : 12;
                    int max = isAM ? 11 : 23;
                    int href = (max - min) / 2 + 1;
                    int value = calendar.get(Calendar.HOUR_OF_DAY);
                    calendar.set(Calendar.HOUR_OF_DAY, (value < href) ? min : max);
                    break;
            }
            // 处理下一级别字段
            return ceiling(calendar, field + 1, modify);
        }

        int[] ignoreFields = new int[]{
                Calendar.HOUR_OF_DAY, // 与HOUR同名
                Calendar.AM_PM, // 此字段单独处理，不参与计算起始和结束
                Calendar.DAY_OF_WEEK_IN_MONTH, // 不参与计算
                Calendar.DAY_OF_YEAR, // DAY_OF_MONTH体现
                Calendar.WEEK_OF_MONTH, // 特殊处理
                Calendar.WEEK_OF_YEAR // WEEK_OF_MONTH体现
        };
        final int endField = truncateMillisecond ? Calendar.SECOND : Calendar.MILLISECOND;
        // 循环处理各级字段，精确到毫秒字段
        for (int i = field + 1; i <= endField; i++) {
            if (ArrayKit.contains(ignoreFields, i)) {
                // 忽略无关字段(WEEK_OF_MONTH)始终不做修改
                continue;
            }

            // 在计算本周的起始和结束日时，月相关的字段忽略
            if (Calendar.WEEK_OF_MONTH == field || Calendar.WEEK_OF_YEAR == field) {
                if (Calendar.DAY_OF_MONTH == i) {
                    continue;
                }
            } else {
                // 其它情况忽略周相关字段计算
                if (Calendar.DAY_OF_WEEK == i) {
                    continue;
                }
            }

            truncate(calendar, i, modify);
        }

        if (truncateMillisecond) {
            calendar.set(Calendar.MILLISECOND, 0);
        }

        return calendar;
    }

    /**
     * 修改日期为某个时间字段起始时间
     *
     * @param date {@link Date}
     * @param type 时间字段
     * @return {@link DateTime}
     */
    public static DateTime truncate(Date date, Fields.Type type) {
        return new DateTime(truncate(toCalendar(date), type));
    }

    /**
     * 修改日期为某个时间字段起始时间
     *
     * @param calendar {@link Calendar}
     * @param type     时间字段
     * @return 原{@link Calendar}
     */
    public static Calendar truncate(Calendar calendar, Fields.Type type) {
        return ceiling(calendar, type.getValue(), Fields.Modify.TRUNCATE);
    }

    /**
     * 修改日期字段值
     *
     * @param calendar {@link Calendar}
     * @param field    字段，见{@link Calendar}
     * @param modify   {@link Fields.Modify}
     */
    private static void truncate(Calendar calendar, int field, Fields.Modify modify) {
        if (Calendar.HOUR == field) {
            // 修正小时。HOUR为12小时制，上午的结束时间为12:00，此处改为HOUR_OF_DAY: 23:59
            field = Calendar.HOUR_OF_DAY;
        }

        switch (modify) {
            case TRUNCATE:
                calendar.set(field, getBeginValue(calendar, field));
                break;
            case CEILING:
                calendar.set(field, getEndValue(calendar, field));
                break;
            case ROUND:
                int min = getBeginValue(calendar, field);
                int max = getEndValue(calendar, field);
                int href;
                if (Calendar.DAY_OF_WEEK == field) {
                    // 星期特殊处理，假设周一是第一天，中间的为周四
                    href = (min + 3) % 7;
                } else {
                    href = (max - min) / 2 + 1;
                }
                int value = calendar.get(field);
                calendar.set(field, (value < href) ? min : max);
                break;
        }
    }

    /**
     * 修改日期为某个时间字段四舍五入时间
     *
     * @param date {@link Date}
     * @param type 时间字段
     * @return {@link DateTime}
     */
    public static DateTime round(Date date, Fields.Type type) {
        return new DateTime(round(toCalendar(date), type));
    }

    /**
     * 修改日期为某个时间字段四舍五入时间
     *
     * @param calendar {@link Calendar}
     * @param type     时间字段
     * @return 原{@link Calendar}
     */
    public static Calendar round(Calendar calendar, Fields.Type type) {
        return ceiling(calendar, type.getValue(), Fields.Modify.ROUND);
    }

    /**
     * 判断两个日期相差的时长,只保留绝对值
     *
     * @param beginDate 起始日期
     * @param endDate   结束日期
     * @param units     相差的单位
     * @return 日期差
     */
    public static long between(Date beginDate, Date endDate, Fields.Units units) {
        return between(beginDate, endDate, units, true);
    }

    /**
     * 判断两个日期相差的时长
     *
     * @param beginDate 起始日期
     * @param endDate   结束日期
     * @param units     相差的单位
     * @param isAbs     日期间隔是否只保留绝对值正数
     * @return 日期差
     */
    public static long between(Date beginDate, Date endDate, Fields.Units units, boolean isAbs) {
        return new Between(beginDate, endDate, isAbs).between(units);
    }

    /**
     * 判断两个日期相差的毫秒数
     *
     * @param beginDate 起始日期
     * @param endDate   结束日期
     * @return 日期差
     */
    public static long betweenMs(Date beginDate, Date endDate) {
        return new Between(beginDate, endDate).between(Fields.Units.MILLISECOND);
    }

    /**
     * 判断两个日期相差的天数
     *
     * <pre>
     * 有时候我们计算相差天数的时候需要忽略时分秒
     * 比如：2016-02-01 23:59:59和2016-02-02 00:00:00相差一秒
     * 如果isReset为false相差天数为0
     * 如果isReset为true相差天数将被计算为1
     * </pre>
     *
     * @param beginDate 起始日期
     * @param endDate   结束日期
     * @param isReset   是否重置时间为起始时间
     * @return 日期差
     */
    public static long betweenDay(Date beginDate, Date endDate, boolean isReset) {
        if (isReset) {
            beginDate = beginOfDay(beginDate);
            endDate = beginOfDay(endDate);
        }
        return between(beginDate, endDate, Fields.Units.DAY);
    }

    /**
     * 计算两个日期相差月数
     * 在非重置情况下,如果起始日期的天大于结束日期的天,月数要少算1（不足1个月）
     *
     * @param beginDate 起始日期
     * @param endDate   结束日期
     * @param isReset   是否重置时间为起始时间(重置天时分秒)
     * @return 相差月数
     */
    public static long betweenMonth(Date beginDate, Date endDate, boolean isReset) {
        return new Between(beginDate, endDate).betweenMonth(isReset);
    }

    /**
     * 计算两个日期相差年数
     * 在非重置情况下,如果起始日期的月大于结束日期的月,年数要少算1（不足1年）
     *
     * @param beginDate 起始日期
     * @param endDate   结束日期
     * @param isReset   是否重置时间为起始时间(重置月天时分秒)
     * @return 相差年数
     */
    public static long betweenYear(Date beginDate, Date endDate, boolean isReset) {
        return new Between(beginDate, endDate).betweenYear(isReset);
    }

    /**
     * 偏移毫秒数
     *
     * @param date   日期
     * @param offset 偏移毫秒数,正数向未来偏移,负数向历史偏移
     * @return 偏移后的日期
     */
    public static DateTime offsetMillisecond(Date date, int offset) {
        return offset(date, Fields.Type.MILLISECOND, offset);
    }

    /**
     * 偏移秒数
     *
     * @param date   日期
     * @param offset 偏移秒数,正数向未来偏移,负数向历史偏移
     * @return 偏移后的日期
     */
    public static DateTime offsetSecond(Date date, int offset) {
        return offset(date, Fields.Type.SECOND, offset);
    }

    /**
     * 偏移分钟
     *
     * @param date   日期
     * @param offset 偏移分钟数,正数向未来偏移,负数向历史偏移
     * @return 偏移后的日期
     */
    public static DateTime offsetMinute(Date date, int offset) {
        return offset(date, Fields.Type.MINUTE, offset);
    }

    /**
     * 偏移小时
     *
     * @param date   日期
     * @param offset 偏移小时数,正数向未来偏移,负数向历史偏移
     * @return 偏移后的日期
     */
    public static DateTime offsetHour(Date date, int offset) {
        return offset(date, Fields.Type.HOUR_OF_DAY, offset);
    }

    /**
     * 偏移天
     *
     * @param date   日期
     * @param offset 偏移天数,正数向未来偏移,负数向历史偏移
     * @return 偏移后的日期
     */
    public static DateTime offsetDay(Date date, int offset) {
        return offset(date, Fields.Type.DAY_OF_YEAR, offset);
    }

    /**
     * 偏移周
     *
     * @param date   日期
     * @param offset 偏移周数,正数向未来偏移,负数向历史偏移
     * @return 偏移后的日期
     */
    public static DateTime offsetWeek(Date date, int offset) {
        return offset(date, Fields.Type.WEEK_OF_YEAR, offset);
    }

    /**
     * 偏移月
     *
     * @param date   日期
     * @param offset 偏移月数,正数向未来偏移,负数向历史偏移
     * @return 偏移后的日期
     */
    public static DateTime offsetMonth(Date date, int offset) {
        return offset(date, Fields.Type.MONTH, offset);
    }

    /**
     * 偏移月
     *
     * @param date   日期
     * @param offset 偏移月数,正数向未来偏移,负数向历史偏移
     * @return 偏移后的日期
     */
    public static DateTime offsetYear(Date date, int offset) {
        return offset(date, Fields.Type.YEAR, offset);
    }

    /**
     * 获取指定日期偏移指定时间后的时间
     *
     * @param date   基准日期
     * @param type   偏移的粒度大小(小时、天、月等)
     * @param offset 偏移量,正数为向后偏移,负数为向前偏移
     * @return 偏移后的日期
     */
    public static DateTime offset(Date date, Fields.Type type, int offset) {
        return new DateTime(date).offset(type, offset);
    }

    /**
     * 是否为本月最后一天
     *
     * @param date {@link Date}
     * @return 是否为本月最后一天
     */
    public static boolean isLastDayOfMonth(final Date date) {
        return date(date).isLastDayOfMonth();
    }

    /**
     * 获得本月的最后一天
     *
     * @param date {@link Date}
     * @return 天
     */
    public static int getLastDayOfMonth(final Date date) {
        return date(date).getLastDayOfMonth();
    }

    /**
     * 转换为{@link DateTime}对象
     *
     * @return 当前时间
     */
    public static DateTime date() {
        return new DateTime();
    }

    /**
     * {@link Date}类型时间转为{@link DateTime}
     *
     * @param date Long类型Date(Unix时间戳)
     * @return 时间对象
     */
    public static DateTime date(Date date) {
        if (date == null) {
            return null;
        }
        if (date instanceof DateTime) {
            return (DateTime) date;
        }
        return new DateTime(date);
    }

    /**
     * Long类型时间转为{@link DateTime}
     * 只支持毫秒级别时间戳，如果需要秒级别时间戳，请自行×1000L
     *
     * @param date Long类型Date(Unix时间戳)
     * @return 时间对象
     */
    public static DateTime date(long date) {
        return new DateTime(date);
    }

    /**
     * {@link Calendar}类型时间转为{@link DateTime}
     *
     * @param calendar {@link Calendar}，如果传入{@code null}，返回{@code null}
     * @return 时间对象
     */
    public static DateTime date(Calendar calendar) {
        if (calendar == null) {
            return null;
        }
        return new DateTime(calendar);
    }

    /**
     * {@link TemporalAccessor}类型时间转为{@link DateTime}
     * 始终根据已有{@link TemporalAccessor} 产生新的{@link DateTime}对象
     *
     * @param temporalAccessor {@link TemporalAccessor},常用子类： {@link LocalDateTime}、 LocalDate，如果传入{@code null}，返回{@code null}
     * @return 时间对象
     */
    public static DateTime date(TemporalAccessor temporalAccessor) {
        if (temporalAccessor == null) {
            return null;
        }
        return new DateTime(temporalAccessor);
    }

    /**
     * 当前时间,格式 yyyy-MM-dd HH:mm:ss
     *
     * @return 当前时间的标准形式字符串
     */
    public static String now() {
        return format(date());
    }

    /**
     * 当前日期，格式 yyyy-MM-dd
     *
     * @return 当前日期的标准形式字符串
     */
    public static String today() {
        return formatDate(date());
    }

    /**
     * 当前时间毫秒数
     *
     * @return 当前时间毫秒数
     */
    public static long timestamp() {
        return timestamp(false);
    }

    /**
     * 当前时间long
     *
     * @param isNano 是否为高精度时间
     * @return 时间
     */
    public static long timestamp(boolean isNano) {
        return isNano ? System.nanoTime() : System.currentTimeMillis();
    }

    /**
     * 当前年份
     *
     * @return 今年
     */
    public static int thisYear() {
        return offsetYear(date(), 0).year();
    }

    /**
     * 当前月份
     *
     * @return 当前月份
     */
    public static int thisMonth() {
        return offsetMonth(date(), 0).month();
    }

    /**
     * 当前日期是星期几
     *
     * @return 当前日期是星期几
     */
    public static int thisWeek() {
        return offsetWeek(date(), 0).dayOfWeek();
    }

    /**
     * 明天日期
     *
     * @return Date
     */
    public static Date tomorrow() {
        return offsetDay(date(), 1);
    }

    /**
     * 下周日期
     *
     * @return Date
     */
    public static Date nextWeek() {
        return offsetWeek(date(), 1);
    }

    /**
     * 下月日期
     *
     * @return Date
     */
    public static Date nextMonth() {
        return offsetMonth(date(), 1);
    }

    /**
     * 明年日期
     *
     * @return Date
     */
    public static Date nextYear() {
        return offsetYear(date(), 1);
    }

    /**
     * 昨天日期
     *
     * @return Date
     */
    public static Date yesterday() {
        return offsetDay(date(), -1);
    }

    /**
     * 上周日期
     *
     * @return Date
     */
    public static Date lastWeek() {
        return offsetWeek(date(), -1);
    }

    /**
     * 上月日期
     *
     * @return Date
     */
    public static Date lastMonth() {
        return offsetMonth(date(), -1);
    }

    /**
     * 去年日期
     *
     * @return Date
     */
    public static Date lastYear() {
        return offsetYear(date(), -1);
    }

}