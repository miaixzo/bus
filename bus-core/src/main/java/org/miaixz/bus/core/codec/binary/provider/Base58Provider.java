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
package org.miaixz.bus.core.codec.binary.provider;

import org.miaixz.bus.core.codec.Decoder;
import org.miaixz.bus.core.codec.Encoder;
import org.miaixz.bus.core.codec.binary.decoder.Base58Decoder;
import org.miaixz.bus.core.codec.binary.encoder.Base58Encoder;

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
    public static byte divmod(byte[] number, int firstDigit, int base, int divisor) {
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

}
