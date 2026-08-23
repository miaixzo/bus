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
package org.miaixz.bus.auth.source.protocol.scim.codec;

import java.net.URI;
import java.util.*;
import java.util.function.Function;

import org.miaixz.bus.auth.FabricX.Body;
import org.miaixz.bus.auth.FabricX.Headers;
import org.miaixz.bus.auth.FabricX.Request;
import org.miaixz.bus.auth.FabricX.Response;
import org.miaixz.bus.auth.source.protocol.scim.*;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.core.net.url.UrlDecoder;
import org.miaixz.bus.extra.json.JsonRecordVerifier;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Decodes RFC 7644 BulkRequest messages and encodes RFC 7644 BulkResponse messages for a SCIM Service Provider.
 * <p>
 * Request decoding owns and closes the inbound body. A returned {@link BulkRequest} owns all password leases nested in
 * resource and PatchOp data and must remain open until ordered bulk execution completes.
 * </p>
 *
 * @author Kimi Liu
 */
public class ScimBulkCodec {

    /**
     * Verifies the exact top-level BulkRequest members from its structural record.
     */
    private static final JsonRecordVerifier<BulkDocument> BULK_VERIFIER = JsonRecordVerifier.of(BulkDocument.class);

    /**
     * Verifies the exact BulkRequest operation members from its structural record.
     */
    private static final JsonRecordVerifier<OperationDocument> OPERATION_VERIFIER = JsonRecordVerifier
            .of(OperationDocument.class);

    /**
     * Verifies the exact embedded SCIM error members from its structural record.
     */
    private static final JsonRecordVerifier<ErrorDocument> ERROR_VERIFIER = JsonRecordVerifier.of(ErrorDocument.class);

    /**
     * Resolves a registered resource type from its standard collection endpoint.
     */
    private final Function<String, ResourceType> resourceTypeResolver;

    /**
     * Maximum accepted or emitted encoded body bytes.
     */
    private final long maximumBytes;

    /**
     * Maximum accepted JSON object/array nesting depth.
     */
    private final int maximumDepth;

    /**
     * Creates a strict Bulk codec with explicit size and depth limits.
     *
     * @param resourceTypeResolver externally supplied registered resource-type resolver
     * @param maximumBytes         positive maximum encoded body bytes
     * @param maximumDepth         positive maximum JSON container depth
     * @throws ValidateException if a limit is not positive
     */
    public ScimBulkCodec(final Function<String, ResourceType> resourceTypeResolver, final long maximumBytes,
            final int maximumDepth) {
        this.resourceTypeResolver = Assert
                .notNull(resourceTypeResolver, "SCIM Bulk resource-type resolver must not be null");
        if (maximumBytes <= 0 || maximumDepth <= 0) {
            throw new ValidateException("SCIM Bulk JSON limits must be positive");
        }
        this.maximumBytes = maximumBytes;
        this.maximumDepth = maximumDepth;
    }

    /**
     * Decodes typed User, Group, or PatchOp operation data for the selected method and path.
     *
     * @param method  standard Bulk operation method
     * @param target  parsed SCIM resource target
     * @param version optional conditional entity-tag
     * @param object  parsed data object
     * @return typed sensitive-data owner
     */
    private static BulkRequest.Data data(
            final Http.Method method,
            final ResourceTarget target,
            final String version,
            final JsonValue.ObjectValue object) {
        if (method == Http.Method.DELETE) {
            throw new ValidateException("SCIM Bulk DELETE prohibits data");
        }
        final Resource.Reference reference = method == Http.Method.POST ? null : reference(target, version);
        if (method == Http.Method.PATCH) {
            return new BulkRequest.PatchData(ScimPatchCodec.decode(object));
        }
        final Class<? extends Resource> resourceClass = resourceClass(target);
        final Resource resource = ScimResourceCodec.decodeResource(object, resourceClass);
        if (method == Http.Method.PUT) {
            final Resource.Common common = resource instanceof User user ? user.common() : ((Group) resource).common();
            if (common.id().isEmpty() || !common.id().getOrThrow().equals(reference.id())) {
                if (resource instanceof User user) {
                    user.close();
                }
                throw new ValidateException("SCIM Bulk PUT data id must match the operation path id");
            }
        }
        return new BulkRequest.ResourceData(resource);
    }

