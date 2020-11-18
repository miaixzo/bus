/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
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

import org.aoju.bus.core.lang.Symbol;

/**
 * BCD码(Binary-Coded Decimal‎)亦称二进码十进数或二-十进制代码
 * BCD码这种编码形式利用了四个位元来储存一个十进制的数码,
 * 使二进制和十进制之间的转换得以快捷的进行
 *
 * @author Kimi Liu
 * @version 6.1.2
 * @since JDK 1.8+
 */
public class BCD {

    /**
     * 字符串转BCD码
     *
     * @param asc ASCII字符串
     * @return BCD
     */
    public static byte[] strToBcd(String asc) {
        int len = asc.length();
        int mod = len % 2;
        if (mod != 0) {
            asc = Symbol.ZERO + asc;
            len = asc.length();
        }
        byte abt[] = new byte[len];
        if (len >= 2) {
            len >>= 1;
        }
        byte bbt[] = new byte[len];
        abt = asc.getBytes();
        int j;
        int k;
        for (int p = 0; p < asc.length() / 2; p++) {
            if ((abt[2 * p] >= Symbol.C_ZERO) && (abt[2 * p] <= Symbol.C_NINE)) {
                j = abt[2 * p] - Symbol.C_ZERO;
            } else if ((abt[2 * p] >= 'a') && (abt[2 * p] <= 'z')) {
                j = abt[2 * p] - 'a' + 0x0a;
            } else {
                j = abt[2 * p] - 'A' + 0x0a;
            }
            if ((abt[2 * p + 1] >= Symbol.C_ZERO) && (abt[2 * p + 1] <= Symbol.C_NINE)) {
                k = abt[2 * p + 1] - Symbol.C_ZERO;
            } else if ((abt[2 * p + 1] >= 'a') && (abt[2 * p + 1] <= 'z')) {
                k = abt[2 * p + 1] - 'a' + 0x0a;
            } else {
                k = abt[2 * p + 1] - 'A' + 0x0a;
            }
            int a = (j << 4) + k;
            byte b = (byte) a;
            bbt[p] = b;
        }
        return bbt;
    }

    /**
     * ASCII转BCD
     *
     * @param ascii ASCII byte数组
     * @return BCD
     */
    public static byte[] ascToBcd(byte[] ascii) {
        return ascToBcd(ascii, ascii.length);
    }

    /**
     * ASCII转BCD
     *
     * @param ascii     ASCII byte数组
     * @param ascLength 长度
     * @return BCD
     */
    public static byte[] ascToBcd(byte[] ascii, int ascLength) {
        byte[] bcd = new byte[ascLength / 2];
        int j = 0;
        for (int i = 0; i < (ascLength + 1) / 2; i++) {
            bcd[i] = ascToBcd(ascii[j++]);
            bcd[i] = (byte) (((j >= ascLength) ? 0x00 : ascToBcd(ascii[j++])) + (bcd[i] << 4));
        }
        return bcd;
    }

    /**
     * BCD转ASCII字符串
     *
     * @param bytes BCD byte数组
     * @return ASCII字符串
     */
    public static String bcdToStr(byte[] bytes) {
        char temp[] = new char[bytes.length * 2], val;

        for (int i = 0; i < bytes.length; i++) {
            val = (char) (((bytes[i] & 0xf0) >> 4) & 0x0f);
            temp[i * 2] = (char) (val > 9 ? val + 'A' - 10 : val + Symbol.C_ZERO);

            val = (char) (bytes[i] & 0x0f);
            temp[i * 2 + 1] = (char) (val > 9 ? val + 'A' - 10 : val + Symbol.C_ZERO);
        }
        return new String(temp);
    }

    /**
     * 转换单个byte为BCD
     *
     * @param asc ACSII
     * @return BCD
     */
    private static byte ascToBcd(byte asc) {
        byte bcd;

        if ((asc >= Symbol.C_ZERO) && (asc <= Symbol.C_NINE)) {
            bcd = (byte) (asc - Symbol.C_ZERO);
        } else if ((asc >= 'A') && (asc <= 'F')) {
            bcd = (byte) (asc - 'A' + 10);
        } else if ((asc >= 'a') && (asc <= 'f')) {
            bcd = (byte) (asc - 'a' + 10);
        } else {
            bcd = (byte) (asc - 48);
        }
        return bcd;
    }

}
