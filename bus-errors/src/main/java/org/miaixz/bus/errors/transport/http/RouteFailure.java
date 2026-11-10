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

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

import org.miaixz.bus.core.basic.normal.Errors;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Carries verified route facts while retaining the existing validation exception contract.
 *
 * @author Kimi Liu
 */
public final class RouteFailure extends ValidateException {

    /**
     * Serialization version for the verified status and legacy validation fields.
     */
    @java.io.Serial
    private static final long serialVersionUID = 1L;

    /**
     * HTTP error status verified by the route owner.
     */
    private final HttpStatus status;
    /**
     * Read-only response headers validated when the route owner created this failure.
     */
    private final transient HttpHeaders headers;

    /**
     * Creates a routing failure identified by the component that owns the route.
     *
     * @param legacy  existing validation error code and description
     * @param status  verified route status
     * @param headers response headers verified against the selected route
     */
    public RouteFailure(Errors legacy, HttpStatus status, HttpHeaders headers) {
        super(legacy);
        this.status = java.util.Objects.requireNonNull(status, "status");
        if (!status.isError()) {
            throw new IllegalArgumentException("Route failure requires an HTTP error status");
        }
        this.headers = new HttpErrorResponse(status, headers).headers();
    }

    /**
     * Returns the HTTP status established by the route owner.
     *
     * @return verified route status
     */
    public HttpStatus status() {
        return this.status;
    }

    /**
     * Returns the validated headers supplied by the route owner.
     *
     * @return verified response headers
     */
    public HttpHeaders headers() {
        return this.headers == null ? HttpHeaders.EMPTY : this.headers;
    }

}