    /**
     * Requires the standard HTTP POST Bulk endpoint without query or fragment components.
     *
     * @param request candidate Bulk HTTP request
     */
    private static void requireBulkEndpoint(final Request request) {
        if (request.method() != Http.Method.POST) {
            throw new ValidateException("SCIM BulkRequest requires HTTP POST");
        }
        final String path = request.url().toUri().getRawPath();
        if (path == null || !(Scim.Paths.BULK.equals(path) || path.endsWith(Scim.Paths.BULK))
                || !request.url().query().isEmpty() || request.url().fragment() != null) {
            throw new ValidateException("SCIM BulkRequest must target the Bulk endpoint without query or fragment");
        }
    }

    /**
     * Returns the writable resource class selected by a standard Bulk operation path.
     *
     * @param target parsed standard resource target
     * @return User or Group model class
     */
    private static Class<? extends Resource> resourceClass(final ResourceTarget target) {
        if (Scim.USER_SCHEMA.equals(target.resourceType().schema())) {
            return User.class;
        }
        if (Scim.GROUP_SCHEMA.equals(target.resourceType().schema())) {
            return Group.class;
        }
        throw new ValidateException("SCIM Bulk operation path must select Users or Groups");
    }

    /**
     * Creates the typed resource reference required by the PatchOp model.
     *
     * @param target  typed resource target
     * @param version optional operation entity-tag
     * @return typed resource reference
     */
    private static Resource.Reference reference(final ResourceTarget target, final String version) {
        return new Resource.Reference(target.resourceType().name(), target.resourceId().getOrThrow(),
                Optional.ofNullable(version));
    }

    /**
     * Converts an exact uppercase RFC 7644 operation method to the shared HTTP method constant.
     *
     * @param value decoded method token
     * @return POST, PUT, PATCH, or DELETE
     */
    private static Http.Method method(final String value) {
        final Http.Method method;
        try {
            method = Http.Method.of(value);
        } catch (IllegalArgumentException cause) {
            throw new ValidateException("SCIM Bulk method must be POST, PUT, PATCH, or DELETE", cause);
        }
        if (!method.value().equals(value)
                || !Set.of(Http.Method.POST, Http.Method.PUT, Http.Method.PATCH, Http.Method.DELETE).contains(method)) {
            throw new ValidateException("SCIM Bulk method must be POST, PUT, PATCH, or DELETE");
        }
        return method;
    }

    /**
     * Reads an optional exact positive JSON integer.
     *
     * @param values parsed object members
     * @param name   exact member name
     * @return positive integer or {@code null}
     */
    private static Integer optionalPositiveInteger(final Map<String, JsonValue> values, final String name) {
        final JsonValue value = values.get(name);
        if (value == null) {
            return null;
        }
        if (!(value instanceof JsonValue.NumberValue number) || number.value().stripTrailingZeros().scale() > 0
                || number.value().signum() <= 0) {
            throw new ValidateException("SCIM BulkRequest failOnErrors must be a positive integer");
        }
        try {
            return number.value().intValueExact();
        } catch (ArithmeticException exception) {
            throw new ValidateException("SCIM BulkRequest failOnErrors exceeds the supported range", exception);
        }
    }

    /**
     * Adds a present optional text value to an ordered JSON object.
     *
     * @param values target JSON members
     * @param name   exact wire member name
     * @param value  optional text value
     */
    private static void put(final Map<String, JsonValue> values, final String name, final Optional<String> value) {
        if (!value.isEmpty()) {
            values.put(name, new JsonValue.StringValue(value.getOrThrow()));
        }
    }

    /**
     * Erases all sensitive operation payloads accumulated before a decode failure.
     *
     * @param operations partially decoded operations
     */
    private static void close(final List<BulkRequest.Operation> operations) {
        for (BulkRequest.Operation operation : operations) {
            final BulkRequest.Data data = operation.data().getOrNull();
            if (data != null) {
                data.close();
            }
        }
    }

