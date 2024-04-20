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

import org.miaixz.bus.core.convert.NumberFormatter;
import org.miaixz.bus.core.date.formatter.DatePeriod;
import org.miaixz.bus.core.date.formatter.DatePrinter;
import org.miaixz.bus.core.date.formatter.FormatBuilder;
import org.miaixz.bus.core.date.formatter.parser.*;
import org.miaixz.bus.core.exception.InternalException;
import org.miaixz.bus.core.lang.Fields;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.RegEx;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.toolkit.MathKit;
import org.miaixz.bus.core.toolkit.ObjectKit;
import org.miaixz.bus.core.toolkit.PatternKit;
import org.miaixz.bus.core.toolkit.StringKit;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.chrono.Era;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.UnsupportedTemporalTypeException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 日期格式化和解析
 * yyyy-MM-dd
 * HH:mm:ss
 * yyyy-MM-dd HH:mm:ss
 * yyyy-MM-dd HH:mm:ss.SSS
 * yyyy-MM-dd HH:mm:ss.SSSSSS
 * yyyy-MM-dd HH:mm:ss.SSSSSSSSS
 * yyyy-MM-dd'T'HH:mm:ss.SSSZ等等，支持毫秒、微秒和纳秒等精确时间
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Formatter {

    /**
     * 按照给定的通配模式 YYYY-MM-DD HH:MM:SS ,将时间格式化成相应的字符串
     *
     * @param date 待格式化的时间
     * @return 格式化成功返回成功后的字符串, 失败返回<b>null</b>
     */
    public static String format(Date date) {
        if (null != date) {
            return Fields.NORM_DATETIME_FORMAT.format(date);
        }
        return Normal.EMPTY;
    }

    /**
     * 根据特定格式格式化日期
     *
     * @param date   被格式化的日期
     * @param format 日期格式,常用格式见： {@link Fields}
     * @return 格式化后的字符串
     */
    public static String format(Date date, String format) {
        if (null == date || StringKit.isBlank(format)) {
            return null;
        }

        final SimpleDateFormat sdf = new SimpleDateFormat(format);
        if (date instanceof DateTime) {
            final TimeZone timeZone = ((DateTime) date).getTimeZone();
            if (null != timeZone) {
                sdf.setTimeZone(timeZone);
            }
        }
        return format(date, sdf);
    }

    /**
     * 根据特定格式格式化日期
     *
     * @param date   被格式化的日期
     * @param format {@link DatePrinter} 或 {@link FormatBuilder}
     * @return 格式化后的字符串
     */
    public static String format(Date date, DatePrinter format) {
        if (null == format || null == date) {
            return null;
        }
        return format.format(date);
    }

    /**
     * 根据特定格式格式化日期
     *
     * @param date   被格式化的日期
     * @param format {@link SimpleDateFormat}
     * @return 格式化后的字符串
     */
    public static String format(Date date, DateFormat format) {
        if (null == format || null == date) {
            return null;
        }
        return format.format(date);
    }

    /**
     * 根据特定格式格式化日期
     *
     * @param date   被格式化的日期
     * @param format {@link SimpleDateFormat} {@link Fields#NORM_DATETIME_FORMAT}
     * @return 格式化后的字符串
     */
    public static String format(Date date, DateTimeFormatter format) {
        if (null == format || null == date) {
            return null;
        }
        return Formatter.format(date.toInstant(), format);
    }

    /**
     * 根据特定格式格式化日期
     *
     * @param date     被格式化的日期
     * @param format   日期格式,常用格式见： {@link Fields}
     * @param timeZone 时区
     * @return 格式化后的字符串
     */
    public static String format(Date date, String format, String timeZone) {
        if (null == date || StringKit.isBlank(format)) {
            return null;
        }

        final SimpleDateFormat sdf = new SimpleDateFormat(format);
        if (null != timeZone) {
            sdf.setTimeZone(new SimpleTimeZone(0, timeZone));
        }
        return format(date, sdf);
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
     * 将指定的日期转换成Unix时间戳
     *
     * @param text 需要转换的日期 yyyy-MM-dd HH:mm:ss
     * @return long 时间戳
     */
    public static long format(String text) {
        try {
            return Fields.NORM_DATETIME_FORMAT.parse(text).getTime();
        } catch (ParseException e) {
            throw new InternalException(e);
        }

    }

    /**
     * 将Unix时间戳转换成日期
     *
     * @param timestamp 时间戳
     * @return String 日期字符串
     */
    public static String format(long timestamp) {
        return Fields.NORM_DATETIME_FORMAT.format(new Date(timestamp));
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
     * 格式化日期时间
     * 格式 yyyy-MM-dd HH:mm:ss
     *
     * @param localDateTime 被格式化的日期
     * @return 格式化后的字符串
     */
    public static String format(LocalDateTime localDateTime) {
        return format(localDateTime, Fields.NORM_DATETIME_PATTERN);
    }

    /**
     * 根据特定格式格式化日期
     *
     * @param localDateTime 被格式化的日期
     * @param format        日期格式，常用格式见： {@link Fields}
     * @return 格式化后的字符串
     */
    public static String format(LocalDateTime localDateTime, String format) {
        if (null == localDateTime || StringKit.isBlank(format)) {
            return null;
        }
        DateTimeFormatter df = DateTimeFormatter.ofPattern(format);
        return localDateTime.format(df);
    }

    /**
     * 格式化日期时间为指定格式
     * 如果为{@link Month}，调用{@link Month#toString()}
     *
     * @param time      {@link TemporalAccessor}
     * @param formatter 日期格式化器，预定义的格式见：{@link DateTimeFormatter}
     * @return 格式化后的字符串
     */
    public static String format(TemporalAccessor time, DateTimeFormatter formatter) {
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
        } catch (UnsupportedTemporalTypeException e) {
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
     * 格式化日期时间为指定格式
     * 如果为{@link Month}，调用{@link Month#toString()}
     *
     * @param time   {@link TemporalAccessor}
     * @param format 日期格式
     * @return 格式化后的字符串
     */
    public static String format(TemporalAccessor time, String format) {
        if (null == time) {
            return null;
        }

        if (time instanceof Month) {
            return time.toString();
        }

        if (time instanceof DayOfWeek
                || time instanceof Month
                || time instanceof Era
                || time instanceof MonthDay) {
            return time.toString();
        }

        final DateTimeFormatter formatter = StringKit.isBlank(format)
                ? null : DateTimeFormatter.ofPattern(format);

        return format(time, formatter);
    }

    /**
     * 格式化为中文日期格式，如果isUppercase为false
     * 则返回：2018年10月24日，否则,返回二〇一八年十月二十四日
     *
     * @param date        被格式化的日期
     * @param isUppercase 是否采用大写形式
     * @param withTime    是否包含时间部分
     * @return 中文日期字符串
     */
    public static String format(Date date, boolean isUppercase, boolean withTime) {
        if (null == date) {
            return null;
        }

        if (false == isUppercase) {
            return (withTime ? Fields.NORM_CN_DATE_TIME_FORMAT : Fields.NORM_DATE_CN_FORMAT).format(date);
        }

        return format(Converter.toCalendar(date), withTime);
    }

    /**
     * 将指定Calendar时间格式化为纯中文形式
     *
     * <pre>
     *     2018-02-24 12:13:14 转换为 二〇一八年二月二十四日（withTime为false）
     *     2018-02-24 12:13:14 转换为 二〇一八年二月二十四日一十二时一十三分一十四秒（withTime为true）
     * </pre>
     *
     * @param calendar {@link Calendar}
     * @param withTime 是否包含时间部分
     * @return 格式化后的字符串
     */
    public static String format(Calendar calendar, boolean withTime) {
        final StringBuilder result = StringKit.builder();

        // 年
        final String year = String.valueOf(calendar.get(Calendar.YEAR));
        final int length = year.length();
        for (int i = 0; i < length; i++) {
            result.append(NumberFormatter.Chinese.numberCharToChinese(year.charAt(i), false));
        }
        result.append('年');

        // 月
        int month = calendar.get(Calendar.MONTH) + 1;
        result.append(NumberFormatter.Chinese.formatThousand(month, false));
        result.append('月');

        // 日
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        result.append(NumberFormatter.Chinese.formatThousand(day, false));
        result.append('日');

        // 只替换年月日，时分秒中零不需要替换
        String temp = result.toString().replace('零', '〇');
        result.delete(0, result.length());
        result.append(temp);


        if (withTime) {
            // 时
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            result.append(NumberFormatter.Chinese.formatThousand(hour, false));
            result.append('时');
            // 分
            int minute = calendar.get(Calendar.MINUTE);
            result.append(NumberFormatter.Chinese.formatThousand(minute, false));
            result.append('分');
            // 秒
            int second = calendar.get(Calendar.SECOND);
            result.append(NumberFormatter.Chinese.formatThousand(second, false));
            result.append('秒');
        }

        return result.toString().replace('零', '〇');
    }

    /**
     * 格式化日期部分(不包括时间)
     * 格式 yyyy-MM-dd
     *
     * @param date 被格式化的日期
     * @return 格式化后的字符串
     */
    public static String formatDate(Date date) {
        return formatDate(date, false);
    }

    /**
     * 格式化日期部分(不包括时间)
     * 格式 yyyy-MM-dd
     *
     * @param date   被格式化的日期
     * @param isHttp 是否http格式
     * @return 格式化后的字符串
     */
    public static String formatDate(Date date, boolean isHttp) {
        if (null == date) {
            return null;
        }
        if (isHttp) {
            // 格式化为Http的标准日期格式
            return Fields.HTTP_DATETIME_FORMAT.format(date);
        }
        return Fields.NORM_DATE_FORMAT.format(date);
    }

    /**
     * 格式化时间
     * 格式 HH:mm:ss
     *
     * @param date 被格式化的日期
     * @return 格式化后的字符串
     */
    public static String formatTime(Date date) {
        if (null == date) {
            return null;
        }
        return Fields.NORM_TIME_FORMAT.format(date);
    }

    /**
     * 格式化日期间隔输出
     *
     * @param beginDate 起始日期
     * @param endDate   结束日期
     * @param units     级别,按照天、小时、分、秒、毫秒分为5个等级
     * @return XX天XX小时XX分XX秒
     */
    public static String formatBetween(Date beginDate, Date endDate, Fields.Units units) {
        return formatBetween(Almanac.between(beginDate, endDate, Fields.Units.MILLISECOND), units);
    }

    /**
     * 格式化日期间隔输出,精确到毫秒
     *
     * @param beginDate 起始日期
     * @param endDate   结束日期
     * @return XX天XX小时XX分XX秒
     */
    public static String formatBetween(Date beginDate, Date endDate) {
        return formatBetween(Almanac.between(beginDate, endDate, Fields.Units.MILLISECOND));
    }

    /**
     * 格式化日期间隔输出
     *
     * @param betweenMs 日期间隔
     * @param units     级别,按照天、小时、分、秒、毫秒分为5个等级
     * @return XX天XX小时XX分XX秒XX毫秒
     */
    public static String formatBetween(long betweenMs, Fields.Units units) {
        return new DatePeriod(betweenMs, units).format();
    }

    /**
     * 格式化日期间隔输出,精确到毫秒
     *
     * @param betweenMs 日期间隔
     * @return XX天XX小时XX分XX秒XX毫秒
     */
    public static String formatBetween(long betweenMs) {
        return new DatePeriod(betweenMs, Fields.Units.MILLISECOND).format();
    }

    /**
     * 构建DateTime对象
     *
     * @param text       Date字符串
     * @param dateFormat 格式化器 {@link SimpleDateFormat}
     * @return DateTime对象
     */
    public static DateTime parse(final CharSequence text, final DateFormat dateFormat) {
        return new DateTime(text, dateFormat);
    }

    /**
     * 构建DateTime对象
     *
     * @param text   Date字符串
     * @param parser 格式化器,{@link FormatBuilder}
     * @return DateTime对象
     */
    public static DateTime parse(final CharSequence text, final PositionDateParser parser) {
        return new DateTime(text, parser);
    }

    /**
     * 构建DateTime对象
     *
     * @param text    Date字符串
     * @param parser  格式化器,{@link FormatBuilder}
     * @param lenient 是否宽容模式
     * @return DateTime对象
     */
    public static DateTime parse(final CharSequence text, final PositionDateParser parser, final boolean lenient) {
        return new DateTime(text, parser, lenient);
    }

    /**
     * 构建DateTime对象
     *
     * @param text      Date字符串
     * @param formatter 格式化器,{@link DateTimeFormatter}
     * @return DateTime对象
     */
    public static DateTime parse(final CharSequence text, final DateTimeFormatter formatter) {
        return new DateTime(text, formatter);
    }

    /**
     * 将特定格式的日期转换为Date对象
     *
     * @param text   特定格式的日期
     * @param format 格式，例如yyyy-MM-dd
     * @return 日期对象
     */
    public static DateTime parse(final CharSequence text, final String format) {
        return new DateTime(text, format);
    }

    /**
     * 将特定格式的日期转换为Date对象
     *
     * @param text   特定格式的日期
     * @param format 格式，例如yyyy-MM-dd
     * @param locale 区域信息
     * @return 日期对象
     */
    public static DateTime parse(final CharSequence text, final String format, final Locale locale) {
        return new DateTime(text, newSimpleFormat(format, locale, null));
    }

    /**
     * 通过给定的日期格式解析日期时间字符串
     * 传入的日期格式会逐个尝试，直到解析成功，返回{@link DateTime}对象，否则抛出{@link InternalException}异常
     *
     * @param text   日期时间字符串，非空
     * @param format 需要尝试的日期时间格式数组，非空, 见SimpleDateFormat
     * @return 解析后的Date
     * @throws IllegalArgumentException if the date string or pattern array is null
     * @throws InternalException        if none of the date patterns were suitable
     */
    public static DateTime parse(final String text, final String... format) throws InternalException {
        return new DateTime(Formatter.parseByPatterns(text, format));
    }

    /**
     * 将日期字符串转换为{@link DateTime}对象，格式：
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
     * <li>yyyy-MM-dd HH:mm:ss.SSSSSS</li>
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
     * @param text 日期字符串
     * @return 日期
     */
    public static DateTime parse(final CharSequence text) {
        if (StringKit.isBlank(text)) {
            return null;
        }
        String dateText = text.toString();
        // 去掉两边空格并去掉中文日期中的“日”和“秒”，以规范长度
        dateText = StringKit.removeAll(dateText.trim(), '日', '秒');

        if (MathKit.isNumber(dateText)) {
            // 纯数字形式
            return PureDateParser.INSTANCE.parse(dateText);
        } else if (PatternKit.isMatch(RegEx.TIME, dateText)) {
            // HH:mm:ss 或者 HH:mm 时间格式匹配单独解析
            return FastTimeParser.INSTANCE.parse(dateText);
        } else if (StringKit.containsAnyIgnoreCase(dateText, Fields.WTB)) {
            // JDK的Date对象toString默认格式，类似于：
            // Tue Jun 4 16:25:15 +0800 2019
            // Thu May 16 17:57:18 GMT+08:00 2019
            // Wed Aug 01 00:00:00 CST 2012
            return CSTDateParser.INSTANCE.parse(dateText);
        } else if (StringKit.contains(dateText, 'T')) {
            // UTC时间
            return UTCDateParser.INSTANCE.parse(dateText);
        }

        //标准日期格式（包括单个数字的日期时间）
        dateText = normalize(dateText);
        if (PatternKit.isMatch(Fields.REGEX_NORM, dateText)) {
            return NormalDateParser.INSTANCE.parse(dateText);
        }

        // 没有更多匹配的时间格式
        throw new InternalException("No format fit for date String [{}] !", dateText);
    }

    /**
     * 通过给定的日期格式解析日期时间字符串
     * 传入的日期格式会逐个尝试，直到解析成功，返回{@link Calendar}对象
     *
     * @param text    日期时间字符串，非空
     * @param pattern 需要尝试的日期时间格式数组，非空, 见SimpleDateFormat
     * @return 解析后的 {@link Calendar}
     */
    public static Calendar parseByPatterns(String text, String... pattern) {
        return parseByPatterns(text, null, pattern);
    }

    /**
     * 通过给定的日期格式解析日期时间字符串
     * 传入的日期格式会逐个尝试，直到解析成功，返回{@link Calendar}对象
     *
     * @param text    日期时间字符串，非空
     * @param locale  地区，当为{@code null}时使用{@link Locale#getDefault()}
     * @param pattern 需要尝试的日期时间格式数组，非空, 见SimpleDateFormat
     * @return 解析后的 {@link Calendar}
     */
    public static Calendar parseByPatterns(String text, Locale locale, String... pattern) {
        return parseByPatterns(text, locale, true, pattern);
    }

    /**
     * 使用指定{@link DateParser}解析字符串为{@link Calendar}
     *
     * @param text    日期字符串
     * @param lenient 是否宽容模式
     * @param parser  {@link DateParser}
     * @return 解析后的 {@link Calendar}，解析失败返回{@code null}
     */
    public static Calendar parseByPatterns(final CharSequence text, final boolean lenient, final PositionDateParser parser) {
        final Calendar calendar = Calendar.getInstance(parser.getTimeZone(), parser.getLocale());
        calendar.clear();
        calendar.setLenient(lenient);

        return parser.parse(StringKit.toString(text), new ParsePosition(0), calendar) ? calendar : null;
    }

    /**
     * 通过给定的日期格式解析日期时间字符串
     * 传入的日期格式会逐个尝试，直到解析成功，返回{@link Calendar}对象
     *
     * @param text    日期时间字符串，非空
     * @param locale  地区，当为{@code null}时使用{@link Locale#getDefault()}
     * @param lenient 日期时间解析是否使用严格模式
     * @param pattern 需要尝试的日期时间格式数组，非空
     * @return 解析后的 {@link Calendar}
     * @see java.util.Calendar#isLenient()
     */
    public static Calendar parseByPatterns(String text, Locale locale, boolean lenient, String... pattern) {
        if (null == text || null == pattern) {
            throw new IllegalArgumentException("Date and Patterns must not be null");
        }

        final TimeZone tz = TimeZone.getDefault();
        final Locale lcl = ObjectKit.defaultIfNull(locale, Locale.getDefault());
        final ParsePosition pos = new ParsePosition(0);
        final Calendar calendar = Calendar.getInstance(tz, lcl);
        calendar.setLenient(lenient);

        for (final String parsePattern : pattern) {
            final FastDateParser fdp = new FastDateParser(parsePattern, tz, lcl);
            calendar.clear();
            try {
                if (fdp.parse(text, pos, calendar) && pos.getIndex() == text.length()) {
                    return calendar;
                }
            } catch (final IllegalArgumentException ignore) {
                // leniency is preventing calendar from being set
            }
            pos.setIndex(0);
        }
        throw new InternalException("Unable to parse the date: {}", text);
    }

    /**
     * 创建{@link SimpleDateFormat}，注意此对象非线程安全
     * 此对象默认为严格格式模式，即parse时如果格式不正确会报错
     *
     * @param pattern 表达式
     * @return {@link SimpleDateFormat}
     */
    public static SimpleDateFormat newSimpleFormat(String pattern) {
        return newSimpleFormat(pattern, null, null);
    }

    /**
     * 创建{@link SimpleDateFormat}，注意此对象非线程安全
     * 此对象默认为严格格式模式，即parse时如果格式不正确会报错
     *
     * @param pattern  表达式
     * @param locale   {@link Locale}，{@code null}表示默认
     * @param timeZone {@link TimeZone}，{@code null}表示默认
     * @return {@link SimpleDateFormat}
     */
    public static SimpleDateFormat newSimpleFormat(String pattern, Locale locale, TimeZone timeZone) {
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

    /**
     * 获取时长单位简写
     *
     * @param unit 单位
     * @return 单位简写名称
     */
    public static String getShotName(TimeUnit unit) {
        switch (unit) {
            case NANOSECONDS:
                return "ns";
            case MICROSECONDS:
                return "μs";
            case MILLISECONDS:
                return "ms";
            case SECONDS:
                return "s";
            case MINUTES:
                return "min";
            case HOURS:
                return "h";
            default:
                return unit.name().toLowerCase();
        }
    }

    /**
     * 标准化日期，默认处理以空格区分的日期时间格式，空格前为日期
     * 将以下字符替换为"-"
     * <pre>
     * "."
     * "/"
     * "年"
     * "月"
     * </pre>
     *
     * <p>
     * 将以下字符替换为":"
     * <pre>
     * "时"
     * "分"
     * "秒"
     * </pre>
     *
     * <p>
     * 将以下字符去除
     * <pre>
     * "日"
     * </pre>
     *
     * <p>
     * 当末位是":"时去除之(不存在毫秒时)
     *
     * @param text 日期时间字符串
     * @return 格式化后的日期字符串
     */
    private static String normalize(CharSequence text) {
        if (StringKit.isBlank(text)) {
            return StringKit.toString(text);
        }

        // 日期时间分开处理
        final List<String> dateAndTime = StringKit.splitTrim(text, Symbol.C_SPACE);
        final int size = dateAndTime.size();
        if (size < 1 || size > 2) {
            // 非可被标准处理的格式
            return StringKit.toString(text);
        }

        final StringBuilder builder = StringKit.builder();

        // 日期部分("\"、"/"、"."、"年"、"月"都替换为"-")
        String datePart = dateAndTime.get(0).replaceAll("[/.年月]", Symbol.MINUS);
        datePart = StringKit.removeSuffix(datePart, "日");
        builder.append(datePart);

        // 时间部分
        if (size == 2) {
            builder.append(Symbol.C_SPACE);
            String timePart = dateAndTime.get(1).replaceAll("[时分秒]", Symbol.COLON);
            timePart = StringKit.removeSuffix(timePart, Symbol.COLON);
            //将ISO8601中的逗号替换为.
            timePart = timePart.replace(Symbol.C_COMMA, Symbol.C_DOT);
            builder.append(timePart);
        }
        return builder.toString();
    }

}
