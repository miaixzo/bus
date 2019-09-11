package org.aoju.bus.socket.spring.intercept;

import org.aoju.bus.socket.spring.support.MessageFrom;
import org.springframework.messaging.MessageChannel;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.messaging.StompSubProtocolHandler;

import java.util.List;

/**
 * @author Kimi Liu
 * @version 3.2.6
 * @since JDK 1.8
 */
public class FromClientExecutionChain {

    private List<FromClientInterceptor> interceptors;

    private int interceptorIndex = -1;

    public FromClientExecutionChain(List<FromClientInterceptor> interceptors) {
        Assert.notNull(interceptors, "interceptors不能为null");
        this.interceptors = interceptors;
    }

    public boolean applyPreHandle(WebSocketSession session, MessageFrom message, MessageChannel outputChannel, StompSubProtocolHandler handler) {
        if (!ObjectUtils.isEmpty(interceptors)) {
            for (int i = 0; i < interceptors.size(); i++) {
                FromClientInterceptor interceptor = interceptors.get(i);
                if (!interceptor.preHandle(session, message, outputChannel, handler)) {
                    applyPostHandle(session, message, outputChannel, handler);
                    return false;
                }
                this.interceptorIndex = i;
            }
        }
        return true;
    }

    public void applyPostHandle(WebSocketSession session, MessageFrom message, MessageChannel outputChannel, StompSubProtocolHandler handler) {
        if (!ObjectUtils.isEmpty(interceptors)) {
            for (int i = this.interceptorIndex; i >= 0; i--) {
                FromClientInterceptor interceptor = interceptors.get(i);
                interceptor.postHandle(session, message, outputChannel, handler);
            }
        }
    }

}
