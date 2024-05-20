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
import org.miaixz.bus.core.center.date.format.FormatBuilder;
import org.miaixz.bus.core.center.date.printer.DefaultDatePrinter;
import org.miaixz.bus.core.lang.Fields;
import org.miaixz.bus.core.xyz.StringKit;

import java.util.Locale;

/**
 * RFC2822日期字符串（JDK的Date对象toString默认格式）及HTTP消息日期解析，支持格式类似于；
 * <pre>
 *   Tue Jun 4 16:25:15 +0800 2019
 *   Thu May 16 17:57:18 GMT+08:00 2019
 *   Wed Aug 01 00:00:00 CST 2012
 *   Thu, 28 Mar 2024 14:33:49 GMT
 * </pre>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class RFC2822DateParser extends DefaultDatePrinter implements PredicateDateParser {

    private static final long serialVersionUID = -1L;

    private static final String KEYWORDS_LOCALE_CHINA = "星期";

    /**
     * java.util.Date EEE MMM zzz 缩写数组
     */
    private static final String[] wtb = { //
            "sun", "mon", "tue", "wed", "thu", "fri", "sat", // 星期
            "jan", "feb", "mar", "apr", "may", "jun", "jul", "aug", "sep", "oct", "nov", "dec", // 月份
            "gmt", "ut", "utc", "est", "edt", "cst", "cdt", "mst", "mdt", "pst", "pdt"// 时间标准
    };

    /**
     * 单例对象
     */
    public static RFC2822DateParser INSTANCE = new RFC2822DateParser();

    @Override
    public boolean test(final CharSequence dateStr) {
        return StringKit.containsAnyIgnoreCase(dateStr, wtb);
    }

    @Override
    public DateTime parse(final String source) {
        if (StringKit.contains(source, ',')) {
            if (StringKit.contains(source, KEYWORDS_LOCALE_CHINA)) {
                // 例如：星期四, 28 三月 2024 14:33:49 GMT
                return new DateTime(source, FormatBuilder.getInstance(Fields.HTTP_DATETIME, Locale.CHINA));
            }
            // 例如：Thu, 28 Mar 2024 14:33:49 GMT
            return new DateTime(source, Formatter.HTTP_DATETIME_FORMAT);
        }

        if (StringKit.contains(source, KEYWORDS_LOCALE_CHINA)) {
            // 例如：星期四, 28 三月 2024 14:33:49 GMT
            return new DateTime(source, FormatBuilder.getInstance(Fields.JDK_DATETIME, Locale.CHINA));
        }
        // 例如：Thu 28 Mar 2024 14:33:49 GMT
        return new DateTime(source, Formatter.JDK_DATETIME_FORMAT);
    }

}
