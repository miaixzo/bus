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
package org.miaixz.bus.core.codec.binary.provider;

import java.io.Serial;
import java.io.Serializable;

import org.miaixz.bus.core.codec.Decoder;
import org.miaixz.bus.core.codec.Encoder;
import org.miaixz.bus.core.codec.binary.decoder.Base58Decoder;
import org.miaixz.bus.core.codec.binary.encoder.Base58Encoder;

/**
 * Provides Base58 encoding and decoding functionality. This implementation does not include checksums or version bytes.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Base58Provider implements Encoder<byte[], String>, Decoder<CharSequence, byte[]>, Serializable {

    @Serial
    private static final long serialVersionUID = 2852258919299L;

    /**
     * Singleton instance of the Base58Provider.
     */
    public static Base58Provider INSTANCE = new Base58Provider();

    /**
     * Divides a number, represented as a byte array of digits in a given base, by a divisor. The input array is
     * modified in-place to contain the quotient, and the remainder is returned.
     *
     * @param number     The number to be divided. This array will be modified to store the quotient.
     * @param firstDigit The index of the first non-zero digit in the number array (an optimization to skip leading
     *                   zeros).
     * @param base       The base of the number's digits (up to 256).
     * @param divisor    The number to divide by (up to 256).
     * @return The remainder of the division.
     */
    public static byte divmod(final byte[] number, final int firstDigit, final int base, final int divisor) {
        // This is just long division that accounts for the base of the input digits.
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
     * Encodes a byte array into a Base58 string.
     *
     * @param data The data to be encoded, without a checksum.
     * @return The Base58 encoded string.
     */
    @Override
    public String encode(final byte[] data) {
        return Base58Encoder.ENCODER.encode(data);
    }

    /**
     * Decodes a Base58 encoded string into a byte array.
     *
     * @param encoded The Base58 encoded string.
     * @return The decoded byte array.
     * @throws IllegalArgumentException if the input string is not a valid Base58 string.
     */
    @Override
    public byte[] decode(final CharSequence encoded) throws IllegalArgumentException {
        return Base58Decoder.DECODER.decode(encoded);
    }

}
