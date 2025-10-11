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
package org.miaixz.bus.pager.cache;

import java.lang.reflect.Constructor;
import java.util.Properties;

import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.cache.metric.CaffeineCache;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.PageException;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.pager.Property;

/**
 * Factory for creating and configuring cache instances for SQL caching. It supports creating caches based on a
 * specified class name or defaulting to {@link CaffeineCache} or {@link SimpleCache}.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class CacheFactory {

    /**
     * Creates a SQL cache instance based on the provided class name and properties. If {@code sqlCacheClass} is empty,
     * it attempts to create a {@link CaffeineCache}, falling back to {@link SimpleCache} if Caffeine is not available.
     * If {@code sqlCacheClass} is provided, it attempts to instantiate that class, supporting constructors with
     * {@link Properties} and {@link String} arguments, or a default constructor.
     *
     * @param <K>           the type of keys maintained by this cache
     * @param <V>           the type of mapped values
     * @param sqlCacheClass the fully qualified class name of the cache implementation to create. Can be null or empty
     *                      to use default.
     * @param prefix        a prefix for properties, used to distinguish cache-specific properties.
     * @param properties    the properties to configure the cache instance.
     * @return a new instance of {@link CacheX}
     * @throws PageException if properties are empty or if there is an error creating the cache instance.
     */
    public static <K, V> CacheX<K, V> createCache(String sqlCacheClass, String prefix, Properties properties) {
        if (ObjectKit.isEmpty(properties)) {
            throw new PageException("Properties is empty");
        }
        properties.setProperty("prefix", StringKit.isNotEmpty(prefix) ? prefix + Symbol.DOT : Normal.EMPTY);
        if (StringKit.isEmpty(sqlCacheClass)) {
            try {
                return new CaffeineCache<>(properties);
            } catch (Throwable t) {
                return new SimpleCache<>(properties);
            }
        } else {
            try {
                Class<? extends CacheX> clazz = (Class<? extends CacheX>) Class.forName(sqlCacheClass);
                try {
                    Constructor<? extends CacheX> constructor = clazz.getConstructor(Properties.class, String.class);
                    return constructor.newInstance(properties);
                } catch (Exception e) {
                    CacheX cache = clazz.newInstance();
                    if (cache instanceof Property) {
                        ((Property) cache).setProperties(properties);
                    }
                    return cache;
                }
            } catch (Throwable t) {
                throw new PageException("Created Sql Cache [" + sqlCacheClass + "] Error", t);
            }
        }
    }

}
