/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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

import java.io.Serial;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.miaixz.bus.core.center.date.culture.en.*;
import org.miaixz.bus.core.center.date.culture.en.Month;
import org.miaixz.bus.core.center.date.format.FormatBuilder;
import org.miaixz.bus.core.center.date.format.FormatManager;
import org.miaixz.bus.core.center.date.format.FormatPeriod;
import org.miaixz.bus.core.center.date.format.parser.DateParser;
import org.miaixz.bus.core.center.date.format.parser.PositionDateParser;
import org.miaixz.bus.core.center.date.printer.FormatPrinter;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Fields;
import org.miaixz.bus.core.lang.Keys;
import org.miaixz.bus.core.lang.exception.DateException;
import org.miaixz.bus.core.xyz.DateKit;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.core.xyz.ZoneKit;

/**
 * 包装{@link Date} 此类继承了{@link Date}，并提供扩展方法，如时区等。 此类重写了父类的{@code toString()}方法，返回值为"yyyy-MM-dd HH:mm:ss"格式
 * 相对于{@link Date}，此类定义了时区，用于标识日期所在时区，默认为当前时区
 * <ul>
 * <li>当使用默认时区时，与LocalDateTime类似，标识本地时间</li>
 * <li>当使用指定时区时，与ZonedDateTime类似，标识某个地区的时间</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class DateTime extends Date {

    @Serial
    private static final long serialVersionUID = 2852250731138L;

    private static boolean useJdkToStringStyle = false;
    /**
     * 是否可变对象
     */
    private boolean mutable = true;
    /**
     * 一周的第一天，默认是周一， 在设置或获得 WEEK_OF_MONTH 或 WEEK_OF_YEAR 字段时，Calendar 必须确定一个月或一年的第一个星期，以此作为参考点。
     */
    private Week firstDayOfWeek = Week.MONDAY;
    /**
     * 时区
     */
    private transient TimeZone timeZone;
    /**
     * 第一周最少天数
     */
    private int minimalDaysInFirstWeek;

    /**
     * 当前时间
     */
    public DateTime() {
        this(TimeZone.getDefault());
    }

    /**
     * 当前时间
     *
     * @param timeZone 时区，{@code null}表示默认时区
     */
    public DateTime(final TimeZone timeZone) {
        this(System.currentTimeMillis(), timeZone);
    }

    /**
     * 给定日期的构造
     *
     * @param date 日期
     */
    public DateTime(final Date date) {
        this(date, (date instanceof DateTime) ? ((DateTime) date).timeZone : TimeZone.getDefault());
    }

    /**
     * 给定日期的构造
     *
     * @param date     日期，{@code null}表示当前时间
     * @param timeZone 时区，{@code null}表示默认时区
     */
    public DateTime(final Date date, final TimeZone timeZone) {
        this(ObjectKit.defaultIfNull(date, Date::new).getTime(), timeZone);
    }

    /**
     * 给定日期的构造
     *
     * @param calendar {@link java.util.Calendar}，不能为{@code null}
     */
    public DateTime(final java.util.Calendar calendar) {
        this(calendar.getTime(), calendar.getTimeZone());
        this.setFirstDayOfWeek(Week.of(calendar.getFirstDayOfWeek()));
    }

    /**
     * 给定日期Instant的构造
     *
     * @param instant {@link Instant} 对象，不能为{@code null}
     */
    public DateTime(final Instant instant) {
        this(instant.toEpochMilli());
    }

    /**
     * 给定日期Instant的构造
     *
     * @param instant {@link Instant} 对象
     * @param zoneId  时区ID
     */
    public DateTime(final Instant instant, final ZoneId zoneId) {
        this(instant.toEpochMilli(), ZoneKit.toTimeZone(zoneId));
    }

    /**
     * 给定日期TemporalAccessor的构造
     *
     * @param temporalAccessor {@link TemporalAccessor} 对象
     */
    public DateTime(final TemporalAccessor temporalAccessor) {
        this(Converter.toInstant(temporalAccessor));
    }

    /**
     * 给定日期ZonedDateTime的构造
     *
     * @param zonedDateTime {@link ZonedDateTime} 对象
     */
    public DateTime(final ZonedDateTime zonedDateTime) {
        this(zonedDateTime.toInstant(), zonedDateTime.getZone());
    }

    /**
     * 给定日期毫秒数的构造
     *
     * @param timeMillis 日期毫秒数
     */
    public DateTime(final long timeMillis) {
        this(timeMillis, TimeZone.getDefault());
    }

    /**
     * 给定日期毫秒数的构造
     *
     * @param timeMillis 日期毫秒数
     * @param timeZone   时区
     */
    public DateTime(final long timeMillis, final TimeZone timeZone) {
        super(timeMillis);
        this.timeZone = ObjectKit.defaultIfNull(timeZone, TimeZone::getDefault);
    }

    /**
     * 构造格式：
     * <ol>
     * <li>yyyy-MM-dd HH:mm:ss</li>
     * <li>yyyy/MM/dd HH:mm:ss</li>
     * <li>yyyy.MM.dd HH:mm:ss</li>
     * <li>yyyy年MM月dd日 HH时mm分ss秒</li>
     * <li>yyyy-MM-dd</li>
     * <li>yyyy/MM/dd</li>
     * <li>yyyy.MM.dd</li>
     * <li>HH:mm:ss</li>
     * <li>HH时mm分ss秒</li>
     * <li>yyyy-MM-dd HH:mm</li>
     * <li>yyyy-MM-dd HH:mm:ss.SSS</li>
     * <li>yyyyMMddHHmmss</li>
     * <li>yyyyMMddHHmmssSSS</li>
     * <li>yyyyMMdd</li>
     * <li>EEE, dd MMM yyyy HH:mm:ss z</li>
     * <li>EEE MMM dd HH:mm:ss zzz yyyy</li>
     * <li>yyyy-MM-dd'T'HH:mm:ss'Z'</li>
     * <li>yyyy-MM-dd'T'HH:mm:ss.SSS'Z'</li>
     * <li>yyyy-MM-dd'T'HH:mm:ssZ</li>
     * <li>yyyy-MM-dd'T'HH:mm:ss.SSSZ</li>
     * </ol>
     *
     * @param date 日期字符串
     */
    public DateTime(final CharSequence date) {
        this(DateKit.parse(date));
    }

    /**
     * 构造
     *
     * @param date   日期字符串
     * @param format 格式
     * @see Fields
     */
    public DateTime(final CharSequence date, final String format) {
        this(parse(date, format));
    }

    /**
     * 构造
     *
     * @param date   日期字符串
     * @param format 格式化器 {@link SimpleDateFormat}
     * @see Fields
     */
    public DateTime(final CharSequence date, final DateFormat format) {
        this(parse(date, format), format.getTimeZone());
    }

    /**
     * 构建DateTime对象
     *
     * @param date      日期字符串
     * @param formatter 格式化器,{@link DateTimeFormatter}
     */
    public DateTime(final CharSequence date, final DateTimeFormatter formatter) {
        this(Converter.toInstant(formatter.parse(date)), formatter.getZone());
    }

    /**
     * 构造
     *
     * @param date       日期字符串
     * @param dateParser 格式化器 {@link DateParser}，可以使用 {@link FormatBuilder}
     * @see Fields
     */
    public DateTime(final CharSequence date, final PositionDateParser dateParser) {
        this(date, dateParser, Keys.getBoolean(Keys.DATE_LENIENT, false));
    }

    /**
     * 构造
     *
     * @param date    日期字符串
     * @param parser  格式化器 {@link DateParser}，可以使用 {@link FormatBuilder}
     * @param lenient 是否宽容模式
     * @see Fields
     */
    public DateTime(final CharSequence date, final PositionDateParser parser, final boolean lenient) {
        this(parse(date, parser, lenient));
    }

    /**
     * 现在的时间
     *
     * @return 现在的时间
     */
    public static DateTime now() {
        return new DateTime();
    }

    /**
     * 转换时间戳为 DateTime，默认时区
     *
     * @param timeMillis 时间戳，毫秒数
     * @return DateTime
     */
    public static DateTime of(final long timeMillis) {
        return new DateTime(timeMillis);
    }

    /**
     * 转换JDK date为 DateTime
     *
     * @param date JDK Date
     * @return DateTime
     */
    public static DateTime of(final Date date) {
        if (date instanceof DateTime) {
            return (DateTime) date;
        }
        return new DateTime(date);
    }

    /**
     * 转换 {@link java.util.Calendar} 为 DateTime，使用{@link java.util.Calendar}中指定的时区
     *
     * @param calendar {@link java.util.Calendar}
     * @return DateTime
     */
    public static DateTime of(final java.util.Calendar calendar) {
        return new DateTime(calendar);
    }

    /**
     * 构造
     *
     * @param date   日期字符串
     * @param format 格式
     * @return this
     * @see Fields
     */
    public static DateTime of(final String date, final String format) {
        return new DateTime(date, format);
    }

    /**
     * 设置全局的，是否使用{@link Date}默认的toString()格式 如果为{@code true}，则调用toString()时返回"EEE MMM dd HH:mm:ss zzz yyyy"格式，
     * 如果为{@code false}，则返回"yyyy-MM-dd HH:mm:ss"， 默认为{@code false}
     *
     * @param customUseJdkToStringStyle 是否使用{@link Date}默认的toString()格式
     */
    public static void setUseJdkToStringStyle(final boolean customUseJdkToStringStyle) {
        useJdkToStringStyle = customUseJdkToStringStyle;
    }

    /**
     * 转换字符串为Date
     *
     * @param date   日期字符串
     * @param format 格式字符串
     * @return {@link Date}
     */
    private static Date parse(final CharSequence date, final String format) {
        final FormatManager formatManager = FormatManager.getInstance();
        return formatManager.isCustomFormat(format) ? formatManager.parse(date, format)
                : parse(date, Formatter.newSimpleFormat(format));
    }

    /**
     * 转换字符串为Date
     *
     * @param date   日期字符串
     * @param format {@link SimpleDateFormat}
     * @return {@link Date}
     */
    private static Date parse(final CharSequence date, final DateFormat format) {
        Assert.notBlank(date, "Date String must be not blank !");
        try {
            return format.parse(date.toString());
        } catch (final Exception e) {
            final String pattern;
            if (format instanceof SimpleDateFormat) {
                pattern = ((SimpleDateFormat) format).toPattern();
            } else {
                pattern = format.toString();
            }
            throw new DateException(StringKit.format("Parse [{}] with format [{}] error!", date, pattern), e);
        }
    }

    /**
     * 转换字符串为Date
     *
     * @param date    日期字符串
     * @param parser  {@link FormatBuilder}
     * @param lenient 是否宽容模式
     * @return {@link java.util.Calendar}
     */
    private static java.util.Calendar parse(final CharSequence date, final PositionDateParser parser,
            final boolean lenient) {
        final java.util.Calendar calendar = Calendar.parse(date, lenient, parser);
        calendar.setFirstDayOfWeek(Week.MONDAY.getCode());
        return calendar;
    }

    /**
     * 调整日期和时间 如果此对象为可变对象，返回自身，否则返回新对象，设置是否可变对象见{@link #setMutable(boolean)}
     *
     * @param datePart 调整的部分 {@link Various}
     * @param offset   偏移量，正数为向后偏移，负数为向前偏移
     * @return 如果此对象为可变对象，返回自身，否则返回新对象
     */
    public DateTime offset(final Various datePart, final int offset) {
        if (Various.ERA == datePart) {
            throw new IllegalArgumentException("ERA is not support offset!");
        }

        final java.util.Calendar cal = toCalendar();
        cal.add(datePart.getValue(), offset);

        final DateTime dt = mutable ? this : ObjectKit.clone(this);
        return dt.setTimeInternal(cal.getTimeInMillis());
    }

    /**
     * 调整日期和时间 返回调整后的新DateTime，不影响原对象
     *
     * @param datePart 调整的部分 {@link Various}
     * @param offset   偏移量，正数为向后偏移，负数为向前偏移
     * @return 如果此对象为可变对象，返回自身，否则返回新对象
     */
    public DateTime offsetNew(final Various datePart, final int offset) {
        final java.util.Calendar cal = toCalendar();
        cal.add(datePart.getValue(), offset);
        return ObjectKit.clone(this).setTimeInternal(cal.getTimeInMillis());
    }

    /**
     * 获得日期的某个部分 例如获得年的部分，则使用 getField(DatePart.YEAR)
     *
     * @param field 表示日期的哪个部分的枚举 {@link Various}
     * @return 某个部分的值
     */
    public int getField(final Various field) {
        return getField(field.getValue());
    }

    /**
     * 获得日期的某个部分 例如获得年的部分，则使用 getField(Calendar.YEAR)
     *
     * @param field 表示日期的哪个部分的int值 {@link java.util.Calendar}
     * @return 某个部分的值
     */
    public int getField(final int field) {
        return toCalendar().get(field);
    }

    /**
     * 设置日期的某个部分 如果此对象为可变对象，返回自身，否则返回新对象，设置是否可变对象见{@link #setMutable(boolean)}
     *
     * @param field 表示日期的哪个部分的枚举 {@link Various}
     * @param value 值
     * @return this
     */
    public DateTime setField(final Various field, final int value) {
        return setField(field.getValue(), value);
    }

    /**
     * 设置日期的某个部分 如果此对象为可变对象，返回自身，否则返回新对象，设置是否可变对象见{@link #setMutable(boolean)}
     *
     * @param field 表示日期的哪个部分的int值 {@link java.util.Calendar}
     * @param value 值
     * @return this
     */
    public DateTime setField(final int field, final int value) {
        final java.util.Calendar calendar = toCalendar();
        calendar.set(field, value);

        DateTime dt = this;
        if (!mutable) {
            dt = ObjectKit.clone(this);
        }
        return dt.setTimeInternal(calendar.getTimeInMillis());
    }

    @Override
    public void setTime(final long time) {
        if (mutable) {
            super.setTime(time);
        } else {
            throw new DateException("This is not a mutable object !");
        }
    }

    /**
     * 获得年的部分
     *
     * @return 年的部分
     */
    public int year() {
        return getField(Various.YEAR);
    }

    /**
     * 获得当前日期所属季度，从1开始计数
     *
     * @return 第几个季度 {@link Quarter}
     */
    public int quarter() {
        return month() / 3 + 1;
    }

    /**
     * 获得当前日期所属季度
     *
     * @return 第几个季度 {@link Quarter}
     */
    public Quarter quarterEnum() {
        return Quarter.of(quarter());
    }

    /**
     * 获得月份，从0开始计数
     *
     * @return 月份
     */
    public int month() {
        return getField(Various.MONTH);
    }

    /**
     * 获取月，从1开始计数
     *
     * @return 月份，1表示一月
     */
    public int monthBaseOne() {
        return month() + 1;
    }

    /**
     * 获得月份，从1开始计数 由于{@link java.util.Calendar} 中的月份按照0开始计数，导致某些需求容易误解，因此如果想用1表示一月，2表示二月则调用此方法
     *
     * @return 月份
     */
    public int monthStartFromOne() {
        return month() + 1;
    }

    /**
     * 获得月份
     *
     * @return {@link Month}
     */
    public Month monthEnum() {
        return Month.of(month());
    }

    /**
     * 获得指定日期是所在年份的第几周 此方法返回值与一周的第一天有关，比如： 2016年1月3日为周日，如果一周的第一天为周日，那这天是第二周（返回2） 如果一周的第一天为周一，那这天是第一周（返回1）
     * 跨年的那个星期得到的结果总是1
     *
     * @return 周
     * @see #setFirstDayOfWeek(Week)
     */
    public int weekOfYear() {
        return getField(Various.WEEK_OF_YEAR);
    }

    /**
     * 获得指定日期是所在月份的第几周 此方法返回值与一周的第一天有关，比如： 2016年1月3日为周日，如果一周的第一天为周日，那这天是第二周（返回2） 如果一周的第一天为周一，那这天是第一周（返回1）
     *
     * @return 周
     * @see #setFirstDayOfWeek(Week)
     */
    public int weekOfMonth() {
        return getField(Various.WEEK_OF_MONTH);
    }

    /**
     * 获得指定日期是这个日期所在月份的第几天，从1开始
     *
     * @return 天，1表示第一天
     */
    public int dayOfMonth() {
        return getField(Various.DAY_OF_MONTH);
    }

    /**
     * 获得指定日期是这个日期所在年份的第几天，从1开始
     *
     * @return 天，1表示第一天
     */
    public int dayOfYear() {
        return getField(Various.DAY_OF_YEAR);
    }

    /**
     * 获得指定日期是星期几，1表示周日，2表示周一
     *
     * @return 星期几
     */
    public int dayOfWeek() {
        return getField(Various.DAY_OF_WEEK);
    }

    /**
     * 获得天所在的周是这个月的第几周
     *
     * @return 天
     */
    public int dayOfWeekInMonth() {
        return getField(Various.DAY_OF_WEEK_IN_MONTH);
    }

    /**
     * 获得指定日期是星期几
     *
     * @return {@link Week}
     */
    public Week dayOfWeekEnum() {
        return Week.of(dayOfWeek());
    }

    /**
     * 获得指定日期的小时数部分
     *
     * @param is24HourClock 是否24小时制
     * @return 小时数
     */
    public int hour(final boolean is24HourClock) {
        return getField(is24HourClock ? Various.HOUR_OF_DAY : Various.HOUR);
    }

    /**
     * 获得指定日期的分钟数部分 例如：10:04:15.250 =》 4
     *
     * @return 分钟数
     */
    public int minute() {
        return getField(Various.MINUTE);
    }

    /**
     * 获得指定日期的秒数部分
     *
     * @return 秒数
     */
    public int second() {
        return getField(Various.SECOND);
    }

    /**
     * 获得指定日期的毫秒数部分
     *
     * @return 毫秒数
     */
    public int millisecond() {
        return getField(Various.MILLISECOND);
    }

    /**
     * 是否为上午
     *
     * @return 是否为上午
     */
    public boolean isAM() {
        return java.util.Calendar.AM == getField(Various.AM_PM);
    }

    /**
     * 是否为下午
     *
     * @return 是否为下午
     */
    public boolean isPM() {
        return java.util.Calendar.PM == getField(Various.AM_PM);
    }

    /**
     * 是否为周末，周末指周六或者周日
     *
     * @return 是否为周末，周末指周六或者周日
     */
    public boolean isWeekend() {
        final int dayOfWeek = dayOfWeek();
        return java.util.Calendar.SATURDAY == dayOfWeek || java.util.Calendar.SUNDAY == dayOfWeek;
    }

    /**
     * 是否闰年
     *
     * @return 是否闰年
     */
    public boolean isLeapYear() {
        return Year.isLeap(year());
    }

    /**
     * 转换为Calendar, 默认 {@link Locale}
     *
     * @return {@link java.util.Calendar}
     */
    public java.util.Calendar toCalendar() {
        return toCalendar(Locale.getDefault(Locale.Category.FORMAT));
    }

    /**
     * 转换为Calendar
     *
     * @param locale 地域 {@link Locale}
     * @return {@link java.util.Calendar}
     */
    public java.util.Calendar toCalendar(final Locale locale) {
        return toCalendar(this.timeZone, locale);
    }

    /**
     * 转换为Calendar
     *
     * @param zone 时区 {@link TimeZone}
     * @return {@link java.util.Calendar}
     */
    public java.util.Calendar toCalendar(final TimeZone zone) {
        return toCalendar(zone, Locale.getDefault(Locale.Category.FORMAT));
    }

    /**
     * 转换为Calendar
     *
     * @param zone   时区 {@link TimeZone}
     * @param locale 地域 {@link Locale}
     * @return {@link java.util.Calendar}
     */
    public java.util.Calendar toCalendar(final TimeZone zone, Locale locale) {
        if (null == locale) {
            locale = Locale.getDefault(Locale.Category.FORMAT);
        }
        final java.util.Calendar cal = (null != zone) ? java.util.Calendar.getInstance(zone, locale)
                : java.util.Calendar.getInstance(locale);
        cal.setFirstDayOfWeek(firstDayOfWeek.getCode());
        if (minimalDaysInFirstWeek > 0) {
            cal.setMinimalDaysInFirstWeek(minimalDaysInFirstWeek);
        }
        cal.setTime(this);
        return cal;
    }

    /**
     * 转换为 {@link Date} 考虑到很多框架（例如Hibernate）的兼容性，提供此方法返回JDK原生的Date对象
     *
     * @return {@link Date}
     */
    public Date toJdkDate() {
        return new Date(this.getTime());
    }

    /**
     * 转换为 {@link LocalDateTime}
     *
     * @return {@link LocalDateTime}
     */
    public LocalDateTime toLocalDateTime() {
        return Converter.of(this.toInstant());
    }

    /**
     * 计算相差时长
     *
     * @param date 对比的日期
     * @return {@link Between}
     */
    public Between between(final Date date) {
        return new Between(this, date);
    }

    /**
     * 计算相差时长
     *
     * @param date 对比的日期
     * @param unit 单位 {@link Units}
     * @return 相差时长
     */
    public long between(final Date date, final Units unit) {
        return new Between(this, date).between(unit);
    }

    /**
     * 计算相差时长
     *
     * @param date        对比的日期
     * @param unit        单位 {@link Units}
     * @param formatLevel 格式化级别
     * @return 相差时长
     */
    public String between(final Date date, final Units unit, final FormatPeriod.Level formatLevel) {
        return new Between(this, date).toString(unit, formatLevel);
    }

    /**
     * 当前日期是否在日期指定范围内 起始日期和结束日期可以互换
     *
     * @param beginDate 起始日期（包含）
     * @param endDate   结束日期（包含）
     * @return 是否在范围内
     */
    public boolean isIn(final Date beginDate, final Date endDate) {
        return isIn(this, beginDate, endDate);
    }

    /**
     * 当前日期是否在日期指定范围内 起始日期和结束日期可以互换
     *
     * @param date      被检查的日期
     * @param beginDate 起始日期（包含）
     * @param endDate   结束日期（包含）
     * @return 是否在范围内
     */
    public boolean isIn(final Date date, final Date beginDate, final Date endDate) {
        return isIn(date, beginDate, endDate, true, true);
    }

    /**
     * 当前日期是否在日期指定范围内 起始日期和结束日期可以互换 通过includeBegin, includeEnd参数控制日期范围区间是否为开区间，例如：传入参数：includeBegin=true,
     * includeEnd=false， 则本方法会判断 date ∈ (beginDate, endDate] 是否成立
     *
     * @param date         被检查的日期
     * @param beginDate    起始日期
     * @param endDate      结束日期
     * @param includeBegin 时间范围是否包含起始日期
     * @param includeEnd   时间范围是否包含结束日期
     * @return 是否在范围内
     */
    public boolean isIn(final Date date, final Date beginDate, final Date endDate, final boolean includeBegin,
            final boolean includeEnd) {
        if (date == null || beginDate == null || endDate == null) {
            throw new IllegalArgumentException("参数不可为null");
        }

        final long thisMills = date.getTime();
        final long beginMills = beginDate.getTime();
        final long endMills = endDate.getTime();
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
     * 是否在给定日期之前
     *
     * @param date 日期
     * @return 是否在给定日期之前
     */
    public boolean isBefore(final Date date) {
        if (null == date) {
            throw new NullPointerException("Date to compare is null !");
        }
        return compareTo(date) < 0;
    }

    /**
     * 是否在给定日期之前或与给定日期相等
     *
     * @param date 日期
     * @return 是否在给定日期之前或与给定日期相等
     */
    public boolean isBeforeOrEquals(final Date date) {
        if (null == date) {
            throw new NullPointerException("Date to compare is null !");
        }
        return compareTo(date) <= 0;
    }

    /**
     * 是否在给定日期之后
     *
     * @param date 日期
     * @return 是否在给定日期之后
     */
    public boolean isAfter(final Date date) {
        if (null == date) {
            throw new NullPointerException("Date to compare is null !");
        }
        return compareTo(date) > 0;
    }

    /**
     * 是否在给定日期之后或与给定日期相等
     *
     * @param date 日期
     * @return 是否在给定日期之后或与给定日期相等
     */
    public boolean isAfterOrEquals(final Date date) {
        if (null == date) {
            throw new NullPointerException("Date to compare is null !");
        }
        return compareTo(date) >= 0;
    }

    /**
     * 对象是否可变 如果为不可变对象，以下方法将返回新方法：
     * <ul>
     * <li>{@link DateTime#offset(Various, int)}</li>
     * <li>{@link DateTime#setField(Various, int)}</li>
     * <li>{@link DateTime#setField(int, int)}</li>
     * </ul>
     * 如果为不可变对象，{@link DateTime#setTime(long)}将抛出异常
     *
     * @return 对象是否可变
     */
    public boolean isMutable() {
        return mutable;
    }

    /**
     * 设置对象是否可变 如果为不可变对象，以下方法将返回新方法：
     * <ul>
     * <li>{@link DateTime#offset(Various, int)}</li>
     * <li>{@link DateTime#setField(Various, int)}</li>
     * <li>{@link DateTime#setField(int, int)}</li>
     * </ul>
     * 如果为不可变对象，{@link DateTime#setTime(long)}将抛出异常
     *
     * @param mutable 是否可变
     * @return this
     */
    public DateTime setMutable(final boolean mutable) {
        this.mutable = mutable;
        return this;
    }

    /**
     * 获得一周的第一天，默认为周一
     *
     * @return 一周的第一天
     */
    public Week getFirstDayOfWeek() {
        return firstDayOfWeek;
    }

    /**
     * 设置一周的第一天 JDK的Calendar中默认一周的第一天是周日，将此默认值设置为周一 设置一周的第一天主要影响{@link #weekOfMonth()}和{@link #weekOfYear()} 两个方法
     *
     * @param firstDayOfWeek 一周的第一天
     * @return this
     * @see #weekOfMonth()
     * @see #weekOfYear()
     */
    public DateTime setFirstDayOfWeek(final Week firstDayOfWeek) {
        this.firstDayOfWeek = firstDayOfWeek;
        return this;
    }

    /**
     * 获取时区
     *
     * @return 时区
     */
    public TimeZone getTimeZone() {
        return this.timeZone;
    }

    /**
     * 设置时区
     *
     * @param timeZone 时区
     * @return this
     */
    public DateTime setTimeZone(final TimeZone timeZone) {
        this.timeZone = ObjectKit.defaultIfNull(timeZone, TimeZone::getDefault);
        return this;
    }

    /**
     * 获取时区ID
     *
     * @return 时区ID
     */
    public ZoneId getZoneId() {
        return this.timeZone.toZoneId();
    }

    /**
     * 设置第一周最少天数
     *
     * @param minimalDaysInFirstWeek 第一周最少天数
     * @return this
     */
    public DateTime setMinimalDaysInFirstWeek(final int minimalDaysInFirstWeek) {
        this.minimalDaysInFirstWeek = minimalDaysInFirstWeek;
        return this;
    }

    /**
     * 是否为本月最后一天
     *
     * @return 是否为本月最后一天
     */
    public boolean isLastDayOfMonth() {
        return dayOfMonth() == getLastDayOfMonth();
    }

    /**
     * 获得本月的最后一天
     *
     * @return 天
     */
    public int getLastDayOfMonth() {
        return monthEnum().getLastDay(isLeapYear());
    }

    /**
     * 转为字符串，如果时区被设置，会转换为其时区对应的时间，否则转换为当前地点对应的时区 可以调用{@link DateTime#setUseJdkToStringStyle(boolean)} 方法自定义默认的风格
     * 如果{@link #useJdkToStringStyle}为{@code true}，返回"EEE MMM dd HH:mm:ss zzz yyyy"格式， 如果为{@code false}，则返回"yyyy-MM-dd
     * HH:mm:ss"
     *
     * @return 格式字符串
     */
    @Override
    public String toString() {
        if (useJdkToStringStyle) {
            return super.toString();
        }
        return toString(this.timeZone);
    }

    /**
     * 转为"yyyy-MM-dd HH:mm:ss" 格式字符串 时区使用当前地区的默认时区
     *
     * @return "yyyy-MM-dd HH:mm:ss" 格式字符串
     */
    public String toStringDefaultTimeZone() {
        return toString(TimeZone.getDefault());
    }

    /**
     * 转为"yyyy-MM-dd HH:mm:ss" 格式字符串 如果时区不为{@code null}，会转换为其时区对应的时间，否则转换为当前时间对应的时区
     *
     * @param timeZone 时区
     * @return "yyyy-MM-dd HH:mm:ss" 格式字符串
     */
    public String toString(final TimeZone timeZone) {
        if (null != timeZone) {
            return toString(Formatter.newSimpleFormat(Fields.NORM_DATETIME, null, timeZone));
        }
        return toString(Formatter.NORM_DATETIME_FORMAT);
    }

    /**
     * 转为"yyyy-MM-dd" 格式字符串
     *
     * @return "yyyy-MM-dd" 格式字符串
     */
    public String toDateString() {
        if (null != this.timeZone) {
            return toString(Formatter.newSimpleFormat(Fields.NORM_DATE, null, timeZone));
        }
        return toString(Formatter.NORM_DATE_FORMAT);
    }

    /**
     * 转为"HH:mm:ss" 格式字符串
     *
     * @return "HH:mm:ss" 格式字符串
     */
    public String toTimeString() {
        if (null != this.timeZone) {
            return toString(Formatter.newSimpleFormat(Fields.NORM_TIME, null, timeZone));
        }
        return toString(Formatter.NORM_TIME_FORMAT);
    }

    /**
     * 转为字符串
     *
     * @param format 日期格式，常用格式见： {@link Fields}
     * @return String
     */
    public String toString(final String format) {
        if (null != this.timeZone) {
            return toString(Formatter.newSimpleFormat(format, null, timeZone));
        }
        return toString(FormatBuilder.getInstance(format));
    }

    /**
     * 转为字符串
     *
     * @param format {@link FormatPrinter} 或 {@link FormatBuilder}
     * @return String
     */
    public String toString(final FormatPrinter format) {
        return format.format(this);
    }

    /**
     * 转为字符串
     *
     * @param format {@link SimpleDateFormat}
     * @return String
     */
    public String toString(final DateFormat format) {
        return format.format(this);
    }

    /**
     * @return 输出精确到毫秒的标准日期形式
     */
    public String toMsString() {
        return toString(Formatter.NORM_DATETIME_MS_FORMAT);
    }

    /**
     * 设置日期时间
     *
     * @param time 日期时间毫秒
     * @return this
     */
    private DateTime setTimeInternal(final long time) {
        super.setTime(time);
        return this;
    }

}
