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
package org.miaixz.bus.spring.bean;

import java.beans.Introspector;
import java.util.Arrays;
import java.util.Objects;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Bean definition and singleton mutations scoped to one Spring application context.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class BeanRegistry {

    /**
     * Context owning all registration operations.
     */
    private final SpringContext context;

    /**
     * Creates a registry bound to one context.
     *
     * @param context context supplying the configurable application context
     */
    public BeanRegistry(SpringContext context) {
        this.context = Objects.requireNonNull(context, "context");
    }

    /**
     * Registers a root Bean definition using the conventional Bean name.
     *
     * @param type Bean implementation class
     */
    public void registerBeanDefinition(Class<?> type) {
        registerBeanDefinition(beanName(type), type);
    }

    /**
     * Registers a root Bean definition after validating name and type uniqueness.
     *
     * @param name unique Bean name
     * @param type Bean implementation class
     */
    public void registerBeanDefinition(String name, Class<?> type) {
        DefaultListableBeanFactory factory = beanFactory();
        validateRegistration(factory, name, type);
        factory.registerBeanDefinition(name, BeanDefinitionBuilder.rootBeanDefinition(type).getBeanDefinition());
    }

    /**
     * Registers an existing singleton using its runtime type and conventional Bean name.
     *
     * @param bean singleton instance
     */
    public void registerSingleton(Object bean) {
        Objects.requireNonNull(bean, "bean");
        registerSingleton(beanName(bean.getClass()), bean.getClass(), bean);
    }

    /**
     * Registers an existing singleton after validating its name and declared type.
     *
     * @param name unique Bean name
     * @param type declared Bean type
     * @param bean singleton instance
     */
    public void registerSingleton(String name, Class<?> type, Object bean) {
        Objects.requireNonNull(bean, "bean");
        if (!type.isInstance(bean)) {
            throw new IllegalArgumentException("Singleton does not implement its declared Bean type");
        }
        DefaultListableBeanFactory factory = beanFactory();
        validateRegistration(factory, name, type);
        factory.autowireBean(bean);
        factory.registerSingleton(name, bean);
    }

    /**
     * Removes a Bean definition and destroys its created singleton, if present.
     *
     * @param name registered Bean definition name
     */
    public void unregisterBeanDefinition(String name) {
        DefaultListableBeanFactory factory = beanFactory();
        requireName(name);
        if (!factory.containsBeanDefinition(name)) {
            throw new IllegalStateException("No Bean definition is registered with name '" + name + "'");
        }
        factory.removeBeanDefinition(name);
    }

    /**
     * Destroys a manually registered singleton owned by this context.
     *
     * @param name registered singleton name
     */
    public void unregisterSingleton(String name) {
        DefaultListableBeanFactory factory = beanFactory();
        requireName(name);
        if (!factory.containsSingleton(name)) {
            throw new IllegalStateException("No singleton is registered with name '" + name + "'");
        }
        factory.destroySingleton(name);
    }

    /**
     * Resolves the mutable Bean factory owned by the bound context.
     *
     * @return mutable default Bean factory
     */
    private DefaultListableBeanFactory beanFactory() {
        ApplicationContext applicationContext = this.context.get();
        if (!(applicationContext instanceof ConfigurableApplicationContext configurable)) {
            throw new IllegalStateException("Bean registration requires a configurable application context");
        }
        if (!(configurable.getBeanFactory() instanceof DefaultListableBeanFactory factory)) {
            throw new IllegalStateException("Bean registration requires a DefaultListableBeanFactory");
        }
        return factory;
    }

    /**
     * Rejects duplicate names, aliases, and already registered Bean types.
     *
     * @param factory target Bean factory
     * @param name    proposed Bean name
     * @param type    proposed Bean type
     */
    private static void validateRegistration(DefaultListableBeanFactory factory, String name, Class<?> type) {
        requireName(name);
        Objects.requireNonNull(type, "type");
        if (factory.containsBeanDefinition(name) || factory.containsSingleton(name) || factory.isAlias(name)) {
            throw new IllegalStateException("Bean name '" + name + "' is already registered");
        }
        String[] conflicts = factory.getBeanNamesForType(type, true, false);
        if (conflicts.length > 0) {
            throw new IllegalStateException(
                    "Bean type '" + type.getName() + "' is already registered as " + Arrays.toString(conflicts));
        }
    }

    /**
     * Derives the conventional lower-camel-case Bean name for a class.
     *
     * @param type Bean type
     * @return conventional Bean name
     */
    private static String beanName(Class<?> type) {
        Objects.requireNonNull(type, "type");
        return Introspector.decapitalize(type.getSimpleName());
    }

    /**
     * Validates a required Bean name.
     *
     * @param name Bean name to validate
     */
    private static void requireName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Bean name must not be blank");
        }
    }

}
