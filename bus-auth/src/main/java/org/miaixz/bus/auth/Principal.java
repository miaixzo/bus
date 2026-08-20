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
package org.miaixz.bus.auth;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Represents the immutable current authenticated-principal view produced by the identity flow.
 * <p>
 * A principal is derived from a stable {@link Subject} and verified claims. It does not replace the stored subject,
 * carry {@link Session} state, define roles or authorities, or perform external authorization decisions; those
 * application concerns remain outside bus-auth.
 * </p>
 *
 * @param name   stable display or login name selected by the identity service
 * @param claims immutable provider-neutral verified claims
 * @author Kimi Liu
 */
public record Principal(String name, JsonValue.ObjectValue claims) {

    /**
     * Creates a detached immutable authenticated-principal view.
     *
     * @param name   non-blank principal name
     * @param claims provider-neutral verified claims
     * @throws IllegalArgumentException if the name is blank or claims are {@code null}
     */
    public Principal {
        Assert.notBlank(name, "Principal name must not be blank");
        Assert.notNull(claims, "Principal claims must not be null");
        claims = new JsonValue.ObjectValue(claims.values());
    }

}
