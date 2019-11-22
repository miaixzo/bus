package org.aoju.bus.socket.spring.intercept;

import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.messaging.StompSubProtocolHandler;

import java.util.List;

/**
 * @author Kimi Liu
 * @version 5.2.3
 * @since JDK 1.8+
 */
public class ToClientExecutionChain {

    private List<ToClientInterceptor> interceptors;

    private int interceptorIndex = -1;

    public ToClientExecutionChain(List<ToClientInterceptor> interceptors) {
        Assert.notNull(interceptors, "interceptors不能为null");
        this.interceptors = interceptors;
    }

    public boolean applyPreHandle(WebSocketSession session, StompHeaderAccessor accessor, Object payload, StompSubProtocolHandler handler) {
        if (!ObjectUtils.isEmpty(interceptors)) {
            for (int i = 0; i < interceptors.size(); i++) {
                ToClientInterceptor interceptor = interceptors.get(i);
                if (!interceptor.preHandle(session, accessor, payload, handler)) {
                    applyPostHandle(session, accessor, payload, handler);
                    return false;
                }
                this.interceptorIndex = i;
            }
        }
        return true;
    }

    public void applyPostHandle(WebSocketSession session, StompHeaderAccessor accessor, Object payload, StompSubProtocolHandler handler) {
        if (!ObjectUtils.isEmpty(interceptors)) {
            for (int i = this.interceptorIndex; i >= 0; i--) {
                ToClientInterceptor interceptor = interceptors.get(i);
                interceptor.postHandle(session, accessor, payload, handler);
            }
        }
    }

}
