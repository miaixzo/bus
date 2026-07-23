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
package org.miaixz.bus.fabric.codec.frame;

import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Length-based frame codec for binary protocols that prefix payloads with a length value.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class LengthCodec implements FrameCodec {

    /**
     * Number of zero-prefixed header bytes before the length field.
     */
    private final int lengthFieldOffset;

    /**
     * Width of the unsigned big-endian length field in bytes.
     */
    private final int lengthFieldSize;

    /**
     * Whether the encoded field value represents the complete frame rather than only the payload.
     */
    private final boolean lengthIncludesHeader;

    /**
     * Maximum decoded or encoded payload size in bytes.
     */
    private final int maxPayloadLength;

    /**
     * Accumulator retaining incomplete frame bytes across decode calls.
     */
    private final Buffer buffer = new Buffer();

    /**
     * Creates a codec from a validated builder snapshot.
     *
     * @param builder builder containing length-field configuration
     */
    private LengthCodec(final Builder builder) {
        this.lengthFieldOffset = validateOffset(builder.lengthFieldOffset);
        this.lengthFieldSize = validateSize(builder.lengthFieldSize);
        this.lengthIncludesHeader = builder.lengthIncludesHeader;
        this.maxPayloadLength = validateMaxPayloadLength(builder.maxPayloadLength);
    }

    /**
     * Creates a codec with a four-byte payload-length field at offset zero and a 16 MiB payload limit.
     *
     * @return codec with default length-field configuration
     */
    public static LengthCodec create() {
        return builder().build();
    }

    /**
     * Creates a builder initialized with the default length-field configuration.
     *
     * @return new length-codec builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Appends non-empty input to the decoder accumulator and emits every complete length-field frame available.
     *
     * @param input non-empty bytes consumed into the decoder accumulator
     * @return immutable list of frames completed by this call, possibly empty when more bytes are required
     * @throws ValidateException if {@code input} is {@code null} or empty
     * @throws ProtocolException if a length field is invalid or exceeds configured limits
     * @throws InternalException if a complete frame cannot be read from the accumulator
     */
    @Override
    public List<Frame> decode(final Buffer input) {
        validateInput(input);
        buffer.write(input, input.size());
        final ArrayList<Frame> frames = new ArrayList<>();
        final int headerLength = headerLength();
        while (buffer.request(headerLength)) {
            final long fieldValue = readLength(buffer, lengthFieldOffset, lengthFieldSize);
            final long totalLength = lengthIncludesHeader ? fieldValue : headerLength + fieldValue;
            if (totalLength < headerLength) {
                throw new ProtocolException("Length-field frame length is smaller than its header");
            }
            final long payloadLength = totalLength - headerLength;
            if (payloadLength > maxPayloadLength) {
                throw new ProtocolException("Length-field payload exceeds maximum length");
            }
            if (totalLength > Integer.MAX_VALUE) {
                throw new ProtocolException("Length-field frame exceeds integer range");
            }
            if (!buffer.request((int) totalLength)) {
                break;
            }
            try {
                buffer.skip(lengthFieldOffset);
                buffer.skip(lengthFieldSize);
                frames.add(Frame.of(buffer.readByteString(payloadLength)));
            } catch (final java.io.EOFException e) {
                throw new InternalException("Unable to read length-field frame", e);
            }
        }
        return List.copyOf(frames);
    }

    /**
     * Writes offset padding, a big-endian length field, and the frame payload to a destination buffer.
     *
     * @param frame  frame whose payload is encoded
     * @param output destination to which encoded bytes are appended
     * @throws ValidateException if {@code frame} or {@code output} is {@code null}
     * @throws ProtocolException if the payload exceeds the configured maximum or its encoded length does not fit the
     *                           configured field width
     */
    @Override
    public void encode(final Frame frame, final Buffer output) {
        final Frame checkedFrame = Assert.notNull(frame, () -> new ValidateException("Frame must not be null"));
        final Buffer checkedOutput = Assert
                .notNull(output, () -> new ValidateException("Frame output must not be null"));
        Assert.isTrue(
                checkedFrame.length() <= maxPayloadLength,
                () -> new ProtocolException("Length-field payload exceeds maximum length"));
        final int headerLength = headerLength();
        final long length = lengthIncludesHeader ? (long) headerLength + checkedFrame.length() : checkedFrame.length();
        validateEncodableLength(length, lengthFieldSize);
        for (int i = 0; i < lengthFieldOffset; i++) {
            checkedOutput.writeByte(0);
        }
        writeLength(checkedOutput, length, lengthFieldSize);
        checkedOutput.write(checkedFrame.payload());
    }

    /**
     * Discards all incomplete bytes retained by the decoder accumulator.
     */
    @Override
    public void reset() {
        buffer.clear();
    }

    /**
     * Returns the header length.
     *
     * @return number of bytes preceding the payload
     */
    private int headerLength() {
        return lengthFieldOffset + lengthFieldSize;
    }

    /**
     * Reads a big-endian length value.
     *
     * @param buffer frame accumulator containing the complete header
     * @param offset zero-based byte offset of the length field
     * @param size   width of the length field in bytes
     * @return non-negative decoded length value
     * @throws ProtocolException if an eight-byte field exceeds the signed {@code long} range
     */
    private static long readLength(final Buffer buffer, final int offset, final int size) {
        long value = 0L;
        for (int i = 0; i < size; i++) {
            final int current = Byte.toUnsignedInt(buffer.getByte(offset + i));
            if (size == Long.BYTES && i == 0 && (current & Normal._128) != 0) {
                throw new ProtocolException("Length-field value exceeds signed long range");
            }
            value = (value << Byte.SIZE) | current;
        }
        return value;
    }

    /**
     * Writes a big-endian length value.
     *
     * @param target destination buffer receiving the field bytes
     * @param value  validated non-negative length value
     * @param size   width of the length field in bytes
     */
    private static void writeLength(final Buffer target, final long value, final int size) {
        for (int shift = (size - 1) * Byte.SIZE; shift >= 0; shift -= Byte.SIZE) {
            target.writeByte((int) ((value >>> shift) & org.miaixz.bus.fabric.Builder.UNSIGNED_BYTE_MASK));
        }
    }

    /**
     * Validates input bytes.
     *
     * @param input decoder input to validate
     * @throws ValidateException if {@code input} is {@code null} or empty
     */
    private static void validateInput(final Buffer input) {
        Assert.isTrue(
                Assert.notNull(input, () -> new ValidateException("Length-field input must not be empty")).size() > 0,
                () -> new ValidateException("Length-field input must not be empty"));
    }

    /**
     * Validates length field offset.
     *
     * @param offset number of bytes preceding the length field
     * @return validated offset in the inclusive range 0 through 1024
     * @throws ValidateException if {@code offset} is outside the supported range
     */
    private static int validateOffset(final int offset) {
        Assert.isTrue(
                offset >= 0 && offset <= Normal._1024,
                () -> new ValidateException("Length-field offset must be between 0 and 1024"));
        return offset;
    }

    /**
     * Validates length field size.
     *
     * @param size requested field width in bytes
     * @return validated field width of 1, 2, 4, or 8 bytes
     * @throws ValidateException if {@code size} is not a supported width
     */
    private static int validateSize(final int size) {
        Assert.isTrue(
                size == Byte.BYTES || size == Short.BYTES || size == Integer.BYTES || size == Long.BYTES,
                () -> new ValidateException("Length-field size must be 1, 2, 4, or 8 bytes"));
        return size;
    }

    /**
     * Validates maximum payload length.
     *
     * @param maxPayloadLength maximum payload length
     * @return validated positive payload limit
     * @throws ValidateException if {@code maxPayloadLength} is not positive
     */
    private static int validateMaxPayloadLength(final int maxPayloadLength) {
        Assert.isTrue(maxPayloadLength > 0, () -> new ValidateException("Maximum payload length must be positive"));
        return maxPayloadLength;
    }

    /**
     * Validates an encodable length value.
     *
     * @param value length value to validate
     * @param size  configured field width in bytes
     * @throws ProtocolException if {@code value} is negative or does not fit the field width
     */
    private static void validateEncodableLength(final long value, final int size) {
        if (value < 0 || value > maxEncodable(size)) {
            throw new ProtocolException("Length-field value does not fit configured field size");
        }
    }

    /**
     * Returns the maximum encodable unsigned value for a field size.
     *
     * @param size field width in bytes
     * @return largest non-negative value supported by the field width and a signed {@code long}
     */
    private static long maxEncodable(final int size) {
        return size == Long.BYTES ? Long.MAX_VALUE : (1L << (size * Byte.SIZE)) - 1L;
    }

    /**
     * Length codec builder.
     */
    public static final class Builder {

        /**
         * Number of zero-prefixed bytes before the length field.
         */
        private int lengthFieldOffset;

        /**
         * Width of the unsigned big-endian length field in bytes.
         */
        private int lengthFieldSize = Integer.BYTES;

        /**
         * Whether the encoded field value includes padding and length-field bytes.
         */
        private boolean lengthIncludesHeader;

        /**
         * Maximum permitted payload size in bytes.
         */
        private int maxPayloadLength = (int) org.miaixz.bus.fabric.Builder.BYTES_16_MIB;

        /**
         * Creates a builder.
         */
        private Builder() {
            // No initialization required.
        }

        /**
         * Sets the length field offset.
         *
         * @param value number of zero-prefixed bytes before the length field
         * @return this builder
         * @throws ValidateException if {@code value} is outside the inclusive range 0 through 1024
         */
        public Builder lengthFieldOffset(final int value) {
            this.lengthFieldOffset = validateOffset(value);
            return this;
        }

        /**
         * Sets the length field size.
         *
         * @param value requested field width of 1, 2, 4, or 8 bytes
         * @return this builder
         * @throws ValidateException if {@code value} is not a supported width
         */
        public Builder lengthFieldSize(final int value) {
            this.lengthFieldSize = validateSize(value);
            return this;
        }

        /**
         * Sets whether length includes header bytes.
         *
         * @param value {@code true} to encode total frame length; {@code false} to encode payload length
         * @return this builder
         */
        public Builder lengthIncludesHeader(final boolean value) {
            this.lengthIncludesHeader = value;
            return this;
        }

        /**
         * Sets the maximum payload length.
         *
         * @param value positive maximum payload size in bytes
         * @return this builder
         * @throws ValidateException if {@code value} is not positive
         */
        public Builder maxPayloadLength(final int value) {
            this.maxPayloadLength = validateMaxPayloadLength(value);
            return this;
        }

        /**
         * Builds the codec.
         *
         * @return codec containing the current validated builder configuration
         */
        public LengthCodec build() {
            return new LengthCodec(this);
        }

    }

}
