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

import org.miaixz.bus.core.center.date.format.CustomFormat;
import org.miaixz.bus.core.lang.Fields;
import org.miaixz.bus.core.toolkit.StringKit;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 日期解析
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Resolver extends Converter {

    /**
     * 解析日期时间字符串为{@link LocalDateTime}
     *
     * @param text   日期时间字符串
     * @param format 日期格式，类似于yyyy-MM-dd HH:mm:ss,SSS
     * @return {@link LocalDateTime}
     */
    public static LocalDateTime parseTime(CharSequence text, final String format) {
        if (StringKit.isBlank(text)) {
            return null;
        }

        if (CustomFormat.isCustomFormat(format)) {
            return of(CustomFormat.parse(text, format));
        }

        DateTimeFormatter formatter = null;
        if (StringKit.isNotBlank(format)) {
            // 修复yyyyMMddHHmmssSSS格式不能解析的问题
            if (StringKit.startWithIgnoreEquals(format, Fields.PURE_DATETIME) && format.endsWith("S")) {
                // 需要填充的0的个数
                final int paddingWidth = 3 - (format.length() - Fields.PURE_DATETIME.length());
                if (paddingWidth > 0) {
                    // 将yyyyMMddHHmmssS、yyyyMMddHHmmssSS的日期统一替换为yyyyMMddHHmmssSSS格式，用0补
                    text += StringKit.repeat('0', paddingWidth);
                }
                formatter = Formatter.PURE_DATETIME_MS_FORMATTER;
            } else {
                formatter = DateTimeFormatter.ofPattern(format);
            }
        }

        return parseTime(text, formatter);
    }

    /**
     * 解析日期时间字符串为{@link LocalDateTime}，格式支持日期时间、日期、时间
     * 如果formatter为{@code null}，则使用{@link DateTimeFormatter#ISO_LOCAL_DATE_TIME}
     *
     * @param text      日期时间字符串
     * @param formatter 日期格式化器，预定义的格式见：{@link DateTimeFormatter}
     * @return {@link LocalDateTime}
     */
    public static LocalDateTime parseTime(final CharSequence text, final DateTimeFormatter formatter) {
        if (StringKit.isBlank(text)) {
            return null;
        }
        if (null == formatter) {
            return LocalDateTime.parse(text);
        }

        return of(formatter.parse(text));
    }

    /**
     * 解析日期字符串为{@link LocalDate}
     *
     * @param text   日期字符串
     * @param format 日期格式，类似于yyyy-MM-dd
     * @return {@link LocalDateTime}
     */
    public static LocalDate parseDate(final CharSequence text, final String format) {
        if (StringKit.isBlank(text)) {
            return null;
        }
        return parseDate(text, DateTimeFormatter.ofPattern(format));
    }

    /**
     * 解析日期时间字符串为{@link LocalDate}，格式支持日期
     *
     * @param text      日期时间字符串
     * @param formatter 日期格式化器，预定义的格式见：{@link DateTimeFormatter}
     * @return {@link LocalDate}
     */
    public static LocalDate parseDate(final CharSequence text, final DateTimeFormatter formatter) {
        if (StringKit.isBlank(text)) {
            return null;
        }
        if (null == formatter) {
            return LocalDate.parse(text);
        }

        return ofDate(formatter.parse(text));
    }

    /**
     * 解析日期时间字符串为{@link LocalDate}，仅支持yyyy-MM-dd'T'HH:mm:ss格式，例如：2007-12-03T10:15:30
     *
     * @param text 日期时间字符串
     * @return {@link LocalDate}
     */
    public static LocalDate parseDateByISO(final CharSequence text) {
        return parseDate(text, (DateTimeFormatter) null);
    }

    /**
     * 解析日期时间字符串为{@link LocalDateTime}，支持：
     * <ul>
     *     <li>{@link DateTimeFormatter#ISO_LOCAL_DATE_TIME} yyyy-MM-dd'T'HH:mm:ss格式，例如：2007-12-03T10:15:30</li>
     *     <li>yyyy-MM-dd HH:mm:ss</li>
     * </ul>
     *
     * @param text 日期时间字符串
     * @return {@link LocalDateTime}
     */
    public static LocalDateTime parseTimeByISO(final CharSequence text) {
        if (StringKit.contains(text, 'T')) {
            return parseTime(text, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } else {
            return parseTime(text, Formatter.NORM_DATETIME_FORMATTER);
        }
    }

}
