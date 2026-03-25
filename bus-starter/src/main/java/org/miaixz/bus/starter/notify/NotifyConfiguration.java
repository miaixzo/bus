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

import jakarta.annotation.Resource;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration class for message notification, responsible for setting up notification-related beans.
 * <p>
 * This class creates and configures the notification service provider factory, which manages and creates various
 * message notification services.
 *
 * <p>
 * <strong>Usage Example:</strong>
 * 
 * <pre>{@code
 * // In application.yml:
 * bus:
 *   notify:
 *     # Notification-related configurations for different providers
 *
 * // In your code:
 * &#64;Autowired
 * private NotifyService notifyService;
 *
 * // Get a specific provider and send a notification
 * Provider emailProvider = notifyService.require(Registry.GENERIC_EDM);
 * // emailProvider.send(...);
 * }</pre>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@EnableConfigurationProperties(value = { NotifyProperties.class })
public class NotifyConfiguration {

    /**
     * Injected notification configuration properties, containing settings for various notification components.
     * Automatically injected via the {@link EnableConfigurationProperties} annotation.
     */
    @Resource
    private NotifyProperties properties;

    /**
     * Creates the notification service provider factory bean.
     * <p>
     * This method creates a {@link NotifyService} instance, which is used to manage and create various notification
     * service providers. The instance is initialized with the application's configuration properties.
     *
     * @return A configured instance of the notification service provider factory.
     */
    @Bean
    public NotifyService notifyProviderFactory() {
        return new NotifyService(this.properties);
    }

}
