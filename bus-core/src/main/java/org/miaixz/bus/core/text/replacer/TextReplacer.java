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
package org.miaixz.bus.core.text.replacer;

import org.miaixz.bus.core.lang.Replacer;
import org.miaixz.bus.core.text.TextBuilder;

import java.io.Serializable;

/**
 * 抽象字符串替换类
 * 通过实现replace方法实现局部替换逻辑
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class TextReplacer implements Replacer<CharSequence>, Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 抽象的字符串替换方法，通过传入原字符串和当前位置，执行替换逻辑，返回处理或替换的字符串长度部分。
     *
     * @param text    被处理的字符串
     * @param indexes 当前位置
     * @param builder 输出
     * @return 处理的原字符串长度，0表示跳过此字符
     */
    protected abstract int replace(CharSequence text, int indexes, TextBuilder builder);

    @Override
    public CharSequence replace(CharSequence text) {
        final int len = text.length();
        final TextBuilder builder = TextBuilder.create(len);
        // 当前位置
        int pos = 0;
        // 处理过的字符数
        int consumed;
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
