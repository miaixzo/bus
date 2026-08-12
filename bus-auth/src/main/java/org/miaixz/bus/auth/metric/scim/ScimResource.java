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
package org.miaixz.bus.auth.metric.scim;

import java.math.BigDecimal;
import java.net.URI;
import java.time.Instant;
import java.util.*;

import org.miaixz.bus.auth.metric.SCIM;
import org.miaixz.bus.auth.metric.SCIM.ResourceType;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Immutable binary-free SCIM User or Group resource. The core schema must match the declared resource type, all JSON
 * attributes are recursively snapshotted, object keys are non-blank, depth and collection counts are bounded, and
 * server metadata is either complete or absent for a create input.
 *
 * @param type       resource type
 * @param id         optional server identifier
 * @param externalId optional client identifier
 * @param schemas    immutable schema identifiers
 * @param attributes recursively immutable resource attributes excluding reserved envelope fields
 * @param meta       optional complete server metadata
 * @author Kimi Liu
 */
public record ScimResource(ResourceType type, String id, String externalId, Set<String> schemas,
        Map<String, Object> attributes, Meta meta) {

    /**
     * Maximum recursive JSON value depth.
     */
    private static final int MAXIMUM_DEPTH = Normal._16;

    /**
     * Maximum object properties or array elements at one level.
     */
    private static final int MAXIMUM_ITEMS = Normal._1024;

    /**
     * Reserved SCIM resource envelope fields.
     */
    private static final Set<String> RESERVED = Set.of("schemas", "id", "externalId", "meta");

    /**
     * Validates and snapshots one resource.
     *
     * @param type       resource type
     * @param id         server identifier
     * @param externalId external identifier
     * @param schemas    schema identifiers
     * @param attributes resource attributes
     * @param meta       server metadata
     */
    public ScimResource {
        type = Assert.notNull(type, () -> new ValidateException("SCIM resource type must not be null"));
        id = optional(id, "SCIM resource identifier");
        externalId = optional(externalId, "SCIM external identifier");
        schemas = Set.copyOf(Assert.notNull(schemas, () -> new ValidateException("SCIM schemas must not be null")));
        Assert.isTrue(
                schemas.size() <= Normal._128 && schemas.contains(type.schema())
                        && schemas.stream().noneMatch(String::isBlank),
                () -> new ValidateException("SCIM resource schemas are invalid"));
        attributes = object(attributes, Normal._1);
        Assert.isTrue(
                attributes.keySet().stream().noneMatch(RESERVED::contains),
                () -> new ValidateException("SCIM attributes contain a reserved envelope field"));
        Assert.isTrue(
                meta == null || id != null && meta.resourceType() == type,
                () -> new ValidateException("SCIM metadata requires a matching resource identifier"));
    }

    /**
     * Creates one User input without server metadata.
     *
     * @param externalId optional external identifier
     * @param attributes User attributes
     * @return immutable User input
     */
    public static ScimResource user(final String externalId, final Map<String, Object> attributes) {
        return new ScimResource(ResourceType.USER, null, externalId, Set.of(SCIM.USER_SCHEMA), attributes, null);
    }

    /**
     * Creates one Group input without server metadata.
     *
     * @param externalId optional external identifier
     * @param attributes Group attributes
     * @return immutable Group input
     */
    public static ScimResource group(final String externalId, final Map<String, Object> attributes) {
        return new ScimResource(ResourceType.GROUP, null, externalId, Set.of(SCIM.GROUP_SCHEMA), attributes, null);
    }

    /**
     * Copies and validates one JSON object.
     *
     * @param source source object
     * @param depth  current depth
     * @return immutable object
     */
    private static Map<String, Object> object(final Map<String, ?> source, final int depth) {
        final Map<String, ?> input = Assert
                .notNull(source, () -> new ValidateException("SCIM attribute object must not be null"));
        Assert.isTrue(
                depth <= MAXIMUM_DEPTH && input.size() <= MAXIMUM_ITEMS,
                () -> new ValidateException("SCIM attribute object exceeds its limit"));
        final LinkedHashMap<String, Object> result = new LinkedHashMap<>();
        input.forEach((key, value) -> {
            final String name = Assert
                    .notBlank(key, () -> new ValidateException("SCIM attribute name must not be blank"));
            Assert.isTrue(
                    result.putIfAbsent(name, copy(value, depth + Normal._1)) == null,
                    () -> new ValidateException("SCIM attribute is duplicated"));
        });
        return java.util.Collections.unmodifiableMap(result);
    }

    /**
     * Recursively copies one supported JSON value.
     *
     * @param source source value
     * @param depth  current depth
     * @return immutable value
     */
    private static Object copy(final Object source, final int depth) {
        if (source == null || source instanceof String || source instanceof Boolean || source instanceof BigDecimal) {
            return source;
        }
        if (source instanceof Number number) {
            return new BigDecimal(number.toString());
        }
        if (source instanceof Map<?, ?> map) {
            final LinkedHashMap<String, Object> values = new LinkedHashMap<>();
            map.forEach((key, value) -> {
                if (!(key instanceof String name)) {
                    throw new ValidateException("SCIM object key must be a string");
                }
                values.put(name, value);
            });
            return object(values, depth);
        }
        if (source instanceof List<?> list) {
            Assert.isTrue(
                    depth <= MAXIMUM_DEPTH && list.size() <= MAXIMUM_ITEMS,
                    () -> new ValidateException("SCIM attribute array exceeds its limit"));
            final ArrayList<Object> result = new ArrayList<>(list.size());
            list.forEach(value -> result.add(copy(value, depth + Normal._1)));
            return List.copyOf(result);
        }
        throw new ValidateException("SCIM attribute value type is unsupported");
    }

    /**
     * Validates one optional bounded text value.
     *
     * @param value source text
     * @param name  value name
     * @return validated text or {@code null}
     */
    private static String optional(final String value, final String name) {
        if (value == null) {
            return null;
        }
        Assert.isTrue(
                !value.isBlank() && value.length() <= Normal._8192,
                () -> new ValidateException(name + " is invalid"));
        return value;
    }

    /**
     * Returns a recursively independent immutable attribute snapshot.
     *
     * @return copied attributes
     */
    @Override
    public Map<String, Object> attributes() {
        return object(attributes, Normal._1);
    }

    /**
     * Returns one attribute by exact path segment.
     *
     * @param name exact attribute name
     * @return optional immutable attribute value
     */
    public Object attribute(final String name) {
        return copy(
                attributes.get(
                        Assert.notBlank(name, () -> new ValidateException("SCIM attribute name must not be blank"))),
                Normal._1);
    }

    /**
     * Returns a copy with replaced recursively immutable attributes.
     *
     * @param values replacement attributes
     * @return replacement resource
     */
    public ScimResource withAttributes(final Map<String, Object> values) {
        return new ScimResource(type, id, externalId, schemas, values, meta);
    }

    /**
     * Immutable complete server metadata.
     *
     * @param resourceType resource type
     * @param created      creation instant
     * @param lastModified last modification instant
     * @param location     absolute resource location
     * @param version      strong entity tag
     */
    public record Meta(ResourceType resourceType, Instant created, Instant lastModified, URI location, String version) {

        /**
         * Validates complete metadata.
         *
         * @param resourceType resource type
         * @param created      creation instant
         * @param lastModified modification instant
         * @param location     resource location
         * @param version      entity tag
         */
        public Meta {
            resourceType = Assert
                    .notNull(resourceType, () -> new ValidateException("SCIM metadata resource type must not be null"));
            created = Assert.notNull(created, () -> new ValidateException("SCIM creation time must not be null"));
            lastModified = Assert
                    .notNull(lastModified, () -> new ValidateException("SCIM modification time must not be null"));
            Assert.isTrue(
                    !lastModified.isBefore(created),
                    () -> new ValidateException("SCIM modification time precedes creation"));
            location = Assert.notNull(location, () -> new ValidateException("SCIM resource location must not be null"));
            Assert.isTrue(
                    location.isAbsolute() && location.getUserInfo() == null && location.getFragment() == null,
                    () -> new ValidateException("SCIM resource location is invalid"));
            version = Assert.notBlank(version, () -> new ValidateException("SCIM version must not be blank"));
        }
    }

}
