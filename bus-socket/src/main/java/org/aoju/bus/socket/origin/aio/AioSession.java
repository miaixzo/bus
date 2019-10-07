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
package org.aoju.bus.socket.origin.aio;

import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.utils.IoUtils;
import org.aoju.bus.socket.SocketUtils;
import org.aoju.bus.socket.origin.OriginConfig;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * AIO会话
 * 每个客户端对应一个会话对象
 *
 * @author Kimi Liu
 * @version 3.6.5
 * @since JDK 1.8
 */
public class AioSession {

    private static final ReadHandler READ_HANDLER = new ReadHandler();
    private AsynchronousSocketChannel channel;
    private IoAction<ByteBuffer> ioAction;
    private ByteBuffer readBuffer;
    private ByteBuffer writeBuffer;
    /**
     * 读取超时时长，小于等于0表示默认
     */
    private long readTimeout;
    /**
     * 写出超时时长，小于等于0表示默认
     */
    private long writeTimeout;

    /**
     * 构造
     *
     * @param channel  {@link AsynchronousSocketChannel}
     * @param ioAction IO消息处理类
     * @param config   配置项
     */
    public AioSession(AsynchronousSocketChannel channel, IoAction<ByteBuffer> ioAction, OriginConfig config) {
        this.channel = channel;
        this.readBuffer = ByteBuffer.allocate(config.getReadBufferSize());
        this.writeBuffer = ByteBuffer.allocate(config.getWriteBufferSize());
        this.ioAction = ioAction;
    }

    /**
     * 获取{@link AsynchronousSocketChannel}
     *
     * @return {@link AsynchronousSocketChannel}
     */
    public AsynchronousSocketChannel getChannel() {
        return this.channel;
    }

    /**
     * 获取读取Buffer
     *
     * @return 读取Buffer
     */
    public ByteBuffer getReadBuffer() {
        return this.readBuffer;
    }

    /**
     * 获取写Buffer
     *
     * @return 写Buffer
     */
    public ByteBuffer getWriteBuffer() {
        return this.writeBuffer;
    }

    /**
     * 获取消息处理器
     *
     * @return {@link IoAction}
     */
    public IoAction<ByteBuffer> getIoAction() {
        return this.ioAction;
    }

    /**
     * 获取远程主机（客户端）地址和端口
     *
     * @return 远程主机（客户端）地址和端口
     */
    public SocketAddress getRemoteAddress() {
        return SocketUtils.getRemoteAddress(this.channel);
    }

    /**
     * 读取数据到Buffer
     *
     * @return this
     */
    public AioSession read() {
        return read(READ_HANDLER);
    }

    /**
     * 读取数据到Buffer
     *
     * @param handler {@link CompletionHandler}
     * @return this
     */
    public AioSession read(CompletionHandler<Integer, AioSession> handler) {
        if (isOpen()) {
            this.readBuffer.clear();
            this.channel.read(this.readBuffer, Math.max(this.readTimeout, 0L), TimeUnit.MILLISECONDS, this, handler);
        }
        return this;
    }

    /**
     * 写数据到目标端，并关闭输出
     *
     * @param data 字节
     * @return this
     */
    public AioSession writeAndClose(ByteBuffer data) {
        write(data);
        return closeOut();
    }

    /**
     * 写数据到目标端
     *
     * @param data 字节
     * @return {@link Future}
     */
    public Future<Integer> write(ByteBuffer data) {
        return this.channel.write(data);
    }

    /**
     * 写数据到目标端
     *
     * @param data    字节
     * @param handler {@link CompletionHandler}
     * @return this
     */
    public AioSession write(ByteBuffer data, CompletionHandler<Integer, AioSession> handler) {
        this.channel.write(data, Math.max(this.writeTimeout, 0L), TimeUnit.MILLISECONDS, this, handler);
        return this;
    }

    /**
     * 会话是否打开状态
     * 当Socket保持连接时会话始终打开
     *
     * @return 会话是否打开状态
     */
    public boolean isOpen() {
        return (null == this.channel) ? false : this.channel.isOpen();
    }

    /**
     * 关闭输出
     *
     * @return this
     */
    public AioSession closeIn() {
        if (null != this.channel) {
            try {
                this.channel.shutdownInput();
            } catch (IOException e) {
                throw new InstrumentException(e);
            }
        }
        return this;
    }

    /**
     * 关闭输出
     *
     * @return this
     */
    public AioSession closeOut() {
        if (null != this.channel) {
            try {
                this.channel.shutdownOutput();
            } catch (IOException e) {
                throw new InstrumentException(e);
            }
        }
        return this;
    }

    /**
     * 关闭会话
     */
    public void close() {
        IoUtils.close(this.channel);
        this.readBuffer = null;
        this.writeBuffer = null;
    }

    /**
     * 执行读，用于读取事件结束的回调
     */
    protected void callbackRead() {
        readBuffer.flip();// 读模式
        ioAction.doAction(this, readBuffer);
    }

}
