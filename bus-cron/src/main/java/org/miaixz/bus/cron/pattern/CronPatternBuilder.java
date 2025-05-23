/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.cron.pattern;

import java.io.Serial;

import org.miaixz.bus.core.Builder;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.text.StringJoiner;
import org.miaixz.bus.core.xyz.ArrayKit;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * 定时任务表达式构建器
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CronPatternBuilder implements Builder<String> {

    @Serial
    private static final long serialVersionUID = 2852332096080L;

    final String[] parts = new String[7];

    /**
     * 创建构建器
     *
     * @return CronPatternBuilder
     */
    public static CronPatternBuilder of() {
        return new CronPatternBuilder();
    }

    /**
     * 设置值
     *
     * @param part   部分，如秒、分、时等
     * @param values 时间值列表
     * @return this
     */
    public CronPatternBuilder setValues(final Part part, final int... values) {
        for (final int value : values) {
            part.checkValue(value);
        }
        return set(part, ArrayKit.join(values, Symbol.COMMA));
    }

    /**
     * 设置区间
     *
     * @param part  部分，如秒、分、时等
     * @param begin 起始值
     * @param end   结束值
     * @return this
     */
    public CronPatternBuilder setRange(final Part part, final int begin, final int end) {
        Assert.notNull(part);
        part.checkValue(begin);
        part.checkValue(end);
        return set(part, StringKit.format("{}-{}", begin, end));
    }

    /**
     * 设置对应部分的定时任务值
     *
     * @param part  部分，如秒、分、时等
     * @param value 表达式值，如"*"、"1,2"、"5-12"等
     * @return this
     */
    public CronPatternBuilder set(final Part part, final String value) {
        parts[part.ordinal()] = value;
        return this;
    }

    @Override
    public String build() {
        for (int i = Part.MINUTE.ordinal(); i < Part.YEAR.ordinal(); i++) {
            // 从分到周，用户未设置使用默认值
            // 秒和年如果不设置，忽略之
            if (StringKit.isBlank(parts[i])) {
                parts[i] = Symbol.STAR;
            }
        }

        return StringJoiner.of(Symbol.SPACE).setNullMode(StringJoiner.NullMode.IGNORE).append(this.parts).toString();
    }

}
