/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
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

    /**
     * Constructs a new Base58Provider. Utility class constructor for static access.
     */
    private Base58Provider() {
    }

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
