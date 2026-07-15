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

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import org.miaixz.bus.spring.GeniusBuilder;

/**
 * Auto-configuration for fabric communication services.
 * <p>
 * This class enables {@link FabricProperties} and creates protocol quick-service beans for configured fabric server
 * capabilities.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@EnableConfigurationProperties(value = { FabricProperties.class })
@ConditionalOnProperty(prefix = GeniusBuilder.FABRIC, name = "enabled", havingValue = "true", matchIfMissing = true)
public class FabricConfiguration {

    /**
     * Constructs a new FabricConfiguration instance.
     */
    public FabricConfiguration() {
        // No initialization required.
    }

    /**
     * Creates the {@link SocketQuickService} bean.
     * <p>
     * This bean starts and stops the configured fabric socket server with the Spring application context. The bean is
     * only created when no other socket quick service bean is already present.
     * </p>
     *
     * @param properties fabric configuration properties
     * @return socket quick service
     */
    @Bean(initMethod = "start", destroyMethod = "stop")
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = GeniusBuilder.FABRIC
            + ".socket", name = "enabled", havingValue = "true", matchIfMissing = true)
    public SocketQuickService socketQuickService(final FabricProperties properties) {
        return new SocketQuickService(properties);
    }

    /**
     * Creates the {@link WebSocketQuickService} bean.
     * <p>
     * This bean starts and stops the configured fabric WebSocket server with the Spring application context. The bean
     * is only created when the WebSocket service is explicitly enabled.
     * </p>
     *
     * @param properties fabric configuration properties
     * @return WebSocket quick service
     */
    @Bean(initMethod = "start", destroyMethod = "stop")
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = GeniusBuilder.FABRIC + ".websocket", name = "enabled", havingValue = "true")
    public WebSocketQuickService webSocketQuickService(final FabricProperties properties) {
        return new WebSocketQuickService(properties);
    }

}
