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

import org.miaixz.bus.core.Provider;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A manager for handling the Strategy Pattern within a Spring context. It loads and caches strategy providers (beans
 * implementing the {@link Provider} interface) from the application context.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Component
public class ProviderManager {

    /**
     * A cache for provider beans, keyed by their interface class.
     */
    public static final Map<Class<Provider<?>>, Collection<Provider<?>>> CACHED_PROVIDERS = new ConcurrentHashMap<>();
    /**
     * The Spring {@link ConfigurableApplicationContext}, which is lazily initialized.
     */
    public static ConfigurableApplicationContext context;

    /**
     * Default constructor.
     */
    public ProviderManager() {

    }

    /**
     * Loads a specific provider that supports a given strategy type.
     *
     * @param providerClass The interface class of the providers.
     * @param support       The specific strategy type to look for.
     * @param <T>           The type of the provider.
     * @param <S>           The type of the support criteria.
     * @return The first matching provider, or null if none is found.
     */
    public static <T extends Provider<S>, S> T load(Class<T> providerClass, S support) {
        Collection<T> providers = loadProvider(providerClass);
        for (Provider<?> provider : providers) {
            if (Objects.equals(provider.type(), support)) {
                return (T) provider;
            }
        }
        return null;
    }

    /**
     * Returns all registered providers for a given provider interface class, sorted by order.
     *
     * @param providerClass The interface class of the providers.
     * @param <T>           The type of the provider.
     * @return A collection of all registered providers.
     */
    public static <T extends Provider<?>> Collection<T> all(Class<T> providerClass) {
        return loadProvider(providerClass);
    }

    /**
     * Loads and caches all beans of a given provider interface type from the Spring application context. The beans are
     * sorted based on Spring's ordering annotations.
     *
     * @param providerClass The provider interface class.
     * @param <T>           The type of the provider.
     * @return A sorted collection of provider beans.
     */
    private static <T extends Provider<?>> Collection<T> loadProvider(Class<T> providerClass) {
        return (Collection<T>) CACHED_PROVIDERS.computeIfAbsent((Class<Provider<?>>) providerClass, key -> {
            if (context == null) {
                context = SpringBuilder.getContext();
            }
            Map<String, T> beansOfType = context.getBeansOfType(providerClass);
            List<T> sortedProviders = new ArrayList<>(beansOfType.values());
            AnnotationAwareOrderComparator.sort(sortedProviders);
            return (Collection<Provider<?>>) sortedProviders;
        });
    }

}
