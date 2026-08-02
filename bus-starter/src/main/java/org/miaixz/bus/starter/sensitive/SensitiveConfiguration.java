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
package org.miaixz.bus.starter.sensitive;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.sensitive.Sanitizer;
import org.miaixz.bus.starter.GeniusBuilder;

/**
 * Activates sensitive-data services independently of the application transport type.
 * <p>
 * When the feature is enabled, logging protection is installed regardless of transport type. The nested Servlet
 * configuration creates request and response advice only for Spring MVC applications.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@EnableConfigurationProperties(value = { SensitiveProperties.class })
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(Sanitizer.class)
@ConditionalOnProperty(prefix = GeniusBuilder.SENSITIVE, name = Normal.ENABLED, havingValue = Normal.TRUE, matchIfMissing = false)
@Import(SensitiveConfiguration.ServletConfiguration.class)
public class SensitiveConfiguration {

    /**
     * Initializes the transport-neutral sensitive-data Bean definitions.
     */
    public SensitiveConfiguration() {
        // No initialization required.
    }

    /**
     * Creates the structured log sanitizer that removes protected values from logging arguments.
     *
     * @return sensitive log operator
     */
    @Bean
    @ConditionalOnMissingBean(Sanitizer.class)
    Sanitizer sanitizer() {
        return new Sanitizer();
    }

    /**
     * Binds the sensitive operator to the logger executor for this application context.
     *
     * @param sanitizer sensitive log operator
     * @return context-scoped sensitive binding
     */
    @Bean
    @ConditionalOnMissingBean(SensitiveBinding.class)
    SensitiveBinding sensitiveBinding(Sanitizer sanitizer) {
        return new SensitiveBinding(sanitizer);
    }

    /**
     * Configures sensitive request and response advice for Servlet-based Spring MVC applications.
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    @ConditionalOnClass(name = { "org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdvice",
            "org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice",
            "org.springframework.web.bind.annotation.ControllerAdvice" })
    static class ServletConfiguration {

        /**
         * Bound sensitive configuration properties.
         */
        private final SensitiveProperties properties;

        /**
         * Creates the Servlet integration from bound sensitive properties.
         *
         * @param properties bound sensitive properties
         */
        ServletConfiguration(SensitiveProperties properties) {
            this.properties = properties;
        }

        /**
         * Creates request-body advice for decrypting protected controller input.
         *
         * @return request-body advice using the current sensitive rules
         */
        @Bean
        @ConditionalOnMissingBean(SensitiveRequestBodyAdvice.class)
        SensitiveRequestBodyAdvice sensitiveRequestBodyAdvice() {
            return new SensitiveRequestBodyAdvice(this.properties);
        }

        /**
         * Creates response-body advice for encrypting or masking protected controller output.
         *
         * @return response-body advice using the current sensitive rules
         */
        @Bean
        @ConditionalOnMissingBean(SensitiveResponseBodyAdvice.class)
        SensitiveResponseBodyAdvice sensitiveResponseBodyAdvice() {
            return new SensitiveResponseBodyAdvice(this.properties);
        }

    }

}
