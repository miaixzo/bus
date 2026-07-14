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

import org.miaixz.bus.core.io.ByteString;
import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Delimiter-based line frame codec.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class LineCodec implements FrameCodec {

    /**
     * Frame delimiter.
     */
    private final ByteString delimiter;

    /**
     * Decoder buffer.
     */
    private final Buffer buffer = new Buffer();

    /**
     * Creates a line codec.
     *
     * @param delimiter delimiter
     */
    private LineCodec(final ByteString delimiter) {
        this.delimiter = validateDelimiter(delimiter);
    }

    /**
     * Creates a default LF codec.
     *
     * @return codec
     */
    public static LineCodec create() {
        return new LineCodec(ByteString.of((byte) Symbol.C_LF));
    }

    /**
     * Creates a codec with a custom delimiter.
     *
     * @param delimiter delimiter
     * @return codec
     */
    public static LineCodec of(final byte[] delimiter) {
        return new LineCodec(delimiter == null ? null : ByteString.of(delimiter));
    }

    /**
     * Creates a codec with a custom delimiter.
     *
     * @param delimiter delimiter
     * @return codec
     */
    public static LineCodec of(final ByteString delimiter) {
        return new LineCodec(delimiter);
    }

    /**
     * Decodes delimiter-separated frames.
     *
     * @param input input bytes
     * @return frames
     */
    @Override
    public List<Frame> decode(final Buffer input) {
        validateInput(input);
        buffer.write(input, input.size());
        final ArrayList<Frame> frames = new ArrayList<>();
        while (buffer.size() > 0) {
            final int index = indexOf(buffer, delimiter);
            if (index < 0) {
                if (buffer.size() > Normal._16 * Normal.MEBI) {
                    throw new ProtocolException("Line frame exceeds maximum length");
                }
                break;
            }
            if (index > Normal._16 * Normal.MEBI) {
                throw new ProtocolException("Line frame exceeds maximum length");
            }
            try {
                frames.add(Frame.of(buffer.readByteString(index)));
                buffer.skip(delimiter.size());
            } catch (final java.io.EOFException e) {
                throw new InternalException("Unable to read line frame", e);
            }
        }
        return List.copyOf(frames);
    }

    /**
     * Encodes a line frame.
     *
     * @param frame  frame
     * @param output encoded byte destination
     */
    @Override
    public void encode(final Frame frame, final Buffer output) {
        final Frame checkedFrame = Assert.notNull(frame, () -> new ValidateException("Frame must not be null"));
        final Buffer checkedOutput = Assert
                .notNull(output, () -> new ValidateException("Frame output must not be null"));
        checkedOutput.write(checkedFrame.payload());
        checkedOutput.write(delimiter);
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
     * @param delimiter delimiter
     * @return delimiter copy
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
     * @param input input buffer
     */
    private static void validateInput(final Buffer input) {
        Assert.isTrue(
                Assert.notNull(input, () -> new ValidateException("Frame input must not be empty")).size() > 0,
                () -> new ValidateException("Frame input must not be empty"));
    }

    /**
     * Finds delimiter in buffered data.
     *
     * @param buffer    buffer
     * @param delimiter delimiter
     * @return index or -1
     */
    private static int indexOf(final Buffer buffer, final ByteString delimiter) {
        try {
            final long index = buffer.indexOf(delimiter);
            if (index > Integer.MAX_VALUE) {
                throw new ProtocolException("Line frame delimiter index exceeds integer range");
            }
            return (int) index;
        } catch (final java.io.IOException e) {
            throw new InternalException("Unable to search line frame delimiter", e);
        }
    }

}
