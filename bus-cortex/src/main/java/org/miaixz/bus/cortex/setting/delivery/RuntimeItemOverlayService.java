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
package org.miaixz.bus.cortex.setting.delivery;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.miaixz.bus.cortex.magic.identity.CortexIdentity;
import org.miaixz.bus.cortex.magic.watch.WatchManager;
import org.miaixz.bus.cortex.setting.item.ItemBindingProjection;
import org.miaixz.bus.cortex.setting.item.Item;
import org.miaixz.bus.cortex.setting.item.ItemKeys;

/**
 * Lightweight runtime overlay publisher outside the revision-tracked curator write path.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class RuntimeItemOverlayService {

    /**
     * Watch-event source name used for runtime overlays.
     */
    private static final String OVERLAY_SOURCE = "runtime-overlay";
    /**
     * Watch-event type emitted after one runtime overlay publish.
     */
    private static final String OVERLAY_PUBLISH_EVENT = "runtime-overlay-publish";
    /**
     * Watch-event type emitted after one runtime overlay clear operation.
     */
    private static final String OVERLAY_CLEAR_EVENT = "runtime-overlay-clear";

    /**
     * Lightweight cache-backed publisher used for runtime overlays and warmup snapshots.
     */
    private final RuntimeItemOverlayPublisher publisher;
    /**
     * Watch manager notified when one overlay is refreshed.
     */
    private final WatchManager watchManager;

    /**
     * Creates one runtime overlay service.
     *
     * @param publisher    lightweight overlay publisher
     * @param watchManager watch manager
     */
    public RuntimeItemOverlayService(RuntimeItemOverlayPublisher publisher, WatchManager watchManager) {
        this.publisher = publisher;
        this.watchManager = watchManager;
    }

    /**
     * Publishes one runtime overlay without creating {@code setting.item.revision} snapshots.
     *
     * @param namespace namespace
     * @param group     setting group
     * @param data_id   setting data identifier
     * @param profile   optional {@code setting.profile}
     * @param content   overlay content
     */
    public void publishRuntimeOverlay(String namespace, String group, String data_id, String profile, String content) {
        publishRuntimeOverlay(namespace, group, data_id, profile, content, 0L);
    }

    /**
     * Publishes one runtime overlay with an optional explicit TTL.
     *
     * @param namespace namespace
     * @param group     setting group
     * @param data_id   setting data identifier
     * @param profile   optional profile
     * @param content   overlay content
     * @param ttlMs     explicit ttl in milliseconds, {@code 0} keeps the publisher default
     */
    public void publishRuntimeOverlay(
            String namespace,
            String group,
            String data_id,
            String profile,
            String content,
            long ttlMs) {
        String resolvedNamespace = CortexIdentity.namespace(namespace);
        publisher.publish(resolvedNamespace, group, data_id, profile, content, ttlMs);
        watchManager.notifySetting(
                ItemKeys.watchKeyForScope(resolvedNamespace, group, data_id, profile),
                content,
                OVERLAY_SOURCE,
                OVERLAY_PUBLISH_EVENT,
                "Runtime overlay updated");
    }

    /**
     * Warms one runtime overlay snapshot without creating {@code setting.item.revision} snapshots.
     *
     * @param entries snapshot entries
     */
    public void warmupSnapshot(List<Item> entries) {
        if (entries == null) {
            return;
        }
        for (Item entry : entries) {
            if (entry == null || entry.getGroup() == null || entry.getData_id() == null) {
                continue;
            }
            List<String> profiles = ItemBindingProjection.normalizedProfileIds(entry);
            if (profiles == null || profiles.isEmpty()) {
                publishRuntimeOverlay(
                        entry.getNamespace_id(),
                        entry.getGroup(),
                        entry.getData_id(),
                        null,
                        entry.getContent());
                continue;
            }
            for (String profile : profiles) {
                publishRuntimeOverlay(
                        entry.getNamespace_id(),
                        entry.getGroup(),
                        entry.getData_id(),
                        profile,
                        entry.getContent());
            }
        }
    }

    /**
     * Resolves one runtime overlay when present.
     *
     * @param namespace namespace
     * @param group     setting group
     * @param data_id   setting data identifier
     * @param profile   optional {@code setting.profile}
     * @return runtime overlay, or {@code null} when absent
     */
    public String resolveRuntimeOverlay(String namespace, String group, String data_id, String profile) {
        return publisher.get(CortexIdentity.namespace(namespace), group, data_id, profile);
    }

    /**
     * Returns overlay presence for one scope.
     *
     * @param namespace namespace
     * @param group     group
     * @param data_id   data identifier
     * @param profile   profile
     * @return overlay view
     */
    public Map<String, Object> describe(String namespace, String group, String data_id, String profile) {
        Map<String, Object> result = new LinkedHashMap<>();
        String content = resolveRuntimeOverlay(namespace, group, data_id, profile);
        result.put("key", ItemKeys.overlayKeyForScope(namespace, group, data_id, profile));
        result.put("present", content != null);
        result.put("content", content);
        return result;
    }

    /**
     * Deletes one runtime overlay and notifies watchers.
     *
     * @param namespace namespace
     * @param group     group
     * @param data_id   data identifier
     * @param profile   profile
     */
    public void clearRuntimeOverlay(String namespace, String group, String data_id, String profile) {
        String resolvedNamespace = CortexIdentity.namespace(namespace);
        publisher.delete(resolvedNamespace, group, data_id, profile);
        watchManager.notifySetting(
                ItemKeys.watchKeyForScope(resolvedNamespace, group, data_id, profile),
                null,
                OVERLAY_SOURCE,
                OVERLAY_CLEAR_EVENT,
                "Runtime overlay cleared");
    }

}
