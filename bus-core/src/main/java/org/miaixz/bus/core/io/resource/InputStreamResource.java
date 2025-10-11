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

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;

import org.miaixz.bus.core.io.stream.ReaderInputStream;
import org.miaixz.bus.core.lang.exception.InternalException;

/**
 * Resource provider based on an {@link InputStream}. Note: The {@code getUrl} method of this object always returns
 * {@code null}.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class InputStreamResource implements Resource, Serializable {

    /**
     * The serial version UID for serialization.
     */
    @Serial
    private static final long serialVersionUID = 2852231716055L;

    /**
     * The underlying {@link InputStream}.
     */
    private final InputStream in;
    /**
     * The name of the resource.
     */
    private final String name;

    /**
     * Constructs an {@code InputStreamResource} from a {@link Reader} and a {@link Charset}. The reader's content is
     * converted to an {@link InputStream} using {@link ReaderInputStream}.
     *
     * @param reader  The {@link Reader} to wrap.
     * @param charset The {@link Charset} to use for encoding the reader's content.
     */
    public InputStreamResource(final Reader reader, final Charset charset) {
        this(new ReaderInputStream(reader, charset));
    }

    /**
     * Constructs an {@code InputStreamResource} from a given {@link InputStream}.
     *
     * @param in The {@link InputStream} to wrap.
     */
    public InputStreamResource(final InputStream in) {
        this(in, null);
    }

    /**
     * Constructs an {@code InputStreamResource} from a given {@link InputStream} and a resource name.
     *
     * @param in   The {@link InputStream} to wrap.
     * @param name The name of the resource.
     */
    public InputStreamResource(final InputStream in, final String name) {
        this.in = in;
        this.name = name;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public URL getUrl() {
        return null;
    }

    @Override
    public long size() {
        try {
            return this.in.available();
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    @Override
    public InputStream getStream() {
        return this.in;
    }

}
