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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.miaixz.bus.auth.protocol.scim.*;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.extra.json.JsonProvider;
import org.miaixz.bus.extra.json.JsonValue;
import org.miaixz.bus.fabric.Headers;
import org.miaixz.bus.fabric.Payload;
import org.miaixz.bus.fabric.protocol.http.HttpRequest;
import org.miaixz.bus.fabric.protocol.http.HttpResponse;
import org.miaixz.bus.fabric.protocol.http.body.PayloadBody;

/**
 * Encodes RFC 7643 SCIM discovery resources and discovery ListResponse messages for a Service Provider.
 * <p>
 * This response-only codec preserves the exact standard wire names, including uppercase {@code Resources}, and does not
 * accept application, Registry, Provider, or Vendor metadata.
 * </p>
 *
 * @author Kimi Liu
 */
public final class ScimDiscoveryCodec {

    /**
     * Runtime-selected provider-neutral JSON implementation.
     */
    private final JsonProvider jsonProvider;

    /**
     * Maximum emitted encoded response body bytes.
     */
    private final long maximumBytes;

    /**
     * Maximum JSON depth reserved for the uniform SCIM codec construction contract.
     */
    private final int maximumDepth;

    /**
     * Creates a discovery response codec with explicit size and depth limits.
     *
     * @param jsonProvider externally selected provider-neutral JSON implementation
     * @param maximumBytes positive maximum encoded response body bytes
     * @param maximumDepth positive maximum JSON container depth
     * @throws IllegalArgumentException if {@code jsonProvider} is {@code null}
     * @throws ValidateException        if a limit is not positive
     */
    public ScimDiscoveryCodec(final JsonProvider jsonProvider, final long maximumBytes, final int maximumDepth) {
        this.jsonProvider = Assert.notNull(jsonProvider, "SCIM discovery JSON provider must not be null");
        if (maximumBytes <= 0 || maximumDepth <= 0) {
            throw new ValidateException("SCIM discovery JSON limits must be positive");
        }
        this.maximumBytes = maximumBytes;
        this.maximumDepth = maximumDepth;
    }

    /**
     * Encodes one ResourceType without constructing an intermediate HTTP response.
     *
     * @param value standard ResourceType resource
     * @return provider-neutral discovery JSON object
     */
    private static JsonValue.ObjectValue resourceType(final ResourceType value) {
        final Map<String, JsonValue> members = base(value.schemas(), value.meta());
        put(members, Scim.Attributes.ID, value.id());
        members.put(Scim.Attributes.RESOURCE_NAME, string(value.name()));
        put(members, Scim.Attributes.DESCRIPTION, value.description());
        members.put(Scim.Attributes.ENDPOINT, string(value.endpoint()));
        members.put(Scim.Attributes.SCHEMA, string(value.schema()));
        if (!value.schemaExtensions().isEmpty()) {
            final List<JsonValue> extensions = new ArrayList<>(value.schemaExtensions().size());
            for (ResourceType.SchemaExtension extension : value.schemaExtensions()) {
                extensions.add(
                        new JsonValue.ObjectValue(Map.of(
                                Scim.Attributes.SCHEMA,
                                string(extension.schema()),
                                Scim.Attributes.REQUIRED,
                                bool(extension.required()))));
            }
            members.put(Scim.Attributes.SCHEMA_EXTENSIONS, new JsonValue.ArrayValue(extensions));
        }
        return new JsonValue.ObjectValue(members);
    }

    /**
     * Encodes one Schema without constructing an intermediate HTTP response.
     *
     * @param value standard Schema resource
     * @return provider-neutral discovery JSON object
     */
    private static JsonValue.ObjectValue schema(final Schema value) {
        final Map<String, JsonValue> members = base(value.schemas(), value.meta());
        members.put(Scim.Attributes.ID, string(value.id()));
        put(members, Scim.Attributes.RESOURCE_NAME, value.name());
        put(members, Scim.Attributes.DESCRIPTION, value.description());
        final List<JsonValue> attributes = new ArrayList<>(value.attributes().size());
        for (Schema.AttributeDefinition definition : value.attributes()) {
            attributes.add(attribute(definition));
        }
        members.put(Scim.Attributes.ATTRIBUTES, new JsonValue.ArrayValue(attributes));
        return new JsonValue.ObjectValue(members);
    }

