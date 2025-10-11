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
package org.miaixz.bus.core.io.file;

import java.io.File;
import java.io.Serial;

import org.miaixz.bus.core.io.resource.Resource;

/**
 * A virtual file class that extends {@link File} to simulate a file in memory.
 * <p>
 * This class provides a way to represent a file in memory without needing to access the physical file system. It can be
 * used for testing, simulating file operations, or handling file content in memory.
 *
 * <p>
 * A virtual file can be associated with a {@link Resource} object, which represents the file's content. If the content
 * resource is null, the file is considered non-existent.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class VirtualFile extends File {

    @Serial
    private static final long serialVersionUID = 2852285580251L;

    /**
     * The resource representing the file's content.
     */
    private final Resource content;

    /**
     * Constructs a new virtual file.
     *
     * @param pathname The file path, cannot be null.
     * @param content  The resource for the file's content, which can be null (indicating the file does not exist).
     * @throws IllegalArgumentException if the pathname is null.
     */
    public VirtualFile(final String pathname, final Resource content) {
        super(pathname);
        if (pathname == null) {
            throw new IllegalArgumentException("pathname cannot be null");
        }
        this.content = content;
    }

    /**
     * Constructs a new virtual file.
     *
     * @param parent  The parent path string, cannot be null.
     * @param child   The child file name, cannot be null.
     * @param content The resource for the file's content, which can be null.
     * @throws IllegalArgumentException if parent or child is null.
     */
    public VirtualFile(final String parent, final String child, final Resource content) {
        super(parent, child);
        if (parent == null || child == null) {
            throw new IllegalArgumentException("parent and child cannot be null");
        }
        this.content = content;
    }

    /**
     * Constructs a new virtual file.
     *
     * @param parent  The parent file object, cannot be null.
     * @param child   The child file name, cannot be null.
     * @param content The resource for the file's content, which can be null.
     * @throws IllegalArgumentException if parent or child is null.
     */
    public VirtualFile(final File parent, final String child, final Resource content) {
        super(parent, child);
        if (parent == null || child == null) {
            throw new IllegalArgumentException("parent and child cannot be null");
        }
        this.content = content;
    }

    /**
     * Gets the resource representing the file's content.
     *
     * @return The content resource, which may be null.
     */
    public Resource getContent() {
        return this.content;
    }

    /**
     * Gets the file's content as a byte array.
     *
     * @return A byte array of the file's content, or null if there is no content.
     */
    public byte[] getBytes() {
        return null != this.content ? this.content.readBytes() : null;
    }

    /**
     * Tests if this file exists.
     *
     * @return {@code true} if the content resource is not null, {@code false} otherwise.
     */
    @Override
    public boolean exists() {
        return null != this.content;
    }

    /**
     * Tests if this is a regular file.
     *
     * @return always {@code true}.
     */
    @Override
    public boolean isFile() {
        return true;
    }

    /**
     * Tests if this is a directory.
     *
     * @return always {@code false}.
     */
    @Override
    public boolean isDirectory() {
        return false;
    }

    /**
     * Gets the length of the file's content in bytes.
     *
     * @return The file length in bytes, or 0 if the file does not exist.
     */
    @Override
    public long length() {
        return null != this.content ? this.content.size() : 0L;
    }

    /**
     * Tests if the application can read this file.
     *
     * @return {@code true} if the file exists, {@code false} otherwise.
     */
    @Override
    public boolean canRead() {
        return exists();
    }

    /**
     * Tests if the application can modify this file.
     *
     * @return always {@code false}.
     */
    @Override
    public boolean canWrite() {
        return false;
    }

    /**
     * Tests if the application can execute this file.
     *
     * @return always {@code false}.
     */
    @Override
    public boolean canExecute() {
        return false;
    }

    /**
     * Gets the last modified time.
     *
     * @return always returns the current system time.
     */
    @Override
    public long lastModified() {
        return System.currentTimeMillis();
    }

}
