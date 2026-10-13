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
import org.miaixz.bus.cortex.Keying;
import org.miaixz.bus.cortex.Keying.SettingSpec;
import org.miaixz.bus.cortex.builtin.SettingGenerator;
import org.miaixz.bus.cortex.setting.SettingPublisher;
import org.miaixz.bus.cortex.setting.delivery.RuntimeItemOverlayPublisher;
import org.miaixz.bus.cortex.setting.item.Item;

/**
 * Cache-backed runtime publisher without revision history. Use {@link SettingPublisher} for revision-tracked
 * publication.
 *
 * @author Kimi Liu
 */
public class SimpleSettingPublisher implements RuntimeItemOverlayPublisher {

    private final CacheX<String, Object> cacheX;
    private final Keying<SettingSpec> keying;

    /**
     * Creates a publisher with the default key strategy.
     *
     * @param cacheX shared overlay cache
     */
    public SimpleSettingPublisher(CacheX<String, Object> cacheX) {
        this(cacheX, SettingGenerator.INSTANCE);
    }

    /**
     * Creates a publisher with a configurable key strategy.
     *
     * @param cacheX shared overlay cache
     * @param keying setting key strategy
     */
    public SimpleSettingPublisher(CacheX<String, Object> cacheX, Keying<SettingSpec> keying) {
        this.cacheX = cacheX;
        this.keying = keying == null ? SettingGenerator.INSTANCE : keying;
    }

    /**
     * Publishes an overlay using the cache's default lifetime.
     *
     * @param item tenant-qualified item and content
     */
    public void publish(Item item) {
        publish(item, 0L);
    }

    @Override
    public void publish(Item item, long ttlMs) {
        cacheX.write(keying.key(SettingSpec.overlay(item)), item.getContent(), ttlMs);
    }

    @Override
    public String get(Item coordinates) {
        Object value = cacheX.read(keying.key(SettingSpec.overlay(coordinates)));
        return value == null ? null : value.toString();
    }

    @Override
    public void delete(Item coordinates) {
        cacheX.remove(keying.key(SettingSpec.overlay(coordinates)));
    }
}
