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
package org.miaixz.bus.starter.tracer;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.miaixz.bus.spring.boot.condition.ConditionalOnEnabled;
import org.miaixz.bus.starter.GeniusBuilder;
import org.miaixz.bus.starter.annotation.EnableTracer;
import org.miaixz.bus.tracer.Tracer;

/**
 * Configures the application-scoped distributed tracer.
 * <p>
 * This class enables the {@link TracerProperties}, which will hold the configuration for the tracing system. It serves
 * as the entry point for setting up tracing-related beans.
 *
 * @author Kimi Liu
 */
@EnableConfigurationProperties(value = { TracerProperties.class })
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(name = "org.miaixz.bus.tracer.Tracer")
@ConditionalOnEnabled(annotation = EnableTracer.class, prefix = GeniusBuilder.TRACER)
public class TracerConfiguration {

    /**
     * Bound tracer configuration properties.
     */
    private final TracerProperties properties;

    /**
     * Stores the tracing activation properties for the application-scoped tracer.
     *
     * @param properties bound configuration properties
     */
    public TracerConfiguration(TracerProperties properties) {
        this.properties = properties;
    }

    /**
     * Creates the Context-local tracer.
     *
     * @return the configured tracing facade
     */
    @Bean
    @ConditionalOnMissingBean(Tracer.class)
    public Tracer tracer() {
        return new Tracer();
    }

}
