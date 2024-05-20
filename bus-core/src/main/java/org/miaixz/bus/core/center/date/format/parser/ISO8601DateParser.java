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
package org.miaixz.bus.core.center.date.format.parser;

import org.miaixz.bus.core.center.date.DateTime;
import org.miaixz.bus.core.center.date.Formatter;
import org.miaixz.bus.core.center.date.printer.DefaultDatePrinter;
import org.miaixz.bus.core.lang.Fields;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.DateException;
import org.miaixz.bus.core.toolkit.PatternKit;
import org.miaixz.bus.core.toolkit.StringKit;

/**
 * ISO8601日期字符串（JDK的Date对象toString默认格式）解析，支持格式；
 * <ol>
 *   <li>yyyy-MM-dd'T'HH:mm:ss'Z'</li>
 *   <li>yyyy-MM-dd'T'HH:mm:ss.SSS'Z'</li>
 *   <li>yyyy-MM-dd'T'HH:mm:ssZ</li>
 *   <li>yyyy-MM-dd'T'HH:mm:ss.SSSZ</li>
 *   <li>yyyy-MM-dd'T'HH:mm:ss+0800</li>
 *   <li>yyyy-MM-dd'T'HH:mm:ss+08:00</li>
 * </ol>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ISO8601DateParser extends DefaultDatePrinter implements PredicateDateParser {

    private static final long serialVersionUID = -1L;

    /**
     * 单例对象
     */
    public static ISO8601DateParser INSTANCE = new ISO8601DateParser();

    /**
     * 如果日期中的毫秒部分超出3位，会导致秒数增加，因此只保留前三位
     *
     * @param dateStr 日期字符串
     * @param before  毫秒部分的前一个字符
     * @param after   毫秒部分的后一个字符
     * @return 规范之后的毫秒部分
     */
    private static String normalizeMillSeconds(final String dateStr, final CharSequence before, final CharSequence after) {
        if (StringKit.isBlank(after)) {
            final String millOrNaco = StringKit.subPre(StringKit.subAfter(dateStr, before, true), 3);
            return StringKit.subBefore(dateStr, before, true) + before + millOrNaco;
        }
        final String millOrNaco = StringKit.subPre(StringKit.subBetween(dateStr, before, after), 3);
        return StringKit.subBefore(dateStr, before, true)
                + before
                + millOrNaco + after + StringKit.subAfter(dateStr, after, true);
    }

    @Override
    public boolean test(final CharSequence dateStr) {
        return StringKit.contains(dateStr, 'T');
    }

    @Override
    public DateTime parse(String source) throws DateException {
        final int length = source.length();
        if (StringKit.contains(source, 'Z')) {
            if (length == Fields.UTC.length() - 4) {
                // 格式类似：2018-09-13T05:34:31Z，-4表示减去4个单引号的长度
                return new DateTime(source, Formatter.UTC_FORMAT);
            }

            final int patternLength = Fields.UTC_MS.length();
            // 格式类似：2018-09-13T05:34:31.999Z，-4表示减去4个单引号的长度
            // 2018-09-13T05:34:31.1Z - 2018-09-13T05:34:31.000000Z
            if (length <= patternLength && length >= patternLength - 6) {
                // 毫秒部分1-7位支持
                return new DateTime(source, Formatter.UTC_MS_FORMAT);
            }
        } else if (StringKit.contains(source, '+')) {
            // 去除类似2019-06-01T19:45:43 +08:00加号前的空格
            source = source.replace(" +", "+");
            final String zoneOffset = StringKit.subAfter(source, '+', true);
            if (StringKit.isBlank(zoneOffset)) {
                throw new DateException("Invalid format: [{}]", source);
            }
            if (!StringKit.contains(zoneOffset, ':')) {
                // +0800转换为+08:00
                final String pre = StringKit.subBefore(source, '+', true);
                source = pre + "+" + zoneOffset.substring(0, 2) + ":" + "00";
            }

            if (StringKit.contains(source, Symbol.C_DOT)) {
                // 带毫秒，格式类似：2018-09-13T05:34:31.999+08:00
                source = normalizeMillSeconds(source, ".", "+");
                return new DateTime(source, Formatter.ISO8601_MS_WITH_XXX_OFFSET_FORMAT);
            } else {
                // 格式类似：2018-09-13T05:34:31+08:00
                return new DateTime(source, Formatter.ISO8601_WITH_XXX_OFFSET_FORMAT);
            }
        } else if (PatternKit.contains("-\\d{2}:?00", source)) {
            // 类似 2022-09-14T23:59:00-08:00 或者 2022-09-14T23:59:00-0800

            // 去除类似2019-06-01T19:45:43 -08:00加号前的空格
            source = source.replace(" -", "-");
            if (':' != source.charAt(source.length() - 3)) {
                source = source.substring(0, source.length() - 2) + ":00";
            }

            if (StringKit.contains(source, Symbol.C_DOT)) {
                // 带毫秒，格式类似：2018-09-13T05:34:31.999-08:00
                source = normalizeMillSeconds(source, ".", "-");
                return new DateTime(source, Formatter.ISO8601_MS_WITH_XXX_OFFSET_FORMAT);
            } else {
                // 格式类似：2018-09-13T05:34:31-08:00
                return new DateTime(source, Formatter.ISO8601_WITH_XXX_OFFSET_FORMAT);
            }
        } else {
            if (length == Fields.ISO8601.length() - 2) {
                // 格式类似：2018-09-13T05:34:31
                return new DateTime(source, Formatter.ISO8601_FORMAT);
            } else if (length == Fields.ISO8601.length() - 5) {
                // 格式类似：2018-09-13T05:34
                return new DateTime(source + ":00", Formatter.ISO8601_FORMAT);
            } else if (StringKit.contains(source, Symbol.C_DOT)) {
                // 可能为：  2021-03-17T06:31:33.99
                source = normalizeMillSeconds(source, ".", null);
                return new DateTime(source, Formatter.ISO8601_MS_FORMAT);
            }
        }
        // 没有更多匹配的时间格式
        throw new DateException("No UTC format fit for date String [{}] !", source);
    }

}
