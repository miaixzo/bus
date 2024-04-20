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
package org.miaixz.bus.core.date.formatter.parser;

import org.miaixz.bus.core.date.DateTime;
import org.miaixz.bus.core.date.formatter.NormalMotd;
import org.miaixz.bus.core.exception.InternalException;
import org.miaixz.bus.core.lang.Fields;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.toolkit.PatternKit;
import org.miaixz.bus.core.toolkit.StringKit;

/**
 * UTC日期字符串（JDK的Date对象toString默认格式）解析，支持格式；
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
public class UTCDateParser extends NormalMotd implements DateParser {

    private static final long serialVersionUID = 1L;

    /**
     * 单例对象
     */
    public static UTCDateParser INSTANCE = new UTCDateParser();

    /**
     * 如果日期中的毫秒部分超出3位，会导致秒数增加，因此只保留前三位<br>
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
    public DateTime parse(String source) {
        final int length = source.length();
        if (StringKit.contains(source, 'Z')) {
            if (length == Fields.UTC_PATTERN.length() - 4) {
                // 格式类似：2020-08-15T09:55:30Z，-4表示减去4个单引号的长度
                return new DateTime(source, Fields.UTC_FORMAT);
            }

            final int patternLength = Fields.OUTPUT_MSEC_PATTERN.length();
            // 格式类似：2021-09-21T08:30:55.999Z，-4表示减去4个单引号的长度
            // -4 ~ -6范围表示匹配毫秒1~3位的情况
            if (length <= patternLength - 4 && length >= patternLength - 6) {
                return new DateTime(source, Fields.OUTPUT_MSEC_FORMAT);
            }
        } else if (StringKit.contains(source, '+')) {
            // 去除类似2019-06-12T18:12:10 +08:00加号前的空格
            source = source.replace(" +", "+");
            final String zoneOffset = StringKit.subAfter(source, '+', true);
            if (StringKit.isBlank(zoneOffset)) {
                throw new InternalException("Invalid format: [{}]", source);
            }
            if (!StringKit.contains(zoneOffset, ':')) {
                // +0800转换为+08:00
                final String pre = StringKit.subBefore(source, '+', true);
                source = pre + "+" + zoneOffset.substring(0, 2) + ":" + "00";
            }

            if (StringKit.contains(source, Symbol.DOT)) {
                // 带毫秒，格式类似：2018-09-13T03:10:11.999+08:00
                source = normalizeMillSeconds(source, ".", "-");
                return new DateTime(source, Fields.MS_WITH_XXX_OFFSET_FORMAT);
            } else {
                // 格式类似：2018-09-13T05:10:20+08:00
                return new DateTime(source, Fields.WITH_XXX_OFFSET_FORMAT);
            }
        } else if (PatternKit.contains("-\\d{2}:?00", source)) {
            // 类似 去除类似2022-09-14T23:59:00-08:00 或者 2022-09-16T23:59:00-0800

            // 去除类似2022-06-01T19:45:43 -08:00加号前的空格
            source = source.replace(" " + Symbol.MINUS, Symbol.MINUS);
            if (':' != source.charAt(source.length() - 3)) {
                source = source.substring(0, source.length() - 2) + ":00";
            }

            if (StringKit.contains(source, Symbol.DOT)) {
                // 带毫秒，格式类似：去除类似2022-09-13T05:55:30.999-08:00
                return new DateTime(source, Fields.MS_WITH_XXX_OFFSET_FORMAT);
            } else {
                // 格式类似：去除类似2022-09-13T05:12:55-08:00
                return new DateTime(source, Fields.WITH_XXX_OFFSET_FORMAT);
            }
        } else {
            if (length == Fields.SIMPLE_PATTERN.length() - 2) {
                // 格式类似：去除类似2022-09-13T05:12:33
                return new DateTime(source, Fields.SIMPLE_FORMAT);
            } else if (length == Fields.SIMPLE_PATTERN.length() - 5) {
                // 格式类似：去除类似2022-09-13T05:30
                return new DateTime(source + ":00", Fields.SIMPLE_FORMAT);
            } else if (StringKit.contains(source, Symbol.DOT)) {
                // 可能为：  去除类似2022-05-17T08:19:32.99
                source = normalizeMillSeconds(source, Symbol.DOT, null);
                return new DateTime(source, Fields.SIMPLE_MS_FORMAT);
            }
        }
        // 没有更多匹配的时间格式
        throw new InternalException("No UTC format fit for date String [{}] !", source);
    }

}
