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
package org.miaixz.bus.starter.wrapper;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import org.miaixz.bus.starter.GeniusBuilder;
import org.miaixz.bus.starter.wrapper.advice.ResponseAdviceConfiguration;
import org.miaixz.bus.starter.wrapper.binding.RequestBindingConfiguration;
import org.miaixz.bus.starter.wrapper.body.BodyCacheConfiguration;
import org.miaixz.bus.starter.wrapper.converter.MessageConverterConfiguration;
import org.miaixz.bus.starter.wrapper.routing.RoutePrefixConfiguration;

/**
 * Root configuration that composes the five independent wrapper capabilities.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(WrapperProperties.class)
@ConditionalOnProperty(prefix = GeniusBuilder.WRAPPER, name = "enabled", havingValue = "true", matchIfMissing = false)
@Import({ BodyCacheConfiguration.class, RequestBindingConfiguration.class, MessageConverterConfiguration.class,
        ResponseAdviceConfiguration.class, RoutePrefixConfiguration.class })
public class WrapperConfiguration {

    /**
     * Initializes the aggregate configuration that imports the independently controlled web wrapper features.
     */
    public WrapperConfiguration() {
        // No initialization required.
    }

}
