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

import org.miaixz.bus.core.lang.exception.DateException;
import org.miaixz.bus.core.xyz.ListKit;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 使用正则列表方式的日期解析器
 * 通过定义若干的日期正则，遍历匹配到给定正则后，按照正则方式解析为日期
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class RegexListDateParser implements DateParser, Serializable {

    private static final long serialVersionUID = -1L;
    private final List<Pattern> list;

    /**
     * 构造
     *
     * @param list 正则列表
     */
    public RegexListDateParser(final List<Pattern> list) {
        this.list = list;
    }

    /**
     * 根据给定的正则列表创建
     *
     * @param args 正则列表
     * @return this
     */
    public static RegexListDateParser of(final Pattern... args) {
        return new RegexListDateParser(ListKit.of(args));
    }

    /**
     * 新增自定义日期正则
     *
     * @param regex 日期正则
     * @return this
     */
    public RegexListDateParser addRegex(final String regex) {
        // 日期正则忽略大小写
        return addPattern(Pattern.compile(regex, Pattern.CASE_INSENSITIVE));
    }

    /**
     * 新增自定义日期正则
     *
     * @param pattern 日期正则
     * @return this
     */
    public RegexListDateParser addPattern(final Pattern pattern) {
        this.list.add(pattern);
        return this;
    }

    @Override
    public Date parse(final CharSequence source) throws DateException {
        Matcher matcher;
        for (final Pattern pattern : this.list) {
            matcher = pattern.matcher(source);
            if (matcher.matches()) {
                return RegexDateParser.parse(matcher);
            }
        }
        throw new DateException("No valid pattern for date string: [{}]", source);
    }

}
