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

import org.miaixz.bus.core.date.calendar.DayWrapper;
import org.miaixz.bus.core.date.calendar.MonthWrapper;
import org.miaixz.bus.core.date.calendar.NonWrapper;
import org.miaixz.bus.core.date.calendar.YearWrapper;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.toolkit.CollKit;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 日历类
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Kalendar {

    /**
     * 生成指定年月的日历， 包含农历和所有节假日信息
     *
     * @param year  年
     * @param month 月
     * @return {@link NonWrapper}
     */
    public static NonWrapper calendar(int year, int month) {
        return calendar(year, month, true, true, null);
    }

    /**
     * 生成指定年月的日历，包含公历节假日信息
     *
     * @param year        年
     * @param month       月
     * @param dateTypeMap 日期类型，0休息日，1等其他为工作日，比如dateTypeMap.put("2020-08-07", 0);
     * @return {@link NonWrapper}
     */
    public static NonWrapper calendar(int year, int month, Map<String, Integer> dateTypeMap) {
        return calendar(year, month, false, true, dateTypeMap);
    }

    /**
     * 生成指定年月的日历，包含农历和所有节假日信息
     *
     * @param year             年
     * @param month            月
     * @param includeLunarDate 包含农历
     * @param includeHoliday   包含节日
     * @param dateTypeMap      日期类型，0休息日，1等其他为工作日，比如dateTypeMap.put("2020-08-07", 0);
     * @return {@link NonWrapper}
     */
    private static NonWrapper calendar(int year, int month, boolean includeLunarDate, boolean includeHoliday, Map<String, Integer> dateTypeMap) {
        YearMonth yearMonth = YearMonth.of(year, month);
        NonWrapper nonWrapper = new NonWrapper();
        Map<String, DayWrapper> dayMap = new ConcurrentHashMap<>(Normal._64);
        List<DayWrapper> dayList = new ArrayList<>();

        List<LocalDateTime> localDateTimeList = Almanac.getLocalDateTimeList(YearMonth.of(year, month));
        if (CollKit.isEmpty(localDateTimeList)) {
            return nonWrapper;
        }
        List<DayWrapper> dayWrapperList = new ArrayList<>();
        localDateTimeList.stream().forEach(localDateTime -> {
            DayWrapper dayWrapper = new DayWrapper(localDateTime, includeLunarDate, includeHoliday);
            dayWrapperList.add(dayWrapper);
            dayMap.put(Formatter.format(localDateTime), dayWrapper);
            dayList.add(dayWrapper);
        });

        if (CollKit.isNotEmpty(dateTypeMap) && CollKit.isNotEmpty(dayMap)) {
            dateTypeMap.forEach((k, v) -> {
                if (dayMap.containsKey(k)) {
                    dayMap.get(k).setDateType(v);
                }
            });
        }

        return new NonWrapper(
                CollKit.newArrayList(new YearWrapper(year,
                        CollKit.newArrayList(
                                new MonthWrapper(month, dayWrapperList, yearMonth.lengthOfMonth())))),
                dayMap,
                dayList);

    }

    /**
     * 生成指定年月的日历，包含公历节假日信息
     *
     * @param year        年
     * @param dateTypeMap 日期类型，0休息日，1等其他为工作日，比如dateTypeMap.put("2020-08-07", 0);
     * @return {@link NonWrapper}
     */
    public static NonWrapper calendar(int year, Map<String, Integer> dateTypeMap) {
        return calendar(year, false, true, dateTypeMap);
    }

    /**
     * 生成指定年月的日历，包含农历和所有节假日信息
     *
     * @param year 年
     * @return {@link NonWrapper}
     */
    public static NonWrapper calendar(int year) {
        return calendar(year, true, true, null);
    }

    /**
     * 生成指定年月的日历，包含农历和所有节假日信息
     *
     * @param year           年
     * @param includeLunar   包含农历
     * @param includeHoliday 包含节日
     * @param dateTypeMap    日期类型，0休息日，1等其他为工作日，比如dateTypeMap.put("2020-08-07", 0);
     * @return {@link NonWrapper}
     */
    private static NonWrapper calendar(int year, boolean includeLunar, boolean includeHoliday, Map<String, Integer> dateTypeMap) {
        Map<String, DayWrapper> dayMap = new ConcurrentHashMap<>(Normal._512);
        List<DayWrapper> dayList = new ArrayList<>();
        List<MonthWrapper> monthWrapperList = new ArrayList<>();

        for (int i = 1; i <= 12; i++) {
            YearMonth yearMonth = YearMonth.of(year, i);
            List<LocalDateTime> localDateTimeList = Almanac.getLocalDateTimeList(YearMonth.of(year, i));
            if (CollKit.isEmpty(localDateTimeList)) {
                continue;
            }
            List<DayWrapper> dayWrapperList = new ArrayList<>();
            localDateTimeList.stream().forEach(localDateTime -> {
                DayWrapper dayWrapper = new DayWrapper(localDateTime, includeLunar, includeHoliday);
                dayWrapperList.add(dayWrapper);
                dayMap.put(Formatter.format(localDateTime), dayWrapper);
                dayList.add(dayWrapper);
            });

            MonthWrapper monthWrapper = new MonthWrapper(i, dayWrapperList, yearMonth.lengthOfMonth());
            monthWrapperList.add(monthWrapper);
        }

        if (CollKit.isNotEmpty(dateTypeMap) && CollKit.isNotEmpty(dayMap)) {
            dateTypeMap.forEach((k, v) -> {
                if (dayMap.containsKey(k)) {
                    dayMap.get(k).setDateType(v);
                }
            });
        }
        return new NonWrapper(CollKit.newArrayList(new YearWrapper(year, monthWrapperList)), dayMap, dayList);
    }

    /**
     * @param year   年
     * @param month  月
     * @param day    日
     * @param hour   小时
     * @param minute 分钟
     * @param second 秒
     * @return {@link Calendar}
     */
    public static Calendar calendar(int year, int month, int day, int hour, int minute, int second) {
        Calendar c = Calendar.getInstance();
        c.set(year, month - 1, day, hour, minute, second);
        c.set(Calendar.MILLISECOND, 0);
        if (0 == year) {
            c.add(Calendar.YEAR, 1);
        }
        return c;
    }

    /**
     * @param year  年
     * @param month 月
     * @param day   日
     * @return {@link Calendar}
     */
    public static Calendar calendar(int year, int month, int day) {
        return calendar(year, month, day, 0, 0, 0);
    }

    /**
     * @param date 日期
     * @return {@link Calendar}
     */
    public static Calendar calendar(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.set(Calendar.MILLISECOND, 0);
        return c;
    }

}
