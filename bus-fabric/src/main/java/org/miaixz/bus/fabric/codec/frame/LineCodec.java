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

import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.miaixz.bus.core.io.ByteString;
import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.Builder;

/**
 * Stateful delimiter-based line frame codec that retains incomplete input between decode calls.
 *
 * @author Kimi Liu
 */
public class LineCodec implements FrameCodec {

    /**
     * Immutable delimiter snapshot appended during encoding and searched during decoding.
     */
    private final ByteString delimiter;

    /**
     * Incomplete encoded bytes retained until a delimiter arrives or the codec is reset.
     */
    private final Buffer buffer = new Buffer();

    /**
     * Creates a line codec.
     *
     * @param delimiter non-empty delimiter containing at most 1024 bytes
     */
    public LineCodec(final ByteString delimiter) {
        this.delimiter = validateDelimiter(delimiter);
    }

    /**
     * Creates a default LF codec.
     *
     * @return stateful codec using a single line-feed byte as its delimiter
     */
    public static LineCodec create() {
        return new LineCodec(ByteString.of((byte) Symbol.C_LF));
    }

    /**
     * Creates a codec with a custom delimiter.
     *
     * @param delimiter custom delimiter bytes copied by the codec
     * @return stateful codec using the supplied delimiter
     * @throws ValidateException if the delimiter is {@code null}, empty, or longer than 1024 bytes
     */
    public static LineCodec of(final byte[] delimiter) {
        return new LineCodec(delimiter == null ? null : ByteString.of(delimiter));
    }

    /**
     * Creates a codec with a custom delimiter.
     *
     * @param delimiter immutable delimiter bytes copied by the codec
     * @return stateful codec using the supplied delimiter
     * @throws ValidateException if the delimiter is {@code null}, empty, or longer than 1024 bytes
     */
    public static LineCodec of(final ByteString delimiter) {
        return new LineCodec(delimiter);
    }

    /**
     * Decodes delimiter-separated frames.
     *
     * @param input non-empty encoded bytes consumed into the codec's retained buffer
     * @return immutable list of complete frames decoded during this call
     * @throws ProtocolException if an unterminated or completed frame exceeds 16 MiB
     * @throws ValidateException if {@code input} is {@code null} or empty
     */
    @Override
    public List<Frame> decode(final Buffer input) {
        validateInput(input);
        buffer.write(input, input.size());
        final ArrayList<Frame> frames = new ArrayList<>();
        while (buffer.size() > 0) {
            final int index = indexOf(buffer, delimiter);
            if (index < 0) {
                if (buffer.size() > Builder.BYTES_16_MIB) {
                    throw new ProtocolException("Line frame exceeds maximum length");
                }
                break;
            }
            if (index > Builder.BYTES_16_MIB) {
                throw new ProtocolException("Line frame exceeds maximum length");
            }
            try {
                frames.add(Frame.of(buffer.readByteString(index)));
                buffer.skip(delimiter.size());
            } catch (final EOFException e) {
                throw new InternalException("Unable to read line frame", e);
            }
        }
        return List.copyOf(frames);
    }

    /**
     * Decodes line payload owners without creating frame wrappers.
     *
     * @param input encoded line bytes
     * @return decoded immutable payload owners
     */
    @Override
    public List<ByteString> decodeOwned(final Buffer input) {
        final ArrayList<ByteString> frames = new ArrayList<>();
        decodeOwned(input, frames);
        return List.copyOf(frames);
    }

    /**
     * Decodes line payload owners into caller-owned storage.
     *
     * @param input  encoded line bytes
     * @param output destination collection
     * @return number of decoded payload owners
     */
    @Override
    public int decodeOwned(final Buffer input, final Collection<? super ByteString> output) {
        validateInput(input);
        buffer.write(input, input.size());
        int decoded = 0;
        while (buffer.size() > 0) {
            final int index = indexOf(buffer, delimiter);
            if (index < 0) {
                if (buffer.size() > Builder.BYTES_16_MIB) {
                    throw new ProtocolException("Line frame exceeds maximum length");
                }
                break;
            }
            if (index > Builder.BYTES_16_MIB) {
                throw new ProtocolException("Line frame exceeds maximum length");
            }
            try {
                output.add(buffer.readByteString(index));
                decoded++;
                buffer.skip(delimiter.size());
            } catch (final EOFException e) {
                throw new InternalException("Unable to read line frame", e);
            }
        }
        return decoded;
    }

    /**
     * Encodes a line frame.
     *
     * @param frame  frame whose payload is written before the delimiter
     * @param output destination receiving payload and delimiter bytes
     * @throws ValidateException if the frame or output buffer is {@code null}
     */
    @Override
    public void encode(final Frame frame, final Buffer output) {
        final Frame checkedFrame = Assert.notNull(frame, () -> new ValidateException("Frame must not be null"));
        encodeOwned(checkedFrame.payload(), output);
    }

    /**
     * Encodes immutable line payload bytes directly.
     *
     * @param payload immutable payload owner
     * @param output  destination buffer
     */
    @Override
    public void encodeOwned(final ByteString payload, final Buffer output) {
        final ByteString checkedPayload = Assert
                .notNull(payload, () -> new ValidateException("Frame payload must not be null"));
        final Buffer checkedOutput = Assert
                .notNull(output, () -> new ValidateException("Frame output must not be null"));
        checkedOutput.write(checkedPayload);
        checkedOutput.write(delimiter);
    }

    /**
     * Creates an independent decoder with the same delimiter.
     *
     * @return fresh line codec
     */
    @Override
    public FrameCodec fork() {
        return new LineCodec(delimiter);
    }

    /**
     * Resets buffered bytes.
     */
    @Override
    public void reset() {
        buffer.clear();
    }

    /**
     * Validates delimiter bytes.
     *
     * @param delimiter candidate delimiter bytes
     * @return delimiter copy
     * @throws ValidateException if the delimiter is {@code null}, empty, or longer than 1024 bytes
     */
    private static ByteString validateDelimiter(final ByteString delimiter) {
        final ByteString checkedDelimiter = Assert
                .notNull(delimiter, () -> new ValidateException("Frame delimiter must contain 1 to 1024 bytes"));
        Assert.isTrue(
                checkedDelimiter.size() > 0 && checkedDelimiter.size() <= Normal._1024,
                () -> new ValidateException("Frame delimiter must contain 1 to 1024 bytes"));
        return ByteString.of(checkedDelimiter.internalArray());
    }

    /**
     * Validates input buffer.
     *
     * @param input candidate buffer for one decode call
     * @throws ValidateException if the buffer is {@code null} or empty
     */
    private static void validateInput(final Buffer input) {
        Assert.isTrue(
                Assert.notNull(input, () -> new ValidateException("Frame input must not be empty")).size() > 0,
                () -> new ValidateException("Frame input must not be empty"));
    }

    /**
     * Finds delimiter in buffered data.
     *
     * @param buffer    retained encoded data to search
     * @param delimiter validated delimiter to locate
     * @return zero-based delimiter offset, or {@code -1} when incomplete
     * @throws ProtocolException if the offset cannot be represented as an integer
     * @throws InternalException if the buffer search fails
     */
    private static int indexOf(final Buffer buffer, final ByteString delimiter) {
        try {
            final long index = delimiter.size() == 1 ? buffer.indexOf(delimiter.getByte(0)) : buffer.indexOf(delimiter);
            if (index > Integer.MAX_VALUE) {
                throw new ProtocolException("Line frame delimiter index exceeds integer range");
            }
            return (int) index;
        } catch (final IOException e) {
            throw new InternalException("Unable to search line frame delimiter", e);
        }
    }

}
