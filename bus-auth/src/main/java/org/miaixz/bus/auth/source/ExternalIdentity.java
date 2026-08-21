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
package org.miaixz.bus.auth.source;

import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.auth.Evidence;
import org.miaixz.bus.core.lang.*;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Represents a verified identity in the namespace of one registered external Source.
 * <p>
 * The subject is the stable identifier asserted by that Source. Attributes and evidence are immutable verified or
 * safely derived values; raw tokens, protocol responses, Vendor payloads, and account-linking decisions are excluded.
 * The identity layer later maps this value to an internal Subject.
 * </p>
 *
 * @param sourceId   registered Source that established the identity
 * @param subject    stable subject identifier within that Source
 * @param attributes immutable provider-neutral identity attributes
 * @param evidence   immutable verified authentication evidence
 * @author Kimi Liu
 */
public record ExternalIdentity(String sourceId, String subject, JsonValue.ObjectValue attributes,
        List<Evidence> evidence) {

    /**
     * Creates an immutable verified external identity.
     *
     * @param sourceId   registered Source identifier
     * @param subject    stable Source-local subject identifier
     * @param attributes provider-neutral identity attributes
     * @param evidence   verified authentication evidence
     * @throws IllegalArgumentException if an identifier is blank, a container is {@code null}, or evidence contains a
     *                                  {@code null} entry
     */
    public ExternalIdentity {
        Assert.notBlank(sourceId, "External identity Source id must not be blank");
        Assert.notBlank(subject, "External identity subject must not be blank");
        Assert.notNull(attributes, "External identity attributes must not be null");
        attributes = new JsonValue.ObjectValue(attributes.values());
        Assert.notNull(evidence, "External identity evidence must not be null");
        final List<Evidence> copy = new ArrayList<>(evidence.size());
        for (Evidence item : evidence) {
            copy.add(Assert.notNull(item, "External identity evidence entry must not be null"));
        }
        evidence = List.copyOf(copy);
    }

}
