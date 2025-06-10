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
 * 基于注册的日期解析器，通过遍历注册的解析器列表，找到适合的解析器并解析为日期。 默认可使用单例 {@link #INSTANCE}，或通过构造创建自定义的解析器实例。
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class RegisterDateParser implements DateParser, Serializable {

    @Serial
    private static final long serialVersionUID = 2852256978098L;

    /**
     * 单例实例
     */
    public static final RegisterDateParser INSTANCE = new RegisterDateParser();

    /**
     * 日期解析器列表
     */
    private final List<PredicateDateParser> list;

    /**
     * 构造，初始化默认解析器列表。
     */
    public RegisterDateParser() {
        list = ListKit.of(
                // HH:mm:ss 或 HH:mm 时间格式解析器
                TimeParser.INSTANCE,
                // 默认正则解析器
                NormalDateParser.INSTANCE);
    }

    /**
     * 解析日期字符串。
     *
     * @param source 日期字符串
     * @return 解析后的日期对象
     * @throws DateException 如果解析失败
     */
    @Override
    public Date parse(final CharSequence source) throws DateException {
        return list.stream().filter(predicateDateParser -> predicateDateParser.test(source)).findFirst()
                .map(predicateDateParser -> predicateDateParser.parse(source)).orElse(null);
    }

    /**
     * 注册自定义日期解析器，优先级高于默认解析器。
     *
     * @param parser 自定义日期解析器
     * @return 当前实例
     */
    public RegisterDateParser register(final PredicateDateParser parser) {
        this.list.add(0, parser);
        return this;
    }

}
