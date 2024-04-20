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

import org.miaixz.bus.core.date.formatter.DatePrinter;
import org.miaixz.bus.core.date.formatter.FormatBuilder;
import org.miaixz.bus.core.date.formatter.parser.DateParser;
import org.miaixz.bus.core.date.formatter.parser.PositionDateParser;
import org.miaixz.bus.core.exception.InternalException;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Fields;
import org.miaixz.bus.core.lang.System;
import org.miaixz.bus.core.toolkit.ObjectKit;
import org.miaixz.bus.core.toolkit.StringKit;
import org.miaixz.bus.core.toolkit.ZoneKit;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalField;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * 包装java.util.Date
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class DateTime extends Date {

    private static final long serialVersionUID = 1L;

    /**
     * 是否可变对象
     */
    private boolean mutable = true;
    /**
     * 一周的第一天，默认是周一， 在设置或获得 WEEK_OF_MONTH 或 WEEK_OF_YEAR 字段时，Calendar 必须确定一个月或一年的第一个星期，以此作为参考点。
     */
    private Fields.Week firstDayOfWeek = Fields.Week.Sun;
    /**
     * 时区
     */
    private TimeZone timeZone;
    /**
     * 第一周最少天数
     */
    private int firstWeekOfDays;

    /**
     * 当前时间
     */
    public DateTime() {
        this(TimeZone.getDefault());
    }

    /**
     * 当前时间
     *
     * @param timeZone 时区
     */
    public DateTime(TimeZone timeZone) {
        this(java.lang.System.currentTimeMillis(), timeZone);
    }

    /**
     * 给定日期的构造
     *
     * @param date 日期
     */
    public DateTime(Date date) {
        this(
                date.getTime(),
                (date instanceof DateTime) ? ((DateTime) date).timeZone : TimeZone.getDefault()
        );
    }

    /**
     * 给定日期的构造
     *
     * @param date     日期，{@code null}表示当前时间
     * @param timeZone 时区，{@code null}表示默认时区
     */
    public DateTime(Date date, TimeZone timeZone) {
        this(ObjectKit.defaultIfNull(date, Date::new).getTime(), timeZone);
    }

    /**
     * 给定日期的构造
     *
     * @param calendar {@link Calendar}，不能为{@code null}
     */
    public DateTime(Calendar calendar) {
        this(calendar.getTime(), calendar.getTimeZone());
        this.setFirstDayOfWeek(Fields.Week.getByCode(calendar.getFirstDayOfWeek()));
    }

    /**
     * 给定日期Instant的构造
     *
     * @param instant {@link Instant} 对象，不能为{@code null}
     */
    public DateTime(Instant instant) {
        this(instant.toEpochMilli());
    }

    /**
     * 给定日期Instant的构造
     *
     * @param instant {@link Instant} 对象
     * @param zoneId  时区ID
     */
    public DateTime(Instant instant, ZoneId zoneId) {
        this(instant.toEpochMilli(), ZoneKit.toTimeZone(zoneId));
    }

    /**
     * 给定日期TemporalAccessor的构造
     *
     * @param temporalAccessor {@link TemporalAccessor} 对象
     */
    public DateTime(TemporalAccessor temporalAccessor) {
        this(Converter.toInstant(temporalAccessor));
    }

    /**
     * 给定日期ZonedDateTime的构造
     *
     * @param zonedDateTime {@link ZonedDateTime} 对象
     */
    public DateTime(ZonedDateTime zonedDateTime) {
        this(zonedDateTime.toInstant(), zonedDateTime.getZone());
    }

    /**
     * 给定日期毫秒数的构造
     *
     * @param timeMillis 日期毫秒数
     */
    public DateTime(long timeMillis) {
        this(timeMillis, TimeZone.getDefault());
    }

    /**
     * 给定日期毫秒数的构造
     *
     * @param timeMillis 日期毫秒数
     * @param timeZone   时区
     */
    public DateTime(long timeMillis, TimeZone timeZone) {
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
     * @param text Date字符串
     */
    public DateTime(final CharSequence text) {
        this(Formatter.parse(text));
    }

    /**
     * 构造
     *
     * @param text   Date字符串
     * @param format 格式
     */
    public DateTime(final CharSequence text, final String format) {
        this(text, Formatter.newSimpleFormat(format));
    }

    /**
     * 构造
     *
     * @param text   Date字符串
     * @param format 格式化器 {@link SimpleDateFormat}
     */
    public DateTime(final CharSequence text, final DateFormat format) {
        this(parse(text, format), format.getTimeZone());
    }

    /**
     * 构建DateTime对象
     *
     * @param text      Date字符串
     * @param formatter 格式化器,{@link DateTimeFormatter}
     */
    public DateTime(final CharSequence text, final DateTimeFormatter formatter) {
        this(Converter.toInstant(formatter.parse(text)), formatter.getZone());
    }

    /**
     * 构造
     *
     * @param text   Date字符串
     * @param parser 格式化器 {@link DateParser}，可以使用 {@link FormatBuilder}
     */
    public DateTime(final CharSequence text, final PositionDateParser parser) {
        this(text, parser, System.getBoolean(System.BUS_DATE_LENIENT, true));
    }

    /**
     * 构造
     *
     * @param text    Date字符串
     * @param parser  格式化器 {@link DateParser}，可以使用 {@link Fields}
     * @param lenient 是否宽容模式
     */
    public DateTime(final CharSequence text, final PositionDateParser parser, final boolean lenient) {
        this(parse(text, parser, lenient));
    }

    /**
     * 转换时间戳为 DateTime
     *
     * @param timeMillis 时间戳，毫秒数
     * @return DateTime
     */
    public static DateTime of(long timeMillis) {
        return new DateTime(timeMillis);
    }

    /**
     * 转换JDK date为 DateTime
     *
     * @param date JDK Date
     * @return DateTime
     */
    public static DateTime of(Date date) {
        if (date instanceof DateTime) {
            return (DateTime) date;
        }
        return new DateTime(date);
    }

    /**
     * 转换 {@link Calendar} 为 DateTime
     *
     * @param calendar {@link Calendar}
     * @return DateTime
     */
    public static DateTime of(Calendar calendar) {
        return new DateTime(calendar);
    }

    /**
     * 构造
     *
     * @param text   Date字符串
     * @param format 格式
     * @return {@link DateTime}
     */
    public static DateTime of(String text, String format) {
        return new DateTime(text, format);
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
     * 转换字符串为Date
     *
     * @param text       日期字符串
     * @param dateFormat {@link SimpleDateFormat}
     * @return {@link Date}
     */
    private static Date parse(final CharSequence text, final DateFormat dateFormat) {
        Assert.notBlank(text, "Date String must be not blank !");
        try {
            return dateFormat.parse(text.toString());
        } catch (final Exception e) {
            final String pattern;
            if (dateFormat instanceof SimpleDateFormat) {
                pattern = ((SimpleDateFormat) dateFormat).toPattern();
            } else {
                pattern = dateFormat.toString();
            }
            throw new InternalException(StringKit.format("Parse [{}] with format [{}] error!", text, pattern), e);
        }
    }

    /**
     * 转换字符串为Date
     *
     * @param text    日期字符串
     * @param parser  {@link FormatBuilder}
     * @param lenient 是否宽容模式
     * @return {@link Calendar}
     */
    private static Calendar parse(final CharSequence text, final PositionDateParser parser, final boolean lenient) {
        Assert.notNull(parser, "Parser or DateFromat must be not null !");
        Assert.notBlank(text, "Date String must be not blank !");

        final Calendar calendar = Formatter.parseByPatterns(text, lenient, parser);
        if (null == calendar) {
            throw new InternalException("Parse [{}] with format [{}] error!", text, parser.getPattern());
        }
        calendar.setFirstDayOfWeek(Fields.Week.Mon.getKey());
        return calendar;
    }

    /**
     * {@link TemporalAccessor}转{@link LocalDateTime}，使用默认时区
     *
     * @param temporalAccessor {@link TemporalAccessor}
     * @return {@link LocalDateTime}
     */
    public static LocalDateTime of(TemporalAccessor temporalAccessor) {
        if (null == temporalAccessor) {
            return null;
        }

        if (temporalAccessor instanceof LocalDate) {
            return ((LocalDate) temporalAccessor).atStartOfDay();
        } else if (temporalAccessor instanceof Instant) {
            return LocalDateTime.ofInstant((Instant) temporalAccessor, ZoneId.systemDefault());
        } else if (temporalAccessor instanceof ZonedDateTime) {
            return ((ZonedDateTime) temporalAccessor).toLocalDateTime();
        }

        return LocalDateTime.of(
                get(temporalAccessor, ChronoField.YEAR),
                get(temporalAccessor, ChronoField.MONTH_OF_YEAR),
                get(temporalAccessor, ChronoField.DAY_OF_MONTH),
                get(temporalAccessor, ChronoField.HOUR_OF_DAY),
                get(temporalAccessor, ChronoField.MINUTE_OF_HOUR),
                get(temporalAccessor, ChronoField.SECOND_OF_MINUTE),
                get(temporalAccessor, ChronoField.NANO_OF_SECOND)
        );
    }

    /**
     * 安全获取时间的某个属性，属性不存在返回0
     *
     * @param temporalAccessor 需要获取的时间对象
     * @param field            需要获取的属性
     * @return 时间的值，如果无法获取则默认为 0
     */
    public static int get(TemporalAccessor temporalAccessor, TemporalField field) {
        if (temporalAccessor.isSupported(field)) {
            return temporalAccessor.get(field);
        }

        return (int) field.range().getMinimum();
    }

    /**
     * 调整日期和时间
     * 如果此对象为可变对象，返回自身，否则返回新对象，设置是否可变对象见{@link #setMutable(boolean)}
     *
     * @param type   调整的部分 {@link Fields.Type}
     * @param offset 偏移量，正数为向后偏移，负数为向前偏移
     * @return 如果此对象为可变对象，返回自身，否则返回新对象
     */
    public DateTime offset(Fields.Type type, int offset) {
        if (Fields.Type.ERA == type) {
            throw new IllegalArgumentException("ERA is not support offset!");
        }

        final Calendar cal = toCalendar();
        cal.add(type.getValue(), offset);

        DateTime dt = mutable ? this : ObjectKit.clone(this);
        return dt.setTimeInternal(cal.getTimeInMillis());
    }

    /**
     * 调整日期和时间
     * 返回调整后的新{@link DateTime}，不影响原对象
     *
     * @param type   调整的部分 {@link Fields.Type}
     * @param offset 偏移量，正数为向后偏移，负数为向前偏移
     * @return 如果此对象为可变对象，返回自身，否则返回新对象
     */
    public DateTime offsetNew(Fields.Type type, int offset) {
        final Calendar cal = toCalendar();
        cal.add(type.getValue(), offset);

        DateTime dt = ObjectKit.clone(this);
        return dt.setTimeInternal(cal.getTimeInMillis());
    }

    /**
     * 获得日期的某个部分
     * 例如获得年的部分,则使用 getField(Calendar.YEAR)
     *
     * @param field 表示日期的哪个部分的枚举 {@link Fields.Type}
     * @return 某个部分的值
     */
    public int getField(Fields.Type field) {
        return getField(field.getValue());
    }

    /**
     * 获得日期的某个部分
     * 例如获得年的部分，则使用 getField(Calendar.YEAR)
     *
     * @param field 表示日期的哪个部分的int值 {@link Calendar}
     * @return 某个部分的值
     */
    public int getField(int field) {
        return toCalendar().get(field);
    }

    /**
     * 设置日期的某个部分
     * 如果此对象为可变对象，返回自身，否则返回新对象，设置是否可变对象见{@link #setMutable(boolean)}
     *
     * @param field 表示日期的哪个部分的枚举 {@link Fields.Type}
     * @param value 值
     * @return {@link DateTime}
     */
    public DateTime setField(Fields.Type field, int value) {
        return setField(field.getValue(), value);
    }

    /**
     * 设置日期的某个部分
     * 如果此对象为可变对象，返回自身，否则返回新对象，设置是否可变对象见{@link #setMutable(boolean)}
     *
     * @param field 表示日期的哪个部分的int值 {@link Calendar}
     * @param value 值
     * @return {@link DateTime}
     */
    public DateTime setField(int field, int value) {
        final Calendar calendar = toCalendar();
        calendar.set(field, value);

        DateTime dt = this;
        if (false == mutable) {
            dt = ObjectKit.clone(this);
        }
        return dt.setTimeInternal(calendar.getTimeInMillis());
    }

    @Override
    public void setTime(long time) {
        if (mutable) {
            super.setTime(time);
        } else {
            throw new InternalException("This is not a mutable object !");
        }
    }

    /**
     * 获得年的部分
     *
     * @return 年的部分
     */
    public int year() {
        return getField(Fields.Type.YEAR);
    }

    /**
     * 获得当前日期所属季度,从1开始计数
     *
     * @return 第几个季度 {@link Fields.Quarter}
     */
    public int quarter() {
        return month() / 3 + 1;
    }

    /**
     * 获得当前日期所属季度
     *
     * @return 第几个季度 {@link Fields.Quarter}
     */
    public Fields.Quarter quarterEnum() {
        return Fields.Quarter.of(quarter());
    }

    /**
     * 获得月份,从0开始计数
     *
     * @return 月份
     */
    public int month() {
        return getField(Fields.Type.MONTH);
    }

    /**
     * 获得月份，从1开始计数
     * 由于{@link Calendar} 中的月份按照0开始计数,导致某些需求容易误解,因此如果想用1表示一月,2表示二月则调用此方法
     *
     * @return 月份
     */
    public int monthStartFromOne() {
        return month() + 1;
    }

    /**
     * 获得月份
     *
     * @return {@link Fields.Month}
     */
    public Fields.Month monthEnum() {
        return Fields.Month.getByCode(month());
    }

    /**
     * 获得指定日期是所在年份的第几周
     * 此方法返回值与一周的第一天有关,比如：
     * 2016年1月3日为周日,如果一周的第一天为周日,那这天是第二周(返回2)
     * 如果一周的第一天为周一,那这天是第一周(返回1)
     *
     * @return 周
     */
    public int weekOfYear() {
        return getField(Fields.Type.WEEK_OF_YEAR);
    }

    /**
     * 获得指定日期是所在月份的第几周
     * 此方法返回值与一周的第一天有关,比如：
     * 2016年1月3日为周日,如果一周的第一天为周日,那这天是第二周(返回2)
     * 如果一周的第一天为周一,那这天是第一周(返回1)
     *
     * @return 周
     */
    public int weekOfMonth() {
        return getField(Fields.Type.WEEK_OF_MONTH);
    }

    /**
     * 获得指定日期是这个日期所在月份的第几天
     *
     * @return 天
     */
    public int dayOfMonth() {
        return getField(Fields.Type.DAY_OF_MONTH);
    }

    /**
     * 获得指定日期是这个日期所在年份的第几天
     *
     * @return 天
     */
    public int dayOfYear() {
        return getField(Fields.Type.DAY_OF_YEAR);
    }

    /**
     * 获得指定日期是星期几,1表示周日,2表示周一
     *
     * @return 星期几
     */
    public int dayOfWeek() {
        return getField(Fields.Type.DAY_OF_WEEK);
    }

    /**
     * 获得天所在的周是这个月的第几周
     *
     * @return 天
     */
    public int dayOfWeekInMonth() {
        return getField(Fields.Type.DAY_OF_WEEK_IN_MONTH);
    }

    /**
     * 获得指定日期是星期几
     *
     * @return {@link Fields.Week}
     */
    public Fields.Week dayOfWeekEnum() {
        return Fields.Week.getByCode(dayOfWeek());
    }

    /**
     * 获得指定日期的小时数部分
     *
     * @param is24HourClock 是否24小时制
     * @return 小时数
     */
    public int hour(boolean is24HourClock) {
        return getField(is24HourClock ? Fields.Type.HOUR_OF_DAY : Fields.Type.HOUR);
    }

    /**
     * 获得指定日期的分钟数部分
     * 例如：10:04:15.250 = 4
     *
     * @return 分钟数
     */
    public int minute() {
        return getField(Fields.Type.MINUTE);
    }

    /**
     * 获得指定日期的秒数部分
     *
     * @return 秒数
     */
    public int second() {
        return getField(Fields.Type.SECOND);
    }

    /**
     * 获得指定日期的毫秒数部分
     *
     * @return 毫秒数
     */
    public int millsecond() {
        return getField(Fields.Type.MILLISECOND);
    }

    /**
     * 是否为上午
     *
     * @return 是否为上午
     */
    public boolean isAM() {
        return Calendar.AM == getField(Fields.Type.AM_PM);
    }

    /**
     * 是否为下午
     *
     * @return 是否为下午
     */
    public boolean isPM() {
        return Calendar.PM == getField(Fields.Type.AM_PM);
    }

    /**
     * 是否为周末，周末指周六或者周日
     *
     * @return 是否为周末，周末指周六或者周日
     */
    public boolean isWeekend() {
        final int dayOfWeek = dayOfWeek();
        return Calendar.SATURDAY == dayOfWeek || Calendar.SUNDAY == dayOfWeek;
    }

    /**
     * 转换为Calendar, 默认 {@link Locale}
     *
     * @return {@link Calendar}
     */
    public Calendar toCalendar() {
        return toCalendar(Locale.getDefault(Locale.Category.FORMAT));
    }

    /**
     * 转换为Calendar
     *
     * @param locale 地域 {@link Locale}
     * @return {@link Calendar}
     */
    public Calendar toCalendar(Locale locale) {
        return toCalendar(this.timeZone, locale);
    }

    /**
     * 转换为Calendar
     *
     * @param zone 时区 {@link TimeZone}
     * @return {@link Calendar}
     */
    public Calendar toCalendar(TimeZone zone) {
        return toCalendar(zone, Locale.getDefault(Locale.Category.FORMAT));
    }

    /**
     * 转换为Calendar
     *
     * @param zone   时区 {@link TimeZone}
     * @param locale 地域 {@link Locale}
     * @return {@link Calendar}
     */
    public Calendar toCalendar(TimeZone zone, Locale locale) {
        if (null == locale) {
            locale = Locale.getDefault(Locale.Category.FORMAT);
        }
        final Calendar cal = (null != zone) ? Calendar.getInstance(zone, locale) : Calendar.getInstance(locale);
        cal.setFirstDayOfWeek(firstDayOfWeek.getKey());
        if (firstWeekOfDays > 0) {
            cal.setMinimalDaysInFirstWeek(firstWeekOfDays);
        }
        cal.setTime(this);
        return cal;
    }

    /**
     * 转换为 {@link Date}
     * 考虑到很多框架(例如Hibernate)的兼容性,提供此方法返回JDK原生的Date对象
     *
     * @return {@link Date}
     */
    public Date toJdkDate() {
        return new Date(this.getTime());
    }

    /**
     * 转为{@link Timestamp}
     *
     * @return {@link Timestamp}
     */
    public Timestamp toTimestamp() {
        return new Timestamp(this.getTime());
    }

    /**
     * 转为 {@link java.sql.Date}
     *
     * @return {@link java.sql.Date}
     */
    public java.sql.Date toSqlDate() {
        return new java.sql.Date(getTime());
    }

    /**
     * 计算相差时长
     *
     * @param date 对比的日期
     * @return {@link  Between}
     */
    public Between between(Date date) {
        return new Between(this, date);
    }

    /**
     * 计算相差时长
     *
     * @param date  对比的日期
     * @param units 单位 {@link Fields.Units}
     * @return 相差时长
     */
    public long between(Date date, Fields.Units units) {
        return new Between(this, date).between(units);
    }

    /**
     * 当前日期是否在日期指定范围内
     * 起始日期和结束日期可以互换
     *
     * @param beginDate 起始日期
     * @param endDate   结束日期
     * @return 是否在范围内
     */
    public boolean isIn(Date beginDate, Date endDate) {
        long beginMills = beginDate.getTime();
        long endMills = endDate.getTime();
        long thisMills = this.getTime();

        return thisMills >= Math.min(beginMills, endMills) && thisMills <= Math.max(beginMills, endMills);
    }

    /**
     * 是否在给定日期之前
     *
     * @param date 日期
     * @return 是否在给定日期之前或与给定日期相等
     */
    public boolean isBefore(Date date) {
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
    public boolean isBeforeOrEquals(Date date) {
        if (null == date) {
            throw new NullPointerException("Date to compare is null !");
        }
        return compareTo(date) <= 0;
    }

    /**
     * 是否在给定日期之后或与给定日期相等
     *
     * @param date 日期
     * @return 是否在给定日期之后或与给定日期相等
     */
    public boolean isAfter(Date date) {
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
    public boolean isAfterOrEquals(Date date) {
        if (null == date) {
            throw new NullPointerException("Date to compare is null !");
        }
        return compareTo(date) >= 0;
    }

    /**
     * 对象是否可变
     * 如果为不可变对象，以下方法将返回新方法：
     * <ul>
     * <li>{@link DateTime#offset(Fields.Type, int)}</li>
     * <li>{@link DateTime#setField(Fields.Type, int)}</li>
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
     * <li>{@link DateTime#offset(Fields.Type, int)}</li>
     * <li>{@link DateTime#setField(Fields.Type, int)}</li>
     * <li>{@link DateTime#setField(int, int)}</li>
     * </ul>
     * 如果为不可变对象，{@link DateTime#setTime(long)}将抛出异常
     *
     * @param mutable 是否可变
     * @return this
     */
    public DateTime setMutable(boolean mutable) {
        this.mutable = mutable;
        return this;
    }

    /**
     * 获得一周的第一天,默认为周一
     *
     * @return 一周的第一天
     */
    public Fields.Week getFirstDayOfWeek() {
        return firstDayOfWeek;
    }

    /**
     * 设置一周的第一天
     * JDK的Calendar中默认一周的第一天是周日,将此默认值设置为周一
     * 设置一周的第一天主要影响{@link #weekOfMonth()}和{@link #weekOfYear()} 两个方法
     *
     * @param firstDayOfWeek 一周的第一天
     * @return this
     * @see #weekOfMonth()
     * @see #weekOfYear()
     */
    public DateTime setFirstDayOfWeek(Fields.Week firstDayOfWeek) {
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
    public DateTime setTimeZone(TimeZone timeZone) {
        this.timeZone = ObjectKit.defaultIfNull(timeZone, TimeZone::getDefault);
        return this;
    }

    /**
     * 设置第一周最少天数
     *
     * @param firstWeekOfDays 第一周最少天数
     * @return this
     */
    public DateTime setFirstWeekOfDays(int firstWeekOfDays) {
        this.firstWeekOfDays = firstWeekOfDays;
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
     * 转为"yyyy-MM-dd" 格式字符串
     *
     * @return "yyyy-MM-dd" 格式字符串
     */
    public String toDateString() {
        if (null != this.timeZone) {
            return toString(Formatter.newSimpleFormat(Fields.NORM_DATE_PATTERN, null, timeZone));
        }
        return toString(Fields.NORM_DATE_FORMAT);
    }

    /**
     * 转为"HH:mm:ss" 格式字符串
     *
     * @return "HH:mm:ss" 格式字符串
     */
    public String toTimeString() {
        if (null != this.timeZone) {
            return toString(Formatter.newSimpleFormat(Fields.NORM_TIME_PATTERN, null, timeZone));
        }
        return toString(Fields.NORM_TIME_FORMAT);
    }

    /**
     * @return 输出精确到毫秒的标准日期形式
     */
    public String toMsString() {
        return toString(Fields.NORM_DATETIME_MS_FORMAT);
    }

    /**
     * 转为"yyyy-MM-dd yyyy-MM-dd HH:mm:ss " 格式字符串
     * 如果时区被设置，会转换为其时区对应的时间，否则转换为当前地点对应的时区
     *
     * @return "yyyy-MM-dd yyyy-MM-dd HH:mm:ss " 格式字符串
     */
    @Override
    public String toString() {
        return toString(this.timeZone);
    }

    /**
     * 转为"yyyy-MM-dd yyyy-MM-dd HH:mm:ss " 格式字符串
     * 如果时区不为{@code null}，会转换为其时区对应的时间，否则转换为当前时间对应的时区
     *
     * @param timeZone 时区
     * @return "yyyy-MM-dd yyyy-MM-dd HH:mm:ss " 格式字符串
     */
    public String toString(TimeZone timeZone) {
        if (null != timeZone) {
            return toString(Formatter.newSimpleFormat(Fields.NORM_DATETIME_PATTERN, null, timeZone));
        }
        return toString(Fields.NORM_DATETIME_FORMAT);
    }

    /**
     * 转为字符串
     *
     * @param format 日期格式，常用格式见： {@link Fields}
     * @return String
     */
    public String toString(String format) {
        if (null != this.timeZone) {
            return toString(Formatter.newSimpleFormat(format, null, timeZone));
        }
        return toString(FormatBuilder.getInstance(format));
    }

    /**
     * 转为字符串
     *
     * @param format {@link DatePrinter} 或 {@link FormatBuilder}
     * @return String
     */
    public String toString(DatePrinter format) {
        return format.format(this);
    }

    /**
     * 转为字符串
     *
     * @param format {@link SimpleDateFormat}
     * @return String
     */
    public String toString(DateFormat format) {
        return format.format(this);
    }

    /**
     * 设置日期时间
     *
     * @param time 日期时间毫秒
     * @return this
     */
    private DateTime setTimeInternal(long time) {
        super.setTime(time);
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
     * 是否闰年
     *
     * @return 是否闰年
     * @see Almanac#isLeapYear(int)
     */
    public boolean isLeapYear() {
        return Almanac.isLeapYear(year());
    }

}
