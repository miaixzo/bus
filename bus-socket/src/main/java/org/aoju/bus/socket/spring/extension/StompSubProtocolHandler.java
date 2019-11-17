package org.aoju.bus.socket.spring.extension;

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
 * @version 5.2.1
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
            String[] arr = payload.split("\n");
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
                    message.setSubId(param.split(":")[1].trim());
                } else if (param.startsWith("destination:")) {
                    message.setDestination(param.split(":")[1].trim());
                } else if (param.startsWith("content-length:")) {
                    message.setContentLength(Integer.valueOf(param.split(":")[1].trim()));
                }
            }
            String content = queue.poll();
            if (!StringUtils.isEmpty(content)) {
                if (content.startsWith("destination:") && StringUtils.isEmpty(message.getDestination())) {
                    message.setDestination(content.split(":")[1].trim());
                } else {
                    message.setContent(content.trim());
                }
            }
            return message;
        }
        return null;
    }

}
