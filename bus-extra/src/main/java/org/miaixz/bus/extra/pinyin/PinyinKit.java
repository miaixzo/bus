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

import org.miaixz.bus.core.center.regex.Pattern;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.PatternKit;

/**
 * 拼音工具类，用于快速获取拼音
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class PinyinKit {

    /**
     * 创建拼音引擎
     *
     * @param engineName 引擎名称
     * @return {@link PinyinProvider}
     */
    public static PinyinProvider createEngine(final String engineName) {
        return PinyinFactory.create(engineName);
    }

    /**
     * 获得全局单例的拼音引擎
     *
     * @return 全局单例的拼音引擎
     */
    public static PinyinProvider getEngine() {
        return PinyinFactory.get();
    }

    /**
     * 如果c为汉字，则返回大写拼音；如果c不是汉字，则返回String.valueOf(c)
     *
     * @param c 任意字符，汉字返回拼音，非汉字原样返回
     * @return 汉字返回拼音，非汉字原样返回
     */
    public static String getPinyin(final char c) {
        return getEngine().getPinyin(c);
    }

    /**
     * 如果c为汉字，则返回大写拼音；如果c不是汉字，则返回String.valueOf(c)
     *
     * @param c    任意字符，汉字返回拼音，非汉字原样返回
     * @param tone 是否保留声调
     * @return 汉字返回拼音，非汉字原样返回
     */
    public static String getPinyin(final char c, final boolean tone) {
        return getEngine().getPinyin(c, tone);
    }

    /**
     * 将输入字符串转为拼音，每个字之间的拼音使用空格分隔
     *
     * @param text 任意字符，汉字返回拼音，非汉字原样返回
     * @return 汉字返回拼音，非汉字原样返回
     */
    public static String getPinyin(final String text) {
        return getPinyin(text, Symbol.SPACE);
    }

    /**
     * 将输入字符串转为拼音，每个字之间的拼音使用空格分隔
     *
     * @param text 任意字符，汉字返回拼音，非汉字原样返回
     * @param tone 是否保留声调
     * @return 汉字返回拼音，非汉字原样返回
     */
    public static String getPinyin(final String text, final boolean tone) {
        return getPinyin(text, Symbol.SPACE, tone);
    }

    /**
     * 将输入字符串转为拼音，以字符为单位插入分隔符
     *
     * @param text      任意字符，汉字返回拼音，非汉字原样返回
     * @param separator 每个字拼音之间的分隔符
     * @return 汉字返回拼音，非汉字原样返回
     */
    public static String getPinyin(final String text, final String separator) {
        return getEngine().getPinyin(text, separator);
    }

    /**
     * 将输入字符串转为拼音，以字符为单位插入分隔符
     *
     * @param text      任意字符，汉字返回拼音，非汉字原样返回
     * @param separator 每个字拼音之间的分隔符
     * @param tone      是否保留声调
     * @return 汉字返回拼音，非汉字原样返回
     */
    public static String getPinyin(final String text, final String separator, final boolean tone) {
        return getEngine().getPinyin(text, separator, tone);
    }

    /**
     * 将输入字符串转为拼音首字母，其它字符原样返回
     *
     * @param c 任意字符，汉字返回拼音，非汉字原样返回
     * @return 汉字返回拼音，非汉字原样返回
     */
    public static char getFirstLetter(final char c) {
        return getEngine().getFirstLetter(c);
    }

    /**
     * 将输入字符串转为拼音首字母，其它字符原样返回
     *
     * @param text      任意字符，汉字返回拼音，非汉字原样返回
     * @param separator 分隔符
     * @return 汉字返回拼音，非汉字原样返回
     */
    public static String getFirstLetter(final String text, final String separator) {
        return getEngine().getFirstLetter(text, separator);
    }

    /**
     * 是否为中文字符
     *
     * @param c 字符
     * @return 是否为中文字符
     */
    public static boolean isChinese(final char c) {
        return '〇' == c || PatternKit.isMatch(Pattern.CHINESE_PATTERN, String.valueOf(c));
    }

}
