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
package org.miaixz.bus.cortex.magic.identity;

import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.cortex.Builder;

/**
 * Atomic sequence generator backed by CacheX.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Sequence {

    /**
     * Shared cache used to maintain named sequence counters.
     */
    private final CacheX<String, Object> cacheX;

    /**
     * Creates a new Sequence backed by the given CacheX.
     *
     * @param cacheX shared cache used to maintain counters
     */
    public Sequence(CacheX<String, Object> cacheX) {
        this.cacheX = cacheX;
    }

    /**
     * Returns the next sequence value for the given key.
     *
     * @param key logical sequence name
     * @return monotonically increasing sequence number
     */
    public long next(String key) {
        return cacheX.increment(Builder.SEQUENCE_PREFIX + key);
    }

}
