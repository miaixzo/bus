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
package org.miaixz.bus.starter.wrapper.converter;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.miaixz.bus.extra.json.JsonProvider;
import org.miaixz.bus.extra.json.JsonTypeFilter;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.spring.web.converter.JsonMessageConverter;
import org.miaixz.bus.spring.web.converter.JsonWebMvcConfigurer;
import org.miaixz.bus.spring.web.converter.MessageConverterRegistrar;
import org.miaixz.bus.spring.web.converter.TextWebMvcConfigurer;
import org.miaixz.bus.starter.GeniusBuilder;

/**
 * Configures application message converters.
 *
 * @author Kimi Liu
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(MessageConverterProperties.class)
@ConditionalOnProperty(prefix = GeniusBuilder.WRAPPER_MESSAGE_CONVERTERS, name = "enabled", havingValue = "true", matchIfMissing = true)
public class MessageConverterConfiguration {

    /**
     * Initializes HTTP message conversion when the wrapper converter feature is enabled.
     */
    public MessageConverterConfiguration() {
        // No initialization required.
    }

    /**
     * Creates the text web mvc configurer.
     *
     * @return the configurer that registers plain-text conversion
     */
    @Bean
    @ConditionalOnMissingBean(TextWebMvcConfigurer.class)
    public TextWebMvcConfigurer textWebMvcConfigurer() {
        return new TextWebMvcConfigurer();
    }

    /**
     * Creates the json web mvc configurer.
     *
     * @param registrars ordered converter registrars contributed by the application
     * @return the configurer that registers JSON conversion
     */
    @Bean
    @ConditionalOnMissingBean(JsonWebMvcConfigurer.class)
    public JsonWebMvcConfigurer jsonWebMvcConfigurer(List<MessageConverterRegistrar> registrars) {
        return new JsonWebMvcConfigurer(registrars);
    }

    /**
     * Creates the json message converter.
     *
     * @param provider    application JSON provider managed by JSON configuration
     * @param properties  message-converter type policy and explicit allow-list
     * @param beanFactory current Bean factory containing the application base-package registration
     * @return the HTTP message converter backed by the selected JSON Provider
     */
    @Bean
    @ConditionalOnBean(JsonProvider.class)
    @ConditionalOnMissingBean(JsonMessageConverter.class)
    public JsonMessageConverter jsonMessageConverter(
            JsonProvider provider,
            MessageConverterProperties properties,
            BeanFactory beanFactory) {
        JsonMessageConverter converter = new JsonMessageConverter(provider);
        if (allowsAll(properties)) {
            converter.typeFilter(JsonTypeFilter.always());
            return converter;
        }
        converter.autoType(String.join(",", allowedTypeRules(properties, beanFactory)));
        return converter;
    }

    /**
     * Resolves the explicit and application-derived type rules while preserving their configured order.
     *
     * @param properties  configured type policy and rules
     * @param beanFactory current Bean factory
     * @return normalized type rules
     */
    static List<String> allowedTypeRules(MessageConverterProperties properties, BeanFactory beanFactory) {
        Set<String> rules = new LinkedHashSet<>();
        if (properties.getTypePolicy() == MessageConverterProperties.TypePolicy.APPLICATION) {
            if (AutoConfigurationPackages.has(beanFactory)) {
                AutoConfigurationPackages.get(beanFactory).stream().map(packageName -> packageName + ".**")
                        .forEach(rules::add);
            } else {
                Logger.warn(
                        false,
                        "Starter",
                        "Unable to discover Spring Boot application packages for JSON type policy; configure allowed-types explicitly");
            }
        }
        rules.addAll(splitAutoTypes(properties.getAutoType()));
        rules.addAll(properties.getAllowedTypes());
        return List.copyOf(rules);
    }

    /**
     * Returns whether the effective configuration explicitly allows every JSON target class.
     *
     * @param properties configured type policy and rules
     * @return {@code true} when all target classes are allowed
     */
    static boolean allowsAll(MessageConverterProperties properties) {
        return properties.getTypePolicy() == MessageConverterProperties.TypePolicy.ALL
                || splitAutoTypes(properties.getAutoType()).stream().anyMatch("**"::equals)
                || properties.getAllowedTypes().stream().anyMatch("**"::equals);
    }

    /**
     * Splits comma-separated and multi-line auto-type rules.
     *
     * @param autoType raw auto-type configuration
     * @return normalized rules
     */
    static List<String> splitAutoTypes(String autoType) {
        if (autoType == null || autoType.isBlank()) {
            return List.of();
        }
        return Arrays.stream(autoType.split("[,\\r\\n]+")).map(String::trim).filter(value -> !value.isEmpty())
                .distinct().toList();
    }

}
