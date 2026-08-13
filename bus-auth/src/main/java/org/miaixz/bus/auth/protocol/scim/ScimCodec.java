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

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;

import org.miaixz.bus.auth.codec.json.StrictJsonReader;
import org.miaixz.bus.auth.protocol.scim.SCIM.MutationResult;
import org.miaixz.bus.auth.protocol.scim.SCIM.Page;
import org.miaixz.bus.auth.protocol.scim.SCIM.ProtocolError;
import org.miaixz.bus.auth.protocol.scim.SCIM.ResourceType;
import org.miaixz.bus.auth.protocol.scim.ScimBulk.Entry;
import org.miaixz.bus.auth.protocol.scim.ScimPatch.Operation;
import org.miaixz.bus.auth.protocol.scim.ScimPatch.Type;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.extra.json.JsonProvider;

/**
 * Maps strict SCIM JSON to the closed domain model and emits deterministic JSON-compatible maps. Input is scanned by
 * {@link StrictJsonReader}; protocol code never invokes a third-party JSON implementation directly.
 *
 * @author Kimi Liu
 */
public final class ScimCodec {

    /**
     * Framework JSON port used only after strict bounded input validation.
     */
    private final JsonProvider json;

    /**
     * Maximum accepted SCIM JSON bytes.
     */
    private final int maximumBytes;

    /**
     * Maximum accepted SCIM JSON nesting depth.
     */
    private final int maximumDepth;

    /**
     * Strict JSON grammar reader.
     */
    private final StrictJsonReader reader;

    /**
     * Creates one codec.
     *
     * @param json         JSON provider
     * @param maximumBytes maximum accepted JSON bytes
     * @param maximumDepth maximum accepted JSON nesting depth
     * @throws ValidateException if the provider is null or either bound is non-positive
     */
    public ScimCodec(final JsonProvider json, final int maximumBytes, final int maximumDepth) {
        this.json = Assert.notNull(json, () -> new ValidateException("SCIM JSON provider must not be null"));
        Assert.isTrue(maximumBytes > Normal._0, () -> new ValidateException("SCIM maximum bytes must be positive"));
        Assert.isTrue(maximumDepth > Normal._0, () -> new ValidateException("SCIM maximum depth must be positive"));
        this.maximumBytes = maximumBytes;
        this.maximumDepth = maximumDepth;
        this.reader = new StrictJsonReader(json, maximumBytes, maximumDepth);
    }

    /**
     * Converts one decoded object to a resource.
     *
     * @param source source object
     * @param type   expected type
     * @return resource
     * @throws ValidateException if schemas, metadata, or resource members are invalid
     */
    static ScimResource resource(final Map<String, Object> source, final ResourceType type) {
        final Set<String> schemas = Set.copyOf(strings(list(source.get("schemas"), "SCIM schemas")));
        Assert.isTrue(
                schemas.contains(type.schema()),
                () -> new ValidateException("SCIM resource core schema is missing"));
        final LinkedHashMap<String, Object> attributes = new LinkedHashMap<>(source);
        attributes.keySet().removeAll(Set.of("schemas", "id", "externalId", "meta"));
        ScimResource.Meta meta = null;
        if (source.get("meta") != null) {
            final Map<String, Object> value = map(source.get("meta"), "SCIM metadata");
            meta = new ScimResource.Meta(type, Instant.parse(text(value.get("created"), "SCIM created")),
                    Instant.parse(text(value.get("lastModified"), "SCIM lastModified")),
                    java.net.URI.create(text(value.get("location"), "SCIM location")),
                    text(value.get("version"), "SCIM version"));
        }
        return new ScimResource(type, optional(source.get("id")), optional(source.get("externalId")), schemas,
                attributes, meta);
    }

    /**
     * Requires one schema identifier.
     *
     * @param source source object
     * @param schema required schema
     * @throws ValidateException if the required schema is absent
     */
    static void requireSchema(final Map<String, Object> source, final String schema) {
        Assert.isTrue(
                strings(list(source.get("schemas"), "SCIM schemas")).contains(schema),
                () -> new ValidateException("SCIM message schema is invalid"));
    }

