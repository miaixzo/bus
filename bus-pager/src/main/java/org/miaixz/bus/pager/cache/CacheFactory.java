/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org mybatis.io and other contributors.         ~
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

import org.miaixz.bus.core.lang.exception.PageException;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.pager.Property;

/**
 * CacheFactory
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class CacheFactory {

    /**
     * 创建 SQL 缓存
     *
     * @param <K>           对象
     * @param <V>           对象
     * @param sqlCacheClass 对象
     * @param prefix        前缀
     * @param properties    属性
     * @return the object
     */
    public static <K, V> Cache<K, V> createCache(String sqlCacheClass, String prefix, Properties properties) {
        if (StringKit.isEmpty(sqlCacheClass)) {
            try {
                Class.forName("com.google.common.cache.Cache");
                return new GuavaCache<>(properties, prefix);
            } catch (Throwable t) {
                return new SimpleCache<>(properties, prefix);
            }
        } else {
            try {
                Class<? extends Cache> clazz = (Class<? extends Cache>) Class.forName(sqlCacheClass);
                try {
                    Constructor<? extends Cache> constructor = clazz.getConstructor(Properties.class, String.class);
                    return constructor.newInstance(properties, prefix);
                } catch (Exception e) {
                    Cache cache = clazz.newInstance();
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
