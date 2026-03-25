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
package org.miaixz.bus.cache.reader;

import org.miaixz.bus.cache.Builder;
import org.miaixz.bus.cache.magic.AnnoHolder;
import org.miaixz.bus.cache.magic.MethodHolder;
import org.miaixz.bus.cache.builtin.PreventObjects;
import org.miaixz.bus.core.lang.annotation.Singleton;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.proxy.invoker.ProxyChain;

/**
 * A cache reader for handling single-key cache operations.
 * <p>
 * This class implements the logic for methods that map to a single cache key. It handles cache hits, misses, and cache
 * penetration prevention scenarios. It also provides integration for hit rate statistics.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Singleton
public class SingleCacheReader extends AbstractReader {

    /**
     * Executes the single-key cache read operation.
     * <p>
     * It first attempts to read from the cache. On a miss, it invokes the original method, writes the result to the
     * cache (if configured to do so), and returns the result. It also handles cache penetration prevention by caching a
     * special placeholder for null results.
     * </p>
     *
     * @param annoHolder   The holder for the caching annotations.
     * @param methodHolder The holder for method metadata.
     * @param baseInvoker  The proxy chain invoker.
     * @param needWrite    If {@code true}, write the result to the cache on a miss.
     * @return The value from the cache or the result of the original method invocation.
     * @throws Throwable if the underlying method invocation throws an exception.
     */
    @Override
    public Object read(AnnoHolder annoHolder, MethodHolder methodHolder, ProxyChain baseInvoker, boolean needWrite)
            throws Throwable {
        String key = Builder.generateSingleKey(annoHolder, baseInvoker.getArguments());
        Object readResult = this.manage.readSingle(annoHolder.getCache(), key);
        doRecord(readResult, key, annoHolder);

        // Cache Hit
        if (null != readResult) {
            // Hit a penetration prevention placeholder
            if (PreventObjects.isPrevent(readResult)) {
                return null;
            }
            return readResult;
        }

        // Cache Miss
        Object invokeResult = doLogInvoke(baseInvoker::proceed);

        // Cache the return type for future use if not already known
        if (null != invokeResult && null == methodHolder.getInnerReturnType()) {
            methodHolder.setInnerReturnType(invokeResult.getClass());
        }

        // If writing is disabled (e.g., @CachedGet), return the result directly.
        if (!needWrite) {
            return invokeResult;
        }

        // If the result is not null, write it to the cache.
        if (null != invokeResult) {
            this.manage.writeSingle(annoHolder.getCache(), key, invokeResult, annoHolder.getExpire());
            return invokeResult;
        }

        // If the result is null and penetration prevention is on, cache the placeholder.
        if (this.context.isPreventOn()) {
            this.manage
                    .writeSingle(annoHolder.getCache(), key, PreventObjects.getPreventObject(), annoHolder.getExpire());
        }

        return null;
    }

    /**
     * Records cache hit and request counts for statistics.
     *
     * @param result     The result from the cache read (can be null for a miss).
     * @param key        The cache key.
     * @param annoHolder The annotation metadata.
     */
    private void doRecord(Object result, String key, AnnoHolder annoHolder) {
        Logger.info("single cache hit rate: {}/1, key: {}", null == result ? 0 : 1, key);
        if (null != this.collector) {
            String pattern = Builder.generatePattern(annoHolder);
            if (null != result) {
                this.collector.hitIncr(pattern, 1);
            }
            this.collector.reqIncr(pattern, 1);
        }
    }

}
