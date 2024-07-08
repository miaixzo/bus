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
package org.miaixz.bus.socket;

import java.nio.ByteBuffer;

/**
 * 列举了当前所关注的各类状态枚举
 *
 * <p>
 * 当前枚举的各状态机事件在发生后都会及时触发{@link Handler#stateEvent(Session, Status, Throwable)}方法。
 * 因此用户在实现的{@linkplain Handler}接口中可对自己关心的状态机事件进行处理。
 * </p>
 *
 * @author Kimi Liu
 * @see Handler
 * @since Java 17+
 */
public enum Status {
    /**
     * 连接已建立并构建Session对象
     */
    NEW_SESSION,
    /**
     * 读通道已被关闭。
     * 通常由以下几种情况会触发该状态：
     * <ol>
     * <li>对端主动关闭write通道，致使本通常满足了EOF条件</li>
     * <li>当前Session处理完读操作后检测到自身正处于{@link Status#SESSION_CLOSING}状态</li>
     * </ol>
     */
    INPUT_SHUTDOWN,
    /**
     * 业务处理异常
     * 执行{@link Handler#process(Session, Object)}期间发生未捕获的异常
     */
    PROCESS_EXCEPTION,
    /**
     * 协议解码异常
     * 执行{@link Message#decode(ByteBuffer, Session)}期间发生未捕获的异常
     */
    DECODE_EXCEPTION,
    /**
     * 读操作异常
     * 在底层服务执行read操作期间因发生异常情况触发了{@link java.nio.channels.CompletionHandler#failed(Throwable, Object)}
     */
    INPUT_EXCEPTION,
    /**
     * 写操作异常。
     * 在底层服务执行write操作期间因发生异常情况触发了{@link java.nio.channels.CompletionHandler#failed(Throwable, Object)}
     */
    OUTPUT_EXCEPTION,
    /**
     * 会话正在关闭中
     * 执行了{@link Session#close(boolean immediate)}方法，并且当前还存在待输出的数据
     */
    SESSION_CLOSING,
    /**
     * 会话关闭成功
     */
    SESSION_CLOSED,
    /**
     * 拒绝接受连接,仅Server端有效
     */
    REJECT_ACCEPT,
    /**
     * 服务端接受连接异常
     */
    ACCEPT_EXCEPTION,
    /**
     * 内部异常
     */
    INTERNAL_EXCEPTION
}
