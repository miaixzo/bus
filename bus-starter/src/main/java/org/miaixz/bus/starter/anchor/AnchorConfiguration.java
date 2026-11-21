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
package org.miaixz.bus.starter.anchor;

import jakarta.servlet.Filter;
import jakarta.servlet.Servlet;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.cortex.guard.token.TrustToken;
import org.miaixz.bus.spring.boot.condition.ConditionalOnEnabled;
import org.miaixz.bus.starter.GeniusBuilder;
import org.miaixz.bus.starter.annotation.EnableAnchor;
import org.miaixz.bus.vortex.guard.AnchorGuard;
import org.miaixz.bus.vortex.guard.AnchorVerifier;

/**
 * Registers inbound caller verification and outbound identity injection for internal service calls.
 * <p>
 * Active only when the application declares {@code @EnableAnchor} or sets {@code bus.anchor.enabled=true}. The
 * {@code in} and {@code out} members select the direction; an explicit {@code false} disables that direction even when
 * the secondary property is true.
 * </p>
 *
 * @author Kimi Liu
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(AnchorProperties.class)
@ConditionalOnEnabled(annotation = EnableAnchor.class, prefix = GeniusBuilder.ANCHOR)
public class AnchorConfiguration {

    /**
     * Rejects a missing deployment value.
     *
     * @param value configured value
     * @param name  public property name
     */
    private static void required(final String value, final String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(name + " is required when the direction is enabled");
        }
    }

    /**
     * Registers the inbound caller identity filter for Servlet applications.
     *
     * @author Kimi Liu
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnEnabled(annotation = EnableAnchor.class, prefix = GeniusBuilder.ANCHOR, member = "in")
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    static class Inbound {

        /**
         * Creates the rotating key holder when inbound enforcement is enabled.
         *
         * @param properties deployment settings
         * @return key holder deriving one effective key per rotation period
         */
        @Bean
        @ConditionalOnMissingBean
        @ConditionalOnProperty(prefix = GeniusBuilder.ANCHOR, name = "inbound.enabled", havingValue = "true", matchIfMissing = true)
        AnchorKeys inboundKeys(final AnchorProperties properties) {
            required(properties.inbound().subjects(), "bus.anchor.inbound.subjects");
            required(properties.inbound().audience(), "bus.anchor.inbound.audience");
            return AnchorKeys.of(properties);
        }

        /**
         * Installs the inbound verifier used by the gateway control plane.
         *
         * @param keys rotating key holder
         * @return verifier installer
         */
        @Bean
        InitializingBean inboundVerifier(final AnchorKeys keys) {
            return () -> AnchorVerifier.install(keys::accepts);
        }

        /**
         * Registers the inbound filter for Servlet applications.
         *
         * @param properties deployment settings
         * @param keys       rotating key holder
         * @return filter registration
         */
        @Bean
        @ConditionalOnClass({ Servlet.class, Filter.class })
        @ConditionalOnProperty(prefix = GeniusBuilder.ANCHOR, name = "inbound.enabled", havingValue = "true", matchIfMissing = true)
        FilterRegistrationBean<ServiceAnchorFilter> serviceAnchorFilter(
                final AnchorProperties properties,
                final AnchorKeys keys) {
            final FilterRegistrationBean<ServiceAnchorFilter> registration = new FilterRegistrationBean<>();
            registration.setFilter(new ServiceAnchorFilter(properties, keys));
            registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 20);
            registration.addUrlPatterns(Symbol.SLASH + Symbol.STAR);
            return registration;
        }

    }

    /**
     * Builds and installs the outbound service identity for reactive gateways.
     *
     * @author Kimi Liu
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnEnabled(annotation = EnableAnchor.class, prefix = GeniusBuilder.ANCHOR, member = "out")
    static class Outbound {

        /**
         * Creates the rotating key holder.
         *
         * @param properties deployment settings
         * @return key holder deriving one effective key per rotation period
         */
        @Bean
        @ConditionalOnMissingBean
        AnchorKeys outboundKeys(final AnchorProperties properties) {
            required(properties.outbound().subject(), "bus.anchor.outbound.subject");
            required(properties.outbound().apps(), "bus.anchor.outbound.apps");
            return AnchorKeys.of(properties);
        }

        /**
         * Installs the outbound identity into the gateway process.
         *
         * @param keys       rotating key holder
         * @param properties deployment settings
         * @return startup callback
         */
        @Bean
        InitializingBean anchorInstaller(final AnchorKeys keys, final AnchorProperties properties) {
            final AnchorProperties.Outbound outbound = properties.outbound();
            final String registryAudience = outbound.registryAudience();
            if (registryAudience != null && !registryAudience.isBlank()) {
                TrustToken.install(() -> keys.token(registryAudience));
            }
            return () -> AnchorGuard.install(keys::token, outbound.header(), outbound.apps(), outbound.paths());
        }

    }

}
