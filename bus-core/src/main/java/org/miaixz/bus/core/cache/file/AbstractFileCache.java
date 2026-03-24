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
package org.miaixz.bus.core.cache.file;

import java.io.File;
import java.io.Serial;
import java.io.Serializable;

import org.miaixz.bus.core.cache.Cache;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.FileKit;

/**
 * An abstract file cache to mitigate performance issues caused by frequent file reads.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public abstract class AbstractFileCache implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852230175903L;

    /**
     * The total cache capacity in bytes.
     */
    protected final int capacity;
    /**
     * The maximum size of a file that can be cached. Files larger than this will not be cached.
     */
    protected final int maxFileSize;
    /**
     * The default timeout for cache entries in milliseconds. A value of 0 means no timeout.
     */
    protected final long timeout;
    /**
     * The underlying cache implementation.
     */
    protected final Cache<File, byte[]> cache;

    /**
     * The current used size of the cache in bytes.
     */
    protected int usedSize;

    /**
     * Constructs a new file cache.
     *
     * @param capacity    The cache capacity in bytes.
     * @param maxFileSize The maximum file size in bytes.
     * @param timeout     The default timeout in milliseconds (0 for no timeout).
     */
    public AbstractFileCache(final int capacity, final int maxFileSize, final long timeout) {
        this.capacity = capacity;
        this.maxFileSize = maxFileSize;
        this.timeout = timeout;
        this.cache = initCache();
    }

    /**
     * Returns the total cache capacity.
     *
     * @return The cache capacity in bytes.
     */
    public int capacity() {
        return capacity;
    }

    /**
     * Returns the currently used cache size.
     *
     * @return The used size in bytes.
     */
    public int getUsedSize() {
        return usedSize;
    }

    /**
     * Returns the maximum allowed file size for caching.
     *
     * @return The maximum file size in bytes.
     */
    public int maxFileSize() {
        return maxFileSize;
    }

    /**
     * Returns the number of files currently in the cache.
     *
     * @return The number of cached files.
     */
    public int getCachedFilesCount() {
        return cache.size();
    }

    /**
     * Returns the default timeout for cache entries.
     *
     * @return The timeout in milliseconds.
     */
    public long timeout() {
        return this.timeout;
    }

    /**
     * Clears the entire cache and resets the used size.
     */
    public void clear() {
        cache.clear();
        usedSize = 0;
    }

    /**
     * Gets the byte content of a file from the cache or reads it from the filesystem.
     *
     * @param path The path to the file.
     * @return The byte content of the file.
     * @throws InternalException if an I/O error occurs.
     */
    public byte[] getFileBytes(final String path) throws InternalException {
        return getFileBytes(new File(path));
    }

    /**
     * Gets the byte content of a file from the cache or reads it from the filesystem.
     *
     * @param file The file to retrieve.
     * @return The byte content of the file.
     * @throws InternalException if an I/O error occurs.
     */
    public byte[] getFileBytes(final File file) throws InternalException {
        byte[] bytes = cache.get(file);
        if (bytes != null) {
            // File is found in cache.
            return bytes;
        }

        // Read all bytes from the file.
        bytes = FileKit.readBytes(file);

        if ((maxFileSize != 0) && (file.length() > maxFileSize)) {
            // File is larger than the allowed size, so don't cache it.
            return bytes;
        }

        usedSize += bytes.length;

        // Put the file into the cache. The underlying cache implementation will handle pruning if capacity is exceeded.
        cache.put(file, bytes);

        return bytes;
    }

    /**
     * Initializes the underlying cache implementation. Must be implemented by subclasses.
     *
     * @return A {@link Cache} instance for storing file data.
     */
    protected abstract Cache<File, byte[]> initCache();

}
