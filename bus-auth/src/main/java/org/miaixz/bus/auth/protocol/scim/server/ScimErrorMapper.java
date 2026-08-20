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
package org.miaixz.bus.auth.protocol.scim.server;

import java.util.List;

import org.miaixz.bus.auth.Outcome;
import org.miaixz.bus.auth.protocol.scim.ErrorResponse;
import org.miaixz.bus.auth.protocol.scim.Scim;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;

/**
 * Maps closed Bus failures and malformed input to standard RFC 7644 Error resources.
 * <p>
 * Internal error objects, structured details, exception messages, and stack traces are never copied to the wire model.
 * </p>
 *
 * @author Kimi Liu
 */
public final class ScimErrorMapper {

    /**
     * Creates a stateless SCIM error mapper.
     */
    public ScimErrorMapper() {
        // No initialization required.
        // The complete mapping is fixed and contains no deployment state.
    }

    /**
     * Creates one standard Error resource from safe fixed values.
     *
     * @param status   decimal HTTP status string
     * @param scimType registered SCIM error type, or {@code null}
     * @param detail   fixed non-sensitive detail
     * @return immutable standard Error resource
     */
    private static ErrorResponse error(final String status, final String scimType, final String detail) {
        return new ErrorResponse(List.of(Scim.ERROR_SCHEMA), status, Optional.ofNullable(scimType),
                Optional.of(detail));
    }

    /**
     * Maps one internal failure code to a stable standard HTTP status, scimType, and safe detail.
     *
     * @param failure closed internal failure
     * @return standard SCIM Error resource
     * @throws IllegalArgumentException if {@code failure} is {@code null}
     */
    public ErrorResponse map(final Outcome.Failure failure) {
        Assert.notNull(failure, "SCIM framework failure must not be null");
        return switch (failure.error().getKey()) {
            case "400" -> error("400", "invalidSyntax", "The SCIM request is invalid.");
            case "401" -> error("401", null, "SCIM client authentication is required.");
            case "403" -> error("403", "sensitive", "The SCIM operation is not permitted.");
            case "404" -> error("404", "noTarget", "The requested SCIM resource does not exist.");
            case "405" -> error("405", null, "The HTTP method is not supported for this SCIM endpoint.");
            case "409" -> error("409", "uniqueness", "The SCIM resource violates a uniqueness constraint.");
            case "412" -> error("412", null, "The SCIM resource version precondition failed.");
            case "413" -> error("413", "tooMany", "The SCIM request exceeds an advertised limit.");
            case "429" -> error("429", "tooMany", "The SCIM request rate exceeds the service limit.");
            case "501" -> error("501", null, "The SCIM operation is not implemented.");
            case "503" -> error("503", null, "The SCIM service is temporarily unavailable.");
            default -> error("500", null, "The SCIM service could not complete the request.");
        };
    }

    /**
     * Maps a decoder failure to a standard invalidSyntax response without exposing the exception.
     *
     * @param cause parsing or validation failure used only to require an explicit failure signal
     * @return standard malformed-request Error resource
     * @throws IllegalArgumentException if {@code cause} is {@code null}
     */
    public ErrorResponse malformed(final RuntimeException cause) {
        Assert.notNull(cause, "SCIM malformed-request cause must not be null");
        return error("400", "invalidSyntax", "The SCIM request body or parameters are malformed.");
    }

}
