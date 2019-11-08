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
package org.aoju.bus.socket.origin;

import org.aoju.bus.core.io.segment.BufferPage;
import org.aoju.bus.core.io.segment.VirtualBuffer;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 包装当前会话分配到的虚拟Buffer,提供流式操作方式
 *
 * @author Kimi Liu
 * @version 5.2.1
 * @since JDK 1.8+
 */
public class WriteBuffer extends OutputStream {

    /**
     * 输出缓存块大小
     */
    private static final int WRITE_CHUNK_SIZE = ServerConfig.getIntProperty(ServerConfig.Property.SESSION_WRITE_CHUNK_SIZE, 4096);
    /**
     * 存储已就绪待输出的数据
     */
    private final VirtualBuffer[] items;
    /**
     * 同步锁
     */
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition notEmpty = lock.newCondition();
    private final Condition notFull = lock.newCondition();
    private final Condition waiting = lock.newCondition();
    /**
     * 为当前 WriteBuffer 提供数据存放功能的缓存页
     */
    private final BufferPage bufferPage;
    private final Function<WriteBuffer, Void> function;
    private volatile boolean isWaiting = false;
    /**
     * items 读索引位
     */
    private int takeIndex;
    /**
     * items 写索引位
     */
    private int putIndex;
    /**
     * items 中存放的缓冲数据数量
     */
    private int count;
    /**
     * 暂存当前业务正在输出的数据,输出完毕后会存放到items中
     */
    private VirtualBuffer writeInBuf;
    /**
     * 当前WriteBuffer是否已关闭
     */
    private boolean closed = false;
    private byte[] cacheByte = new byte[8];

    protected WriteBuffer(BufferPage bufferPage, Function<WriteBuffer, Void> flushFunction, int writeQueueSize) {
        this.bufferPage = bufferPage;
        this.function = flushFunction;
        this.items = new VirtualBuffer[writeQueueSize];
    }

    /**
     * 按照{@link OutputStream#write(int)}规范：要写入的字节是参数 b 的八个低位。 b 的 24 个高位将被忽略
     * 而使用该接口时容易传入非byte范围内的数据，接口定义与实际使用出现歧义的可能性较大，故建议废弃该方法，选用{@link WriteBuffer#writeByte(byte)}。
     *
     * @param b byte
     * @throws IOException 如果发生 I/O 错误
     */
    @Override
    public void write(int b) throws IOException {
        writeByte((byte) b);
    }

    public void writeShort(short v) throws IOException {
        cacheByte[0] = (byte) ((v >>> 8) & 0xFF);
        cacheByte[1] = (byte) ((v >>> 0) & 0xFF);
        write(cacheByte, 0, 2);
    }

    public void writeByte(byte b) {
        if (writeInBuf == null) {
            writeInBuf = bufferPage.allocate(WRITE_CHUNK_SIZE);
        }
        writeInBuf.buffer().put(b);
        if (writeInBuf.buffer().hasRemaining()) {
            return;
        }
        writeInBuf.buffer().flip();
        lock.lock();
        try {
            this.put(writeInBuf);
        } finally {
            lock.unlock();
        }
        writeInBuf = null;
        function.apply(this);
    }

