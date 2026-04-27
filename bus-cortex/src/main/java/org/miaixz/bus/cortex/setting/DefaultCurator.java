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
package org.miaixz.bus.cortex.setting;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.miaixz.bus.cortex.Builder;
import org.miaixz.bus.cortex.Curator;
import org.miaixz.bus.cortex.Listener;
import org.miaixz.bus.cortex.Vector;
import org.miaixz.bus.cortex.Watch;
import org.miaixz.bus.cortex.setting.curator.ItemCuratorService;
import org.miaixz.bus.cortex.setting.delivery.RuntimeItemOverlayService;
import org.miaixz.bus.cortex.setting.item.Item;
import org.miaixz.bus.cortex.setting.item.ItemBindingProjection;
import org.miaixz.bus.cortex.setting.item.GrayRequestContext;
import org.miaixz.bus.cortex.setting.item.ItemKeys;
import org.miaixz.bus.cortex.setting.item.ItemQuery;
import org.miaixz.bus.cortex.magic.identity.CortexIdentity;
import org.miaixz.bus.cortex.magic.watch.WatchManager;

/**
 * Default consumer-facing curator implementation.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class DefaultCurator implements Curator {

    /**
     * Application service that backs setting reads, writes, and history operations.
     */
    private final ItemCuratorService settingCuratorService;
    /**
     * Watch manager used to register and remove setting subscriptions.
     */
    private final WatchManager watchManager;
    /**
     * Namespace identifier served by this curator instance.
     */
    private final String namespace_id;
    /**
     * Optional runtime overlay service consulted before durable setting resolution.
     */
    private final RuntimeItemOverlayService runtimeSettingOverlayService;

    /**
     * Creates a DefaultCurator using the default namespace.
     *
     * @param settingCuratorService curator application service
     * @param watchManager          watch manager for subscription support
     */
    public DefaultCurator(ItemCuratorService settingCuratorService, WatchManager watchManager) {
        this(settingCuratorService, watchManager, Builder.DEFAULT_NAMESPACE, null);
    }

    /**
     * Creates a DefaultCurator for a specific namespace.
     *
     * @param settingCuratorService curator application service
     * @param watchManager          watch manager for subscription support
     * @param namespace_id          setting namespace identifier
     */
    public DefaultCurator(ItemCuratorService settingCuratorService, WatchManager watchManager, String namespace_id) {
        this(settingCuratorService, watchManager, namespace_id, null);
    }

    /**
     * Creates a DefaultCurator for a specific namespace with runtime overlay support.
     *
     * @param settingCuratorService        curator application service
     * @param watchManager                 watch manager for subscription support
     * @param namespace_id                 setting namespace identifier
     * @param runtimeSettingOverlayService runtime overlay service
     */
    public DefaultCurator(ItemCuratorService settingCuratorService, WatchManager watchManager, String namespace_id,
            RuntimeItemOverlayService runtimeSettingOverlayService) {
        this.settingCuratorService = settingCuratorService;
        this.watchManager = watchManager;
        this.namespace_id = CortexIdentity.namespace(namespace_id);
        this.runtimeSettingOverlayService = runtimeSettingOverlayService;
    }

    /**
     * Returns the current setting value for the given group and data_id.
     *
     * @param group   setting group
     * @param data_id setting data identifier
     * @return current setting content, or null if not found
     */
    @Override
    public String get(String group, String data_id) {
        String overlay = runtimeOverlay(group, data_id, null);
        if (overlay != null) {
            return overlay;
        }
        ItemQuery query = new ItemQuery();
        query.setNamespace_id(namespace_id);
        query.setGroup(group);
        query.setData_id(data_id);
        return settingCuratorService.resolve(query);
    }

    /**
     * Returns setting content with optional gray-release routing.
     *
     * @param group    setting group
     * @param data_id  setting data identifier
     * @param clientIp IP address of the requesting client (used for gray routing)
     * @return gray content if applicable, otherwise the main setting value
     */
    @Override
    public String get(String group, String data_id, String clientIp) {
        return get(group, data_id, null, clientIp);
    }

    /**
     * Returns one setting value for the given group, data_id, and profile without gray-routing context.
     *
     * @param group   setting group
     * @param data_id setting data identifier
     * @param profile optional profile
     * @return resolved setting value
     */
    @Override
    public String getProfile(String group, String data_id, String profile) {
        String overlay = runtimeOverlay(group, data_id, profile);
        if (overlay != null) {
            return overlay;
        }
        ItemQuery query = new ItemQuery();
        query.setNamespace_id(namespace_id);
        query.setGroup(group);
        query.setData_id(data_id);
        query.setProfile_id(profile);
        return settingCuratorService.resolve(query);
    }

    /**
     * Returns one setting value for the given group, data_id, profile, and client IP.
     *
     * @param group    setting group
     * @param data_id  setting data identifier
     * @param profile  optional profile
     * @param clientIp IP address of the requesting client
     * @return resolved setting value
     */
    @Override
    public String get(String group, String data_id, String profile, String clientIp) {
        String overlay = runtimeOverlay(group, data_id, profile);
        if (overlay != null) {
            return overlay;
        }
        ItemQuery query = new ItemQuery();
        query.setNamespace_id(namespace_id);
        query.setGroup(group);
        query.setData_id(data_id);
        query.setProfile_id(profile);
        GrayRequestContext context = new GrayRequestContext();
        context.setClientIp(clientIp);
        query.setRequestContext(context);
        return settingCuratorService.resolve(query);
    }

    /**
     * Publishes new setting content.
     *
     * @param group   setting group
     * @param data_id setting data identifier
     * @param content new setting content
     */
    @Override
    public void publish(String group, String data_id, String content) {
        publish(group, data_id, null, content);
    }

    /**
     * Publishes one setting value for the given group and data_id with an optional profile binding.
     *
     * @param group   setting group
     * @param data_id setting data identifier
     * @param profile optional profile
     * @param content new setting content
     */
    @Override
    public void publish(String group, String data_id, String profile, String content) {
        Item entry = new Item();
        entry.setNamespace_id(namespace_id);
        entry.setGroup(group);
        entry.setData_id(data_id);
        ItemBindingProjection.normalizeProfileIdsInto(entry, profile);
        entry.setContent(content);
        settingCuratorService.publish(entry);
    }

    /**
     * Returns multiple setting values keyed by `group:data_id[:profile]`.
     *
     * @param groupAndDataIds logical setting keys
     * @return resolved setting values
     */
    @Override
    public Map<String, String> batchGet(List<String> groupAndDataIds) {
        Map<String, String> result = new LinkedHashMap<>();
        if (groupAndDataIds == null) {
            return result;
        }
        for (String key : groupAndDataIds) {
            if (key == null || key.isBlank()) {
                continue;
            }
            String[] parts = key.split(":", 3);
            if (parts.length < 2) {
                continue;
            }
            String profile = parts.length > 2 ? parts[2] : null;
            result.put(key, getProfile(parts[0], parts[1], profile));
        }
        return result;
    }

    /**
     * Rolls back one setting value without an explicit profile.
     *
     * @param group    setting group
     * @param data_id  setting data identifier
     * @param revision historical revision number
     */
    @Override
    public void rollback(String group, String data_id, String revision) {
        rollback(group, data_id, null, revision);
    }

    /**
     * Rolls back one setting value for the given profile to a historical revision.
     *
     * @param group    setting group
     * @param data_id  setting data identifier
     * @param profile  optional profile
     * @param revision historical revision number
     */
    @Override
    public void rollback(String group, String data_id, String profile, String revision) {
        settingCuratorService.rollback(namespace_id, group, data_id, profile, revision);
    }

    /**
     * Subscribes to changes on a setting key and returns the watch ID.
     *
     * @param group    setting group
     * @param data_id  setting data identifier
     * @param listener callback invoked whenever the setting value changes
     * @return watch identifier used to cancel the subscription
     */
    @Override
    public String watch(String group, String data_id, Consumer<String> listener) {
        Vector vector = new Vector();
        vector.setNamespace_id(namespace_id);
        vector.setId(ItemKeys.watchKeyForScope(namespace_id, group, data_id, null));
        Listener<Watch<String>> wl = event -> {
            for (String item : event.getAdded()) {
                listener.accept(item);
            }
            for (String item : event.getUpdated()) {
                listener.accept(item);
            }
            if (!event.getRemoved().isEmpty()) {
                listener.accept(null);
            }
        };
        return watchManager.add(vector, wl);
    }

    /**
     * Cancels a setting watch subscription.
     *
     * @param watch_id watch identifier previously returned by {@link #watch}
     */
    @Override
    public void unwatch(String watch_id) {
        watchManager.remove(watch_id);
    }

    /**
     * Resolves one runtime overlay when configured.
     *
     * @param group   setting group
     * @param data_id setting data identifier
     * @param profile optional profile
     * @return runtime overlay, or {@code null} when absent
     */
    private String runtimeOverlay(String group, String data_id, String profile) {
        if (runtimeSettingOverlayService == null) {
            return null;
        }
        return runtimeSettingOverlayService.resolveRuntimeOverlay(namespace_id, group, data_id, profile);
    }

}
