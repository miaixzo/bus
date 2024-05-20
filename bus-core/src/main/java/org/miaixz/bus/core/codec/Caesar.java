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
package org.miaixz.bus.core.codec;

import org.miaixz.bus.core.lang.Assert;

/**
 * 凯撒密码实现
 * 算法来自：<a href="https://github.com/zhaorenjie110/SymmetricEncryptionAndDecryption">https://github.com/zhaorenjie110/SymmetricEncryptionAndDecryption</a>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Caesar {

    /**
     * 26个字母表
     */
    public static final String TABLE = "AaBbCcDdEeFfGgHhIiJjKkLlMmNnOoPpQqRrSsTtUuVvWwXxYyZz";

    /**
     * 传入明文，加密得到密文
     *
     * @param message 加密的消息
     * @param offset  偏移量
     * @return 加密后的内容
     */
    public static String encode(final String message, final int offset) {
        Assert.notNull(message, "message must be not null!");
        final int len = message.length();
        final char[] plain = message.toCharArray();
        char c;
        for (int i = 0; i < len; i++) {
            c = message.charAt(i);
            if (!Character.isLetter(c)) {
                continue;
            }
            plain[i] = encodeChar(c, offset);
        }
        return new String(plain);
    }

    /**
     * 传入明文解密到密文
     *
     * @param cipherText 密文
     * @param offset     偏移量
     * @return 解密后的内容
     */
    public static String decode(final String cipherText, final int offset) {
        Assert.notNull(cipherText, "cipherText must be not null!");
        final int len = cipherText.length();
        final char[] plain = cipherText.toCharArray();
        char c;
        for (int i = 0; i < len; i++) {
            c = cipherText.charAt(i);
            if (!Character.isLetter(c)) {
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
    private static char encodeChar(final char c, final int offset) {
        final int position = (TABLE.indexOf(c) + offset) % 52;
        return TABLE.charAt(position);

    }

    /**
     * 解密轮盘
     *
     * @param c 字符
     * @return 解密后的字符
     */
    private static char decodeChar(final char c, final int offset) {
        int position = (TABLE.indexOf(c) - offset) % 52;
        if (position < 0) {
            position += 52;
        }
        return TABLE.charAt(position);
    }

}
