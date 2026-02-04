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
package org.miaixz.bus.limiter.metric;

import java.lang.reflect.Method;

import org.miaixz.bus.core.cache.provider.TimedCache;
import org.miaixz.bus.core.xyz.CacheKit;
import org.miaixz.bus.core.xyz.MethodKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.limiter.Builder;
import org.miaixz.bus.limiter.Holder;
import org.miaixz.bus.limiter.Provider;
import org.miaixz.bus.limiter.magic.StrategyMode;

/**
 * Implements the {@link Provider} interface for handling the HOT_METHOD strategy mode. This provider is responsible for
 * caching method invocation results for a specified duration to optimize performance for frequently accessed methods
 * (hot methods).
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class MethodProvider implements Provider {

    /**
     * A timed cache to store results of hot method invocations. The cache key is a combination of the method name and
     * its arguments, and the value is the method's return result. Entries are automatically pruned after their
     * expiration time, which is configured by {@link Holder#load()}.getSeconds().
     */
    private final TimedCache<String, Object> cache;

    /**
     * Constructs a new {@code MethodProvider} and initializes the timed cache. The cache's time-to-live (TTL) is
     * determined by the {@code seconds} property loaded from the global {@link Holder} context. A pruning schedule is
     * also set up to clean expired entries.
     */
    public MethodProvider() {
        cache = CacheKit.newTimedCache(1000L * Holder.load().getSeconds());
        cache.schedulePrune(1000);
    }

    /**
     * Returns the strategy mode supported by this provider, which is {@link StrategyMode#HOT_METHOD}.
     *
     * @return The {@link StrategyMode#HOT_METHOD} enum value.
     */
    @Override
    public StrategyMode get() {
        return StrategyMode.HOT_METHOD;
    }

    /**
     * Processes the method invocation by first checking if the result is available in the cache. If the result is
     * cached and not expired, it is returned directly. Otherwise, the method is invoked, its result is stored in the
     * cache, and then returned.
     *
     * @param bean   The target object on which the method is invoked.
     * @param method The {@link Method} being invoked.
     * @param args   The arguments passed to the method invocation.
     * @return The result of the method invocation, either from cache or actual execution.
     */
    @Override
    public Object process(Object bean, Method method, Object[] args) {
        // Generate a unique key for the hot method based on its name and arguments
        String hotKey = StringKit.format(
                "{}-{}",
                Builder.resolveMethodName(method),
                org.miaixz.bus.crypto.Builder.md5Hex(JsonKit.toJsonString(args)));

        // Cache operation
        if (cache.containsKey(hotKey)) {
            return cache.get(hotKey, false);
        } else {
            // Execute the method and cache the result
            Object result = MethodKit.invoke(bean, method, args);
            cache.put(hotKey, result);
            return result;
        }
    }

}
