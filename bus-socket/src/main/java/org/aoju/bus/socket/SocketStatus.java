/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2023 aoju.org sandao and other contributors.               *
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
package org.aoju.bus.socket;

import org.aoju.bus.socket.process.MessageProcessor;

import java.nio.ByteBuffer;

/**
 * 列举了当前bus-socket所关注的各类状态枚举
 *
 * <pre>
 * 当前枚举的各状态机事件在发生后都会及时触发
 * {@link MessageProcessor#stateEvent(AioSession, SocketStatus, Throwable)}方法
 * 因此用户在实现的{@linkplain MessageProcessor}接口中可对自己关心的状态机事件进行处理
 * </pre>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public enum SocketStatus {

    /**
     * 连接已建立并构建Session对象
     */
    NEW_SESSION,
    /**
     * 读通道已被关闭
     * 通常由以下几种情况会触发该状态：
     * <ol>
     * <li>对端主动关闭write通道，致使本通常满足了EOF条件</li>
     * <li>当前AioSession处理完读操作后检测到自身正处于{@link SocketStatus#SESSION_CLOSING}状态</li>
     * </ol>
     * 未来该状态机可能会废除，并转移至NetMonitor
     */
    INPUT_SHUTDOWN,
    /**
     * 业务处理异常
     * 执行{@link MessageProcessor#process(AioSession, Object)}期间发生用户未捕获的异常
     */
    PROCESS_EXCEPTION,
    /**
     * 协议解码异常
     * 执行{@link Protocol#decode(ByteBuffer, AioSession)}期间发生未捕获的异常
     */
    DECODE_EXCEPTION,
    /**
     * 读操作异常
     * 在底层服务执行read操作期间因发生异常情况出发了{@link java.nio.channels.CompletionHandler#failed(Throwable, Object)}
     * 未来该状态机可能会废除，并转移至NetMonitor
     */
    INPUT_EXCEPTION,
    /**
     * 写操作异常
     * 在底层服务执行write操作期间因发生异常情况出发了{@link java.nio.channels.CompletionHandler#failed(Throwable, Object)}
     * 未来该状态机可能会废除，并转移至NetMonitor
     */
    OUTPUT_EXCEPTION,
    /**
     * 会话正在关闭中
     */
    SESSION_CLOSING,
    /**
     * 会话关闭成功
     * AioSession关闭成功
     */
    SESSION_CLOSED,
    /**
     * 拒绝接受连接,仅Server端有效
     */
    REJECT_ACCEPT,
    /**
     * 服务端接受连接异常
     */
    ACCEPT_EXCEPTION

}
