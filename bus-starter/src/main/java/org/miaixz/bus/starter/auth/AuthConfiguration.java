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

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import org.miaixz.bus.auth.FabricX;
import org.miaixz.bus.auth.source.SourceAggregate;
import org.miaixz.bus.auth.source.SourceSuite;
import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.cache.Factory;
import org.miaixz.bus.cache.nimble.MemoryCache;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.spring.ContextBuilder;
import org.miaixz.bus.spring.boot.condition.ConditionalOnEnabled;
import org.miaixz.bus.starter.GeniusBuilder;
import org.miaixz.bus.starter.annotation.EnableAuth;

/**
 * Assembles the Spring-owned infrastructure required by the bus-auth Source runtime.
 * <p>
 * The configuration freezes one Source registration snapshot, creates one atomic authentication cache, and supplies
 * both to {@link AuthService}. It is active by default when bus-auth is present, can be disabled with
 * {@code bus.auth.enabled=false}, and can always be enabled explicitly with {@link EnableAuth}.
 * </p>
 *
 * @author Kimi Liu
 */
@EnableConfigurationProperties(AuthProperties.class)
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(name = "org.miaixz.bus.auth.cache.AuthCache")
@ConditionalOnEnabled(annotation = EnableAuth.class, prefix = GeniusBuilder.AUTH, matchIfMissing = true)
public class AuthConfiguration {

    /**
     * Bound root authentication properties.
     */
    private final AuthProperties properties;

    /**
     * Creates authentication auto-configuration from bound root properties.
     *
     * @param properties bound root authentication properties
     */
    public AuthConfiguration(AuthProperties properties) {
        this.properties = properties;
    }

    /**
     * Creates the MVC method-argument resolver for the current authenticated subject.
     *
     * @param contextBuilder context-scoped authentication state
     * @return authentication method-argument resolver
     */
    @Bean
    @ConditionalOnMissingBean(AuthMethodResolver.class)
    public AuthMethodResolver authMethodResolver(ContextBuilder contextBuilder) {
        return new AuthMethodResolver(contextBuilder);
    }

    /**
     * Creates the context-owned executor used by authentication cache and Runtime operations.
     * <p>
     * Applications may replace it with an {@link Executor} bean named {@code authExecutor}.
     * </p>
     *
     * @return context-owned authentication executor
     */
    @Bean(name = "authExecutor", destroyMethod = "shutdown")
    @ConditionalOnMissingBean(name = "authExecutor")
    public ExecutorService authExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }

    /**
     * Discovers and freezes protocol and Vendor Source registrations exactly once.
     *
     * @return immutable Source aggregate
     */
    @Bean
    @ConditionalOnMissingBean(SourceAggregate.class)
    public SourceAggregate authSources() {
        return SourceSuite.load().freeze();
    }

    /**
     * Creates the Spring facade over the shared Source aggregate and authentication infrastructure.
     *
     * @param environment protected Spring configuration environment
     * @param cache       atomic authentication cache
     * @param executor    authentication Runtime executor
     * @param sources     shared immutable Source aggregate
     * @return configured authentication service
     */
    @Bean
    @ConditionalOnMissingBean(AuthService.class)
    public AuthService authService(
            Environment environment,
            @Qualifier("authCache") CacheX<String, Object> cache,
            @Qualifier("authExecutor") Executor executor,
            SourceAggregate sources) {
        return new AuthService(environment, cache, executor, sources);
    }

    /**
     * Creates the atomic cache used by authentication security-state flows.
     * <p>
     * {@code bus.auth.cache.type} may select {@code memory}, {@code redis}, or {@code redis-cluster}. When absent, the
     * starter creates an atomic in-memory backend. Client secrets are never stored in this cache.
     * </p>
     *
     * @param executor executor used by asynchronous cache backends
     * @return authentication-specific atomic cache
     */
    @Bean(name = "authCache")
    @ConditionalOnMissingBean(name = "authCache")
    public CacheX<String, Object> authCache(@Qualifier("authExecutor") Executor executor) {
        if (hasAuthBackend()) {
            return new Factory().initialize(properties.getCache(), executor);
        }
        return new MemoryCache<>(FabricX.clock()::millis);
    }

    /**
     * Returns whether authentication defines a concrete dedicated cache backend.
     *
     * @return {@code true} when {@code bus.auth.cache.type} selects a concrete backend
     */
    private boolean hasAuthBackend() {
        if (properties.getCache() == null) {
            return false;
        }
        String type = properties.getCache().getType();
        return StringKit.isNotBlank(type) && !Normal.DEFAULT.equalsIgnoreCase(type.trim());
    }

}