    public void writeInt(int v) throws IOException {
        cacheByte[0] = (byte) ((v >>> 24) & 0xFF);
        cacheByte[1] = (byte) ((v >>> 16) & 0xFF);
        cacheByte[2] = (byte) ((v >>> 8) & 0xFF);
        cacheByte[3] = (byte) ((v >>> 0) & 0xFF);
        write(cacheByte, 0, 4);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        if (closed) {
            throw new IOException("OutputStream has closed");
        }
        if (b == null) {
            throw new NullPointerException();
        } else if ((off < 0) || (off > b.length) || (len < 0) ||
                ((off + len) > b.length) || ((off + len) < 0)) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return;
        }
        lock.lock();
        try {
            waitPreWriteFinish();
            do {
                if (writeInBuf == null) {
                    writeInBuf = bufferPage.allocate(Math.max(WRITE_CHUNK_SIZE, len - off));
                }
                ByteBuffer writeBuffer = writeInBuf.buffer();
                int minSize = Math.min(writeBuffer.remaining(), len - off);
                if (minSize == 0 || closed) {
                    writeInBuf.clean();
                    throw new IOException("writeBuffer.remaining:" + writeBuffer.remaining() + " closed:" + closed);
                }
                writeBuffer.put(b, off, minSize);
                off += minSize;
                if (!writeBuffer.hasRemaining()) {
                    writeBuffer.flip();
                    VirtualBuffer buffer = writeInBuf;
                    writeInBuf = null;
                    this.put(buffer);
                    function.apply(this);
                }
            } while (off < len);
            notifyWaiting();
        } finally {
            lock.unlock();
        }
    }

    /**
     * 唤醒处于waiting状态的线程
     */
    private void notifyWaiting() {
        isWaiting = false;
        waiting.signal();
    }

    /**
     * 确保数据输出有序性
     *
     * @throws IOException 如果发生 I/O 错误
     */
    private void waitPreWriteFinish() throws IOException {
        while (isWaiting) {
            try {
                waiting.await();
            } catch (InterruptedException e) {
                throw new IOException(e);
            }
        }
    }

    /**
     * 写入内容并刷新缓冲区。在{@link Message#process(AioSession, Object)}执行的write操作可无需调用该方法，业务执行完毕后框架本身会自动触发flush。
     * 调用该方法后数据会及时的输出到对端，如果再循环体中通过该方法往某个通道中写入数据将无法获得最佳性能表现，
     *
     * @param b 待输出数据
     * @throws IOException 如果发生 I/O 错误
     */
    public void writeAndFlush(byte[] b) throws IOException {
        if (b == null) {
            throw new NullPointerException();
        }
        writeAndFlush(b, 0, b.length);
    }

    /**
     * @param b   待输出数据
     * @param off b的起始位点
     * @param len 从b中输出的数据长度
     * @throws IOException 如果发生 I/O 错误
     * @see WriteBuffer#writeAndFlush(byte[])
     */
    public void writeAndFlush(byte[] b, int off, int len) throws IOException {
        write(b, off, len);
        flush();
    }

    @Override
    public void flush() {
        if (closed) {
            throw new RuntimeException("OutputStream has closed");
        }
        int size = this.count;
        if (size > 0) {
            function.apply(this);
        } else if (writeInBuf != null && writeInBuf.buffer().position() > 0 && lock.tryLock()) {
            try {
                if (writeInBuf != null && writeInBuf.buffer().position() > 0) {
                    final VirtualBuffer buffer = writeInBuf;
                    writeInBuf = null;
                    buffer.buffer().flip();
                    this.put(buffer);
                    size++;
                }
            } finally {
                lock.unlock();
            }
            if (size > 0) {
                function.apply(this);
            }
        }

    }

    @Override
    public void close() throws IOException {
        lock.lock();
        try {
            if (closed) {
                throw new IOException("OutputStream has closed");
            }
            flush();

            closed = true;

            VirtualBuffer byteBuf;
            while ((byteBuf = poll()) != null) {
                byteBuf.clean();
            }
            if (writeInBuf != null) {
                writeInBuf.clean();
            }
        } finally {
            lock.unlock();
        }
    }

    boolean isClosed() {
        return closed;
    }

    boolean hasData() {
        return count > 0 || (writeInBuf != null && writeInBuf.buffer().position() > 0);
    }


    /**
     * 存储缓冲区至队列中以备输出
     *
     * @param virtualBuffer 缓存对象
     */
    private void put(VirtualBuffer virtualBuffer) {
        try {
            while (count == items.length) {
                isWaiting = true;
                notFull.await();
            }
            items[putIndex] = virtualBuffer;
            if (++putIndex == items.length) {
                putIndex = 0;
            }
            count++;
            notEmpty.signal();
        } catch (InterruptedException e1) {
            throw new RuntimeException(e1);
        }
    }

    /**
     * 获取并移除当前缓冲队列中头部的VirtualBuffer
     *
     * @return 待输出的VirtualBuffer
     */
    VirtualBuffer poll() {
        lock.lock();
        try {
            if (count == 0) {
                return null;
            }
            VirtualBuffer x = items[takeIndex];
            items[takeIndex] = null;
            if (++takeIndex == items.length) {
                takeIndex = 0;
            }
            count--;
            notFull.signal();
            return x;
        } finally {
            lock.unlock();
        }
    }

}
