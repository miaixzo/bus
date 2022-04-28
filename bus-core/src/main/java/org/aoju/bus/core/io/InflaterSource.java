/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2022 aoju.org and other contributors.                      *
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
package org.aoju.bus.core.io;

import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.core.toolkit.IoKit;

import java.io.EOFException;
import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

/**
 * 解压从另一个源读取的数据
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public final class InflaterSource implements Source {

    private final BufferSource source;
    private final Inflater inflater;

    private int bufferBytesHeldByInflater;
    private boolean closed;

    public InflaterSource(Source source, Inflater inflater) {
        this(IoKit.buffer(source), inflater);
    }

    InflaterSource(BufferSource source, Inflater inflater) {
        if (null == source) {
            throw new IllegalArgumentException("source == null");
        }
        if (null == inflater) {
            throw new IllegalArgumentException("inflater == null");
        }
        this.source = source;
        this.inflater = inflater;
    }

    @Override
    public long read(
            Buffer sink, long byteCount) throws IOException {
        if (byteCount < 0) throw new IllegalArgumentException("byteCount < 0: " + byteCount);
        if (closed) throw new IllegalStateException("closed");
        if (byteCount == 0) return 0;

        while (true) {
            boolean sourceExhausted = refill();

            try {
                Segment tail = sink.writableSegment(1);
                int toRead = (int) Math.min(byteCount, Segment.SIZE - tail.limit);
                int bytesInflated = inflater.inflate(tail.data, tail.limit, toRead);
                if (bytesInflated > 0) {
                    tail.limit += bytesInflated;
                    sink.size += bytesInflated;
                    return bytesInflated;
                }
                if (inflater.finished() || inflater.needsDictionary()) {
                    releaseInflatedBytes();
                    if (tail.pos == tail.limit) {
                        sink.head = tail.pop();
                        LifeCycle.recycle(tail);
                    }
                    return -1;
                }
                if (sourceExhausted) throw new EOFException("source exhausted prematurely");
            } catch (DataFormatException e) {
                throw new IOException(e);
            }
        }
    }

    public final boolean refill() throws IOException {
        if (!inflater.needsInput()) return false;

        releaseInflatedBytes();
        if (inflater.getRemaining() != 0) throw new IllegalStateException(Symbol.QUESTION_MARK);

        if (source.exhausted()) {
            return true;
        }

        Segment head = source.buffer().head;
        bufferBytesHeldByInflater = head.limit - head.pos;
        inflater.setInput(head.data, head.pos, bufferBytesHeldByInflater);
        return false;
    }

    private void releaseInflatedBytes() throws IOException {
        if (bufferBytesHeldByInflater == 0) return;
        int toRelease = bufferBytesHeldByInflater - inflater.getRemaining();
        bufferBytesHeldByInflater -= toRelease;
        source.skip(toRelease);
    }

    @Override
    public Timeout timeout() {
        return source.timeout();
    }

    @Override
    public void close() throws IOException {
        if (closed) return;
        inflater.end();
        closed = true;
        source.close();
    }

}
