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
package org.aoju.bus.socket.origin;

/**
 * 消息处理器，通过实现该接口，对完成解码的消息进行业务处理
 *
 * @author Kimi Liu
 * @version 5.0.8
 * @since JDK 1.8+
 */
public interface Message<T> {

    /**
     * 处理接收到的消息
     *
     * @param session 通信会话
     * @param msg     待处理的业务消息
     */
    void process(AioSession<T> session, T msg);

    /**
     * 状态机事件,当枚举事件发生时由框架触发该方法
     *
     * @param session          本次触发状态机的AioSession对象
     * @param stateMachineEnum 状态枚举
     * @param throwable        异常对象，如果存在的话
     * @see StateMachine
     */
    void stateEvent(AioSession<T> session, StateMachine stateMachineEnum, Throwable throwable);

    /**
     * 将AioSession加入群组group
     *
     * @param group   分组
     * @param session 会话
     */
    void join(String group, AioSession<T> session);


    /**
     * 将AioSession从群众group中移除
     *
     * @param group   分组
     * @param session 会话
     */
    void remove(String group, AioSession<T> session);

    /**
     * AioSession从所有群组中退出
     *
     * @param session 会话
     */
    void remove(AioSession<T> session);

    /**
     * 群发消息
     *
     * @param group 分组
     * @param t     信息
     */
    void writeToGroup(String group, byte[] t);
}
