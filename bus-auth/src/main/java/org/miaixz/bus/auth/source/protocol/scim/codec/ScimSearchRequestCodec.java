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

import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.auth.FabricX.Body;
import org.miaixz.bus.auth.FabricX.Url;
import org.miaixz.bus.auth.source.protocol.scim.*;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.extra.json.JsonRecordVerifier;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Decodes RFC 7644 GET query parameters and POST SearchRequest bodies into their distinct typed protocol models.
 *
 * @author Kimi Liu
 */
public class ScimSearchRequestCodec {

    /**
     * Verifies the exact POST SearchRequest body vocabulary through a structural record.
     */
    private static final JsonRecordVerifier<SearchDocument> SEARCH_VERIFIER = JsonRecordVerifier
            .of(SearchDocument.class);

    /**
     * Maximum accepted POST body or UTF-8 filter bytes.
     */
    private final long maximumBytes;

    /**
     * Maximum accepted JSON or filter-expression nesting depth.
     */
    private final int maximumDepth;

    /**
     * Creates a search codec with explicit provider and safety limits.
     *
     * @param maximumBytes positive body and filter limit
     * @param maximumDepth positive JSON and filter depth limit
     * @throws ValidateException if a limit is not positive
     */
    public ScimSearchRequestCodec(final long maximumBytes, final int maximumDepth) {
        if (maximumBytes <= 0 || maximumDepth <= 0) {
            throw new ValidateException("SCIM search limits must be positive");
        }
        this.maximumBytes = maximumBytes;
        this.maximumDepth = maximumDepth;
    }

    /**
     * Tests one exact standard GET query member name.
     *
     * @param name candidate member name
     * @return whether the member belongs to the RFC 7644 search query
     */
    private static boolean queryMember(final String name) {
        return switch (name) {
            case Scim.Attributes.FILTER, Scim.Attributes.ATTRIBUTES, Scim.Attributes.EXCLUDED_ATTRIBUTES, Scim.Attributes.SORT_BY, Scim.Attributes.SORT_ORDER, Scim.Attributes.START_INDEX, Scim.Attributes.COUNT -> true;
            default -> false;
        };
    }

    /**
     * Splits a comma-separated GET attribute path parameter.
     *
     * @param value decoded query value
     * @param name  parameter name
     * @return immutable path list
     */
    private static List<String> paths(final String value, final String name) {
        if (value == null) {
            return List.of();
        }
        final String[] parts = value.split(Symbol.COMMA, -1);
        final List<String> paths = new ArrayList<>(parts.length);
        for (String part : parts) {
            if (part.isBlank()) {
                throw new ValidateException("SCIM " + name + " contains an empty attribute path");
            }
            paths.add(part);
        }
        return List.copyOf(paths);
    }

    /**
     * Reads one optional POST attribute-path array.
     *
     * @param value parsed JSON value
     * @param name  member name
     * @return immutable path list
     */
    private static List<String> paths(final JsonValue value, final String name) {
        return value == null ? List.of() : ScimResourceCodec.strings(value, name);
    }

    /**
     * Resolves an optional standard sort direction.
     *
     * @param value exact wire value
     * @return typed direction or {@code null}
     */
    private static SearchParameters.SortOrder sortOrder(final String value) {
        if (value == null) {
            return null;
        }
        return switch (value) {
            case "ascending" -> SearchParameters.SortOrder.ASCENDING;
            case "descending" -> SearchParameters.SortOrder.DESCENDING;
            default -> throw new ValidateException("SCIM sortOrder must be ascending or descending");
        };
    }

    /**
     * Reads one optional query integer without signs or coercion.
     *
     * @param value   decoded query value
     * @param name    parameter name
     * @param minimum inclusive minimum
     * @return parsed integer or {@code null}
     */
    private static Integer optionalInteger(final String value, final String name, final int minimum) {
        if (value == null) {
            return null;
        }
        if (value.isEmpty() || !value.chars().allMatch(Character::isDigit)) {
            throw new ValidateException("SCIM " + name + " must be a decimal integer");
        }
        try {
            final int parsed = Integer.parseInt(value);
            if (parsed < minimum) {
                throw new ValidateException("SCIM " + name + " is below its permitted minimum");
            }
            return parsed;
        } catch (NumberFormatException failure) {
            throw new ValidateException("SCIM " + name + " exceeds the supported integer range", failure);
        }
    }

    /**
     * Reads one optional exact JSON integer.
     *
     * @param value   parsed JSON value
     * @param name    member name
     * @param minimum inclusive minimum
     * @return parsed integer or {@code null}
     */
    private static Integer optionalInteger(final JsonValue value, final String name, final int minimum) {
        if (value == null) {
            return null;
        }
        if (!(value instanceof JsonValue.NumberValue number) || number.value().stripTrailingZeros().scale() > 0) {
            throw new ValidateException("SCIM " + name + " must be an integral JSON number");
        }
        try {
            final int parsed = number.value().intValueExact();
            if (parsed < minimum) {
                throw new ValidateException("SCIM " + name + " is below its permitted minimum");
            }
            return parsed;
        } catch (ArithmeticException failure) {
            throw new ValidateException("SCIM " + name + " exceeds the supported integer range", failure);
        }
    }

