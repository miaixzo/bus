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

import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.miaixz.bus.extra.json.JsonProvider;
import org.miaixz.bus.spring.web.converter.JsonMessageConverter;
import org.miaixz.bus.spring.web.converter.JsonWebMvcConfigurer;
import org.miaixz.bus.spring.web.converter.MessageConverterRegistrar;
import org.miaixz.bus.spring.web.converter.TextWebMvcConfigurer;
import org.miaixz.bus.starter.GeniusBuilder;

/**
 * Configures application message converters.
 *
 * @author Kimi Liu
 * @since Java 21+
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
     * @param provider application JSON provider managed by JSON configuration
     * @return the HTTP message converter backed by the selected JSON Provider
     */
    @Bean
    @ConditionalOnBean(JsonProvider.class)
    @ConditionalOnMissingBean(JsonMessageConverter.class)
    public JsonMessageConverter jsonMessageConverter(JsonProvider provider) {
        return new JsonMessageConverter(provider);
    }

}
