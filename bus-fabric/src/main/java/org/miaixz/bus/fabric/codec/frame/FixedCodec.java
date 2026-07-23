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
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.Builder;

/**
 * Fixed-length frame codec.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class FixedCodec implements FrameCodec {

    /**
     * Required payload size for every encoded and decoded frame.
     */
    private final int length;

    /**
     * Accumulator retaining bytes that do not yet form a complete frame.
     */
    private final Buffer buffer = new Buffer();

    /**
     * Creates a fixed-length codec.
     *
     * @param length validated frame size from 1 byte through 16 MiB
     */
    private FixedCodec(final int length) {
        this.length = validateLength(length);
    }

    /**
     * Creates a fixed-length codec.
     *
     * @param length frame size from 1 byte through 16 MiB
     * @return new stateful fixed-length codec
     */
    public static FixedCodec of(final int length) {
        return new FixedCodec(length);
    }

    /**
     * Decodes fixed-length frames.
     *
     * @param input non-null, non-empty buffer whose bytes are consumed into the decoder accumulator
     * @return immutable list of all complete fixed-length frames currently available
     */
    @Override
    public List<Frame> decode(final Buffer input) {
        validateInput(input);
        buffer.write(input, input.size());
        final ArrayList<Frame> frames = new ArrayList<>();
        while (buffer.request(length)) {
            try {
                frames.add(Frame.of(buffer.readByteString(length)));
            } catch (final java.io.EOFException e) {
                throw new InternalException("Unable to read fixed frame", e);
            }
        }
        return List.copyOf(frames);
    }

    /**
     * Encodes a fixed-length frame.
     *
     * @param frame  non-null frame whose payload length must equal the configured fixed length
     * @param output non-null destination receiving the frame payload without additional metadata
     */
    @Override
    public void encode(final Frame frame, final Buffer output) {
        final Frame checkedFrame = Assert.notNull(frame, () -> new ValidateException("Frame must not be null"));
        final Buffer checkedOutput = Assert
                .notNull(output, () -> new ValidateException("Frame output must not be null"));
        Assert.isTrue(
                checkedFrame.length() == length,
                () -> new ProtocolException("Frame length does not match fixed length"));
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
     * Validates frame length.
     *
     * @param length candidate fixed frame length
     * @return unchanged length from 1 byte through 16 MiB
     */
    private static int validateLength(final int length) {
        Assert.isTrue(
                length > 0 && length <= Builder.BYTES_16_MIB,
                () -> new ValidateException("Frame length must be between 1 and 16777216"));
        return length;
    }

    /**
     * Validates input buffer.
     *
     * @param input buffer required to be non-null and contain at least one byte
     */
    private static void validateInput(final Buffer input) {
        Assert.isTrue(
                Assert.notNull(input, () -> new ValidateException("Frame input must not be empty")).size() > 0,
                () -> new ValidateException("Frame input must not be empty"));
    }

}
