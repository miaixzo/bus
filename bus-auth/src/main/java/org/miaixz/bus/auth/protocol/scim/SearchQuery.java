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

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Represents a GET collection search with parameters carried only by the HTTP query.
 *
 * @param target     exact resource collection target
 * @param parameters common standard search parameters
 * @author Kimi Liu
 */
public record SearchQuery(ResourceTarget target, SearchParameters parameters) {

    /**
     * Requires a collection target and non-null parameter object.
     *
     * @throws IllegalArgumentException if a component is {@code null}
     * @throws ValidateException        if the target identifies one resource
     */
    public SearchQuery {
        target = Assert.notNull(target, "SCIM GET search target must not be null");
        if (target.resourceId().isPresent()) {
            throw new ValidateException("SCIM GET search target must identify a resource collection");
        }
        parameters = Assert.notNull(parameters, "SCIM GET search parameters must not be null");
    }

}
