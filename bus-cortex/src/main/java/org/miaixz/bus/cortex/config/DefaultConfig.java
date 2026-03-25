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

import java.util.function.Consumer;

import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.cortex.Builder;
import org.miaixz.bus.cortex.Config;
import org.miaixz.bus.cortex.Vector;
import org.miaixz.bus.cortex.Listener;
import org.miaixz.bus.cortex.registry.WatchManager;
import org.miaixz.bus.extra.json.JsonKit;

/**
 * Default CacheX-backed configuration center implementation.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class DefaultConfig implements Config {

    /**
     * Publisher responsible for persisting versioned config changes.
     */
    private final ConfigPublisher configPublisher;
    /**
     * Shared cache used for direct config reads and gray-rule lookups.
     */
    private final CacheX<String, Object> cacheX;
    /**
     * Watch manager used to register and remove config subscriptions.
     */
    private final WatchManager watchManager;
    /**
     * Namespace served by this config-center instance.
     */
    private final String namespace;
    /**
     * Gray-rule matcher used to evaluate client-specific config delivery.
     */
    private final GrayRouter grayRouter = new GrayRouter();

    /**
     * Creates a DefaultConfig using the default namespace.
     *
     * @param configPublisher publisher for versioned config storage
     * @param cacheX          shared cache for direct reads
     * @param watchManager    watch manager for subscription support
     */
    public DefaultConfig(ConfigPublisher configPublisher, CacheX<String, Object> cacheX, WatchManager watchManager) {
        this(configPublisher, cacheX, watchManager, Builder.DEFAULT_NAMESPACE);
    }

    /**
     * Creates a DefaultConfig for a specific namespace.
     *
     * @param configPublisher publisher for versioned config storage
     * @param cacheX          shared cache for direct reads
     * @param watchManager    watch manager for subscription support
     * @param namespace       configuration namespace
     */
    public DefaultConfig(ConfigPublisher configPublisher, CacheX<String, Object> cacheX, WatchManager watchManager,
            String namespace) {
        this.configPublisher = configPublisher;
        this.cacheX = cacheX;
        this.watchManager = watchManager;
        this.namespace = namespace;
    }

    /**
     * Returns the current configuration value for the given group and dataId.
     *
     * @param group  configuration group
     * @param dataId configuration data identifier
     * @return current configuration content, or null if not found
     */
    @Override
    public String get(String group, String dataId) {
        String key = Builder.CFG_PREFIX + namespace + ":" + group + ":" + dataId;
        Object raw = cacheX.read(key);
        return raw != null ? (String) raw : null;
    }

    /**
     * Returns configuration content with optional gray-release routing.
     *
     * @param group    configuration group
     * @param dataId   configuration data identifier
     * @param clientIp IP address of the requesting client (used for gray routing)
     * @return gray content if applicable, otherwise the main configuration value
     */
    @Override
    public String get(String group, String dataId, String clientIp) {
        String grayKey = Builder.CFG_PREFIX + namespace + ":" + group + ":" + dataId + ":gray";
        Object rawGray = cacheX.read(grayKey);
        if (rawGray != null) {
            GrayRule rule = JsonKit.toPojo((String) rawGray, GrayRule.class);
            if (rule != null) {
                RequestContext ctx = new RequestContext();
                ctx.setClientIp(clientIp);
                if (grayRouter.matches(rule, ctx)) {
                    return rule.getGrayContent();
                }
            }
        }
        return get(group, dataId);
    }

    /**
     * Publishes new configuration content.
     *
     * @param group   configuration group
     * @param dataId  configuration data identifier
     * @param content new configuration content
     */
    @Override
    public void publish(String group, String dataId, String content) {
        configPublisher.publish(namespace, group, dataId, content);
    }

    /**
     * Rolls back configuration to a previously saved version snapshot.
     *
     * @param group   configuration group
     * @param dataId  configuration data identifier
     * @param version version number to restore
     */
    @Override
    public void rollback(String group, String dataId, long version) {
        String snapKey = Builder.CFG_PREFIX + namespace + ":" + group + ":" + dataId + ":ver:" + version;
        Object raw = cacheX.read(snapKey);
        if (raw != null) {
            ConfigVersion cv = JsonKit.toPojo((String) raw, ConfigVersion.class);
            if (cv != null) {
                configPublisher.publish(namespace, group, dataId, cv.getContent());
            }
        }
    }

    /**
     * Subscribes to changes on a configuration key and returns the watch ID.
     *
     * @param group    configuration group
     * @param dataId   configuration data identifier
     * @param listener callback invoked whenever the configuration value changes
     * @return watch identifier used to cancel the subscription
     */
    @Override
    public String watch(String group, String dataId, Consumer<String> listener) {
        Vector vector = new Vector();
        vector.setNamespace(namespace);
        vector.setId(group + ":" + dataId);
        Listener<String> wl = (added, removed, updated) -> {
            for (String item : added) {
                listener.accept(item);
            }
            for (String item : updated) {
                listener.accept(item);
            }
        };
        return watchManager.add(vector, wl);
    }

    /**
     * Cancels a configuration watch subscription.
     *
     * @param watchId watch identifier previously returned by {@link #watch}
     */
    @Override
    public void unwatch(String watchId) {
        watchManager.remove(watchId);
    }

}
