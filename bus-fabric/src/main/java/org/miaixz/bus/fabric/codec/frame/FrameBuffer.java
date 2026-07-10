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

import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.codec.stream.SegmentedBuffer;

/**
 * Mutable byte buffer for incremental frame decoding.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class FrameBuffer {

    /**
     * Stored bytes.
     */
    private final SegmentedBuffer buffer = SegmentedBuffer.create();

    /**
     * Creates an empty frame buffer.
     */
    public FrameBuffer() {
        // No initialization required.
    }

    /**
     * Appends input bytes.
     *
     * @param input input buffer
     */
    public void append(final ByteBuffer input) {
        if (input == null) {
            throw new ValidateException("Frame buffer input must not be null");
        }
        buffer.append(input);
        input.position(input.limit());
    }

    /**
     * Returns whether length bytes are available.
     *
     * @param length requested length
     * @return true when available
     */
    public boolean has(final int length) {
        if (length < 0) {
            throw new ValidateException("Frame buffer length must be non-negative");
        }
        return buffer.size() >= length;
    }

    /**
     * Returns one readable byte by absolute offset.
     *
     * @param index zero-based readable offset
     * @return byte
     */
    public byte get(final int index) {
        return buffer.get(index);
    }

    /**
     * Takes bytes from the buffer head.
     *
     * @param length length
     * @return readable byte buffer
     */
    public ByteBuffer take(final int length) {
        return read(length);
    }

    /**
     * Reads bytes from the buffer head and discards them.
     *
     * @param length length
     * @return readable byte buffer
     */
    public ByteBuffer read(final int length) {
        if (length < 0 || !has(length)) {
            throw new ValidateException("Frame buffer does not contain requested length");
        }
        final byte[] data = buffer.copy(0, length);
        buffer.discard(length);
        return ByteBuffer.wrap(data);
    }

    /**
     * Discards bytes from the buffer head.
     *
     * @param length length
     */
    public void discard(final int length) {
        if (length < 0 || !has(length)) {
            throw new ValidateException("Frame buffer does not contain requested length");
        }
        buffer.discard(length);
    }

    /**
     * Returns a compact snapshot of readable bytes.
     *
     * @return bytes
     */
    public byte[] toByteArray() {
        return buffer.copy(0, buffer.size());
    }

    /**
     * Returns readable size.
     *
     * @return readable size
     */
    public int size() {
        return buffer.size();
    }

    /**
     * Clears buffered bytes.
     */
    public void clear() {
        buffer.clear();
    }

}
