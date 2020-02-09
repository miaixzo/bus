/*
 * The MIT License
 *
 * Copyright (c) 2015-2020 aoju.org All rights reserved.
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
package org.aoju.bus.http.cache;

import org.aoju.bus.core.io.*;
import org.aoju.bus.core.utils.IoUtils;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * 将一个上游源复制到多个下游源。每个下游源返回与上游源相同的字节。
 * 下游源可以在上游返回数据时读取数据，也可以在上游源耗尽数据后读取数据
 * 当字节从上游返回时，它们被写入本地文件。必要时从该文件读取的下游源
 * 这个类保留一个最近从上游读取的字节的小缓冲区。减少文件I/O和数据复制
 *
 * @author Kimi Liu
 * @version 5.5.6
 * @since JDK 1.8+
 */
final class Relay {

    static final ByteString PREFIX_CLEAN = ByteString.encodeUtf8("Httpd cache v1\n");
    static final ByteString PREFIX_DIRTY = ByteString.encodeUtf8("Httpd DIRTY :(\n");
    private static final int SOURCE_UPSTREAM = 1;
    private static final int SOURCE_FILE = 2;
    private static final long FILE_HEADER_SIZE = 32L;
    /**
     * {@code upstreamReader}从upstream提取字节时使用的缓冲区。
     * 只有{@code upstreamReader}线程可以访问这个缓冲区
     */
    final Buffer upstreamBuffer = new Buffer();
    /**
     * 最近从{@link #upstream}读取的字节数。这是{@link #file}的后缀
     */
    final Buffer buffer = new Buffer();
    /**
     * {@code buffer}的最大大小
     */
    final long bufferMaxSize;
    /**
     * 用户提供的附加数据与源数据保持一致
     */
    private final ByteString metadata;
    /**
     * 读取/写入上游源及其元数据的持久性.
     */
    RandomAccessFile file;
    /**
     * 当前可以访问上游的线程。可能是零
     */
    Thread upstreamReader;
    /**
     * 文件具有upstream字节的完整副本，则为Null。只有{@code upstreamReader}线程可以访问这个源代码
     */
    Source upstream;
    /**
     * 从{@link #upstream}消耗的字节数
     */
    long upstreamPos;
    /**
     * 如果没有从{@code upstream}读取的字节，则为True
     */
    boolean complete;
    /**
     * 读取此流的活动源的数目的引用计数。当递减到0时，资源被释放，所有对{@link #newSource}的调用都返回null
     */
    int sourceCount;

    private Relay(RandomAccessFile file, Source upstream, long upstreamPos, ByteString metadata,
                  long bufferMaxSize) {
        this.file = file;
        this.upstream = upstream;
        this.complete = upstream == null;
        this.upstreamPos = upstreamPos;
        this.metadata = metadata;
        this.bufferMaxSize = bufferMaxSize;
    }

    /**
     * 创建一个从{@code upstream}读取实时流的新中继，使用{@code file}与其他源共享该数据
     *
     * @param file          文件信息
     * @param upstream      缓存流
     * @param metadata      元数据
     * @param bufferMaxSize 最大值
     * @return the relay
     * @throws IOException 异常
     */
    public static Relay edit(
            File file, Source upstream, ByteString metadata, long bufferMaxSize) throws IOException {
        RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
        Relay result = new Relay(randomAccessFile, upstream, 0L, metadata, bufferMaxSize);

        randomAccessFile.setLength(0L);
        result.writeHeader(PREFIX_DIRTY, -1L, -1L);

        return result;
    }

    /**
     * 创建一个从{@code file}读取记录流的中继
     *
     * @param file 文件信息
     * @return the relay
     * @throws IOException 异常
     */
    public static Relay read(File file) throws IOException {
        RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
        FileOperator fileOperator = new FileOperator(randomAccessFile.getChannel());

        // Read the header.
        Buffer header = new Buffer();
        fileOperator.read(0, header, FILE_HEADER_SIZE);
        ByteString prefix = header.readByteString(PREFIX_CLEAN.size());
        if (!prefix.equals(PREFIX_CLEAN)) throw new IOException("unreadable cache file");
        long upstreamSize = header.readLong();
        long metadataSize = header.readLong();

        // Read the metadata.
        Buffer metadataBuffer = new Buffer();
        fileOperator.read(FILE_HEADER_SIZE + upstreamSize, metadataBuffer, metadataSize);
        ByteString metadata = metadataBuffer.readByteString();

        // Return the result.
        return new Relay(randomAccessFile, null, upstreamSize, metadata, 0L);
    }

    private void writeHeader(
            ByteString prefix, long upstreamSize, long metadataSize) throws IOException {
        Buffer header = new Buffer();
        header.write(prefix);
        header.writeLong(upstreamSize);
        header.writeLong(metadataSize);
        if (header.size() != FILE_HEADER_SIZE) throw new IllegalArgumentException();

        FileOperator fileOperator = new FileOperator(file.getChannel());
        fileOperator.write(0, header, FILE_HEADER_SIZE);
    }

