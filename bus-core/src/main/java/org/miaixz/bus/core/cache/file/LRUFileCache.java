/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
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
