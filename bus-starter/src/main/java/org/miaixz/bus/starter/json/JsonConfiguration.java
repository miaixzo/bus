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
package org.miaixz.bus.starter.json;

import java.lang.reflect.Constructor;

import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ResolvableType;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import org.miaixz.bus.extra.json.JsonFactory;
import org.miaixz.bus.extra.json.JsonProvider;
import org.miaixz.bus.logger.Logger;

/**
 * Creates and installs the application-wide JSON provider.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@EnableConfigurationProperties(JsonProperties.class)
@Configuration(proxyBeanMethods = false)
public class JsonConfiguration {

    /**
     * Creates the shared JSON provider auto-configuration.
     */
    public JsonConfiguration() {
        // No initialization required.
    }

    /**
     * Creates the default provider selected from Bus properties, Spring's preferred mapper property, and the runtime
     * classpath. If the selected framework exposes a unique configured engine bean, that engine is reused.
     *
     * @param properties  Bus JSON properties
     * @param environment Spring environment containing compatibility mapper properties
     * @param beanFactory bean factory used to locate a configured Gson or Jackson engine
     * @return selected JSON provider
     */
    @Bean
    @ConditionalOnMissingBean(JsonProvider.class)
    public JsonProvider jsonProvider(
            JsonProperties properties,
            Environment environment,
            ListableBeanFactory beanFactory) {
        String requestedProvider = resolveRequestedProvider(properties, environment);
        return withConfiguredEngine(JsonFactory.of(requestedProvider), beanFactory);
    }

    /**
     * Validates, installs, and publishes the unique application-wide JSON provider selection.
     *
     * @param providers all JSON provider beans visible to the application context
     * @return immutable selected-provider holder shared by starter integrations
     * @throws IllegalStateException if a unique or primary provider cannot be resolved
     */
    @Bean
    public SelectedJsonProvider selectedJsonProvider(ObjectProvider<JsonProvider> providers) {
        JsonProvider provider = providers.getIfUnique();
        if (provider == null) {
            throw new IllegalStateException("A unique or @Primary JsonProvider bean is required");
        }
        JsonFactory.install(provider);
        Logger.info(false, "Starter", "JSON provider selected: {}", provider.name());
        return new SelectedJsonProvider(provider);
    }

    /**
     * Resolves the requested provider with Bus configuration taking precedence over Spring compatibility properties.
     *
     * @param properties  Bus JSON properties
     * @param environment Spring environment
     * @return configured provider name, or {@code auto} when no preference is configured
     */
    private static String resolveRequestedProvider(JsonProperties properties, Environment environment) {
        String requested = properties.getProvider();
        if (StringUtils.hasText(requested) && !"auto".equalsIgnoreCase(requested)) {
            return requested;
        }
        String preferred = environment.getProperty("spring.http.converters.preferred-json-mapper");
        if (!StringUtils.hasText(preferred)) {
            preferred = environment.getProperty("spring.mvc.converters.preferred-json-mapper");
        }
        return StringUtils.hasText(preferred) ? preferred : "auto";
    }

    /**
     * Rebuilds Gson and Jackson providers around the application's unique framework engine bean when one exists.
     * Fastjson requires no engine-instance binding and is returned unchanged.
     *
     * @param provider    provider initially created through the Bus SPI
     * @param beanFactory bean factory used to locate a unique engine bean
     * @return provider backed by the configured engine, or the original provider when no engine bean exists
     * @throws IllegalStateException if engine beans are ambiguous or provider construction fails
     */
    private static JsonProvider withConfiguredEngine(JsonProvider provider, ListableBeanFactory beanFactory) {
        String engineTypeName = switch (provider.name()) {
            case "gson" -> "com.google.gson.Gson";
            case "jackson" -> "tools.jackson.databind.ObjectMapper";
            default -> null;
        };
        if (engineTypeName == null) {
            return provider;
        }
        try {
            Class<?> engineType = Class.forName(engineTypeName, false, provider.getClass().getClassLoader());
            ResolvableType resolvableType = ResolvableType.forClass(engineType);
            String[] beanNames = beanFactory.getBeanNamesForType(resolvableType);
            if (beanNames.length == 0) {
                return provider;
            }
            Object engine = beanFactory.getBeanProvider(resolvableType).getIfUnique();
            if (engine == null) {
                throw new IllegalStateException(
                        "Multiple " + engineTypeName + " beans are available without a unique @Primary bean");
            }
            Constructor<? extends JsonProvider> constructor = provider.getClass().getConstructor(engineType);
            return constructor.newInstance(engine);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to bind configured " + provider.name() + " engine", e);
        }
    }

}
