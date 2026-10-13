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

import org.miaixz.bus.core.center.function.ConsumerX;
import org.miaixz.bus.cortex.Curator;
import org.miaixz.bus.cortex.Listener;
import org.miaixz.bus.cortex.Vector;
import org.miaixz.bus.cortex.Watch;
import org.miaixz.bus.cortex.magic.watch.WatchManager;
import org.miaixz.bus.cortex.setting.curator.ItemCuratorService;
import org.miaixz.bus.cortex.setting.delivery.RuntimeItemOverlayService;
import org.miaixz.bus.cortex.setting.item.Item;
import org.miaixz.bus.cortex.setting.item.ItemQuery;

/** Resolves configuration using tenant-qualified objects supplied by the caller. */
public class DefaultCurator implements Curator {

    private final ItemCuratorService settingCuratorService;
    private final WatchManager watchManager;
    private final RuntimeItemOverlayService runtimeSettingOverlayService;

    public DefaultCurator(ItemCuratorService settingCuratorService, WatchManager watchManager) {
        this(settingCuratorService, watchManager, null);
    }

    public DefaultCurator(ItemCuratorService settingCuratorService, WatchManager watchManager,
            RuntimeItemOverlayService runtimeSettingOverlayService) {
        this.settingCuratorService = settingCuratorService;
        this.watchManager = watchManager;
        this.runtimeSettingOverlayService = runtimeSettingOverlayService;
    }

    @Override
    public String get(ItemQuery query) {
        ItemQuery prepared = settingCuratorService.prepare(query);
        if (prepared == null || !settingCuratorService.allows(prepared)) {
            return prepared == null ? null : prepared.getFallbackValue();
        }
        // Authorize the exact coordinate before consulting a runtime overlay.
        if (settingCuratorService.find(prepared) == null) {
            return prepared.getFallbackValue();
        }
        if (prepared.isPreferOverlay() && runtimeSettingOverlayService != null) {
            String overlay = runtimeSettingOverlayService.resolveRuntimeOverlay(prepared.coordinates());
            if (overlay != null) {
                return overlay;
            }
        }
        return settingCuratorService.resolve(prepared);
    }

    @Override
    public void publish(Item item) {
        settingCuratorService.publish(item);
    }

    @Override
    public void rollback(Item coordinates, String revision) {
        settingCuratorService.rollback(coordinates, revision);
    }

    @Override
    public String watch(Item coordinates, ConsumerX<String> listener) {
        settingCuratorService.find(coordinates);
        Vector vector = new Vector();
        vector.setTenant_id(coordinates.getTenant_id());
        vector.setApp_id(coordinates.getApp_id());
        vector.setId(settingCuratorService.watchKey(coordinates));
        Listener<Watch<String>> callback = event -> {
            for (String value : event.getAdded()) {
                listener.accept(value);
            }
            for (String value : event.getUpdated()) {
                listener.accept(value);
            }
            if (!event.getRemoved().isEmpty()) {
                listener.accept(null);
            }
        };
        return watchManager.add(vector, callback);
    }

    @Override
    public void unwatch(String watch_id) {
        watchManager.remove(watch_id);
    }
}
