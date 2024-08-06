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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.chrono.ChronoLocalDate;
import java.time.chrono.ChronoLocalDateTime;
import java.time.chrono.Era;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.UnsupportedTemporalTypeException;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.function.Function;

import org.miaixz.bus.core.center.date.format.CustomFormat;
import org.miaixz.bus.core.center.date.format.FormatBuilder;
import org.miaixz.bus.core.lang.Fields;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.LambdaKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.core.xyz.ZoneKit;

/**
 * 日期格式化和解析 yyyy-MM-dd HH:mm:ss yyyy-MM-dd HH:mm:ss yyyy-MM-dd HH:mm:ss.SSS yyyy-MM-dd HH:mm:ss.SSSSSS yyyy-MM-dd
 * HH:mm:ss.SSSSSSSSS yyyy-MM-dd'T'HH:mm:ss.SSSZ等等，支持毫秒、微秒和纳秒等精确时间
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Formatter {

    /**
     * 年月格式 {@link FormatBuilder}：yyyy-MM
     */
    public static final FormatBuilder NORM_MONTH_FORMAT = FormatBuilder.getInstance(Fields.NORM_MONTH);
    /**
     * 年月格式 {@link DateTimeFormatter}：yyyy-MM
     */
    public static final DateTimeFormatter NORM_MONTH_FORMATTER = FormatBuilder.getDateTimeInstance(Fields.NORM_MONTH);
    /**
     * 简单年月格式 {@link FormatBuilder}：yyyyMM
     */
    public static final FormatBuilder SIMPLE_MONTH_FORMAT = FormatBuilder.getInstance(Fields.SIMPLE_MONTH);
    /**
     * 简单年月格式 {@link DateTimeFormatter}：yyyyMM
     */
    public static final DateTimeFormatter SIMPLE_MONTH_FORMATTER = FormatBuilder
            .getDateTimeInstance(Fields.SIMPLE_MONTH);
    /**
     * 标准日期格式 {@link FormatBuilder}：yyyy-MM-dd
     */
    public static final FormatBuilder NORM_DATE_FORMAT = FormatBuilder.getInstance(Fields.NORM_DATE);
    /**
     * 标准日期格式 {@link DateTimeFormatter}：yyyy-MM-dd
     */
    public static final DateTimeFormatter NORM_DATE_FORMATTER = FormatBuilder.getDateTimeInstance(Fields.NORM_DATE);
    /**
     * 标准时间格式 {@link FormatBuilder}：HH:mm
     */
    public static final FormatBuilder NORM_HOUR_MINUTE_FORMAT = FormatBuilder.getInstance(Fields.NORM_HOUR_MINUTE);
    /**
     * 标准日期格式 {@link DateTimeFormatter}：HH:mm
     */
    public static final DateTimeFormatter NORM_HOUR_MINUTE_FORMATTER = FormatBuilder
            .getDateTimeInstance(Fields.NORM_HOUR_MINUTE);
    /**
     * 标准时间格式 {@link FormatBuilder}：HH:mm:ss
     */
    public static final FormatBuilder NORM_TIME_FORMAT = FormatBuilder.getInstance(Fields.NORM_TIME);
    /**
     * 标准日期格式 {@link DateTimeFormatter}：HH:mm:ss
     */
    public static final DateTimeFormatter NORM_TIME_FORMATTER = FormatBuilder.getDateTimeInstance(Fields.NORM_TIME);
    /**
     * 标准日期时间格式，精确到分 {@link FormatBuilder}：yyyy-MM-dd HH:mm
     */
    public static final FormatBuilder NORM_DATETIME_MINUTE_FORMAT = FormatBuilder
            .getInstance(Fields.NORM_DATETIME_MINUTE);
    /**
     * 标准日期格式 {@link DateTimeFormatter}：yyyy-MM-dd HH:mm
     */
    public static final DateTimeFormatter NORM_DATETIME_MINUTE_FORMATTER = FormatBuilder
            .getDateTimeInstance(Fields.NORM_DATETIME_MINUTE);
    /**
     * 标准日期时间格式，精确到秒 {@link FormatBuilder}：yyyy-MM-dd HH:mm:ss
     */
    public static final FormatBuilder NORM_DATETIME_FORMAT = FormatBuilder.getInstance(Fields.NORM_DATETIME);
    /**
     * 标准日期时间格式，精确到秒 {@link DateTimeFormatter}：yyyy-MM-dd HH:mm:ss
     */
    public static final DateTimeFormatter NORM_DATETIME_FORMATTER = FormatBuilder
            .getDateTimeInstance(Fields.NORM_DATETIME);
    /**
     * 标准日期时间格式，精确到毫秒 {@link FormatBuilder}：yyyy-MM-dd HH:mm:ss.SSS
     */
    public static final FormatBuilder NORM_DATETIME_MS_FORMAT = FormatBuilder.getInstance(Fields.NORM_DATETIME_MS);
    /**
     * 标准日期时间格式，精确到毫秒 {@link DateTimeFormatter}：yyyy-MM-dd HH:mm:ss.SSS
     */
    public static final DateTimeFormatter NORM_DATETIME_MS_FORMATTER = FormatBuilder
            .getDateTimeInstance(Fields.NORM_DATETIME_MS);
    /**
     * ISO8601日期时间格式，精确到毫秒 {@link FormatBuilder}：yyyy-MM-dd HH:mm:ss,SSS
     */
    public static final FormatBuilder NORM_DATETIME_COMMA_MS_FORMAT = FormatBuilder
            .getInstance(Fields.NORM_DATETIME_COMMA_MS);
    /**
     * 标准日期格式 {@link DateTimeFormatter}：yyyy-MM-dd HH:mm:ss,SSS
     */
    public static final DateTimeFormatter NORM_DATETIME_COMMA_MS_FORMATTER = FormatBuilder
            .getDateTimeInstance(Fields.NORM_DATETIME_COMMA_MS);
    /**
     * 标准日期格式 {@link FormatBuilder}：MM月dd日
     */
    public static final FormatBuilder CN_MONTH_FORMAT = FormatBuilder.getInstance(Fields.CN_MONTH);
    /**
     * 标准日期格式 {@link DateTimeFormatter}：MM月dd日
     */
    public static final DateTimeFormatter CN_MONTH_FORMATTER = FormatBuilder.getDateTimeInstance(Fields.CN_MONTH);
    /**
     * 标准日期格式 {@link FormatBuilder}：yyyy年MM月dd日
     */
    public static final FormatBuilder CN_DATE_FORMAT = FormatBuilder.getInstance(Fields.CN_DATE);
    /**
     * 标准日期格式 {@link DateTimeFormatter}：yyyy年MM月dd日
     */
    public static final DateTimeFormatter CN_DATE_FORMATTER = FormatBuilder.getDateTimeInstance(Fields.CN_DATE);
    /**
     * 标准日期格式 {@link FormatBuilder}：yyyy年MM月dd日HH时mm分ss秒
     */
    public static final FormatBuilder CN_DATE_TIME_FORMAT = FormatBuilder.getInstance(Fields.CN_DATE_TIME);
    /**
     * 标准日期格式 {@link DateTimeFormatter}：yyyy年MM月dd日HH时mm分ss秒
     */
    public static final DateTimeFormatter CN_DATE_TIME_FORMATTER = FormatBuilder
            .getDateTimeInstance(Fields.CN_DATE_TIME);
    /**
     * 标准日期格式 {@link FormatBuilder}：yyyyMMdd
     */
    public static final FormatBuilder PURE_DATE_FORMAT = FormatBuilder.getInstance(Fields.PURE_DATE);
    /**
     * 标准日期格式 {@link DateTimeFormatter}：yyyyMMdd
     */
    public static final DateTimeFormatter PURE_DATE_FORMATTER = FormatBuilder.getDateTimeInstance(Fields.PURE_DATE);
    /**
     * 标准日期格式 {@link FormatBuilder}：HHmm
     */
    public static final FormatBuilder PPURE_HOUR_MINUTE_FORMAT = FormatBuilder.getInstance(Fields.PURE_HOUR_MINUTE);
    /**
     * 标准日期格式 {@link DateTimeFormatter}：HHmm
     */
    public static final DateTimeFormatter PURE_HOUR_MINUTE_FORMATTER = FormatBuilder
            .getDateTimeInstance(Fields.PURE_HOUR_MINUTE);
    /**
     * 标准日期格式 {@link FormatBuilder}：HHmmss
     */
    public static final FormatBuilder PURE_TIME_FORMAT = FormatBuilder.getInstance(Fields.PURE_TIME);
    /**
     * 标准日期格式 {@link DateTimeFormatter}：HHmmss
     */
    public static final DateTimeFormatter PURE_TIME_FORMATTER = FormatBuilder.getDateTimeInstance(Fields.PURE_TIME);
    /**
     * 标准日期格式 {@link FormatBuilder}：yyyyMMddHHmmss
     */
    public static final FormatBuilder PURE_DATETIME_FORMAT = FormatBuilder.getInstance(Fields.PURE_DATETIME);
    /**
     * 标准日期格式 {@link DateTimeFormatter}：yyyyMMddHHmmss
     */
    public static final DateTimeFormatter PURE_DATETIME_FORMATTER = FormatBuilder
            .getDateTimeInstance(Fields.PURE_DATETIME);
    /**
     * 标准日期格式 {@link FormatBuilder}：yyyyMMddHHmmssSSS
     */
    public static final FormatBuilder PURE_DATETIME_MS_FORMAT = FormatBuilder.getInstance(Fields.PURE_DATETIME_MS);
    /**
     * 格式化通配符: {@link FormatBuilder} yyyyMMddHHmmss.SSS
     */
    public static final FormatBuilder PURE_DATETIME_TIP_FORMAT = FormatBuilder
            .getInstance(Fields.PURE_DATETIME_TIP_PATTERN);
    /**
     * 标准日期格式 {@link DateTimeFormatter}：yyyyMMddHHmmssSSS see
     * https://stackoverflow.com/questions/22588051/is-java-time-failing-to-parse-fraction-of-second jdk8 bug at:
     * https://bugs.openjdk.java.net/browse/JDK-8031085
     */
    public static final DateTimeFormatter PURE_DATETIME_MS_FORMATTER = new DateTimeFormatterBuilder()
            .appendPattern(Fields.PURE_DATETIME).appendValue(ChronoField.MILLI_OF_SECOND, 3).toFormatter();
    /**
     * HTTP头中日期时间格式 {@link FormatBuilder}：EEE, dd MMM yyyy HH:mm:ss GMT
     */
    public static final FormatBuilder HTTP_DATETIME_FORMAT_GMT = FormatBuilder.getInstance(Fields.HTTP_DATETIME,
            TimeZone.getTimeZone("GMT"), Locale.US);
    /**
     * HTTP头中日期时间格式 {@link FormatBuilder}：EEE, dd MMM yyyy HH:mm:ss z
     */
    public static final FormatBuilder HTTP_DATETIME_FORMAT = FormatBuilder.getInstance(Fields.HTTP_DATETIME, Locale.US);
    /**
     * JDK中日期时间格式 {@link FormatBuilder}：EEE MMM dd HH:mm:ss zzz yyyy
     */
    public static final FormatBuilder JDK_DATETIME_FORMAT = FormatBuilder.getInstance(Fields.JDK_DATETIME, Locale.US);
    /**
     * ISO8601日期时间{@link FormatBuilder}：yyyy-MM-dd'T'HH:mm:ss
     */
    public static final FormatBuilder ISO8601_FORMAT = FormatBuilder.getInstance(Fields.ISO8601);
    /**
     * UTC时间{@link FormatBuilder}：yyyy-MM-dd'T'HH:mm:ss.SSS
     */
    public static final FormatBuilder ISO8601_MS_FORMAT = FormatBuilder.getInstance(Fields.ISO8601_MS);
    /**
     * ISO8601时间{@link FormatBuilder}：yyyy-MM-dd'T'HH:mm:ss'Z'
     */
    public static final FormatBuilder UTC_FORMAT = FormatBuilder.getInstance(Fields.UTC, ZoneKit.ZONE_UTC);
    /**
     * ISO8601时间{@link FormatBuilder}：yyyy-MM-dd'T'HH:mm:ssZ，Z表示一个时间偏移，如+0800
     */
    public static final FormatBuilder ISO8601_WITH_ZONE_OFFSET_FORMAT = FormatBuilder
            .getInstance(Fields.ISO8601_WITH_ZONE_OFFSET);
    /**
     * ISO8601时间{@link FormatBuilder}：yyyy-MM-dd'T'HH:mm:ssXXX
     */
    public static final FormatBuilder ISO8601_WITH_XXX_OFFSET_FORMAT = FormatBuilder
            .getInstance(Fields.ISO8601_WITH_XXX_OFFSET);
    /**
     * ISO8601时间{@link FormatBuilder}：yyyy-MM-dd'T'HH:mm:ss.SSS'Z'
     */
    public static final FormatBuilder UTC_MS_FORMAT = FormatBuilder.getInstance(Fields.UTC_MS, ZoneKit.ZONE_UTC);
    /**
     * ISO8601时间{@link FormatBuilder}：yyyy-MM-dd'T'HH:mm:ss.SSSZ
     */
    public static final FormatBuilder ISO8601_MS_WITH_ZONE_OFFSET_FORMAT = FormatBuilder
            .getInstance(Fields.ISO8601_MS_WITH_ZONE_OFFSET);
    /**
     * UTC时间{@link FormatBuilder}：yyyy-MM-dd'T'HH:mm:ss.SSSXXX
     */
    public static final FormatBuilder ISO8601_MS_WITH_XXX_OFFSET_FORMAT = FormatBuilder
            .getInstance(Fields.ISO8601_MS_WITH_XXX_OFFSET);
    /**
     * 只有时分秒的最大时间
     */
    public static final LocalTime MAX_HMS = LocalTime.of(23, 59, 59);

    /**
     * 将指定的日期转换成Unix时间戳
     *
     * @param text 需要转换的日期 yyyy-MM-dd HH:mm:ss
     * @return long 时间戳
     */
    public static long format(String text) {
        return NORM_DATETIME_FORMAT.parse(text).getTime();

    }

    /**
     * 将Unix时间戳转换成日期
     *
     * @param timestamp 时间戳
     * @return String 日期字符串
     */
    public static String format(long timestamp) {
        return NORM_DATETIME_FORMAT.format(new Date(timestamp));
    }

    /**
     * 将Unix时间戳转换成日期
     *
     * @param timestamp 时间戳
     * @param format    格式
     * @return String 日期字符串
     */
    public static String format(long timestamp, String format) {
        return new SimpleDateFormat(format).format(new Date(timestamp));
    }

    /**
     * 将指定的日期转换成Unix时间戳
     *
     * @param text   需要转换的日期
     * @param format 格式
     * @return long 时间戳
     */
    public static long format(String text, String format) {
        try {
            return new SimpleDateFormat(format).parse(text).getTime();
        } catch (ParseException e) {
            throw new InternalException(e);
        }
    }

    /**
     * 格式化日期时间 格式 yyyy-MM-dd HH:mm:ss
     *
     * @param localDateTime 被格式化的日期
     * @return 格式化后的字符串
     */
    public static String format(LocalDateTime localDateTime) {
        return format(localDateTime, Fields.NORM_DATETIME);
    }

    /**
     * 根据特定格式格式化日期
     *
     * @param localDateTime 被格式化的日期
     * @param format        日期格式，常用格式见： {@link Fields}
     * @return 格式化后的字符串
     */
    public static String format(final LocalDateTime localDateTime, final String format) {
        return Formatter.format(localDateTime, format);
    }

    /**
     * 格式化日期时间为指定格式 如果为{@link Month}，调用{@link Month#toString()}
     *
     * @param time      {@link TemporalAccessor}
     * @param formatter 日期格式化器，预定义的格式见：{@link DateTimeFormatter}
     * @return 格式化后的字符串
     */
    public static String format(final TemporalAccessor time, DateTimeFormatter formatter) {
        if (null == time) {
            return null;
        }

        if (time instanceof Month) {
            return time.toString();
        }

        if (null == formatter) {
            formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        }

        try {
            return formatter.format(time);
        } catch (final UnsupportedTemporalTypeException e) {
            if (time instanceof LocalDate && e.getMessage().contains("HourOfDay")) {
                // 用户传入LocalDate，但是要求格式化带有时间部分，转换为LocalDateTime重试
                return formatter.format(((LocalDate) time).atStartOfDay());
            } else if (time instanceof LocalTime && e.getMessage().contains("YearOfEra")) {
                // 用户传入LocalTime，但是要求格式化带有日期部分，转换为LocalDateTime重试
                return formatter.format(((LocalTime) time).atDate(LocalDate.now()));
            } else if (time instanceof Instant) {
                // 时间戳没有时区信息，赋予默认时区
                return formatter.format(((Instant) time).atZone(ZoneId.systemDefault()));
            }
            throw e;
        }
    }

    /**
     * 格式化日期时间为指定格式 如果为{@link Month}，调用{@link Month#toString()}
     *
     * @param time   {@link TemporalAccessor}
     * @param format 日期格式
     * @return 格式化后的字符串
     */
    public static String format(final TemporalAccessor time, final String format) {
        if (null == time) {
            return null;
        }

        if (time instanceof DayOfWeek || time instanceof java.time.Month || time instanceof Era
                || time instanceof MonthDay) {
            return time.toString();
        }

        // 检查自定义格式
        if (CustomFormat.isCustomFormat(format)) {
            return CustomFormat.format(time, format);
        }

        final DateTimeFormatter formatter = StringKit.isBlank(format) ? null : DateTimeFormatter.ofPattern(format);

        return format(time, formatter);
    }

    /**
     * 按照给定的通配模式,格式化成相应的时间字符串
     *
     * @param text        原始时间字符串
     * @param srcPattern  原始时间通配符
     * @param destPattern 格式化成的时间通配符
     * @return 格式化成功返回成功后的字符串, 失败返回<b>""</b>
     */
    public static String format(String text, String srcPattern, String destPattern) {
        try {
            SimpleDateFormat srcSdf = new SimpleDateFormat(srcPattern);
            SimpleDateFormat dstSdf = new SimpleDateFormat(destPattern);
            return dstSdf.format(srcSdf.parse(text));
        } catch (ParseException e) {
            return Normal.EMPTY;
        }
    }

    /**
     * 格式化日期时间为yyyy-MM-dd HH:mm:ss格式
     *
     * @param time {@link LocalDateTime}
     * @return 格式化后的字符串
     */
    public static String format(final ChronoLocalDateTime<?> time) {
        return format(time, NORM_DATETIME_FORMATTER);
    }

    /**
     * 格式化日期时间为yyyy-MM-dd格式
     *
     * @param date {@link LocalDate}
     * @return 格式化后的字符串
     */
    public static String format(final ChronoLocalDate date) {
        return format(date, NORM_DATE_FORMATTER);
    }

    /**
     * 格式化时间函数
     *
     * @param dateTimeFormatter {@link DateTimeFormatter}
     * @return 格式化时间的函数
     */
    public static Function<TemporalAccessor, String> format(final DateTimeFormatter dateTimeFormatter) {
        return LambdaKit.toFunction(Formatter::format, dateTimeFormatter);
    }

    /**
     * 创建{@link SimpleDateFormat}，注意此对象非线程安全！ 此对象默认为严格格式模式，即parse时如果格式不正确会报错。
     *
     * @param pattern 表达式
     * @return {@link SimpleDateFormat}
     */
    public static SimpleDateFormat newSimpleFormat(final String pattern) {
        return newSimpleFormat(pattern, null, null);
    }

    /**
     * 创建{@link SimpleDateFormat}，注意此对象非线程安全！ 此对象默认为严格格式模式，即parse时如果格式不正确会报错。
     *
     * @param pattern  表达式
     * @param locale   {@link Locale}，{@code null}表示默认
     * @param timeZone {@link TimeZone}，{@code null}表示默认
     * @return {@link SimpleDateFormat}
     */
    public static SimpleDateFormat newSimpleFormat(final String pattern, Locale locale, final TimeZone timeZone) {
        if (null == locale) {
            locale = Locale.getDefault(Locale.Category.FORMAT);
        }
        final SimpleDateFormat format = new SimpleDateFormat(pattern, locale);
        if (null != timeZone) {
            format.setTimeZone(timeZone);
        }
        format.setLenient(false);
        return format;
    }

}
