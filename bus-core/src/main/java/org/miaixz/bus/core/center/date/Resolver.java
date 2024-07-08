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

import org.miaixz.bus.core.center.date.format.CustomFormat;
import org.miaixz.bus.core.center.date.format.FormatBuilder;
import org.miaixz.bus.core.center.date.format.parser.PositionDateParser;
import org.miaixz.bus.core.center.date.format.parser.RegisterDateParser;
import org.miaixz.bus.core.lang.Fields;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.DateException;
import org.miaixz.bus.core.text.CharsBacker;
import org.miaixz.bus.core.xyz.StringKit;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * 日期解析
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Resolver extends Converter {

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
     * @param date 日期字符串
     * @return 日期
     */
    public static DateTime parse(final CharSequence date) {
        return (DateTime) RegisterDateParser.INSTANCE.parse(date);
    }

    /**
     * 构建DateTime对象
     *
     * @param date    Date字符串
     * @param format 格式化器 {@link SimpleDateFormat}
     * @return DateTime对象
     */
    public static DateTime parse(final CharSequence date, final DateFormat format) {
        return new DateTime(date, format);
    }

    /**
     * 构建DateTime对象
     *
     * @param date Date字符串
     * @param parser  格式化器,{@link FormatBuilder}
     * @return DateTime对象
     */
    public static DateTime parse(final CharSequence date, final PositionDateParser parser) {
        return new DateTime(date, parser);
    }

    /**
     * 构建DateTime对象
     *
     * @param date Date字符串
     * @param parser  格式化器,{@link FormatBuilder}
     * @param lenient 是否宽容模式
     * @return DateTime对象
     */
    public static DateTime parse(final CharSequence date, final PositionDateParser parser, final boolean lenient) {
        return new DateTime(date, parser, lenient);
    }

    /**
     * 构建DateTime对象
     *
     * @param date   Date字符串
     * @param formatter 格式化器,{@link DateTimeFormatter}
     * @return DateTime对象
     */
    public static DateTime parse(final CharSequence date, final DateTimeFormatter formatter) {
        return new DateTime(date, formatter);
    }

    /**
     * 将特定格式的日期转换为Date对象
     *
     * @param date 特定格式的日期
     * @param format  格式，例如yyyy-MM-dd
     * @return 日期对象
     */
    public static DateTime parse(final CharSequence date, final String format) {
        return new DateTime(date, format);
    }

    /**
     * 将特定格式的日期转换为Date对象
     *
     * @param date 特定格式的日期
     * @param format  格式，例如yyyy-MM-dd
     * @param locale  区域信息
     * @return 日期对象
     */
    public static DateTime parse(final CharSequence date, final String format, final Locale locale) {
        if (CustomFormat.isCustomFormat(format)) {
            // 自定义格式化器忽略Locale
            return new DateTime(CustomFormat.parse(date, format));
        }
        return new DateTime(date, newSimpleFormat(format, locale, null));
    }

    /**
     * 通过给定的日期格式解析日期时间字符串
     * 传入的日期格式会逐个尝试，直到解析成功，返回{@link DateTime}对象，否则抛出{@link DateException}异常
     *
     * @param date          日期时间字符串，非空
     * @param parsePatterns 需要尝试的日期时间格式数组，非空, 见SimpleDateFormat
     * @return 解析后的Date
     * @throws IllegalArgumentException if the date string or pattern array is null
     * @throws DateException            if none of the date patterns were suitable
     */
    public static DateTime parse(final String date, final String... parsePatterns) throws DateException {
        return date(Calendar.parseByPatterns(date, parsePatterns));
    }

    /**
     * 解析日期时间字符串为{@link LocalDateTime}
     *
     * @param date   日期时间字符串
     * @param format 日期格式，类似于yyyy-MM-dd HH:mm:ss,SSS
     * @return {@link LocalDateTime}
     */
    public static LocalDateTime parseTime(CharSequence date, final String format) {
        if (StringKit.isBlank(date)) {
            return null;
        }

        if (CustomFormat.isCustomFormat(format)) {
            return of(CustomFormat.parse(date, format).toInstant());
        }

        DateTimeFormatter formatter = null;
        if (StringKit.isNotBlank(format)) {
            // 修复yyyyMMddHHmmssSSS格式不能解析的问题
            if (StringKit.startWithIgnoreEquals(format, Fields.PURE_DATETIME) && format.endsWith("S")) {
                // 需要填充的0的个数
                final int paddingWidth = 3 - (format.length() - Fields.PURE_DATETIME.length());
                if (paddingWidth > 0) {
                    // 将yyyyMMddHHmmssS、yyyyMMddHHmmssSS的日期统一替换为yyyyMMddHHmmssSSS格式，用0补
                    date += StringKit.repeat('0', paddingWidth);
                }
                formatter = Formatter.PURE_DATETIME_MS_FORMATTER;
            } else {
                formatter = DateTimeFormatter.ofPattern(format);
            }
        }

        return parseTime(date, formatter);
    }

    /**
     * 解析日期时间字符串为{@link LocalDateTime}，格式支持日期时间、日期、时间
     * 如果formatter为{@code null}，则使用{@link DateTimeFormatter#ISO_LOCAL_DATE_TIME}
     *
     * @param date      日期时间字符串
     * @param formatter 日期格式化器，预定义的格式见：{@link DateTimeFormatter}
     * @return {@link LocalDateTime}
     */
    public static LocalDateTime parseTime(final CharSequence date, final DateTimeFormatter formatter) {
        if (StringKit.isBlank(date)) {
            return null;
        }
        if (null == formatter) {
            return LocalDateTime.parse(date);
        }

        return of(formatter.parse(date));
    }

    /**
     * 解析日期字符串为{@link LocalDate}
     *
     * @param date   日期字符串
     * @param format 日期格式，类似于yyyy-MM-dd
     * @return {@link LocalDateTime}
     */
    public static LocalDate parseDate(final CharSequence date, final String format) {
        if (StringKit.isBlank(date)) {
            return null;
        }
        return parseDate(date, DateTimeFormatter.ofPattern(format));
    }

    /**
     * 解析日期时间字符串为{@link LocalDate}，格式支持日期
     *
     * @param date      日期时间字符串
     * @param formatter 日期格式化器，预定义的格式见：{@link DateTimeFormatter}
     * @return {@link LocalDate}
     */
    public static LocalDate parseDate(final CharSequence date, final DateTimeFormatter formatter) {
        if (StringKit.isBlank(date)) {
            return null;
        }
        if (null == formatter) {
            return LocalDate.parse(date);
        }

        return ofDate(formatter.parse(date));
    }

    /**
     * 解析日期时间字符串为{@link LocalDate}，仅支持yyyy-MM-dd'T'HH:mm:ss格式，例如：2007-12-03T10:15:30
     *
     * @param date 日期时间字符串
     * @return {@link LocalDate}
     */
    public static LocalDate parseDateByISO(final CharSequence date) {
        return parseDate(date, (DateTimeFormatter) null);
    }

    /**
     * 解析日期时间字符串为{@link LocalDateTime}，支持：
     * <ul>
     *     <li>{@link DateTimeFormatter#ISO_LOCAL_DATE_TIME} yyyy-MM-dd'T'HH:mm:ss格式，例如：2007-12-03T10:15:30</li>
     *     <li>yyyy-MM-dd HH:mm:ss</li>
     * </ul>
     *
     * @param date 日期时间字符串
     * @return {@link LocalDateTime}
     */
    public static LocalDateTime parseTimeByISO(final CharSequence date) {
        if (StringKit.contains(date, 'T')) {
            return parseTime(date, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } else {
            return parseTime(date, Formatter.NORM_DATETIME_FORMATTER);
        }
    }

    /**
     * 标准化日期，默认处理以空格区分的日期时间格式，空格前为日期，空格后为时间：
     * 将以下字符替换为"-"
     *
     * <pre>
     * "."
     * "/"
     * "年"
     * "月"
     * </pre>
     * <p>
     * 将以下字符去除
     *
     * <pre>
     * "日"
     * </pre>
     * <p>
     * 将以下字符替换为":"
     *
     * <pre>
     * "时"
     * "分"
     * "秒"
     * </pre>
     * <p>
     * 当末位是":"时去除之（不存在毫秒时）
     *
     * @param date 日期时间字符串
     * @return 格式化后的日期字符串
     */
    public static String normalize(final CharSequence date) {
        if (StringKit.isBlank(date)) {
            return StringKit.toString(date);
        }

        // 日期时间分开处理
        final List<String> dateAndTime = CharsBacker.splitTrim(date, Symbol.SPACE);
        final int size = dateAndTime.size();
        if (size < 1 || size > 2) {
            // 非可被标准处理的格式
            return StringKit.toString(date);
        }

        final StringBuilder builder = StringKit.builder();

        // 日期部分（"\"、"/"、"."、"年"、"月"都替换为"-"）
        String datePart = dateAndTime.get(0).replaceAll("[/.年月]", Symbol.MINUS);
        datePart = StringKit.removeSuffix(datePart, "日");
        builder.append(datePart);

        // 时间部分
        if (size == 2) {
            builder.append(Symbol.C_SPACE);
            String timePart = dateAndTime.get(1).replaceAll("[时分秒]", Symbol.COLON);
            timePart = StringKit.removeSuffix(timePart, Symbol.COLON);
            //将ISO8601中的逗号替换为.
            timePart = timePart.replace(Symbol.C_COMMA, '.');
            builder.append(timePart);
        }

        return builder.toString();
    }

}
