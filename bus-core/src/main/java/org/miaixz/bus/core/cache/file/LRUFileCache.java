/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.core.cache.file;

import java.io.File;
import java.io.Serial;

import org.miaixz.bus.core.cache.Cache;
import org.miaixz.bus.core.cache.provider.LRUCache;

/**
 * A file cache implementation that uses the LRU (Least Recently Used) strategy to manage cached files and mitigate
 * performance issues from frequent file reads.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class LRUFileCache extends AbstractFileCache {

    @Serial
    private static final long serialVersionUID = 2852230517893L;

    /**
     * Constructs an LRU file cache with a default max file size (half of the capacity) and no timeout.
     *
     * @param capacity The cache capacity in bytes.
     */
    public LRUFileCache(final int capacity) {
        this(capacity, capacity / 2, 0);
    }

    /**
     * Constructs an LRU file cache with a specified max file size and no timeout.
     *
     * @param capacity    The cache capacity in bytes.
     * @param maxFileSize The maximum file size in bytes.
     */
    public LRUFileCache(final int capacity, final int maxFileSize) {
        this(capacity, maxFileSize, 0);
    }

    /**
     * Constructs an LRU file cache with specified capacity, max file size, and timeout.
     *
     * @param capacity    The cache capacity in bytes.
     * @param maxFileSize The maximum file size in bytes.
     * @param timeout     The default timeout in milliseconds (0 for no timeout).
     */
    public LRUFileCache(final int capacity, final int maxFileSize, final long timeout) {
        super(capacity, maxFileSize, timeout);
    }

    /**
     * Initializes the underlying LRU cache. This implementation uses a custom {@link LRUCache} that tracks memory usage
     * in bytes.
     *
     * @return A new {@link LRUCache} instance.
     */
    @Override
    protected Cache<File, byte[]> initCache() {
        // The cache capacity is managed by the parent class in terms of byte size,
        // while the underlying LRUCache manages the number of items.
        return new LRUCache<>(this.capacity, super.timeout) {

            @Serial
            private static final long serialVersionUID = 2852551020813L;

            /**
             * Determines if the cache is full by comparing the used byte size against the capacity.
             *
             * @return {@code true} if the used size exceeds the capacity.
             */
            @Override
            public boolean isFull() {
                return LRUFileCache.this.usedSize > this.capacity;
            }

            /**
             * Updates the used size when an item is removed from the cache.
             *
             * @param key          The file being removed.
             * @param cachedObject The byte content of the file being removed.
             */
            @Override
            protected void onRemove(final File key, final byte[] cachedObject) {
                usedSize -= cachedObject.length;
            }
        };
    }

}
