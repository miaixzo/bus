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
import java.util.Date;
import java.util.List;

import org.miaixz.bus.core.lang.exception.DateException;
import org.miaixz.bus.core.xyz.ListKit;

/**
 * 基于注册的日期解析器，通过遍历列表，找到合适的解析器，然后解析为日期 默认的，可以调用{@link #INSTANCE}使用全局的解析器，亦或者通过构造自定义独立的注册解析器
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class RegisterDateParser implements DateParser, Serializable {

    @Serial
    private static final long serialVersionUID = 2852292112391L;

    /**
     * 单例
     */
    public static final RegisterDateParser INSTANCE = new RegisterDateParser();

    private final List<PredicateDateParser> list;

    /**
     * 构造
     */
    public RegisterDateParser() {
        list = ListKit.of(
                // HH:mm:ss 或者 HH:mm 时间格式匹配单独解析
                TimeParser.INSTANCE,
                // 默认的正则解析器
                NormalDateParser.INSTANCE);
    }

    @Override
    public Date parse(final CharSequence source) throws DateException {
        return list.stream().filter(predicateDateParser -> predicateDateParser.test(source)).findFirst()
                .map(predicateDateParser -> predicateDateParser.parse(source)).orElse(null);
    }

    /**
     * 注册自定义的{@link PredicateDateParser} 通过此方法，用户可以自定义日期字符串的匹配和解析，通过循环匹配，找到合适的解析器，解析之。
     *
     * @param parser {@link PredicateDateParser}
     * @return this
     */
    public RegisterDateParser register(final PredicateDateParser parser) {
        // 用户定义的规则优先
        this.list.add(0, parser);
        return this;
    }

}
