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
package org.miaixz.bus.auth.protocol.scim;

import java.util.List;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Models the RFC 7644 SCIM error response without exposing internal Bus failure objects or exception details.
 *
 * @param schemas  singleton standard Error schema URI
 * @param status   decimal HTTP error status as a JSON string
 * @param scimType registered SCIM error keyword when applicable
 * @param detail   safe human-readable explanation when applicable
 * @author Kimi Liu
 */
public record ErrorResponse(List<String> schemas, String status, Optional<String> scimType, Optional<String> detail) {

    /**
     * Enforces the standard Error schema and canonical status representation.
     *
     * @throws IllegalArgumentException if a required value or optional container is {@code null}
     * @throws ValidateException        if the schema, status, or an optional text value is invalid
     */
    public ErrorResponse {
        Assert.notNull(schemas, "SCIM Error schemas must not be null");
        schemas = List.copyOf(schemas);
        if (!schemas.equals(List.of(Scim.ERROR_SCHEMA))) {
            throw new ValidateException("SCIM Error schemas must contain only the standard schema URI");
        }
        status = Assert.notBlank(status, "SCIM Error status must not be blank");
        validateStatus(status);
        scimType = optionalText(scimType, "SCIM Error scimType");
        detail = optionalText(detail, "SCIM Error detail");
    }

    /**
     * Requires a three-digit decimal HTTP error status from 300 through 599.
     *
     * @param value status lexical value
     */
    private static void validateStatus(final String value) {
        if (value.length() != 3 || !Character.isDigit(value.charAt(0)) || !Character.isDigit(value.charAt(1))
                || !Character.isDigit(value.charAt(2))) {
            throw new ValidateException("SCIM Error status must be a three-digit decimal string");
        }
        final int statusCode = Integer.parseInt(value);
        if (statusCode < 300 || statusCode > 599) {
            throw new ValidateException("SCIM Error status must be between 300 and 599");
        }
    }

    /**
     * Normalizes a required Bus Optional containing non-blank text.
     *
     * @param value required optional container
     * @param label validation label
     * @return independent optional with the same text value
     */
    private static Optional<String> optionalText(final Optional<String> value, final String label) {
        Assert.notNull(value, label + " container must not be null");
        if (!value.isEmpty()) {
            Assert.notBlank(value.getOrThrow(), label + " must not be blank");
        }
        return Optional.ofNullable(value.getOrNull());
    }

    /**
     * Returns a redacted representation that omits the potentially sensitive error detail.
     *
     * @return redacted error representation
     */
    @Override
    public String toString() {
        return "ErrorResponse[status=" + status + ", scimType=" + scimType + ", detail=<redacted>]";
    }

}
