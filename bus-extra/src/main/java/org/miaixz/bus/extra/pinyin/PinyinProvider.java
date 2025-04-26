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
package org.miaixz.bus.extra.pinyin;

import java.util.List;

import org.miaixz.bus.core.Provider;
import org.miaixz.bus.core.lang.EnumValue;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.text.CharsBacker;
import org.miaixz.bus.core.xyz.CollKit;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * 拼音引擎接口，具体的拼音实现通过实现此接口，完成具体实现功能
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface PinyinProvider extends Provider {

    /**
     * 如果c为汉字，则返回大写拼音；如果c不是汉字，则返回String.valueOf(c)
     *
     * @param c 任意字符，汉字返回拼音，非汉字原样返回
     * @return 汉字返回拼音，非汉字原样返回
     */
    default String getPinyin(final char c) {
        return getPinyin(c, false);
    }

    /**
     * 如果c为汉字，则返回大写拼音；如果c不是汉字，则返回String.valueOf(c)
     *
     * @param c    任意字符，汉字返回拼音，非汉字原样返回
     * @param tone 是否返回声调
     * @return 汉字返回拼音，非汉字原样返回
     */
    String getPinyin(char c, boolean tone);

    /**
     * 获取字符串对应的完整拼音，非中文返回原字符
     *
     * @param text      字符串
     * @param separator 拼音之间的分隔符
     * @return 拼音
     */
    default String getPinyin(final String text, final String separator) {
        return getPinyin(text, separator, false);
    }

    /**
     * 获取字符串对应的完整拼音，非中文返回原字符
     *
     * @param text      字符串
     * @param separator 拼音之间的分隔符
     * @param tone      是否返回声调
     * @return 拼音
     */
    String getPinyin(String text, String separator, boolean tone);

    /**
     * 将输入字符串转为拼音首字母，其它字符原样返回
     *
     * @param c 任意字符，汉字返回拼音，非汉字原样返回
     * @return 汉字返回拼音，非汉字原样返回
     */
    default char getFirstLetter(final char c) {
        return getPinyin(c).charAt(0);
    }

    /**
     * 将输入字符串转为拼音首字母，其它字符原样返回
     *
     * @param str       任意字符，汉字返回拼音，非汉字原样返回
     * @param separator 分隔符
     * @return 汉字返回拼音，非汉字原样返回
     */
    default String getFirstLetter(final String str, final String separator) {
        final String splitSeparator = StringKit.isEmpty(separator) ? Symbol.SHAPE : separator;
        final List<String> split = CharsBacker.split(getPinyin(str, splitSeparator), splitSeparator);
        return CollKit.join(split, separator, (s) -> String.valueOf(!s.isEmpty() ? s.charAt(0) : Normal.EMPTY));
    }

    @Override
    default Object type() {
        return EnumValue.Povider.PINYIN;
    }

}
