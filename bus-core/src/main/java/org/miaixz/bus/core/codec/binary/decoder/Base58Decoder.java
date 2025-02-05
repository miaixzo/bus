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
package org.miaixz.bus.core.codec.binary.decoder;

import java.util.Arrays;

import org.miaixz.bus.core.codec.Decoder;
import org.miaixz.bus.core.codec.binary.encoder.Base58Encoder;
import org.miaixz.bus.core.codec.binary.provider.Base58Provider;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * Base58解码器
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Base58Decoder implements Decoder<CharSequence, byte[]> {

    /**
     * 解码器
     */
    public static Base58Decoder DECODER = new Base58Decoder(Base58Encoder.DEFAULT_ALPHABET);

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
        for (int inputStart = zeros; inputStart < input58.length;) {
            decoded[--outputStart] = Base58Provider.divmod(input58, inputStart, 58, 256);
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
