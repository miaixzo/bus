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
import org.miaixz.bus.fabric.Builder;

/**
 * Length-based frame codec for binary protocols that prefix payloads with a length value.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class LengthCodec implements FrameCodec {

    /**
     * Length field offset.
     */
    private final int lengthFieldOffset;

    /**
     * Length field byte count.
     */
    private final int lengthFieldSize;

    /**
     * Whether the encoded length includes the header.
     */
    private final boolean lengthIncludesHeader;

    /**
     * Maximum payload length.
     */
    private final int maxPayloadLength;

    /**
     * Decoder buffer.
     */
    private final Buffer buffer = new Buffer();

    /**
     * Creates a codec.
     *
     * @param builder builder
     */
    private LengthCodec(final Builder builder) {
        this.lengthFieldOffset = validateOffset(builder.lengthFieldOffset);
        this.lengthFieldSize = validateSize(builder.lengthFieldSize);
        this.lengthIncludesHeader = builder.lengthIncludesHeader;
        this.maxPayloadLength = validateMaxPayloadLength(builder.maxPayloadLength);
    }

    /**
     * Creates the default codec.
     *
     * @return codec
     */
    public static LengthCodec create() {
        return builder().build();
    }

    /**
     * Creates a builder.
     *
     * @return builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Decodes length-field frames.
     *
     * @param input input bytes
     * @return decoded frames
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
     * Encodes a frame.
     *
     * @param frame  frame
     * @param output encoded byte destination
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
     * Resets buffered bytes.
     */
    @Override
    public void reset() {
        buffer.clear();
    }

    /**
     * Returns the header length.
     *
     * @return header length
     */
    private int headerLength() {
        return lengthFieldOffset + lengthFieldSize;
    }

    /**
     * Reads a big-endian length value.
     *
     * @param buffer frame buffer
     * @param offset field offset
     * @param size   field size
     * @return length value
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
     * @param target target buffer
     * @param value  length value
     * @param size   field size
     */
    private static void writeLength(final Buffer target, final long value, final int size) {
        for (int shift = (size - 1) * Byte.SIZE; shift >= 0; shift -= Byte.SIZE) {
            target.writeByte((int) ((value >>> shift) & org.miaixz.bus.fabric.Builder.UNSIGNED_BYTE_MASK));
        }
    }

    /**
     * Validates input bytes.
     *
     * @param input input bytes
     */
    private static void validateInput(final Buffer input) {
        Assert.isTrue(
                Assert.notNull(input, () -> new ValidateException("Length-field input must not be empty")).size() > 0,
                () -> new ValidateException("Length-field input must not be empty"));
    }

    /**
     * Validates length field offset.
     *
     * @param offset offset
     * @return validated offset
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
     * @param size size
     * @return validated size
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
     * @return validated length
     */
    private static int validateMaxPayloadLength(final int maxPayloadLength) {
        Assert.isTrue(maxPayloadLength > 0, () -> new ValidateException("Maximum payload length must be positive"));
        return maxPayloadLength;
    }

    /**
     * Validates an encodable length value.
     *
     * @param value length value
     * @param size  field size
     */
    private static void validateEncodableLength(final long value, final int size) {
        if (value < 0 || value > maxEncodable(size)) {
            throw new ProtocolException("Length-field value does not fit configured field size");
        }
    }

    /**
     * Returns the maximum encodable unsigned value for a field size.
     *
     * @param size field size
     * @return maximum value
     */
    private static long maxEncodable(final int size) {
        return size == Long.BYTES ? Long.MAX_VALUE : (1L << (size * Byte.SIZE)) - 1L;
    }

    /**
     * Length codec builder.
     */
    public static final class Builder {

        /**
         * Length field offset.
         */
        private int lengthFieldOffset;

        /**
         * Length field size.
         */
        private int lengthFieldSize = Integer.BYTES;

        /**
         * Whether length includes header bytes.
         */
        private boolean lengthIncludesHeader;

        /**
         * Maximum payload length.
         */
        private int maxPayloadLength = (int) (Normal._16 * Normal.MEBI);

        /**
         * Creates a builder.
         */
        private Builder() {
            // No initialization required.
        }

        /**
         * Sets the length field offset.
         *
         * @param value offset
         * @return this builder
         */
        public Builder lengthFieldOffset(final int value) {
            this.lengthFieldOffset = validateOffset(value);
            return this;
        }

        /**
         * Sets the length field size.
         *
         * @param value size
         * @return this builder
         */
        public Builder lengthFieldSize(final int value) {
            this.lengthFieldSize = validateSize(value);
            return this;
        }

        /**
         * Sets whether length includes header bytes.
         *
         * @param value true when length includes header bytes
         * @return this builder
         */
        public Builder lengthIncludesHeader(final boolean value) {
            this.lengthIncludesHeader = value;
            return this;
        }

        /**
         * Sets the maximum payload length.
         *
         * @param value maximum payload length
         * @return this builder
         */
        public Builder maxPayloadLength(final int value) {
            this.maxPayloadLength = validateMaxPayloadLength(value);
            return this;
        }

        /**
         * Builds the codec.
         *
         * @return codec
         */
        public LengthCodec build() {
            return new LengthCodec(this);
        }

    }

}