    /**
     * Decodes one GET collection query while keeping the route-derived target external to query parsing.
     *
     * @param url    request URL containing only standard search query parameters
     * @param target route-resolved collection target
     * @return typed GET search query
     */
    public SearchQuery decodeQuery(final Url url, final ResourceTarget target) {
        final Url source = Assert.notNull(url, "SCIM search URL must not be null");
        if (source.fragment() != null) {
            throw new ValidateException("SCIM GET search target must not contain a fragment");
        }
        for (String name : source.queryParameterNames()) {
            if (!queryMember(name) || source.queryParameterValues(name).size() != 1) {
                throw new ValidateException("SCIM GET search contains an unknown or repeated query parameter");
            }
        }
        final SearchParameters parameters = parameters(
                source.queryParameter(Scim.Attributes.FILTER),
                paths(source.queryParameter(Scim.Attributes.ATTRIBUTES), Scim.Attributes.ATTRIBUTES),
                paths(source.queryParameter(Scim.Attributes.EXCLUDED_ATTRIBUTES), Scim.Attributes.EXCLUDED_ATTRIBUTES),
                source.queryParameter(Scim.Attributes.SORT_BY),
                source.queryParameter(Scim.Attributes.SORT_ORDER),
                optionalInteger(source.queryParameter(Scim.Attributes.START_INDEX), Scim.Attributes.START_INDEX, 1),
                optionalInteger(source.queryParameter(Scim.Attributes.COUNT), Scim.Attributes.COUNT, 0));
        return new SearchQuery(target, parameters);
    }

    /**
     * Decodes one POST SearchRequest JSON body without reading route or query state.
     *
     * @param body owned SCIM JSON body closed by this method
     * @return typed POST SearchRequest
     */
    public SearchRequest decodeBody(final Body body) {
        final Body encoded = Assert.notNull(body, "SCIM SearchRequest body must not be null");
        try (encoded) {
            final JsonValue.ObjectValue object = ScimResourceCodec.object(encoded, maximumBytes, maximumDepth);
            SEARCH_VERIFIER.validate(object);
            final List<String> schemas = ScimResourceCodec.strings(
                    ScimResourceCodec.required(object.values(), Scim.Attributes.SCHEMAS),
                    Scim.Attributes.SCHEMAS);
            final SearchParameters parameters = parameters(
                    ScimResourceCodec.optionalString(object.values(), Scim.Attributes.FILTER),
                    paths(object.values().get(Scim.Attributes.ATTRIBUTES), Scim.Attributes.ATTRIBUTES),
                    paths(
                            object.values().get(Scim.Attributes.EXCLUDED_ATTRIBUTES),
                            Scim.Attributes.EXCLUDED_ATTRIBUTES),
                    ScimResourceCodec.optionalString(object.values(), Scim.Attributes.SORT_BY),
                    ScimResourceCodec.optionalString(object.values(), Scim.Attributes.SORT_ORDER),
                    optionalInteger(object.values().get(Scim.Attributes.START_INDEX), Scim.Attributes.START_INDEX, 1),
                    optionalInteger(object.values().get(Scim.Attributes.COUNT), Scim.Attributes.COUNT, 0));
            return new SearchRequest(schemas, parameters);
        }
    }

    /**
     * Constructs parameters shared by GET and POST representations.
     *
     * @param filterText         optional filter expression
     * @param attributes         requested attributes
     * @param excludedAttributes excluded attributes
     * @param sortBy             optional sort path
     * @param sortOrder          optional direction
     * @param startIndex         optional one-based start index
     * @param count              optional requested count
     * @return validated common parameters
     */
    private SearchParameters parameters(
            final String filterText,
            final List<String> attributes,
            final List<String> excludedAttributes,
            final String sortBy,
            final String sortOrder,
            final Integer startIndex,
            final Integer count) {
        return new SearchParameters(attributes, excludedAttributes, Optional.ofNullable(filter(filterText)),
                Optional.ofNullable(sortBy), Optional.ofNullable(sortOrder(sortOrder)), Optional.ofNullable(startIndex),
                Optional.ofNullable(count));
    }

    /**
     * Parses one bounded RFC 7644 filter expression.
     *
     * @param value decoded filter text
     * @return parsed filter or {@code null}
     */
    private Filter filter(final String value) {
        if (value == null) {
            return null;
        }
        if (value.isBlank() || value.getBytes(Charset.UTF_8).length > maximumBytes) {
            throw new ValidateException("SCIM filter is blank or exceeds the configured size limit");
        }
        final int maximumLength = maximumBytes > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) maximumBytes;
        return new ScimFilterParser(value, maximumLength, maximumDepth).parse();
    }

    /**
     * Owns the exact POST SearchRequest JSON vocabulary.
     *
     * @param schemas            required SearchRequest schema
     * @param filter             optional filter
     * @param attributes         optional requested attributes
     * @param excludedAttributes optional excluded attributes
     * @param sortBy             optional sort path
     * @param sortOrder          optional sort direction
     * @param startIndex         optional one-based start index
     * @param count              optional result count
     */
    private record SearchDocument(JsonValue schemas, Optional<JsonValue> filter, Optional<JsonValue> attributes,
            Optional<JsonValue> excludedAttributes, Optional<JsonValue> sortBy, Optional<JsonValue> sortOrder,
            Optional<JsonValue> startIndex, Optional<JsonValue> count) {

    }

}
