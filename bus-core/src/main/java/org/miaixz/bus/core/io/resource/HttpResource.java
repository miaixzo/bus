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

import java.io.InputStream;
import java.io.Serial;
import java.io.Serializable;
import java.net.URL;

import org.miaixz.bus.core.lang.Assert;

/**
 * HTTP resource, used for custom form data, with customizable Content-Type. This class wraps an existing
 * {@link Resource} and allows specifying a custom Content-Type for scenarios like HTTP requests.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class HttpResource implements Resource, Serializable {

    @Serial
    private static final long serialVersionUID = 2852231136075L;

    /**
     * The wrapped resource.
     */
    private final Resource resource;
    /**
     * The custom Content-Type for this HTTP resource.
     */
    private final String contentType;

    /**
     * Constructs an {@code HttpResource} with the given resource and Content-Type.
     *
     * @param resource    The resource to wrap, must not be {@code null}.
     * @param contentType The Content-Type string. If {@code null}, no custom Content-Type is set.
     * @throws IllegalArgumentException if the {@code resource} is {@code null}.
     */
    public HttpResource(final Resource resource, final String contentType) {
        this.resource = Assert.notNull(resource, "Resource must be not null !");
        this.contentType = contentType;
    }

    /**
     * Returns the name of the underlying resource.
     *
     * @return The name of the resource.
     */
    @Override
    public String getName() {
        return resource.getName();
    }

    /**
     * Returns the URL of the underlying resource.
     *
     * @return The URL of the resource.
     */
    @Override
    public URL getUrl() {
        return resource.getUrl();
    }

    /**
     * Returns the size of the underlying resource.
     *
     * @return The size of the resource in bytes.
     */
    @Override
    public long size() {
        return resource.size();
    }

    /**
     * Returns an input stream for the underlying resource.
     *
     * @return An input stream for reading the resource.
     */
    @Override
    public InputStream getStream() {
        return resource.getStream();
    }

    /**
     * Retrieves the custom Content-Type of this HTTP resource.
     *
     * @return The Content-Type string, or {@code null} if not set.
     */
    public String getContentType() {
        return this.contentType;
    }

}
