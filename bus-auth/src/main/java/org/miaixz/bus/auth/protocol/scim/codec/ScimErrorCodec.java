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
package org.miaixz.bus.auth.protocol.scim.codec;

import java.util.LinkedHashMap;
import java.util.Map;

import org.miaixz.bus.auth.FabricX.Body;
import org.miaixz.bus.auth.FabricX.Headers;
import org.miaixz.bus.auth.FabricX.Request;
import org.miaixz.bus.auth.FabricX.Response;
import org.miaixz.bus.auth.protocol.scim.ErrorResponse;
import org.miaixz.bus.auth.protocol.scim.Scim;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.extra.json.JsonProvider;
import org.miaixz.bus.extra.json.JsonRecordVerifier;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Encodes RFC 7644 Error representations as status-consistent SCIM Service Provider HTTP responses.
 * <p>
 * The codec never serializes Bus errors, exceptions, stack traces, Registry values, or internal Outcome objects.
 * </p>
 *
 * @author Kimi Liu
 */
public class ScimErrorCodec {

    /**
     * Verifies the exact RFC 7644 Error object shape from its structural record.
     */
    private static final JsonRecordVerifier<ErrorDocument> ERROR_VERIFIER = JsonRecordVerifier.of(ErrorDocument.class);

    /**
     * Runtime-selected provider-neutral JSON implementation.
     */
    private final JsonProvider jsonProvider;

    /**
     * Maximum emitted encoded response body bytes.
     */
    private final long maximumBytes;

    /**
     * Maximum emitted JSON object/array nesting depth.
     */
    private final int maximumDepth;

    /**
     * Creates a strict Error response codec with explicit limits.
     *
     * @param jsonProvider externally selected provider-neutral JSON implementation
     * @param maximumBytes positive maximum encoded response body bytes
     * @param maximumDepth positive maximum JSON container depth
     * @throws IllegalArgumentException if {@code jsonProvider} is {@code null}
     * @throws ValidateException        if a limit is not positive
     */
    public ScimErrorCodec(final JsonProvider jsonProvider, final long maximumBytes, final int maximumDepth) {
        this.jsonProvider = Assert.notNull(jsonProvider, "SCIM Error JSON provider must not be null");
        if (maximumBytes <= 0 || maximumDepth <= 0) {
            throw new ValidateException("SCIM Error JSON limits must be positive");
        }
        this.maximumBytes = maximumBytes;
        this.maximumDepth = maximumDepth;
    }

    /**
     * Adds a present optional string to the Error JSON object.
     *
     * @param values target members
     * @param name   exact standard wire member name
     * @param value  optional text value
     */
    private static void put(final Map<String, JsonValue> values, final String name, final Optional<String> value) {
        if (!value.isEmpty()) {
            values.put(name, new JsonValue.StringValue(value.getOrThrow()));
        }
    }

    /**
     * Decodes one already parsed RFC 7644 Error object into the typed protocol model.
     *
     * @param object provider-neutral Error JSON object
     * @return validated typed Error response
     * @throws IllegalArgumentException if {@code object} is {@code null}
     * @throws ValidateException        if the JSON shape or a standard member is invalid
     */
    public ErrorResponse decode(final JsonValue.ObjectValue object) {
        final JsonValue.ObjectValue value = Assert.notNull(object, "SCIM Error JSON object must not be null");
        ERROR_VERIFIER.validate(value);
        return new ErrorResponse(ScimResourceCodec
                .strings(ScimResourceCodec.required(value.values(), Scim.Attributes.SCHEMAS), Scim.Attributes.SCHEMAS),
                ScimResourceCodec.requiredString(value.values(), Scim.Attributes.STATUS),
                Optional.ofNullable(ScimResourceCodec.optionalString(value.values(), Scim.Attributes.SCIM_TYPE)),
                Optional.ofNullable(ScimResourceCodec.optionalString(value.values(), Scim.Attributes.DETAIL)));
    }

    /**
     * Encodes one standard Error body and uses its status as the HTTP response status.
     *
     * @param request  originating Fabric HTTP request
     * @param response standard safe SCIM Error representation
     * @return complete non-cacheable application/scim+json HTTP error response
     * @throws IllegalArgumentException if an argument is {@code null}
     * @throws ValidateException        if the status cannot be represented as an HTTP error status or limits are
     *                                  exceeded
     */
    public Response encode(final Request request, final ErrorResponse response) {
        final Request origin = Assert.notNull(request, "SCIM Error origin request must not be null");
        final ErrorResponse value = Assert.notNull(response, "SCIM Error response must not be null");
        final int status;
        try {
            status = Integer.parseInt(value.status());
        } catch (NumberFormatException exception) {
            throw new ValidateException("SCIM Error status must be a decimal HTTP status", exception);
        }
        if (status < 300 || status > 599) {
            throw new ValidateException("SCIM Error HTTP status must be between 300 and 599");
        }
        final Map<String, JsonValue> members = new LinkedHashMap<>();
        members.put(Scim.Attributes.SCHEMAS, ScimResourceCodec.array(value.schemas()));
        members.put(Scim.Attributes.STATUS, new JsonValue.StringValue(value.status()));
        put(members, Scim.Attributes.SCIM_TYPE, value.scimType());
        put(members, Scim.Attributes.DETAIL, value.detail());
        final byte[] body = ScimResourceCodec
                .bytes(new JsonValue.ObjectValue(members), jsonProvider, maximumBytes, maximumDepth);
        return Response.builder().request(origin).code(status).headers(
                Headers.of(Http.Header.CACHE_CONTROL, Http.Cache.NO_STORE, Http.Header.PRAGMA, Http.Cache.NO_CACHE))
                .body(Body.of(body, MediaType.APPLICATION_SCIM_JSON_TYPE)).build();
    }

    /**
     * Defines the exact RFC 7644 Error JSON shape for structural verification.
     *
     * @param schemas  singleton Error schema array
     * @param status   decimal HTTP status string
     * @param scimType optional registered SCIM error keyword
     * @param detail   optional human-readable detail
     */
    private record ErrorDocument(JsonValue schemas, JsonValue status, JsonValue scimType, JsonValue detail) {

    }

}
