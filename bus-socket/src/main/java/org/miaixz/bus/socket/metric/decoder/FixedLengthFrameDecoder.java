/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org sandao and other contributors.         ~
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
package org.miaixz.bus.socket.metric.decoder;

import java.nio.ByteBuffer;

/**
 * A decoder that frames messages based on a fixed length.
 * <p>
 * This decoder reads bytes from a {@link ByteBuffer} until a predefined fixed length is reached. It is suitable for
 * protocols where message lengths are known in advance.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class FixedLengthFrameDecoder implements SocketDecoder {

    /**
     * The internal buffer to accumulate bytes until the fixed length is reached.
     */
    private ByteBuffer buffer;
    /**
     * A flag indicating whether the fixed-length frame has been fully read.
     */
    private boolean finishRead;

    /**
     * Constructs a {@code FixedLengthFrameDecoder} with the specified frame length.
     *
     * @param frameLength the fixed length of each frame
     * @throws IllegalArgumentException if {@code frameLength} is not a positive integer
     */
    public FixedLengthFrameDecoder(int frameLength) {
        if (frameLength <= 0) {
            throw new IllegalArgumentException("frameLength must be a positive integer: " + frameLength);
        } else {
            buffer = ByteBuffer.allocate(frameLength);
        }
    }

    /**
     * Decodes bytes from the provided {@link ByteBuffer} to fill the fixed-length frame.
     *
     * @param byteBuffer the buffer containing the data to decode
     * @return {@code true} if the fixed-length frame has been fully decoded, {@code false} otherwise
     * @throws RuntimeException if the decoder has already finished reading a frame
     */
    public boolean decode(ByteBuffer byteBuffer) {
        if (finishRead) {
            throw new RuntimeException("delimiter has finish read");
        }
        if (buffer.remaining() >= byteBuffer.remaining()) {
            buffer.put(byteBuffer);
        } else {
            int limit = byteBuffer.limit();
            byteBuffer.limit(byteBuffer.position() + buffer.remaining());
            buffer.put(byteBuffer);
            byteBuffer.limit(limit);
        }

        if (buffer.hasRemaining()) {
            return false;
        }
        buffer.flip();
        finishRead = true;
        return true;
    }

    /**
     * Returns the internal buffer containing the decoded fixed-length frame.
     *
     * @return the {@link ByteBuffer} with the decoded frame
     */
    public ByteBuffer getBuffer() {
        return buffer;
    }

}
