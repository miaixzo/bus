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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.miaixz.bus.auth.Credential;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Immutable protocol consumer metadata accepted by authentication services.
 */
public record ConsumerMetadata(String id, Optional<Credential.Reference> credential, List<String> redirectUris,
        Set<String> grantTypes, Set<String> responseTypes, Set<String> scopes, JsonValue.ObjectValue metadata) {

    /**
     * Creates and freezes validated consumer metadata.
     */
    public ConsumerMetadata {
        Assert.notBlank(id, "Consumer identifier must not be blank");
        Assert.notNull(credential, "Consumer credential container must not be null");
        credential = Optional.ofNullable(credential.getOrNull());
        redirectUris = immutableList(redirectUris, "Consumer redirect URI");
        grantTypes = immutableSet(grantTypes, "Consumer grant type");
        responseTypes = immutableSet(responseTypes, "Consumer response type");
        scopes = immutableSet(scopes, "Consumer scope");
        Assert.notNull(metadata, "Consumer metadata must not be null");
        metadata = new JsonValue.ObjectValue(metadata.values());
    }

    private static List<String> immutableList(final List<String> values, final String label) {
        Assert.notNull(values, label + " list must not be null");
        final List<String> copy = new ArrayList<>(values.size());
        final Set<String> unique = new HashSet<>(values.size());
        for (String value : values) {
            Assert.notBlank(value, label + " must not be blank");
            if (!unique.add(value)) {
                throw new ValidateException(label + " list must not contain duplicates");
            }
            copy.add(value);
        }
        return List.copyOf(copy);
    }

    private static Set<String> immutableSet(final Set<String> values, final String label) {
        Assert.notNull(values, label + " set must not be null");
        for (String value : values) {
            Assert.notBlank(value, label + " must not be blank");
        }
        return Set.copyOf(values);
    }
}
