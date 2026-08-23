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
package org.miaixz.bus.auth.shared.claim;

import java.time.Instant;
import java.util.*;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Retains an ordered implementation-neutral claim collection with exact JSON types, provenance, and sensitivity labels.
 *
 * @param claims ordered entries whose target claim names are unique
 * @author Kimi Liu
 */
public record ClaimSet(List<Entry> claims) {

    /**
     * Validates and freezes entries while enforcing exact target-name uniqueness.
     *
     * @throws IllegalArgumentException if the list or an entry is {@code null}
     * @throws ValidateException        if a target claim name is duplicated
     */
    public ClaimSet {
        Assert.notNull(claims, "ClaimSet entries must not be null");
        final List<Entry> copy = new ArrayList<>(claims.size());
        final Set<String> names = new HashSet<>(claims.size());
        for (Entry entry : claims) {
            final Entry checked = Assert.notNull(entry, "ClaimSet entry must not be null");
            if (!names.add(checked.name())) {
                throw new ValidateException("ClaimSet target claim names must be unique");
            }
            copy.add(checked);
        }
        claims = List.copyOf(copy);
    }

    /**
     * Finds one exact case-sensitive target claim.
     *
     * @param name target claim name
     * @return matching entry when present
     */
    public Optional<Entry> claim(final String name) {
        Assert.notBlank(name, "ClaimSet lookup name must not be blank");
        return Optional.of(claims.stream().filter(entry -> entry.name().equals(name)).findFirst());
    }

    /**
     * Projects entries to a implementation-neutral JSON object without changing value types or order.
     *
     * @return immutable target-name to exact JsonValue object
     */
    public JsonValue.ObjectValue values() {
        final Map<String, JsonValue> values = new LinkedHashMap<>(claims.size());
        claims.forEach(entry -> values.put(entry.name(), entry.value()));
        return new JsonValue.ObjectValue(values);
    }

    /**
     * Represents one mapped claim and its security-relevant provenance.
     *
     * @param name      exact target claim name
     * @param value     exact implementation-neutral JSON value
     * @param origin    source attribute or evidence provenance
     * @param sensitive whether callers must suppress the value from logs and general display
     * @author Kimi Liu
     */
    public record Entry(String name, JsonValue value, Origin origin, boolean sensitive) {

        /**
         * Validates one immutable mapped claim entry.
         *
         * @throws IllegalArgumentException if text is blank or another component is {@code null}
         */
        public Entry {
            Assert.notBlank(name, "Mapped claim target name must not be blank");
            Assert.notNull(value, "Mapped claim value must not be null");
            Assert.notNull(origin, "Mapped claim origin must not be null");
        }

    }

    /**
     * Identifies the exact source name and optional verified evidence provenance of a mapped claim.
     *
     * @param source     explicit source category selected by the mapping
     * @param sourceName exact source attribute or evidence claim name
     * @param issuer     optional verified evidence issuer or explicit mapping filter
     * @param verifiedAt optional evidence verification instant
     * @author Kimi Liu
     */
    public record Origin(ClaimMapping.Source source, String sourceName, Optional<String> issuer,
            Optional<Instant> verifiedAt) {

        /**
         * Validates and freezes provenance without inventing missing verification metadata.
         *
         * @throws IllegalArgumentException if a component is {@code null} or text is blank
         * @throws ValidateException        if a Subject attribute incorrectly declares evidence provenance
         */
        public Origin {
            Assert.notNull(source, "Mapped claim source category must not be null");
            Assert.notBlank(sourceName, "Mapped claim source name must not be blank");
            Assert.notNull(issuer, "Mapped claim issuer container must not be null");
            Assert.notNull(verifiedAt, "Mapped claim verification-time container must not be null");
            issuer = issuer.map(value -> Assert.notBlank(value, "Mapped claim issuer must not be blank"));
            verifiedAt = verifiedAt
                    .map(value -> Assert.notNull(value, "Mapped claim verification time must not be null"));
            if (source == ClaimMapping.Source.SUBJECT_ATTRIBUTE && (issuer.isPresent() || verifiedAt.isPresent())) {
                throw new ValidateException("Subject attribute claim origin must not claim evidence provenance");
            }
        }

    }

}
