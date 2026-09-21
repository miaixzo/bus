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
package org.miaixz.bus.core.codec.binary;

import java.io.ByteArrayOutputStream;
import java.io.Serial;
import java.io.Serializable;
import java.util.Arrays;

import org.miaixz.bus.core.codec.Decoder;
import org.miaixz.bus.core.codec.Encoder;
import org.miaixz.bus.core.xyz.ArrayKit;

/**
 * Encodes and decodes Base85 byte sequences.
 * <p>
 * Base85 maps four binary bytes into five printable characters. The default mode uses the continuous Ascii85 alphabet
 * from {@code '!'} to {@code 'u'}, and the optional Z85 mode uses the ZeroMQ alphabet.
 *
 * @author Kimi Liu
 */
public class Base85Codec implements Encoder<byte[], byte[]>, Decoder<byte[], byte[]>, Serializable {

    @Serial
    private static final long serialVersionUID = 2852283255072L;

    /**
     * The shared Base85 codec instance.
     */
    public static final Base85Codec INSTANCE = new Base85Codec();

    /**
     * The number of raw bytes processed by a full Base85 block.
     */
    private static final int CHUNK_SIZE = 4;

    /**
     * The number of encoded characters produced by a full Base85 block.
     */
    private static final int ENCODED_CHUNK_SIZE = 5;

    /**
     * Creates a new {@code Base85Codec} instance.
     */
    public Base85Codec() {
        // No initialization required.
    }

    /**
     * Encodes bytes into Base85 bytes using the Ascii85 alphabet.
     *
     * @param data The bytes to encode.
     * @return The encoded bytes.
     */
    @Override
    public byte[] encode(final byte[] data) {
        return encode(data, false);
    }

    /**
     * Encodes bytes into Base85 bytes.
     *
     * @param data   The bytes to encode.
     * @param useZ85 Whether to use the ZeroMQ Z85 alphabet.
     * @return The encoded bytes.
     */
    public byte[] encode(final byte[] data, final boolean useZ85) {
        final Base85Encoder encoder = useZ85 ? Base85Encoder.Z85_ENCODER : Base85Encoder.STANDARD_ENCODER;
        return encoder.encode(data);
    }

    /**
     * Decodes Base85 bytes using the Ascii85 alphabet.
     *
     * @param encoded The Base85 bytes to decode.
     * @return The decoded bytes.
     */
    @Override
    public byte[] decode(final byte[] encoded) {
        return decode(encoded, false);
    }

    /**
     * Decodes Base85 bytes.
     *
     * @param encoded The Base85 bytes to decode.
     * @param useZ85  Whether to use the ZeroMQ Z85 alphabet.
     * @return The decoded bytes.
     */
    public byte[] decode(final byte[] encoded, final boolean useZ85) {
        final Base85Decoder decoder = useZ85 ? Base85Decoder.Z85_DECODER : Base85Decoder.STANDARD_DECODER;
        return decoder.decode(encoded);
    }

    /**
     * Base85 encoder backed by a caller-provided alphabet.
     */
    public static class Base85Encoder implements Encoder<byte[], byte[]> {

        /**
         * The continuous Ascii85 alphabet from {@code '!'} to {@code 'u'}.
         */
        private static final byte[] STANDARD = new byte[85];

        /**
         * The shared encoder for the Ascii85 alphabet.
         */
        public static final Base85Encoder STANDARD_ENCODER = new Base85Encoder(STANDARD);

        /**
         * The ZeroMQ Z85 alphabet.
         */
        private static final byte[] Z85 = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e',
                'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
                'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U',
                'V', 'W', 'X', 'Y', 'Z', '.', '-', ':', '+', '=', '^', '!', '/', '*', '?', '&', '<', '>', '(', ')', '[',
                ']', '{', '}', '@', '%', '$', '#' };

        /**
         * The shared encoder for the ZeroMQ Z85 alphabet.
         */
        public static final Base85Encoder Z85_ENCODER = new Base85Encoder(Z85);

        static {
            for (int i = 0; i < STANDARD.length; i++) {
                STANDARD[i] = (byte) ('!' + i);
            }
        }

        /**
         * The alphabet used to convert Base85 digits to bytes.
         */
        private final byte[] alphabet;

        /**
         * Creates an encoder that uses the supplied alphabet.
         *
         * @param alphabet The 85-character alphabet.
         */
        public Base85Encoder(final byte[] alphabet) {
            this.alphabet = alphabet;
        }

        /**
         * Estimates the encoded byte length for the specified input length.
         *
         * @param inputLength The input byte length.
         * @return The estimated encoded byte length.
         */
        private static int estimateOutputLength(final int inputLength) {
            return (int) Math.ceil(inputLength * ENCODED_CHUNK_SIZE / (double) CHUNK_SIZE);
        }

