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
package org.miaixz.bus.core.text.placeholder;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.text.StringValidator;

import java.util.Objects;
import java.util.function.UnaryOperator;

/**
 * 一个简单的占位符解析器。给定占位符的左右边界符号以及转义符，
 * 将允许把一段字符串中的占位符解析并替换为指定内容，支持指定转义符对边界符号进行转义。
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class PlaceholderParser implements UnaryOperator<String> {

    /**
     * processor
     */
    private final UnaryOperator<String> processor;

    /**
     * 占位符开始符号
     */
    private final String open;

    /**
     * 结束符号长度
     */
    private final int openLength;

    /**
     * 占位符结束符号
     */
    private final String close;

    /**
     * 结束符号长度
     */
    private final int closeLength;

    /**
     * 转义符
     */
    private final char escape;

    /**
     * 创建一个占位符解析器，默认转义符为{@code "\"}
     *
     * @param processor 占位符处理器
     * @param prefix    占位符开始符号，不允许为空
     * @param suffix    占位符结束符号，不允许为空
     */
    public PlaceholderParser(
            final UnaryOperator<String> processor, final String prefix, final String suffix) {
        this(processor, prefix, suffix, Symbol.C_BACKSLASH);
    }

    /**
     * 创建一个占位符解析器
     *
     * @param processor 占位符处理器
     * @param prefix    占位符开始符号，不允许为空
     * @param suffix    占位符结束符号，不允许为空
     * @param escape    转义符
     */
    public PlaceholderParser(
            final UnaryOperator<String> processor, final String prefix, final String suffix, final char escape) {
        Assert.isFalse(StringValidator.isEmpty(prefix), "开始符号不能为空");
        Assert.isFalse(StringValidator.isEmpty(suffix), "结束符号不能为空");
        this.processor = Objects.requireNonNull(processor);
        this.open = prefix;
        this.openLength = prefix.length();
        this.close = suffix;
        this.closeLength = suffix.length();
        this.escape = escape;
    }

    /**
     * 解析并替换字符串中的占位符
     *
     * @param text 待解析的字符串
     * @return 处理后的字符串
     */
    @Override
    public String apply(final String text) {
        if (StringValidator.isEmpty(text)) {
            return Normal.EMPTY;
        }

        // 寻找第一个开始符号
        int closeCursor = 0;
        int openCursor = text.indexOf(open, closeCursor);
        if (openCursor == -1) {
            return text;
        }

        // 开始匹配
        final char[] src = text.toCharArray();
        final StringBuilder result = new StringBuilder(src.length);
        final StringBuilder expression = new StringBuilder();
        while (openCursor > -1) {

            // 开始符号是否被转义，若是则跳过并寻找下一个开始符号
            if (openCursor > 0 && src[openCursor - 1] == escape) {
                result.append(src, closeCursor, openCursor - closeCursor - 1).append(open);
                closeCursor = openCursor + openLength;
                openCursor = text.indexOf(open, closeCursor);
                continue;
            }

            // 记录当前占位符的开始符号与上一占位符的结束符号间的字符串
            result.append(src, closeCursor, openCursor - closeCursor);

            // 重置结束游标至当前占位符的开始处
            closeCursor = openCursor + openLength;

            // 寻找结束符号下标
            int end = text.indexOf(close, closeCursor);
            while (end > -1) {
                // 结束符号被转义，寻找下一个结束符号
                if (end > closeCursor && src[end - 1] == escape) {
                    expression.append(src, closeCursor, end - closeCursor - 1).append(close);
                    closeCursor = end + closeLength;
                    end = text.indexOf(close, closeCursor);
                }
                // 找到结束符号
                else {
                    expression.append(src, closeCursor, end - closeCursor);
                    break;
                }
            }

            // 未能找到结束符号，说明匹配异常
            if (end == -1) {
                throw new InternalException("\"{}\" 中字符下标 {} 处的开始符没有找到对应的结束符", text, openCursor);
            }
            // 找到结束符号，将开始到结束符号之间的字符串替换为指定表达式
            else {
                result.append(processor.apply(expression.toString()));
                expression.setLength(0);
                // 完成当前占位符的处理匹配，寻找下一个
                closeCursor = end + closeLength;
            }

            // 寻找下一个开始符号
            openCursor = text.indexOf(open, closeCursor);
        }

        // 若匹配结束后仍有未处理的字符串，则直接将其拼接到表达式上
        if (closeCursor < src.length) {
            result.append(src, closeCursor, src.length - closeCursor);
        }
        return result.toString();
    }

}
