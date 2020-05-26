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

import org.aoju.bus.core.toolkit.ObjectKit;
import org.aoju.bus.socket.spring.support.MessageFrom;
import org.springframework.messaging.MessageChannel;
import org.springframework.util.Assert;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.messaging.StompSubProtocolHandler;

import java.util.List;

/**
 * @author Kimi Liu
 * @version 5.9.3
 * @since JDK 1.8+
 */
public class FromClientExecutionChain {

    private List<FromClientInterceptor> interceptors;

    private int interceptorIndex = -1;

    public FromClientExecutionChain(List<FromClientInterceptor> interceptors) {
        Assert.notNull(interceptors, "interceptors不能为null");
        this.interceptors = interceptors;
    }

    public boolean applyPreHandle(WebSocketSession session, MessageFrom message, MessageChannel outputChannel, StompSubProtocolHandler handler) {
        if (!ObjectKit.isEmpty(interceptors)) {
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
        if (!ObjectKit.isEmpty(interceptors)) {
            for (int i = this.interceptorIndex; i >= 0; i--) {
                FromClientInterceptor interceptor = interceptors.get(i);
                interceptor.postHandle(session, message, outputChannel, handler);
            }
        }
    }

}
