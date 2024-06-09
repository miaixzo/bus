/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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

import org.miaixz.bus.core.center.date.printer.DefaultDatePrinter;
import org.miaixz.bus.core.lang.exception.DateException;
import org.miaixz.bus.core.xyz.ListKit;

import java.util.Date;
import java.util.List;

/**
 * 基于注册的日期解析器，通过遍历列表，找到合适的解析器，然后解析为日期
 * 默认的，可以调用{@link #INSTANCE}使用全局的解析器，亦或者通过构造自定义独立的注册解析器
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class RegisterDateParser extends DefaultDatePrinter implements DateParser {

    /**
     * 单例
     */
    public static final RegisterDateParser INSTANCE = new RegisterDateParser();
    private static final long serialVersionUID = -1L;
    private final List<PredicateDateParser> parserList;

    /**
     * 构造
     */
    public RegisterDateParser() {
        parserList = ListKit.of(
                // 纯数字形式
                PureDateParser.INSTANCE,
                // HH:mm:ss 或者 HH:mm 时间格式匹配单独解析
                TimeParser.INSTANCE,
                // JDK的Date对象toString默认格式，类似于：
                // Tue Jun 4 16:25:15 +0800 2019
                // Thu May 16 17:57:18 GMT+08:00 2019
                // Wed Aug 01 00:00:00 CST 2012
                RFC2822DateParser.INSTANCE,
                // ISO8601标准时间
                // yyyy-MM-dd'T'HH:mm:ss'Z'
                // yyyy-MM-dd'T'HH:mm:ss+0800
                ISO8601DateParser.INSTANCE
        );
    }

    @Override
    public Date parse(final String source) throws DateException {
        return parserList
                .stream()
                .filter(predicateDateParser -> predicateDateParser.test(source))
                .findFirst()
                .map(predicateDateParser -> predicateDateParser.parse(source)).orElse(null);
    }

    /**
     * 注册自定义的{@link PredicateDateParser}
     * 通过此方法，用户可以自定义日期字符串的匹配和解析，通过循环匹配，找到合适的解析器，解析之。
     *
     * @param dateParser {@link PredicateDateParser}
     * @return this
     */
    public RegisterDateParser register(final PredicateDateParser dateParser) {
        this.parserList.add(dateParser);
        return this;
    }

}
