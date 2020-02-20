/*
 * The MIT License
 *
 * Copyright (c) 2015-2020 aoju.org All rights reserved.
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
package org.aoju.bus.socket.spring.extension;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.DelegatingWebSocketMessageBrokerConfiguration;
import org.springframework.web.socket.config.annotation.WebMvcStompEndpointRegistry;

/**
 * 代替{@link DelegatingWebSocketMessageBrokerConfiguration},
 * 使用自定义的{@link StompEndpointRegistry}
 * 代替默认的{@link WebMvcStompEndpointRegistry}
 *
 * @author Kimi Liu
 * @version 5.6.2
 * @since JDK 1.8+
 */
public class MessageConfiguration extends DelegatingWebSocketMessageBrokerConfiguration {

    @Bean
    @Override
    public HandlerMapping stompWebSocketHandlerMapping() {
        WebSocketHandler handler = decorateWebSocketHandler(subProtocolWebSocketHandler());
        // 使用 StompEndpointRegistry 代替 WebMvcStompEndpointRegistry
        StompEndpointRegistry registry = new StompEndpointRegistry(
                handler, getTransportRegistration(), messageBrokerTaskScheduler());
        ApplicationContext applicationContext = getApplicationContext();
        if (applicationContext != null) {
            registry.setApplicationContext(applicationContext);
        }
        registerStompEndpoints(registry);
        return registry.getHandlerMapping();
    }

}
