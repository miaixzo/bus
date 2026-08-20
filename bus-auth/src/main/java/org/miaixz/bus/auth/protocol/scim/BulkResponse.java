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

import java.net.URI;
import java.util.List;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Models an RFC 7644 bulk response and preserves operation result order.
 *
 * @param schemas    singleton standard BulkResponse schema URI
 * @param operations operation results in request processing order
 * @author Kimi Liu
 */
public record BulkResponse(List<String> schemas, List<Operation> operations) {

    /**
     * Enforces the BulkResponse schema and immutable operation sequence.
     *
     * @throws IllegalArgumentException if a collection or operation is {@code null}
     * @throws ValidateException        if the schema is not the standard BulkResponse schema
     */
    public BulkResponse {
        Assert.notNull(schemas, "SCIM BulkResponse schemas must not be null");
        schemas = List.copyOf(schemas);
        if (!schemas.equals(List.of(Scim.BULK_RESPONSE_SCHEMA))) {
            throw new ValidateException("SCIM BulkResponse schemas must contain only the standard schema URI");
        }
        Assert.notNull(operations, "SCIM BulkResponse Operations must not be null");
        for (Operation operation : operations) {
            Assert.notNull(operation, "SCIM BulkResponse operation must not be null");
        }
        operations = List.copyOf(operations);
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
     * Parses a canonical three-digit HTTP status between 200 and 599.
     *
     * @param value status lexical value
     * @return parsed status code
     */
    private static int parseStatus(final String value) {
        if (value.length() != 3 || !Character.isDigit(value.charAt(0)) || !Character.isDigit(value.charAt(1))
                || !Character.isDigit(value.charAt(2))) {
            throw new ValidateException("SCIM bulk response status must be a three-digit decimal string");
        }
        final int status = Integer.parseInt(value);
        if (status < 200 || status > 599) {
            throw new ValidateException("SCIM bulk response status must be between 200 and 599");
        }
        return status;
    }

    /**
     * Requires an embedded RFC 7644 Error object whose body status matches the operation status.
     *
     * @param error  embedded JSON error object
     * @param status enclosing operation status
     */
    private static void validateError(final JsonValue.ObjectValue error, final String status) {
        final JsonValue schemas = error.values().get(Scim.Attributes.SCHEMAS);
        if (!(schemas instanceof JsonValue.ArrayValue array) || array.values().size() != 1
                || !(array.values().get(0) instanceof JsonValue.StringValue schema)
                || !Scim.ERROR_SCHEMA.equals(schema.value())) {
            throw new ValidateException("SCIM bulk error response must contain the standard Error schema");
        }
        final JsonValue bodyStatus = error.values().get(Scim.Attributes.STATUS);
        if (!(bodyStatus instanceof JsonValue.StringValue text) || !status.equals(text.value())) {
            throw new ValidateException("SCIM bulk error response status must match its operation status");
        }
    }

    /**
     * Models one RFC 7644 bulk operation result.
     *
     * @param method   original POST, PUT, PATCH, or DELETE method
     * @param bulkId   original request-local POST identifier when supplied
     * @param location absolute URI of the affected resource when supplied
     * @param response standard SCIM Error object for an unsuccessful result
     * @param status   three-digit decimal HTTP status code
     * @author Kimi Liu
     */
    public record Operation(Http.Method method, Optional<String> bulkId, Optional<String> location,
            Optional<JsonValue.ObjectValue> response, String status) {

        /**
         * Enforces method, URI, status, and standard error-object consistency.
         *
         * @throws IllegalArgumentException if a required value or optional container is {@code null}
         * @throws ValidateException        if a method, location, status, or response combination is invalid
         */
        public Operation {
            method = Assert.notNull(method, "SCIM bulk response method must not be null");
            if (method != Http.Method.POST && method != Http.Method.PUT && method != Http.Method.PATCH
                    && method != Http.Method.DELETE) {
                throw new ValidateException("SCIM bulk response method must be POST, PUT, PATCH, or DELETE");
            }
            bulkId = optionalText(bulkId, "SCIM bulk response bulkId");
            location = optionalText(location, "SCIM bulk response location");
            if (!location.isEmpty()) {
                try {
                    if (!URI.create(location.getOrThrow()).isAbsolute()) {
                        throw new ValidateException("SCIM bulk response location must be absolute");
                    }
                } catch (IllegalArgumentException exception) {
                    throw new ValidateException("SCIM bulk response location must be a valid absolute URI", exception);
                }
            }
            Assert.notNull(response, "SCIM bulk response error container must not be null");
            final JsonValue.ObjectValue source = response.getOrNull();
            response = source == null ? Optional.empty() : Optional.of(new JsonValue.ObjectValue(source.values()));
            status = Assert.notBlank(status, "SCIM bulk response status must not be blank");
            final int statusCode = parseStatus(status);
            if (statusCode >= 300) {
                if (response.isEmpty()) {
                    throw new ValidateException("SCIM failed bulk response requires a standard Error object");
                }
                validateError(response.getOrThrow(), status);
            } else if (!response.isEmpty()) {
                throw new ValidateException("SCIM successful bulk response must not contain an Error object");
            }
        }

        /**
         * Returns a redacted representation that does not disclose SCIM error details.
         *
         * @return redacted operation-result representation
         */
        @Override
        public String toString() {
            return "Operation[method=" + method + ", status=" + status + ", response=<redacted>]";
        }

    }

}