    private void writeMetadata(long upstreamSize) throws IOException {
        Buffer metadataBuffer = new Buffer();
        metadataBuffer.write(metadata);

        FileOperator fileOperator = new FileOperator(file.getChannel());
        fileOperator.write(FILE_HEADER_SIZE + upstreamSize, metadataBuffer, metadata.size());
    }

    void commit(long upstreamSize) throws IOException {
        // 将元数据写入文件末尾
        writeMetadata(upstreamSize);
        file.getChannel().force(false);

        writeHeader(PREFIX_CLEAN, upstreamSize, metadata.size());
        file.getChannel().force(false);

        synchronized (Relay.this) {
            complete = true;
        }

        IoUtils.close(upstream);
        upstream = null;
    }

    boolean isClosed() {
        return file == null;
    }

    public ByteString metadata() {
        return metadata;
    }

    /**
     * 返回与上游相同的字节的新源。如果此继电器已关闭，且没有其他可能的源，则返回null。
     * 在这种情况下，调用者应该在使用{@link #read}构建新中继后重试
     *
     * @return 缓冲字节流
     */
    public Source newSource() {
        synchronized (Relay.this) {
            if (file == null) return null;
            sourceCount++;
        }

        return new RelaySource();
    }

    class RelaySource implements Source {
        private final Timeout timeout = new Timeout();

        /**
         * 读取和写入共享文件的操作符。如果此源已关闭，则为null
         */
        private FileOperator fileOperator = new FileOperator(file.getChannel());

        /**
         * 下一个要读的字节。它总是小于或等于{@code upstreamPos}
         */
        private long sourcePos;

        @Override
        public long read(Buffer sink, long byteCount) throws IOException {
            if (fileOperator == null) throw new IllegalStateException("closed");

            long upstreamPos;
            int source;

            selectSource:
            synchronized (Relay.this) {
                while (sourcePos == (upstreamPos = Relay.this.upstreamPos)) {
                    if (complete) return -1L;

                    // 另一个线程已经读取，等待
                    if (upstreamReader != null) {
                        timeout.waitUntilNotified(Relay.this);
                        continue;
                    }
                    upstreamReader = Thread.currentThread();
                    source = SOURCE_UPSTREAM;
                    break selectSource;
                }

                long bufferPos = upstreamPos - buffer.size();

                // 读的字节在缓冲区之前。从文件中读取
                if (sourcePos < bufferPos) {
                    source = SOURCE_FILE;
                    break selectSource;
                }

                // 缓冲区有需要的数据。从那里读取并立即返回
                long bytesToRead = Math.min(byteCount, upstreamPos - sourcePos);
                buffer.copyTo(sink, sourcePos - bufferPos, bytesToRead);
                sourcePos += bytesToRead;
                return bytesToRead;
            }

            if (source == SOURCE_FILE) {
                long bytesToRead = Math.min(byteCount, upstreamPos - sourcePos);
                fileOperator.read(FILE_HEADER_SIZE + sourcePos, sink, bytesToRead);
                sourcePos += bytesToRead;
                return bytesToRead;
            }

            // 从upstream读取。这总是读取一个完整的缓冲区:这可能比当前调用Source.read()所请求的要多
            try {
                long upstreamBytesRead = upstream.read(upstreamBuffer, bufferMaxSize);

                if (upstreamBytesRead == -1L) {
                    commit(upstreamPos);
                    return -1L;
                }

                // 更新此源并准备此调用的结果
                long bytesRead = Math.min(upstreamBytesRead, byteCount);
                upstreamBuffer.copyTo(sink, 0, bytesRead);
                sourcePos += bytesRead;

                // 将upstream字节追加到文件中。
                fileOperator.write(
                        FILE_HEADER_SIZE + upstreamPos, upstreamBuffer.clone(), upstreamBytesRead);

                synchronized (Relay.this) {
                    // 向缓冲区追加新的upstream
                    buffer.write(upstreamBuffer, upstreamBytesRead);
                    if (buffer.size() > bufferMaxSize) {
                        buffer.skip(buffer.size() - bufferMaxSize);
                    }

                    // 既然文件和缓冲区都有，就调整upstreamPos
                    Relay.this.upstreamPos += upstreamBytesRead;
                }

                return bytesRead;
            } finally {
                synchronized (Relay.this) {
                    upstreamReader = null;
                    Relay.this.notifyAll();
                }
            }
        }

        @Override
        public Timeout timeout() {
            return timeout;
        }

        @Override
        public void close() throws IOException {
            if (fileOperator == null) return;
            fileOperator = null;

            RandomAccessFile fileToClose = null;
            synchronized (Relay.this) {
                sourceCount--;
                if (sourceCount == 0) {
                    fileToClose = file;
                    file = null;
                }
            }

            if (fileToClose != null) {
                IoUtils.close(fileToClose);
            }
        }
    }

}
