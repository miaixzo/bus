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

import java.util.Objects;

import org.miaixz.bus.cortex.guard.CortexGuard;
import org.miaixz.bus.cortex.setting.SettingProvider;
import org.miaixz.bus.cortex.setting.curator.ItemService;
import org.miaixz.bus.cortex.setting.item.ItemCodec;
import org.miaixz.bus.cortex.setting.item.ItemQuery;

/**
 * Explicit safe current-definition export and published-value export with optional host access checks.
 *
 * @author Kimi Liu
 */
public final class ItemExportService {

    /**
     * Current safe views.
     */
    private final ItemService items;
    /**
     * Published content authority.
     */
    private final ItemQueryService published;
    /**
     * Optional host export policy.
     */
    private final CortexGuard guard;

    /**
     * Creates explicit export boundaries.
     *
     * @param items     current views
     * @param published formal content
     * @param guard     optional host access policy
     */
    public ItemExportService(ItemService items, ItemQueryService published, CortexGuard guard) {
        this.items = Objects.requireNonNull(items);
        this.published = Objects.requireNonNull(published);
        this.guard = guard;
    }

    /**
     * Exports masked current definition; never ciphertext internals.
     *
     * @param context operation coordinates
     * @param item    exact source
     * @return safe canonical definition or absent
     */
    public String current(SettingProvider.Context context, String item) {
        context.check(guard, "export", "item", item);
        ItemCodec.Document view = items.view(context, item);
        return view == null ? null : new ItemCodec(64, 100000).content(view);
    }

    /**
     * Exports published values with optional host sensitive-export policy.
     *
     * @param context operation coordinates
     * @param query   exact application request
     * @return published business values
     */
    public String published(SettingProvider.Context context, ItemQuery query) {
        context.check(guard, "export-sensitive", "app", query.getApp_id());
        return published.get(context, query);
    }

}
