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

import java.io.Serial;

import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.xyz.ByteKit;

/**
 * {@link CharSequence} resource, treating a character sequence as a resource. This class provides an implementation of
 * the {@link Resource} interface for resources represented by a {@link CharSequence}, such as a {@link String} or
 * {@link StringBuilder}.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CharSequenceResource extends BytesResource {

    @Serial
    private static final long serialVersionUID = 2852230576091L;

    /**
     * The name of the character set used for encoding. Since {@link java.nio.charset.Charset} is not serializable, its
     * name is stored instead.
     */
    private final String charsetName;

    /**
     * Constructs a {@code CharSequenceResource} with the given character sequence data, using UTF-8 encoding.
     *
     * @param data The character sequence data to be used as the resource.
     */
    public CharSequenceResource(final CharSequence data) {
        this(data, null);
    }

    /**
     * Constructs a {@code CharSequenceResource} with the given character sequence data and resource name, using UTF-8
     * encoding.
     *
     * @param data The character sequence data to be used as the resource.
     * @param name The name of the resource.
     */
    public CharSequenceResource(final CharSequence data, final String name) {
        this(data, name, Charset.UTF_8);
    }

    /**
     * Constructs a {@code CharSequenceResource} with the given character sequence data, resource name, and character
     * set.
     *
     * @param data    The character sequence data to be used as the resource.
     * @param name    The name of the resource.
     * @param charset The {@link java.nio.charset.Charset} to use for encoding the character sequence data.
     */
    public CharSequenceResource(final CharSequence data, final String name, final java.nio.charset.Charset charset) {
        super(ByteKit.toBytes(data, charset), name);
        this.charsetName = charset.name();
    }

    /**
     * Reads the content of the resource as a string using the character set specified during construction.
     *
     * @return The content of the resource as a string.
     */
    public String readString() {
        return readString(getCharset());
    }

    /**
     * Retrieves the name of the character set used for this resource.
     *
     * @return The name of the character set.
     */
    public String getCharsetName() {
        return this.charsetName;
    }

    /**
     * Retrieves the {@link java.nio.charset.Charset} object used for this resource.
     *
     * @return The {@link java.nio.charset.Charset} object.
     */
    public java.nio.charset.Charset getCharset() {
        return Charset.charset(this.charsetName);
    }

}
