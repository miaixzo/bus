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

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Fixed-length frame codec.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class LengthCodec implements FrameCodec {

    /**
     * Maximum supported frame length.
     */
    private static final int MAX_LENGTH = 16_777_216;

    /**
     * Fixed frame length.
     */
    private final int length;

    /**
     * Decoder buffer.
     */
    private final FrameBuffer buffer = new FrameBuffer();

    /**
     * Creates a fixed-length codec.
     *
     * @param length frame length
     */
    private LengthCodec(final int length) {
        this.length = validateLength(length);
    }

    /**
     * Creates a fixed-length codec.
     *
     * @param length frame length
     * @return codec
     */
    public static LengthCodec of(final int length) {
        return new LengthCodec(length);
    }

    /**
     * Decodes fixed-length frames.
     *
     * @param input input bytes
     * @return frames
     */
    @Override
    public List<Frame> decode(final ByteBuffer input) {
        validateInput(input);
        buffer.append(input.duplicate());
        final ArrayList<Frame> frames = new ArrayList<>();
        while (buffer.has(length)) {
            frames.add(Frame.of(buffer.take(length)));
        }
        return List.copyOf(frames);
    }

    /**
     * Encodes a fixed-length frame.
     *
     * @param frame frame
     * @return encoded bytes
     */
    @Override
    public ByteBuffer encode(final Frame frame) {
        if (frame == null) {
            throw new ValidateException("Frame must not be null");
        }
        if (frame.length() != length) {
            throw new ProtocolException("Frame length does not match fixed length");
        }
        final ByteBuffer payload = frame.payload();
        final ByteBuffer encoded = ByteBuffer.allocate(length);
        encoded.put(payload);
        encoded.flip();
        return encoded;
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
     * @param length frame length
     * @return validated length
     */
    private static int validateLength(final int length) {
        if (length <= 0 || length > MAX_LENGTH) {
            throw new ValidateException("Frame length must be between 1 and 16777216");
        }
        return length;
    }

    /**
     * Validates input buffer.
     *
     * @param input input buffer
     */
    private static void validateInput(final ByteBuffer input) {
        if (input == null || !input.hasRemaining()) {
            throw new ValidateException("Frame input must not be empty");
        }
    }

}
