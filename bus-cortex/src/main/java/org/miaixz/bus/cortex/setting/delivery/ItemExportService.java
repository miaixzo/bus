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

import org.miaixz.bus.cortex.setting.SettingEnforcer;
import org.miaixz.bus.cortex.setting.curator.ItemService;
import org.miaixz.bus.cortex.setting.item.ItemCodec;
import org.miaixz.bus.cortex.setting.item.ItemQuery;

/**
 * Explicit safe current-definition export versus authorized published-value export.
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
     * Mandatory export permissions.
     */
    private final SettingEnforcer enforcer;

    /**
     * Creates explicit export boundaries.
     *
     * @param items     current views
     * @param published formal content
     * @param enforcer  permissions
     */
    public ItemExportService(ItemService items, ItemQueryService published, SettingEnforcer enforcer) {
        this.items = Objects.requireNonNull(items);
        this.published = Objects.requireNonNull(published);
        this.enforcer = Objects.requireNonNull(enforcer);
    }

    /**
     * Exports masked current definition; never ciphertext internals.
     *
     * @param context trusted identity
     * @param item    exact source
     * @return safe canonical definition or absent
     */
    public String current(SettingEnforcer.Context context, String item) {
        enforcer.require(context, "export", "item", item);
        ItemCodec.Document view = items.view(context, item);
        return view == null ? null : new ItemCodec(64, 100000).content(view);
    }

    /**
     * Exports published values only with explicit sensitive-export authorization.
     *
     * @param context trusted identity
     * @param query   exact application request
     * @return published business values
     */
    public String published(SettingEnforcer.Context context, ItemQuery query) {
        if (!context.equals(enforcer.current()))
            throw new SecurityException("Export identity mismatch");
        enforcer.require(context, "export-sensitive", "app", query.getApp_id());
        return published.get(query);
    }

}
