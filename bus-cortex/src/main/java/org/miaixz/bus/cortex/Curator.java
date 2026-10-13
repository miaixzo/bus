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
package org.miaixz.bus.cortex;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.miaixz.bus.core.center.function.ConsumerX;
import org.miaixz.bus.cortex.builtin.SettingGenerator;
import org.miaixz.bus.cortex.setting.item.Item;
import org.miaixz.bus.cortex.setting.item.ItemQuery;

/** Consumer contract for tenant-scoped configuration content and subscriptions. */
public interface Curator {

    String get(ItemQuery query);

    void publish(Item item);

    void rollback(Item coordinates, String revision);

    default Map<String, String> batchGet(List<ItemQuery> queries) {
        Map<String, String> result = new LinkedHashMap<>();
        if (queries != null) {
            for (ItemQuery query : queries) {
                if (query != null) {
                    result.put(SettingGenerator.INSTANCE.key(Keying.SettingSpec.export(query.coordinates())), get(query));
                }
            }
        }
        return result;
    }

    default String getOrDefault(ItemQuery query, String fallback) {
        String value = get(query);
        return value == null ? fallback : value;
    }

    String watch(Item coordinates, ConsumerX<String> listener);

    void unwatch(String watch_id);
}
