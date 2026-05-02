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
import java.util.Map;

import org.miaixz.bus.cortex.setting.curator.ItemCuratorService;
import org.miaixz.bus.cortex.setting.item.ItemExposure;
import org.miaixz.bus.cortex.setting.item.ItemFormat;
import org.miaixz.bus.cortex.setting.item.ItemScope;

/**
 * Export service for resolved setting scopes.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class ItemExportService {

    /**
     * Curator application service used to query and resolve export candidates.
     */
    private final ItemCuratorService settingCuratorService;

    /**
     * Creates an ItemExportService.
     *
     * @param settingCuratorService curator application service
     */
    public ItemExportService(ItemCuratorService settingCuratorService) {
        this.settingCuratorService = settingCuratorService;
    }

    /**
     * Exports resolved values for the given scope.
     *
     * @param scope export scope
     * @return resolved export payload
     */
    public Map<String, String> export(ItemScope scope) {
        ItemScope prepared = scope == null ? new ItemScope() : scope;
        prepared = settingCuratorService == null ? prepared : settingCuratorService.prepare(prepared);
        return settingCuratorService.export(prepared, ItemExposure.PUBLIC);
    }

    /**
     * Exports resolved values in a format-oriented view.
     *
     * @param scope  scope
     * @param format export format
     * @return formatted export payload
     */
    public Map<String, Object> export(ItemScope scope, ItemFormat format) {
        Map<String, String> exported = export(scope);
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("format", format == null ? ItemFormat.PROPERTIES : format);
        payload.put("values", exported);
        payload.put("count", exported.size());
        return payload;
    }

}
