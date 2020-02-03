/*
 * The MIT License
 *
 * Copyright (c) 2015-2020 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.cache.metric;

import org.aoju.bus.cache.CacheX;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 内存缓存支持
 *
 * @author Kimi Liu
 * @version 5.5.5
 * @since JDK 1.8+
 */
public class MemoryCache implements CacheX {

    private ConcurrentMap<String, Object> map = new ConcurrentHashMap<>();

    @Override
    public Object read(String key) {
        return map.get(key);
    }

    @Override
    public void write(String key, Object value, long expire) {
        map.put(key, value);
    }

    @Override
    public Map<String, Object> read(Collection<String> keys) {
        Map<String, Object> subCache = new HashMap<>(keys.size());
        for (String key : keys) {
            subCache.put(key, read(key));
        }
        return subCache;
    }

    @Override
    public void write(Map<String, Object> keyValueMap, long expire) {
        map.putAll(keyValueMap);
    }

    @Override
    public void remove(String... keys) {
        for (String key : keys) {
            map.remove(key);
        }
    }

    @Override
    public void clear() {
        map.clear();
    }

}
