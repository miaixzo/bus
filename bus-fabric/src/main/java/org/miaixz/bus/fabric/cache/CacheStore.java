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
package org.miaixz.bus.fabric.cache;

import java.util.Iterator;

/**
 * Protocol-neutral cache storage contract.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface CacheStore extends AutoCloseable {

    /**
     * Gets a cached entry.
     *
     * @param key key
     * @return entry or null
     */
    CacheEntry get(String key);

    /**
     * Stores an entry.
     *
     * @param key   key
     * @param entry entry
     */
    void put(String key, CacheEntry entry);

    /**
     * Opens a streaming cache writer when the store supports cache-writing bodies.
     *
     * @param key   key
     * @param entry entry metadata
     * @return writer or null when unsupported
     */
    default CacheWriter writer(final String key, final CacheEntry entry) {
        return null;
    }

    /**
     * Removes a cached entry.
     *
     * @param key key
     */
    void remove(String key);

    /**
     * Returns a key snapshot iterator.
     *
     * @return keys
     */
    Iterator<String> keys();

    /**
     * Closes this store.
     */
    @Override
    void close();

}
