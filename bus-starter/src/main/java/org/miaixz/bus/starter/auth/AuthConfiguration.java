/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.starter.auth;

import jakarta.annotation.Resource;
import org.miaixz.bus.auth.cache.AuthCache;
import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.spring.GeniusBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration class for authorization, responsible for setting up authorization-related beans.
 * <p>
 * This class creates and configures the following main components:
 * <ul>
 * <li>{@link AuthService} - The authorization service provider factory for creating various third-party authorization
 * services.</li>
 * <li>{@link CacheX} - The authorization cache implementation, using {@link AuthCache} as the default.</li>
 * </ul>
 * <p>
 * <strong>Configuration Example (in {@code application.yml}):</strong>
 * 
 * <pre>{@code
 * bus:
 *   auth:
 *     cache:
 *       type: default  # Use the default cache
 * }
 * </pre>
 * <p>
 * <strong>Usage in Code:</strong>
 * 
 * <pre>{@code
 * 
 * &#64;Autowired
 * private AuthService authService;
 *
 * // Get the GitHub authorization provider
 * Provider provider = authService.require(Registry.GITHUB);
 * }</pre>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@EnableConfigurationProperties(value = { AuthProperties.class })
public class AuthConfiguration {

    /**
     * Injected authorization configuration properties, containing settings for various authorization components.
     * Automatically injected via the {@link EnableConfigurationProperties} annotation.
     */
    @Resource
    AuthProperties properties;

    /**
     * Creates the authorization service provider factory bean.
     * <p>
     * This method creates an {@link AuthService} instance, which is used to manage and create various third-party
     * authorization service providers. The instance is initialized with the provided cache implementation and
     * configuration properties.
     * </p>
     *
     * @param cache The cache implementation for storing temporary data during the authorization process.
     * @return A configured instance of the authorization service provider factory.
     */
    @Bean
    public AuthService authProviderFactory(CacheX cache) {
        return new AuthService(this.properties, cache);
    }

    /**
     * Creates the default authorization cache implementation bean.
     * <p>
     * This method creates a default cache implementation under the following conditions:
     * <ul>
     * <li>No custom {@link CacheX} bean exists in the container.</li>
     * <li>The cache type in the configuration properties is set to "default" (which is the default setting).</li>
     * </ul>
     *
     * @return The default authorization cache implementation instance.
     */
    @Bean
    @ConditionalOnMissingBean(CacheX.class)
    @ConditionalOnProperty(name = GeniusBuilder.AUTH + ".cache.type", havingValue = "default", matchIfMissing = true)
    public CacheX authCache() {
        return AuthCache.INSTANCE;
    }

}
