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
package org.aoju.bus.socket.origin;


import org.aoju.bus.core.io.PageBuffer;
import org.aoju.bus.core.io.VirtualBuffer;
import org.aoju.bus.logger.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * AIO传输层会话
 *
 * <p>
 * AioSession为最核心的类,封装{@link AsynchronousSocketChannel} API接口,简化IO操作
 * </p>
 * <p>
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
 * <li>{@link TcpAioSession#setAttachment(Object)}  </li>
 * </ol>
 *
 * </p>
 *
 * @author Kimi Liu
 * @version 6.0.3
 * @since JDK 1.8+
 */
class TcpAioSession<T> extends AioSession<T> {

    /**
     * 底层通信channel对象
     */
    protected AsynchronousSocketChannel channel;
    /**
     * 读缓冲
     * <p>大小取决于AioQuickClient/AioQuickServer设置的setReadBufferSize</p>
     */
    protected VirtualBuffer readBuffer;
    /**
     * 写缓冲
     */
    protected VirtualBuffer writeBuffer;
    /**
     * 会话当前状态
     *
     * @see TcpAioSession#SESSION_STATUS_CLOSED
     * @see TcpAioSession#SESSION_STATUS_CLOSING
     * @see TcpAioSession#SESSION_STATUS_ENABLED
     */
    protected byte status = SESSION_STATUS_ENABLED;
    /**
     * 输出信号量,防止并发write导致异常
     */
    private Semaphore semaphore = new Semaphore(1);
    private TcpReadHandler<T> readCompletionHandler;
    private TcpWriteHandler<T> writeCompletionHandler;
    private ServerConfig<T> ioServerConfig;
    private InputStream inputStream;
    private WriteBuffer byteBuf;

    TcpAioSession(AsynchronousSocketChannel channel, final ServerConfig<T> config, TcpReadHandler<T> readCompletionHandler, TcpWriteHandler<T> writeCompletionHandler, PageBuffer pageBuffer) {
        this.channel = channel;
        this.readCompletionHandler = readCompletionHandler;
        this.writeCompletionHandler = writeCompletionHandler;
        this.ioServerConfig = config;

        this.readBuffer = pageBuffer.allocate(config.getReadBufferSize());
        byteBuf = new WriteBuffer(pageBuffer, var -> {
            if (!semaphore.tryAcquire()) {
                return null;
            }
            TcpAioSession.this.writeBuffer = var.poll();
            if (writeBuffer == null) {
                semaphore.release();
            } else {
                continueWrite(writeBuffer);
            }
            return null;
        }, ioServerConfig.getWriteQueueCapacity());
        //触发状态机
        config.getProcessor().stateEvent(this, StateMachine.NEW_SESSION, null);
    }

    /**
     * 初始化AioSession
     */
    void initSession() {
        continueRead();
    }

    /**
     * 触发AIO的写操作,
     * <p>需要调用控制同步</p>
     */
    void writeToChannel() {
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
        semaphore.release();
        //此时可能是Closing或Closed状态
        if (status != SESSION_STATUS_ENABLED) {
            close();
        } else if (!byteBuf.isClosed()) {
            //也许此时有新的消息通过write方法添加到writeCacheQueue中
            byteBuf.flush();
        }
    }


    /**
     * 内部方法：触发通道的读操作
     *
     * @param buffer
     */
    protected final void readFromChannel0(ByteBuffer buffer) {
        channel.read(buffer, this, readCompletionHandler);
    }

    /**
     * 内部方法：触发通道的写操作
     */
    protected final void writeToChannel0(ByteBuffer buffer) {
        channel.write(buffer, 0L, TimeUnit.MILLISECONDS, this, writeCompletionHandler);
    }

    public final WriteBuffer writeBuffer() {
        return byteBuf;
    }
//
//    /**
//     * 强制关闭当前AIOSession
//     * <p>若此时还存留待输出的数据,则会导致该部分数据丢失</p>
//     */
//    public final void close() {
//        close(true);
//    }

    /**
     * 是否立即关闭会话
     *
     * @param immediate true:立即关闭,false:响应消息发送完后关闭
     */
    public synchronized void close(boolean immediate) {
        //status == SESSION_STATUS_CLOSED说明close方法被重复调用
        if (status == SESSION_STATUS_CLOSED) {
            Logger.warn("ignore, session:{} is closed:", getSessionID());
            return;
        }
        status = immediate ? SESSION_STATUS_CLOSED : SESSION_STATUS_CLOSING;
        if (immediate) {
            try {
                if (!byteBuf.isClosed()) {
                    byteBuf.close();
                }
                byteBuf = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
            readBuffer.clean();
            readBuffer = null;
            if (writeBuffer != null) {
                writeBuffer.clean();
                writeBuffer = null;
            }
            try {
                channel.shutdownInput();
            } catch (IOException e) {
                Logger.debug(e.getMessage(), e);
            }
            try {
                channel.shutdownOutput();
            } catch (IOException e) {
                Logger.debug(e.getMessage(), e);
            }
            try {
                channel.close();
            } catch (IOException e) {
                Logger.debug("close session exception", e);
            }
            ioServerConfig.getProcessor().stateEvent(this, StateMachine.SESSION_CLOSED, null);
        } else if ((writeBuffer == null || !writeBuffer.buffer().hasRemaining()) && !byteBuf.hasData()) {
            close(true);
        } else {
            ioServerConfig.getProcessor().stateEvent(this, StateMachine.SESSION_CLOSING, null);
            if (!byteBuf.isClosed()) {
                byteBuf.flush();
            }
        }
    }

    /**
     * 获取当前Session的唯一标识
     */
    public final String getSessionID() {
        return "aioSession-" + hashCode();
    }

    /**
     * 当前会话是否已失效
     */
    public final boolean isInvalid() {
        return status != SESSION_STATUS_ENABLED;
    }


    /**
     * 触发通道的读回调操作
     */
    void readFromChannel(boolean eof) {
        if (status == SESSION_STATUS_CLOSED) {
            return;
        }
        final ByteBuffer readBuffer = this.readBuffer.buffer();
        readBuffer.flip();
        final Message<T> messageProcessor = ioServerConfig.getProcessor();
        while (readBuffer.hasRemaining() && status == SESSION_STATUS_ENABLED) {
            T dataEntry;
            try {
                dataEntry = ioServerConfig.getProtocol().decode(readBuffer, this);
            } catch (Exception e) {
                messageProcessor.stateEvent(this, StateMachine.DECODE_EXCEPTION, e);
                throw e;
            }
            if (dataEntry == null) {
                break;
            }

            //处理消息
            try {
                messageProcessor.process(this, dataEntry);
            } catch (Exception e) {
                messageProcessor.stateEvent(this, StateMachine.PROCESS_EXCEPTION, e);
            }
        }


        if (eof || status == SESSION_STATUS_CLOSING) {
            close(false);
            messageProcessor.stateEvent(this, StateMachine.INPUT_SHUTDOWN, null);
            return;
        }
        if (status == SESSION_STATUS_CLOSED) {
            return;
        }

        //数据读取完毕
        if (readBuffer.remaining() == 0) {
            readBuffer.clear();
        } else if (readBuffer.position() > 0) {
            // 仅当发生数据读取时调用compact,减少内存拷贝
            readBuffer.compact();
        } else {
            readBuffer.position(readBuffer.limit());
            readBuffer.limit(readBuffer.capacity());
        }

        //读缓冲区已满
        if (!readBuffer.hasRemaining()) {
            RuntimeException exception = new RuntimeException("readBuffer has no remaining");
            messageProcessor.stateEvent(this, StateMachine.DECODE_EXCEPTION, exception);
            throw exception;
        }

        if (byteBuf != null && !byteBuf.isClosed()) {
            byteBuf.flush();
        }
        continueRead();
    }


    protected void continueRead() {
        readFromChannel0(readBuffer.buffer());
    }

    protected void continueWrite(VirtualBuffer writeBuffer) {
        writeToChannel0(writeBuffer.buffer());
    }

    /**
     * @see AsynchronousSocketChannel#getLocalAddress()
     */
    public final InetSocketAddress getLocalAddress() throws IOException {
        assertChannel();
        return (InetSocketAddress) channel.getLocalAddress();
    }

    /**
     * @see AsynchronousSocketChannel#getRemoteAddress()
     */
    public final InetSocketAddress getRemoteAddress() throws IOException {
        assertChannel();
        return (InetSocketAddress) channel.getRemoteAddress();
    }

    private void assertChannel() throws IOException {
        if (status == SESSION_STATUS_CLOSED || channel == null) {
            throw new IOException("session is closed");
        }
    }

    ServerConfig<T> getServerConfig() {
        return this.ioServerConfig;
    }

    /**
     * 获得数据输入流对象
     * <p>
     * faster模式下调用该方法会触发UnsupportedOperationException异常
     * </p>
     * <p>
     * MessageProcessor采用异步处理消息的方式时,调用该方法可能会出现异常
     * </p>
     */
    public InputStream getInputStream() throws IOException {
        return inputStream == null ? getInputStream(-1) : inputStream;
    }

    /**
     * 获取已知长度的InputStream
     *
     * @param length InputStream长度
     */
    public InputStream getInputStream(int length) throws IOException {
        if (inputStream != null) {
            throw new IOException("pre inputStream has not closed");
        }
        if (inputStream != null) {
            return inputStream;
        }
        synchronized (this) {
            if (inputStream == null) {
                inputStream = new InnerInputStream(length);
            }
        }
        return inputStream;
    }


    private class InnerInputStream extends InputStream {
        private int remainLength;

        public InnerInputStream(int length) {
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
            readBuffer.clear();

            try {
                int readSize = channel.read(readBuffer).get();
                readBuffer.flip();
                if (readSize == -1) {
                    remainLength = 0;
                    return -1;
                } else {
                    return read();
                }
            } catch (Exception e) {
                throw new IOException(e);
            }
        }

        @Override
        public int available() {
            return remainLength == 0 ? 0 : readBuffer.buffer().remaining();
        }

        @Override
        public void close() {
            if (TcpAioSession.this.inputStream == InnerInputStream.this) {
                TcpAioSession.this.inputStream = null;
            }
        }
    }
}
