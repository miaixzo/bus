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
package org.miaixz.bus.starter.notify;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.miaixz.bus.spring.boot.condition.ConditionalOnEnabled;
import org.miaixz.bus.starter.GeniusBuilder;
import org.miaixz.bus.starter.annotation.EnableNotify;

/**
 * Configures notification providers and the application-scoped notification service.
 * <p>
 * This class creates and configures the notification service provider factory, which manages and creates various
 * message notification services.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@EnableConfigurationProperties(value = { NotifyProperties.class })
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(name = "org.miaixz.bus.notify.Provider")
@ConditionalOnEnabled(annotation = EnableNotify.class, prefix = GeniusBuilder.NOTIFY)
public class NotifyConfiguration {

    /**
     * Bound notify configuration properties.
     */
    private final NotifyProperties properties;

    /**
     * Stores the channel definitions used to construct the notification registry and service.
     *
     * @param properties bound configuration properties
     */
    public NotifyConfiguration(NotifyProperties properties) {
        this.properties = properties;
    }

    /**
     * Creates the notification service provider factory bean.
     * <p>
     * This method creates a {@link NotifyService} instance, which is used to manage and create various notification
     * service providers. The instance is initialized with the application's configuration properties.
     *
     * @return A configured instance of the notification service provider factory.
     */
    @Bean(destroyMethod = "close")
    @ConditionalOnMissingBean(NotifyService.class)
    NotifyService notifyService() {
        return new NotifyService(this.properties);
    }

}
