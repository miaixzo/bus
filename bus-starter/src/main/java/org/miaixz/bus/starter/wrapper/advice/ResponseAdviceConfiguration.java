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
package org.miaixz.bus.starter.wrapper.advice;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.miaixz.bus.spring.web.advice.MessageResponseBodyAdvice;
import org.miaixz.bus.starter.GeniusBuilder;

/**
 * Configures response wrapping advice.
 *
 * @author Kimi Liu
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(ResponseAdviceProperties.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass(name = "org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice")
@ConditionalOnProperty(prefix = GeniusBuilder.WRAPPER_RESPONSE_ADVICE, name = "enabled", havingValue = "true", matchIfMissing = false)
public class ResponseAdviceConfiguration {

    /**
     * Bound response advice configuration properties.
     */
    private final ResponseAdviceProperties properties;

    /**
     * Stores the response-envelope policy used by the MVC advice and converter.
     *
     * @param properties bound configuration properties
     */
    public ResponseAdviceConfiguration(ResponseAdviceProperties properties) {
        this.properties = properties;
    }

    /**
     * Creates the message response body advice.
     *
     * @return the advice that wraps eligible response bodies
     */
    @Bean
    @ConditionalOnMissingBean(MessageResponseBodyAdvice.class)
    public MessageResponseBodyAdvice messageResponseBodyAdvice() {
        return new MessageResponseBodyAdvice();
    }

}
