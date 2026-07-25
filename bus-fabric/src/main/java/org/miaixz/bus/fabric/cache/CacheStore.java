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
     * Looks up the entry associated with a cache key.
     *
     * @param key cache key to look up
     * @return cached entry, or {@code null} when the key is absent
     */
    CacheEntry get(String key);

    /**
     * Stores or replaces the entry associated with a cache key.
     *
     * @param key   cache key under which the entry is stored
     * @param entry cache metadata and body reference to store
     */
    void put(String key, CacheEntry entry);

    /**
     * Opens a streaming cache writer when the store supports cache-writing bodies.
     *
     * @param key   cache key under which the completed entry will be stored
     * @param entry cache metadata associated with the streamed body
     * @return streaming writer, or {@code null} when streaming writes are unsupported
     */
    default CacheWriter writer(final String key, final CacheEntry entry) {
        return null;
    }

    /**
     * Removes a cached entry.
     *
     * @param key cache key to remove
     */
    void remove(String key);

    /**
     * Returns an iterator over a snapshot of the stored cache keys.
     *
     * @return iterator over cache keys present when the snapshot was created
     */
    Iterator<String> keys();

    /**
     * Closes this store.
     */
    @Override
    void close();

}
