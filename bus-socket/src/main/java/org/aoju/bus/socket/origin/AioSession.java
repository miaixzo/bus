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

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;

/**
 * @author Kimi Liu
 * @version 6.1.0
 * @since JDK 1.8+
 */
public abstract class AioSession<T> {

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
     * @see AioSession#SESSION_STATUS_CLOSED
     * @see AioSession#SESSION_STATUS_CLOSING
     * @see AioSession#SESSION_STATUS_ENABLED
     */
    protected byte status = SESSION_STATUS_ENABLED;
    /**
     * 附件对象
     */
    private Object attachment;

    AioSession() {
    }

    public abstract WriteBuffer writeBuffer();

    /**
     * 强制关闭当前AIOSession
     * <p>若此时还存留待输出的数据,则会导致该部分数据丢失</p>
     */
    public final void close() {
        close(true);
    }

    /**
     * 是否立即关闭会话
     *
     * @param immediate true:立即关闭,false:响应消息发送完后关闭
     */
    public abstract void close(boolean immediate);

    /**
     * 获取当前Session的唯一标识
     *
     * @return 唯一标识
     */
    public String getSessionID() {
        return "aioSession-" + hashCode();
    }

    /**
     * 当前会话是否已失效
     *
     * @return true/false
     */
    public boolean isInvalid() {
        return status != SESSION_STATUS_ENABLED;
    }

    /**
     * 获取附件对象
     *
     * @param <T> 对象
     * @return the object
     */
    public final <T> T getAttachment() {
        return (T) attachment;
    }

    /**
     * 存放附件,支持任意类型
     *
     * @param <T>        对象
     * @param attachment 附件
     */
    public final <T> void setAttachment(T attachment) {
        this.attachment = attachment;
    }

    public abstract InetSocketAddress getLocalAddress() throws IOException;

    public abstract InetSocketAddress getRemoteAddress() throws IOException;

    /**
     * 获得数据输入流对象
     * <p>
     * faster模式下调用该方法会触发UnsupportedOperationException异常
     * </p>
     * <p>
     * MessageProcessor采用异步处理消息的方式时,调用该方法可能会出现异常
     * </p>
     *
     * @return 输入流对象
     * @throws IOException 异常
     */
    public InputStream getInputStream() throws IOException {
        throw new UnsupportedOperationException();
    }

    /**
     * 获取已知长度的InputStream
     *
     * @param length InputStream长度
     * @return 输入流对象
     * @throws IOException 异常
     */
    public InputStream getInputStream(int length) throws IOException {
        throw new UnsupportedOperationException();
    }


}
