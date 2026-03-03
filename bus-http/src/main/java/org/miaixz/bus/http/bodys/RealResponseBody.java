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
