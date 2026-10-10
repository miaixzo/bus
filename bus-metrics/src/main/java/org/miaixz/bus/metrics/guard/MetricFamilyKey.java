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
package org.miaixz.bus.metrics.guard;

import java.util.*;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import org.miaixz.bus.metrics.nimble.InstrumentKind;
import org.miaixz.bus.metrics.nimble.MetricDescriptor;
import org.miaixz.bus.metrics.nimble.NumberKind;
import org.miaixz.bus.metrics.observe.tag.AttributeType;

/**
 * Pair of logical and backend-export identities for a metric family.
 *
 * @param logical logical Bus identity, including instrumentation scope
 * @param export  flattened backend identity after name and label normalization
 * @author Kimi Liu
 */
public record MetricFamilyKey(Logical logical, Export export) {

    /**
     * Validates both identities.
     */
    public MetricFamilyKey {
        logical = Objects.requireNonNull(logical, "Logical family key must not be null");
        export = Objects.requireNonNull(export, "Export family key must not be null");
    }

    /**
     * Creates keys for a descriptor using backend-specific normalizers.
     *
     * @param descriptor          metric descriptor
     * @param nameNormalizer      backend metric-name normalizer
     * @param attributeNormalizer backend attribute-name normalizer
     * @return immutable key pair
     */
    public static MetricFamilyKey of(
            MetricDescriptor descriptor,
            UnaryOperator<String> nameNormalizer,
            UnaryOperator<String> attributeNormalizer) {
        Objects.requireNonNull(nameNormalizer, "Metric name normalizer must not be null");
        Function<MetricDescriptor, String> exportName = value -> nameNormalizer.apply(value.name());
        return of(descriptor, exportName, attributeNormalizer);
    }

    /**
     * Creates keys using a descriptor-aware backend export-name function.
     *
     * @param descriptor          metric descriptor
     * @param exportName          backend final export-name function
     * @param attributeNormalizer backend attribute-name normalizer
     * @return immutable key pair
     */
    public static MetricFamilyKey of(
            MetricDescriptor descriptor,
            Function<MetricDescriptor, String> exportName,
            UnaryOperator<String> attributeNormalizer) {
        Objects.requireNonNull(descriptor, "Metric descriptor must not be null");
        Objects.requireNonNull(exportName, "Metric export-name function must not be null");
        Objects.requireNonNull(attributeNormalizer, "Attribute normalizer must not be null");

        List<Attribute> logicalAttributes = descriptor.attributes().stream()
                .map(value -> new Attribute(value.key(), value.type(), value.strictIdentity())).toList();
        List<Attribute> exportAttributes = new ArrayList<>(logicalAttributes.size());
        Set<String> normalizedNames = new HashSet<>();
        for (Attribute attribute : logicalAttributes) {
            String normalized = requireNormalized("attribute", attributeNormalizer.apply(attribute.name()));
            if (!normalizedNames.add(normalized)) {
                throw new IllegalArgumentException(
                        "Attribute names collide after backend normalization: " + normalized);
            }
            exportAttributes.add(new Attribute(normalized, attribute.type(), attribute.strictIdentity()));
        }
        exportAttributes.sort((left, right) -> left.name().compareTo(right.name()));

        Logical logical = new Logical(descriptor.scope(), descriptor.name(), descriptor.kind(), descriptor.numberKind(),
                descriptor.unit(), logicalAttributes);
        Export export = new Export(requireNormalized("metric", exportName.apply(descriptor)), descriptor.kind(),
                descriptor.numberKind(), descriptor.unit(), exportAttributes);
        return new MetricFamilyKey(logical, export);
    }

    /**
     * Creates keys without backend normalization.
     *
     * @param descriptor metric descriptor
     * @return immutable key pair
     */
    public static MetricFamilyKey identity(MetricDescriptor descriptor) {
        return of(descriptor, UnaryOperator.identity(), UnaryOperator.identity());
    }

    /**
     * Requires a backend normalizer to produce a usable identifier.
     *
     * @param label identifier kind used in diagnostics
     * @param value normalized identifier
     * @return validated identifier
     */
    private static String requireNormalized(String label, String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Normalized " + label + " name must not be blank");
        }
        return value;
    }

    /**
     * Logical family identity.
     *
     * @param scope      instrumentation scope
     * @param name       metric name
     * @param kind       instrument kind
     * @param numberKind numeric channel
     * @param unit       metric unit
     * @param attributes ordered attribute schema
     */
    public record Logical(String scope, String name, InstrumentKind kind, NumberKind numberKind, String unit,
            List<Attribute> attributes) {

        /**
         * Copies the attribute schema.
         */
        public Logical {
            attributes = List.copyOf(attributes);
        }
    }

    /**
     * Backend export family identity.
     *
     * @param name       normalized metric name
     * @param kind       instrument kind
     * @param numberKind numeric channel
     * @param unit       metric unit
     * @param attributes normalized ordered attribute schema
     */
    public record Export(String name, InstrumentKind kind, NumberKind numberKind, String unit,
            List<Attribute> attributes) {

        /**
         * Copies the attribute schema.
         */
        public Export {
            attributes = List.copyOf(attributes);
        }
    }

    /**
     * Attribute schema identity.
     *
     * @param name           attribute name
     * @param type           value type
     * @param strictIdentity strict resource identity flag
     */
    public record Attribute(String name, AttributeType type, boolean strictIdentity) {
    }

}
