/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
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
 * @since Java 17+
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
