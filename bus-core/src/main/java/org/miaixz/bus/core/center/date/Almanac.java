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
package org.miaixz.bus.core.center.date;

import org.miaixz.bus.core.lang.Fields;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.chrono.ChronoLocalDate;
import java.time.chrono.ChronoLocalDateTime;
import java.time.temporal.*;

/**
 * 日期计算类
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Almanac extends Resolver {

    /**
     * 当前日期是否在日期指定范围内
     * 起始日期和结束日期可以互换
     *
     * @param date      被检查的日期
     * @param beginDate 起始日期（包含）
     * @param endDate   结束日期（包含）
     * @return 是否在范围内
     */
    public static boolean isIn(final TemporalAccessor date, final TemporalAccessor beginDate, final TemporalAccessor endDate) {
        return isIn(date, beginDate, endDate, true, true);
    }

    /**
     * 当前日期是否在日期指定范围内
     * 起始日期和结束日期可以互换
     * 通过includeBegin, includeEnd参数控制日期范围区间是否为开区间，例如：传入参数：includeBegin=true, includeEnd=false，
     * 则本方法会判断 date ∈ (beginDate, endDate] 是否成立
     *
     * @param date         被检查的日期
     * @param beginDate    起始日期
     * @param endDate      结束日期
     * @param includeBegin 时间范围是否包含起始日期
     * @param includeEnd   时间范围是否包含结束日期
     * @return 是否在范围内
     */
    public static boolean isIn(final TemporalAccessor date, final TemporalAccessor beginDate, final TemporalAccessor endDate,
                               final boolean includeBegin, final boolean includeEnd) {
        if (date == null || beginDate == null || endDate == null) {
            throw new IllegalArgumentException("参数不可为null");
        }

        final long thisMills = toEpochMilli(date);
        final long beginMills = toEpochMilli(beginDate);
        final long endMills = toEpochMilli(endDate);
        final long rangeMin = Math.min(beginMills, endMills);
        final long rangeMax = Math.max(beginMills, endMills);

        // 先判断是否满足 date ∈ (beginDate, endDate)
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
     * @return true 表示时间有重合
     */
    public static boolean isOverlap(final ChronoLocalDateTime<?> realStartTime, final ChronoLocalDateTime<?> realEndTime,
                                    final ChronoLocalDateTime<?> startTime, final ChronoLocalDateTime<?> endTime) {
        // x>b||a>y 无交集
        // 则有交集的逻辑为 !(x>b||a>y)
        // 根据德摩根公式，可化简为 x<=b && a<=y 即 realStartTime<=endTime && startTime<=realEndTime
        return realStartTime.compareTo(endTime) <= 0 && startTime.compareTo(realEndTime) <= 0;
    }

    /**
     * 比较两个日期是否为同一天
     *
     * @param date1 日期1
     * @param date2 日期2
     * @return 是否为同一天
     */
    public static boolean isSameDay(final ChronoLocalDateTime<?> date1, final ChronoLocalDateTime<?> date2) {
        return date1 != null && date2 != null && date1.toLocalDate().isEqual(date2.toLocalDate());
    }

    /**
     * 比较两个日期是否为同一天
     *
     * @param date1 日期1
     * @param date2 日期2
     * @return 是否为同一天
     */
    public static boolean isSameDay(final ChronoLocalDate date1, final ChronoLocalDate date2) {
        return date1 != null && date2 != null && date1.isEqual(date2);
    }

    /**
     * 是否为周末（周六或周日）
     *
     * @param localDateTime 判定的日期{@link LocalDateTime}
     * @return 是否为周末（周六或周日）
     */
    public static boolean isWeekend(final LocalDateTime localDateTime) {
        return isWeekend(localDateTime.toLocalDate());
    }

    /**
     * 是否为周末（周六或周日）
     *
     * @param localDate 判定的日期{@link LocalDate}
     * @return 是否为周末（周六或周日）
     */
    public static boolean isWeekend(final LocalDate localDate) {
        final DayOfWeek dayOfWeek = localDate.getDayOfWeek();
        return DayOfWeek.SATURDAY == dayOfWeek || DayOfWeek.SUNDAY == dayOfWeek;
    }

    /**
     * 日期偏移,根据field不同加不同值（偏移会修改传入的对象）
     *
     * @param time   {@link LocalDateTime}
     * @param number 偏移量，正数为向后偏移，负数为向前偏移
     * @param field  偏移单位，见{@link ChronoUnit}，不能为null
     * @return 偏移后的日期时间
     */
    public static LocalDateTime offset(final LocalDateTime time, final long number, final TemporalUnit field) {
        return offset(time, number, field);
    }

    /**
     * 日期偏移,根据field不同加不同值（偏移会修改传入的对象）
     *
     * @param <T>    日期类型，如LocalDate或LocalDateTime
     * @param time   {@link Temporal}
     * @param number 偏移量，正数为向后偏移，负数为向前偏移
     * @param field  偏移单位，见{@link ChronoUnit}，不能为null
     * @return 偏移后的日期时间
     */
    public static <T extends Temporal> T offset(final T time, final long number, final TemporalUnit field) {
        if (null == time) {
            return null;
        }

        return (T) time.plus(number, field);
    }

    /**
     * 修改为一天的开始时间，例如：2020-02-02 00:00:00,000
     *
     * @param time 日期时间
     * @return 一天的开始时间
     */
    public static LocalDateTime beginOfDay(final LocalDateTime time) {
        return time.with(LocalTime.MIN);
    }

    /**
     * 修改为一天的结束时间，例如：
     * <ul>
     * 	<li>毫秒不归零：2020-02-02 23:59:59,999</li>
     * 	<li>毫秒归零：2020-02-02 23:59:59,000</li>
     * </ul>
     *
     * @param time                日期时间
     * @param truncateMillisecond 是否毫秒归零
     * @return 一天的结束时间
     */
    public static LocalDateTime endOfDay(final LocalDateTime time, final boolean truncateMillisecond) {
        return time.with(max(truncateMillisecond));
    }

    /**
     * 修改为月初的开始时间，例如：2020-02-01 00:00:00,000
     *
     * @param time 日期时间
     * @return 月初的开始时间
     */
    public static LocalDateTime beginOfMonth(final LocalDateTime time) {
        return beginOfDay(time).with(TemporalAdjusters.firstDayOfMonth());
    }

    /**
     * 修改为月底的结束时间
     *
     * @param time                日期时间
     * @param truncateMillisecond 是否毫秒归零
     * @return 月底的结束时间
     */
    public static LocalDateTime endOfMonth(final LocalDateTime time, final boolean truncateMillisecond) {
        return endOfDay(time, truncateMillisecond).with(TemporalAdjusters.lastDayOfMonth());
    }

    /**
     * 修改为一年的开始时间，例如：2020-01-01 00:00:00,000
     *
     * @param time 日期时间
     * @return 一年的开始时间
     */
    public static LocalDateTime beginOfYear(final LocalDateTime time) {
        return beginOfDay(time).with(TemporalAdjusters.firstDayOfYear());
    }

    /**
     * 修改为一年的结束时间
     *
     * @param time                日期时间
     * @param truncateMillisecond 是否毫秒归零
     * @return 一年的结束时间
     */
    public static LocalDateTime endOfYear(final LocalDateTime time, final boolean truncateMillisecond) {
        return endOfDay(time, truncateMillisecond).with(TemporalAdjusters.lastDayOfYear());
    }

    /**
     * 获取{@link LocalDate}对应的星期值
     *
     * @param localDate 日期{@link LocalDate}
     * @return {@link Fields.Week}
     */
    public static Fields.Week dayOfWeek(final LocalDate localDate) {
        return Fields.Week.of(localDate.getDayOfWeek());
    }

    /**
     * 获得指定日期是所在年份的第几周，如：
     * <ul>
     *     <li>如果一年的第一天是星期一，则第一周从第一天开始，没有零周</li>
     *     <li>如果一年的第二天是星期一，则第一周从第二天开始，而第一天在零周</li>
     *     <li>如果一年的第4天是星期一，则第一周从第4天开始，第1至第3天在零周</li>
     *     <li>如果一年的第5天是星期一，则第二周从第5天开始，第1至第4天在第一周</li>
     * </ul>
     *
     * @param date 日期（{@link LocalDate} 或者 {@link LocalDateTime}等）
     * @return 所在年的第几周
     */
    public static int weekOfYear(final TemporalAccessor date) {
        return get(date, WeekFields.ISO.weekOfYear());
    }

    /**
     * 偏移到指定的周几
     *
     * @param temporal   日期或者日期时间
     * @param dayOfWeek  周几
     * @param <T>        日期类型，如LocalDate或LocalDateTime
     * @param isPrevious 是否向前偏移，{@code true}向前偏移，{@code false}向后偏移。
     * @return 偏移后的日期
     */
    public <T extends Temporal> T offset(final T temporal, final DayOfWeek dayOfWeek, final boolean isPrevious) {
        return (T) temporal.with(isPrevious ? TemporalAdjusters.previous(dayOfWeek) : TemporalAdjusters.next(dayOfWeek));
    }

}
