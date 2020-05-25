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
package org.aoju.bus.socket.spring.extension;

import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.core.utils.StringUtils;
import org.aoju.bus.socket.spring.intercept.FromClientExecutionChain;
import org.aoju.bus.socket.spring.intercept.FromClientInterceptor;
import org.aoju.bus.socket.spring.intercept.ToClientExecutionChain;
import org.aoju.bus.socket.spring.intercept.ToClientInterceptor;
import org.aoju.bus.socket.spring.support.MessageFrom;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.util.Assert;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedTransferQueue;

/**
 * 代替{@link org.springframework.web.socket.messaging.StompSubProtocolHandler}
 * 增加了对拦截器的支持
 *
 * @author Kimi Liu
 * @version 5.9.3
 * @since JDK 1.8+
 */
public class StompSubProtocolHandler extends org.springframework.web.socket.messaging.StompSubProtocolHandler {

    private List<FromClientInterceptor> fromClientInterceptors = new ArrayList<>();

    private List<ToClientInterceptor> toClientInterceptors = new ArrayList<>();

    @Override
    public void handleMessageFromClient(WebSocketSession session, WebSocketMessage<?> webSocketMessage, MessageChannel outputChannel) {
        FromClientExecutionChain chain = new FromClientExecutionChain(fromClientInterceptors);
        MessageFrom message = getMessageFromClient(webSocketMessage);
        if (chain.applyPreHandle(session, message, outputChannel, this)) {
            super.handleMessageFromClient(session, webSocketMessage, outputChannel);
            chain.applyPostHandle(session, message, outputChannel, this);
        }
    }

    @Override
    public void handleMessageToClient(WebSocketSession session, Message<?> message) {
        ToClientExecutionChain chain = new ToClientExecutionChain(toClientInterceptors);

        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        Object payload = message.getPayload();
        if (chain.applyPreHandle(session, accessor, payload, this)) {
            super.handleMessageToClient(session, message);
            chain.applyPostHandle(session, accessor, payload, this);
        }
    }

    public void addFromClientInterceptor(FromClientInterceptor interceptor) {
        Assert.notNull(interceptor, "interceptor不能为null");
        this.fromClientInterceptors.add(interceptor);
    }

    public void addToClientInterceptor(ToClientInterceptor interceptor) {
        Assert.notNull(interceptor, "interceptor不能为null");
        this.toClientInterceptors.add(interceptor);
    }

    private MessageFrom getMessageFromClient(WebSocketMessage<?> webSocketMessage) {
        if (webSocketMessage instanceof TextMessage) {
            MessageFrom message = new MessageFrom();
            String payload = ((TextMessage) webSocketMessage).getPayload();
            String[] arr = payload.split(Symbol.LF);
            Queue<String> queue = new LinkedTransferQueue<>();
            for (String str : arr) {
                String strTrim = str.trim();
                if (StringUtils.isEmpty(strTrim)) {
                    continue;
                }
                queue.offer(strTrim);
            }
            String type = queue.poll();
            message.setType(type);
            int last = 0;
            if ("SEND".equals(type)) {
                last = 1;
            }
            while (queue.size() > last) {
                String param = queue.poll();
                if (param.startsWith("id:")) {
                    message.setSubId(param.split(Symbol.COLON)[1].trim());
                } else if (param.startsWith("destination:")) {
                    message.setDestination(param.split(Symbol.COLON)[1].trim());
                } else if (param.startsWith("content-length:")) {
                    message.setContentLength(Integer.valueOf(param.split(Symbol.COLON)[1].trim()));
                }
            }
            String content = queue.poll();
            if (!StringUtils.isEmpty(content)) {
                if (content.startsWith("destination:") && StringUtils.isEmpty(message.getDestination())) {
                    message.setDestination(content.split(Symbol.COLON)[1].trim());
                } else {
                    message.setContent(content.trim());
                }
            }
            return message;
        }
        return null;
    }

}
