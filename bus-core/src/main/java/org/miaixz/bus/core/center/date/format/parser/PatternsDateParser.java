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
package org.miaixz.bus.core.center.date.format.parser;

import java.io.Serial;
import java.io.Serializable;
import java.util.Locale;

import org.miaixz.bus.core.center.date.Calendar;
import org.miaixz.bus.core.center.date.DateTime;
import org.miaixz.bus.core.lang.exception.DateException;

/**
 * 通过给定的日期格式解析日期时间字符串，逐个尝试格式直到解析成功，返回 {@link DateTime} 对象，若失败则抛出 {@link DateException}。
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class PatternsDateParser implements DateParser, Serializable {

    @Serial
    private static final long serialVersionUID = 2852256632611L;

    /**
     * 日期格式模式数组
     */
    private String[] patterns;

    /**
     * 地域设置
     */
    private Locale locale;

    /**
     * 构造，初始化日期格式模式。
     *
     * @param args 多个日期格式模式
     */
    public PatternsDateParser(final String... args) {
        this.patterns = args;
    }

    /**
     * 创建 PatternsDateParser 实例。
     *
     * @param args 多个日期格式模式
     * @return PatternsDateParser 实例
     */
    public static PatternsDateParser of(final String... args) {
        return new PatternsDateParser(args);
    }

    /**
     * 设置日期格式模式数组。
     *
     * @param patterns 日期格式模式列表
     * @return 当前实例
     */
    public PatternsDateParser setPatterns(final String... patterns) {
        this.patterns = patterns;
        return this;
    }

    /**
     * 获取地域设置。
     *
     * @return 地域设置
     */
    public Locale getLocale() {
        return locale;
    }

    /**
     * 设置地域。
     *
     * @param locale 地域设置
     * @return 当前实例
     */
    public PatternsDateParser setLocale(final Locale locale) {
        this.locale = locale;
        return this;
    }

    /**
     * 解析日期字符串。
     *
     * @param source 日期字符串
     * @return 解析后的 DateTime 对象
     * @throws DateException 如果解析失败
     */
    @Override
    public DateTime parse(final CharSequence source) {
        return new DateTime(Calendar.parseByPatterns(source, this.locale, this.patterns));
    }

}
