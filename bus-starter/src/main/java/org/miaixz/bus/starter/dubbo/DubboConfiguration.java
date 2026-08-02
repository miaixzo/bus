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
package org.miaixz.bus.starter.dubbo;

import org.apache.dubbo.config.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.miaixz.bus.spring.boot.condition.ConditionalOnEnabled;
import org.miaixz.bus.starter.GeniusBuilder;
import org.miaixz.bus.starter.annotation.EnableDubbo;

/**
 * Configures Apache Dubbo service scanning from the bound Starter properties.
 * <p>
 * This class enables the {@link DubboProperties}, which in turn configures the necessary Dubbo beans for the
 * application context.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@EnableConfigurationProperties(value = { DubboProperties.class })
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(name = "org.apache.dubbo.config.ApplicationConfig")
@ConditionalOnEnabled(annotation = EnableDubbo.class, prefix = GeniusBuilder.DUBBO)
public class DubboConfiguration {

    /**
     * Bound dubbo configuration properties.
     */
    private final DubboProperties properties;

    /**
     * Stores the Dubbo scanning policy used to create framework configuration Beans.
     *
     * @param properties bound configuration properties
     */
    public DubboConfiguration(DubboProperties properties) {
        this.properties = properties;
    }

    /**
     * Creates the Dubbo application configuration bean.
     *
     * @return the configured Dubbo application settings
     */
    @Bean
    @ConditionalOnMissingBean(ApplicationConfig.class)
    @ConfigurationProperties(prefix = GeniusBuilder.DUBBO + ".application")
    public ApplicationConfig applicationConfig() {
        return new ApplicationConfig();
    }

    /**
     * Creates the Dubbo provider configuration bean.
     *
     * @return the configured Dubbo provider settings
     */
    @Bean
    @ConditionalOnMissingBean(ProviderConfig.class)
    @ConfigurationProperties(prefix = GeniusBuilder.DUBBO + ".provider")
    public ProviderConfig providerConfig() {
        return new ProviderConfig();
    }

    /**
     * Creates the Dubbo monitor configuration bean.
     *
     * @return the configured Dubbo monitor settings
     */
    @Bean
    @ConditionalOnMissingBean(MonitorConfig.class)
    @ConfigurationProperties(prefix = GeniusBuilder.DUBBO + ".monitor")
    public MonitorConfig monitorConfig() {
        return new MonitorConfig();
    }

    /**
     * Creates the Dubbo consumer configuration bean.
     *
     * @return the configured Dubbo consumer settings
     */
    @Bean
    @ConditionalOnMissingBean(ConsumerConfig.class)
    @ConfigurationProperties(prefix = GeniusBuilder.DUBBO + ".consumer")
    public ConsumerConfig consumerConfig() {
        return new ConsumerConfig();
    }

    /**
     * Creates the Dubbo registry configuration bean.
     *
     * @return the configured Dubbo registry settings
     */
    @Bean
    @ConditionalOnMissingBean(RegistryConfig.class)
    @ConfigurationProperties(prefix = GeniusBuilder.DUBBO + ".registry")
    public RegistryConfig registryConfig() {
        return new RegistryConfig();
    }

    /**
     * Creates the Dubbo protocol configuration bean.
     *
     * @return the configured Dubbo protocol settings
     */
    @Bean
    @ConditionalOnMissingBean(ProtocolConfig.class)
    @ConfigurationProperties(prefix = GeniusBuilder.DUBBO + ".protocol")
    public ProtocolConfig protocolConfig() {
        return new ProtocolConfig();
    }

}
