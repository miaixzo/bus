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
package org.miaixz.bus.core.text.replacer;

import java.io.Serial;
import java.io.Serializable;
import java.util.function.UnaryOperator;

import org.miaixz.bus.core.xyz.StringKit;

/**
 * 抽象字符串替换类 通过实现replace方法实现局部替换逻辑
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class StringReplacer implements UnaryOperator<CharSequence>, Serializable {

    @Serial
    private static final long serialVersionUID = 2852322990618L;

    /**
     * 抽象的字符串替换方法，通过传入原字符串和当前位置，执行替换逻辑，返回处理或替换的字符串长度部分。
     *
     * @param text 被处理的字符串
     * @param pos  当前位置
     * @param out  输出
     * @return 处理的原字符串长度，0表示跳过此字符
     */
    protected abstract int replace(CharSequence text, int pos, StringBuilder out);

    /**
     * 执行替换，按照{@link #replace(CharSequence, int, StringBuilder)}逻辑替换对应部分，其它部分保持原样
     *
     * @param text 被处理的字符串
     * @return 替换后的字符串
     */
    @Override
    public CharSequence apply(final CharSequence text) {
        if (StringKit.isEmpty(text)) {
            return text;
        }
        final int len = text.length();
        final StringBuilder builder = new StringBuilder(len);
        int pos = 0;// 当前位置
        int consumed;// 处理过的字符数
        while (pos < len) {
            consumed = replace(text, pos, builder);
            if (0 == consumed) {
                // 0表示未处理或替换任何字符，原样输出本字符并从下一个字符继续
                builder.append(text.charAt(pos));
                pos++;
            }
            pos += consumed;
        }
        return builder;
    }

}
