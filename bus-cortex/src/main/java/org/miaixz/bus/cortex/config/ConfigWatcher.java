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
package org.miaixz.bus.cortex.config;

import java.util.List;

import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.cortex.Listener;
import org.miaixz.bus.logger.Logger;

/**
 * Watch listener that logs configuration changes.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class ConfigWatcher implements Listener<String> {

    /**
     * Shared cache available for supplemental config-watch lookups.
     */
    private final CacheX<String, Object> cacheX;

    /**
     * Creates a ConfigWatcher that may access the cache for additional lookups.
     *
     * @param cacheX shared cache (reserved for future use)
     */
    public ConfigWatcher(CacheX<String, Object> cacheX) {
        this.cacheX = cacheX;
    }

    /**
     * Logs each added, removed and updated configuration value.
     *
     * @param added   newly added configuration values
     * @param removed removed configuration values
     * @param updated updated configuration values
     */
    @Override
    public void accept(List<String> added, List<String> removed, List<String> updated) {
        for (String item : added) {
            Logger.info("Config added: {}", item);
        }
        for (String item : removed) {
            Logger.info("Config removed: {}", item);
        }
        for (String item : updated) {
            Logger.info("Config updated: {}", item);
        }
    }

}
