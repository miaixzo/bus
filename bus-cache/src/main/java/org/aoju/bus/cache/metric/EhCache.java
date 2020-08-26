/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
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
 ********************************************************************************/
package org.aoju.bus.cache.metric;

import com.google.common.collect.Sets;
import org.aoju.bus.cache.CacheX;
import org.ehcache.PersistentCacheManager;
import org.ehcache.config.CacheConfiguration;
import org.ehcache.config.ResourcePools;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.EntryUnit;
import org.ehcache.config.units.MemoryUnit;

import javax.annotation.PreDestroy;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * EhCache进程内缓存框架,
 * 具有快速、精干等特点
 *
 * @author Kimi Liu
 * @version 6.0.8
 * @since JDK 1.8+
 */
public class EhCache implements CacheX {

    private PersistentCacheManager cacheManager;
    private org.ehcache.Cache ehcache;

    public EhCache(long heapEntries, long offHeapMBSize, long diskGBSize) {
        this(heapEntries, offHeapMBSize, System.getProperty("user.home") + "/.EhCache", diskGBSize);
    }

    public EhCache(long heapEntries, long offHeapMBSize, String diskPath, long diskGBSize) {

        ResourcePools resourcePools = ResourcePoolsBuilder.newResourcePoolsBuilder()
                .heap(heapEntries, EntryUnit.ENTRIES)
                .offheap(offHeapMBSize, MemoryUnit.MB)
                .disk(diskGBSize, MemoryUnit.GB)
                .build();

        CacheConfiguration<String, Serializable> configuration = CacheConfigurationBuilder
                .newCacheConfigurationBuilder(String.class, Serializable.class, resourcePools)
                .build();

        cacheManager = CacheManagerBuilder.newCacheManagerBuilder()
                .with(CacheManagerBuilder.persistence(diskPath))
                .withCache("cache", configuration)
                .build(true);

        ehcache = cacheManager.getCache("cache", String.class, Serializable.class);
    }

    @Override
    public Object read(String key) {
        return ehcache.get(key);
    }

    @Override
    public void write(String key, Object value, long expire) {
        ehcache.put(key, value);
    }

    @Override
    public Map<String, Object> read(Collection<String> keys) {
        Map<String, Object> map = new HashMap<>(keys.size());
        for (String key : keys) {
            map.put(key, ehcache.get(key));
        }

        return map;
    }

    @Override
    public void write(Map<String, Object> keyValueMap, long expire) {
        Map<String, Serializable> map = new HashMap<>(keyValueMap.size());
        for (Map.Entry<String, Object> entry : keyValueMap.entrySet()) {
            Object value = entry.getValue();
            map.put(entry.getKey(), (Serializable) value);
        }
        ehcache.putAll(map);
    }

    @Override
    public void remove(String... keys) {
        ehcache.removeAll(Sets.newHashSet(keys));
    }

    @Override
    public void clear() {
        ehcache.clear();
    }

    @PreDestroy
    public void tearDown() {
        if (this.cacheManager != null) {
            this.cacheManager.close();
        }
    }

}
