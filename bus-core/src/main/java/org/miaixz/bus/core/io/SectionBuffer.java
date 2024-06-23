/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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
 * 缓冲区的一段
 * 缓冲区中的每个段都是一个循环链表节点,它引用以下内容和
 * 缓冲区中前面的段
 * 池中的每个段都是一个单链列表节点,引用池
 * 段的底层字节数组可以在缓冲区和字节字符串之间共享 当一个
 * 段不能回收,也不能改变它的字节数据
 * 唯一的例外是允许所有者段附加到段中,写入数据
 * {@code limit}及以上 每个字节数组都有一个单独的拥有段 的立场,
 * 限制、prev和next引用不共享
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SectionBuffer {

    /**
     * 所有段的大小(以字节为单位)
     */
    public static final int SIZE = 8192;

    /**
     * 这样做避免了这么多字节的{@code arraycopy()}时，将被共享
     */
    public static final int SHARE_MINIMUM = Normal._1024;

    public final byte[] data;

    /**
     * 此段中要读取的应用程序数据字节的下一个字节.
     */
    public int pos;

    /**
     * 准备写入的可用数据的第一个字节.
     */
    public int limit;

    /**
     * 如果其他段或字节字符串使用相同的字节数组，则为真.
     */
    public boolean shared;

    /**
     * 如果这个段拥有字节数组并可以向其追加，则为True，扩展{@code limit}.
     */
    public boolean owner;

    /**
     * 链表或循环链表中的下一段.
     */
    public SectionBuffer next;

    /**
     * 循环链表中的前一段.
     */
    public SectionBuffer prev;

    public SectionBuffer() {
        this.data = new byte[SIZE];
        this.owner = true;
        this.shared = false;
    }

    public SectionBuffer(byte[] data, int pos, int limit, boolean shared, boolean owner) {
        this.data = data;
        this.pos = pos;
        this.limit = limit;
        this.shared = shared;
        this.owner = owner;
    }

    /**
     * Returns a new segment that shares the underlying byte array with this. Adjusting pos and limit
     * are safe but writes are forbidden. This also marks the current segment as shared, which
     * prevents it from being pooled.
     */
    public final SectionBuffer sharedCopy() {
        shared = true;
        return new SectionBuffer(data, pos, limit, true, false);
    }

    /**
     * Returns a new segment that its own private copy of the underlying byte array.
     */
    public final SectionBuffer unsharedCopy() {
        return new SectionBuffer(data.clone(), pos, limit, false, true);
    }

    /**
     * Removes this segment of a circularly-linked list and returns its successor.
     * Returns null if the list is now empty.
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
     * Appends {@code segment} after this segment in the circularly-linked list.
     * Returns the pushed segment.
     */
    public final SectionBuffer push(SectionBuffer segment) {
        segment.prev = this;
        segment.next = next;
        next.prev = segment;
        next = segment;
        return segment;
    }

    /**
     * Splits this head of a circularly-linked list into two segments. The first
     * segment contains the data in {@code [pos..pos+byteCount)}. The second
     * segment contains the data in {@code [pos+byteCount..limit)}. This can be
     * useful when moving partial segments from one buffer to another.
     *
     * Returns the new head of the circularly-linked list.
     */
    public final SectionBuffer split(int byteCount) {
        if (byteCount <= 0 || byteCount > limit - pos) throw new IllegalArgumentException();
        SectionBuffer prefix;

        // We have two competing performance goals:
        //  - Avoid copying data. We accomplish this by sharing segments.
        //  - Avoid short shared segments. These are bad for performance because they are readonly and
        //    may lead to long chains of short segments.
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
     */
    public final void writeTo(SectionBuffer sink, int byteCount) {
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
