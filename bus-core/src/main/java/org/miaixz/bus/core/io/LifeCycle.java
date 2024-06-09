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
 * 这是避免GC搅动和零填充所必需的
 * 这个池是一个线程安全的静态单例
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public final class LifeCycle {

    /**
     * 池的最大字节数 64 KiB
     */
    public static final long MAX_SIZE = Normal._64 * Normal._1024;

    /**
     * 段的单链表
     */
    public static SectionBuffer next;

    /**
     * 该池中的总字节数
     */
    public static long byteCount;

    public LifeCycle() {

    }

    public static SectionBuffer take() {
        synchronized (LifeCycle.class) {
            if (null != next) {
                SectionBuffer result = next;
                next = result.next;
                result.next = null;
                byteCount -= SectionBuffer.SIZE;
                return result;
            }
        }
        return new SectionBuffer(); // Pool is empty. Don't zero-fill while holding a lock.
    }

    public static void recycle(SectionBuffer segment) {
        if (segment.next != null || segment.prev != null) throw new IllegalArgumentException();
        if (segment.shared) return; // This segment cannot be recycled.
        synchronized (LifeCycle.class) {
            if (byteCount + SectionBuffer.SIZE > MAX_SIZE) return; // Pool is full.
            byteCount += SectionBuffer.SIZE;
            segment.next = next;
            segment.pos = segment.limit = 0;
            next = segment;
        }
    }

}
