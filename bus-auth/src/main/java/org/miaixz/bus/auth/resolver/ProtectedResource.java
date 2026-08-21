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
package org.miaixz.bus.auth.resolver;

import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Parsed immutable protected-resource metadata.
 */
public record ProtectedResource(String id, List<String> audience, List<String> scopes,
        JsonValue.ObjectValue attributes) {

    public ProtectedResource {
        Assert.notBlank(id, "Protected-resource identifier must not be blank");
        audience = immutable(audience, "Protected-resource audience");
        scopes = immutable(scopes, "Protected-resource scope");
        Assert.notNull(attributes, "Protected-resource attributes must not be null");
        attributes = new JsonValue.ObjectValue(attributes.values());
    }

    private static List<String> immutable(final List<String> values, final String label) {
        Assert.notNull(values, label + " list must not be null");
        final List<String> copy = new ArrayList<>(values.size());
        for (String value : values) {
            copy.add(Assert.notBlank(value, label + " must not be blank"));
        }
        return List.copyOf(copy);
    }
}