    /**
     * Encodes one recursive RFC 7643 schema attribute definition.
     *
     * @param value typed attribute definition
     * @return provider-neutral attribute JSON object
     */
    private static JsonValue.ObjectValue attribute(final Schema.AttributeDefinition value) {
        final Map<String, JsonValue> members = new LinkedHashMap<>();
        members.put(Scim.Attributes.RESOURCE_NAME, string(value.name()));
        members.put(Scim.Attributes.TYPE, string(value.type().value()));
        members.put(Scim.Attributes.MULTI_VALUED, bool(value.multiValued()));
        put(members, Scim.Attributes.DESCRIPTION, value.description());
        members.put(Scim.Attributes.REQUIRED, bool(value.required()));
        members.put(Scim.Attributes.CASE_EXACT, bool(value.caseExact()));
        members.put(Scim.Attributes.MUTABILITY, string(value.mutability().value()));
        members.put(Scim.Attributes.RETURNED, string(value.returned().value()));
        members.put(Scim.Attributes.UNIQUENESS, string(value.uniqueness().value()));
        if (!value.canonicalValues().isEmpty()) {
            members.put(Scim.Attributes.CANONICAL_VALUES, ScimResourceCodec.array(value.canonicalValues()));
        }
        if (!value.referenceTypes().isEmpty()) {
            members.put(Scim.Attributes.REFERENCE_TYPES, ScimResourceCodec.array(value.referenceTypes()));
        }
        if (!value.subAttributes().isEmpty()) {
            final List<JsonValue> subAttributes = new ArrayList<>(value.subAttributes().size());
            for (Schema.AttributeDefinition child : value.subAttributes()) {
                subAttributes.add(attribute(child));
            }
            members.put(Scim.Attributes.SUB_ATTRIBUTES, new JsonValue.ArrayValue(subAttributes));
        }
        return new JsonValue.ObjectValue(members);
    }

    /**
     * Creates common discovery resource members with optional standard Meta.
     *
     * @param schemas standard resource schema identifiers
     * @param meta    optional service-provider-maintained metadata
     * @return ordered mutable member map
     */
    private static Map<String, JsonValue> base(final List<String> schemas, final Optional<Resource.Meta> meta) {
        final Map<String, JsonValue> members = new LinkedHashMap<>();
        members.put(Scim.Attributes.SCHEMAS, ScimResourceCodec.array(schemas));
        if (!meta.isEmpty()) {
            members.put(Scim.Attributes.META, ScimResourceCodec.encodeMeta(meta.getOrThrow()));
        }
        return members;
    }

    /**
     * Encodes one simple feature support object.
     *
     * @param value typed feature support declaration
     * @return JSON object containing the standard supported member
     */
    private static JsonValue.ObjectValue supported(final ServiceProviderConfig.Supported value) {
        return new JsonValue.ObjectValue(Map.of(Scim.Attributes.SUPPORTED, bool(value.supported())));
    }

    /**
     * Encodes the Bulk feature support declaration and limits.
     *
     * @param value typed Bulk support declaration
     * @return standard Bulk support JSON object
     */
    private static JsonValue.ObjectValue bulk(final ServiceProviderConfig.Bulk value) {
        return new JsonValue.ObjectValue(Map.of(
                Scim.Attributes.SUPPORTED,
                bool(value.supported()),
                Scim.Attributes.MAX_OPERATIONS,
                number(value.maxOperations()),
                Scim.Attributes.MAX_PAYLOAD_SIZE,
                number(value.maxPayloadSize())));
    }

    /**
     * Encodes the filter support declaration and maximum result count.
     *
     * @param value typed filter support declaration
     * @return standard filter support JSON object
     */
    private static JsonValue.ObjectValue filter(final ServiceProviderConfig.FilterSupport value) {
        return new JsonValue.ObjectValue(Map.of(
                Scim.Attributes.SUPPORTED,
                bool(value.supported()),
                Scim.Attributes.MAX_RESULTS,
                number(value.maxResults())));
    }

    /**
     * Creates an exact JSON string value.
     *
     * @param value decoded text
     * @return provider-neutral string value
     */
    private static JsonValue.StringValue string(final String value) {
        return new JsonValue.StringValue(value);
    }

    /**
     * Creates an exact JSON boolean value.
     *
     * @param value boolean value
     * @return provider-neutral boolean value
     */
    private static JsonValue.BooleanValue bool(final boolean value) {
        return new JsonValue.BooleanValue(value);
    }

