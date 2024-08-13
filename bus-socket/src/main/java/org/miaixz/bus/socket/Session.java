/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.socket;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;

import org.miaixz.bus.socket.buffer.WriteBuffer;

/**
 * 会话相关实现
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class Session {

    /**
     * Session状态:已关闭
     */
    protected static final byte SESSION_STATUS_CLOSED = 1;
    /**
     * Session状态:关闭中
     */
    protected static final byte SESSION_STATUS_CLOSING = 2;
    /**
     * Session状态:正常
     */
    protected static final byte SESSION_STATUS_ENABLED = 3;

    /**
     * 会话当前状态
     *
     * @see Session#SESSION_STATUS_CLOSED
     * @see Session#SESSION_STATUS_CLOSING
     * @see Session#SESSION_STATUS_ENABLED
     */
    protected byte status = SESSION_STATUS_ENABLED;
    /**
     * 是否读通道以至末尾
     */
    protected boolean eof;
    protected int modCount = 0;
    /**
     * 附件对象
     */
    private Object attachment;

    /**
     * 获取WriteBuffer用以数据输出
     *
     * @return WriteBuffer
     */
    public abstract WriteBuffer writeBuffer();

    /**
     * 获取读缓冲区对象
     */
    public abstract ByteBuffer readBuffer();

    /**
     * 强制关闭当前Session 若此时还存留待输出的数据，则会导致该部分数据丢失
     */
    public final void close() {
        close(true);
    }

    public abstract void awaitRead();

    /**
     * 继续触发读行为，该方法仅可在异步处理模式下可使用，否则会触发不可预知的异常
     */
    public abstract void signalRead();

    /**
     * 是否立即关闭会话
     *
     * @param immediate true:立即关闭,false:响应消息发送完后关闭
     */
    public abstract void close(boolean immediate);

    /**
     * 获取当前Session的唯一标识
     *
     * @return sessionId
     */
    public String getSessionID() {
        return "Session-" + hashCode();
    }

    /**
     * 当前会话是否已失效
     *
     * @return 是否失效
     */
    public boolean isInvalid() {
        return status != SESSION_STATUS_ENABLED;
    }

    /**
     * 获取附件对象
     *
     * @param <A> 附件对象类型
     * @return 附件
     */
    public final <A> A getAttachment() {
        return (A) attachment;
    }

    /**
     * 存放附件，支持任意类型
     *
     * @param <A>        附件对象类型
     * @param attachment 附件对象
     */
    public final <A> void setAttachment(A attachment) {
        this.attachment = attachment;
    }

    /**
     * 获取当前会话的本地连接地址
     *
     * @return 本地地址
     * @throws IOException IO异常
     * @see AsynchronousSocketChannel#getLocalAddress()
     */
    public abstract InetSocketAddress getLocalAddress() throws IOException;

    /**
     * 获取当前会话的远程连接地址
     *
     * @return 远程地址
     * @throws IOException IO异常
     * @see AsynchronousSocketChannel#getRemoteAddress()
     */
    public abstract InetSocketAddress getRemoteAddress() throws IOException;

    /**
     * 获得数据输入流对象 faster模式下调用该方法会触发UnsupportedOperationException异常。 Handler采用异步处理消息的方式时，调用该方法可能会出现异常。
     *
     * @return 输入流
     * @throws IOException IO异常
     */
    public InputStream getInputStream() throws IOException {
        throw new UnsupportedOperationException();
    }

    /**
     * 获取已知长度的InputStream
     *
     * @param length InputStream长度
     * @return 输入流
     * @throws IOException IO异常
     */
    public InputStream getInputStream(int length) throws IOException {
        throw new UnsupportedOperationException();
    }

}
