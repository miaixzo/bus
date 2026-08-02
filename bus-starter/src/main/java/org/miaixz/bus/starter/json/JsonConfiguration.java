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

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.miaixz.bus.extra.json.JsonFactory;
import org.miaixz.bus.extra.json.JsonProvider;
import org.miaixz.bus.starter.GeniusBuilder;

/**
 * Creates the Context-local JSON provider.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@EnableConfigurationProperties(JsonProperties.class)
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(prefix = GeniusBuilder.JSON, name = "enabled", havingValue = "true", matchIfMissing = false)
public class JsonConfiguration {

    /**
     * Bound json configuration properties.
     */
    private final JsonProperties properties;

    /**
     * Stores the provider selection used to create the application-context JSON provider.
     *
     * @param properties bound configuration properties
     */
    public JsonConfiguration(JsonProperties properties) {
        this.properties = properties;
    }

    /**
     * Creates the default provider selected exclusively from Bus properties.
     *
     * @return selected JSON provider
     */
    @Bean
    @ConditionalOnMissingBean(JsonProvider.class)
    public JsonProvider jsonProvider() {
        return JsonFactory.of(this.properties.getProvider().key());
    }

}
