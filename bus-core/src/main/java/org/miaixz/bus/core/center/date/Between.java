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
package org.miaixz.bus.core.center.date;

import org.miaixz.bus.core.center.date.culture.en.Units;
import org.miaixz.bus.core.center.date.format.FormatPeriod;
import org.miaixz.bus.core.lang.Assert;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.util.Date;

/**
 * 日期间隔
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Between implements Serializable {

    private static final long serialVersionUID = -1L;

    /**
     * 开始日期
     */
    private final Date begin;
    /**
     * 结束日期
     */
    private final Date end;

    /**
     * 构造 在前的日期做为起始时间，在后的做为结束时间，间隔只保留绝对值正数
     *
     * @param begin 起始时间
     * @param end   结束时间
     */
    public Between(final Date begin, final Date end) {
        this(begin, end, true);
    }

    /**
     * 构造 在前的日期做为起始时间，在后的做为结束时间
     *
     * @param begin 起始时间
     * @param end   结束时间
     * @param isAbs 日期间隔是否只保留绝对值正数
     */
    public Between(final Date begin, final Date end, final boolean isAbs) {
        Assert.notNull(begin, "Begin date is null !");
        Assert.notNull(end, "End date is null !");

        if (isAbs && begin.after(end)) {
            // 间隔只为正数的情况下，如果开始日期晚于结束日期，置换之
            this.begin = end;
            this.end = begin;
        } else {
            this.begin = begin;
            this.end = end;
        }
    }

    /**
     * 创建 在前的日期做为起始时间，在后的做为结束时间，间隔只保留绝对值正数
     *
     * @param begin 起始时间
     * @param end   结束时间
     * @return Between
     */
    public static Between of(final Date begin, final Date end) {
        return new Between(begin, end);
    }

    /**
     * 创建 在前的日期做为起始时间，在后的做为结束时间，间隔只保留绝对值正数
     *
     * @param begin 起始时间
     * @param end   结束时间
     * @param isAbs 日期间隔是否只保留绝对值正数
     * @return Between
     */
    public static Between of(final Date begin, final Date end, final boolean isAbs) {
        return new Between(begin, end, isAbs);
    }

    /**
     * 获取两个日期的差，如果结束时间早于开始时间，获取结果为负。 返回结果为{@link Duration}对象，通过调用toXXX方法返回相差单位
     *
     * @param startTimeInclude 开始时间（包含）
     * @param endTimeExclude   结束时间（不包含）
     * @return 时间差 {@link Duration}对象
     */
    public static Duration between(final Temporal startTimeInclude, final Temporal endTimeExclude) {
        return Duration.between(startTimeInclude, endTimeExclude);
    }

    /**
     * 获取两个日期的差，如果结束时间早于开始时间，获取结果为负 返回结果为{@link Duration}对象，通过调用toXXX方法返回相差单位
     *
     * @param startTimeInclude 开始时间（包含）
     * @param endTimeExclude   结束时间（不包含）
     * @return 时间差 {@link Duration}对象
     * @see Between#between(Temporal, Temporal)
     */
    public static Duration between(final LocalDateTime startTimeInclude, final LocalDateTime endTimeExclude) {
        return between(startTimeInclude, endTimeExclude);
    }

    /**
     * 获取两个日期的差，如果结束时间早于开始时间，获取结果为负。 返回结果为时间差的long值
     *
     * @param startTimeInclude 开始时间（包括）
     * @param endTimeExclude   结束时间（不包括）
     * @param unit             时间差单位
     * @return 时间差
     */
    public static long between(final Temporal startTimeInclude, final Temporal endTimeExclude, final ChronoUnit unit) {
        return unit.between(startTimeInclude, endTimeExclude);
    }

    /**
     * 获取两个日期的差，如果结束时间早于开始时间，获取结果为负 返回结果为时间差的long值
     *
     * @param startTimeInclude 开始时间（包括）
     * @param endTimeExclude   结束时间（不包括）
     * @param unit             时间差单位
     * @return 时间差
     */
    public static long between(final LocalDateTime startTimeInclude, final LocalDateTime endTimeExclude,
            final ChronoUnit unit) {
        return between(startTimeInclude, endTimeExclude, unit);
    }

    /**
     * 获取两个日期的表象时间差，如果结束时间早于开始时间，获取结果为负。 比如2011年2月1日，和2021年8月11日，日相差了10天，月相差6月
     *
     * @param startTimeInclude 开始时间（包括）
     * @param endTimeExclude   结束时间（不包括）
     * @return 时间差
     */
    public static Period between(final LocalDate startTimeInclude, final LocalDate endTimeExclude) {
        return Period.between(startTimeInclude, endTimeExclude);
    }

    /**
     * 判断两个日期相差的时长 返回 给定单位的时长差
     *
     * @param unit 相差的单位：相差 天{@link Units#DAY}、小时{@link Units#HOUR} 等
     * @return 时长差
     */
    public long between(final Units unit) {
        final long diff = end.getTime() - begin.getTime();
        return diff / unit.getMillis();
    }

    /**
     * 计算两个日期相差月数 在非重置情况下，如果起始日期的天大于结束日期的天，月数要少算1（不足1个月）
     *
     * @param isReset 是否重置时间为起始时间（重置天时分秒）
     * @return 相差月数
     */
    public long betweenMonth(final boolean isReset) {
        final java.util.Calendar beginCal = Calendar.calendar(begin);
        final java.util.Calendar endCal = Calendar.calendar(end);

        final int betweenYear = endCal.get(java.util.Calendar.YEAR) - beginCal.get(java.util.Calendar.YEAR);
        final int betweenMonthOfYear = endCal.get(java.util.Calendar.MONTH) - beginCal.get(java.util.Calendar.MONTH);

        final int result = betweenYear * 12 + betweenMonthOfYear;
        if (!isReset) {
            endCal.set(java.util.Calendar.YEAR, beginCal.get(java.util.Calendar.YEAR));
            endCal.set(java.util.Calendar.MONTH, beginCal.get(java.util.Calendar.MONTH));
            final long between = endCal.getTimeInMillis() - beginCal.getTimeInMillis();
            if (between < 0) {
                return result - 1;
            }
        }
        return result;
    }

    /**
     * 计算两个日期相差年数 在非重置情况下，如果起始日期的月大于结束日期的月，年数要少算1（不足1年）
     *
     * @param isReset 是否重置时间为起始时间（重置月天时分秒）
     * @return 相差年数
     */
    public long betweenYear(final boolean isReset) {
        final java.util.Calendar beginCal = Calendar.calendar(begin);
        final java.util.Calendar endCal = Calendar.calendar(end);

        final int result = endCal.get(java.util.Calendar.YEAR) - beginCal.get(java.util.Calendar.YEAR);
        if (false == isReset) {
            final int beginMonthBase0 = beginCal.get(java.util.Calendar.MONTH);
            final int endMonthBase0 = endCal.get(java.util.Calendar.MONTH);
            if (beginMonthBase0 < endMonthBase0) {
                return result;
            } else if (beginMonthBase0 > endMonthBase0) {
                return result - 1;
            } else if (java.util.Calendar.FEBRUARY == beginMonthBase0 && Calendar.isLastDayOfMonth(beginCal)
                    && Calendar.isLastDayOfMonth(endCal)) {
                // 考虑闰年的2月情况
                // 两个日期都位于2月的最后一天，此时月数按照相等对待，此时都设置为1号
                beginCal.set(java.util.Calendar.DAY_OF_MONTH, 1);
                endCal.set(java.util.Calendar.DAY_OF_MONTH, 1);
            }

            endCal.set(java.util.Calendar.YEAR, beginCal.get(java.util.Calendar.YEAR));
            final long between = endCal.getTimeInMillis() - beginCal.getTimeInMillis();
            if (between < 0) {
                return result - 1;
            }
        }
        return result;
    }

    /**
     * 获取开始时间
     *
     * @return 获取开始时间
     */
    public Date getBegin() {
        return begin;
    }

    /**
     * 获取结束日期
     *
     * @return 结束日期
     */
    public Date getEnd() {
        return end;
    }

    /**
     * 格式化输出时间差
     *
     * @param unit  日期单位
     * @param level 级别
     * @return 字符串
     */
    public String toString(final Units unit, final FormatPeriod.Level level) {
        return FormatPeriod.of(between(unit), level).format();
    }

    /**
     * 格式化输出时间差
     *
     * @param level 级别
     * @return 字符串
     */
    public String toString(final FormatPeriod.Level level) {
        return toString(Units.MS, level);
    }

    @Override
    public String toString() {
        return toString(FormatPeriod.Level.MILLISECOND);
    }

}
