/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.spring.boot;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 * An implementation of {@link BeanPostProcessor} that injects a {@link StartupReporter} into any bean that implements
 * the {@link StartupReporterAware} interface.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class StartupReporterProcessor implements BeanPostProcessor {

    /**
     * The {@link StartupReporter} instance to be injected.
     */
    private final StartupReporter startupReporter;

    /**
     * Constructs a new {@code StartupReporterProcessor} with the given {@link StartupReporter}.
     *
     * @param startupReporter The {@link StartupReporter} instance.
     */
    public StartupReporterProcessor(StartupReporter startupReporter) {
        this.startupReporter = startupReporter;
    }

    /**
     * Applies this {@code BeanPostProcessor} to the given new bean instance before any construction callbacks (like
     * {@code InitializingBean}'s {@code afterPropertiesSet} or a custom init-method).
     * <p>
     * If the bean implements {@link StartupReporterAware}, its {@code setStartupReporter} method is called with the
     * {@code startupReporter} instance.
     * </p>
     *
     * @param bean     The new bean instance.
     * @param beanName The name of the bean.
     * @return The given bean instance.
     * @throws BeansException in case of initialization errors.
     */
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof StartupReporterAware) {
            ((StartupReporterAware) bean).setStartupReporter(startupReporter);
        }
        return bean;
    }

}
