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

import jakarta.annotation.Resource;
import org.miaixz.bus.spring.GeniusBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration for internationalization (i18n). This class sets up the {@link MessageSource} bean based on the
 * properties defined in {@link I18nProperties}.
 * <p>
 * The registered {@link I18nMessage} is also the Spring {@link MessageSource}. This keeps framework-level lookups and
 * direct application lookups on one resolver instead of maintaining separate helper and message-source implementations.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@EnableConfigurationProperties(value = { I18nProperties.class })
@ConditionalOnProperty(prefix = GeniusBuilder.I18N, name = "enabled", havingValue = "true", matchIfMissing = true)
public class I18nConfiguration {

    /**
     * Injected i18n configuration properties.
     */
    @Resource
    I18nProperties properties;

    /**
     * Creates the unified i18n bean. It is exposed under Spring's conventional {@code messageSource} bean name while
     * retaining the concrete {@link I18nMessage} type for direct injection.
     *
     * @return the configured i18n message source
     */
    @Bean(name = "messageSource")
    @ConditionalOnMissingBean(name = "messageSource")
    public I18nMessage messageSource() {
        return new I18nMessage(this.properties);
    }

}
