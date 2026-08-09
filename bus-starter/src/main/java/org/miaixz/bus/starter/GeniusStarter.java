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
package org.miaixz.bus.starter;

import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;

import org.miaixz.bus.spring.ContextBuilder;
import org.miaixz.bus.spring.ContextDecorator;
import org.miaixz.bus.spring.ContextManager;
import org.miaixz.bus.spring.ContextProvider;
import org.miaixz.bus.spring.SpringBuilder;
import org.miaixz.bus.spring.bean.BeanMetadata;
import org.miaixz.bus.spring.bean.BeanProvider;
import org.miaixz.bus.spring.bean.BeanRegistry;
import org.miaixz.bus.spring.bean.EnvironmentResolver;
import org.miaixz.bus.spring.bean.ProviderRegistry;
import org.miaixz.bus.spring.bean.SpringContext;

/**
 * Registers shared Spring infrastructure and discovers framework components outside the Starter configuration layer.
 * <p>
 * Starter packages are excluded from component scanning because every Starter feature has an explicit
 * auto-configuration or {@code @EnableXxx} import entry. This prevents disabled feature configurations from being
 * discovered as ordinary components while retaining discovery for components owned by other Bus modules.
 *
 * @author Kimi Liu
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@Configuration(proxyBeanMethods = false)
@ComponentScan(value = "org.miaixz.**", excludeFilters = @ComponentScan.Filter(type = FilterType.REGEX, pattern = "org\\.miaixz\\.bus\\.starter(?:\\..*)?"))
public class GeniusStarter {

    /**
     * Initializes the shared Spring infrastructure used by Starter features.
     */
    public GeniusStarter() {
        // No initialization required.
    }

    /**
     * Registers the holder for the current Spring application context.
     *
     * @param applicationContext application context
     * @return the application-context holder
     */
    @Bean
    @ConditionalOnMissingBean(SpringContext.class)
    SpringContext springContext(ApplicationContext applicationContext) {
        return new SpringContext(applicationContext);
    }

    /**
     * Registers the read-only Bean lookup service.
     *
     * @param springContext spring context
     * @return the read-only Bean lookup service
     */
    @Bean
    @ConditionalOnMissingBean(BeanProvider.class)
    BeanProvider beanProvider(SpringContext springContext) {
        return new BeanProvider(springContext);
    }

    /**
     * Registers the Bean definition and singleton registration service.
     *
     * @param springContext spring context
     * @return the Bean registration service
     */
    @Bean
    @ConditionalOnMissingBean(BeanRegistry.class)
    BeanRegistry beanRegistry(SpringContext springContext) {
        return new BeanRegistry(springContext);
    }

    /**
     * Registers the side-effect-free Bean metadata inspection service.
     *
     * @return the Bean metadata inspection service
     */
    @Bean
    @ConditionalOnMissingBean(BeanMetadata.class)
    BeanMetadata beanMetadata() {
        return new BeanMetadata();
    }

    /**
     * Registers the environment property and profile resolution service.
     *
     * @param environment Spring environment
     * @return the environment property resolution service
     */
    @Bean
    @ConditionalOnMissingBean(EnvironmentResolver.class)
    EnvironmentResolver environmentResolver(Environment environment) {
        return new EnvironmentResolver(environment);
    }

    /**
     * Registers the context-owned, ordered Provider registry.
     *
     * @param springContext spring context
     * @param beanProvider  Spring Bean provider
     * @return the context-owned Provider registry
     */
    @Bean
    @ConditionalOnMissingBean(ProviderRegistry.class)
    ProviderRegistry providerRegistry(SpringContext springContext, BeanProvider beanProvider) {
        return new ProviderRegistry(springContext, beanProvider);
    }

    /**
     * Registers the isolated request-context lifecycle manager.
     *
     * @return the request-context lifecycle manager
     */
    @Bean
    @ConditionalOnMissingBean(ContextManager.class)
    ContextManager contextManager() {
        return new ContextManager();
    }

    /**
     * Registers the facade used to build and propagate request context.
     *
     * @param contextManager   context manager
     * @param contextProviders context providers
     * @return the request-context builder
     */
    @Bean
    @ConditionalOnMissingBean(ContextBuilder.class)
    ContextBuilder contextBuilder(ContextManager contextManager, List<ContextProvider> contextProviders) {
        return new ContextBuilder(contextManager, contextProviders);
    }

    /**
     * Creates the stable Spring facade from collaborators owned by the same application context.
     *
     * @param springContext       application-context holder
     * @param beanProvider        read-only Bean lookup service
     * @param beanRegistry        Bean mutation service
     * @param beanMetadata        Bean metadata inspector
     * @param environmentResolver environment property service
     * @param providerRegistry    ordered provider registry
     * @return application-context-scoped Spring facade
     */
    @Bean
    @ConditionalOnMissingBean(SpringBuilder.class)
    SpringBuilder springBuilder(
            SpringContext springContext,
            BeanProvider beanProvider,
            BeanRegistry beanRegistry,
            BeanMetadata beanMetadata,
            EnvironmentResolver environmentResolver,
            ProviderRegistry providerRegistry) {
        return new SpringBuilder(springContext, beanProvider, beanRegistry, beanMetadata, environmentResolver,
                providerRegistry);
    }

    /**
     * Registers the task decorator that propagates request context across executors.
     *
     * @param contextBuilder application-context-scoped runtime context facade
     * @return the task decorator that propagates request context
     */
    @Bean
    @ConditionalOnMissingBean(ContextDecorator.class)
    ContextDecorator contextDecorator(ContextBuilder contextBuilder) {
        return new ContextDecorator(contextBuilder);
    }

}
