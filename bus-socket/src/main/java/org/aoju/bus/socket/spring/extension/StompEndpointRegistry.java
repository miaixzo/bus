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

import org.aoju.bus.core.lang.exception.SocketException;
import org.aoju.bus.logger.Logger;
import org.aoju.bus.socket.spring.intercept.FromClientInterceptor;
import org.aoju.bus.socket.spring.intercept.ToClientInterceptor;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.util.MultiValueMap;
import org.springframework.web.HttpRequestHandler;
import org.springframework.web.servlet.handler.AbstractHandlerMapping;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.StompWebSocketEndpointRegistration;
import org.springframework.web.socket.config.annotation.WebMvcStompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebMvcStompWebSocketEndpointRegistration;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;
import org.springframework.web.socket.handler.WebSocketHandlerDecorator;
import org.springframework.web.socket.messaging.StompSubProtocolErrorHandler;
import org.springframework.web.socket.messaging.SubProtocolWebSocketHandler;
import org.springframework.web.socket.server.support.WebSocketHandlerMapping;
import org.springframework.web.util.UrlPathHelper;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 代替{@link WebMvcStompEndpointRegistry}
 *
 * @author Kimi Liu
 * @version 5.9.3
 * @since JDK 1.8+
 */
public class StompEndpointRegistry extends WebMvcStompEndpointRegistry {

    // 代替父类中的属性
    private final WebSocketHandler webSocketHandler;

    private final TaskScheduler sockJsScheduler;
    private final SubProtocolWebSocketHandler subProtocolWebSocketHandler;
    // 使用CustomizeStompSubProtocolHandler代替StompSubProtocolHandler
    private final StompSubProtocolHandler stompHandler;
    private final List<WebMvcStompWebSocketEndpointRegistration> registrations = new ArrayList<>();
    private int order = 1;
    private UrlPathHelper urlPathHelper;

    public StompEndpointRegistry(WebSocketHandler webSocketHandler, WebSocketTransportRegistration transportRegistration, TaskScheduler defaultSockJsTaskScheduler) {
        super(webSocketHandler, transportRegistration, defaultSockJsTaskScheduler);
        this.webSocketHandler = webSocketHandler;
        this.subProtocolWebSocketHandler = unwrapSubProtocolWebSocketHandler(webSocketHandler);
        // 使用类反射获取transportRegistration中的属性
        Integer sendTimeLimit = getTransportRegistrationValue(transportRegistration, "sendTimeLimit");
        Integer sendBufferSizeLimit = getTransportRegistrationValue(transportRegistration, "sendBufferSizeLimit");
        Integer timeToFirstMessage = getTransportRegistrationValue(transportRegistration, "timeToFirstMessage");
        Integer messageSizeLimit = getTransportRegistrationValue(transportRegistration, "messageSizeLimit");
        if (sendTimeLimit != null) {
            this.subProtocolWebSocketHandler.setSendTimeLimit(sendTimeLimit);
        }
        if (sendBufferSizeLimit != null) {
            this.subProtocolWebSocketHandler.setSendBufferSizeLimit(sendBufferSizeLimit);
        }
        if (timeToFirstMessage != null) {
            this.subProtocolWebSocketHandler.setTimeToFirstMessage(timeToFirstMessage);
        }
        // 替换为自定义的stompHandler
        this.stompHandler = new StompSubProtocolHandler();
        if (messageSizeLimit != null) {
            this.stompHandler.setMessageSizeLimit(messageSizeLimit);
        }
        this.sockJsScheduler = defaultSockJsTaskScheduler;
    }

    private static SubProtocolWebSocketHandler unwrapSubProtocolWebSocketHandler(WebSocketHandler handler) {
        WebSocketHandler actual = WebSocketHandlerDecorator.unwrap(handler);
        if (!(actual instanceof SubProtocolWebSocketHandler)) {
            throw new IllegalArgumentException("No SubProtocolWebSocketHandler in " + handler);
        }
        return (SubProtocolWebSocketHandler) actual;
    }

    // 使用反射获取WebSocketTransportRegistration实例的属性
    private static Integer getTransportRegistrationValue(WebSocketTransportRegistration transportRegistration, String fieldName) {
        Integer ret = null;
        try {
            Field limitField = WebSocketTransportRegistration.class.getDeclaredField(fieldName);
            limitField.setAccessible(true);
            Object value = limitField.get(transportRegistration);
            if (value != null) {
                ret = (Integer) value;
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            Logger.error(e.getMessage(), e);
            throw new SocketException("获取" + fieldName + "的值出错", e);
        }
        return ret;
    }

    @Override
    public StompWebSocketEndpointRegistration addEndpoint(String... paths) {
        this.subProtocolWebSocketHandler.addProtocolHandler(this.stompHandler);
        WebMvcStompWebSocketEndpointRegistration registration =
                new WebMvcStompWebSocketEndpointRegistration(paths, this.webSocketHandler, this.sockJsScheduler);
        this.registrations.add(registration);
        return registration;
    }

    @Override
    protected int getOrder() {
        return this.order;
    }

    @Override
    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    protected UrlPathHelper getUrlPathHelper() {
        return this.urlPathHelper;
    }

    @Override
    public void setUrlPathHelper(UrlPathHelper urlPathHelper) {
        this.urlPathHelper = urlPathHelper;
    }

    @Override
    public StompEndpointRegistry setErrorHandler(StompSubProtocolErrorHandler errorHandler) {
        this.stompHandler.setErrorHandler(errorHandler);
        return this;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        super.setApplicationContext(applicationContext);
    }

    public AbstractHandlerMapping getHandlerMapping() {
        Map<String, Object> urlMap = new LinkedHashMap<>();
        for (WebMvcStompWebSocketEndpointRegistration registration : this.registrations) {
            MultiValueMap<HttpRequestHandler, String> mappings = registration.getMappings();
            mappings.forEach((httpHandler, patterns) -> {
                for (String pattern : patterns) {
                    urlMap.put(pattern, httpHandler);
                }
            });
        }
        WebSocketHandlerMapping hm = new WebSocketHandlerMapping();
        hm.setUrlMap(urlMap);
        hm.setOrder(this.order);
        if (this.urlPathHelper != null) {
            hm.setUrlPathHelper(this.urlPathHelper);
        }
        return hm;
    }

    // 增加消息拦截器
    public StompEndpointRegistry addFromClientInterceptor(FromClientInterceptor interceptor) {
        this.stompHandler.addFromClientInterceptor(interceptor);
        return this;
    }

    // 增加消息拦截器
    public StompEndpointRegistry addToClientInterceptor(ToClientInterceptor interceptor) {
        this.stompHandler.addToClientInterceptor(interceptor);
        return this;
    }

}
