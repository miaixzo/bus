/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
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
 ********************************************************************************/
package org.aoju.bus.core.io;

import java.io.IOException;

/**
 * 附加的源和汇。接收器的输出是源的输入。
 * 通常每个线程都由自己的线程访问:生产者线程向接收器写入数据，消费者线程从源读取数据
 *
 * @author Kimi Liu
 * @version 6.0.2
 * @since JDK 1.8+
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

    public Source source() {
        return source;
    }

    public Sink sink() {
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
                        // 等待，直到源耗尽缓冲区
                        timeout.waitUntilNotified(buffer);
                        continue;
                    }

                    long bytesToWrite = Math.min(bufferSpaceAvailable, byteCount);
                    buffer.write(source, bytesToWrite);
                    byteCount -= bytesToWrite;
                    // 通知源程序它可以继续读取
                    buffer.notifyAll();
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
                // 通知源不再接收字节
                buffer.notifyAll();
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
                    // 等待，直到接收器填满缓冲区
                    timeout.waitUntilNotified(buffer);
                }

                long result = buffer.read(sink, byteCount);
                // 通知接收器它可以继续写
                buffer.notifyAll();
                return result;
            }
        }

        @Override
        public void close() {
            synchronized (buffer) {
                sourceClosed = true;
                // 通知接收器不需要更多的字节
                buffer.notifyAll();
            }
        }

        @Override
        public Timeout timeout() {
            return timeout;
        }
    }

}