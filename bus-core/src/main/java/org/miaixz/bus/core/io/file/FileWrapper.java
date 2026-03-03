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
package org.miaixz.bus.core.io.file;

import java.io.File;
import java.io.Serial;
import java.io.Serializable;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Wrapper;
import org.miaixz.bus.core.xyz.FileKit;
import org.miaixz.bus.core.xyz.ObjectKit;

/**
 * File wrapper, extending the {@link File} object. This class provides a convenient way to encapsulate a {@link File}
 * object along with its character encoding, offering additional utility methods related to file properties.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class FileWrapper implements Wrapper<File>, Serializable {

    @Serial
    private static final long serialVersionUID = 2852227812657L;

    /**
     * The wrapped {@link File} object.
     */
    protected File file;
    /**
     * The character set used for the file, typically for reading or writing text content.
     */
    protected java.nio.charset.Charset charset;

    /**
     * Constructs a new {@code FileWrapper} instance.
     *
     * @param file    The {@link File} object to wrap. Must not be {@code null}.
     * @param charset The character set to use for file operations. If {@code null}, {@link Charset#UTF_8} is used as
     *                the default encoding.
     * @throws NullPointerException if the provided {@code file} is {@code null}.
     */
    public FileWrapper(final File file, final java.nio.charset.Charset charset) {
        this.file = Assert.notNull(file);
        this.charset = ObjectKit.defaultIfNull(charset, Charset.UTF_8);
    }

    /**
     * Gets the raw, wrapped {@link File} object.
     *
     * @return The wrapped {@link File} object.
     */
    @Override
    public File getRaw() {
        return file;
    }

    /**
     * Sets the wrapped {@link File} object.
     *
     * @param file The new {@link File} object to set. Must not be {@code null}.
     * @return This {@code FileWrapper} instance, allowing for method chaining.
     * @throws NullPointerException if the provided {@code file} is {@code null}.
     */
    public FileWrapper setFile(final File file) {
        this.file = file;
        return this;
    }

    /**
     * Gets the character set encoding associated with this file wrapper.
     *
     * @return The {@link java.nio.charset.Charset} used for file operations.
     */
    public java.nio.charset.Charset getCharset() {
        return charset;
    }

    /**
     * Sets the character set encoding for this file wrapper.
     *
     * @param charset The new {@link java.nio.charset.Charset} to set. If {@code null}, {@link Charset#UTF_8} will be
     *                used.
     * @return This {@code FileWrapper} instance, allowing for method chaining.
     */
    public FileWrapper setCharset(final java.nio.charset.Charset charset) {
        this.charset = charset;
        return this;
    }

    /**
     * Returns a human-readable string representation of the file size. For example, "1.23 MB", "456 KB", "789 Bytes".
     *
     * @return A string representing the readable file size.
     */
    public String readableFileSize() {
        return FileKit.readableFileSize(file.length());
    }

}
