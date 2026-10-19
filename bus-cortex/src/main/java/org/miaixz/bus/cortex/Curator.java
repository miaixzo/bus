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

/**
 * Consumer contract for tenant-scoped configuration content and subscriptions.
 */
public interface Curator {

    /**
     * Resolves configuration for a tenant-qualified query.
     *
     * @param query configuration query
     * @return resolved value or the query fallback
     */
    String get(ItemQuery query);

    /**
     * Stages a configuration item for publication.
     *
     * @param item item to stage
     */
    void publish(Item item);

    /**
     * Stages a historical revision as the next item revision.
     *
     * @param coordinates item coordinates
     * @param revision    historical revision identifier
     */
    void rollback(Item coordinates, String revision);

    /**
     * Resolves multiple queries while preserving their input order.
     *
     * @param queries configuration queries
     * @return map keyed by canonical exported item key
     */
    default Map<String, String> batchGet(List<ItemQuery> queries) {
        Map<String, String> result = new LinkedHashMap<>();
        if (queries != null) {
            for (ItemQuery query : queries) {
                if (query != null) {
                    result.put(
                            SettingGenerator.INSTANCE.key(Keying.SettingSpec.export(query.coordinates())),
                            get(query));
                }
            }
        }
        return result;
    }

    /**
     * Resolves a query and substitutes a caller-provided fallback when no value exists.
     *
     * @param query    configuration query
     * @param fallback fallback returned for a missing value
     * @return resolved value or {@code fallback}
     */
    default String getOrDefault(ItemQuery query, String fallback) {
        String value = get(query);
        return value == null ? fallback : value;
    }

    /**
     * Subscribes to changes for one configuration coordinate.
     *
     * @param coordinates item coordinates
     * @param listener    value-change listener
     * @return watch identifier used to cancel the subscription
     */
    String watch(Item coordinates, ConsumerX<String> listener);

    /**
     * Cancels a configuration subscription.
     *
     * @param watch_id watch identifier returned by {@link #watch(Item, ConsumerX)}
     */
    void unwatch(String watch_id);

}
