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
package org.miaixz.bus.core.codec.binary.encoder;

import org.miaixz.bus.core.codec.Encoder;
import org.miaixz.bus.core.codec.binary.provider.Base58Provider;
import org.miaixz.bus.core.lang.Normal;

import java.util.Arrays;

/**
 * Base58编码器
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Base58Encoder implements Encoder<byte[], String> {

    public static final String DEFAULT_ALPHABET = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz";

    /**
     * 编码器
     */
    public static final Base58Encoder ENCODER = new Base58Encoder(DEFAULT_ALPHABET.toCharArray());

    private final char[] alphabet;
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
        data = Arrays.copyOf(data, data.length);
        final char[] encoded = new char[data.length * 2];
        int outputStart = encoded.length;
        for (int inputStart = zeroCount; inputStart < data.length; ) {
            encoded[--outputStart] = alphabet[Base58Provider.divmod(data, inputStart, 256, 58)];
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
