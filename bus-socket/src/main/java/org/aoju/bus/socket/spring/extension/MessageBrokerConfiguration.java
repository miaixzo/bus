package org.aoju.bus.socket.spring.extension;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.DelegatingWebSocketMessageBrokerConfiguration;
import org.springframework.web.socket.config.annotation.WebMvcStompEndpointRegistry;

/**
 * 代替{@link DelegatingWebSocketMessageBrokerConfiguration}，
 * 使用自定义的{@link StompEndpointRegistry}
 * 代替默认的{@link WebMvcStompEndpointRegistry}
 *
 * @author Kimi Liu
 * @version 3.5.0
 * @since JDK 1.8
 */
public class MessageBrokerConfiguration extends DelegatingWebSocketMessageBrokerConfiguration {

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
