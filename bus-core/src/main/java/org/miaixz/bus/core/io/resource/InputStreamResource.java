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

    /**
     * Returns the name of this input stream resource.
     *
     * @return The name of the resource.
     */
    @Override
    public String getName() {
        return this.name;
    }

    /**
     * Returns the URL of this input stream resource.
     * <p>
     * Returns {@code null} since an input stream resource does not have a URL.
     *
     * @return {@code null}.
     */
    @Override
    public URL getUrl() {
        return null;
    }

    /**
     * Returns the size of this input stream resource based on available bytes.
     *
     * @return The number of available bytes.
     */
    @Override
    public long size() {
        try {
            return this.in.available();
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Returns the input stream for this resource.
     *
     * @return The underlying input stream.
     */
    @Override
    public InputStream getStream() {
        return this.in;
    }

}
