/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org sandao and other contributors.             *
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
package org.miaixz.bus.socket;

import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.socket.buffers.BufferPage;
import org.miaixz.bus.socket.buffers.VirtualBuffer;
import org.miaixz.bus.socket.buffers.WriteBuffer;
import org.miaixz.bus.socket.handler.ReadCompletionHandler;
import org.miaixz.bus.socket.handler.WriteCompletionHandler;
import org.miaixz.bus.socket.process.MessageProcessor;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * AIO传输层会话
 * AioSession为smart-socket最核心的类，封装{@link AsynchronousSocketChannel} API接口，简化IO操作
 * 其中开放给用户使用的接口为：
 * <ol>
 * <li>{@link TcpAioSession#close()}</li>
 * <li>{@link TcpAioSession#close(boolean)}</li>
 * <li>{@link TcpAioSession#getAttachment()} </li>
 * <li>{@link TcpAioSession#getInputStream()} </li>
 * <li>{@link TcpAioSession#getInputStream(int)} </li>
 * <li>{@link TcpAioSession#getLocalAddress()} </li>
 * <li>{@link TcpAioSession#getRemoteAddress()} </li>
 * <li>{@link TcpAioSession#getSessionID()} </li>
 * <li>{@link TcpAioSession#isInvalid()} </li>
 * <li>{@link TcpAioSession#setAttachment(Object)} </li>
 * </ol>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class TcpAioSession extends AioSession {

    /**
     * 底层通信channel对象
     */
    private final AsynchronousSocketChannel channel;
    /**
     * 输出流
     */
    private final WriteBuffer byteBuf;
    /**
     * 输出信号量,防止并发write导致异常
     */
    private final Semaphore semaphore = new Semaphore(1);
    /**
     * 读回调
     */
    private final ReadCompletionHandler readCompletionHandler;
    /**
     * 写回调
     */
    private final WriteCompletionHandler writeCompletionHandler;
    /**
     * 服务配置
     */
    private final ServerConfig serverConfig;
    private final Supplier<VirtualBuffer> function;
    /**
     * 是否读通道以至末尾
     */
    boolean eof;
    /**
     * 读缓冲。
     * <p>大小取决于AioQuickClient/AioQuickServer设置的setReadBufferSize</p>
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

    private int modCount = 0;

    /**
     * @param channel                Socket通道
     * @param config                 配置项
     * @param readCompletionHandler  读回调
     * @param writeCompletionHandler 写回调
     * @param bufferPage             绑定内存页
     */
    TcpAioSession(AsynchronousSocketChannel channel, final ServerConfig config, ReadCompletionHandler readCompletionHandler, WriteCompletionHandler writeCompletionHandler, BufferPage bufferPage, Supplier<VirtualBuffer> supplier) {
        this.channel = channel;
        this.readCompletionHandler = readCompletionHandler;
        this.writeCompletionHandler = writeCompletionHandler;
        this.serverConfig = config;
        this.function = supplier;
        Consumer<WriteBuffer> flushConsumer = var -> {
            if (!semaphore.tryAcquire()) {
                return;
            }
            TcpAioSession.this.writeBuffer = var.poll();
            if (writeBuffer == null) {
                semaphore.release();
            } else {
                continueWrite(writeBuffer);
            }
        };
        byteBuf = new WriteBuffer(bufferPage, flushConsumer, serverConfig.getWriteBufferSize(), serverConfig.getWriteBufferCapacity());
        // 触发状态机
        config.getProcessor().stateEvent(this, SocketStatus.NEW_SESSION, null);
        doRead();
    }

    public void doRead() {
        this.readBuffer = function.get();
        this.readBuffer.buffer().flip();
        signalRead();
    }

    public void suspendRead() {
        this.readBuffer.clean();
        this.readBuffer = null;
    }

    /**
     * 触发AIO的写操作,
     * <p>需要调用控制同步</p>
     */
    public void writeCompleted() {
        if (writeBuffer == null) {
            writeBuffer = byteBuf.pollItem();
        } else if (!writeBuffer.buffer().hasRemaining()) {
            writeBuffer.clean();
//            byteBuf.reuse(writeBuffer);
            writeBuffer = byteBuf.pollItem();
        }

        if (writeBuffer != null) {
            continueWrite(writeBuffer);
            return;
        }
        semaphore.release();
        // 此时可能是Closing或Closed状态
        if (status != SESSION_STATUS_ENABLED) {
            close();
        } else {
            // 也许此时有新的消息通过write方法添加到writeCacheQueue中
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
     * 是否立即关闭会话
     *
     * @param immediate true:立即关闭,false:响应消息发送完后关闭
     */
    public synchronized void close(boolean immediate) {
        if (status == SESSION_STATUS_CLOSED) {
            return;
        }
        status = immediate ? SESSION_STATUS_CLOSED : SESSION_STATUS_CLOSING;
        if (immediate) {
            try {
                byteBuf.close();
                readBuffer.clean();
                if (writeBuffer != null) {
                    writeBuffer.clean();
                    writeBuffer = null;
                }
            } finally {
                IoKit.close(channel);
                serverConfig.getProcessor().stateEvent(this, SocketStatus.SESSION_CLOSED, null);
            }
        } else if ((writeBuffer == null || !writeBuffer.buffer().hasRemaining()) && byteBuf.isEmpty()) {
            close(true);
        } else {
            serverConfig.getProcessor().stateEvent(this, SocketStatus.SESSION_CLOSING, null);
            byteBuf.flush();
        }
    }

    /**
     * 获取当前Session的唯一标识
     *
     * @return sessionId
     */
    public String getSessionID() {
        return "aioSession-" + hashCode();
    }

    /**
     * 当前会话是否已失效
     *
     * @return 是否失效
     */
    public boolean isInvalid() {
        return status != SESSION_STATUS_ENABLED;
    }

    public void flipRead(boolean eof) {
        this.eof = eof;
        this.readBuffer.buffer().flip();
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
        final MessageProcessor messageProcessor = serverConfig.getProcessor();
        while (readBuffer.hasRemaining() && status == SESSION_STATUS_ENABLED) {
            Object dataEntry;
            try {
                dataEntry = serverConfig.getProtocol().decode(readBuffer, this);
            } catch (Exception e) {
                messageProcessor.stateEvent(this, SocketStatus.DECODE_EXCEPTION, e);
                throw e;
            }
            if (dataEntry == null) {
                break;
            }

            //处理消息
            try {
                messageProcessor.process(this, dataEntry);
                if (modCount != this.modCount) {
                    return;
                }
            } catch (Exception e) {
                messageProcessor.stateEvent(this, SocketStatus.PROCESS_EXCEPTION, e);
            }
        }

        if (eof || status == SESSION_STATUS_CLOSING) {
            close(false);
            messageProcessor.stateEvent(this, SocketStatus.INPUT_SHUTDOWN, null);
            return;
        }
        if (status == SESSION_STATUS_CLOSED) {
            return;
        }

        byteBuf.flush();

        readBuffer.compact();
        //读缓冲区已满
        if (!readBuffer.hasRemaining()) {
            RuntimeException exception = new RuntimeException("readBuffer overflow");
            messageProcessor.stateEvent(this, SocketStatus.DECODE_EXCEPTION, exception);
            throw exception;
        }

        //read from channel
        NetMonitor monitor = getServerConfig().getMonitor();
        if (monitor != null) {
            monitor.beforeRead(this);
        }
        channel.read(readBuffer, 0L, TimeUnit.MILLISECONDS, this, readCompletionHandler);
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
     * 触发写操作
     *
     * @param writeBuffer 存放待输出数据的buffer
     */
    private void continueWrite(VirtualBuffer writeBuffer) {
        NetMonitor monitor = getServerConfig().getMonitor();
        if (monitor != null) {
            monitor.beforeWrite(this);
        }
        channel.write(writeBuffer.buffer(), 0L, TimeUnit.MILLISECONDS, this, writeCompletionHandler);
    }

    /**
     * @return 本地地址
     * @throws IOException IO异常
     * @see AsynchronousSocketChannel#getLocalAddress()
     */
    public final InetSocketAddress getLocalAddress() throws IOException {
        assertChannel();
        return (InetSocketAddress) channel.getLocalAddress();
    }

    /**
     * @return 远程地址
     * @throws IOException IO异常
     * @see AsynchronousSocketChannel#getRemoteAddress()
     */
    public final InetSocketAddress getRemoteAddress() throws IOException {
        assertChannel();
        return (InetSocketAddress) channel.getRemoteAddress();
    }

    /**
     * 断言当前会话是否可用
     *
     * @throws IOException IO异常
     */
    private void assertChannel() throws IOException {
        if (status == SESSION_STATUS_CLOSED || null == channel) {
            throw new IOException("session is closed");
        }
    }

    public ServerConfig getServerConfig() {
        return this.serverConfig;
    }

    /**
     * 获得数据输入流对象。
     * <p>
     * faster模式下调用该方法会触发UnsupportedOperationException异常
     * MessageProcessor采用异步处理消息的方式时，调用该方法可能会出现异常
     * </p>
     *
     * @return 同步读操作的流对象
     * @throws IOException io异常
     */
    public final InputStream getInputStream() throws IOException {
        return null == inputStream ? getInputStream(-1) : inputStream;
    }

    /**
     * 获取已知长度的InputStream
     *
     * @param length InputStream长度
     * @return 同步读操作的流对象
     * @throws IOException io异常
     */
    public final InputStream getInputStream(int length) throws IOException {
        if (null != inputStream) {
            throw new IOException("pre inputStream has not closed");
        }
        synchronized (this) {
            if (null == inputStream) {
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
            ByteBuffer readBuffer = TcpAioSession.this.readBuffer.buffer();
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
            if (null == b) {
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
            ByteBuffer readBuffer = TcpAioSession.this.readBuffer.buffer();
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
            ByteBuffer readBuffer = TcpAioSession.this.readBuffer.buffer();
            if (remainLength < -1) {
                return readBuffer.remaining();
            } else {
                return Math.min(remainLength, readBuffer.remaining());
            }
        }

        @Override
        public void close() {
            if (TcpAioSession.this.inputStream == InnerInputStream.this) {
                TcpAioSession.this.inputStream = null;
            }
        }
    }

}
