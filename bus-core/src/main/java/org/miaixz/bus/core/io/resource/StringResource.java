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

import java.io.Serial;

import org.miaixz.bus.core.lang.Charset;

/**
 * String resource, treating a string as a resource. This class extends {@link CharSequenceResource} to specifically
 * handle {@link String} objects as resources.
 *
 * @author Kimi Liu
 * @see CharSequenceResource
 * @since Java 17+
 */
public class StringResource extends CharSequenceResource {

    @Serial
    private static final long serialVersionUID = 2852232676563L;

    /**
     * Constructs a {@code StringResource} with the given string data, using UTF-8 encoding.
     *
     * @param data The string data to be used as the resource.
     */
    public StringResource(final String data) {
        super(data, null);
    }

    /**
     * Constructs a {@code StringResource} with the given string data and resource name, using UTF-8 encoding.
     *
     * @param data The string data to be used as the resource.
     * @param name The name of the resource.
     */
    public StringResource(final String data, final String name) {
        super(data, name, Charset.UTF_8);
    }

    /**
     * Constructs a {@code StringResource} with the given string data, resource name, and character set.
     *
     * @param data    The string data to be used as the resource.
     * @param name    The name of the resource.
     * @param charset The {@link java.nio.charset.Charset} to use for encoding the string data.
     */
    public StringResource(final String data, final String name, final java.nio.charset.Charset charset) {
        super(data, name, charset);
    }

}
