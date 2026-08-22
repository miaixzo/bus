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
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Seals the SCIM 2.0 resource representations implemented by this module.
 *
 * @author Kimi Liu
 */
public interface Resource {

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
     * Parses and requires an absolute URI without accepting invalid lexical input.
     *
     * @param value URI lexical value
     * @param label validation label
     * @return parsed absolute URI
     * @throws ValidateException if the value is not an absolute URI
     */
    private static URI parseAbsoluteUri(final String value, final String label) {
        try {
            final URI uri = URI.create(value);
            if (!uri.isAbsolute()) {
                throw new ValidateException(label + " must be absolute");
            }
            return uri;
        } catch (IllegalArgumentException exception) {
            throw new ValidateException(label + " must be a valid absolute URI", exception);
        }
    }

    /**
     * Tests the complete weak or strong HTTP entity-tag lexical shape required by SCIM version values.
     *
     * @param value candidate entity-tag
     * @return {@code true} when the value has a valid quoted HTTP entity-tag shape
     */
    private static boolean isEntityTag(final String value) {
        final int quote = value.startsWith("W/\"") ? 2 : value.startsWith("\"") ? 0 : -1;
        if (quote < 0 || value.length() <= quote + 1 || value.charAt(value.length() - 1) != Symbol.C_DOUBLE_QUOTES) {
            return false;
        }
        for (int index = quote + 1; index < value.length() - 1; index++) {
            final char character = value.charAt(index);
            if (character == Symbol.C_DOUBLE_QUOTES || character <= 0x20 || character == 0x7f) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the standard schema identifiers present in this resource representation.
     *
     * @return immutable schema URI list in wire order
     */
    List<String> schemas();

    /**
     * Returns service-provider-maintained resource metadata when assigned.
     *
     * @return immutable optional resource metadata
     */
    Optional<Meta> meta();

    /**
     * Carries the common RFC 7643 resource attributes and top-level schema extension objects.
     *
     * @param schemas    non-empty unique schema URI list in wire order
     * @param id         service-provider-issued resource identifier when assigned
     * @param externalId provisioning-client correlation identifier when present
     * @param meta       service-provider-maintained resource metadata when assigned
     * @param extensions extension objects keyed by their declared schema URIs
     * @author Kimi Liu
     */
    record Common(List<String> schemas, Optional<String> id, Optional<String> externalId, Optional<Meta> meta,
            JsonValue.ObjectValue extensions) {

        /**
         * Validates and defensively copies all common resource attributes.
         *
         * @throws IllegalArgumentException if a required value or container is {@code null}
         * @throws ValidateException        if a schema, optional text, or extension key violates RFC 7643
         */
        public Common {
            Assert.notNull(schemas, "SCIM resource schemas must not be null");
            if (schemas.isEmpty()) {
                throw new ValidateException("SCIM resource schemas must not be empty");
            }
            final Set<String> uniqueSchemas = new HashSet<>(schemas.size());
            for (String schema : schemas) {
                final String value = Assert.notBlank(schema, "SCIM resource schema URI must not be blank");
                final URI uri = parseAbsoluteUri(value, "SCIM resource schema");
                if (!"urn".equalsIgnoreCase(uri.getScheme())) {
                    throw new ValidateException("SCIM resource schema must be an absolute URN");
                }
                if (!uniqueSchemas.add(value)) {
                    throw new ValidateException("SCIM resource schemas must be unique");
                }
            }
            schemas = List.copyOf(schemas);
            id = optionalText(id, "SCIM resource id");
            externalId = optionalText(externalId, "SCIM resource externalId");
            Assert.notNull(meta, "SCIM resource meta container must not be null");
            meta = Optional.ofNullable(meta.getOrNull());
            final JsonValue.ObjectValue source = Assert
                    .notNull(extensions, "SCIM resource extensions must not be null");
            for (String extensionSchema : source.values().keySet()) {
                if (!uniqueSchemas.contains(extensionSchema) || Scim.USER_SCHEMA.equals(extensionSchema)
                        || Scim.GROUP_SCHEMA.equals(extensionSchema)) {
                    throw new ValidateException("SCIM extension key must identify a declared extension schema");
                }
                if (!(source.values().get(extensionSchema) instanceof JsonValue.ObjectValue)) {
                    throw new ValidateException("SCIM extension value must be a JSON object");
                }
            }
            extensions = new JsonValue.ObjectValue(source.values());
        }

    }

    /**
     * Carries service-provider-maintained RFC 7643 resource metadata.
     *
     * @param resourceType resource type name
     * @param created      resource creation time when known
     * @param lastModified most recent modification time when known
     * @param version      complete HTTP entity-tag lexical value when versioning is enabled
     * @param location     absolute resource URI when assigned
     * @author Kimi Liu
     */
    record Meta(String resourceType, Optional<Instant> created, Optional<Instant> lastModified,
            Optional<String> version, Optional<String> location) {

        /**
         * Validates the resource type, timestamps, entity-tag, and location.
         *
         * @throws IllegalArgumentException if a required value or container is {@code null}
         * @throws ValidateException        if text, temporal order, entity-tag, or location is invalid
         */
        public Meta {
            resourceType = Assert.notBlank(resourceType, "SCIM meta resourceType must not be blank");
            Assert.notNull(created, "SCIM meta created container must not be null");
            created = Optional.ofNullable(created.getOrNull());
            Assert.notNull(lastModified, "SCIM meta lastModified container must not be null");
            lastModified = Optional.ofNullable(lastModified.getOrNull());
            if (!created.isEmpty() && !lastModified.isEmpty()
                    && lastModified.getOrThrow().isBefore(created.getOrThrow())) {
                throw new ValidateException("SCIM meta lastModified must not precede created");
            }
            version = optionalText(version, "SCIM meta version");
            if (!version.isEmpty() && !isEntityTag(version.getOrThrow())) {
                throw new ValidateException("SCIM meta version must be a complete HTTP entity-tag");
            }
            location = optionalText(location, "SCIM meta location");
            if (!location.isEmpty()) {
                parseAbsoluteUri(location.getOrThrow(), "SCIM meta location");
            }
        }

    }

    /**
     * Identifies an existing SCIM resource and its optional conditional version.
     *
     * @param resourceType exact resource type name
     * @param id           service-provider-issued resource identifier
     * @param version      complete If-Match entity-tag when conditional mutation is requested
     * @author Kimi Liu
     */
    record Reference(String resourceType, String id, Optional<String> version) {

        /**
         * Validates the resource identity and optional conditional entity-tag.
         *
         * @throws IllegalArgumentException if a required value or container is {@code null}
         * @throws ValidateException        if the entity-tag lexical value is invalid
         */
        public Reference {
            resourceType = Assert.notBlank(resourceType, "SCIM reference resourceType must not be blank");
            id = Assert.notBlank(id, "SCIM reference id must not be blank");
            version = optionalText(version, "SCIM reference version");
            if (!version.isEmpty() && !isEntityTag(version.getOrThrow())) {
                throw new ValidateException("SCIM reference version must be a complete If-Match entity-tag");
            }
        }

    }

}
