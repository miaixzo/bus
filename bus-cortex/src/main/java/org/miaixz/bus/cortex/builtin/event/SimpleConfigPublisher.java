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
package org.miaixz.bus.cortex.builtin.event;

import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.cortex.Builder;

/**
 * Lightweight config publisher that writes directly to the shared cache without version tracking.
 * <p>
 * Use {@code org.miaixz.bus.cortex.config.ConfigPublisher} when versioned history is required.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SimpleConfigPublisher {

    /**
     * Shared cache used to store published configuration content.
     */
    private final CacheX<String, Object> cacheX;

    /**
     * Creates a SimpleConfigPublisher backed by the given CacheX.
     *
     * @param cacheX shared cache used to store configuration content
     */
    public SimpleConfigPublisher(CacheX<String, Object> cacheX) {
        this.cacheX = cacheX;
    }

    /**
     * Publishes configuration content to the given key.
     *
     * @param namespace configuration namespace
     * @param group     configuration group
     * @param dataId    configuration data identifier
     * @param content   configuration content
     */
    public void publish(String namespace, String group, String dataId, String content) {
        String key = Builder.CFG_PREFIX + namespace + ":" + group + ":" + dataId;
        cacheX.write(key, content, 0L);
    }

}