    /**
     * Decodes one method-specific BulkRequest operation and closes its payload if validation fails.
     *
     * @param object parsed operation object
     * @return typed operation
     */
    private BulkRequest.Operation decodeOperation(final JsonValue.ObjectValue object) {
        OPERATION_VERIFIER.validate(object);
        final Http.Method method = method(ScimResourceCodec.requiredString(object.values(), Scim.Attributes.METHOD));
        final String bulkId = ScimResourceCodec.optionalString(object.values(), Scim.Attributes.BULK_ID);
        final String path = ScimResourceCodec.requiredString(object.values(), Scim.Attributes.PATH);
        final ResourceTarget target = target(path);
        final String version = ScimResourceCodec.optionalString(object.values(), Scim.Attributes.VERSION);
        final JsonValue encodedData = object.values().get(Scim.Attributes.DATA);
        final BulkRequest.Data data;
        if (encodedData == null) {
            data = null;
        } else if (!(encodedData instanceof JsonValue.ObjectValue dataObject)) {
            throw new ValidateException("SCIM BulkRequest operation data must be an object");
        } else {
            data = data(method, target, version, dataObject);
        }
        try {
            return new BulkRequest.Operation(method, Optional.ofNullable(bulkId), target, Optional.ofNullable(version),
                    Optional.ofNullable(data));
        } catch (RuntimeException exception) {
            if (data != null) {
                data.close();
            }
            throw exception;
        }
    }

    /**
     * Resolves an exact mutation target from a two-segment relative Bulk path.
     *
     * @param path relative resource path
     * @return typed target reference
     */
    private ResourceTarget target(final String path) {
        final URI uri;
        try {
            uri = URI.create(path);
        } catch (IllegalArgumentException exception) {
            throw new ValidateException("SCIM Bulk operation path is invalid", exception);
        }
        final String rawPath = uri.getRawPath();
        if (rawPath == null || !rawPath.startsWith(Symbol.SLASH) || uri.isAbsolute() || uri.getAuthority() != null
                || uri.getQuery() != null || uri.getFragment() != null) {
            throw new ValidateException("SCIM Bulk operation path must be a relative absolute-path");
        }
        final int separator = rawPath.indexOf(Symbol.C_SLASH, 1);
        final String endpoint = separator < 0 ? rawPath : rawPath.substring(0, separator);
        final ResourceType resourceType = Assert.notNull(
                resourceTypeResolver.apply(endpoint),
                "SCIM Bulk operation endpoint must resolve to a registered ResourceType");
        if (!endpoint.equals(resourceType.endpoint())) {
            throw new ValidateException("SCIM Bulk ResourceType endpoint does not match the operation path");
        }
        if (separator < 0) {
            return new ResourceTarget(resourceType, Optional.empty());
        }
        if (separator == rawPath.length() - 1 || rawPath.indexOf(Symbol.C_SLASH, separator + 1) >= 0) {
            throw new ValidateException("SCIM Bulk operation path may identify at most one resource");
        }
        final String id = UrlDecoder.decodeStrictForPath(rawPath.substring(separator + 1), Charset.UTF_8);
        return new ResourceTarget(resourceType, Optional.of(id));
    }

    /**
     * Decodes one HTTP POST BulkRequest into ordered typed operations.
     *
     * @param request owned Fabric HTTP request whose body is closed by this method
     * @return typed BulkRequest owning all decoded sensitive payloads
     * @throws IllegalArgumentException if {@code request} is {@code null}
     * @throws ValidateException        if the endpoint, method, media, JSON, schema, or operation is invalid
     */
    public BulkRequest decode(final Request request) {
        final Request encoded = Assert.notNull(request, "SCIM Bulk HTTP request must not be null");
        requireBulkEndpoint(encoded);
        final List<BulkRequest.Operation> operations = new ArrayList<>();
        final Body body = encoded.body();
        try (body) {
            final JsonValue.ObjectValue object = ScimResourceCodec.object(body, maximumBytes, maximumDepth);
            BULK_VERIFIER.validate(object);
            final List<String> schemas = ScimResourceCodec.strings(
                    ScimResourceCodec.required(object.values(), Scim.Attributes.SCHEMAS),
                    Scim.Attributes.SCHEMAS);
            if (!schemas.equals(List.of(Scim.BULK_REQUEST_SCHEMA))) {
                throw new ValidateException("SCIM BulkRequest schemas must contain only the standard schema URI");
            }
            final Integer failOnErrors = optionalPositiveInteger(object.values(), Scim.Attributes.FAIL_ON_ERRORS);
            final JsonValue encodedOperations = ScimResourceCodec.required(object.values(), Scim.Attributes.OPERATIONS);
            if (!(encodedOperations instanceof JsonValue.ArrayValue array) || array.values().isEmpty()) {
                throw new ValidateException("SCIM BulkRequest Operations must be a non-empty array");
            }
            for (JsonValue item : array.values()) {
                if (!(item instanceof JsonValue.ObjectValue operation)) {
                    throw new ValidateException("SCIM BulkRequest operation must be an object");
                }
                operations.add(decodeOperation(operation));
            }
            return new BulkRequest(schemas, Optional.ofNullable(failOnErrors), operations);
        } catch (RuntimeException exception) {
            close(operations);
            throw exception;
        }
    }

