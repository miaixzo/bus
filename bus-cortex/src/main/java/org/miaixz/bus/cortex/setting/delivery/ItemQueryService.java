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

import org.miaixz.bus.cortex.Keying.SettingSpec;
import org.miaixz.bus.cortex.builtin.SettingGenerator;
import org.miaixz.bus.cortex.setting.curator.ItemCuratorService;
import org.miaixz.bus.cortex.setting.item.Item;
import org.miaixz.bus.cortex.setting.item.ItemNormalizer;
import org.miaixz.bus.cortex.setting.item.ItemQuery;
import org.miaixz.bus.cortex.setting.secret.SecretMasker;

/**
 * Consumer-facing query service for resolved setting values.
 *
 * @author Kimi Liu
 */
public class ItemQueryService {

    /**
     * Curator application service used for read operations.
     */
    private final ItemCuratorService settingCuratorService;

    /**
     * Secret masker used for management-safe inspection payloads.
     */
    private final SecretMasker secretMasker;

    /**
     * Optional runtime overlay service consulted before durable setting resolution.
     */
    private final RuntimeItemOverlayService runtimeSettingOverlayService;

    /**
     * Creates an ItemQueryService.
     *
     * @param settingCuratorService curator application service
     * @param secretMasker          secret masker
     */
    public ItemQueryService(ItemCuratorService settingCuratorService, SecretMasker secretMasker) {
        this(settingCuratorService, secretMasker, null);
    }

    /**
     * Creates an ItemQueryService with runtime overlay support.
     *
     * @param settingCuratorService        curator application service
     * @param secretMasker                 secret masker
     * @param runtimeSettingOverlayService runtime overlay service
     */
    public ItemQueryService(ItemCuratorService settingCuratorService, SecretMasker secretMasker,
            RuntimeItemOverlayService runtimeSettingOverlayService) {
        this.settingCuratorService = settingCuratorService;
        this.secretMasker = secretMasker;
        this.runtimeSettingOverlayService = runtimeSettingOverlayService;
    }

    /**
     * Resolves one setting value for an external consumer.
     *
     * @param query query filter
     * @return resolved value or {@code null}
     */
    public String resolve(ItemQuery query) {
        query = settingCuratorService == null ? query : settingCuratorService.prepare(query);
        if (query == null) {
            return null;
        }
        if (settingCuratorService != null && !settingCuratorService.allows(query)) {
            return query.getFallbackValue();
        }
        String overlay = runtimeOverlay(query, true);
        if (overlay != null) {
            return overlay;
        }
        return settingCuratorService.resolve(query, Item.Type.CONFIG_MAP);
    }

    /**
     * Resolves a batch of setting values using one shared scope.
     *
     * @param queries setting queries
     * @return resolved values keyed by logical query key
     */
    public Map<String, String> batchResolve(List<ItemQuery> queries) {
        Map<String, String> result = new LinkedHashMap<>();
        if (queries == null) {
            return result;
        }
        for (ItemQuery query : queries) {
            if (query == null) {
                continue;
            }
            result.put(SettingGenerator.INSTANCE.key(SettingSpec.export(query.coordinates())), resolve(query));
        }
        return result;
    }

    /**
     * Returns a management-safe snapshot of one entry for inspection APIs.
     *
     * @param query query filter
     * @return masked entry or {@code null}
     */
    public Item inspect(ItemQuery query) {
        query = settingCuratorService == null ? query : settingCuratorService.prepare(query);
        if (query == null) {
            return null;
        }
        if (settingCuratorService != null && !settingCuratorService.allows(query)) {
            return null;
        }
        return secretMasker.mask(settingCuratorService.find(query));
    }

    /**
     * Returns one overlay-aware management snapshot.
     *
     * @param query query filter
     * @return overlay and masked durable content
     */
    public Map<String, Object> overlayView(ItemQuery query) {
        Map<String, Object> result = new LinkedHashMap<>();
        Item entry = inspect(query);
        String overlay = entry == null ? null : runtimeOverlay(query, false);
        if (entry != null && (ItemNormalizer.isEncryptedFlagEnabled(entry.getEncrypted())
                || Integer.valueOf(Item.Type.SECRET.key()).equals(entry.getType()))) {
            overlay = secretMasker.mask(overlay);
        }
        boolean overlayPresent = overlay != null;
        boolean durablePresent = entry != null;
        String effectiveSource = overlayPresent && (query == null || query.isPreferOverlay()) ? "runtime-overlay"
                : durablePresent ? "durable-setting" : "none";
        result.put("entry", entry);
        result.put("overlay", overlay);
        String preview = entry == null ? null : settingCuratorService.preview(query);
        if (entry != null && (ItemNormalizer.isEncryptedFlagEnabled(entry.getEncrypted())
                || Integer.valueOf(Item.Type.SECRET.key()).equals(entry.getType()))) {
            preview = secretMasker.mask(preview);
        }
        result.put("preview", preview);
        result.put("overlayPresent", overlayPresent);
        result.put("durablePresent", durablePresent);
        result.put("effectiveSource", effectiveSource);
        result.put("effectiveValue", resolve(query));
        result.put("resolvedFrom", effectiveSource);
        result.put("watchKey", query == null ? null : settingCuratorService.watchKey(query.coordinates()));
        result.put("previewSource", durablePresent ? "durable-setting" : "none");
        return result;
    }

    /**
     * Resolves one runtime overlay when configured.
     *
     * @param query             query filter
     * @param respectPreference whether the query's overlay preference flag should be honored
     * @return runtime overlay, or {@code null} when absent
     */
    private String runtimeOverlay(ItemQuery query, boolean respectPreference) {
        if (query == null || runtimeSettingOverlayService == null || settingCuratorService == null) {
            return null;
        }
        if (respectPreference && !query.isPreferOverlay()) {
            return null;
        }
        Item item = settingCuratorService.find(query);
        if (item == null || respectPreference && !Integer.valueOf(Item.Type.CONFIG_MAP.key()).equals(item.getType())) {
            return null;
        }
        return runtimeSettingOverlayService.resolveRuntimeOverlay(query.coordinates());
    }

}
