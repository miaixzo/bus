/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.core.io.source;

import org.miaixz.bus.core.io.SectionBuffer;
import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.io.timout.Timeout;

import java.io.IOException;

/**
 * 一个{@link Source},它可以窥视上游的{@link BufferSource}并允许读取和
 * 展开缓冲数据而不使用它 这是通过请求额外的数据吗
 * 如果需要,则复制上游源文件,如果需要,则从上游源文件的内部缓冲区复制
 * 此源还维护其上游缓冲区的起始位置的快照
 * 每次读取时验证 如果从上游缓冲区读取,则此源将变为
 * 无效,在以后的读取中抛出{@link IllegalStateException}
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class PeekSource implements Source {

    private final BufferSource upstream;
    private final Buffer buffer;

    private SectionBuffer expectedSegment;
    private int expectedPos;
    private boolean closed;
    private long pos;

    public PeekSource(BufferSource upstream) {
        this.upstream = upstream;
        this.buffer = upstream.getBuffer();
        this.expectedSegment = buffer.head;
        this.expectedPos = expectedSegment != null ? expectedSegment.pos : -1;
    }

    @Override
    public long read(Buffer sink, long byteCount) throws IOException {
        if (byteCount < 0) throw new IllegalArgumentException("byteCount < 0: " + byteCount);
        if (closed) throw new IllegalStateException("closed");

        // Source becomes invalid if there is an expected SectionBuffer and it and the expected position
        // do not match the current head and head position of the upstream buffer
        if (expectedSegment != null
                && (expectedSegment != buffer.head || expectedPos != buffer.head.pos)) {
            throw new IllegalStateException("Peek source is invalid because upstream source was used");
        }
        if (byteCount == 0L) return 0L;
        if (!upstream.request(pos + 1)) return -1L;

        if (expectedSegment == null && buffer.head != null) {
            // Only once the buffer actually holds data should an expected SectionBuffer and position be
            // recorded. This allows reads from the peek source to repeatedly return -1 and for data to be
            // added later. Unit tests depend on this behavior.
            expectedSegment = buffer.head;
            expectedPos = buffer.head.pos;
        }

        long toCopy = Math.min(byteCount, buffer.size - pos);
        buffer.copyTo(sink, pos, toCopy);
        pos += toCopy;
        return toCopy;
    }

    @Override
    public Timeout timeout() {
        return upstream.timeout();
    }

    @Override
    public void close() throws IOException {
        closed = true;
    }

}
