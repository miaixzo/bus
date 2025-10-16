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

    @Override
    public String getName() {
        return this.fileObject.getName();
    }

    @Override
    public URL getUrl() {
        try {
            return this.fileObject.toUri().toURL();
        } catch (final MalformedURLException e) {
            return null;
        }
    }

    @Override
    public long size() {
        return UrlKit.size(getUrl());
    }

    @Override
    public InputStream getStream() {
        try {
            return this.fileObject.openInputStream();
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

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
