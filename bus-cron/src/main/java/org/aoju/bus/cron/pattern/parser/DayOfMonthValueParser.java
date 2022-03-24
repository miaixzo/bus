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
import org.aoju.bus.cron.pattern.matcher.DayOfMonthValueMatcher;
import org.aoju.bus.cron.pattern.matcher.ValueMatcher;

import java.util.List;

/**
 * 每月的几号值处理
 * 每月最多31天，32和“L”都表示最后一天
 *
 * @author Kimi Liu
 * @version 6.5.0
 * @since Java 17+
 */
public class DayOfMonthValueParser extends AbstractValueParser {

    /**
     * 构造
     */
    public DayOfMonthValueParser() {
        super(1, 31);
    }

    @Override
    public int parse(String value) throws CrontabException {
        if ("L".equalsIgnoreCase(value) || "32".equals(value)) {// 每月最后一天
            return 32;
        } else {
            return super.parse(value);
        }
    }

    @Override
    protected ValueMatcher buildValueMatcher(List<Integer> values) {
        //考虑每月的天数不同，且存在闰年情况，日匹配单独使用
        return new DayOfMonthValueMatcher(values);
    }

}
