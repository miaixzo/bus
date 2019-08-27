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

import org.aoju.bus.core.utils.IoUtils;

import java.io.EOFException;
import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

/**
 * 解压从另一个源读取的数据.
 *
 * @author Kimi Liu
 * @version 3.1.2
 * @since JDK 1.8
 */
public final class InflaterSource implements Source {

    private final BufferedSource source;
    private final Inflater inflater;

    private int bufferBytesHeldByInflater;
    private boolean closed;

    public InflaterSource(Source source, Inflater inflater) {
        this(IoUtils.buffer(source), inflater);
    }

    InflaterSource(BufferedSource source, Inflater inflater) {
        if (source == null) throw new IllegalArgumentException("source == null");
        if (inflater == null) throw new IllegalArgumentException("inflater == null");
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

            // Decompress the inflater's compressed data into the sink.
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
                        // We allocated a tail segment, but didn't end up needing it. Recycle!
                        sink.head = tail.pop();
                        SegmentPool.recycle(tail);
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
        if (inflater.getRemaining() != 0) throw new IllegalStateException("?"); // TODO: possible?

        // If there are compressed bytes in the source, assign them to the inflater.
        if (source.exhausted()) return true;

        // Assign buffer bytes to the inflater.
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
