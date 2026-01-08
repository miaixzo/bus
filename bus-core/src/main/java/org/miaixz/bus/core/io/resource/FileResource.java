/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.core.io.resource;

import java.io.File;
import java.io.InputStream;
import java.io.Serial;
import java.io.Serializable;
import java.net.URL;
import java.nio.file.Path;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.NotFoundException;
import org.miaixz.bus.core.xyz.FileKit;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.core.xyz.UrlKit;

/**
 * File resource access object, supporting {@link Path} and {@link File} access. This class provides an implementation
 * of the {@link Resource} interface for file-system based resources.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class FileResource implements Resource, Serializable {

    @Serial
    private static final long serialVersionUID = 2852230925613L;

    /**
     * The underlying {@link File} object.
     */
    private final File file;
    /**
     * The last modified timestamp of the file at the time of creation.
     */
    private final long lastModified;
    /**
     * The name of the resource.
     */
    private final String name;

    /**
     * Constructs a {@code FileResource} from a given file path. The path can be an absolute path or a path relative to
     * the ClassPath, but it cannot point to a file within a JAR package.
     *
     * @param path The absolute or relative path to the file.
     */
    public FileResource(final String path) {
        this(FileKit.file(path));
    }

    /**
     * Constructs a {@code FileResource} from a given {@link Path}. The resource name will be the file's own name,
     * including the extension.
     *
     * @param path The {@link Path} to the file.
     */
    public FileResource(final Path path) {
        this(path.toFile());
    }

    /**
     * Constructs a {@code FileResource} from a given {@link File}. The resource name will be the file's own name,
     * including the extension.
     *
     * @param file The {@link File} object.
     */
    public FileResource(final File file) {
        this(file, null);
    }

    /**
     * Constructs a {@code FileResource} from a given {@link File} and a specified file name.
     *
     * @param file     The {@link File} object.
     * @param fileName The name of the file, including the extension. If {@code null}, the file's own name will be used.
     */
    public FileResource(final File file, final String fileName) {
        this.file = Assert.notNull(file, "File must be not null !");
        this.lastModified = file.lastModified();
        this.name = ObjectKit.defaultIfNull(fileName, file::getName);
    }

    /**
     * Returns the name of this file resource.
     *
     * @return The name of the file.
     */
    @Override
    public String getName() {
        return this.name;
    }

    /**
     * Returns the URL of this file resource.
     *
     * @return The URL representing the file path.
     */
    @Override
    public URL getUrl() {
        return UrlKit.getURL(this.file);
    }

    /**
     * Returns the size of this file resource.
     *
     * @return The size of the file in bytes.
     */
    @Override
    public long size() {
        return this.file.length();
    }

    /**
     * Returns an input stream for this file resource.
     *
     * @return An input stream for reading the file.
     * @throws NotFoundException If the file does not exist.
     */
    @Override
    public InputStream getStream() throws NotFoundException {
        if (!exists()) {
            throw new NotFoundException("File [{}] not exist!", this.file.getAbsolutePath());
        }
        return FileKit.getInputStream(this.file);
    }

    /**
     * Retrieves the underlying {@link File} object.
     *
     * @return The {@link File} object.
     */
    public File getFile() {
        return this.file;
    }

    /**
     * Checks if the file represented by this resource exists.
     *
     * @return {@code true} if the file exists, {@code false} otherwise.
     */
    public boolean exists() {
        return this.file.exists();
    }

    /**
     * Checks if this file resource has been modified since it was last accessed.
     *
     * @return {@code true} if the file has been modified, {@code false} otherwise.
     */
    @Override
    public boolean isModified() {
        return this.lastModified != file.lastModified();
    }

    /**
     * Returns the string representation of the file path.
     *
     * @return The file path as a string.
     */
    @Override
    public String toString() {
        return this.file.toString();
    }

}
