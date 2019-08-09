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

import java.io.IOException;

/**
 * A source and a sink that are attached. The sink's output is the source's input. Typically each
 * is accessed by its own thread: a producer thread writes data to the sink and a consumer thread
 * reads data from the source.
 *
 * <p>This class uses a buffer to decouple source and sink. This buffer has a user-specified maximum
 * size. When a producer thread outruns its consumer the buffer fills up and eventually writes to
 * the sink will block until the consumer has caught up. Symmetrically, if a consumer outruns its
 * producer reads block until there is data to be read. Limits on the amount of time spent waiting
 * for the other party can be configured with {@linkplain Timeout timeouts} on the source and the
 * sink.
 *
 * <p>When the sink is closed, source reads will continue to complete normally until the buffer has
 * been exhausted. At that point reads will return -1, indicating the end of the stream. But if the
 * source is closed first, writes to the sink will immediately fail with an {@link IOException}.
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public final class Pipe {

    final long maxBufferSize;
    final Buffer buffer = new Buffer();
    private final Sink sink = new PipeSink();
    private final Source source = new PipeSource();
    boolean sinkClosed;
    boolean sourceClosed;

    public Pipe(long maxBufferSize) {
        if (maxBufferSize < 1L) {
            throw new IllegalArgumentException("maxBufferSize < 1: " + maxBufferSize);
        }
        this.maxBufferSize = maxBufferSize;
    }

    public final Source source() {
        return source;
    }

    public final Sink sink() {
        return sink;
    }

    final class PipeSink implements Sink {
        final Timeout timeout = new Timeout();

        @Override
        public void write(Buffer source, long byteCount) throws IOException {
            synchronized (buffer) {
                if (sinkClosed) throw new IllegalStateException("closed");

                while (byteCount > 0) {
                    if (sourceClosed) throw new IOException("source is closed");

                    long bufferSpaceAvailable = maxBufferSize - buffer.size();
                    if (bufferSpaceAvailable == 0) {
                        timeout.waitUntilNotified(buffer); // Wait until the source drains the buffer.
                        continue;
                    }

                    long bytesToWrite = Math.min(bufferSpaceAvailable, byteCount);
                    buffer.write(source, bytesToWrite);
                    byteCount -= bytesToWrite;
                    buffer.notifyAll(); // Notify the source that it can resume reading.
                }
            }
        }

        @Override
        public void flush() throws IOException {
            synchronized (buffer) {
                if (sinkClosed) throw new IllegalStateException("closed");
                if (sourceClosed && buffer.size() > 0) throw new IOException("source is closed");
            }
        }

        @Override
        public void close() throws IOException {
            synchronized (buffer) {
                if (sinkClosed) return;
                if (sourceClosed && buffer.size() > 0) throw new IOException("source is closed");
                sinkClosed = true;
                buffer.notifyAll(); // Notify the source that no more bytes are coming.
            }
        }

        @Override
        public Timeout timeout() {
            return timeout;
        }
    }

    final class PipeSource implements Source {
        final Timeout timeout = new Timeout();

        @Override
        public long read(Buffer sink, long byteCount) throws IOException {
            synchronized (buffer) {
                if (sourceClosed) throw new IllegalStateException("closed");

                while (buffer.size() == 0) {
                    if (sinkClosed) return -1L;
                    timeout.waitUntilNotified(buffer); // Wait until the sink fills the buffer.
                }

                long result = buffer.read(sink, byteCount);
                buffer.notifyAll(); // Notify the sink that it can resume writing.
                return result;
            }
        }

        @Override
        public void close() {
            synchronized (buffer) {
                sourceClosed = true;
                buffer.notifyAll(); // Notify the sink that no more bytes are desired.
            }
        }

        @Override
        public Timeout timeout() {
            return timeout;
        }
    }

}
