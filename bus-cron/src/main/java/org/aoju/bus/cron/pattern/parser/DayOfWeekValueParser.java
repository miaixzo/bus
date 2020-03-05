/*********************************************************************************
 *                                                                               *
 * The MIT License                                                               *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
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
 ********************************************************************************/
package org.aoju.bus.cron.pattern.parser;

import org.aoju.bus.core.lang.exception.InstrumentException;

/**
 * 星期值处理
 * 1表示星期一,2表示星期二,依次类推,0和7都可以表示星期日
 *
 * @author Kimi Liu
 * @version 5.6.6
 * @since JDK 1.8+
 */
public class DayOfWeekValueParser extends SimpleValueParser {

    /**
     * 周的别名
     */
    private static final String[] ALIASES = {"sun", "mon", "tue", "wed", "thu", "fri", "sat"};

    public DayOfWeekValueParser() {
        super(0, 7);
    }

    /**
     * 对于星期提供转换
     * 1表示星期一,2表示星期二,依次类推,0和7都可以表示星期日
     */
    @Override
    public int parse(String value) throws InstrumentException {
        try {
            return super.parse(value) % 7;
        } catch (Exception e) {
            return parseAlias(value);
        }
    }

    /**
     * 解析别名
     *
     * @param value 别名值
     * @return 月份int值
     * @throws InstrumentException 异常
     */
    private int parseAlias(String value) throws InstrumentException {
        if ("L".equalsIgnoreCase(value)) {
            //最后一天为星期六
            return ALIASES.length - 1;
        }

        for (int i = 0; i < ALIASES.length; i++) {
            if (ALIASES[i].equalsIgnoreCase(value)) {
                return i;
            }
        }
        throw new InstrumentException("Invalid month alias: {}", value);
    }

}
