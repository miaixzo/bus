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

import java.util.Set;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;

/**
 * Immutable HTTP status and headers for a verified empty error response.
 *
 * @param status  verified HTTP status
 * @param headers detached, read-only response headers
 * @author Kimi Liu
 */
public record HttpErrorResponse(HttpStatusCode status, HttpHeaders headers) {

    /**
     * Authentication challenges with fixed public values for framework errors.
     */
    private static final Set<String> CHALLENGES = Set.of("Basic", "Bearer", "Bearer error=\"invalid_token\"",
            "Bearer error=\"insufficient_scope\"", "Bearer error=\"invalid_request\"");

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
     * Selects approved headers for the verified status and applies the configured cache override.
     *
     * @param status       verified status
     * @param headers      original response headers
     * @param cacheControl optional Cache-Control override
     * @return immutable response metadata
     */
    public static HttpErrorResponse empty(HttpStatusCode status, HttpHeaders headers, String cacheControl) {
        HttpHeaders resolved = new HttpHeaders();
        headers.forEach((name, values) -> {
            if (publicHeader(status.value(), name)) {
                var safe = values.stream().filter(value -> publicValue(name, value)).toList();
                if (!safe.isEmpty()) {
                    resolved.put(name, safe);
                }
            }
        });
        if (!cacheControl.isBlank()) {
            resolved.setCacheControl(cacheControl);
        }
        return new HttpErrorResponse(status, resolved);
    }

    /**
     * Selects protocol headers applicable to the verified error status.
     *
     * @param status numeric error status
     * @param name   response header name
     * @return whether the error response can carry the header
     */
    private static boolean publicHeader(int status, String name) {
        return HttpHeaders.CACHE_CONTROL.equalsIgnoreCase(name)
                || (status == 405 && HttpHeaders.ALLOW.equalsIgnoreCase(name))
                || ((status == 406 || status == 415) && HttpHeaders.ACCEPT.equalsIgnoreCase(name))
                || (status == 415 && "Accept-Patch".equalsIgnoreCase(name))
                || (status == 401 && HttpHeaders.WWW_AUTHENTICATE.equalsIgnoreCase(name))
                || ((status == 429 || status == 503) && HttpHeaders.RETRY_AFTER.equalsIgnoreCase(name));
    }

    /**
     * Validates the syntax or fixed vocabulary of a permitted protocol header.
     *
     * @param name permitted header name
     * @param value proposed header value
     * @return whether the value can be included in the public response
     */
    private static boolean publicValue(String name, String value) {
        if (!lineSafe(value) || value.length() > 1024) {
            return false;
        }
        if (HttpHeaders.ALLOW.equalsIgnoreCase(name)) {
            return value.matches("(GET|HEAD|POST|PUT|PATCH|DELETE|OPTIONS|TRACE|CONNECT)"
                    + "(, *?(GET|HEAD|POST|PUT|PATCH|DELETE|OPTIONS|TRACE|CONNECT))*");
        }
        if (HttpHeaders.WWW_AUTHENTICATE.equalsIgnoreCase(name)) {
            return CHALLENGES.contains(value);
        }
        if (HttpHeaders.RETRY_AFTER.equalsIgnoreCase(name)) {
            if (value.matches("[0-9]{1,10}")) {
                return true;
            }
            try {
                java.time.ZonedDateTime.parse(value, java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME);
                return true;
            } catch (java.time.format.DateTimeParseException invalid) {
                return false;
            }
        }
        if (HttpHeaders.CACHE_CONTROL.equalsIgnoreCase(name)) {
            return value.matches("(no-store|no-cache|private|public|max-age=[0-9]{1,10}|must-revalidate)"
                    + "(, *?(no-store|no-cache|private|public|max-age=[0-9]{1,10}|must-revalidate))*");
        }
        try {
            return !MediaType.parseMediaTypes(value).isEmpty();
        } catch (IllegalArgumentException invalid) {
            return false;
        }
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
