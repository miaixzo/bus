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
package org.aoju.bus.socket.origin.plugins;

import org.aoju.bus.socket.origin.AioSession;
import org.aoju.bus.socket.origin.NetMonitor;
import org.aoju.bus.socket.origin.StateMachine;

/**
 * @author Kimi Liu
 * @version 5.2.2
 * @since JDK 1.8+
 */
public interface Plugin<T> extends NetMonitor<T> {

    /**
     * 对请求消息进行预处理,并决策是否进行后续的MessageProcessor处理
     * 若返回false,则当前消息将被忽略
     * 若返回true,该消息会正常秩序MessageProcessor.process.
     *
     * @param session 会话
     * @param t       对象
     * @return true/false
     */
    boolean preProcess(AioSession<T> session, T t);


    /**
     * @param stateMachine 状态
     * @param session      会话
     * @param throwable    线程
     */
    void stateEvent(StateMachine stateMachine, AioSession<T> session, Throwable throwable);

}
