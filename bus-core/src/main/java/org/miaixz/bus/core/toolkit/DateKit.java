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
package org.miaixz.bus.core.toolkit;

import org.miaixz.bus.core.date.*;
import org.miaixz.bus.core.lang.Fields;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 时间工具类
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class DateKit extends Almanac {

    /**
     * 计时,常用于记录某段代码的执行时间,单位：纳秒
     *
     * @param preTime 之前记录的时间
     * @return 时间差, 纳秒
     */
    public static long spendNt(long preTime) {
        return System.nanoTime() - preTime;
    }

    /**
     * 计时,常用于记录某段代码的执行时间,单位：毫秒
     *
     * @param preTime 之前记录的时间
     * @return 时间差, 毫秒
     */
    public static long spendMs(long preTime) {
        return System.currentTimeMillis() - preTime;
    }

    /**
     * 生日转为年龄，计算法定年龄
     *
     * @param birthDay 生日，标准日期字符串
     * @return 年龄
     */
    public static int ageOfNow(String birthDay) {
        return ageOfNow(parse(birthDay));
    }

    /**
     * 生日转为年龄，计算法定年龄
     *
     * @param birthDay 生日
     * @return 年龄
     */
    public static int ageOfNow(Date birthDay) {
        return getAge(birthDay, date());
    }

    /**
     * 返回文字描述的日期
     *
     * @param date 日期
     * @return 日期
     */
    public static String getLastTime(Date date) {
        if (null == date) {
            return null;
        }
        long diff = System.currentTimeMillis() - date.getTime();
        long r;
        if (diff > Fields.Units.WEEK.getUnit()) {
            r = (diff / Fields.Units.WEEK.getUnit());
            return r + "周前";
        }
        if (diff > Fields.Units.DAY.getUnit()) {
            r = (diff / Fields.Units.DAY.getUnit());
            return r + "天前";
        }
        if (diff > Fields.Units.HOUR.getUnit()) {
            r = (diff / Fields.Units.HOUR.getUnit());
            return r + "个小时前";
        }
        if (diff > Fields.Units.MINUTE.getUnit()) {
            r = (diff / Fields.Units.MINUTE.getUnit());
            return r + "分钟前";
        }
        return "刚刚";
    }

    /**
     * 创建日期范围生成器
     *
     * @param start 起始日期时间
     * @param end   结束日期时间
     * @param type  步进单位
     * @return {@link Boundary}
     */
    public static Boundary range(Date start, Date end, final Fields.Type type) {
        return new Boundary(start, end, type);
    }

    /**
     * 两个时间区间取交集
     *
     * @param start 开始区间
     * @param end   结束区间
     * @return true 包含
     */
    public static List<DateTime> rangeContains(Boundary start, Boundary end) {
        List<DateTime> startDateTimes = CollKit.newArrayList((Iterable<DateTime>) start);
        List<DateTime> endDateTimes = CollKit.newArrayList((Iterable<DateTime>) end);
        return startDateTimes.stream().filter(endDateTimes::contains).collect(Collectors.toList());
    }

    /**
     * 两个时间区间取差集(end - start)
     *
     * @param start 开始区间
     * @param end   结束区间
     * @return true 包含
     */
    public static List<DateTime> rangeNotContains(Boundary start, Boundary end) {
        List<DateTime> startDateTimes = CollKit.newArrayList((Iterable<DateTime>) start);
        List<DateTime> endDateTimes = CollKit.newArrayList((Iterable<DateTime>) end);
        return endDateTimes.stream().filter(item -> !startDateTimes.contains(item)).collect(Collectors.toList());
    }

    /**
     * 按日期范围遍历，执行 function
     *
     * @param start 起始日期时间（包括）
     * @param end   结束日期时间
     * @param type  步进单位
     * @param func  每次遍历要执行的 function
     * @param <T>   Date经过函数处理结果类型
     * @return 结果列表
     */
    public static <T> List<T> rangeFunc(Date start, Date end, final Fields.Type type, Function<Date, T> func) {
        if (start == null || end == null || start.after(end)) {
            return Collections.emptyList();
        }
        ArrayList<T> list = new ArrayList<>();
        for (DateTime date : range(start, end, type)) {
            list.add(func.apply(date));
        }
        return list;
    }

    /**
     * 按日期范围遍历，执行 consumer
     *
     * @param start    起始日期时间（包括）
     * @param end      结束日期时间
     * @param type     步进单位
     * @param consumer 每次遍历要执行的 consumer
     */
    public static void rangeConsume(Date start, Date end, final Fields.Type type, Consumer<Date> consumer) {
        if (start == null || end == null || start.after(end)) {
            return;
        }
        range(start, end, type).forEach(consumer);
    }

    /**
     * 根据步进单位获取起始日期时间和结束日期时间的时间区间集合
     *
     * @param start 起始日期时间
     * @param end   结束日期时间
     * @param type  步进单位
     * @return {@link Boundary}
     */
    public static List<DateTime> rangeToList(Date start, Date end, Fields.Type type) {
        return CollKit.newArrayList((Iterable<DateTime>) range(start, end, type));
    }

    /**
     * 根据步进单位和步进获取起始日期时间和结束日期时间的时间区间集合
     *
     * @param start 起始日期时间
     * @param end   结束日期时间
     * @param type  步进单位
     * @param step  步进
     * @return {@link Boundary}
     */
    public static List<DateTime> rangeToList(Date start, Date end, final Fields.Type type, int step) {
        return CollKit.newArrayList((Iterable<DateTime>) new Boundary(start, end, type, step));
    }

    /**
     * 通过公历构造
     *
     * @return {@link Lunar}
     */
    public Lunar getLunar() {
        return new Lunar(date());
    }

    /**
     * 通过公历构造
     *
     * @param calendar 　公历日期
     * @return {@link Lunar}
     */
    public Lunar getLunar(Calendar calendar) {
        return new Lunar(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH),
                calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), calendar.get(Calendar.SECOND));
    }

    /**
     * 通过年、月、日构造
     *
     * @param year  农历年
     * @param month 农历月份,范围1-12
     * @param day   农历日1-30
     * @return {@link Lunar}
     */
    public Lunar getLunar(int year, int month, int day) {
        return getLunar(year, month, day, 0, 0);
    }

    /**
     * 通过年、月、日构造
     *
     * @param year   农历年
     * @param month  农历月份,范围1-12
     * @param day    农历日1-30
     * @param hour   小时
     * @param minute 分钟
     * @return {@link Lunar}
     */
    public Lunar getLunar(int year, int month, int day, int hour, int minute) {
        return getLunar(year, month, day, hour, minute, 0);
    }

    /**
     * 通过年、月、日构造
     *
     * @param year   农历年
     * @param month  农历月份,范围1-12
     * @param day    农历日1-30
     * @param hour   小时
     * @param minute 分钟
     * @param second 秒
     * @return {@link Lunar}
     */
    public Lunar getLunar(int year, int month, int day, int hour, int minute, int second) {
        return new Lunar(year, month, day, hour, minute, second);
    }

    /**
     * 通过公历构造
     *
     * @return {@link Solar}
     */
    public Solar getSolar() {
        return new Solar(date());
    }

    /**
     * 通过公历构造
     *
     * @param calendar 　公历日期
     * @return {@link Solar}
     */
    public Solar getSolar(Calendar calendar) {
        return new Solar(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH),
                calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), calendar.get(Calendar.SECOND));
    }

    /**
     * 通过年、月、日构造
     *
     * @param year  农历年
     * @param month 农历月份,范围1-12
     * @param day   农历日1-30
     * @return {@link Solar}
     */
    public Solar getSolar(int year, int month, int day) {
        return getSolar(year, month, day, 0, 0);
    }

    /**
     * 通过年、月、日构造
     *
     * @param year   农历年
     * @param month  农历月份,范围1-12
     * @param day    农历日1-30
     * @param hour   小时
     * @param minute 分钟
     * @return {@link Solar}
     */
    public Solar getSolar(int year, int month, int day, int hour, int minute) {
        return getSolar(year, month, day, hour, minute, 0);
    }

    /**
     * 通过年、月、日构造
     *
     * @param year   农历年
     * @param month  农历月份,范围1-12
     * @param day    农历日1-30
     * @param hour   小时
     * @param minute 分钟
     * @param second 秒
     * @return {@link Solar}
     */
    public Solar getSolar(int year, int month, int day, int hour, int minute, int second) {
        return new Solar(year, month, day, hour, minute, second);
    }

}
