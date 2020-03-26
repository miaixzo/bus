/*********************************************************************************
 *                                                                               *
 * The MIT License                                                               *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
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
 ********************************************************************************/
package org.aoju.bus.core.codec;

import org.aoju.bus.core.lang.Normal;

/**
 * 凯撒密码实现
 *
 * @author Kimi Liu
 * @version 5.8.1
 * @since JDK 1.8+
 */
public class Caesar {

    /**
     * 传入明文,加密得到密文
     *
     * @param message 加密的消息
     * @param offset  偏移量
     * @return 加密后的内容
     */
    public static String encode(String message, int offset) {
        final int len = message.length();
        final char[] plain = message.toCharArray();
        char c;
        for (int i = 0; i < len; i++) {
            c = message.charAt(i);
            if (false == Character.isLetter(c)) {
                continue;
            }
            plain[i] = encodeChar(c, offset);
        }
        return new String(plain);
    }

    /**
     * 传入明文解密到密文
     *
     * @param cipher 密文
     * @param offset 偏移量
     * @return 解密后的内容
     */
    public static String decode(String cipher, int offset) {
        final int len = cipher.length();
        final char[] plain = cipher.toCharArray();
        char c;
        for (int i = 0; i < len; i++) {
            c = cipher.charAt(i);
            if (false == Character.isLetter(c)) {
                continue;
            }
            plain[i] = decodeChar(c, offset);
        }
        return new String(plain);
    }

    /**
     * 加密轮盘
     *
     * @param c      被加密字符
     * @param offset 偏移量
     * @return 加密后的字符
     */
    private static char encodeChar(char c, int offset) {
        int position = (Normal.UPPER_LOWER.indexOf(c) + offset) % 52;
        return Normal.UPPER_LOWER.charAt(position);

    }

    /**
     * 解密轮盘
     *
     * @param c      字符
     * @param offset 偏移量
     * @return 解密后的字符
     */
    private static char decodeChar(char c, int offset) {
        int position = (Normal.UPPER_LOWER.indexOf(c) - offset) % 52;
        if (position < 0) {
            position += 52;
        }
        return Normal.UPPER_LOWER.charAt(position);
    }

}