    /**
     * Converts one object to a string-keyed map.
     *
     * @param source source value
     * @param name   diagnostic name
     * @return validated map
     */
    static Map<String, Object> map(final Object source, final String name) {
        if (!(source instanceof Map<?, ?> values)) {
            throw new ValidateException(name + " must be an object");
        }
        final LinkedHashMap<String, Object> result = new LinkedHashMap<>();
        values.forEach((key, value) -> {
            if (!(key instanceof String text)) {
                throw new ValidateException(name + " contains a non-string key");
            }
            result.put(text, value);
        });
        return result;
    }

    /**
     * Requires one array value.
     *
     * @param source source value
     * @param name   diagnostic name
     * @return array
     */
    static List<?> list(final Object source, final String name) {
        if (!(source instanceof List<?> values)) {
            throw new ValidateException(name + " must be an array");
        }
        return values;
    }

    /**
     * Converts an array to strings.
     *
     * @param source source array
     * @return strings
     */
    static List<String> strings(final List<?> source) {
        return source.stream().map(value -> text(value, "SCIM string array value")).toList();
    }

    /**
     * Requires one non-blank string.
     *
     * @param source source value
     * @param name   diagnostic name
     * @return text
     */
    static String text(final Object source, final String name) {
        if (!(source instanceof String value) || value.isBlank()) {
            throw new ValidateException(name + " must be a non-blank string");
        }
        return value;
    }

    /**
     * Reads one optional string.
     *
     * @param source source value
     * @return optional string
     */
    static String optional(final Object source) {
        return source == null ? null : text(source, "SCIM optional string");
    }

    /**
     * Parses a non-negative integral number.
     *
     * @param source source number
     * @param name   diagnostic name
     * @return integer
     */
    static int integer(final Object source, final String name) {
        if (!(source instanceof Number number)) {
            throw new ValidateException(name + " must be a number");
        }
        try {
            final int value = new java.math.BigDecimal(number.toString()).intValueExact();
            Assert.isTrue(value >= Normal._0, () -> new ValidateException(name + " must not be negative"));
            return value;
        } catch (final ArithmeticException failure) {
            throw new ValidateException(name + " must be an integer");
        }
    }

    /**
     * Parses one BulkRequest resource path.
     *
     * @param path   operation path
     * @param method operation method
     * @return parsed target
     * @throws ValidateException if the path does not identify a supported collection or resource
     */
    static BulkTarget bulkTarget(final String path, final Http.Method method) {
        Assert.isTrue(
                path.startsWith("/") && path.length() > Normal._1,
                () -> new ValidateException("SCIM bulk path is invalid"));
        final String relative = path.substring(Normal._1);
        for (final ResourceType type : ResourceType.values()) {
            if (relative.equals(type.path())) {
                Assert.isTrue(
                        method == Http.Method.POST,
                        () -> new ValidateException("SCIM bulk collection path requires POST"));
                return new BulkTarget(type, null);
            }
            if (relative.startsWith(type.path() + "/")) {
                final String identifier = relative.substring(type.path().length() + Normal._1);
                Assert.isTrue(
                        !identifier.isBlank() && identifier.indexOf('/') < Normal._0,
                        () -> new ValidateException("SCIM bulk identifier is invalid"));
                return new BulkTarget(type, identifier);
            }
        }
        throw new ValidateException("SCIM bulk path is invalid");
    }

