/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2021 aoju.org and other contributors.                      *
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
package org.aoju.bus.core.date;

import org.aoju.bus.core.convert.NumberFormatter;
import org.aoju.bus.core.date.formatter.*;
import org.aoju.bus.core.lang.Fields;
import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.core.lang.RegEx;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.toolkit.*;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
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
 * @version 6.3.1
 * @since JDK 1.8+
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
     * 按照给定的通配模式,格式化成相应的时间字符串
     *
     * @param srcDate     原始时间字符串
     * @param srcPattern  原始时间通配符
     * @param destPattern 格式化成的时间通配符
     * @return 格式化成功返回成功后的字符串, 失败返回<b>""</b>
     */
    public static String format(String srcDate, String srcPattern, String destPattern) {
        try {
            SimpleDateFormat srcSdf = new SimpleDateFormat(srcPattern);
            SimpleDateFormat dstSdf = new SimpleDateFormat(destPattern);
            return dstSdf.format(srcSdf.parse(srcDate));
        } catch (ParseException e) {
            return Normal.EMPTY;
        }
    }

    /**
     * 将指定的日期转换成Unix时间戳
     *
     * @param date 需要转换的日期 yyyy-MM-dd HH:mm:ss
     * @return long 时间戳
     */
    public static long format(String date) {
        try {
            return Fields.NORM_DATETIME_FORMAT.parse(date).getTime();
        } catch (ParseException e) {
            throw new InstrumentException(e);
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
     * @param date   需要转换的日期
     * @param format 格式
     * @return long 时间戳
     */
    public static long format(String date, String format) {
        try {
            return new SimpleDateFormat(format).parse(date).getTime();
        } catch (ParseException e) {
            throw new InstrumentException(e);
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
     *
     * @param time      {@link TemporalAccessor}
     * @param formatter 日期格式化器，预定义的格式见：{@link DateTimeFormatter}
     * @return 格式化后的字符串
     */
    public static String format(TemporalAccessor time, DateTimeFormatter formatter) {
        if (null == time) {
            return null;
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
     *
     * @param time   {@link TemporalAccessor}
     * @param format 日期格式
     * @return 格式化后的字符串
     */
    public static String format(TemporalAccessor time, String format) {
        if (null == time) {
            return null;
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

        return parse(Converter.toCalendar(date), withTime);
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
        String year = String.valueOf(calendar.get(Calendar.YEAR));
        final int length = year.length();
        for (int i = 0; i < length; i++) {
            result.append(NumberFormatter.toChinese(year.charAt(i), false));
        }
        result.append('年');

        // 月
        int month = calendar.get(Calendar.MONTH) + 1;
        result.append(NumberFormatter.format(month, false));
        result.append('月');

        // 日
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        result.append(NumberFormatter.format(day, false));
        result.append('日');

        if (withTime) {
            // 时
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            result.append(NumberFormatter.format(hour, false));
            result.append('时');
            // 分
            int minute = calendar.get(Calendar.MINUTE);
            result.append(NumberFormatter.format(minute, false));
            result.append('分');
            // 秒
            int second = calendar.get(Calendar.SECOND);
            result.append(NumberFormatter.format(second, false));
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
    public static String parse(Calendar calendar, boolean withTime) {
        final StringBuilder result = StringKit.builder();

        // 年
        String year = String.valueOf(calendar.get(Calendar.YEAR));
        final int length = year.length();
        for (int i = 0; i < length; i++) {
            result.append(NumberFormatter.toChinese(year.charAt(i), false));
        }
        result.append('年');

        // 月
        int month = calendar.get(Calendar.MONTH) + 1;
        result.append(NumberFormatter.format(month, false));
        result.append('月');

        // 日
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        result.append(NumberFormatter.format(day, false));
        result.append('日');

        if (withTime) {
            // 时
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            result.append(NumberFormatter.format(hour, false));
            result.append('时');
            // 分
            int minute = calendar.get(Calendar.MINUTE);
            result.append(NumberFormatter.format(minute, false));
            result.append('分');
            // 秒
            int second = calendar.get(Calendar.SECOND);
            result.append(NumberFormatter.format(second, false));
            result.append('秒');
        }

        return result.toString().replace('零', '〇');
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
    public static DateTime parse(CharSequence text) {
        if (StringKit.isBlank(text)) {
            return null;
        }
        String dateStr = text.toString();
        // 去掉两边空格并去掉中文日期中的“日”和“秒”，以规范长度
        dateStr = StringKit.removeAll(dateStr.trim(), '日', '秒');
        int length = dateStr.length();

        if (MathKit.isNumber(dateStr)) {
            // 纯数字形式
            if (length == Fields.PURE_DATETIME_PATTERN.length()) {
                return parse(dateStr, Fields.PURE_DATETIME_FORMAT);
            } else if (length == Fields.PURE_DATETIME_MS_PATTERN.length()) {
                return parse(dateStr, Fields.PURE_DATETIME_MS_FORMAT);
            } else if (length == Fields.PURE_DATE_PATTERN.length()) {
                return parse(dateStr, Fields.PURE_DATE_FORMAT);
            } else if (length == Fields.PURE_TIME_PATTERN.length()) {
                return parse(dateStr, Fields.PURE_TIME_FORMAT);
            }
        } else if (PatternKit.isMatch(RegEx.TIME, dateStr)) {
            // HH:mm:ss 或者 HH:mm 时间格式匹配单独解析
            return parseTimeToday(dateStr);
        } else if (StringKit.containsAnyIgnoreCase(dateStr, Fields.WTB)) {
            // JDK的Date对象toString默认格式，类似于：
            // Tue Jan 07 15:22:15 +0800 2020
            // Wed Jan 08 00:00:00 CST 2020
            // Thu Jan 09 17:51:10 GMT+08:00 2020
            return parseCST(dateStr);
        } else if (StringKit.contains(dateStr, 'T')) {
            // UTC时间
            return parseUTC(dateStr);
        }

        // 含有单个位数数字的日期时间格式
        dateStr = normalize(dateStr);
        if (PatternKit.isMatch(Fields.REGEX_NORM, dateStr)) {
            final int colonCount = CharsKit.count(dateStr, Symbol.COLON);
            switch (colonCount) {
                case 0:
                    // yyyy-MM-dd
                    return parse(dateStr, Fields.NORM_DATE_FORMAT);
                case 1:
                    // yyyy-MM-dd HH:mm
                    return parse(dateStr, Fields.NORM_DATETIME_MINUTE_FORMAT);
                case 2:
                    final int indexOfDot = StringKit.indexOf(dateStr, Symbol.C_DOT);
                    if (indexOfDot > 0) {
                        final int length1 = dateStr.length();
                        // yyyy-MM-dd HH:mm:ss.SSS 或者 yyyy-MM-dd HH:mm:ss.SSSSSS
                        if (length1 - indexOfDot > 4) {
                            // 类似yyyy-MM-dd HH:mm:ss.SSSSSS，采取截断操作
                            dateStr = StringKit.subPre(dateStr, indexOfDot + 4);
                        }
                        return parse(dateStr, Fields.NORM_DATETIME_MS_FORMAT);
                    }
                    // yyyy-MM-dd HH:mm:ss
                    return parse(dateStr, Fields.NORM_DATETIME_FORMAT);
            }
        }

        // 没有更多匹配的时间格式
        throw new InstrumentException("No format fit for date String [{}] !", dateStr);
    }

    /**
     * 构建LocalDateTime对象
     *
     * @param text    时间字符串(带格式)
     * @param pattern 使用{@link Fields}定义的格式
     * @return LocalDateTime对象
     */
    public static LocalDateTime parse(CharSequence text, String pattern) {
        text = normalize(text);
        DateTimeFormatter df = DateTimeFormatter.ofPattern(pattern);
        try {
            return LocalDateTime.parse(text, df);
        } catch (DateTimeParseException e) {
            // 在给定日期字符串没有时间部分时，LocalDateTime会报错，此时使用LocalDate中转转换
            return LocalDate.parse(text, df).atStartOfDay();
        }
    }

    /**
     * 构建DateTime对象
     *
     * @param text   Date字符串
     * @param format 格式化器 {@link SimpleDateFormat}
     * @return DateTime对象
     */
    public static DateTime parse(String text, DateFormat format) {
        return new DateTime(text, format);
    }

    /**
     * 构建DateTime对象
     *
     * @param text   Date字符串
     * @param parser 格式化器,{@link FormatBuilder}
     * @return DateTime对象
     */
    public static DateTime parse(String text, DateParser parser) {
        return new DateTime(text, parser);
    }

    /**
     * 将特定格式的日期转换为Date对象
     *
     * @param text    特定格式的日期
     * @param pattern 格式,例如yyyy-MM-dd
     * @return 日期对象
     */
    public static DateTime parse(String text, String pattern) {
        return new DateTime(text, pattern);
    }

    /**
     * 通过给定的日期格式解析日期时间字符串
     * 传入的日期格式会逐个尝试，直到解析成功，返回{@link Calendar}对象
     *
     * @param text   日期时间字符串，非空
     * @param format 需要尝试的日期时间格式数组，非空, 见SimpleDateFormat
     * @return 解析后的 {@link Calendar}
     */
    public static Calendar parse(String text, String... format) {
        return parseByPatterns(text, null, format);
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
    public static Calendar parse(String text, Locale locale, String... pattern) {
        return parseByPatterns(text, locale, true, pattern);
    }

    /**
     * 构建DateTime对象
     *
     * @param text    Date字符串
     * @param parser  格式化器,{@link DateParser}
     * @param lenient 是否宽容模式
     * @return DateTime对象
     */
    public static DateTime parse(CharSequence text, DateParser parser, boolean lenient) {
        return new DateTime(text, parser, lenient);
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
    public static Calendar parse(String text, Locale locale, boolean lenient, String... pattern) {
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
        throw new InstrumentException("Unable to parse the date: {}", text);
    }

    /**
     * 解析日期字符串，忽略时分秒，支持的格式包括：
     * <pre>
     * yyyy-MM-dd
     * yyyy/MM/dd
     * yyyy.MM.dd
     * yyyy年MM月dd日
     * </pre>
     *
     * @param text 标准形式的日期字符串
     * @return 日期对象
     */
    public static DateTime parseDate(String text) {
        return parse(normalize(text), Fields.NORM_DATE_FORMAT);
    }

    /**
     * 解析时间,格式HH:mm:ss,默认为1970-01-01
     *
     * @param text 标准形式的日期字符串
     * @return 日期对象
     */
    public static DateTime parseTime(String text) {
        return parse(normalize(text), Fields.NORM_TIME_FORMAT);
    }

    /**
     * 解析日期时间字符串，格式支持：
     *
     * <pre>
     * yyyy-MM-dd HH:mm:ss
     * yyyy/MM/dd HH:mm:ss
     * yyyy.MM.dd HH:mm:ss
     * yyyy年MM月dd日 HH:mm:ss
     * </pre>
     *
     * @param text 标准形式的时间字符串
     * @return 日期对象
     */
    public static DateTime parseDateTime(String text) {
        return parse(normalize(text), Fields.NORM_DATETIME_FORMAT);
    }

    /**
     * 解析时间,格式HH:mm:ss,日期默认为今天
     *
     * @param text 标准形式的日期字符串
     * @return 日期对象
     */
    public static DateTime parseTimeToday(String text) {
        text = StringKit.format("{} {}", formatDate(new DateTime()), text);
        if (1 == StringKit.count(text, Symbol.C_COLON)) {
            // 时间格式为 HH:mm
            return parse(text, Fields.NORM_DATETIME_MINUTE_PATTERN);
        } else {
            // 时间格式为 HH:mm:ss
            return parse(text, Fields.NORM_DATETIME_FORMAT);
        }
    }

    /**
     * 格式化成yyMMddHHmm后转换为int型
     *
     * @param date 日期
     * @return int
     */
    public static int toIntSecond(Date date) {
        return Integer.parseInt(format(date, Fields.PURE_DATE_MINUTE_PATTERN));
    }

    /**
     * 使用指定{@link DateParser}解析字符串为{@link Calendar}
     *
     * @param text    日期字符串
     * @param lenient 是否宽容模式
     * @param parser  {@link DateParser}
     * @return 解析后的 {@link Calendar}，解析失败返回{@code null}
     */
    public static Calendar parse(CharSequence text, boolean lenient, DateParser parser) {
        final Calendar calendar = Calendar.getInstance(parser.getTimeZone(), parser.getLocale());
        calendar.clear();
        calendar.setLenient(lenient);

        return parser.parse(StringKit.toString(text), new ParsePosition(0), calendar) ? calendar : null;
    }

    /**
     * 解析CST时间，格式：
     * <ol>
     * <li>EEE MMM dd HH:mm:ss z yyyy(例如：Wed Aug 01 00:00:00 CST 2020)</li>
     * </ol>
     *
     * @param text UTC时间
     * @return 日期对象
     */
    public static DateTime parseCST(CharSequence text) {
        if (null == text) {
            return null;
        }

        return parse((String) text, Fields.JDK_DATETIME_FORMAT);
    }

    /**
     * 解析UTC时间，格式：
     * <ol>
     * <li>yyyy-MM-dd'T'HH:mm:ss'Z'</li>
     * <li>yyyy-MM-dd'T'HH:mm:ss.SSS'Z'</li>
     * <li>yyyy-MM-dd'T'HH:mm:ssZ</li>
     * <li>yyyy-MM-dd'T'HH:mm:ss.SSSZ</li>
     * <li>yyyy-MM-dd'T'HH:mm:ss+0800</li>
     * <li>yyyy-MM-dd'T'HH:mm:ss+08:00</li>
     * </ol>
     *
     * @param text UTC时间
     * @return 日期对象
     */
    public static DateTime parseUTC(String text) {
        if (null == text) {
            return null;
        }
        int length = text.length();
        if (StringKit.contains(text, 'Z')) {
            if (length == Fields.UTC_PATTERN.length() - 4) {
                // 格式类似：2020-09-11T06:34:32Z，-4表示减去4个单引号的长度
                return parse(text, Fields.UTC_FORMAT);
            }

            final int patternLength = Fields.OUTPUT_MSEC_PATTERN.length();
            // 格式类似：2020-09-11T06:34:32.999Z，-4表示减去4个单引号的长度
            // -4 ~ -6范围表示匹配毫秒1~3位的情况
            if (length <= patternLength - 4 && length >= patternLength - 6) {
                return parse(text, Fields.OUTPUT_MSEC_FORMAT);
            }
        } else if (StringKit.contains(text, Symbol.C_PLUS)) {
            // 去除类似2021-08-17T19:45:43 +08:00加号前的空格
            text = text.replace(Symbol.SPACE + Symbol.PLUS, Symbol.PLUS);
            final String zoneOffset = StringKit.subAfter(text, Symbol.C_PLUS, true);
            if (StringKit.isBlank(zoneOffset)) {
                throw new InstrumentException("Invalid format: [{}]", text);
            }
            if (false == StringKit.contains(zoneOffset, Symbol.C_COLON)) {
                // +0800转换为+08:00
                final String pre = StringKit.subBefore(text, Symbol.C_PLUS, true);
                text = pre + Symbol.PLUS + zoneOffset.substring(0, 2) + Symbol.C_COLON + Symbol.ZERO + Symbol.ZERO;
            }

            if (StringKit.contains(text, Symbol.DOT)) {
                // 带毫秒，格式类似：2021-08-17T05:34:31.999+08:00
                return parse(text, Fields.WITH_XXX_OFFSET_FORMAT);
            } else {
                // 格式类似：2021-08-17T05:30:21+08:00
                return parse(text, Fields.WITH_XXX_OFFSET_FORMAT);
            }
        } else {
            if (length == Fields.SIMPLE_PATTERN.length() - 2) {
                // 格式类似：2021-08-13T05:30:21
                return parse(text, Fields.SIMPLE_FORMAT);
            } else if (StringKit.contains(text, Symbol.DOT)) {
                // 可能为：  2021-08-17T06:31:33.99
                return parse(text, Fields.SIMPLE_MS_FORMAT);
            }
        }

        // 没有更多匹配的时间格式
        throw new InstrumentException("No format fit for date String [{}] !", text);
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
        throw new InstrumentException("Unable to parse the date: {}", text);
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
     * @param dateStr 日期时间字符串
     * @return 格式化后的日期字符串
     */
    private static String normalize(CharSequence dateStr) {
        if (StringKit.isBlank(dateStr)) {
            return StringKit.toString(dateStr);
        }

        // 日期时间分开处理
        final List<String> dateAndTime = StringKit.splitTrim(dateStr, Symbol.C_SPACE);
        final int size = dateAndTime.size();
        if (size < 1 || size > 2) {
            // 非可被标准处理的格式
            return StringKit.toString(dateStr);
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
