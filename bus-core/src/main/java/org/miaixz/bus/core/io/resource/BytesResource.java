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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serial;
import java.io.Serializable;
import java.net.URL;
import java.nio.charset.Charset;

import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * Resource provider based on a byte array. Note: The {@code getUrl} method of this object always returns {@code null}.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class BytesResource implements Resource, Serializable {

    @Serial
    private static final long serialVersionUID = 2852230357020L;

    /**
     * The byte array representing the resource content.
     */
    private final byte[] bytes;
    /**
     * The name of the resource.
     */
    private final String name;

    /**
     * Constructs a {@code BytesResource} with the given byte array.
     *
     * @param bytes The byte array content of the resource.
     */
    public BytesResource(final byte[] bytes) {
        this(bytes, null);
    }

    /**
     * Constructs a {@code BytesResource} with the given byte array and resource name.
     *
     * @param bytes The byte array content of the resource.
     * @param name  The name of the resource.
     */
    public BytesResource(final byte[] bytes, final String name) {
        this.bytes = bytes;
        this.name = name;
    }

    /**
     * Returns the name of this byte array resource.
     *
     * @return The name of the resource.
     */
    @Override
    public String getName() {
        return this.name;
    }

    /**
     * Returns the URL of this byte array resource.
     * <p>
     * Returns {@code null} since a byte array resource does not have a URL.
     *
     * @return {@code null}.
     */
    @Override
    public URL getUrl() {
        return null;
    }

    /**
     * Returns the size of this byte array resource.
     *
     * @return The size of the byte array in bytes.
     */
    @Override
    public long size() {
        return this.bytes.length;
    }

    /**
     * Returns an input stream for this byte array resource.
     *
     * @return A new {@link ByteArrayInputStream} that reads from the byte array.
     */
    @Override
    public InputStream getStream() {
        return new ByteArrayInputStream(this.bytes);
    }

    /**
     * Reads the content of this byte array resource as a string.
     *
     * @param charset The character set to use for decoding the bytes.
     * @return The decoded string.
     * @throws InternalException If an error occurs during decoding.
     */
    @Override
    public String readString(final Charset charset) throws InternalException {
        return StringKit.toString(this.bytes, charset);
    }

    /**
     * Reads the content of this byte array resource as a byte array.
     *
     * @return A copy of the underlying byte array.
     * @throws InternalException This implementation does not throw this exception.
     */
    @Override
    public byte[] readBytes() throws InternalException {
        return this.bytes;
    }

}
