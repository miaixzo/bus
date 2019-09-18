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

import java.io.IOException;

/**
 * 附加的源和接收器。接收器的输出是源的输入。通常每个
 * 由它自己的线程访问:一个生产者线程将数据写入接收器和一个消费者线程
 * 从源读取数据。
 * 这个类使用一个缓冲区来解耦源和接收器。此缓冲区具有用户指定的最大值
 * 大小。当生产者线程超出其消费者时，缓冲区将被填满并最终写入
 * 水槽会堵塞，直到消费者赶上为止。对称地说，如果消费者跑得比它快
 * 生产者读取块，直到有数据要读取。限制等待的时间
 * 当接收器关闭时，源读取将继续正常完成，直到缓冲区
 * 此时read将返回-1，表示流的结束。但是,如果
 * 首先关闭源，对接收器的写入将立即失败，并带有{@link IOException}。
 *
 * @author Kimi Liu
 * @version 3.5.0
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
