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
package org.miaixz.bus.starter.mapper;

import java.util.Objects;

import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 * Binds every default or user-supplied {@link SqlSessionFactory} to the application-context datasource dialect
 * listener after the factory has been initialized.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class DialectBinding implements BeanPostProcessor {

    /**
     * Lazy provider for the context-local datasource dialect listener.
     */
    private final ObjectProvider<DialectListener> listeners;

    /**
     * Creates a dialect binding that does not force JDBC infrastructure to initialize early.
     *
     * @param listeners lazy dialect-listener provider
     */
    public DialectBinding(ObjectProvider<DialectListener> listeners) {
        this.listeners = Objects.requireNonNull(listeners, "listeners");
    }

    /**
     * Associates an initialized MyBatis factory with the current datasource routing context.
     *
     * @param bean     initialized Spring Bean
     * @param beanName Spring Bean name
     * @return the original initialized Bean
     * @throws BeansException when listener resolution or dialect binding fails
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof SqlSessionFactory sqlSessionFactory) {
            DialectListener listener = this.listeners.getIfAvailable();
            if (listener != null) {
                listener.bind(sqlSessionFactory.getConfiguration());
            }
        }
        return bean;
    }

}
