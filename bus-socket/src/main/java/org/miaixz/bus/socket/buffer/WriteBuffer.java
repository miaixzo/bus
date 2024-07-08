/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org sandao and other contributors.             ~
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
package org.miaixz.bus.socket.buffer;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.WritePendingException;
import java.util.concurrent.Semaphore;
import java.util.function.Consumer;

/**
 * 包装当前会话分配到的虚拟Buffer,提供流式操作方式
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public final class WriteBuffer extends OutputStream {

    /**
     * 存储已就绪待输出的数据
     */
    private final VirtualBuffer[] items;

    /**
     * 为当前 WriteBuffer 提供数据存放功能的缓存页
     */
    private final BufferPage bufferPage;
    /**
     * 缓冲区数据刷新Function
     */
    private final Consumer<VirtualBuffer> writeConsumer;
    /**
     * 默认内存块大小
     */
    private final int chunkSize;
    /**
     * 输出信号量,防止并发write导致异常
     */
    private final Semaphore semaphore = new Semaphore(1);
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
    /**
     * 辅助8字节以内输出的缓存组数
     */
    private byte[] cacheByte;
    private Consumer<WriteBuffer> completionConsumer;

    public WriteBuffer(BufferPage bufferPage, Consumer<VirtualBuffer> writeConsumer, int chunkSize, int capacity) {
        this.bufferPage = bufferPage;
        this.writeConsumer = writeConsumer;
        this.items = new VirtualBuffer[capacity];
        this.chunkSize = chunkSize;
    }

    /**
     * 按照{@link OutputStream#write(int)}规范：要写入的字节是参数 b 的八个低位。 b 的 24 个高位将被忽略。
     * 而使用该接口时容易传入非byte范围内的数据，接口定义与实际使用出现歧义的可能性较大，故建议废弃该方法，选用{@link WriteBuffer#writeByte(byte)}。
     *
     * @param b 输出字节
     */
    @Override
    public void write(int b) {
        writeByte((byte) b);
    }

    /**
     * 输出一个short类型的数据
     *
     * @param v short数值
     * @throws IOException IO异常
     */
    public void writeShort(short v) throws IOException {
        initCacheBytes();
        cacheByte[0] = (byte) ((v >>> 8) & 0xFF);
        cacheByte[1] = (byte) (v & 0xFF);
        write(cacheByte, 0, 2);
    }

    /**
     * @param b 待输出数值
     * @see #write(int)
     */
    public synchronized void writeByte(byte b) {
        if (writeInBuf == null) {
            writeInBuf = bufferPage.allocate(chunkSize);
        }
        writeInBuf.buffer().put(b);
        flushWriteBuffer(false);
    }

    private void flushWriteBuffer(boolean forceFlush) {
        if (!forceFlush && writeInBuf.buffer().hasRemaining()) {
            return;
        }
        writeInBuf.buffer().flip();
        VirtualBuffer virtualBuffer = writeInBuf;
        writeInBuf = null;
        if (count == 0 && semaphore.tryAcquire()) {
            writeConsumer.accept(virtualBuffer);
            return;
        }

        try {
            while (count == items.length) {
                this.wait();
                // 防止因close诱发内存泄露
                if (closed) {
                    virtualBuffer.clean();
                    return;
                }
            }

            items[putIndex] = virtualBuffer;
            if (++putIndex == items.length) {
                putIndex = 0;
            }
            count++;
        } catch (InterruptedException e1) {
            throw new RuntimeException(e1);
        } finally {
            flush();
        }
    }

    /**
     * 输出int数值,占用4个字节
     *
     * @param v int数值
     * @throws IOException IO异常
     */
    public void writeInt(int v) throws IOException {
        initCacheBytes();
        cacheByte[0] = (byte) ((v >>> 24) & 0xFF);
        cacheByte[1] = (byte) ((v >>> 16) & 0xFF);
        cacheByte[2] = (byte) ((v >>> 8) & 0xFF);
        cacheByte[3] = (byte) (v & 0xFF);
        write(cacheByte, 0, 4);
    }

    /**
     * 输出long数值,占用8个字节
     *
     * @param v long数值
     * @throws IOException IO异常
     */
    public void writeLong(long v) throws IOException {
        initCacheBytes();
        cacheByte[0] = (byte) ((v >>> 56) & 0xFF);
        cacheByte[1] = (byte) ((v >>> 48) & 0xFF);
        cacheByte[2] = (byte) ((v >>> 40) & 0xFF);
        cacheByte[3] = (byte) ((v >>> 32) & 0xFF);
        cacheByte[4] = (byte) ((v >>> 24) & 0xFF);
        cacheByte[5] = (byte) ((v >>> 16) & 0xFF);
        cacheByte[6] = (byte) ((v >>> 8) & 0xFF);
        cacheByte[7] = (byte) (v & 0xFF);
        write(cacheByte, 0, 8);
    }

    @Override
    public synchronized void write(byte[] b, int off, int len) throws IOException {
        if (len == 0) {
            return;
        }
        if (writeInBuf == null) {
            if (chunkSize >= len) {
                writeInBuf = bufferPage.allocate(chunkSize);
            } else {
                int m = len % chunkSize;
                writeInBuf = bufferPage.allocate(m == 0 ? len : len + chunkSize - m);
            }
        }
        ByteBuffer writeBuffer = writeInBuf.buffer();
        if (closed) {
            writeInBuf.clean();
            writeInBuf = null;
            throw new IOException("writeBuffer has closed");
        }
        int remaining = writeBuffer.remaining();
        if (remaining > len) {
            writeBuffer.put(b, off, len);
        } else {
            writeBuffer.put(b, off, remaining);
            flushWriteBuffer(true);
            if (len > remaining) {
                write(b, off + remaining, len - remaining);
            }
        }
    }

    /**
     * 执行异步输出操作
     * 此方法会将指定的字节流异步写入，并在完成时通知提供的消费者。
     *
     * @param bytes    待输出的字节流。
     * @param offset   字节流中开始输出的偏移量。
     * @param len      要输出的字节数。
     * @param consumer 完成输出后调用的消费者接口，用于处理写入完成后的缓冲区。
     * @throws IOException           如果在写入过程中发生I/O错误。
     * @throws WritePendingException 如果已有写入操作未完成，此时再调用此方法会抛出此异常。
     */
    public synchronized void write(byte[] bytes, int offset, int len, Consumer<WriteBuffer> consumer) throws IOException {
        if (completionConsumer != null) {
            throw new WritePendingException();
        }
        this.completionConsumer = consumer;
        write(bytes, offset, len);
        flush();
    }

    public synchronized void write(byte[] bytes, Consumer<WriteBuffer> consumer) throws IOException {
        write(bytes, 0, bytes.length, consumer);
    }

    public synchronized void transferFrom(ByteBuffer byteBuffer, Consumer<WriteBuffer> consumer) throws IOException {
        if (!byteBuffer.hasRemaining()) {
            throw new IllegalStateException("none remaining byteBuffer");
        }
        if (writeInBuf != null && writeInBuf.buffer().position() > 0) {
            flushWriteBuffer(true);
        }
        if (completionConsumer != null) {
            throw new WritePendingException();
        }
        if (writeInBuf != null && writeInBuf.buffer().position() > 0) {
            throw new IllegalStateException();
        }
        this.completionConsumer = consumer;
        VirtualBuffer wrap = VirtualBuffer.wrap(byteBuffer);
        if (count == 0 && semaphore.tryAcquire()) {
            writeConsumer.accept(wrap);
            return;
        }
        try {
            while (count == items.length) {
                this.wait();
                // 防止因close诱发内存泄露
                if (closed) {
                    return;
                }
            }

            items[putIndex] = wrap;
            if (++putIndex == items.length) {
                putIndex = 0;
            }
            count++;
        } catch (InterruptedException e1) {
            throw new RuntimeException(e1);
        } finally {
            flush();
        }
    }

    /**
     * 初始化8字节的缓存数值
     */
    private void initCacheBytes() {
        if (cacheByte == null) {
            cacheByte = new byte[8];
        }
    }

    @Override
    public void flush() {
        if (closed) {
            throw new RuntimeException("OutputStream has closed");
        }
        if (semaphore.tryAcquire()) {
            VirtualBuffer virtualBuffer = poll();
            if (virtualBuffer == null) {
                semaphore.release();
            } else {
                writeConsumer.accept(virtualBuffer);
            }
        }
    }

    @Override
    public synchronized void close() {
        if (closed) {
            return;
        }
        flush();
        closed = true;
        if (writeInBuf != null) {
            writeInBuf.clean();
            writeInBuf = null;
        }
        VirtualBuffer byteBuf;
        while ((byteBuf = poll()) != null) {
            byteBuf.clean();
        }
    }

    /**
     * 是否存在待输出的数据
     *
     * @return true:有,false:无
     */
    public boolean isEmpty() {
        return count == 0 && (writeInBuf == null || writeInBuf.buffer().position() == 0);
    }

    public void finishWrite() {
        semaphore.release();
    }

    private VirtualBuffer pollItem() {
        if (count == 0) {
            if (completionConsumer != null) {
                Consumer<WriteBuffer> consumer = completionConsumer;
                this.completionConsumer = null;
                consumer.accept(this);
            }
            return null;
        }
        VirtualBuffer x = items[takeIndex];
        items[takeIndex] = null;
        if (++takeIndex == items.length) {
            takeIndex = 0;
        }
        if (count-- == items.length) {
            this.notifyAll();
        }
        return x;
    }

    /**
     * 获取并移除当前缓冲队列中头部的VirtualBuffer
     *
     * @return 待输出的VirtualBuffer
     */
    public synchronized VirtualBuffer poll() {
        VirtualBuffer item = pollItem();
        if (item != null) {
            return item;
        }
        if (writeInBuf != null && writeInBuf.buffer().position() > 0) {
            writeInBuf.buffer().flip();
            VirtualBuffer buffer = writeInBuf;
            writeInBuf = null;
            return buffer;
        } else {
            return null;
        }
    }

}