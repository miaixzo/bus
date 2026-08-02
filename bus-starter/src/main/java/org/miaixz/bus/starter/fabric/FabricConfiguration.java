/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.starter.fabric;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.miaixz.bus.starter.GeniusBuilder;

/**
 * Configures TCP and WebSocket fabric communication services.
 * <p>
 * This class enables {@link FabricProperties} and creates protocol quick-service beans for configured fabric server
 * capabilities.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@EnableConfigurationProperties(value = { FabricProperties.class })
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(name = "org.miaixz.bus.fabric.Fabric")
@ConditionalOnProperty(prefix = GeniusBuilder.FABRIC, name = "enabled", havingValue = "true", matchIfMissing = false)
public class FabricConfiguration {

    /**
     * Bound fabric configuration properties.
     */
    private final FabricProperties properties;

    /**
     * Stores the transport properties used to construct TCP and WebSocket services.
     *
     * @param properties bound configuration properties
     */
    public FabricConfiguration(FabricProperties properties) {
        this.properties = properties;
    }

    /**
     * Creates the {@link SocketQuickService} bean.
     * <p>
     * This bean starts and stops the configured fabric socket server with the Spring application context. The bean is
     * only created when no other socket quick service bean is already present.
     * </p>
     *
     * @return socket quick service
     */
    @Bean(initMethod = "start", destroyMethod = "stop")
    @ConditionalOnMissingBean(SocketQuickService.class)
    @ConditionalOnProperty(prefix = GeniusBuilder.FABRIC
            + ".socket", name = "enabled", havingValue = "true", matchIfMissing = true)
    public SocketQuickService socketQuickService() {
        return new SocketQuickService(this.properties);
    }

    /**
     * Creates the {@link WebSocketQuickService} bean.
     * <p>
     * This bean starts and stops the configured fabric WebSocket server with the Spring application context. The bean
     * is only created when the WebSocket service is explicitly enabled.
     * </p>
     *
     * @return WebSocket quick service
     */
    @Bean(initMethod = "start", destroyMethod = "stop")
    @ConditionalOnMissingBean(WebSocketQuickService.class)
    @ConditionalOnProperty(prefix = GeniusBuilder.FABRIC
            + ".websocket", name = "enabled", havingValue = "true", matchIfMissing = false)
    public WebSocketQuickService webSocketQuickService() {
        return new WebSocketQuickService(this.properties);
    }

}