    /**
     * Serializes a JSON-compatible value with sorted object keys.
     *
     * @param value source value
     * @return canonical JSON
     * @throws ValidateException if a value type is not JSON-compatible
     */
    static String canonicalValue(final Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof Boolean || value instanceof Number) {
            return value.toString();
        }
        if (value instanceof String string) {
            return quote(string);
        }
        if (value instanceof Iterable<?> iterable) {
            final ArrayList<String> values = new ArrayList<>();
            iterable.forEach(item -> values.add(canonicalValue(item)));
            return '[' + String.join(",", values) + ']';
        }
        if (value instanceof Map<?, ?> map) {
            final TreeMap<String, Object> sorted = new TreeMap<>();
            map.forEach((key, item) -> sorted.put(String.valueOf(key), item));
            final ArrayList<String> values = new ArrayList<>();
            sorted.forEach((key, item) -> values.add(quote(key) + ':' + canonicalValue(item)));
            return '{' + String.join(",", values) + '}';
        }
        throw new ValidateException("SCIM canonical value type is unsupported");
    }

    /**
     * Escapes one canonical JSON string.
     *
     * @param value source string
     * @return quoted JSON string
     */
    static String quote(final String value) {
        final StringBuilder result = new StringBuilder("\"");
        for (int index = Normal._0; index < value.length(); index++) {
            final char current = value.charAt(index);
            switch (current) {
                case '"' -> result.append("\\\"");
                case '\\' -> result.append("\\\\");
                case '\b' -> result.append("\\b");
                case '\f' -> result.append("\\f");
                case '\n' -> result.append("\\n");
                case '\r' -> result.append("\\r");
                case '\t' -> result.append("\\t");
                default -> {
                    if (current < 0x20) {
                        result.append(String.format(java.util.Locale.ROOT, "\\u%04x", (int) current));
                    } else {
                        result.append(current);
                    }
                }
            }
        }
        return result.append('"').toString();
    }

    /**
     * Decodes one User or Group resource.
     *
     * @param body JSON body
     * @param type expected resource type
     * @return immutable resource
     * @throws ValidateException if the body or resource model is invalid
     */
    public ScimResource resource(final byte[] body, final ResourceType type) {
        return resource(object(body), type);
    }

    /**
     * Decodes a PatchOp operation list.
     *
     * @param body JSON body
     * @return immutable operations
     * @throws ValidateException if the PatchOp body, schema, or operation is invalid
     */
    public List<Operation> patch(final byte[] body) {
        final Map<String, Object> source = object(body);
        requireSchema(source, SCIM.PATCH_SCHEMA);
        final List<?> operations = list(source.get("Operations"), "SCIM PatchOp Operations");
        final ArrayList<Operation> result = new ArrayList<>();
        for (final Object item : operations) {
            final Map<String, Object> operation = map(item, "SCIM PATCH operation");
            final String name = text(operation.get("op"), "SCIM PATCH operation name");
            final Type type;
            try {
                type = Type.valueOf(name.toUpperCase(java.util.Locale.ROOT));
            } catch (final IllegalArgumentException failure) {
                throw new ValidateException("SCIM PATCH operation name is invalid");
            }
            result.add(new Operation(type, optional(operation.get("path")), operation.get("value")));
        }
        return List.copyOf(result);
    }

    /**
     * Decodes one bounded BulkRequest.
     *
     * @param body JSON body
     * @return immutable bulk request
     * @throws ValidateException if the body is null, exceeds a bound, or contains an invalid operation
     */
    public ScimBulk.Request bulk(final byte[] body) {
        final byte[] sourceBody = Assert
                .notNull(body, () -> new ValidateException("SCIM bulk request body must not be null"));
        Assert.isTrue(
                sourceBody.length <= maximumBytes,
                () -> new ValidateException("SCIM bulk request exceeds its byte limit"));
        final Map<String, Object> source = object(sourceBody);
        requireSchema(source, SCIM.BULK_REQUEST_SCHEMA);
        final List<?> operations = list(source.get("Operations"), "SCIM bulk Operations");
        Assert.isTrue(
                operations.size() <= SCIM.MAXIMUM_BULK_OPERATIONS,
                () -> new ValidateException("SCIM bulk operation count exceeds its limit"));
        final ArrayList<Entry> entries = new ArrayList<>();
        for (final Object item : operations) {
            final Map<String, Object> operation = map(item, "SCIM bulk operation");
            final Http.Method method;
            try {
                method = Http.Method
                        .valueOf(text(operation.get("method"), "SCIM bulk method").toUpperCase(java.util.Locale.ROOT));
            } catch (final IllegalArgumentException failure) {
                throw new ValidateException("SCIM bulk method is invalid");
            }
            final String path = text(operation.get("path"), "SCIM bulk path");
            final BulkTarget target = bulkTarget(path, method);
            Object data = operation.get("data");
            if (method == Http.Method.POST || method == Http.Method.PUT) {
                data = resource(map(data, "SCIM bulk resource"), target.type());
            } else if (method == Http.Method.PATCH) {
                final byte[] encoded = json.write(data);
                data = patch(encoded);
            }
            entries.add(new Entry(method, target.type(), target.identifier(), optional(operation.get("bulkId")), data));
        }
        final int failOnErrors = source.get("failOnErrors") == null ? Normal._0
                : integer(source.get("failOnErrors"), "SCIM bulk failOnErrors");
        return new ScimBulk.Request(entries, failOnErrors, sourceBody.length);
    }

    /**
     * Emits one BulkResponse.
     *
     * @param response bulk response
     * @return JSON bytes
     */
    public byte[] write(final ScimBulk.Response response) {
        final List<Map<String, Object>> operations = response.operations().stream().map(this::bulkResult).toList();
        return json.write(Map.of("schemas", List.of(SCIM.BULK_RESPONSE_SCHEMA), "Operations", operations));
    }

    /**
     * Emits one resource body.
     *
     * @param resource resource
     * @return UTF-8 JSON bytes
     */
    public byte[] write(final ScimResource resource) {
        return json.write(resource(resource));
    }

    /**
     * Emits one ListResponse body.
     *
     * @param page       repository page
     * @param startIndex one-based page start
     * @return UTF-8 JSON bytes
     */
    public byte[] write(final Page page, final int startIndex) {
        final LinkedHashMap<String, Object> result = new LinkedHashMap<>();
        result.put("schemas", List.of(SCIM.LIST_RESPONSE_SCHEMA));
        result.put("totalResults", page.totalResults());
        result.put("startIndex", startIndex);
        result.put("itemsPerPage", page.resources().size());
        result.put("Resources", page.resources().stream().map(this::resource).toList());
        return json.write(result);
    }

    /**
     * Emits one stable SCIM error body.
     *
     * @param status HTTP status
     * @param error  protocol error
     * @return UTF-8 JSON bytes
     */
    public byte[] error(final int status, final ProtocolError error) {
        final LinkedHashMap<String, Object> result = new LinkedHashMap<>();
        result.put("schemas", List.of(SCIM.ERROR_SCHEMA));
        result.put("status", Integer.toString(status));
        result.put("scimType", error.getKey());
        result.put("detail", error.getValue());
        return json.write(result);
    }

    /**
     * Creates canonical JSON bytes for entity-tag generation.
     *
     * @param resource resource
     * @return deterministic UTF-8 JSON
     */
    public byte[] canonical(final ScimResource resource) {
        return canonicalValue(resource(resource)).getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Reads one strict JSON object.
     *
     * @param body body bytes
     * @return string-keyed object
     * @throws ValidateException if the body is null, malformed, excessive, or not an object
     */
    public Map<String, Object> object(final byte[] body) {
        final Object value = reader.read(body, Map.class);
        return map(value, "SCIM request body");
    }

    /**
     * Converts one resource to a JSON-compatible map.
     *
     * @param value resource
     * @return wire map
     */
    public Map<String, Object> resource(final ScimResource value) {
        final ScimResource source = Assert
                .notNull(value, () -> new ValidateException("SCIM resource must not be null"));
        final LinkedHashMap<String, Object> result = new LinkedHashMap<>();
        result.put("schemas", source.schemas());
        if (source.id() != null) {
            result.put("id", source.id());
        }
        if (source.externalId() != null) {
            result.put("externalId", source.externalId());
        }
        result.putAll(source.attributes());
        if (source.meta() != null) {
            result.put(
                    "meta",
                    Map.of(
                            "resourceType",
                            source.meta().resourceType().name(),
                            "created",
                            source.meta().created().toString(),
                            "lastModified",
                            source.meta().lastModified().toString(),
                            "location",
                            source.meta().location().toString(),
                            "version",
                            source.meta().version()));
        }
        return java.util.Collections.unmodifiableMap(result);
    }

    /**
     * Converts one mutation result to a BulkResponse operation.
     *
     * @param result mutation result
     * @return operation map
     */
    Map<String, Object> bulkResult(final MutationResult result) {
        final LinkedHashMap<String, Object> value = new LinkedHashMap<>();
        value.put("method", result.method().name());
        value.put("status", Integer.toString(result.status()));
        if (result.bulkId() != null) {
            value.put("bulkId", result.bulkId());
        }
        if (result.location() != null) {
            value.put("location", result.location().toString());
        }
        if (result.resource() != null) {
            value.put("response", resource(result.resource()));
        }
        return value;
    }

    /**
     * Parsed bulk path.
     *
     * @param type       resource type
     * @param identifier optional identifier
     * @author Kimi Liu
     */
    record BulkTarget(ResourceType type, String identifier) {

        /**
         * Validates the parsed target.
         *
         * @throws ValidateException if the resource type is {@code null} or the identifier is blank
         */
        BulkTarget {
            type = Assert.notNull(type, () -> new ValidateException("SCIM bulk resource type must not be null"));
            if (identifier != null && identifier.isBlank()) {
                throw new ValidateException("SCIM bulk resource identifier must not be blank");
            }
        }
    }

}
