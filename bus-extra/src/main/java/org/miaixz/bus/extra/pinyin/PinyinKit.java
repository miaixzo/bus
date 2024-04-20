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
package org.miaixz.bus.extra.pinyin;

import org.miaixz.bus.core.exception.InternalException;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.RegEx;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.toolkit.StringKit;

/**
 * 拼音工具类，通过SPI自动识别
 * 1. TinyPinyin
 * 2. JPinyin
 * 3. Pinyin4j
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class PinyinKit {

    /**
     * 获得全局单例的拼音引擎
     *
     * @return 全局单例的拼音引擎
     */
    public static PinyinProvider getProvider() {
        return PinyinFactory.get();
    }

    /**
     * 如果c为汉字，则返回大写拼音；如果c不是汉字，则返回String.valueOf(c)
     *
     * @param c 任意字符，汉字返回拼音，非汉字原样返回
     * @return 汉字返回拼音，非汉字原样返回
     */
    public static String getPinyin(char c) {
        return getProvider().getPinyin(c);
    }

    /**
     * 将输入字符串转为拼音，每个字之间的拼音使用空格分隔
     *
     * @param text 任意字符，汉字返回拼音，非汉字原样返回
     * @return 汉字返回拼音，非汉字原样返回
     */
    public static String getPinyin(String text) {
        return getPinyin(text, Symbol.SPACE);
    }

    /**
     * 将输入字符串转为拼音，以字符为单位插入分隔符
     *
     * @param text      任意字符，汉字返回拼音，非汉字原样返回
     * @param separator 每个字拼音之间的分隔符
     * @return 汉字返回拼音，非汉字原样返回
     */
    public static String getPinyin(String text, String separator) {
        return getProvider().getPinyin(text, separator);
    }

    /**
     * 将输入字符串转为拼音首字母，其它字符原样返回
     *
     * @param c 任意字符，汉字返回拼音，非汉字原样返回
     * @return 汉字返回拼音，非汉字原样返回
     */
    public static char getFirstLetter(char c) {
        return getProvider().getFirstLetter(c);
    }

    /**
     * 将输入字符串转为拼音首字母，其它字符原样返回
     *
     * @param text      任意字符，汉字返回拼音，非汉字原样返回
     * @param separator 分隔符
     * @return 汉字返回拼音，非汉字原样返回
     */
    public static String getFirstLetter(String text, String separator) {
        return getProvider().getFirstLetter(text, separator);
    }

    /**
     * 获取汉字对应的ascii码
     *
     * @param chs 汉字
     * @return ascii码
     */
    private static int getChsAscii(String chs) {
        int asc;
        byte[] bytes = chs.getBytes(Charset.GBK);
        switch (bytes.length) {
            case 1:
                // 英文字符
                asc = bytes[0];
                break;
            case 2:
                // 中文字符
                int hightByte = Normal._256 + bytes[0];
                int lowByte = Normal._256 + bytes[1];
                asc = (Normal._256 * hightByte + lowByte) - Normal._256 * Normal._256;
                break;
            default:
                throw new InternalException("Illegal resource string");
        }
        return asc;
    }

    /**
     * 是否为中文字符
     *
     * @param c 字符
     * @return 是否为中文字符
     */
    public static boolean isChinese(char c) {
        return '〇' == c || String.valueOf(c).matches(RegEx.CHINESE_PATTERN);
    }

    /**
     * 判断某个字符是否为汉字
     *
     * @param c 需要判断的字符
     * @return 是汉字返回true, 否则返回false
     */
    public static boolean isChinese(String c) {
        if (StringKit.isEmpty(c)) {
            return false;
        }
        return c.matches(RegEx.CHINESE_PATTERN);
    }

}
