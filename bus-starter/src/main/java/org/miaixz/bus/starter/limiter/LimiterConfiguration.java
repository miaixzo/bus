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
package org.miaixz.bus.starter.limiter;

import java.util.List;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;

import org.miaixz.bus.core.xyz.ReflectKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.limiter.Context;
import org.miaixz.bus.limiter.Supplier;
import org.miaixz.bus.limiter.nimble.FallbackProvider;
import org.miaixz.bus.limiter.nimble.MethodProvider;
import org.miaixz.bus.limiter.nimble.RequestProvider;
import org.miaixz.bus.spring.boot.condition.ConditionalOnEnabled;
import org.miaixz.bus.starter.GeniusBuilder;
import org.miaixz.bus.starter.annotation.EnableLimiter;

/**
 * Configures limiter definition scanning and the application-scoped limiter service.
 * <p>
 * This class sets up the necessary beans for the limiter functionality, including the core service, strategy providers,
 * and the annotation scanner.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@EnableConfigurationProperties(value = { LimiterProperties.class })
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(name = "org.miaixz.bus.limiter.Provider")
@ConditionalOnEnabled(annotation = EnableLimiter.class, prefix = GeniusBuilder.LIMITER)
public class LimiterConfiguration {

    /**
     * Bound limiter configuration properties.
     */
    private final LimiterProperties properties;

    /**
     * Stores the limiter scanning and runtime policy used by the service Bean.
     *
     * @param properties bound configuration properties
     */
    public LimiterConfiguration(LimiterProperties properties) {
        this.properties = properties;
    }

    /**
     * Creates the {@link LimiterService} bean, which initializes the global limiter context.
     *
     * @return A new {@link LimiterService} instance.
     */
    @Bean
    @ConditionalOnMissingBean(LimiterService.class)
    public LimiterService limiterService() {
        Context context = new Context();
        context.setSeconds(Math.toIntExact(this.properties.getHotspotCacheDuration().toSeconds()));
        context.setLogger(this.properties.isLogger());
        context.setSupplier(this.properties.getSupplierClass());
        context.setExtension(this.properties.getExtension());
        return new LimiterService(context);
    }

    /**
     * Creates the {@link RequestProvider} bean, which is responsible for handling request-based limiting strategies.
     * <p>
     * It also configures a custom user identifier {@link Supplier} if one is specified in the properties.
     * </p>
     *
     * @return A configured {@link RequestProvider} instance.
     */
    @Bean
    @ConditionalOnMissingBean(RequestProvider.class)
    public RequestProvider requestProvider() {
        RequestProvider strategy = new RequestProvider();
        String implClassName = this.properties.getSupplierClass();
        // Check if a custom user identifier provider is specified.
        if (StringKit.isNotEmpty(implClassName)) {
            Supplier instance = ReflectKit.newInstance(implClassName);
            // Ensure it inherits from the correct abstract class.
            if (Supplier.class.isAssignableFrom(instance.getClass())) {
                strategy.setMarkSupplier(instance);
            }
        }
        return strategy;
    }

    /**
     * Creates the {@link FallbackProvider} bean, which handles circuit breaking (downgrade) strategies.
     *
     * @return A new {@link FallbackProvider} instance.
     */
    @Bean
    @ConditionalOnMissingBean(FallbackProvider.class)
    public FallbackProvider fallbackProvider() {
        return new FallbackProvider();
    }

    /**
     * Creates the {@link MethodProvider} bean, which handles hotspot method limiting strategies.
     *
     * @return A new {@link MethodProvider} instance.
     */
    @Bean
    @ConditionalOnMissingBean(MethodProvider.class)
    public MethodProvider methodProvider() {
        return new MethodProvider();
    }

    /**
     * Creates the {@link LimiterScanner} bean, which is a post-processor that scans for limiter annotations and creates
     * proxies for the annotated beans.
     *
     * @param beanFactory current Bean factory containing the application base-package registration
     * @return limiter scanner restricted to the application base packages
     */
    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    @ConditionalOnMissingBean(LimiterScanner.class)
    public static LimiterScanner scanner(BeanFactory beanFactory) {
        if (!AutoConfigurationPackages.has(beanFactory)) {
            throw new IllegalStateException("Limiter requires Spring Boot application base-package registration");
        }
        List<String> basePackages = AutoConfigurationPackages.get(beanFactory);
        if (basePackages.isEmpty()) {
            throw new IllegalStateException("Limiter requires at least one Spring Boot application base package");
        }
        return new LimiterScanner(basePackages);
    }

}
