/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2023 aoju.org and other contributors.                      *
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
package org.aoju.bus.extra.pinyin;

import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.core.toolkit.ArrayKit;
import org.aoju.bus.core.toolkit.StringKit;

/**
 * 拼音服务提供者
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface PinyinProvider {

    /**
     * 如果c为汉字，则返回大写拼音；如果c不是汉字，则返回String.valueOf(c)
     *
     * @param c 任意字符，汉字返回拼音，非汉字原样返回
     * @return 汉字返回拼音，非汉字原样返回
     */
    String getPinyin(char c);

    /**
     * 获取字符串对应的完整拼音，非中文返回原字符
     *
     * @param text      字符串
     * @param separator 拼音之间的分隔符
     * @return 拼音
     */
    String getPinyin(String text, String separator);

    /**
     * 将输入字符串转为拼音首字母，其它字符原样返回
     *
     * @param c 任意字符，汉字返回拼音，非汉字原样返回
     * @return 汉字返回拼音，非汉字原样返回
     */
    default char getFirstLetter(char c) {
        return getPinyin(c).charAt(0);
    }

    /**
     * 将输入字符串转为拼音首字母，其它字符原样返回
     *
     * @param text      任意字符，汉字返回拼音，非汉字原样返回
     * @param separator 分隔符
     * @return 汉字返回拼音，非汉字原样返回
     */
    default String getFirstLetter(String text, String separator) {
        final String splitSeparator = StringKit.isEmpty(separator) ? Symbol.SHAPE : separator;
        final String[] split = StringKit.splitToArray(getPinyin(text, splitSeparator), splitSeparator);
        return ArrayKit.join(split, separator, (s) -> String.valueOf(s.length() > 0 ? s.charAt(0) : Normal.EMPTY));
    }

}
