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

import lombok.Getter;
import lombok.Setter;
import org.apache.dubbo.config.*;
import org.miaixz.bus.spring.GeniusBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Configuration properties for Apache Dubbo.
 * <p>
 * This class defines the top-level properties for Dubbo integration and also creates beans for the core Dubbo
 * configuration objects, binding them to nested properties under the {@code bus.dubbo} prefix.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Getter
@Setter
@ConfigurationProperties(prefix = GeniusBuilder.DUBBO)
public class DubboProperties {

    /**
     * The base packages to scan for Dubbo service interfaces.
     */
    protected String basePackages;

    /**
     * Type-safe alternative to {@link #basePackages} for specifying the packages to scan for classes annotated with
     * {@code @DubboService}.
     */
    protected String basePackageClasses;

    /**
     * Whether to allow binding to multiple Spring beans.
     */
    protected boolean multiple;

    /**
     * Creates the Dubbo {@link ApplicationConfig} bean.
     *
     * @return The application configuration bean, bound to properties at {@code bus.dubbo.application}.
     */
    @Bean
    @ConfigurationProperties(prefix = GeniusBuilder.DUBBO + ".application")
    public ApplicationConfig applicationConfig() {
        return new ApplicationConfig();
    }

    /**
     * Creates the Dubbo {@link ProviderConfig} bean.
     *
     * @return The provider configuration bean, bound to properties at {@code bus.dubbo.provider}.
     */
    @Bean
    @ConfigurationProperties(prefix = GeniusBuilder.DUBBO + ".provider")
    public ProviderConfig providerConfig() {
        return new ProviderConfig();
    }

    /**
     * Creates the Dubbo {@link MonitorConfig} bean.
     *
     * @return The monitor configuration bean, bound to properties at {@code bus.dubbo.monitor}.
     */
    @Bean
    @ConfigurationProperties(prefix = GeniusBuilder.DUBBO + ".monitor")
    public MonitorConfig monitorConfig() {
        return new MonitorConfig();
    }

    /**
     * Creates the Dubbo {@link ConsumerConfig} bean.
     *
     * @return The consumer configuration bean, bound to properties at {@code bus.dubbo.consumer}.
     */
    @Bean
    @ConfigurationProperties(prefix = GeniusBuilder.DUBBO + ".consumer")
    public ConsumerConfig consumerConfig() {
        return new ConsumerConfig();
    }

    /**
     * Creates the Dubbo {@link RegistryConfig} bean.
     *
     * @return The registry configuration bean, bound to properties at {@code bus.dubbo.registry}.
     */
    @Bean
    @ConfigurationProperties(prefix = GeniusBuilder.DUBBO + ".registry")
    public RegistryConfig registryConfig() {
        return new RegistryConfig();
    }

    /**
     * Creates the Dubbo {@link ProtocolConfig} bean.
     *
     * @return The protocol configuration bean, bound to properties at {@code bus.dubbo.protocol}.
     */
    @Bean
    @ConfigurationProperties(prefix = GeniusBuilder.DUBBO + ".protocol")
    public ProtocolConfig protocolConfig() {
        return new ProtocolConfig();
    }

}
