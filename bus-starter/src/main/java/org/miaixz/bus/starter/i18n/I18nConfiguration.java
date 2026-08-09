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
package org.miaixz.bus.starter.i18n;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.miaixz.bus.spring.boot.condition.ConditionalOnEnabled;
import org.miaixz.bus.starter.GeniusBuilder;
import org.miaixz.bus.starter.annotation.EnableI18n;

/**
 * Configures internationalization (i18n). This class sets up the {@link MessageSource} bean based on the properties
 * defined in {@link I18nProperties}.
 * <p>
 * The registered {@link I18nMessage} delegates to the application-owned Spring {@link MessageSource} and uses the Bus
 * resource bundle configuration only as a fallback.
 * </p>
 *
 * @author Kimi Liu
 */
@EnableConfigurationProperties(value = { I18nProperties.class })
@Configuration(proxyBeanMethods = false)
@ConditionalOnEnabled(annotation = EnableI18n.class, prefix = GeniusBuilder.I18N)
public class I18nConfiguration {

    /**
     * Bound i18n configuration properties.
     */
    private final I18nProperties properties;

    /**
     * Stores the message-source properties used by the i18n adapter Bean factories.
     *
     * @param properties bound configuration properties
     */
    public I18nConfiguration(I18nProperties properties) {
        this.properties = properties;
    }

    /**
     * Creates the application i18n access adapter.
     *
     * @param messageSource application-owned Spring message source
     * @return the configured i18n message source
     */
    @Bean
    @ConditionalOnMissingBean(I18nMessage.class)
    public I18nMessage i18nMessage(MessageSource messageSource) {
        return new I18nMessage(messageSource, this.properties);
    }

}
