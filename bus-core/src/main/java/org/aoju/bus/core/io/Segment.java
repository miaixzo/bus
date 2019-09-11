/*
 * The MIT License
 *
 * Copyright (c) 2017, aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.core.io;

/**
 * 缓冲区的一段
 * 缓冲区中的每个段都是一个循环链表节点，它引用以下内容和
 * 缓冲区中前面的段。
 * 池中的每个段都是一个单链列表节点，引用池。
 * 段的底层字节数组可以在缓冲区和字节字符串之间共享。当一个
 * 段不能回收，也不能改变它的字节数据。
 * 唯一的例外是允许所有者段附加到段中，写入数据
 * {@code limit}及以上。每个字节数组都有一个单独的拥有段。的立场,
 * 限制、prev和next引用不共享。
 *
 * @author Kimi Liu
 * @version 3.2.6
 * @since JDK 1.8
 */
public final class Segment {

    /**
     * The size of all segments in bytes.
     */
    public static final int SIZE = 8192;

    /**
     * Segments will be shared when doing so avoids {@code arraycopy()} of this many bytes.
     */
    public static final int SHARE_MINIMUM = 1024;

    public final byte[] data;

    /**
     * The next byte of application data byte to read in this segment.
     */
    public int pos;

    /**
     * The first byte of available data ready to be written to.
     */
    public int limit;

    /**
     * True if other segments or byte strings use the same byte array.
     */
    public boolean shared;

    /**
     * True if this segment owns the byte array and can append to it, extending {@code limit}.
     */
    public boolean owner;

    /**
     * Next segment in a linked or circularly-linked list.
     */
    public Segment next;

    /**
     * Previous segment in a circularly-linked list.
     */
    public Segment prev;

    Segment() {
        this.data = new byte[SIZE];
        this.owner = true;
        this.shared = false;
    }

    Segment(byte[] data, int pos, int limit, boolean shared, boolean owner) {
        this.data = data;
        this.pos = pos;
        this.limit = limit;
        this.shared = shared;
        this.owner = owner;
    }

    final Segment sharedCopy() {
        shared = true;
        return new Segment(data, pos, limit, true, false);
    }

    final Segment unsharedCopy() {
        return new Segment(data.clone(), pos, limit, false, true);
    }

    public final Segment pop() {
        Segment result = next != this ? next : null;
        prev.next = next;
        next.prev = prev;
        next = null;
        prev = null;
        return result;
    }

    public final Segment push(Segment segment) {
        segment.prev = this;
        segment.next = next;
        next.prev = segment;
        next = segment;
        return segment;
    }

    public final Segment split(int byteCount) {
        if (byteCount <= 0 || byteCount > limit - pos) throw new IllegalArgumentException();
        Segment prefix;

        // We have two competing performance goals:
        //  - Avoid copying data. We accomplish this by sharing segments.
        //  - Avoid short shared segments. These are bad for performance because they are readonly and
        //    may lead to long chains of short segments.
        // To balance these goals we only share segments when the copy will be large.
        if (byteCount >= SHARE_MINIMUM) {
            prefix = sharedCopy();
        } else {
            prefix = SegmentPool.take();
            System.arraycopy(data, pos, prefix.data, 0, byteCount);
        }

        prefix.limit = prefix.pos + byteCount;
        pos += byteCount;
        prev.push(prefix);
        return prefix;
    }

    public final void compact() {
        if (prev == this) throw new IllegalStateException();
        if (!prev.owner) return; // Cannot compact: prev isn't writable.
        int byteCount = limit - pos;
        int availableByteCount = SIZE - prev.limit + (prev.shared ? 0 : prev.pos);
        if (byteCount > availableByteCount) return; // Cannot compact: not enough writable space.
        writeTo(prev, byteCount);
        pop();
        SegmentPool.recycle(this);
    }

    public final void writeTo(Segment sink, int byteCount) {
        if (!sink.owner) throw new IllegalArgumentException();
        if (sink.limit + byteCount > SIZE) {
            // We can't fit byteCount bytes at the sink's current position. Shift sink first.
            if (sink.shared) throw new IllegalArgumentException();
            if (sink.limit + byteCount - sink.pos > SIZE) throw new IllegalArgumentException();
            System.arraycopy(sink.data, sink.pos, sink.data, 0, sink.limit - sink.pos);
            sink.limit -= sink.pos;
            sink.pos = 0;
        }

        System.arraycopy(data, pos, sink.data, sink.limit, byteCount);
        sink.limit += byteCount;
        pos += byteCount;
    }

}
