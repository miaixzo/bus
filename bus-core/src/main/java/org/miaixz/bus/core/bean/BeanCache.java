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
package org.miaixz.bus.core.bean;

import java.beans.PropertyDescriptor;
import java.util.Map;

import org.miaixz.bus.core.center.function.SupplierX;
import org.miaixz.bus.core.center.map.reference.ReferenceConcurrentMap;
import org.miaixz.bus.core.center.map.reference.WeakConcurrentMap;

/**
 * A cache for Bean properties, designed to prevent performance issues caused by repeated reflection.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public enum BeanCache {

    /**
     * The singleton instance of {@code BeanCache}.
     */
    INSTANCE;

    /**
     * Cache for {@link PropertyDescriptor}s, keyed by class, where property names are case-sensitive. Uses
     * {@link WeakConcurrentMap} for automatic cleanup of entries when the class is no longer referenced.
     */
    private final WeakConcurrentMap<Class<?>, Map<String, PropertyDescriptor>> pdCache = new WeakConcurrentMap<>();
    /**
     * Cache for {@link PropertyDescriptor}s, keyed by class, where property names are case-insensitive. Uses
     * {@link WeakConcurrentMap} for automatic cleanup of entries when the class is no longer referenced.
     */
    private final WeakConcurrentMap<Class<?>, Map<String, PropertyDescriptor>> ignoreCasePdCache = new WeakConcurrentMap<>();

    /**
     * Retrieves a map of property names to their corresponding {@link PropertyDescriptor}s for a given bean class.
     *
     * @param beanClass  The class of the bean.
     * @param ignoreCase Whether to ignore case when matching property names.
     * @return A map where keys are property names (or their lowercase versions if {@code ignoreCase} is true) and
     *         values are {@link PropertyDescriptor} objects.
     */
    public Map<String, PropertyDescriptor> getPropertyDescriptorMap(
            final Class<?> beanClass,
            final boolean ignoreCase) {
        return getCache(ignoreCase).get(beanClass);
    }

    /**
     * Retrieves a map of property names to their corresponding {@link PropertyDescriptor}s for a given bean class. If
     * the map is not already in the cache, it is computed using the provided supplier and then cached.
     *
     * @param beanClass  The class of the bean.
     * @param ignoreCase Whether to ignore case when matching property names.
     * @param supplier   A {@link SupplierX} function to compute the map if it's not found in the cache.
     * @return A map where keys are property names (or their lowercase versions if {@code ignoreCase} is true) and
     *         values are {@link PropertyDescriptor} objects.
     */
    public Map<String, PropertyDescriptor> getPropertyDescriptorMap(
            final Class<?> beanClass,
            final boolean ignoreCase,
            final SupplierX<Map<String, PropertyDescriptor>> supplier) {
        return getCache(ignoreCase).computeIfAbsent(beanClass, (key) -> supplier.get());
    }

    /**
     * Puts a map of property names to {@link PropertyDescriptor}s into the cache for a given bean class.
     *
     * @param beanClass                      The class of the bean.
     * @param fieldNamePropertyDescriptorMap The map of property names to {@link PropertyDescriptor}s to cache.
     * @param ignoreCase                     Whether the cached map should be associated with case-insensitive property
     *                                       names.
     */
    public void putPropertyDescriptorMap(
            final Class<?> beanClass,
            final Map<String, PropertyDescriptor> fieldNamePropertyDescriptorMap,
            final boolean ignoreCase) {
        getCache(ignoreCase).put(beanClass, fieldNamePropertyDescriptorMap);
    }

    /**
     * Clears all cached {@link PropertyDescriptor} maps from both case-sensitive and case-insensitive caches.
     */
    public void clear() {
        this.pdCache.clear();
        this.ignoreCasePdCache.clear();
    }

    /**
     * Returns the appropriate cache (case-sensitive or case-insensitive) based on the {@code ignoreCase} flag.
     *
     * @param ignoreCase Whether to retrieve the case-insensitive cache.
     * @return The {@link ReferenceConcurrentMap} instance to use.
     */
    private ReferenceConcurrentMap<Class<?>, Map<String, PropertyDescriptor>> getCache(final boolean ignoreCase) {
        return ignoreCase ? ignoreCasePdCache : pdCache;
    }

}
