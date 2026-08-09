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
package org.miaixz.bus.starter.auth;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.cache.Factory;
import org.miaixz.bus.cache.nimble.MemoryCache;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.spring.ContextBuilder;
import org.miaixz.bus.spring.boot.condition.ConditionalOnEnabled;
import org.miaixz.bus.starter.GeniusBuilder;
import org.miaixz.bus.starter.annotation.EnableAuth;

/**
 * Configures authorization services and protected-method resolution.
 * <p>
 * This class creates and configures the following main components:
 * <ul>
 * <li>{@link AuthService} - The authorization service provider factory for creating various third-party authorization
 * services.</li>
 * <li>{@link CacheX} - The context-owned authorization cache implementation.</li>
 * </ul>
 * <p>
 * <strong>Configuration Example (in {@code application.yml}):</strong>
 *
 * <pre>{@code
 * bus:
 *   auth:
 *     cache:
 *       type: redis
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
 */
@EnableConfigurationProperties(value = { AuthProperties.class })
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(name = "org.miaixz.bus.auth.cache.AuthCache")
@ConditionalOnEnabled(annotation = EnableAuth.class, prefix = GeniusBuilder.AUTH)
public class AuthConfiguration {

    /**
     * Bound auth configuration properties.
     */
    private final AuthProperties properties;

    /**
     * Stores the properties used to construct authentication providers and services.
     *
     * @param properties authorization properties
     */
    public AuthConfiguration(AuthProperties properties) {
        this.properties = properties;
    }

    /**
     * Creates the MVC adapter that resolves the current authenticated principal.
     *
     * @param contextBuilder context-scoped authentication state
     * @return the Auth method argument resolver
     */
    @Bean
    @ConditionalOnMissingBean(AuthMethodResolver.class)
    public AuthMethodResolver authMethodResolver(ContextBuilder contextBuilder) {
        return new AuthMethodResolver(contextBuilder);
    }

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
    @ConditionalOnMissingBean(AuthService.class)
    public AuthService authProviderFactory(@Qualifier("authCache") CacheX<String, Object> cache) {
        return new AuthService(this.properties, cache);
    }

    /**
     * Creates the authorization cache implementation bean.
     * <p>
     * Resolution order:
     * <ul>
     * <li>If {@code bus.auth.cache.*} configures a concrete backend type, create an auth-specific cache through
     * {@link Factory}.</li>
     * <li>If no auth-specific backend is configured, create a context-owned in-memory cache.</li>
     * </ul>
     *
     * @return authorization cache implementation
     */
    @Bean("authCache")
    @ConditionalOnMissingBean(name = "authCache")
    public CacheX<String, Object> authCache() {
        if (hasAuthBackend()) {
            return new Factory().initialize(this.properties.getCache());
        }
        return new MemoryCache<>();
    }

    /**
     * Returns whether auth defines its own concrete cache backend instead of reusing the shared default cache.
     *
     * @return {@code true} when auth should initialize a dedicated backend
     */
    private boolean hasAuthBackend() {
        if (this.properties.getCache() == null) {
            return false;
        }
        String type = this.properties.getCache().getType();
        return StringKit.isNotBlank(type) && !"default".equalsIgnoreCase(type.trim());
    }

}
