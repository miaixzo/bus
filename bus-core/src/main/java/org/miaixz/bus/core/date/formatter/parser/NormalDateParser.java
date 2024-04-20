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
import org.miaixz.bus.core.toolkit.StringKit;

/**
 * 标准日期字符串解析，支持格式；
 * <pre>
 *     yyyy-MM-dd HH:mm:ss.SSSSSS
 *     yyyy-MM-dd HH:mm:ss.SSS
 *     yyyy-MM-dd HH:mm:ss
 *     yyyy-MM-dd HH:mm
 *     yyyy-MM-dd
 * </pre>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class NormalDateParser extends NormalMotd implements DateParser {

    public static NormalDateParser INSTANCE = new NormalDateParser();

    @Override
    public DateTime parse(String source) {
        final int colonCount = StringKit.count(source, Symbol.C_COLON);
        switch (colonCount) {
            case 0:
                // yyyy-MM-dd
                return new DateTime(source, Fields.NORM_DATE_FORMAT);
            case 1:
                // yyyy-MM-dd HH:mm
                return new DateTime(source, Fields.NORM_DATETIME_MINUTE_FORMAT);
            case 2:
                final int indexOfDot = StringKit.indexOf(source, Symbol.C_DOT);
                if (indexOfDot > 0) {
                    final int length1 = source.length();
                    // yyyy-MM-dd HH:mm:ss.SSS 或者 yyyy-MM-dd HH:mm:ss.SSSSSS
                    if (length1 - indexOfDot > 4) {
                        // 类似yyyy-MM-dd HH:mm:ss.SSSSSS，采取截断操作
                        source = StringKit.subPre(source, indexOfDot + 4);
                    }
                    return new DateTime(source, Fields.NORM_DATETIME_MS_FORMAT);
                }
                // yyyy-MM-dd HH:mm:ss
                return new DateTime(source, Fields.NORM_DATETIME_FORMAT);
        }

        throw new InternalException("No format fit for date String [{}] !", source);
    }

}
