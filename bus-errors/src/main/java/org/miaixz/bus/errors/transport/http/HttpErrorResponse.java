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
package org.miaixz.bus.errors.transport.http;

import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;

/**
 * Immutable HTTP status and headers for a verified empty error response.
 *
 * @param status  verified HTTP status
 * @param headers detached, read-only response headers
 *
 * @author Kimi Liu
 */
public record HttpErrorResponse(HttpStatusCode status, HttpHeaders headers) {

    /**
     * Copies legal repeated header values without retaining writable aliases.
     *
     * @param status  verified HTTP status
     * @param headers detached, read-only response headers
     */
    public HttpErrorResponse {
        HttpHeaders detached = new HttpHeaders();
        headers.forEach((name, values) -> values.forEach(value -> {
            if (lineSafe(name) && lineSafe(value)) {
                detached.add(name, value);
            }
        }));
        headers = HttpHeaders.readOnlyHttpHeaders(detached);
    }

    /**
     * Preserves verified response headers and applies the configured cache override.
     *
     * @param status       verified status
     * @param headers      original response headers
     * @param cacheControl optional Cache-Control override
     * @return immutable response metadata
     */
    public static HttpErrorResponse empty(HttpStatusCode status, HttpHeaders headers, String cacheControl) {
        HttpHeaders resolved = new HttpHeaders();
        headers.forEach((name, values) -> resolved.put(name, List.copyOf(values)));
        if (!cacheControl.isBlank()) {
            resolved.setCacheControl(cacheControl);
        }
        return new HttpErrorResponse(status, resolved);
    }

    /**
     * Checks whether a header name or value occupies one HTTP header line.
     *
     * @param value candidate header text
     * @return whether the value is nonnull and contains no line breaks
     */
    private static boolean lineSafe(String value) {
        return value != null && value.indexOf('\r') < 0 && value.indexOf('\n') < 0;
    }

}
