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
package org.miaixz.bus.core.io.resource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;

import javax.tools.FileObject;

import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.core.xyz.UrlKit;

/**
 * Wrapper for {@link FileObject} resources. This class provides an implementation of the {@link Resource} interface for
 * resources represented by a {@link FileObject}, typically used in Java compilation tasks.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class FileObjectResource implements Resource {

    /**
     * The underlying {@link FileObject} instance.
     */
    private final FileObject fileObject;

    /**
     * Constructs a {@code FileObjectResource} with the given {@link FileObject}.
     *
     * @param fileObject The {@link FileObject} to wrap.
     */
    public FileObjectResource(final FileObject fileObject) {
        this.fileObject = fileObject;
    }

    /**
     * Retrieves the original {@link FileObject} instance.
     *
     * @return The wrapped {@link FileObject}.
     */
    public FileObject getFileObject() {
        return this.fileObject;
    }

    /**
     * Returns the name of the file object.
     *
     * @return The name of the file.
     */
    @Override
    public String getName() {
        return this.fileObject.getName();
    }

    /**
     * Returns the URL of the file object.
     *
     * @return The URL representing the file, or {@code null} if it cannot be converted to a URL.
     */
    @Override
    public URL getUrl() {
        try {
            return this.fileObject.toUri().toURL();
        } catch (final MalformedURLException e) {
            return null;
        }
    }

    /**
     * Returns the size of the file object.
     *
     * @return The size of the file in bytes.
     */
    @Override
    public long size() {
        return UrlKit.size(getUrl());
    }

    /**
     * Returns an input stream for the file object.
     *
     * @return An input stream for reading the file.
     * @throws InternalException If an I/O error occurs while opening the stream.
     */
    @Override
    public InputStream getStream() {
        try {
            return this.fileObject.openInputStream();
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Returns a buffered reader for the file object.
     * <p>
     * The charset parameter is ignored by the underlying {@code FileObject.openReader()} method.
     *
     * @param charset The character set (ignored).
     * @return A buffered reader for reading the file.
     */
    @Override
    public BufferedReader getReader(final Charset charset) {
        try {
            // The charset parameter is ignored by openReader(boolean ignoreEncodingErrors) in FileObject,
            // but it's kept for consistency with the Resource interface.
            return IoKit.toBuffered(this.fileObject.openReader(false));
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

}
