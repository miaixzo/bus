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
package org.miaixz.bus.cron.pattern;

import java.time.LocalDateTime;
import java.util.*;

import org.miaixz.bus.core.center.date.Calendar;
import org.miaixz.bus.core.center.date.culture.en.Week;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.xyz.CompareKit;
import org.miaixz.bus.core.xyz.DateKit;
import org.miaixz.bus.cron.pattern.matcher.PatternMatcher;
import org.miaixz.bus.cron.pattern.parser.PatternParser;

/**
 * 定时任务表达式 表达式类似于Linux的crontab表达式，表达式使用空格分成5个部分，按顺序依次为：
 * <ol>
 * <li><strong>分</strong> ：范围：0~59</li>
 * <li><strong>时</strong> ：范围：0~23</li>
 * <li><strong>日</strong> ：范围：1~31，<strong>"L"</strong> 表示月的最后一天</li>
 * <li><strong>月</strong> ：范围：1~12，同时支持不区分大小写的别名："jan","feb", "mar", "apr", "may","jun", "jul", "aug", "sep","oct",
 * "nov", "dec"</li>
 * <li><strong>周</strong> ：范围：0 (Sunday)~6(Saturday)，7也可以表示周日，同时支持不区分大小写的别名："sun","mon", "tue", "wed", "thu","fri",
 * "sat"，<strong>"L"</strong> 表示周六</li>
 * </ol>
 * <p>
 * 为了兼容Quartz表达式，同时支持6位和7位表达式，其中：
 *
 * <pre>
 * 当为6位时，第一位表示<strong>秒</strong> ，范围0~59，但是第一位不做匹配
 * 当为7位时，最后一位表示<strong>年</strong> ，范围1970~2099，但是第7位不做解析，也不做匹配
 * </pre>
 * <p>
 * 当定时任务运行到的时间匹配这些表达式后，任务被启动。 注意：
 *
 * <pre>
 * 当isMatchSecond为{@code
 * true
 * }时才会匹配秒部分
 * 默认都是关闭的
 * </pre>
 * <p>
 * 对于每一个子表达式，同样支持以下形式：
 * <ul>
 * <li><strong>*</strong> ：表示匹配这个位置所有的时间</li>
 * <li><strong>?</strong> ：表示匹配这个位置任意的时间（与"*"作用一致）</li>
 * <li><strong>*&#47;2</strong> ：表示间隔时间，例如在分上，表示每两分钟，同样*可以使用数字列表代替，逗号分隔</li>
 * <li><strong>2-8</strong> ：表示连续区间，例如在分上，表示2,3,4,5,6,7,8分</li>
 * <li><strong>2,3,5,8</strong> ：表示列表</li>
 * <li><strong>cronA | cronB</strong> ：表示多个定时表达式</li>
 * </ul>
 * 注意：在每一个子表达式中优先级：
 *
 * <pre>
 * 间隔（/） &gt; 区间（-） &gt; 列表（,）
 * </pre>
 * <p>
 * 例如 2,3,6/3中，由于“/”优先级高，因此相当于2,3,(6/3)，结果与 2,3,6等价
 *
 * <p>
 * 一些示例：
 * <ul>
 * <li><strong>5 * * * *</strong> ：每个点钟的5分执行，00:05,01:05……</li>
 * <li><strong>* * * * *</strong> ：每分钟执行</li>
 * <li><strong>*&#47;2 * * * *</strong> ：每两分钟执行</li>
 * <li><strong>* 12 * * *</strong> ：12点的每分钟执行</li>
 * <li><strong>59 11 * * 1,2</strong> ：每周一和周二的11:59执行</li>
 * <li><strong>3-18&#47;5 * * * *</strong> ：3~18分，每5分钟执行一次，即0:03, 0:08, 0:13, 0:18, 1:03, 1:08……</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CronPattern {

    private final String pattern;
    private final List<PatternMatcher> matchers;

    /**
     * 构造
     *
     * @param pattern 表达式
     */
    public CronPattern(final String pattern) {
        this.pattern = pattern;
        this.matchers = PatternParser.parse(pattern);
    }

    /**
     * 解析表达式为 CronPattern
     *
     * @param pattern 表达式
     * @return CronPattern
     */
    public static CronPattern of(final String pattern) {
        return new CronPattern(pattern);
    }

    /**
     * 列举指定日期之后（到开始日期对应年年底）内第一个匹配表达式的日期
     *
     * @param pattern 表达式
     * @param start   起始时间
     * @return 日期
     */
    public static Date nextDateAfter(final CronPattern pattern, final Date start) {
        return DateKit.date(pattern.nextMatchAfter(Calendar.calendar(start)));
    }

    /**
     * 列举指定日期之后（到开始日期对应年年底）内所有匹配表达式的日期
     *
     * @param patternStr 表达式字符串
     * @param start      起始时间
     * @param count      列举数量
     * @return 日期列表
     */
    public static List<Date> matchedDates(final String patternStr, final Date start, final int count) {
        return matchedDates(patternStr, start, DateKit.endOfYear(start, false), count);
    }

    /**
     * 列举指定日期范围内所有匹配表达式的日期
     *
     * @param patternStr 表达式字符串
     * @param start      起始时间
     * @param end        结束时间
     * @param count      列举数量
     * @return 日期列表
     */
    public static List<Date> matchedDates(final String patternStr, final Date start, final Date end, final int count) {
        return matchedDates(patternStr, start.getTime(), end.getTime(), count);
    }

    /**
     * 列举指定日期范围内所有匹配表达式的日期
     *
     * @param patternStr 表达式字符串
     * @param start      起始时间
     * @param end        结束时间
     * @param count      列举数量
     * @return 日期列表
     */
    public static List<Date> matchedDates(final String patternStr, final long start, final long end, final int count) {
        return matchedDates(new CronPattern(patternStr), start, end, count);
    }

    /**
     * 列举指定日期范围内所有匹配表达式的日期
     *
     * @param pattern 表达式
     * @param start   起始时间
     * @param end     结束时间
     * @param count   列举数量
     * @return 日期列表
     */
    public static List<Date> matchedDates(final CronPattern pattern, final long start, final long end,
            final int count) {
        Assert.isTrue(start < end, "Start date is later than end !");

        final List<Date> result = new ArrayList<>(count);

        java.util.Calendar calendar = pattern.nextMatchAfter(Calendar.calendar(start));
        while (calendar.getTimeInMillis() < end) {
            result.add(DateKit.date(calendar));
            if (result.size() >= count) {
                break;
            }
            calendar = pattern.nextMatchAfter(calendar);
        }

        return result;
    }

    /**
     * 获取处理后的字段列表 月份从1开始，周从0开始
     *
     * @param dateTime      {@link java.util.Calendar}
     * @param isMatchSecond 是否匹配秒，{@link false}则秒返回-1
     * @return 字段值列表
     */
    static int[] getFields(final LocalDateTime dateTime, final boolean isMatchSecond) {
        final int second = isMatchSecond ? dateTime.getSecond() : -1;
        final int minute = dateTime.getMinute();
        final int hour = dateTime.getHour();
        final int dayOfMonth = dateTime.getDayOfMonth();
        final int month = dateTime.getMonthValue();// 月份从1开始
        final int dayOfWeek = Week.of(dateTime.getDayOfWeek()).getCode() - 1; // 星期从0开始，0和7都表示周日
        final int year = dateTime.getYear();
        return new int[] { second, minute, hour, dayOfMonth, month, dayOfWeek, year };
    }

    /**
     * 获取处理后的字段列表 月份从1开始，周从0开始
     *
     * @param calendar      {@link java.util.Calendar}
     * @param isMatchSecond 是否匹配秒，{@link false}则秒返回-1
     * @return 字段值列表
     */
    static int[] getFields(final java.util.Calendar calendar, final boolean isMatchSecond) {
        final int second = isMatchSecond ? calendar.get(java.util.Calendar.SECOND) : -1;
        final int minute = calendar.get(java.util.Calendar.MINUTE);
        final int hour = calendar.get(java.util.Calendar.HOUR_OF_DAY);
        final int dayOfMonth = calendar.get(java.util.Calendar.DAY_OF_MONTH);
        final int monthBase1 = calendar.get(java.util.Calendar.MONTH) + 1;// 月份从1开始
        final int dayOfWeekBase0 = calendar.get(java.util.Calendar.DAY_OF_WEEK) - 1; // 星期从0开始，0和7都表示周日
        final int year = calendar.get(java.util.Calendar.YEAR);
        return new int[] { second, minute, hour, dayOfMonth, monthBase1, dayOfWeekBase0, year };
    }

    /**
     * 给定时间是否匹配定时任务表达式
     *
     * @param millis        时间毫秒数
     * @param isMatchSecond 是否匹配秒
     * @return 如果匹配返回 {@code true}, 否则返回 {@code false}
     */
    public boolean match(final long millis, final boolean isMatchSecond) {
        return match(TimeZone.getDefault(), millis, isMatchSecond);
    }

    /**
     * 给定时间是否匹配定时任务表达式
     *
     * @param timezone      时区 {@link TimeZone}
     * @param millis        时间毫秒数
     * @param isMatchSecond 是否匹配秒
     * @return 如果匹配返回 {@code true}, 否则返回 {@code false}
     */
    public boolean match(final TimeZone timezone, final long millis, final boolean isMatchSecond) {
        final GregorianCalendar calendar = new GregorianCalendar(timezone);
        calendar.setTimeInMillis(millis);
        return match(calendar, isMatchSecond);
    }

    /**
     * 给定时间是否匹配定时任务表达式
     *
     * @param calendar      时间
     * @param isMatchSecond 是否匹配秒
     * @return 如果匹配返回 {@code true}, 否则返回 {@code false}
     */
    public boolean match(final java.util.Calendar calendar, final boolean isMatchSecond) {
        return match(getFields(calendar, isMatchSecond));
    }

    /**
     * 给定时间是否匹配定时任务表达式
     *
     * @param dateTime      时间
     * @param isMatchSecond 是否匹配秒
     * @return 如果匹配返回 {@code true}, 否则返回 {@code false}
     */
    public boolean match(final LocalDateTime dateTime, final boolean isMatchSecond) {
        return match(getFields(dateTime, isMatchSecond));
    }

    /**
     * 返回匹配到的下一个时间
     *
     * @param calendar 时间
     * @return 匹配到的下一个时间
     */
    public java.util.Calendar nextMatchAfter(java.util.Calendar calendar) {
        // 当提供的时间已经匹配表达式时，增加1秒以匹配下一个时间
        if (match(calendar, true)) {
            final java.util.Calendar newCalendar = java.util.Calendar.getInstance(calendar.getTimeZone());
            newCalendar.setTimeInMillis(calendar.getTimeInMillis() + 1000);
            calendar = newCalendar;
        }

        java.util.Calendar next = nextMatchAfter(getFields(calendar, true), calendar.getTimeZone());
        if (!match(next, true)) {
            next.set(java.util.Calendar.DAY_OF_MONTH, next.get(java.util.Calendar.DAY_OF_MONTH) + 1);
            next = Calendar.beginOfDay(next);
            return nextMatchAfter(next);
        }
        return next;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final CronPattern that = (CronPattern) o;
        return Objects.equals(pattern, that.pattern);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pattern);
    }

    @Override
    public String toString() {
        return this.pattern;
    }

    /**
     * 给定时间是否匹配定时任务表达式
     *
     * @param fields 时间字段值，{second, minute, hour, dayOfMonth, monthBase1, dayOfWeekBase0, year}
     * @return 如果匹配返回 {@code true}, 否则返回 {@code false}
     */
    private boolean match(final int[] fields) {
        for (final PatternMatcher matcher : matchers) {
            if (matcher.match(fields)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取下一个最近的匹配日期时间
     *
     * @param values 时间字段值，{second, minute, hour, dayOfMonth, monthBase1, dayOfWeekBase0, year}
     * @param zone   时区
     * @return {@link java.util.Calendar}，毫秒数为0
     */
    private java.util.Calendar nextMatchAfter(final int[] values, final TimeZone zone) {
        java.util.Calendar minMatch = null;
        for (final PatternMatcher matcher : matchers) {
            if (null == minMatch) {
                minMatch = matcher.nextMatchAfter(values, zone);
            } else {
                minMatch = CompareKit.min(minMatch, matcher.nextMatchAfter(values, zone));
            }
        }
        // 返回匹配到的最早日期
        return minMatch;
    }

}
