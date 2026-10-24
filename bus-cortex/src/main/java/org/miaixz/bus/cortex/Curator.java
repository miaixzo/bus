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

import java.util.List;
import java.util.Map;

import org.miaixz.bus.core.center.function.ConsumerX;
import org.miaixz.bus.cortex.setting.item.ItemQuery;

/**
 * Application-facing published configuration reads and authorized subscriptions.
 *
 * @author Kimi Liu
 */
public interface Curator {

    /**
     * Reads exact published content; never consults editable or temporary configuration.
     *
     * @param query tenant-qualified application configuration request
     * @return encoded published configuration, or null for authorized absence
     */
    String get(ItemQuery query);

    /**
     * Resolves the supplied application queries through the grouped immutable-release path.
     *
     * @param queries tenant-qualified application configuration requests
     * @return the grouped released values for the supplied queries
     */
    Map<String, String> batchGet(List<ItemQuery> queries);

    /**
     * Returns a caller fallback only when the authorized lookup has no value.
     *
     * @param query    tenant-qualified application configuration request
     * @param fallback caller-supplied value for an authorized lookup with no result
     * @return published configuration when present, otherwise the supplied fallback
     */
    default String getOrDefault(ItemQuery query, String fallback) {
        String value = get(query);
        return value == null ? fallback : value;
    }

    /**
     * Authorizes an application subscription and registers its tenant-scoped transport callback.
     *
     * @param query    tenant-qualified application configuration request
     * @param listener subscription callback for this application
     * @return identifier of the registered transport subscription
     */
    String watch(ItemQuery query, ConsumerX<String> listener);

    /**
     * Removes the exact subscription identifier.
     *
     * @param watch_id exact subscription identifier
     */
    void unwatch(String watch_id);

}
