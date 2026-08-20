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
package org.miaixz.bus.auth.codec;

import org.miaixz.bus.core.lang.Assert;

/**
 * Represents one decoded name/value pair in an ordered form or URI query sequence.
 * <p>
 * Empty names and values remain distinct from missing pairs, and duplicate names remain separate list entries. This
 * transport value does not interpret OAuth, OpenID Connect, SAML, SCIM, or Vendor parameter semantics.
 * </p>
 *
 * @param name  decoded parameter name, which may be empty
 * @param value decoded parameter value, which may be empty
 * @author Kimi Liu
 */
public record Parameter(String name, String value) {

    /**
     * Creates one decoded parameter without normalizing empty text.
     *
     * @param name  decoded parameter name
     * @param value decoded parameter value
     * @throws IllegalArgumentException if either component is {@code null}
     */
    public Parameter {
        Assert.notNull(name, "Codec parameter name must not be null");
        Assert.notNull(value, "Codec parameter value must not be null");
    }

}
