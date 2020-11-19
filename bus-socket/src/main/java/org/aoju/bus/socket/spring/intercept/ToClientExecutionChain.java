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
 *                                                                               *
 ********************************************************************************/
package org.aoju.bus.socket.spring.intercept;

import org.aoju.bus.core.toolkit.ObjectKit;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.util.Assert;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.messaging.StompSubProtocolHandler;

import java.util.List;

/**
 * @author Kimi Liu
 * @version 6.1.2
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
        if (!ObjectKit.isEmpty(interceptors)) {
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
        if (!ObjectKit.isEmpty(interceptors)) {
            for (int i = this.interceptorIndex; i >= 0; i--) {
                ToClientInterceptor interceptor = interceptors.get(i);
                interceptor.postHandle(session, accessor, payload, handler);
            }
        }
    }

}
