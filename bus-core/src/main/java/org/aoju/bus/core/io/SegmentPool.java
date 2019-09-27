/*
 * The MIT License
 *
 * Copyright (c) 2017 aoju.org All rights reserved.
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
 * 这是避免GC搅动和零填充所必需的。
 * 这个池是一个线程安全的静态单例。
 *
 * @author Kimi Liu
 * @version 3.6.0
 * @since JDK 1.8
 */
public final class SegmentPool {

    /**
     * The maximum number of bytes to pool.
     */
    static final long MAX_SIZE = 64 * 1024; // 64 KiB.

    /**
     * Singly-linked list of segments.
     */
    static Segment next;

    /**
     * Total bytes in this pool.
     */
    static long byteCount;

    private SegmentPool() {
    }

    static Segment take() {
        synchronized (SegmentPool.class) {
            if (next != null) {
                Segment result = next;
                next = result.next;
                result.next = null;
                byteCount -= Segment.SIZE;
                return result;
            }
        }
        return new Segment(); // Pool is empty. Don't zero-fill while holding a lock.
    }

    public static void recycle(Segment segment) {
        if (segment.next != null || segment.prev != null) throw new IllegalArgumentException();
        if (segment.shared) return; // This segment cannot be recycled.
        synchronized (SegmentPool.class) {
            if (byteCount + Segment.SIZE > MAX_SIZE) return; // Pool is full.
            byteCount += Segment.SIZE;
            segment.next = next;
            segment.pos = segment.limit = 0;
            next = segment;
        }
    }

}
