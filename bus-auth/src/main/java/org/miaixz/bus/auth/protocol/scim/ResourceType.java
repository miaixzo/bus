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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Models an RFC 7643 ResourceType discovery resource.
 *
 * @param schemas          singleton standard ResourceType schema URI
 * @param id               resource type identifier when supplied
 * @param name             resource type name
 * @param description      human-readable resource type description when supplied
 * @param endpoint         relative SCIM resource collection endpoint
 * @param schema           absolute core schema URI for the resource type
 * @param schemaExtensions supported extension schemas and required flags
 * @param meta             service-provider-maintained discovery resource metadata
 * @author Kimi Liu
 */
public record ResourceType(List<String> schemas, Optional<String> id, String name, Optional<String> description,
        String endpoint, String schema, List<SchemaExtension> schemaExtensions, Optional<Resource.Meta> meta)
        implements Resource {

    /**
     * Enforces the discovery schema, endpoint shape, schema URIs, and extension uniqueness.
     *
     * @throws IllegalArgumentException if a required value, container, or extension is {@code null}
     * @throws ValidateException        if a schema, endpoint, optional text, or duplicate extension is invalid
     */
    public ResourceType {
        Assert.notNull(schemas, "SCIM ResourceType schemas must not be null");
        schemas = List.copyOf(schemas);
        if (!schemas.equals(List.of(Scim.RESOURCE_TYPE_SCHEMA))) {
            throw new ValidateException("SCIM ResourceType must use only the standard ResourceType schema URI");
        }
        id = optionalText(id, "SCIM ResourceType id");
        name = Assert.notBlank(name, "SCIM ResourceType name must not be blank");
        description = optionalText(description, "SCIM ResourceType description");
        endpoint = relativeEndpoint(endpoint);
        schema = absoluteUri(schema, "SCIM ResourceType schema");
        Assert.notNull(schemaExtensions, "SCIM ResourceType schemaExtensions must not be null");
        final Set<String> extensionUris = new HashSet<>(schemaExtensions.size());
        for (SchemaExtension extension : schemaExtensions) {
            final SchemaExtension item = Assert
                    .notNull(extension, "SCIM ResourceType schemaExtension must not be null");
            if (schema.equals(item.schema()) || !extensionUris.add(item.schema())) {
                throw new ValidateException(
                        "SCIM ResourceType extension schemas must be unique and differ from schema");
            }
        }
        schemaExtensions = List.copyOf(schemaExtensions);
        Assert.notNull(meta, "SCIM ResourceType meta container must not be null");
        meta = Optional.ofNullable(meta.getOrNull());
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
     * Parses and requires one absolute schema URI.
     *
     * @param value URI lexical value
     * @param label validation label
     * @return unchanged valid URI text
     */
    private static String absoluteUri(final String value, final String label) {
        final String text = Assert.notBlank(value, label + " must not be blank");
        try {
            if (!URI.create(text).isAbsolute()) {
                throw new ValidateException(label + " must be absolute");
            }
            return text;
        } catch (IllegalArgumentException exception) {
            throw new ValidateException(label + " must be a valid absolute URI", exception);
        }
    }

    /**
     * Validates an RFC 7643 relative resource collection endpoint.
     *
     * @param value endpoint lexical value
     * @return unchanged valid endpoint
     */
    private static String relativeEndpoint(final String value) {
        final String endpoint = Assert.notBlank(value, "SCIM ResourceType endpoint must not be blank");
        try {
            final URI uri = URI.create(endpoint);
            if (!endpoint.startsWith(Symbol.SLASH) || endpoint.startsWith("//") || uri.isAbsolute()
                    || uri.getAuthority() != null || uri.getQuery() != null || uri.getFragment() != null) {
                throw new ValidateException("SCIM ResourceType endpoint must be a relative absolute-path");
            }
            return endpoint;
        } catch (IllegalArgumentException exception) {
            throw new ValidateException("SCIM ResourceType endpoint is invalid", exception);
        }
    }

    /**
     * Declares one schema extension supported by a resource type.
     *
     * @param schema   absolute extension schema URI
     * @param required whether every resource of this type must include the extension
     * @author Kimi Liu
     */
    public record SchemaExtension(String schema, boolean required) {

        /**
         * Requires an absolute extension schema URI.
         *
         * @throws IllegalArgumentException if {@code schema} is {@code null}
         * @throws ValidateException        if the schema is blank or not an absolute URI
         */
        public SchemaExtension {
            schema = absoluteUri(schema, "SCIM ResourceType extension schema");
        }

    }

}
