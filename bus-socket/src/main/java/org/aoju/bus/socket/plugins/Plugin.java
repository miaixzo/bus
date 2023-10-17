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
package org.aoju.bus.socket.plugins;

import org.aoju.bus.socket.AioSession;
import org.aoju.bus.socket.NetMonitor;
import org.aoju.bus.socket.SocketStatus;
import org.aoju.bus.socket.process.MessageProcessor;

/**
 * 插件接口
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface Plugin<T> extends NetMonitor {

    /**
     * 对请求消息进行预处理,并决策是否进行后续的MessageProcessor处理
     * 若返回false,则当前消息将被忽略
     * 若返回true,该消息会正常秩序MessageProcessor.process.
     *
     * @param session 会话
     * @param t       对象
     * @return the true/false
     */
    boolean preProcess(AioSession session, T t);

    /**
     * 监听状态机事件
     *
     * @param socketStatus 状态
     * @param session      会话
     * @param throwable    线程
     * @see MessageProcessor#stateEvent(AioSession, SocketStatus, Throwable)
     */
    void stateEvent(SocketStatus socketStatus, AioSession session, Throwable throwable);

}
