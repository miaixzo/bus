/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org sandao and other contributors.             ~
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
