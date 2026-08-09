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

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

import org.miaixz.bus.core.Provider;

/**
 * Context-owned discovery, ordering and caching of Spring Provider Beans.
 *
 * @author Kimi Liu
 */
public class ProviderRegistry implements ApplicationListener<ContextClosedEvent>, AutoCloseable {

    /**
     * Application context that owns this registry.
     */
    private final SpringContext context;
    /**
     * Bean lookup service used to discover Provider implementations.
     */
    private final BeanProvider beans;
    /**
     * Context-local cache of ordered Providers grouped by contract type.
     */
    private final Map<Class<?>, List<?>> providers = new ConcurrentHashMap<>();

    /**
     * Creates a Provider registry bound to one Spring context.
     *
     * @param context owning application context
     * @param beans   Bean lookup service used for Provider discovery
     */
    public ProviderRegistry(SpringContext context, BeanProvider beans) {
        this.context = Objects.requireNonNull(context, "context");
        this.beans = Objects.requireNonNull(beans, "beans");
    }

    /**
     * Returns the first ordered Provider whose declared type matches the support value.
     *
     * @param <T>           result type
     * @param <S>           support type
     * @param providerClass provider contract
     * @param support       provider selection predicate
     * @return the first matching Provider, or {@code null} when none supports the value
     */
    public <T extends Provider<S>, S> T load(Class<T> providerClass, S support) {
        for (T provider : all(providerClass)) {
            if (Objects.equals(provider.type(), support)) {
                return provider;
            }
        }
        return null;
    }

    /**
     * Returns an immutable, Spring-ordered Provider list for this context.
     *
     * @param <T>           result type
     * @param providerClass provider contract
     * @return an immutable, Spring-ordered list of Providers
     */
    public <T extends Provider<?>> List<T> all(Class<T> providerClass) {
        Objects.requireNonNull(providerClass, "providerClass");
        return cast(this.providers.computeIfAbsent(providerClass, this::loadProviders));
    }

    /**
     * Clears this registry only when its owning Spring context closes.
     */
    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        try {
            if (this.context.get() == event.getApplicationContext()) {
                close();
            }
        } catch (IllegalStateException ignored) {
            close();
        }
    }

    /**
     * Releases all Context-local Provider cache entries.
     */
    @Override
    public void close() {
        this.providers.clear();
    }

    /**
     * Loads the providers.
     *
     * @param providerClass provider contract
     * @return loaded providers
     */
    private List<?> loadProviders(Class<?> providerClass) {
        Map<String, ?> beansOfType = this.beans.getBeansOfType(providerClass);
        List<?> sortedProviders = new ArrayList<>(beansOfType.values());
        AnnotationAwareOrderComparator.sort(sortedProviders);
        return List.copyOf(sortedProviders);
    }

    /**
     * Casts a provider collection after assignability has been validated.
     *
     * @param <T>       result type
     * @param providers ordered providers
     * @return the cached Provider list viewed with its declared contract type
     */
    private static <T> List<T> cast(Collection<?> providers) {
        return (List<T>) providers;
    }

}
