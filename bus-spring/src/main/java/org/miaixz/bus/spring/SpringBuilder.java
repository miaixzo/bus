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
package org.miaixz.bus.spring;

import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.core.ResolvableType;

import org.miaixz.bus.core.Provider;
import org.miaixz.bus.core.lang.reflect.TypeReference;
import org.miaixz.bus.spring.bean.*;

/**
 * Instance facade for the six narrowly scoped Spring Bean infrastructure services.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class SpringBuilder {

    /**
     * Application context owned by this facade.
     */
    private final SpringContext context;
    /**
     * Read-only Bean lookup service.
     */
    private final BeanProvider beans;
    /**
     * Bean registration service.
     */
    private final BeanRegistry registry;
    /**
     * Side-effect-free Bean metadata service.
     */
    private final BeanMetadata metadata;
    /**
     * Environment property resolution service.
     */
    private final EnvironmentResolver environment;
    /**
     * Ordered Provider discovery service.
     */
    private final ProviderRegistry providers;

    /**
     * Creates a facade whose collaborators all belong to the same application context.
     *
     * @param context     owning application context
     * @param beans       read-only Bean lookup service
     * @param registry    Bean registration service
     * @param metadata    side-effect-free Bean metadata service
     * @param environment Spring environment
     * @param providers   ordered provider candidates
     */
    public SpringBuilder(SpringContext context, BeanProvider beans, BeanRegistry registry, BeanMetadata metadata,
            EnvironmentResolver environment, ProviderRegistry providers) {
        this.context = Objects.requireNonNull(context, "context");
        this.beans = Objects.requireNonNull(beans, "beans");
        this.registry = Objects.requireNonNull(registry, "registry");
        this.metadata = Objects.requireNonNull(metadata, "metadata");
        this.environment = Objects.requireNonNull(environment, "environment");
        this.providers = Objects.requireNonNull(providers, "providers");
    }

    /**
     * Exposes the active application context owned by this facade.
     *
     * @return the facade's active application context
     */
    public ApplicationContext getContext() {
        return this.context.get();
    }

    /**
     * Returns the owned context as a read-only listable Bean factory.
     *
     * @return the bean factory
     */
    public ListableBeanFactory getBeanFactory() {
        return this.context.get();
    }

    /**
     * Resolves a named Bean without imposing a compile-time result type.
     *
     * @param <T>  result type
     * @param name logical name
     * @return the Bean registered under the supplied name
     */
    public <T> T getBean(String name) {
        return this.beans.getBean(name);
    }

    /**
     * Resolves the unique Bean of a type, passing explicit arguments to Spring when it must be created.
     *
     * @param <T>       result type
     * @param type      required Bean contract
     * @param arguments explicit invocation arguments
     * @return the unique Bean assignable to the required contract
     */
    public <T> T getBean(Class<T> type, Object... arguments) {
        return this.beans.getBean(type, arguments);
    }

    /**
     * Resolves a named Bean and supplies explicit construction arguments when Spring creates it.
     *
     * @param <T>       result type
     * @param name      logical name
     * @param arguments explicit invocation arguments
     * @return the Bean registered under the supplied name
     */
    public <T> T getBean(String name, Object... arguments) {
        return this.beans.getBean(name, arguments);
    }

    /**
     * Resolves a named Bean and verifies that it implements the required contract.
     *
     * @param <T>  result type
     * @param name logical name
     * @param type required Bean contract
     * @return the named Bean cast to the required contract
     */
    public <T> T getBean(String name, Class<T> type) {
        return this.beans.getBean(name, type);
    }

    /**
     * Returns a Bean matching a concrete generic type reference.
     *
     * @param <T>       result type
     * @param reference Bean reference containing a name and required type
     * @return the unique Bean matching the concrete generic signature
     */
    public <T> T getBean(TypeReference<T> reference) {
        ParameterizedType type = (ParameterizedType) Objects.requireNonNull(reference, "reference").getType();
        Class<T> rawType = (Class<T>) type.getRawType();
        Class<?>[] genericTypes = Arrays.stream(type.getActualTypeArguments()).map(argument -> (Class<?>) argument)
                .toArray(Class<?>[]::new);
        String[] names = this.beans.getBeanNamesForType(ResolvableType.forClassWithGenerics(rawType, genericTypes));
        if (names.length != 1) {
            throw new IllegalStateException(
                    "Expected one Bean for the requested generic type but found " + names.length);
        }
        return this.beans.getBean(names[0], rawType);
    }

    /**
     * Resolves every Bean assignable to the required contract, keyed by Bean name.
     *
     * @param <T>  result type
     * @param type required Bean contract
     * @return matching Beans keyed by their registration names
     */
    public <T> Map<String, T> getBeansOfType(Class<T> type) {
        return this.beans.getBeansOfType(type);
    }

    /**
     * Resolves the registration names of Beans assignable to a contract.
     *
     * @param type required Bean contract
     * @return registration names in Spring's discovery order
     */
    public String[] getBeanNamesForType(Class<?> type) {
        return this.beans.getBeanNamesForType(type);
    }

    /**
     * Registers the bean definition.
     *
     * @param type concrete Bean class registered under its derived name
     */
    public void registerBeanDefinition(Class<?> type) {
        this.registry.registerBeanDefinition(type);
    }

    /**
     * Registers the bean definition.
     *
     * @param name logical name
     * @param type concrete Bean class associated with the definition
     */
    public void registerBeanDefinition(String name, Class<?> type) {
        this.registry.registerBeanDefinition(name, type);
    }

    /**
     * Registers an existing object as a singleton under its derived Bean name.
     *
     * @param bean Bean instance
     */
    public void registerSingleton(Object bean) {
        this.registry.registerSingleton(bean);
    }

    /**
     * Registers an existing object as a named singleton after validating its declared contract.
     *
     * @param name logical name
     * @param type contract the singleton must implement
     * @param bean Bean instance
     */
    public void registerSingleton(String name, Class<?> type, Object bean) {
        this.registry.registerSingleton(name, type, bean);
    }

    /**
     * Unregisters the bean definition.
     *
     * @param name logical name
     */
    public void unregisterBeanDefinition(String name) {
        this.registry.unregisterBeanDefinition(name);
    }

    /**
     * Unregisters the singleton.
     *
     * @param name logical name
     */
    public void unregisterSingleton(String name) {
        this.registry.unregisterSingleton(name);
    }

    /**
     * Publishes an application event through the owned Spring context.
     *
     * @param event published event
     */
    public void publishEvent(Object event) {
        this.context.publishEvent(event);
    }

    /**
     * Resolves a String property from the owned Spring environment.
     *
     * @param key lookup key
     * @return resolved property value, or {@code null} when absent
     */
    public String getProperty(String key) {
        return this.environment.getProperty(key);
    }

    /**
     * Returns an isolated copy of all active Spring profile names.
     *
     * @return the active profiles
     */
    public String[] getActiveProfiles() {
        return this.environment.getActiveProfiles();
    }

    /**
     * Selects the first active Spring profile.
     *
     * @return first active profile, or {@code null} when no profile is active
     */
    public String getActiveProfile() {
        return this.environment.getActiveProfile();
    }

    /**
     * Resolves the application name from {@code spring.application.name}.
     *
     * @return the application name
     */
    public String getApplicationName() {
        return this.environment.getApplicationName();
    }

    /**
     * Tests whether a non-production demonstration profile is active.
     *
     * @return whether demo mode
     */
    public boolean isDemoMode() {
        return this.environment.isDemoMode();
    }

    /**
     * Tests whether the development profile is active.
     *
     * @return whether dev mode
     */
    public boolean isDevMode() {
        return this.environment.isDevMode();
    }

    /**
     * Tests whether the test profile is active.
     *
     * @return whether test mode
     */
    public boolean isTestMode() {
        return this.environment.isTestMode();
    }

    /**
     * Tests whether the production profile is active.
     *
     * @return whether prod mode
     */
    public boolean isProdMode() {
        return this.environment.isProdMode();
    }

    /**
     * Resolves Spring property placeholders embedded in arbitrary text.
     *
     * @param text text containing Spring property placeholders
     * @return text with placeholders replaced
     */
    public String replacePlaceholders(String text) {
        return this.environment.replacePlaceholders(text);
    }

    /**
     * Resolves the bean class type.
     *
     * @param definition Bean definition whose target type is required
     * @return resolved bean class type
     */
    public Class<?> resolveBeanClassType(BeanDefinition definition) {
        return this.metadata.resolveBeanClassType(definition);
    }

    /**
     * Tests whether a Bean definition originated from Spring configuration method metadata.
     *
     * @param definition Bean definition whose declared class is required
     * @return {@code true} when the definition was declared by a configuration-class method
     */
    public boolean isFromConfigurationSource(BeanDefinition definition) {
        return this.metadata.isFromConfigurationSource(definition);
    }

    /**
     * Selects the first ordered provider whose declared support matches the supplied value.
     *
     * @param <T>           result type
     * @param <S>           support type
     * @param providerClass provider contract
     * @param support       provider selection predicate
     * @return loaded provider
     */
    public <T extends Provider<S>, S> T loadProvider(Class<T> providerClass, S support) {
        return this.providers.load(providerClass, support);
    }

    /**
     * Resolves every provider of a contract in Spring ordering.
     *
     * @param <T>           result type
     * @param providerClass provider contract
     * @return immutable ordered provider list for this application context
     */
    public <T extends Provider<?>> List<T> getProviders(Class<T> providerClass) {
        return this.providers.all(providerClass);
    }

}
