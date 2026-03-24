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
package org.miaixz.bus.image.metric.api;

import java.util.HashMap;
import java.util.Map;

import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.exception.NotFoundException;

/**
 * @author Kimi Liu
 * @since Java 21+
 */
public abstract class ConfigurationCache<C, T> {

    private final Map<String, CacheEntry<T>> cache = new HashMap<>();
    private final C conf;
    private long staleTimeout;

    public ConfigurationCache(C conf) {
        if (conf == null)
            throw new NullPointerException();
        this.conf = conf;
    }

    public int getStaleTimeout() {
        return (int) (staleTimeout / 1000);
    }

    public void setStaleTimeout(int staleTimeout) {
        this.staleTimeout = staleTimeout * 1000L;
    }

    public void clear() {
        cache.clear();
    }

    public T get(String key) throws InternalException {
        long now = System.currentTimeMillis();
        CacheEntry<T> entry = cache.get(key);
        if (entry == null || (staleTimeout != 0 && now > entry.fetchTime + staleTimeout)) {
            T value = null;
            try {
                value = find(conf, key);
            } catch (NotFoundException e) {
            }
            entry = new CacheEntry<T>(value, now);
            cache.put(key, entry);
        }
        return entry.value;
    }

    protected abstract T find(C conf, String key) throws InternalException;

    private static final class CacheEntry<T> {

        final T value;
        final long fetchTime;

        CacheEntry(T value, long fetchTime) {
            this.value = value;
            this.fetchTime = fetchTime;
        }
    }

}
