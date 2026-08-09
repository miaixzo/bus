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
package org.miaixz.bus.starter.validate;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.miaixz.bus.spring.boot.condition.ConditionalOnEnabled;
import org.miaixz.bus.starter.GeniusBuilder;
import org.miaixz.bus.starter.annotation.EnableValidate;

/**
 * Configures annotation-driven method validation and controller validation advice.
 * <p>
 * This configuration class enables the AOP-based validation feature by importing the {@link AspectjValidateProxy}. This
 * allows for automatic validation of method parameters in Spring-managed beans (typically controllers) that are
 * annotated with validation constraints.
 *
 * @author Kimi Liu
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(name = "org.miaixz.bus.validate.Builder")
@ConditionalOnEnabled(annotation = EnableValidate.class, prefix = GeniusBuilder.VALIDATE)
public class ValidateConfiguration {

    /**
     * Initializes validation infrastructure selected by {@code @EnableValidate} or the corresponding property.
     */
    public ValidateConfiguration() {
        // No initialization required.
    }

    /**
     * Creates the validation aspect only when this feature configuration is active.
     *
     * @return validation aspect
     */
    @Bean
    @ConditionalOnMissingBean(AspectjValidateProxy.class)
    AspectjValidateProxy aspectjValidateProxy() {
        return new AspectjValidateProxy();
    }

    /**
     * Creates the validation advice explicitly without component scanning.
     *
     * @return the advice that performs automatic method validation
     */
    @Bean
    @ConditionalOnMissingBean(AutoValidateAdvice.class)
    AutoValidateAdvice autoValidateAdvice() {
        return new AutoValidateAdvice();
    }

}
