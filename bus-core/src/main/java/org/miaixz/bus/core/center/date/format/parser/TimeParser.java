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
import org.miaixz.bus.core.center.regex.Pattern;
import org.miaixz.bus.core.lang.Fields;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.toolkit.DateKit;
import org.miaixz.bus.core.toolkit.PatternKit;
import org.miaixz.bus.core.toolkit.StringKit;

/**
 * 时间日期字符串，日期默认为当天，支持格式类似于；
 * <pre>
 *   HH:mm:ss
 *   HH:mm
 * </pre>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class TimeParser extends DefaultDatePrinter implements PredicateDateParser {
    /**
     * 单例
     */
    public static final TimeParser INSTANCE = new TimeParser();
    private static final long serialVersionUID = -1L;

    @Override
    public boolean test(final CharSequence dateStr) {
        return PatternKit.isMatch(Pattern.TIME_PATTERN, dateStr);
    }

    @Override
    public DateTime parse(String source) {
        source = StringKit.replaceChars(source, "时分秒", Symbol.COLON);

        source = StringKit.format("{} {}", DateKit.formatToday(), source);
        if (1 == StringKit.count(source, Symbol.C_COLON)) {
            // 时间格式为 HH:mm
            return new DateTime(source, Fields.NORM_DATETIME_MINUTE);
        } else {
            // 时间格式为 HH:mm:ss
            return new DateTime(source, Formatter.NORM_DATETIME_FORMAT);
        }
    }

}
