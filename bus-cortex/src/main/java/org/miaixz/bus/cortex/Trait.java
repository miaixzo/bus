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

/**
 * Strongly typed store traits shared by registry and setting stores.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public enum Trait {

    /**
     * Store supports writing multiple records in one call.
     */
    BATCH("batch"),
    /**
     * Store supports query operations beyond direct key lookup.
     */
    QUERY("query"),
    /**
     * Store persists data outside the current process.
     */
    DURABLE("durable"),
    /**
     * Store keeps a local cache layer.
     */
    CACHE("cache"),
    /**
     * Store supports explicit cache eviction.
     */
    EVICT("evict"),
    /**
     * Store supports rebuilding its derived view.
     */
    REBUILD("rebuild"),
    /**
     * Store supports route-based registry queries.
     */
    ROUTE_QUERY("routeQuery"),
    /**
     * Store can return runtime instance snapshots.
     */
    INSTANCES("instances"),
    /**
     * Store supports deletion of persisted records.
     */
    DELETE("delete"),
    /**
     * Store can roll back metadata to an earlier value.
     */
    ROLLBACK_METADATA("rollbackMetadata");

    /**
     * Legacy string key exposed through compatibility maps.
     */
    private final String key;

    /**
     * Creates a trait with its compatibility key.
     *
     * @param key legacy trait key
     */
    Trait(String key) {
        this.key = key;
    }

    /**
     * Returns the legacy map key for compatibility responses.
     *
     * @return legacy trait key
     */
    public String key() {
        return key;
    }

}
