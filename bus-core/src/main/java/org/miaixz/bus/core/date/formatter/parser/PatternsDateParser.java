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
import org.miaixz.bus.core.date.Formatter;
import org.miaixz.bus.core.date.formatter.NormalMotd;

import java.util.Calendar;
import java.util.Locale;

/**
 * 通过给定的日期格式解析日期时间字符串
 * 传入的日期格式会逐个尝试，直到解析成功，返回{@link Calendar}对象
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class PatternsDateParser extends NormalMotd implements DateParser {

    private String[] parsePatterns;
    private Locale locale;

    /**
     * 构造
     *
     * @param parsePatterns 多个日期格式
     */
    public PatternsDateParser(final String... parsePatterns) {
        this.parsePatterns = parsePatterns;
    }

    /**
     * 创建 PatternsDateParser
     *
     * @param parsePatterns 多个日期格式
     * @return PatternsDateParser
     */
    public static PatternsDateParser of(final String... parsePatterns) {
        return new PatternsDateParser(parsePatterns);
    }

    /**
     * 设置多个日期格式
     *
     * @param parsePatterns 日期格式列表
     * @return this
     */
    public PatternsDateParser setParsePatterns(final String... parsePatterns) {
        this.parsePatterns = parsePatterns;
        return this;
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    /**
     * 设置{@link Locale}
     *
     * @param locale {@link Locale}
     * @return this
     */
    public PatternsDateParser setLocale(final Locale locale) {
        this.locale = locale;
        return this;
    }

    @Override
    public DateTime parse(final String source) {
        return new DateTime(Formatter.parseByPatterns(source, this.locale, this.parsePatterns));
    }

}
