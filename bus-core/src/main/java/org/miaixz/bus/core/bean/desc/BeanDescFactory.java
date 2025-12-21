/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
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
