/*
 * The MIT License
 *
 * Copyright (c) 2017, aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.core.codec;

import org.aoju.bus.core.utils.ArrayUtils;
import org.aoju.bus.core.utils.StringUtils;

import java.nio.charset.Charset;

/**
 * Base64解码实现
 *
 * @author Kimi Liu
 * @version 3.1.2
 * @since JDK 1.8
 */
public class Base64Decoder {

    /**
     * base64解码
     *
     * @param source 被解码的base64字符串
     * @return 被加密后的字符串
     */
    public static String decodeStr(String source) {
        return decodeStr(source, org.aoju.bus.core.consts.Charset.UTF_8);
    }

    /**
     * base64解码
     *
     * @param source  被解码的base64字符串
     * @param charset 字符集
     * @return 被加密后的字符串
     */
    public static String decodeStr(String source, String charset) {
        return StringUtils.str(decode(source, charset), charset);
    }

    /**
     * base64解码
     *
     * @param source  被解码的base64字符串
     * @param charset 字符集
     * @return 被加密后的字符串
     */
    public static String decodeStr(String source, Charset charset) {
        return StringUtils.str(decode(source, charset), charset);
    }

    /**
     * base64解码
     *
     * @param source 被解码的base64字符串
     * @return 被加密后的字符串
     */
    public static byte[] decode(String source) {
        return decode(source, org.aoju.bus.core.consts.Charset.UTF_8);
    }

    /**
     * base64解码
     *
     * @param source  被解码的base64字符串
     * @param charset 字符集
     * @return 被加密后的字符串
     */
    public static byte[] decode(String source, String charset) {
        return decode(StringUtils.bytes(source, charset));
    }

    /**
     * base64解码
     *
     * @param source  被解码的base64字符串
     * @param charset 字符集
     * @return 被加密后的字符串
     */
    public static byte[] decode(String source, Charset charset) {
        return decode(StringUtils.bytes(source, charset));
    }

    /**
     * 解码Base64
     *
     * @param in 输入
     * @return 解码后的bytes
     */
    public static byte[] decode(byte[] in) {
        if (ArrayUtils.isEmpty(in)) {
            return in;
        }
        return decode(in, 0, in.length);
    }

    /**
     * 解码Base64
     *
     * @param in     输入
     * @param pos    开始位置
     * @param length 长度
     * @return 解码后的bytes
     */
    public static byte[] decode(byte[] in, int pos, int length) {
        if (ArrayUtils.isEmpty(in)) {
            return in;
        }

        final IntWrapper offset = new IntWrapper(pos);

        byte sestet0;
        byte sestet1;
        byte sestet2;
        byte sestet3;
        int maxPos = pos + length - 1;
        int octetId = 0;
        byte[] octet = new byte[length * 3 / 4];// over-estimated if non-base64 characters present
        while (offset.value <= maxPos) {
            sestet0 = getNextValidDecodeByte(in, offset, maxPos);
            sestet1 = getNextValidDecodeByte(in, offset, maxPos);
            sestet2 = getNextValidDecodeByte(in, offset, maxPos);
            sestet3 = getNextValidDecodeByte(in, offset, maxPos);

            if (Base64.PADDING != sestet1) {
                octet[octetId++] = (byte) ((sestet0 << 2) | (sestet1 >>> 4));
            }
            if (Base64.PADDING != sestet2) {
                octet[octetId++] = (byte) (((sestet1 & 0xf) << 4) | (sestet2 >>> 2));
            }
            if (Base64.PADDING != sestet3) {
                octet[octetId++] = (byte) (((sestet2 & 3) << 6) | sestet3);
            }
        }

        if (octetId == octet.length) {
            return octet;
        } else {
            // 如果有非Base64字符混入，则实际结果比解析的要短，截取之
            return (byte[]) ArrayUtils.copy(octet, new byte[octetId], octetId);
        }
    }

    /**
     * 获取下一个有效的byte字符
     *
     * @param in     输入
     * @param pos    当前位置，调用此方法后此位置保持在有效字符的下一个位置
     * @param maxPos 最大位置
     * @return 有效字符，如果达到末尾返回
     */
    private static byte getNextValidDecodeByte(byte[] in, IntWrapper pos, int maxPos) {
        byte base64Byte;
        byte decodeByte;
        while (pos.value <= maxPos) {
            base64Byte = in[pos.value++];
            if (base64Byte > -1) {
                decodeByte = Base64.DECODE_TABLE[base64Byte];
                if (decodeByte > -1) {
                    return decodeByte;
                }
            }
        }
        return Base64.PADDING;
    }

    /**
     * int包装，使之可变
     */
    private static class IntWrapper {
        int value;

        IntWrapper(int value) {
            this.value = value;
        }
    }

}