    /**
     * Encodes an ordered BulkResponse as HTTP 200 application/scim+json.
     *
     * @param request  originating Fabric HTTP Bulk request
     * @param response standard ordered BulkResponse
     * @return complete non-cacheable HTTP response
     * @throws IllegalArgumentException if an argument is {@code null}
     * @throws ValidateException        if an embedded error or encoded size is invalid
     */
    public Response encode(final Request request, final BulkResponse response) {
        final Request origin = Assert.notNull(request, "SCIM Bulk origin request must not be null");
        final BulkResponse value = Assert.notNull(response, "SCIM BulkResponse must not be null");
        final Map<String, JsonValue> members = new LinkedHashMap<>();
        members.put(Scim.Attributes.SCHEMAS, ScimResourceCodec.array(value.schemas()));
        final List<JsonValue> operations = new ArrayList<>(value.operations().size());
        for (BulkResponse.Operation operation : value.operations()) {
            final Map<String, JsonValue> item = new LinkedHashMap<>();
            item.put(Scim.Attributes.METHOD, new JsonValue.StringValue(operation.method().value()));
            put(item, Scim.Attributes.BULK_ID, operation.bulkId());
            put(item, Scim.Attributes.LOCATION, operation.location());
            if (!operation.response().isEmpty()) {
                final JsonValue.ObjectValue error = operation.response().getOrThrow();
                ERROR_VERIFIER.validate(error);
                item.put(Scim.Attributes.RESPONSE, error);
            }
            item.put(Scim.Attributes.STATUS, new JsonValue.StringValue(operation.status()));
            operations.add(new JsonValue.ObjectValue(item));
        }
        members.put(Scim.Attributes.OPERATIONS, new JsonValue.ArrayValue(operations));
        final byte[] body = ScimResourceCodec.bytes(new JsonValue.ObjectValue(members), maximumBytes, maximumDepth);
        return Response.builder().request(origin).code(Http.Status.OK).headers(
                Headers.of(Http.Header.CACHE_CONTROL, Http.Cache.NO_STORE, Http.Header.PRAGMA, Http.Cache.NO_CACHE))
                .body(Body.of(body, MediaType.APPLICATION_SCIM_JSON_TYPE)).build();
    }

    /**
     * Defines the exact top-level BulkRequest JSON shape for structural verification.
     *
     * @param schemas      standard message schemas
     * @param failOnErrors optional failure threshold
     * @param Operations   ordered operation array
     *
     * @author Kimi Liu
     */
    private record BulkDocument(JsonValue schemas, JsonValue failOnErrors, JsonValue Operations) {

    }

    /**
     * Defines the exact BulkRequest operation JSON shape for structural verification.
     *
     * @param method  HTTP operation method
     * @param bulkId  optional request-local creation identifier
     * @param path    relative resource target
     * @param version optional entity-tag
     * @param data    optional method-specific payload
     *
     * @author Kimi Liu
     */
    private record OperationDocument(JsonValue method, JsonValue bulkId, JsonValue path, JsonValue version,
            JsonValue data) {

    }

    /**
     * Defines the exact embedded SCIM Error JSON shape for structural verification.
     *
     * @param schemas  standard Error schema
     * @param status   HTTP status text
     * @param scimType optional SCIM error keyword
     * @param detail   optional human-readable detail
     *
     * @author Kimi Liu
     */
    private record ErrorDocument(JsonValue schemas, JsonValue status, JsonValue scimType, JsonValue detail) {

    }

}
