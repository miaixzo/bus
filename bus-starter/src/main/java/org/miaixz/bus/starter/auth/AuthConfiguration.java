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
