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
import java.util.ArrayList;
import java.util.List;

/**
 * A decoder that frames messages based on a specified delimiter.
 * <p>
 * This decoder reads bytes from a {@link ByteBuffer} until a predefined delimiter byte sequence is found. It supports
 * handling fragmented messages across multiple buffer reads.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class DelimiterFrameDecoder implements SocketDecoder {

    /**
     * The position to reposition the buffer if a partial delimiter is found.
     */
    private final int reposition;
    /**
     * A list of ByteBuffers storing the data that has been parsed so far.
     */
    private final List<ByteBuffer> bufferList;
    /**
     * The byte sequence marking the end of a message.
     */
    private byte[] endFLag;
    /**
     * The expected index within the {@code endFLag} for the current byte being checked.
     */
    private int exceptIndex;
    /**
     * A flag indicating whether the current message has been fully read.
     */
    private boolean finishRead;
    /**
     * The current position in the {@code bufferList}.
     */
    private int position;

    /**
     * Constructs a {@code DelimiterFrameDecoder} with the specified end flag and unit buffer size.
     *
     * @param endFLag        the byte array representing the message delimiter
     * @param unitBufferSize the size of each internal buffer unit
     * @throws IllegalArgumentException if {@code endFLag} is empty or {@code unitBufferSize} is less than 1
     */
    public DelimiterFrameDecoder(byte[] endFLag, int unitBufferSize) {
        if (endFLag == null || endFLag.length == 0) {
            throw new IllegalArgumentException("endFLag cannot be empty");
        }
        if (unitBufferSize < 1) {
            throw new IllegalArgumentException("unitBufferSize Must be greater than 1");
        }
        this.endFLag = endFLag;
        int p = 0;
        for (int i = 1; i < endFLag.length; i++) {
            if (endFLag[i] != endFLag[0]) {
                p = i - 1;
                break;
            }
        }
        reposition = p;
        bufferList = new ArrayList<>();
        bufferList.add(ByteBuffer.allocate(unitBufferSize));
    }

    /**
     * Decodes bytes from the provided {@link ByteBuffer} to find the message delimiter.
     *
     * @param byteBuffer the buffer containing the data to decode
     * @return {@code true} if a complete frame has been decoded, {@code false} otherwise
     * @throws RuntimeException if the decoder has already finished reading a delimiter
     */
    public boolean decode(ByteBuffer byteBuffer) {
        if (finishRead) {
            throw new RuntimeException("delimiter has finish read");
        }
        ByteBuffer preBuffer = bufferList.get(position);

        while (byteBuffer.hasRemaining()) {
            if (!preBuffer.hasRemaining()) {
                preBuffer.flip();
                position++;
                if (position < bufferList.size()) {
                    preBuffer = bufferList.get(position);
                    preBuffer.clear();
                } else {
                    preBuffer = ByteBuffer.allocate(preBuffer.capacity());
                    bufferList.add(preBuffer);
                }
            }
            byte data = byteBuffer.get();
            preBuffer.put(data);
            if (data != endFLag[exceptIndex]) {
                if (exceptIndex != reposition + 1 || data != endFLag[reposition]) {
                    exceptIndex = endFLag[0] == data ? 1 : 0;
                }
            } else if (++exceptIndex == endFLag.length) {
                preBuffer.flip();
                finishRead = true;
                break;
            }
        }

        return finishRead;
    }

    @Override
    public ByteBuffer getBuffer() {
        if (position == 0) {
            return bufferList.get(position);
        }
        int totalLength = 0;
        for (int i = 0; i <= position; i++) {
            totalLength += bufferList.get(i).limit();
        }
        byte[] data = new byte[totalLength];
        int index = 0;
        for (int i = 0; i <= position; i++) {
            ByteBuffer b = bufferList.get(i);
            System.arraycopy(b.array(), b.position(), data, index, b.remaining());
            index += b.remaining();
        }
        return ByteBuffer.wrap(data);
    }

    /**
     * Resets the decoder to its initial state.
     */
    public void reset() {
        reset(null);
    }

    /**
     * Resets the decoder to its initial state and optionally updates the delimiter.
     *
     * @param endFLag the new delimiter to use, or {@code null} to keep the existing one
     */
    public void reset(byte[] endFLag) {
        if (endFLag != null) {
            this.endFLag = endFLag;
        }
        finishRead = false;
        exceptIndex = 0;
        position = 0;
        bufferList.get(position).clear();
    }

}
