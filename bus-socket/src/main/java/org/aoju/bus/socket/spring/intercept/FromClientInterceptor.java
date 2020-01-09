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
 * @version 5.5.2
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
