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
package org.miaixz.bus.socket.accord;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.socket.*;
import org.miaixz.bus.socket.buffer.BufferPage;
import org.miaixz.bus.socket.buffer.VirtualBuffer;
import org.miaixz.bus.socket.buffer.WriteBuffer;
import org.miaixz.bus.socket.metric.channels.AsynchronousChannelProvider;

/**
 * AIO传输层会话
 *
 * <p>
 * Session为最核心的类，封装{@link AsynchronousSocketChannel} API接口，简化IO操作。
 * </p>
 * 其中开放给用户使用的接口为：
 * <ol>
 * <li>{@link TcpSession#close()}</li>
 * <li>{@link TcpSession#close(boolean)}</li>
 * <li>{@link TcpSession#getAttachment()}</li>
 * <li>{@link TcpSession#getInputStream()}</li>
 * <li>{@link TcpSession#getInputStream(int)}</li>
 * <li>{@link TcpSession#getLocalAddress()}</li>
 * <li>{@link TcpSession#getRemoteAddress()}</li>
 * <li>{@link TcpSession#getSessionID()}</li>
 * <li>{@link TcpSession#isInvalid()}</li>
 * <li>{@link TcpSession#setAttachment(Object)}</li>
 * </ol>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public final class TcpSession extends Session {

    /**
     * 底层通信channel对象
     */
    private final AsynchronousSocketChannel channel;
    /**
     * 输出流
     */
    private final WriteBuffer byteBuf;
    /**
     * 服务上下文
     */
    private final Context context;
    /**
     * 缓冲函数
     */
    private final Supplier<VirtualBuffer> readBufferSupplier;
    /**
     * 读缓冲 大小取决于AioClient/AioServer设置的setReadBufferSize
     */
    private VirtualBuffer readBuffer;
    /**
     * 写缓冲
     */
    private VirtualBuffer writeBuffer;
    /**
     * 同步输入流
     */
    private InputStream inputStream;

    /**
     * @param channel Socket通道
     */
    public TcpSession(AsynchronousSocketChannel channel, Context context, BufferPage writeBufferPage,
            Supplier<VirtualBuffer> readBufferSupplier) {
        this.channel = channel;
        this.context = context;
        this.readBufferSupplier = readBufferSupplier;
        byteBuf = new WriteBuffer(writeBufferPage, this::continueWrite, this.context.getWriteBufferSize(),
                this.context.getWriteBufferCapacity());
        // 触发状态机
        this.context.getProcessor().stateEvent(this, Status.NEW_SESSION, null);
        doRead();
    }

    void doRead() {
        this.readBuffer = readBufferSupplier.get();
        this.readBuffer.buffer().flip();
        signalRead();
    }

    /**
     * 触发AIO的写操作, 需要调用控制同步
     */
    void writeCompleted(int result) {
        Monitor monitor = context.getMonitor();
        if (monitor != null) {
            monitor.afterWrite(this, result);
        }
        VirtualBuffer writeBuffer = TcpSession.this.writeBuffer;
        TcpSession.this.writeBuffer = null;
        if (writeBuffer == null) {
            writeBuffer = byteBuf.poll();
        } else if (!writeBuffer.buffer().hasRemaining()) {
            writeBuffer.clean();
            writeBuffer = byteBuf.poll();
        }

        if (writeBuffer != null) {
            continueWrite(writeBuffer);
            return;
        }
        byteBuf.finishWrite();
        // 此时可能是Closing或Closed状态
        if (status != SESSION_STATUS_ENABLED) {
            close();
        } else {
            // 也许此时有新的消息通过write方法添加到writeCacheQueue中
            byteBuf.flush();
        }
    }

    /**
     * 是否立即关闭会话
     *
     * @param immediate true:立即关闭,false:响应消息发送完后关闭
     */
    public synchronized void close(boolean immediate) {
        // status == SESSION_STATUS_CLOSED说明close方法被重复调用
        if (status == SESSION_STATUS_CLOSED) {
            return;
        }
        status = immediate ? SESSION_STATUS_CLOSED : SESSION_STATUS_CLOSING;
        if (immediate) {
            try {
                byteBuf.close();
                if (readBuffer != null) {
                    readBuffer.clean();
                    readBuffer = null;
                }
                if (writeBuffer != null) {
                    writeBuffer.clean();
                    writeBuffer = null;
                }
            } finally {
                IoKit.close(channel);
                context.getProcessor().stateEvent(this, Status.SESSION_CLOSED, null);
            }
        } else if ((writeBuffer == null || !writeBuffer.buffer().hasRemaining()) && byteBuf.isEmpty()) {
            close(true);
        } else {
            context.getProcessor().stateEvent(this, Status.SESSION_CLOSING, null);
            byteBuf.flush();
        }
    }

    /**
     * @return 输入流
     */
    public WriteBuffer writeBuffer() {
        return byteBuf;
    }

    @Override
    public ByteBuffer readBuffer() {
        return readBuffer.buffer();
    }

    @Override
    public void awaitRead() {
        modCount++;
    }

    /**
     * 读事件回调处理
     */
    private static final CompletionHandler<Integer, TcpSession> READ_COMPLETION_HANDLER = new CompletionHandler<>() {
        @Override
        public void completed(Integer result, TcpSession session) {
            try {
                session.readCompleted(result);
            } catch (Throwable throwable) {
                failed(throwable, session);
            }
        }

        @Override
        public void failed(Throwable exc, TcpSession session) {
            try {
                session.context.getProcessor().stateEvent(session, Status.INPUT_EXCEPTION, exc);
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                session.close(false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    void readCompleted(int result) {
        // 释放缓冲区
        if (result == AsynchronousChannelProvider.READ_MONITOR_SIGNAL) {
            this.readBuffer.clean();
            this.readBuffer = null;
            return;
        }
        if (result == AsynchronousChannelProvider.READABLE_SIGNAL) {
            doRead();
            return;
        }
        // 接收到的消息进行预处理
        Monitor monitor = context.getMonitor();
        if (monitor != null) {
            monitor.afterRead(this, result);
        }
        this.eof = result == -1;
        if (SESSION_STATUS_CLOSED != status) {
            this.readBuffer.buffer().flip();
            signalRead();
        }
    }

    /**
     * 触发通道的读回调操作
     */
    public void signalRead() {
        int modCount = this.modCount;
        if (status == SESSION_STATUS_CLOSED) {
            return;
        }
        final ByteBuffer readBuffer = this.readBuffer.buffer();
        final Handler handler = context.getProcessor();
        while (readBuffer.hasRemaining() && status == SESSION_STATUS_ENABLED) {
            Object dataEntry;
            try {
                dataEntry = context.getProtocol().decode(readBuffer, this);
            } catch (Exception e) {
                handler.stateEvent(this, Status.DECODE_EXCEPTION, e);
                throw e;
            }
            if (dataEntry == null) {
                break;
            }

            // 处理消息
            try {
                handler.process(this, dataEntry);
                if (modCount != this.modCount) {
                    return;
                }
            } catch (Exception e) {
                handler.stateEvent(this, Status.PROCESS_EXCEPTION, e);
            }
        }

        if (eof || status == SESSION_STATUS_CLOSING) {
            close(false);
            handler.stateEvent(this, Status.INPUT_SHUTDOWN, null);
            return;
        }
        if (status == SESSION_STATUS_CLOSED) {
            return;
        }

        byteBuf.flush();

        readBuffer.compact();
        // 读缓冲区已满
        if (!readBuffer.hasRemaining()) {
            InternalException exception = new InternalException(
                    "readBuffer overflow. The current TCP connection will be closed. Please fix your "
                            + context.getProtocol().getClass().getSimpleName() + "#decode bug.");
            handler.stateEvent(this, Status.DECODE_EXCEPTION, exception);
            throw exception;
        }

        // 从通道读取
        Monitor monitor = context.getMonitor();
        if (monitor != null) {
            monitor.beforeRead(this);
        }
        channel.read(readBuffer, 0L, TimeUnit.MILLISECONDS, this, READ_COMPLETION_HANDLER);
    }

    /**
     * 触发写操作
     *
     * @param writeBuffer 存放待输出数据的buffer
     */
    private void continueWrite(VirtualBuffer writeBuffer) {
        this.writeBuffer = writeBuffer;
        Monitor monitor = context.getMonitor();
        if (monitor != null) {
            monitor.beforeWrite(this);
        }
        channel.write(writeBuffer.buffer(), 0L, TimeUnit.MILLISECONDS, this, WRITE_COMPLETION_HANDLER);
    }

    /**
     * @return 本地地址
     * @throws IOException IO异常
     * @see AsynchronousSocketChannel#getLocalAddress()
     */
    public InetSocketAddress getLocalAddress() throws IOException {
        assertChannel();
        return (InetSocketAddress) channel.getLocalAddress();
    }

    /**
     * @return 远程地址
     * @throws IOException IO异常
     * @see AsynchronousSocketChannel#getRemoteAddress()
     */
    public InetSocketAddress getRemoteAddress() throws IOException {
        assertChannel();
        return (InetSocketAddress) channel.getRemoteAddress();
    }

    /**
     * 同步读取数据
     */
    private int synRead() throws IOException {
        ByteBuffer buffer = readBuffer.buffer();
        if (buffer.remaining() > 0) {
            return 0;
        }
        try {
            buffer.clear();
            int size = channel.read(buffer).get();
            buffer.flip();
            return size;
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    /**
     * 断言当前会话是否可用
     *
     * @throws IOException IO异常
     */
    private void assertChannel() throws IOException {
        if (status == SESSION_STATUS_CLOSED || channel == null) {
            throw new IOException("session is closed");
        }
    }

    /**
     * 获得数据输入流对象。
     * <p>
     * faster模式下调用该方法会触发UnsupportedOperationException异常。 Handler采用异步处理消息的方式时，调用该方法可能会出现异常。
     * </p>
     *
     * @return 同步读操作的流对象
     * @throws IOException io异常
     */
    public InputStream getInputStream() throws IOException {
        return inputStream == null ? getInputStream(-1) : inputStream;
    }

    /**
     * 获取已知长度的InputStream
     *
     * @param length InputStream长度
     * @return 同步读操作的流对象
     * @throws IOException io异常
     */
    public InputStream getInputStream(int length) throws IOException {
        if (inputStream != null) {
            throw new IOException("pre inputStream has not closed");
        }
        synchronized (this) {
            if (inputStream == null) {
                inputStream = new InnerInputStream(length);
            }
        }
        return inputStream;
    }

    /**
     * 同步读操作的InputStream
     */
    private class InnerInputStream extends InputStream {
        /**
         * 当前InputSteam可读字节数
         */
        private int remainLength;

        InnerInputStream(int length) {
            this.remainLength = length >= 0 ? length : -1;
        }

        @Override
        public int read() throws IOException {
            if (remainLength == 0) {
                return -1;
            }
            ByteBuffer readBuffer = TcpSession.this.readBuffer.buffer();
            if (readBuffer.hasRemaining()) {
                remainLength--;
                return readBuffer.get();
            }
            if (synRead() == -1) {
                remainLength = 0;
            }
            return read();
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            if (b == null) {
                throw new NullPointerException();
            } else if (off < 0 || len < 0 || len > b.length - off) {
                throw new IndexOutOfBoundsException();
            } else if (len == 0) {
                return 0;
            }
            if (remainLength == 0) {
                return -1;
            }
            if (remainLength > 0 && remainLength < len) {
                len = remainLength;
            }
            ByteBuffer readBuffer = TcpSession.this.readBuffer.buffer();
            int size = 0;
            while (len > 0 && synRead() != -1) {
                int readSize = Math.min(readBuffer.remaining(), len);
                readBuffer.get(b, off + size, readSize);
                size += readSize;
                len -= readSize;
            }
            remainLength -= size;
            return size;
        }

        @Override
        public int available() throws IOException {
            if (remainLength == 0) {
                return 0;
            }
            if (synRead() == -1) {
                remainLength = 0;
                return remainLength;
            }
            ByteBuffer readBuffer = TcpSession.this.readBuffer.buffer();
            if (remainLength < -1) {
                return readBuffer.remaining();
            } else {
                return Math.min(remainLength, readBuffer.remaining());
            }
        }

        @Override
        public void close() {
            if (TcpSession.this.inputStream == InnerInputStream.this) {
                TcpSession.this.inputStream = null;
            }
        }
    }

    /**
     * 写事件回调处理
     */
    private static final CompletionHandler<Integer, TcpSession> WRITE_COMPLETION_HANDLER = new CompletionHandler<Integer, TcpSession>() {
        @Override
        public void completed(Integer result, TcpSession session) {
            try {
                session.writeCompleted(result);
            } catch (Throwable throwable) {
                failed(throwable, session);
            }
        }

        @Override
        public void failed(Throwable exc, TcpSession session) {
            try {
                session.context.getProcessor().stateEvent(session, Status.OUTPUT_EXCEPTION, exc);
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                session.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

}
