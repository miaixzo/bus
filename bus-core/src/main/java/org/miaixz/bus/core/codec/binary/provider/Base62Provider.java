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

import java.io.ByteArrayOutputStream;
import java.io.Serial;
import java.io.Serializable;

import org.miaixz.bus.core.codec.Decoder;
import org.miaixz.bus.core.codec.Encoder;
import org.miaixz.bus.core.codec.binary.decoder.Base62Decoder;
import org.miaixz.bus.core.codec.binary.encoder.Base62Encoder;
import org.miaixz.bus.core.xyz.ArrayKit;

/**
 * Provides Base62 encoding and decoding functionality, commonly used for short URLs. The implementation is based on the
 * work from https://github.com/seruco/base62.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Base62Provider implements Encoder<byte[], byte[]>, Decoder<byte[], byte[]>, Serializable {

    /**
     * Constructs a new Base62Provider. Utility class constructor for static access.
     */
    private Base62Provider() {
    }

    @Serial
    private static final long serialVersionUID = 2852259077806L;

    /**
     * Singleton instance of the Base62Provider.
     */
    public static Base62Provider INSTANCE = new Base62Provider();

    /**
     * Translates a byte array of indices into a byte array of characters from a dictionary.
     *
     * @param indices    The byte array of indices.
     * @param dictionary The dictionary to use for translation.
     * @return The translated byte array.
     */
    public static byte[] translate(final byte[] indices, final byte[] dictionary) {
        final byte[] translation = new byte[indices.length];

        for (int i = 0; i < indices.length; i++) {
            translation[i] = dictionary[indices[i]];
        }

        return translation;
    }

    /**
     * Converts a byte array from a source base to a target base.
     *
     * @param message    The byte array to convert.
     * @param sourceBase The source base.
     * @param targetBase The target base.
     * @return The converted byte array.
     */
    public static byte[] convert(final byte[] message, final int sourceBase, final int targetBase) {
        // Estimate the length of the output to avoid resizing the buffer.
        final int estimatedLength = estimateOutputLength(message.length, sourceBase, targetBase);
        final ByteArrayOutputStream out = new ByteArrayOutputStream(estimatedLength);

        byte[] source = message;

        while (source.length > 0) {
            final ByteArrayOutputStream quotient = new ByteArrayOutputStream(source.length);
            int remainder = 0;

            for (final byte b : source) {
                final int accumulator = (b & 0xFF) + remainder * sourceBase;
                final int digit = (accumulator - (accumulator % targetBase)) / targetBase;
                remainder = accumulator % targetBase;

                if (quotient.size() > 0 || digit > 0) {
                    quotient.write(digit);
                }
            }

            out.write(remainder);
            source = quotient.toByteArray();
        }

        // Pad the output with leading zeros to match the input.
        for (int i = 0; i < message.length - 1 && message[i] == 0; i++) {
            out.write(0);
        }

        return ArrayKit.reverse(out.toByteArray());
    }

    /**
     * Estimates the length of the output when converting between bases.
     *
     * @param inputLength The length of the input array.
     * @param sourceBase  The source base.
     * @param targetBase  The target base.
     * @return The estimated output length.
     */
    private static int estimateOutputLength(final int inputLength, final int sourceBase, final int targetBase) {
        return (int) Math.ceil((Math.log(sourceBase) / Math.log(targetBase)) * inputLength);
    }

    /**
     * Encodes a byte array into a Base62 byte array using the GMP-style alphabet.
     *
     * @param data The byte array to encode.
     * @return The Base62 encoded byte array.
     */
    @Override
    public byte[] encode(final byte[] data) {
        return encode(data, false);
    }

    /**
     * Encodes a byte array into a Base62 byte array.
     *
     * @param data        The byte array to encode.
     * @param useInverted If {@code true}, the inverted alphabet is used; otherwise, the GMP-style alphabet is used.
     * @return The Base62 encoded byte array.
     */
    public byte[] encode(final byte[] data, final boolean useInverted) {
        final Base62Encoder encoder = useInverted ? Base62Encoder.INVERTED_ENCODER : Base62Encoder.GMP_ENCODER;
        return encoder.encode(data);
    }

    /**
     * Decodes a Base62 byte array using the GMP-style alphabet.
     *
     * @param encoded The Base62 byte array to decode.
     * @return The decoded byte array.
     */
    @Override
    public byte[] decode(final byte[] encoded) {
        return decode(encoded, false);
    }

    /**
     * Decodes a Base62 byte array.
     *
     * @param encoded     The Base62 byte array to decode.
     * @param useInverted If {@code true}, the inverted alphabet is used for decoding; otherwise, the GMP-style alphabet
     *                    is used.
     * @return The decoded byte array.
     */
    public byte[] decode(final byte[] encoded, final boolean useInverted) {
        final Base62Decoder decoder = useInverted ? Base62Decoder.INVERTED_DECODER : Base62Decoder.GMP_DECODER;
        return decoder.decode(encoded);
    }

}
