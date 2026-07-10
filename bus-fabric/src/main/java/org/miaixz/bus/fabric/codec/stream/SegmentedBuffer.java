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
package org.miaixz.bus.fabric.codec.stream;

import java.nio.ByteBuffer;
import java.util.ArrayDeque;

import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Incremental byte accumulator backed by fixed-size segments.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class SegmentedBuffer {

    /**
     * Default segment size.
     */
    private static final int DEFAULT_SEGMENT_SIZE = 8192;

    /**
     * Segments in readable order.
     */
    private final ArrayDeque<Segment> segments;

    /**
     * Segment size.
     */
    private final int segmentSize;

    /**
     * Readable byte count.
     */
    private int size;

    /**
     * Creates an empty segmented buffer.
     *
     * @param segmentSize segment size
     */
    private SegmentedBuffer(final int segmentSize) {
        if (segmentSize <= 0) {
            throw new ValidateException("Segment size must be positive");
        }
        this.segmentSize = segmentSize;
        this.segments = new ArrayDeque<>();
    }

    /**
     * Creates an empty buffer using the default segment size.
     *
     * @return buffer
     */
    public static SegmentedBuffer create() {
        return new SegmentedBuffer(DEFAULT_SEGMENT_SIZE);
    }

    /**
     * Creates an empty buffer using a custom segment size.
     *
     * @param segmentSize segment size
     * @return buffer
     */
    public static SegmentedBuffer create(final int segmentSize) {
        return new SegmentedBuffer(segmentSize);
    }

    /**
     * Appends all remaining bytes from the source buffer.
     *
     * @param source source buffer
     */
    public void append(final ByteBuffer source) {
        if (source == null) {
            throw new ValidateException("Segmented buffer source must not be null");
        }
        final ByteBuffer view = source.duplicate();
        if (view.remaining() > Integer.MAX_VALUE - size) {
            throw new InternalException("Segmented buffer size overflow");
        }
        while (view.hasRemaining()) {
            Segment tail = segments.peekLast();
            if (tail == null || tail.full()) {
                tail = new Segment(segmentSize);
                segments.addLast(tail);
            }
            final int count = Math.min(view.remaining(), tail.remaining());
            view.get(tail.bytes, tail.end, count);
            tail.end += count;
            size += count;
        }
    }

    /**
     * Returns one byte by absolute readable offset.
     *
     * @param index zero-based readable offset
     * @return byte
     */
    public byte get(final int index) {
        checkRange(index, 1);
        int skipped = 0;
        for (final Segment segment : segments) {
            final int readable = segment.readable();
            if (index < skipped + readable) {
                return segment.bytes[segment.start + index - skipped];
            }
            skipped += readable;
        }
        throw new InternalException("Segmented buffer index lookup failed");
    }

    /**
     * Copies a readable range into a compact byte array.
     *
     * @param offset range offset
     * @param length range length
     * @return copied bytes
     */
    public byte[] copy(final int offset, final int length) {
        checkRange(offset, length);
        final byte[] target = new byte[length];
        int copied = 0;
        int skipped = 0;
        for (final Segment segment : segments) {
            final int readable = segment.readable();
            final int segmentStart = skipped;
            final int segmentEnd = skipped + readable;
            if (offset < segmentEnd && copied < length) {
                final int from = Math.max(offset, segmentStart);
                final int to = Math.min(offset + length, segmentEnd);
                final int count = to - from;
                System.arraycopy(segment.bytes, segment.start + from - segmentStart, target, copied, count);
                copied += count;
            }
            skipped = segmentEnd;
            if (copied == length) {
                return target;
            }
        }
        throw new InternalException("Segmented buffer range copy failed");
    }

    /**
     * Copies a readable range into a target buffer.
     *
     * @param offset range offset
     * @param target target buffer
     * @param length range length
     * @return copied byte count
     */
    public int copyTo(final int offset, final ByteBuffer target, final int length) {
        if (target == null) {
            throw new ValidateException("Segmented buffer target must not be null");
        }
        if (length > target.remaining()) {
            throw new ValidateException("Segmented buffer target does not have enough space");
        }
        checkRange(offset, length);
        int copied = 0;
        int skipped = 0;
        for (final Segment segment : segments) {
            final int readable = segment.readable();
            final int segmentStart = skipped;
            final int segmentEnd = skipped + readable;
            if (offset < segmentEnd && copied < length) {
                final int from = Math.max(offset, segmentStart);
                final int to = Math.min(offset + length, segmentEnd);
                final int count = to - from;
                target.put(segment.bytes, segment.start + from - segmentStart, count);
                copied += count;
            }
            skipped = segmentEnd;
            if (copied == length) {
                return copied;
            }
        }
        throw new InternalException("Segmented buffer target copy failed");
    }

    /**
     * Discards bytes from the readable head.
     *
     * @param count byte count
     */
    public void discard(final int count) {
        checkRange(0, count);
        int remaining = count;
        while (remaining > 0) {
            final Segment head = segments.peekFirst();
            if (head == null) {
                throw new InternalException("Segmented buffer discard failed");
            }
            final int skipped = Math.min(remaining, head.readable());
            head.start += skipped;
            remaining -= skipped;
            size -= skipped;
            if (head.readable() == 0) {
                segments.removeFirst();
            }
        }
    }

    /**
     * Returns readable bytes.
     *
     * @return readable size
     */
    public int size() {
        return size;
    }

    /**
     * Clears all buffered bytes.
     */
    public void clear() {
        segments.clear();
        size = 0;
    }

    /**
     * Validates a readable range.
     *
     * @param offset range offset
     * @param length range length
     */
    private void checkRange(final int offset, final int length) {
        if (offset < 0 || length < 0 || offset > size || length > size - offset) {
            throw new ValidateException("Segmented buffer range is outside readable bytes");
        }
    }

    /**
     * Mutable segment.
     */
    private static final class Segment {

        /**
         * Segment bytes.
         */
        private final byte[] bytes;

        /**
         * Read cursor.
         */
        private int start;

        /**
         * Write cursor.
         */
        private int end;

        /**
         * Creates a segment.
         *
         * @param capacity capacity
         */
        private Segment(final int capacity) {
            this.bytes = new byte[capacity];
        }

        /**
         * Returns readable bytes.
         *
         * @return readable bytes
         */
        private int readable() {
            return end - start;
        }

        /**
         * Returns writable bytes.
         *
         * @return remaining bytes
         */
        private int remaining() {
            return bytes.length - end;
        }

        /**
         * Returns whether the segment is full.
         *
         * @return true when full
         */
        private boolean full() {
            return end == bytes.length;
        }

    }

}
