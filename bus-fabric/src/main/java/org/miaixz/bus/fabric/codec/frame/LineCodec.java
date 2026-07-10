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

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.xyz.ArrayKit;

/**
 * Delimiter-based line frame codec.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class LineCodec implements FrameCodec {

    /**
     * Maximum frame payload length.
     */
    private static final int MAX_FRAME = 16_777_216;

    /**
     * Maximum delimiter length.
     */
    private static final int MAX_DELIMITER = 1024;

    /**
     * Frame delimiter.
     */
    private final byte[] delimiter;

    /**
     * Decoder buffer.
     */
    private final FrameBuffer buffer = new FrameBuffer();

    /**
     * Creates a line codec.
     *
     * @param delimiter delimiter
     */
    private LineCodec(final byte[] delimiter) {
        this.delimiter = validateDelimiter(delimiter);
    }

    /**
     * Creates a default LF codec.
     *
     * @return codec
     */
    public static LineCodec create() {
        return new LineCodec(new byte[] { (byte) Symbol.C_LF });
    }

    /**
     * Creates a codec with a custom delimiter.
     *
     * @param delimiter delimiter
     * @return codec
     */
    public static LineCodec of(final byte[] delimiter) {
        return new LineCodec(delimiter);
    }

    /**
     * Decodes delimiter-separated frames.
     *
     * @param input input bytes
     * @return frames
     */
    @Override
    public List<Frame> decode(final ByteBuffer input) {
        validateInput(input);
        buffer.append(input.duplicate());
        final ArrayList<Frame> frames = new ArrayList<>();
        while (buffer.size() > 0) {
            final int index = indexOf(buffer, delimiter);
            if (index < 0) {
                if (buffer.size() > MAX_FRAME) {
                    throw new ProtocolException("Line frame exceeds maximum length");
                }
                break;
            }
            if (index > MAX_FRAME) {
                throw new ProtocolException("Line frame exceeds maximum length");
            }
            frames.add(Frame.of(buffer.read(index)));
            buffer.discard(delimiter.length);
        }
        return List.copyOf(frames);
    }

    /**
     * Encodes a line frame.
     *
     * @param frame frame
     * @return encoded bytes
     */
    @Override
    public ByteBuffer encode(final Frame frame) {
        if (frame == null) {
            throw new ValidateException("Frame must not be null");
        }
        final ByteBuffer payload = frame.payload();
        final ByteBuffer encoded = ByteBuffer.allocate(payload.remaining() + delimiter.length);
        encoded.put(payload);
        encoded.put(delimiter);
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
     * Validates delimiter bytes.
     *
     * @param delimiter delimiter
     * @return delimiter copy
     */
    private static byte[] validateDelimiter(final byte[] delimiter) {
        if (delimiter == null || delimiter.length == 0 || delimiter.length > MAX_DELIMITER) {
            throw new ValidateException("Frame delimiter must contain 1 to 1024 bytes");
        }
        return ArrayKit.clone(delimiter);
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

    /**
     * Finds delimiter in buffered data.
     *
     * @param buffer    buffer
     * @param delimiter delimiter
     * @return index or -1
     */
    private static int indexOf(final FrameBuffer buffer, final byte[] delimiter) {
        for (int i = 0; i <= buffer.size() - delimiter.length; i++) {
            boolean matched = true;
            for (int j = 0; j < delimiter.length; j++) {
                if (buffer.get(i + j) != delimiter[j]) {
                    matched = false;
                    break;
                }
            }
            if (matched) {
                return i;
            }
        }
        return -1;
    }

}
