/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2023 aoju.org mybatis.io and other contributors.           *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.aoju.bus.pager.cache;

import com.google.common.cache.CacheBuilder;
import org.aoju.bus.core.toolkit.StringKit;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * Simple Guava Cache
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class GuavaCache<K, V> implements Cache<K, V> {

    private final com.google.common.cache.Cache<K, V> CACHE;

    public GuavaCache(Properties properties, String prefix) {
        CacheBuilder cacheBuilder = CacheBuilder.newBuilder();
        String maximumSize = properties.getProperty(prefix + ".maximumSize");
        if (StringKit.isNotEmpty(maximumSize)) {
            cacheBuilder.maximumSize(Long.parseLong(maximumSize));
        } else {
            cacheBuilder.maximumSize(1000);
        }
        String expireAfterAccess = properties.getProperty(prefix + ".expireAfterAccess");
        if (StringKit.isNotEmpty(expireAfterAccess)) {
            cacheBuilder.expireAfterAccess(Long.parseLong(expireAfterAccess), TimeUnit.MILLISECONDS);
        }
        String expireAfterWrite = properties.getProperty(prefix + ".expireAfterWrite");
        if (StringKit.isNotEmpty(expireAfterWrite)) {
            cacheBuilder.expireAfterWrite(Long.parseLong(expireAfterWrite), TimeUnit.MILLISECONDS);
        }
        String initialCapacity = properties.getProperty(prefix + ".initialCapacity");
        if (StringKit.isNotEmpty(initialCapacity)) {
            cacheBuilder.initialCapacity(Integer.parseInt(initialCapacity));
        }
        CACHE = cacheBuilder.build();
    }

    @Override
    public V get(K key) {
        return CACHE.getIfPresent(key);
    }

    @Override
    public void put(K key, V value) {
        CACHE.put(key, value);
    }

}
