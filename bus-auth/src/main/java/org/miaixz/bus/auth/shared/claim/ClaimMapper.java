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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.miaixz.bus.auth.Evidence;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Applies ordered explicit ClaimMapping rules to verified attributes and evidence without scripts or type coercion.
 *
 * @author Kimi Liu
 */
public final class ClaimMapper {

    /**
     * Creates a stateless deterministic claim mapper.
     */
    public ClaimMapper() {
        // No initialization required.
    }

    /**
     * Resolves one mapping from attributes, evidence, or its exact explicit default.
     *
     * @param request complete mapping input
     * @param mapping current explicit rule
     * @return resolved entry, absence, or ambiguity
     */
    private static Resolution resolve(final Request request, final ClaimMapping mapping) {
        if (mapping.source() == ClaimMapping.Source.SUBJECT_ATTRIBUTE) {
            final JsonValue value = request.attributes().values().get(mapping.sourceName());
            if (value != null) {
                return Resolution.present(entry(mapping, value, Optional.empty(), Optional.empty()));
            }
            return fallback(mapping);
        }
        final List<Evidence.Claim> candidates = request.evidence().stream().map(Evidence::claim)
                .filter(claim -> claim.name().equals(mapping.sourceName()))
                .filter(claim -> mapping.issuer().map(value -> value.equals(claim.issuer())).orElse(true)).toList();
        if (candidates.isEmpty()) {
            return fallback(mapping);
        }
        final JsonValue first = candidates.get(0).value();
        if (candidates.stream().skip(1).anyMatch(claim -> !first.equals(claim.value()))) {
            return Resolution.ambiguousResult();
        }
        final Evidence.Claim selected = candidates.get(0);
        return Resolution.present(
                entry(mapping, selected.value(), Optional.of(selected.issuer()), Optional.of(selected.verifiedAt())));
    }

    /**
     * Uses an explicit exact JsonValue default when present, otherwise reports absence.
     *
     * @param mapping current explicit rule
     * @return resolved default or absent resolution
     */
    private static Resolution fallback(final ClaimMapping mapping) {
        return mapping.defaultValue()
                .map(value -> Resolution.present(entry(mapping, value, mapping.issuer(), Optional.empty())))
                .orElseGet(Resolution::absent);
    }

    /**
     * Creates one mapped entry with exact value and provenance metadata.
     *
     * @param mapping    source and target rule
     * @param value      exact source or default JsonValue
     * @param issuer     optional verified or configured issuer
     * @param verifiedAt optional evidence verification time
     * @return immutable mapped claim entry
     */
    private static ClaimSet.Entry entry(
            final ClaimMapping mapping,
            final JsonValue value,
            final Optional<String> issuer,
            final Optional<java.time.Instant> verifiedAt) {
        final ClaimSet.Origin origin = new ClaimSet.Origin(mapping.source(), mapping.sourceName(), issuer, verifiedAt);
        return new ClaimSet.Entry(mapping.targetName(), value, origin, mapping.sensitive());
    }

    /**
     * Maps all rules once and returns successful entries alongside deterministic missing or conflict issues.
     *
     * @param request immutable attributes, evidence, and ordered mapping rules
     * @return mapped ClaimSet and non-sensitive issues
     */
    public Result map(final Request request) {
        Assert.notNull(request, "Claim mapping request must not be null");
        final List<ClaimSet.Entry> entries = new ArrayList<>();
        final List<Issue> issues = new ArrayList<>();
        final Set<String> targets = new HashSet<>();
        for (ClaimMapping mapping : request.mappings()) {
            if (!targets.add(mapping.targetName())) {
                issues.add(
                        new Issue(Kind.DUPLICATE_TARGET, mapping.targetName(),
                                "Multiple mappings declare the same target claim"));
                continue;
            }
            final Resolution resolution = resolve(request, mapping);
            if (resolution.ambiguous()) {
                issues.add(
                        new Issue(Kind.AMBIGUOUS, mapping.targetName(),
                                "Multiple distinct verified evidence values match the mapping"));
            } else if (resolution.entry().isPresent()) {
                entries.add(resolution.entry().getOrThrow());
            } else if (mapping.required()) {
                issues.add(
                        new Issue(Kind.MISSING, mapping.targetName(), "A required mapped claim source is unavailable"));
            }
        }
        return new Result(new ClaimSet(entries), issues);
    }

