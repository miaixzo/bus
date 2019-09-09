/*
 * The MIT License
 *
 * Copyright (c) 2017, aoju.org All rights reserved.
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

/**
 * Socket流处理接口
 * 实现此接口用于处理接收到的消息，发送指定消息
 *
 * @param <T> 经过解码器解码后的数据类型
 * @author Kimi Liu
 * @version 3.2.5
 * @since JDK 1.8
 */
public interface IoAction<T> {

    /**
     * 接收客户端连接（会话建立）事件处理
     *
     * @param session 会话
     */
    void accept(AioSession session);

    /**
     * 执行数据处理（消息读取）
     *
     * @param session Socket Session会话
     * @param data    解码后的数据
     */
    void doAction(AioSession session, T data);

    /**
     * 数据读取失败的回调事件处理（消息读取失败）
     *
     * @param exc     异常
     * @param session Session
     */
    void failed(Throwable exc, AioSession session);

}
