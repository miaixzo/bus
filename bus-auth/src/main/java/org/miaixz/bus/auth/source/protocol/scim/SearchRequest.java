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
package org.miaixz.bus.auth.source.protocol.scim;

import java.util.List;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Represents exactly the RFC 7644 POST {@code /.search} JSON request body.
 *
 * @param schemas    singleton standard SearchRequest schema URI
 * @param parameters common standard search parameters
 * @author Kimi Liu
 */
public record SearchRequest(List<String> schemas, SearchParameters parameters) {

    /**
     * Validates and freezes the standard POST search body.
     *
     * @throws IllegalArgumentException if a component is {@code null}
     * @throws ValidateException        if schemas is not the singleton SearchRequest schema
     */
    public SearchRequest {
        Assert.notNull(schemas, "SCIM SearchRequest schemas must not be null");
        schemas = List.copyOf(schemas);
        if (!schemas.equals(List.of(Scim.SEARCH_REQUEST_SCHEMA))) {
            throw new ValidateException("SCIM SearchRequest must use only the standard SearchRequest schema URI");
        }
        parameters = Assert.notNull(parameters, "SCIM SearchRequest parameters must not be null");
    }

}
