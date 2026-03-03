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
package org.miaixz.bus.core.bean.desc;

import org.miaixz.bus.core.center.map.reference.WeakConcurrentMap;
import org.miaixz.bus.core.lang.reflect.JdkProxy;
import org.miaixz.bus.core.xyz.ArrayKit;
import org.miaixz.bus.core.xyz.FieldKit;
import org.miaixz.bus.core.xyz.RecordKit;

/**
 * A factory for creating {@link BeanDesc} instances. It caches descriptors and selects the appropriate {@code BeanDesc}
 * implementation based on the class type:
 * <ul>
 * <li>For Java Records, it creates a {@link RecordBeanDesc}.</li>
 * <li>For proxy classes or classes without fields, it falls back to a {@link SimpleBeanDesc}.</li>
 * <li>For standard JavaBeans, it creates a {@link StrictBeanDesc}.</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class BeanDescFactory {

    /**
     * Constructs a new BeanDescFactory. Utility class constructor for static access.
     */
    private BeanDescFactory() {
    }

    /**
     * A weak concurrent map for caching {@code BeanDesc} instances.
     */
    private static final WeakConcurrentMap<Class<?>, BeanDesc> Cache = new WeakConcurrentMap<>();

    /**
     * Gets a cached {@link BeanDesc} for the given class. If the descriptor is not in the cache, a new one is created
     * and added.
     *
     * @param clazz The bean class.
     * @return The {@link BeanDesc} for the class.
     */
    public static BeanDesc getBeanDesc(final Class<?> clazz) {
        return Cache.computeIfAbsent(clazz, BeanDescFactory::getBeanDescWithoutCache);
    }

    /**
     * Gets a new {@link BeanDesc} for the given class without using the cache. This method selects the appropriate
     * descriptor implementation based on the class type.
     *
     * @param clazz The bean class.
     * @return A new {@link BeanDesc} instance.
     */
    public static BeanDesc getBeanDescWithoutCache(final Class<?> clazz) {
        if (RecordKit.isRecord(clazz)) {
            return new RecordBeanDesc(clazz);
        } else if (JdkProxy.isProxyClass(clazz) || ArrayKit.isEmpty(FieldKit.getFields(clazz))) {
            // Proxies and classes without fields are better handled by method-based descriptors.
            return new SimpleBeanDesc(clazz);
        } else {
            return new StrictBeanDesc(clazz);
        }
    }

    /**
     * Clears the global {@code BeanDesc} cache.
     */
    public static void clearCache() {
        Cache.clear();
    }

}
