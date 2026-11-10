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

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import org.miaixz.bus.core.basic.normal.Errors;

/**
 * Supplies catalog-backed codes and descriptions to HTTP error renderers.
 *
 * @param code        public application error code
 * @param description public error description
 * @author Kimi Liu
 */
public record PublicHttpError(String code, String description) {

    /**
     * Resolves a registered application code without exposing diagnostic text or an unregistered code.
     *
     * @param code     proposed application code, possibly from an exception
     * @param fallback published error definition for this failure
     * @return the registered public error or its fallback
     */
    public static PublicHttpError resolve(String code, Errors fallback) {
        var registered = code == null ? null : Errors.require(code);
        return of(registered == null ? fallback : registered);
    }

    /**
     * Creates a public response from a code-owned error definition.
     *
     * @param definition published definition supplied by the protocol or error catalog
     * @return code and description for the existing wire format
     */
    public static PublicHttpError of(Errors definition) {
        return new PublicHttpError(definition.getKey(), definition.getValue());
    }

    /**
     * Builds a safe public error from a verified transport status.
     *
     * @param status verified HTTP error status
     * @return numeric code and a fixed description for that status
     */
    public static PublicHttpError forStatus(HttpStatusCode status) {
        var standard = HttpStatus.resolve(status.value());
        String description = status.is5xxServerError() ? "Request failed"
                : standard == null ? "Request rejected" : standard.getReasonPhrase();
        return new PublicHttpError(Integer.toString(status.value()), description);
    }

}
