package org.aoju.bus.socket.spring.intercept;

import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.messaging.StompSubProtocolHandler;

/**
 * websocket消息拦截器，
 * 拦截发送给客户端的消息
 *
 * @author Kimi Liu
 * @version 3.0.9
 * @since JDK 1.8
 */
public interface ToClientInterceptor {

    /**
     * 前置处理
     *
     * @param session        websocket session
     * @param accessor       websocket消息头解析器
     * @param payload        websocket消息内容
     * @param handler        stomp协议控制器
     * @return true 执行后续操作，false 取消后续操作
     */
    default boolean preHandle(WebSocketSession session, StompHeaderAccessor accessor, Object payload, StompSubProtocolHandler handler) {
        return true;
    }

    /**
     * 后置处理
     *
     * @param session        websocket session
     * @param accessor       websocket消息头解析器
     * @param payload        websocket消息内容
     * @param handler        stomp协议控制器
     */
    default void postHandle(WebSocketSession session, StompHeaderAccessor accessor, Object payload, StompSubProtocolHandler handler) {

    }

}
