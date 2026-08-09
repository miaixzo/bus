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
package org.miaixz.bus.starter.office;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.miaixz.bus.office.Provider;
import org.miaixz.bus.office.Registry;
import org.miaixz.bus.office.builtin.LocalOfficeProvider;
import org.miaixz.bus.office.builtin.OnlineOfficeProvider;
import org.miaixz.bus.spring.boot.condition.ConditionalOnEnabled;
import org.miaixz.bus.starter.GeniusBuilder;
import org.miaixz.bus.starter.annotation.EnableOffice;

/**
 * Configures online document preview and conversion services.
 * <p>
 * This class enables the {@link OfficeProperties} and configures the necessary beans for both local and online document
 * conversion, conditional on their presence in the classpath.
 *
 * @author Kimi Liu
 */
@ConditionalOnClass({ LocalOfficeProvider.class, OnlineOfficeProvider.class })
@EnableConfigurationProperties(value = { OfficeProperties.class })
@Configuration(proxyBeanMethods = false)
@ConditionalOnEnabled(annotation = EnableOffice.class, prefix = GeniusBuilder.OFFICE)
public class OfficeConfiguration {

    /**
     * Bound office configuration properties.
     */
    private final OfficeProperties properties;

    /**
     * Stores the document-provider definitions used to construct the Office registry and service.
     *
     * @param properties bound configuration properties
     */
    public OfficeConfiguration(OfficeProperties properties) {
        this.properties = properties;
    }

    /**
     * Creates the provider registry owned by the current application context.
     *
     * @return the context-local Office registry
     */
    @Bean(destroyMethod = "close")
    @ConditionalOnMissingBean(Registry.class)
    public Registry officeRegistry() {
        return new Registry();
    }

    /**
     * Creates the default local conversion provider.
     *
     * @return local conversion provider
     */
    @Bean("localOfficeProvider")
    @ConditionalOnMissingBean(name = "localOfficeProvider")
    public Provider localOfficeProvider() {
        return new LocalOfficeProvider();
    }

    /**
     * Creates the default online conversion provider.
     *
     * @return online conversion provider
     */
    @Bean("onlineOfficeProvider")
    @ConditionalOnMissingBean(name = "onlineOfficeProvider")
    public Provider onlineOfficeProvider() {
        return new OnlineOfficeProvider();
    }

    /**
     * Registers the context-local Office providers in the context-local registry.
     *
     * @param registry       context-local registry
     * @param localProvider  local conversion provider
     * @param onlineProvider online conversion provider
     * @return provider registration adapter
     */
    @Bean
    @ConditionalOnMissingBean(OfficeService.class)
    OfficeService officeService(
            Registry registry,
            @Qualifier("localOfficeProvider") Provider localProvider,
            @Qualifier("onlineOfficeProvider") Provider onlineProvider) {
        return new OfficeService(registry, localProvider, onlineProvider);
    }

}
