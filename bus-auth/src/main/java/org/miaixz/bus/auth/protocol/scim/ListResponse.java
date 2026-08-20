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
 * Models the RFC 7644 list or search response including its standard pagination metadata.
 *
 * @param schemas      singleton standard ListResponse schema URI
 * @param totalResults total number of results matching the operation before pagination
 * @param startIndex   one-based index of the first returned result when supplied
 * @param itemsPerPage number of resources returned on this page when supplied
 * @param resources    ordered resources serialized under the case-sensitive {@code Resources} wire member
 * @author Kimi Liu
 */
public record ListResponse(List<String> schemas, long totalResults, Optional<Integer> startIndex,
        Optional<Integer> itemsPerPage, List<Resource> resources) {

    /**
     * Enforces the ListResponse schema and RFC 7644 pagination invariants.
     *
     * @throws IllegalArgumentException if a collection, optional container, or resource is {@code null}
     * @throws ValidateException        if a count, index, schema, or page cardinality is invalid
     */
    public ListResponse {
        Assert.notNull(schemas, "SCIM ListResponse schemas must not be null");
        schemas = List.copyOf(schemas);
        if (!schemas.equals(List.of(Scim.LIST_RESPONSE_SCHEMA))) {
            throw new ValidateException("SCIM ListResponse schemas must contain only the standard schema URI");
        }
        if (totalResults < 0) {
            throw new ValidateException("SCIM ListResponse totalResults must not be negative");
        }
        Assert.notNull(startIndex, "SCIM ListResponse startIndex container must not be null");
        startIndex = Optional.ofNullable(startIndex.getOrNull());
        if (!startIndex.isEmpty() && startIndex.getOrThrow() < 1) {
            throw new ValidateException("SCIM ListResponse startIndex must be at least one");
        }
        Assert.notNull(itemsPerPage, "SCIM ListResponse itemsPerPage container must not be null");
        itemsPerPage = Optional.ofNullable(itemsPerPage.getOrNull());
        if (!itemsPerPage.isEmpty() && itemsPerPage.getOrThrow() < 0) {
            throw new ValidateException("SCIM ListResponse itemsPerPage must not be negative");
        }
        Assert.notNull(resources, "SCIM ListResponse Resources must not be null");
        for (Resource resource : resources) {
            Assert.notNull(resource, "SCIM ListResponse Resource must not be null");
        }
        resources = List.copyOf(resources);
        if (resources.size() > totalResults) {
            throw new ValidateException("SCIM ListResponse Resources must not exceed totalResults");
        }
        if (!itemsPerPage.isEmpty() && resources.size() != itemsPerPage.getOrThrow()) {
            throw new ValidateException("SCIM ListResponse itemsPerPage must equal the Resources count");
        }
    }

}
