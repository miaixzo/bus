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
package org.miaixz.bus.http.bodys;

import org.miaixz.bus.core.io.source.BufferSource;
import org.miaixz.bus.core.lang.MediaType;

/**
 * A concrete implementation of {@link ResponseBody}.
 * <p>
 * This class represents the content of an HTTP response and can only be used once. It provides access to the media
 * type, content length, and data source of the response content. The media type is stored as a string to avoid parsing
 * errors.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class RealResponseBody extends ResponseBody {

    /**
     * The media type as a string.
     */
    private final String contentType;
    /**
     * The content length.
     */
    private final long length;
    /**
     * The data source.
     */
    private final BufferSource source;

    /**
     * Constructs a new {@code RealResponseBody} instance.
     *
     * @param contentType The media type as a string, which may be null.
     * @param length      The content length.
     * @param source      The data source.
     */
    public RealResponseBody(String contentType, long length, BufferSource source) {
        this.contentType = contentType;
        this.length = length;
        this.source = source;
    }

    /**
     * Returns the media type of this response body.
     *
     * @return The media type, or null if not present.
     */
    @Override
    public MediaType contentType() {
        return null != contentType ? MediaType.valueOf(contentType) : null;
    }

    /**
     * Returns the content length of this response body.
     *
     * @return The content length.
     */
    @Override
    public long contentLength() {
        return length;
    }

    /**
     * Returns the data source for this response body.
     *
     * @return The data source.
     */
    @Override
    public BufferSource source() {
        return source;
    }

}
