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
package org.miaixz.bus.image.metric.web;

import java.io.InputStream;
import java.util.Objects;

import org.miaixz.bus.core.net.Http;

/**
 * Single part in a multipart message.
 *
 * @param contentType the content type.
 * @param location    the location.
 * @param payload     the payload.
 * @author Kimi Liu
 * @since Java 21+
 */
public record MultipartPart(String contentType, String location, Payload payload) {

    /**
     * Creates a new instance.
     *
     * @param contentType the content type.
     * @param location    the location.
     * @param payload     the payload.
     */
    public MultipartPart {
        Objects.requireNonNull(contentType, "Content type cannot be null");
        Objects.requireNonNull(payload, "Payload cannot be null");
    }

    /**
     * Executes the new input stream operation.
     *
     * @return the operation result.
     */
    public InputStream newInputStream() {
        return payload.newInputStream();
    }

    /**
     * Executes the generate header operation.
     *
     * @param boundary the boundary.
     * @return the operation result.
     */
    public String generateHeader(String boundary) {
        Objects.requireNonNull(boundary, "Boundary cannot be null");
        StringBuilder header = new StringBuilder(256).append("\r\n--").append(boundary).append("\r\n")
                .append(Http.Header.CONTENT_TYPE).append(": ").append(contentType);

        long size = payload.size();
        if (size < 0) {
            header.append("\r\n").append(Http.Header.CONTENT_ENCODING).append(": gzip, identity");
        } else {
            header.append("\r\n").append(Http.Header.CONTENT_LENGTH).append(": ").append(size);
        }
        if (location != null && !location.isEmpty()) {
            header.append("\r\n").append(Http.Header.CONTENT_LOCATION).append(": ").append(location);
        }
        return header.append("\r\n\r\n").toString();
    }

}
