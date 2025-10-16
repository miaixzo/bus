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
import java.io.Serializable;

import org.miaixz.bus.core.cache.Cache;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.FileKit;

/**
 * An abstract file cache to mitigate performance issues caused by frequent file reads.
 *
 * @author Kimi Liu
 * @since Java 17+
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
