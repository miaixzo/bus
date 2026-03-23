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

import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.cortex.Builder;
import org.miaixz.bus.cortex.registry.WatchManager;
import org.miaixz.bus.extra.json.JsonKit;

/**
 * Versioned configuration publisher with snapshot history.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ConfigPublisher {

    /**
     * Shared cache used to persist config values and version snapshots.
     */
    private final CacheX<String, Object> cacheX;
    /**
     * Watch manager notified after config content is published.
     */
    private final WatchManager watchManager;
    /**
     * Maximum number of historical versions retained per config key.
     */
    private final int maxVersions;

    /**
     * Creates a ConfigPublisher with default max version history (10 snapshots).
     *
     * @param cacheX       shared cache for configuration and version storage
     * @param watchManager watch manager to notify on publish
     */
    public ConfigPublisher(CacheX<String, Object> cacheX, WatchManager watchManager) {
        this(cacheX, watchManager, 10);
    }

    /**
     * Creates a ConfigPublisher with an explicit maximum version history size.
     *
     * @param cacheX       shared cache for configuration and version storage
     * @param watchManager watch manager to notify on publish
     * @param maxVersions  maximum number of historical snapshots to retain per config key
     */
    public ConfigPublisher(CacheX<String, Object> cacheX, WatchManager watchManager, int maxVersions) {
        this.cacheX = cacheX;
        this.watchManager = watchManager;
        this.maxVersions = maxVersions;
    }

    /**
     * Publishes new configuration content, maintaining version history.
     *
     * @param namespace configuration namespace
     * @param group     configuration group
     * @param dataId    configuration data identifier
     * @param content   configuration content
     */
    public void publish(String namespace, String group, String dataId, String content) {
        String mainKey = Builder.CFG_PREFIX + namespace + ":" + group + ":" + dataId;
        String seqKey = Builder.SEQUENCE_PREFIX + "config:" + namespace + ":" + group + ":" + dataId;
        long version = cacheX.increment(seqKey);
        String snapKey = mainKey + ":ver:" + version;
        cacheX.write(mainKey, content, 0L);
        ConfigVersion cv = new ConfigVersion(content, version, "system", System.currentTimeMillis());
        cacheX.write(snapKey, JsonKit.toJsonString(cv), 0L);
        if (version > maxVersions) {
            long oldVersion = version - maxVersions;
            cacheX.remove(mainKey + ":ver:" + oldVersion);
        }
        watchManager.notifyConfig(mainKey, content);
    }

}
