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
package org.miaixz.bus.cortex.setting.watch;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.miaixz.bus.cortex.Suite;
import org.miaixz.bus.cortex.Trait;

/**
 * Durable store contract for runtime-watch entities.
 *
 * @author Kimi Liu
 */
public interface WatchStore {

    /**
     * Saves one runtime watch.
     *
     * @param watch runtime watch
     * @return saved runtime watch
     */
    Watch save(Watch watch);

    /**
     * Saves a batch of runtime watches.
     *
     * @param watches runtime watches
     * @return saved non-null runtime watches
     */
    default List<Watch> saveAll(List<Watch> watches) {
        if (watches == null || watches.isEmpty()) {
            return List.of();
        }
        List<Watch> result = new ArrayList<>(watches.size());
        for (Watch watch : watches) {
            if (watch != null) {
                result.add(save(watch));
            }
        }
        return result;
    }

    /**
     * Finds one runtime watch by identifier.
     *
     * @param tenant_id tenant identifier
     * @param watch_id  runtime watch identifier
     * @return matched runtime watch, or {@code null}
     */
    Watch find(String tenant_id, String watch_id);

    /**
     * Finds one runtime watch by instance and item.
     *
     * @param tenant_id   tenant identifier
     * @param instance_id runtime instance identifier
     * @param item_id     configuration item identifier
     * @return matched runtime watch, or {@code null}
     */
    Watch find(String tenant_id, String instance_id, String item_id);

    /**
     * Queries runtime watches belonging to an instance.
     *
     * @param tenant_id   tenant identifier
     * @param instance_id runtime instance identifier
     * @return matching runtime watches
     */
    List<Watch> query(String tenant_id, String instance_id);

    /**
     * Deletes one runtime watch.
     *
     * @param tenant_id tenant identifier
     * @param watch_id  runtime watch identifier
     * @return deleted runtime watch, or {@code null}
     */
    Watch delete(String tenant_id, String watch_id);

    /**
     * Returns strongly typed durable-store capabilities.
     *
     * @return fixed runtime-watch store capabilities
     */
    default Suite storeCapabilities() {
        return Suite.of(Trait.BATCH, Trait.QUERY, Trait.DURABLE, Trait.DELETE);
    }

    /**
     * Returns legacy string-keyed durable-store capabilities.
     *
     * @return fixed runtime-watch store capabilities
     */
    default Map<String, Boolean> capabilities() {
        return storeCapabilities().asMap();
    }

}