    /**
     * Creates an exact JSON integral number value.
     *
     * @param value integral value
     * @return provider-neutral number value
     */
    private static JsonValue.NumberValue number(final long value) {
        return new JsonValue.NumberValue(BigDecimal.valueOf(value));
    }

    /**
     * Adds a present optional string to an ordered JSON object.
     *
     * @param values target members
     * @param name   exact wire member name
     * @param value  optional text value
     */
    private static void put(final Map<String, JsonValue> values, final String name, final Optional<String> value) {
        if (!value.isEmpty()) {
            values.put(name, string(value.getOrThrow()));
        }
    }

    /**
     * Adds a present optional integer to an ordered JSON object.
     *
     * @param values target members
     * @param name   exact wire member name
     * @param value  optional integer value
     */
    private static void putNumber(
            final Map<String, JsonValue> values,
            final String name,
            final Optional<Integer> value) {
        if (!value.isEmpty()) {
            values.put(name, number(value.getOrThrow()));
        }
    }

    /**
     * Encodes one ServiceProviderConfig discovery resource as HTTP 200.
     *
     * @param request  originating Fabric HTTP request
     * @param resource standard ServiceProviderConfig resource
     * @return complete non-cacheable application/scim+json response
     */
    public HttpResponse encode(final HttpRequest request, final ServiceProviderConfig resource) {
        final ServiceProviderConfig value = Assert.notNull(resource, "SCIM ServiceProviderConfig must not be null");
        final Map<String, JsonValue> members = base(value.schemas(), value.meta());
        put(members, Scim.Attributes.DOCUMENTATION_URI, value.documentationUri());
        members.put(Scim.Attributes.PATCH_SUPPORTED, supported(value.patch()));
        members.put(Scim.Attributes.BULK_SUPPORTED, bulk(value.bulk()));
        members.put(Scim.Attributes.FILTER_SUPPORTED, filter(value.filter()));
        members.put(Scim.Attributes.CHANGE_PASSWORD, supported(value.changePassword()));
        members.put(Scim.Attributes.SORT_SUPPORTED, supported(value.sort()));
        members.put(Scim.Attributes.ETAG_SUPPORTED, supported(value.etag()));
        final List<JsonValue> schemes = new ArrayList<>(value.authenticationSchemes().size());
        for (ServiceProviderConfig.AuthenticationScheme scheme : value.authenticationSchemes()) {
            final Map<String, JsonValue> item = new LinkedHashMap<>();
            item.put(Scim.Attributes.TYPE, string(scheme.type()));
            item.put(Scim.Attributes.RESOURCE_NAME, string(scheme.name()));
            item.put(Scim.Attributes.DESCRIPTION, string(scheme.description()));
            put(item, Scim.Attributes.SPEC_URI, scheme.specUri());
            put(item, Scim.Attributes.DOCUMENTATION_URI, scheme.documentationUri());
            schemes.add(new JsonValue.ObjectValue(item));
        }
        members.put(Scim.Attributes.AUTHENTICATION_SCHEMES, new JsonValue.ArrayValue(schemes));
        return response(request, new JsonValue.ObjectValue(members), value.meta());
    }

    /**
     * Encodes one ResourceType discovery resource as HTTP 200.
     *
     * @param request  originating Fabric HTTP request
     * @param resource standard ResourceType resource
     * @return complete non-cacheable application/scim+json response
     */
    public HttpResponse encode(final HttpRequest request, final ResourceType resource) {
        final ResourceType value = Assert.notNull(resource, "SCIM ResourceType must not be null");
        final Map<String, JsonValue> members = base(value.schemas(), value.meta());
        put(members, Scim.Attributes.ID, value.id());
        members.put(Scim.Attributes.RESOURCE_NAME, string(value.name()));
        put(members, Scim.Attributes.DESCRIPTION, value.description());
        members.put(Scim.Attributes.ENDPOINT, string(value.endpoint()));
        members.put(Scim.Attributes.SCHEMA, string(value.schema()));
        if (!value.schemaExtensions().isEmpty()) {
            final List<JsonValue> extensions = new ArrayList<>(value.schemaExtensions().size());
            for (ResourceType.SchemaExtension extension : value.schemaExtensions()) {
                extensions.add(
                        new JsonValue.ObjectValue(Map.of(
                                Scim.Attributes.SCHEMA,
                                string(extension.schema()),
                                Scim.Attributes.REQUIRED,
                                bool(extension.required()))));
            }
            members.put(Scim.Attributes.SCHEMA_EXTENSIONS, new JsonValue.ArrayValue(extensions));
        }
        return response(request, new JsonValue.ObjectValue(members), value.meta());
    }

