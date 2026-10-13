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

import org.miaixz.bus.cortex.Keying;
import org.miaixz.bus.cortex.Keying.SettingSpec;
import org.miaixz.bus.cortex.builtin.SettingGenerator;
import org.miaixz.bus.cortex.magic.watch.WatchManager;
import org.miaixz.bus.cortex.setting.item.Item;
import org.miaixz.bus.cortex.setting.item.ItemNormalizer;

/**
 * Runtime overlay publication outside the revision-tracked configuration write path.
 *
 * @author Kimi Liu
 */
public class RuntimeItemOverlayService {

    private static final String OVERLAY_SOURCE = "runtime-overlay";
    private static final String OVERLAY_PUBLISH_EVENT = "runtime-overlay-publish";
    private static final String OVERLAY_CLEAR_EVENT = "runtime-overlay-clear";
    private final RuntimeItemOverlayPublisher publisher;
    private final WatchManager watchManager;
    private final Keying<SettingSpec> keying;

    /**
     * Creates a runtime overlay service with the default key strategy.
     *
     * @param publisher cache-backed overlay publisher
     * @param watchManager watch notification manager
     */
    public RuntimeItemOverlayService(RuntimeItemOverlayPublisher publisher, WatchManager watchManager) {
        this(publisher, watchManager, SettingGenerator.INSTANCE);
    }

    /**
     * Creates a runtime overlay service.
     *
     * @param publisher cache-backed overlay publisher
     * @param watchManager watch notification manager
     * @param keying setting key strategy
     */
    public RuntimeItemOverlayService(RuntimeItemOverlayPublisher publisher, WatchManager watchManager,
            Keying<SettingSpec> keying) {
        this.publisher = publisher;
        this.watchManager = watchManager;
        this.keying = keying == null ? SettingGenerator.INSTANCE : keying;
    }

    /**
     * Publishes an overlay using the publisher's default lifetime.
     *
     * @param item tenant-qualified item and overlay content
     */
    public void publishRuntimeOverlay(Item item) {
        publishRuntimeOverlay(item, 0L);
    }

    /**
     * Publishes an overlay and notifies watchers of the same item coordinates.
     *
     * @param item tenant-qualified item and overlay content
     * @param ttlMs time-to-live in milliseconds; zero uses the publisher default
     */
    public void publishRuntimeOverlay(Item item, long ttlMs) {
        ItemNormalizer.coordinates(item);
        publisher.publish(item, ttlMs);
        watchManager.notifySetting(item.getTenant_id(), keying.key(SettingSpec.watch(item)), item.getContent(),
                OVERLAY_SOURCE, OVERLAY_PUBLISH_EVENT, "Runtime overlay updated");
    }

    /**
     * Warms runtime overlays without creating revision snapshots.
     *
     * @param entries snapshot items
     */
    public void warmupSnapshot(List<Item> entries) {
        if (entries != null) {
            for (Item entry : entries) {
                if (entry != null) {
                    publishRuntimeOverlay(entry);
                }
            }
        }
    }

    /**
     * Resolves an overlay for exact item coordinates.
     *
     * @param coordinates tenant-qualified item coordinates
     * @return overlay content, or {@code null} when absent
     */
    public String resolveRuntimeOverlay(Item coordinates) {
        return publisher.get(ItemNormalizer.coordinates(coordinates));
    }

    /**
     * Describes overlay presence and content.
     *
     * @param coordinates tenant-qualified item coordinates
     * @return overlay description
     */
    public Map<String, Object> describe(Item coordinates) {
        Map<String, Object> result = new LinkedHashMap<>();
        String content = resolveRuntimeOverlay(coordinates);
        result.put("key", keying.key(SettingSpec.overlay(coordinates)));
        result.put("present", content != null);
        result.put("content", content);
        return result;
    }

    /**
     * Clears an overlay and notifies watchers of the same item coordinates.
     *
     * @param coordinates tenant-qualified item coordinates
     */
    public void clearRuntimeOverlay(Item coordinates) {
        ItemNormalizer.coordinates(coordinates);
        publisher.delete(coordinates);
        watchManager.notifySetting(coordinates.getTenant_id(), keying.key(SettingSpec.watch(coordinates)), null,
                OVERLAY_SOURCE, OVERLAY_CLEAR_EVENT, "Runtime overlay cleared");
    }
}
