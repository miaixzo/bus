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
package org.miaixz.bus.core.codec.provider;

import org.miaixz.bus.core.codec.Decoder;
import org.miaixz.bus.core.codec.Encoder;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.toolkit.StringKit;

import java.util.Arrays;

/**
 * Base58编码器
 * 此编码器不包括校验码、版本等信息
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Base58Provider implements Encoder<byte[], String>, Decoder<CharSequence, byte[]> {

    /**
     * 实例
     */
    public static Base58Provider INSTANCE = new Base58Provider();

    /**
     * 将数字除以给定的除数，表示为字节数组，每个字节包含指定基数中的单个数字
     * 给定的数字被就地修改以包含商，返回值是余数
     *
     * @param number     要除的数
     * @param firstDigit 在第一个非零数字的数组中的索引（这用于通过跳过前导零进行优化）
     * @param base       表示数字位数的基数（最多 256）
     * @param divisor    要除以的数（最多 256）
     * @return 除法运算的其余部分
     */
    private static byte divmod(byte[] number, int firstDigit, int base, int divisor) {
        // 用来表示输入数字的基数
        int remainder = 0;
        for (int i = firstDigit; i < number.length; i++) {
            final int digit = (int) number[i] & 0xFF;
            final int temp = remainder * base + digit;
            number[i] = (byte) (temp / divisor);
            remainder = temp % divisor;
        }
        return (byte) remainder;
    }

    /**
     * Base58编码
     *
     * @param data 被编码的数据，不带校验和
     * @return 编码后的字符串
     */
    @Override
    public String encode(final byte[] data) {
        return Base58Encoder.ENCODER.encode(data);
    }

    /**
     * 解码给定的Base58字符串
     *
     * @param encoded Base58编码字符串
     * @return 解码后的bytes
     * @throws IllegalArgumentException 非标准Base58字符串
     */
    @Override
    public byte[] decode(final CharSequence encoded) throws IllegalArgumentException {
        return Base58Decoder.DECODER.decode(encoded);
    }

    /**
     * Base58编码器
     */
    public static class Base58Encoder implements Encoder<byte[], String> {
        private static final String DEFAULT_ALPHABET = Normal.UPPER_NUMBER + "012345";

        /**
         * 默认编码器
         */
        public static final Base58Encoder ENCODER = new Base58Encoder(DEFAULT_ALPHABET.toCharArray());
        /**
         * 编码字母表
         */
        private final char[] alphabet;
        /**
         * 编码字符0
         */
        private final char alphabetZero;

        /**
         * 构造
         *
         * @param alphabet 编码字母表
         */
        public Base58Encoder(final char[] alphabet) {
            this.alphabet = alphabet;
            alphabetZero = alphabet[0];
        }

        @Override
        public String encode(byte[] data) {
            if (null == data) {
                return null;
            }
            if (data.length == 0) {
                return Normal.EMPTY;
            }
            // 计算开头0的个数
            int zeroCount = 0;
            while (zeroCount < data.length && data[zeroCount] == 0) {
                ++zeroCount;
            }
            // 将256位编码转换为58位编码
            data = Arrays.copyOf(data, data.length); // since we modify it in-place
            final char[] encoded = new char[data.length * 2]; // upper bound
            int outputStart = encoded.length;
            for (int inputStart = zeroCount; inputStart < data.length; ) {
                encoded[--outputStart] = alphabet[divmod(data, inputStart, 256, 58)];
                if (data[inputStart] == 0) {
                    ++inputStart; // optimization - skip leading zeros
                }
            }
            // Preserve exactly as many leading encoded zeros in output as there were leading zeros in input.
            while (outputStart < encoded.length && encoded[outputStart] == alphabetZero) {
                ++outputStart;
            }
            while (--zeroCount >= 0) {
                encoded[--outputStart] = alphabetZero;
            }
            // Return encoded string (including encoded leading zeros).
            return new String(encoded, outputStart, encoded.length - outputStart);
        }
    }

    /**
     * Base58解码器
     */
    public static class Base58Decoder implements Decoder<CharSequence, byte[]> {

        /**
         * 默认解码器
         */
        public static Base58Decoder DECODER = new Base58Decoder(Base58Encoder.DEFAULT_ALPHABET);
        /**
         * 查找表
         */
        private final byte[] lookupTable;

        /**
         * 构造
         *
         * @param alphabet 编码字符表
         */
        public Base58Decoder(final String alphabet) {
            final byte[] lookupTable = new byte['z' + 1];
            Arrays.fill(lookupTable, (byte) -1);

            final int length = alphabet.length();
            for (int i = 0; i < length; i++) {
                lookupTable[alphabet.charAt(i)] = (byte) i;
            }
            this.lookupTable = lookupTable;
        }

        @Override
        public byte[] decode(final CharSequence encoded) {
            if (encoded.length() == 0) {
                return new byte[0];
            }
            // Convert the base58-encoded ASCII chars to a base58 byte sequence (base58 digits).
            final byte[] input58 = new byte[encoded.length()];
            for (int i = 0; i < encoded.length(); ++i) {
                final char c = encoded.charAt(i);
                final int digit = c < 128 ? lookupTable[c] : -1;
                if (digit < 0) {
                    throw new IllegalArgumentException(StringKit.format("Invalid char '{}' at [{}]", c, i));
                }
                input58[i] = (byte) digit;
            }
            // Count leading zeros.
            int zeros = 0;
            while (zeros < input58.length && input58[zeros] == 0) {
                ++zeros;
            }
            // Convert base-58 digits to base-256 digits.
            final byte[] decoded = new byte[encoded.length()];
            int outputStart = decoded.length;
            for (int inputStart = zeros; inputStart < input58.length; ) {
                decoded[--outputStart] = divmod(input58, inputStart, 58, 256);
                if (input58[inputStart] == 0) {
                    ++inputStart; // optimization - skip leading zeros
                }
            }
            // Ignore extra leading zeroes that were added during the calculation.
            while (outputStart < decoded.length && decoded[outputStart] == 0) {
                ++outputStart;
            }
            // Return decoded data (including original number of leading zeros).
            return Arrays.copyOfRange(decoded, outputStart - zeros, decoded.length);
        }
    }

}