    /**
     * Encodes one Schema discovery resource and all recursive attribute definitions as HTTP 200.
     *
     * @param request  originating Fabric HTTP request
     * @param resource standard Schema resource
     * @return complete non-cacheable application/scim+json response
     */
    public HttpResponse encode(final HttpRequest request, final Schema resource) {
        final Schema value = Assert.notNull(resource, "SCIM Schema must not be null");
        final Map<String, JsonValue> members = base(value.schemas(), value.meta());
        members.put(Scim.Attributes.ID, string(value.id()));
        put(members, Scim.Attributes.RESOURCE_NAME, value.name());
        put(members, Scim.Attributes.DESCRIPTION, value.description());
        final List<JsonValue> attributes = new ArrayList<>(value.attributes().size());
        for (Schema.AttributeDefinition attribute : value.attributes()) {
            attributes.add(attribute(attribute));
        }
        members.put(Scim.Attributes.ATTRIBUTES, new JsonValue.ArrayValue(attributes));
        return response(request, new JsonValue.ObjectValue(members), value.meta());
    }

    /**
     * Encodes a discovery ListResponse containing only ResourceType or only Schema resources as HTTP 200.
     *
     * @param request  originating Fabric HTTP request
     * @param response homogeneous standard discovery ListResponse
     * @return complete non-cacheable application/scim+json response
     * @throws ValidateException if resources are heterogeneous or not discovery resources
     */
    public HttpResponse encode(final HttpRequest request, final ListResponse response) {
        final ListResponse value = Assert.notNull(response, "SCIM discovery ListResponse must not be null");
        Class<?> resourceClass = null;
        final List<JsonValue> resources = new ArrayList<>(value.resources().size());
        for (Resource resource : value.resources()) {
            if (!(resource instanceof ResourceType) && !(resource instanceof Schema)) {
                throw new ValidateException(
                        "SCIM discovery ListResponse must contain ResourceType or Schema resources");
            }
            if (resourceClass == null) {
                resourceClass = resource.getClass();
            } else if (resourceClass != resource.getClass()) {
                throw new ValidateException("SCIM discovery ListResponse resources must be homogeneous");
            }
            resources.add(resource instanceof ResourceType type ? resourceType(type) : schema((Schema) resource));
        }
        final Map<String, JsonValue> members = new LinkedHashMap<>();
        members.put(Scim.Attributes.SCHEMAS, ScimResourceCodec.array(value.schemas()));
        members.put(Scim.Attributes.TOTAL_RESULTS, number(value.totalResults()));
        putNumber(members, Scim.Attributes.START_INDEX, value.startIndex());
        putNumber(members, Scim.Attributes.ITEMS_PER_PAGE, value.itemsPerPage());
        members.put(Scim.Attributes.RESOURCES, new JsonValue.ArrayValue(resources));
        return response(request, new JsonValue.ObjectValue(members), Optional.empty());
    }

    /**
     * Serializes one discovery object to a bounded non-cacheable HTTP 200 response.
     *
     * @param request originating Fabric HTTP request
     * @param object  complete standard discovery JSON object
     * @param meta    optional metadata used for Location and ETag headers
     * @return complete HTTP response
     */
    private HttpResponse response(
            final HttpRequest request,
            final JsonValue.ObjectValue object,
            final Optional<Resource.Meta> meta) {
        final HttpRequest origin = Assert.notNull(request, "SCIM discovery origin request must not be null");
        final byte[] body = ScimResourceCodec.bytes(object, jsonProvider, maximumBytes, maximumDepth);
        final Headers.Builder headers = Headers.builder().add(Http.Header.CACHE_CONTROL, Http.Cache.NO_STORE)
                .add(Http.Header.PRAGMA, Http.Cache.NO_CACHE);
        final Resource.Meta metadata = meta.getOrNull();
        if (metadata != null && !metadata.location().isEmpty()) {
            headers.add(Http.Header.LOCATION, metadata.location().getOrThrow());
        }
        if (metadata != null && !metadata.version().isEmpty()) {
            headers.add(Http.Header.ETAG, metadata.version().getOrThrow());
        }
        return HttpResponse.builder().request(origin).code(Http.Status.OK).headers(headers.build())
                .body(PayloadBody.of(Payload.of(body), MediaType.APPLICATION_SCIM_JSON_TYPE)).build();
    }

}