    /**
     * Enumerates deterministic mapping issue categories.
     *
     * @author Kimi Liu
     */
    public enum Kind {
        /**
         * A required source and explicit default are absent.
         */
        MISSING,
        /**
         * Multiple distinct verified evidence values match one rule.
         */
        AMBIGUOUS,
        /**
         * More than one rule declares the same target claim.
         */
        DUPLICATE_TARGET

    }

    /**
     * Carries immutable input values for one deterministic mapping pass.
     *
     * @param attributes complete verified Subject attribute object
     * @param evidence   verified authentication evidence
     * @param mappings   ordered explicit mapping rules
     * @author Kimi Liu
     */
    public record Request(JsonValue.ObjectValue attributes, List<Evidence> evidence, List<ClaimMapping> mappings) {

        /**
         * Detaches and freezes input collections and provider-neutral JSON values.
         *
         * @throws IllegalArgumentException if a component or collection entry is {@code null}
         */
        public Request {
            Assert.notNull(attributes, "Claim mapping attributes must not be null");
            attributes = new JsonValue.ObjectValue(attributes.values());
            evidence = immutable(evidence, "Claim mapping evidence entry");
            mappings = immutable(mappings, "Claim mapping rule");
        }

        /**
         * Copies one ordered non-null list.
         *
         * @param values source values
         * @param label  entry diagnostic label
         * @param <T>    entry type
         * @return immutable ordered list
         */
        private static <T> List<T> immutable(final List<T> values, final String label) {
            Assert.notNull(values, label + " list must not be null");
            final List<T> copy = new ArrayList<>(values.size());
            for (T value : values) {
                copy.add(Assert.notNull(value, label + " must not be null"));
            }
            return List.copyOf(copy);
        }

    }

    /**
     * Returns mapped entries and every non-sensitive deterministic mapping issue.
     *
     * @param claims successfully mapped unique claims
     * @param issues missing or conflict issues
     * @author Kimi Liu
     */
    public record Result(ClaimSet claims, List<Issue> issues) {

        /**
         * Validates and freezes result components.
         */
        public Result {
            Assert.notNull(claims, "Claim mapping result must not be null");
            issues = Request.immutable(issues, "Claim mapping issue");
        }

    }

    /**
     * Describes one safe mapping failure without copying source values into diagnostics.
     *
     * @param kind            deterministic issue category
     * @param targetName      affected target claim name
     * @param safeDescription non-sensitive description
     * @author Kimi Liu
     */
    public record Issue(Kind kind, String targetName, String safeDescription) {

        /**
         * Validates safe issue fields.
         */
        public Issue {
            Assert.notNull(kind, "Claim mapping issue kind must not be null");
            Assert.notBlank(targetName, "Claim mapping issue target must not be blank");
            Assert.notBlank(safeDescription, "Claim mapping issue description must not be blank");
        }

    }

    /**
     * Represents internal single-rule resolution state without exposing source values in issues.
     *
     * @param entry     resolved entry when present
     * @param ambiguous whether distinct evidence values conflicted
     */
    private record Resolution(Optional<ClaimSet.Entry> entry, boolean ambiguous) {

        /**
         * Creates a successful resolution containing one mapped entry.
         *
         * @param entry mapped entry
         * @return present resolution
         */
        private static Resolution present(final ClaimSet.Entry entry) {
            return new Resolution(Optional.of(entry), false);
        }

        /**
         * Creates a resolution representing an unavailable source and no default.
         *
         * @return absent resolution
         */
        private static Resolution absent() {
            return new Resolution(Optional.empty(), false);
        }

        /**
         * Creates a resolution representing multiple distinct verified source values.
         *
         * @return ambiguous resolution
         */
        private static Resolution ambiguousResult() {
            return new Resolution(Optional.empty(), true);
        }

    }

}
