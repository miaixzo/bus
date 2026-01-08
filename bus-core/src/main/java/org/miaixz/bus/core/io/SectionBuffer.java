/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.core.io;

import org.miaixz.bus.core.lang.Normal;

/**
 * A segment of a buffer. Each segment in a buffer is a circularly-linked list node that references the following and
 * preceding segments in the buffer. Each segment in the pool is a singly-linked list node that references the pool's
 * next segment.
 *
 * <p>
 * The underlying byte array of a segment can be shared between a buffer and a byte string. When a segment cannot be
 * recycled, its byte data cannot be changed. The only exception is that an owner segment is allowed to append to
 * itself, writing data at or beyond {@code limit}.
 *
 * <p>
 * Each byte array has a single owning segment. The pos, limit, prev, and next references are not shared.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SectionBuffer {

    /**
     * The size of all segments in bytes.
     */
    public static final int SIZE = 8192;

    /**
     * This avoids {@code arraycopy()} for this many bytes when they will be shared.
     */
    public static final int SHARE_MINIMUM = Normal._1024;

    /**
     * The underlying byte array for this segment.
     */
    public final byte[] data;

    /**
     * The next byte of application data to read in this segment.
     */
    public int pos;

    /**
     * The first byte of available data to write.
     */
    public int limit;

    /**
     * True if other segments or byte strings share the same byte array.
     */
    public boolean shared;

    /**
     * True if this segment owns the byte array and can append to it, extending {@code limit}.
     */
    public boolean owner;

    /**
     * The next segment in the circularly-linked list.
     */
    public SectionBuffer next;

    /**
     * The previous segment in the circularly-linked list.
     */
    public SectionBuffer prev;

    /**
     * Constructs a new, unshared SectionBuffer with a default size.
     */
    public SectionBuffer() {
        this.data = new byte[SIZE];
        this.owner = true;
        this.shared = false;
    }

    /**
     * Constructs a new SectionBuffer with the given data and properties.
     *
     * @param data   The byte array for this segment.
     * @param pos    The next byte of application data to read in this segment.
     * @param limit  The first byte of available data to write.
     * @param shared True if other segments or byte strings share the same byte array.
     * @param owner  True if this segment owns the byte array and can append to it.
     */
    public SectionBuffer(byte[] data, int pos, int limit, boolean shared, boolean owner) {
        this.data = data;
        this.pos = pos;
        this.limit = limit;
        this.shared = shared;
        this.owner = owner;
    }

    /**
     * Returns a new segment that shares the underlying byte array with this. Adjusting pos and limit are safe but
     * writes are forbidden. This also marks the current segment as shared, which prevents it from being pooled.
     *
     * @return A new shared {@link SectionBuffer} instance.
     */
    public final SectionBuffer sharedCopy() {
        shared = true;
        return new SectionBuffer(data, pos, limit, true, false);
    }

    /**
     * Returns a new segment that has its own private copy of the underlying byte array.
     *
     * @return A new unshared {@link SectionBuffer} instance with a copy of the data.
     */
    public final SectionBuffer unsharedCopy() {
        return new SectionBuffer(data.clone(), pos, limit, false, true);
    }

    /**
     * Removes this segment from a circularly-linked list and returns its successor. Returns null if the list is now
     * empty.
     *
     * @return The successor segment, or null if the list becomes empty.
     */
    public final SectionBuffer pop() {
        SectionBuffer result = next != this ? next : null;
        prev.next = next;
        next.prev = prev;
        next = null;
        prev = null;
        return result;
    }

    /**
     * Appends {@code segment} after this segment in the circularly-linked list. Returns the pushed segment.
     *
     * @param segment The segment to push.
     * @return The pushed segment.
     */
    public final SectionBuffer push(SectionBuffer segment) {
        segment.prev = this;
        segment.next = next;
        next.prev = segment;
        next = segment;
        return segment;
    }

    /**
     * Splits this head of a circularly-linked list into two segments. The first segment contains the data in
     * {@code [pos..pos+byteCount)}. The second segment contains the data in {@code [pos+byteCount..limit)}. This can be
     * useful when moving partial segments from one buffer to another.
     *
     * <p>
     * Returns the new head of the circularly-linked list.
     *
     * @param byteCount The number of bytes to include in the first segment.
     * @return The new head of the circularly-linked list.
     * @throws IllegalArgumentException if {@code byteCount} is negative or exceeds the available data.
     */
    public final SectionBuffer split(int byteCount) {
        if (byteCount <= 0 || byteCount > limit - pos)
            throw new IllegalArgumentException();
        SectionBuffer prefix;

        // We have two competing performance goals:
        // - Avoid copying data. We accomplish this by sharing segments.
        // - Avoid short shared segments. These are bad for performance because they are readonly and
        // may lead to long chains of short segments.
        // To balance these goals we only share segments when the copy will be large.
        if (byteCount >= SHARE_MINIMUM) {
            prefix = sharedCopy();
        } else {
            prefix = LifeCycle.take();
            System.arraycopy(data, pos, prefix.data, 0, byteCount);
        }

        prefix.limit = prefix.pos + byteCount;
        pos += byteCount;
        prev.push(prefix);
        return prefix;
    }

    /**
     * Compacts this segment by moving its data to the previous segment if possible. This operation is only allowed if
     * the previous segment is owned and has enough space. If successful, this segment is recycled.
     *
     * @throws IllegalStateException if this segment is the only segment in the list (i.e., {@code prev == this}).
     */
    public void compact() {
        if (prev == this) {
            throw new IllegalStateException();
        }
        if (!prev.owner) {
            return;
        }
        int byteCount = limit - pos;
        int availableByteCount = SIZE - prev.limit + (prev.shared ? 0 : prev.pos);
        if (byteCount > availableByteCount) {
            return;
        }
        writeTo(prev, byteCount);
        pop();
        LifeCycle.recycle(this);
    }

    /**
     * Moves {@code byteCount} bytes from this segment to {@code sink}.
     *
     * @param sink      The destination segment to write to.
     * @param byteCount The number of bytes to move.
     * @throws IllegalArgumentException if the sink is not an owner, or if the data cannot fit.
     */
    public final void writeTo(SectionBuffer sink, int byteCount) {
        if (!sink.owner)
            throw new IllegalArgumentException();
        if (sink.limit + byteCount > SIZE) {
            // We can't fit byteCount bytes at the sink's current position. Shift sink first.
            if (sink.shared)
                throw new IllegalArgumentException();
            if (sink.limit + byteCount - sink.pos > SIZE)
                throw new IllegalArgumentException();
            System.arraycopy(sink.data, sink.pos, sink.data, 0, sink.limit - sink.pos);
            sink.limit -= sink.pos;
            sink.pos = 0;
        }

        System.arraycopy(data, pos, sink.data, sink.limit, byteCount);
        sink.limit += byteCount;
        pos += byteCount;
    }

}
