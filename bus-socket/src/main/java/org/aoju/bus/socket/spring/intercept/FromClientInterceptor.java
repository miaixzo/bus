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
package org.aoju.bus.socket.spring.intercept;

import org.aoju.bus.socket.spring.support.MessageFrom;
import org.springframework.messaging.MessageChannel;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.messaging.StompSubProtocolHandler;

/**
 * websocket消息拦截器,
 * 拦截客户端发来的消息
 *
 * @author Kimi Liu
 * @version 6.1.0
 * @since JDK 1.8+
 */
public interface FromClientInterceptor {

    /**
     * 前置处理
     *
     * @param session       websocket session
     * @param message       websocket消息
     * @param outputChannel websocket消息通道
     * @param handler       stomp协议控制器
     * @return true 执行后续操作,false 取消后续操作
     */
    default boolean preHandle(WebSocketSession session, MessageFrom message, MessageChannel outputChannel, StompSubProtocolHandler handler) {
        return true;
    }

    /**
     * 后置处理
     *
     * @param session       websocket session
     * @param message       websocket消息
     * @param outputChannel websocket消息通道
     * @param handler       stomp协议控制器
     */
    default void postHandle(WebSocketSession session, MessageFrom message, MessageChannel outputChannel, StompSubProtocolHandler handler) {

    }

}
