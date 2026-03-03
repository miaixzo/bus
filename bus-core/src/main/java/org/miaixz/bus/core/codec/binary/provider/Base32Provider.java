/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.core.codec.binary.provider;

import java.io.Serial;
import java.io.Serializable;

import org.miaixz.bus.core.codec.Decoder;
import org.miaixz.bus.core.codec.Encoder;
import org.miaixz.bus.core.codec.binary.decoder.Base32Decoder;
import org.miaixz.bus.core.codec.binary.encoder.Base32Encoder;

/**
 * Provides Base32 encoding and decoding as defined by RFC 4648.
 * <p>
 * Base32 uses a 32-character set to represent binary data. Five ASCII characters are encoded into eight Base32
 * characters, resulting in a 3/5 increase in length. Padding with '=' is used if the input data is not a multiple of 5
 * bytes.
 *
 * <p>
 * This class supports two alphabets as per RFC 4648:
 * <ul>
 * <li>Base 32 Alphabet (ABCDEFGHIJKLMNOPQRSTUVWXYZ234567)</li>
 * <li>"Extended Hex" Base 32 Alphabet (0123456789ABCDEFGHIJKLMNOPQRSTUV)</li>
 * </ul>
 *
 * @author Kimi Liu
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc4648#section-6">RFC 4648 Section 6</a>
 * @since Java 17+
 */
public class Base32Provider implements Encoder<byte[], String>, Decoder<CharSequence, byte[]>, Serializable {

    /**
     * Constructs a new Base32Provider. Utility class constructor for static access.
     */
    private Base32Provider() {
    }

    @Serial
    private static final long serialVersionUID = 2852258698190L;

    /**
     * Singleton instance of the Base32Provider.
     */
    public static Base32Provider INSTANCE = new Base32Provider();

    /**
     * Encodes a byte array into a Base32 string using the default alphabet.
     *
     * @param data The byte array to encode.
     * @return The Base32 encoded string.
     */
    @Override
    public String encode(final byte[] data) {
        return encode(data, false);
    }

    /**
     * Encodes a byte array into a Base32 string.
     *
     * @param data   The byte array to encode.
     * @param useHex If {@code true}, the "Extended Hex" alphabet is used; otherwise, the default alphabet is used.
     * @return The Base32 encoded string.
     */
    public String encode(final byte[] data, final boolean useHex) {
        final Base32Encoder encoder = useHex ? Base32Encoder.HEX_ENCODER : Base32Encoder.ENCODER;
        return encoder.encode(data);
    }

    /**
     * Decodes a Base32 encoded string using the default alphabet.
     *
     * @param encoded The Base32 string to decode.
     * @return The decoded byte array.
     */
    @Override
    public byte[] decode(final CharSequence encoded) {
        return decode(encoded, false);
    }

    /**
     * Decodes a Base32 encoded string.
     *
     * @param encoded The Base32 string to decode.
     * @param useHex  If {@code true}, the "Extended Hex" alphabet is used for decoding; otherwise, the default alphabet
     *                is used.
     * @return The decoded byte array.
     */
    public byte[] decode(final CharSequence encoded, final boolean useHex) {
        final Base32Decoder decoder = useHex ? Base32Decoder.HEX_DECODER : Base32Decoder.DECODER;
        return decoder.decode(encoded);
    }

}
