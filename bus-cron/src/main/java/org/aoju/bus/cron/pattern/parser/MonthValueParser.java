/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2022 aoju.org and other contributors.                      *
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
package org.aoju.bus.cron.pattern.parser;

import org.aoju.bus.core.lang.exception.CrontabException;

/**
 * 月份值处理
 * 限定于1-12，1表示一月，支持别名（忽略大小写），如一月是{@code jan}
 *
 * @author Kimi Liu
 * @version 6.5.0
 * @since Java 17+
 */
public class MonthValueParser extends AbstractValueParser {

    /**
     * Months aliases.
     */
    private static final String[] ALIASES = {"jan", "feb", "mar", "apr", "may", "jun", "jul", "aug", "sep", "oct", "nov", "dec"};

    public MonthValueParser() {
        super(1, 12);
    }

    @Override
    public int parse(String value) throws CrontabException {
        try {
            return super.parse(value);
        } catch (Exception e) {
            return parseAlias(value);
        }
    }

    /**
     * 解析别名
     *
     * @param value 别名值
     * @return 月份int值
     * @throws CrontabException 无效月别名抛出此异常
     */
    private int parseAlias(String value) throws CrontabException {
        for (int i = 0; i < ALIASES.length; i++) {
            if (ALIASES[i].equalsIgnoreCase(value)) {
                return i + 1;
            }
        }
        throw new CrontabException("Invalid month alias: {}", value);
    }

}