        /**
         * Encodes bytes into Base85 bytes.
         *
         * @param data The bytes to encode.
         * @return The encoded bytes.
         */
        @Override
        public byte[] encode(final byte[] data) {
            if (ArrayKit.isEmpty(data)) {
                return new byte[0];
            }

            final ByteArrayOutputStream out = new ByteArrayOutputStream(estimateOutputLength(data.length));
            long tuple = 0;
            int count = 0;

            for (final byte value : data) {
                tuple = (tuple << 8) | (value & 0xff);
                count++;

                if (count == CHUNK_SIZE) {
                    writeEncodedTuple(out, tuple, ENCODED_CHUNK_SIZE);
                    tuple = 0;
                    count = 0;
                }
            }

            if (count > 0) {
                tuple <<= 8 * (CHUNK_SIZE - count);
                writeEncodedTuple(out, tuple, count + 1);
            }

            return out.toByteArray();
        }

        /**
         * Writes one unsigned 32-bit tuple as Base85 characters.
         *
         * @param out       The output stream receiving encoded bytes.
         * @param tuple     The unsigned 32-bit tuple held in a {@code long}.
         * @param charCount The number of Base85 characters to emit.
         */
        private void writeEncodedTuple(final ByteArrayOutputStream out, long tuple, final int charCount) {
            final byte[] encoded = new byte[ENCODED_CHUNK_SIZE];
            for (int i = ENCODED_CHUNK_SIZE - 1; i >= 0; i--) {
                encoded[i] = alphabet[(int) (tuple % 85)];
                tuple /= 85;
            }
            for (int i = 0; i < charCount; i++) {
                out.write(encoded[i]);
            }
        }

    }

    /**
     * Base85 decoder backed by a caller-provided alphabet.
     */
    public static class Base85Decoder implements Decoder<byte[], byte[]> {

        /**
         * The shared decoder for the Ascii85 alphabet.
         */
        public static final Base85Decoder STANDARD_DECODER = new Base85Decoder(Base85Encoder.STANDARD);

        /**
         * The shared decoder for the ZeroMQ Z85 alphabet.
         */
        public static final Base85Decoder Z85_DECODER = new Base85Decoder(Base85Encoder.Z85);

        /**
         * Lookup table that maps encoded byte values to Base85 digit values.
         */
        private final byte[] lookupTable;

        /**
         * Creates a decoder that uses the supplied alphabet.
         *
         * @param alphabet The 85-character alphabet.
         */
        public Base85Decoder(final byte[] alphabet) {
            int maxChar = 0;
            for (final byte value : alphabet) {
                maxChar = Math.max(maxChar, value & 0xff);
            }
            this.lookupTable = new byte[maxChar + 1];
            Arrays.fill(this.lookupTable, (byte) -1);
            for (int i = 0; i < alphabet.length; i++) {
                this.lookupTable[alphabet[i] & 0xff] = (byte) i;
            }
        }

        /**
         * Writes decoded bytes from one unsigned 32-bit tuple.
         *
         * @param out       The output stream receiving decoded bytes.
         * @param tuple     The decoded unsigned 32-bit tuple.
         * @param byteCount The number of decoded bytes to emit.
         */
        private static void writeDecodedTuple(final ByteArrayOutputStream out, final long tuple, final int byteCount) {
            for (int i = CHUNK_SIZE - 1; i >= CHUNK_SIZE - byteCount; i--) {
                out.write((int) ((tuple >> (i * 8)) & 0xff));
            }
        }

        /**
         * Estimates the decoded byte length for the specified encoded length.
         *
         * @param encodedLength The encoded byte length.
         * @return The estimated decoded byte length.
         */
        private static int estimateDecodedLength(final int encodedLength) {
            return (int) Math.ceil(encodedLength * CHUNK_SIZE / (double) ENCODED_CHUNK_SIZE);
        }

        /**
         * Decodes Base85 bytes into raw bytes.
         *
         * @param encoded The Base85 bytes to decode.
         * @return The decoded bytes.
         */
        @Override
        public byte[] decode(final byte[] encoded) {
            if (ArrayKit.isEmpty(encoded)) {
                return new byte[0];
            }

            final ByteArrayOutputStream out = new ByteArrayOutputStream(estimateDecodedLength(encoded.length));
            long tuple = 0;
            int count = 0;

            for (final byte value : encoded) {
                final int index = value & 0xff;
                if (index >= lookupTable.length || lookupTable[index] == -1) {
                    throw new IllegalArgumentException("Invalid Base85 character: '" + (char) value + "'");
                }

                tuple = tuple * 85 + lookupTable[index];
                count++;

                if (count == ENCODED_CHUNK_SIZE) {
                    writeDecodedTuple(out, tuple, CHUNK_SIZE);
                    tuple = 0;
                    count = 0;
                }
            }

            if (count > 0) {
                if (count == 1) {
                    throw new IllegalArgumentException("Invalid Base85 encoding: single trailing character");
                }
                for (int i = count; i < ENCODED_CHUNK_SIZE; i++) {
                    tuple = tuple * 85 + 84;
                }
                writeDecodedTuple(out, tuple, count - 1);
            }

            return out.toByteArray();
        }

    }

}
