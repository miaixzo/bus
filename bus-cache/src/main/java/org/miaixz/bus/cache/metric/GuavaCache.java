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
package org.miaixz.bus.cache.metric;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.miaixz.bus.cache.CacheX;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * Guava 缓存支持
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class GuavaCache<K, V> implements CacheX<K, V> {

    private LoadingCache<String, Object> guavaCache;

    public GuavaCache(long size, long expire) {
        guavaCache = CacheBuilder.newBuilder().maximumSize(size).expireAfterWrite(expire, TimeUnit.MILLISECONDS)
                .build(new CacheLoader<>() {
                    @Override
                    public Object load(String key) {
                        return null;
                    }
                });
    }

    @Override
    public V read(K key) {
        return (V) guavaCache.getIfPresent(key);
    }

    @Override
    public Map<K, V> read(Collection<K> keys) {
        return (Map<K, V>) guavaCache.getAllPresent(keys);
    }

    @Override
    public void write(K key, V value, long expire) {
        guavaCache.put((String) key, value);
    }

    @Override
    public void write(Map<K, V> keyValueMap, long expire) {
        guavaCache.putAll((Map<? extends String, ?>) keyValueMap);
    }

    @Override
    public void remove(K... keys) {
        guavaCache.invalidateAll(Arrays.asList(keys));
    }

    @Override
    public void clear() {
        guavaCache.cleanUp();
    }

}
