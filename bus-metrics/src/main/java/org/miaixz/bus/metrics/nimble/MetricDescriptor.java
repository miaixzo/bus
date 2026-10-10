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
package org.miaixz.bus.metrics.nimble;

import java.util.*;

import org.miaixz.bus.metrics.Builder;
import org.miaixz.bus.metrics.observe.tag.AttributeDescriptor;
import org.miaixz.bus.metrics.observe.tag.Attributes;

/**
 * Immutable schema and metadata for one metric family.
 *
 * @param scope       instrumentation scope
 * @param name        metric name
 * @param kind        instrument kind
 * @param numberKind  numeric channel
 * @param unit        unit, or an empty string when unspecified
 * @param description human-readable description, or an empty string
 * @param attributes  ordered attribute schema
 * @author Kimi Liu
 */
public record MetricDescriptor(String scope, String name, InstrumentKind kind, NumberKind numberKind, String unit,
        String description, List<AttributeDescriptor<?>> attributes) {

    /**
     * Validates and canonicalizes a descriptor.
     */
    public MetricDescriptor {
        scope = scope == null || scope.isBlank() ? Builder.OTEL_SCOPE : scope.trim();
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Metric name must not be blank");
        }
        name = name.trim();
        kind = Objects.requireNonNull(kind, "Instrument kind must not be null");
        numberKind = Objects.requireNonNull(numberKind, "Number kind must not be null");
        unit = Objects.requireNonNull(unit, "Metric unit must not be null");
        description = Objects.requireNonNull(description, "Metric description must not be null");
        validateText("Metric unit", unit);

        Objects.requireNonNull(attributes, "Attribute schema must not be null");
        List<AttributeDescriptor<?>> ordered = new ArrayList<>(attributes.size());
        Set<String> keys = new HashSet<>();
        for (AttributeDescriptor<?> attribute : attributes) {
            AttributeDescriptor<?> checked = Objects.requireNonNull(attribute, "Attribute descriptor must not be null");
            if (!keys.add(checked.key())) {
                throw new IllegalArgumentException("Duplicate attribute key in schema: " + checked.key());
            }
            ordered.add(checked);
        }
        ordered.sort((left, right) -> left.key().compareTo(right.key()));
        attributes = List.copyOf(ordered);
    }

    /**
     * Creates a descriptor in the canonical Bus instrumentation scope.
     *
     * @param name        metric name
     * @param kind        instrument kind
     * @param numberKind  numeric representation
     * @param unit        metric unit, or an empty string
     * @param description human-readable description, or an empty string
     * @param attributes  ordered or unordered attribute schema
     * @return canonical metric descriptor
     */
    public static MetricDescriptor of(
            String name,
            InstrumentKind kind,
            NumberKind numberKind,
            String unit,
            String description,
            List<AttributeDescriptor<?>> attributes) {
        return new MetricDescriptor(Builder.OTEL_SCOPE, name, kind, numberKind, unit, description, attributes);
    }

    /**
     * Creates a legacy-compatible long counter descriptor.
     *
     * @param name       metric name
     * @param attributes attribute values defining the schema
     * @return descriptor
     */
    public static MetricDescriptor counter(String name, Attributes attributes) {
        return active(name, InstrumentKind.COUNTER, NumberKind.LONG, attributes);
    }

    /**
     * Creates a legacy-compatible double gauge descriptor.
     *
     * @param name       metric name
     * @param attributes attribute values defining the schema
     * @return descriptor
     */
    public static MetricDescriptor gauge(String name, Attributes attributes) {
        return active(name, InstrumentKind.GAUGE, NumberKind.DOUBLE, attributes);
    }

    /**
     * Creates a legacy-compatible timer descriptor.
     *
     * @param name       metric name
     * @param attributes attribute values defining the schema
     * @return descriptor
     */
    public static MetricDescriptor timer(String name, Attributes attributes) {
        MetricDescriptor descriptor = active(name, InstrumentKind.TIMER, NumberKind.DOUBLE, attributes);
        return new MetricDescriptor(descriptor.scope(), descriptor.name(), descriptor.kind(), descriptor.numberKind(),
                "s", descriptor.description(), descriptor.attributes());
    }

    /**
     * Creates a legacy-compatible histogram descriptor.
     *
     * @param name       metric name
     * @param attributes attribute values defining the schema
     * @return descriptor
     */
    public static MetricDescriptor histogram(String name, Attributes attributes) {
        return active(name, InstrumentKind.HISTOGRAM, NumberKind.DOUBLE, attributes);
    }

    /**
     * Creates a descriptor from an active legacy metric call.
     *
     * @param name       metric name
     * @param kind       instrument kind
     * @param numberKind numeric representation
     * @param attributes active attribute values
     * @return metric descriptor
     */
    private static MetricDescriptor active(
            String name,
            InstrumentKind kind,
            NumberKind numberKind,
            Attributes attributes) {
        Objects.requireNonNull(attributes, "Metric attributes must not be null");
        List<AttributeDescriptor<?>> schema = new ArrayList<>(attributes.values().size());
        for (Attributes.Value<?> value : attributes.values()) {
            schema.add(value.descriptor());
        }
        return of(name, kind, numberKind, "", "", schema);
    }

    /**
     * Rejects control characters from descriptor metadata.
     *
     * @param label diagnostic label
     * @param value metadata value
     */
    private static void validateText(String label, String value) {
        for (int i = 0; i < value.length(); i++) {
            if (Character.isISOControl(value.charAt(i))) {
                throw new IllegalArgumentException(label + " must not contain control characters");
            }
        }
    }

    /**
     * Checks logical family identity, which intentionally excludes description.
     *
     * @param other descriptor to compare
     * @return {@code true} when both descriptors identify the same family
     */
    public boolean sameIdentity(MetricDescriptor other) {
        return other != null && scope.equals(other.scope) && name.equals(other.name) && kind == other.kind
                && numberKind == other.numberKind && unit.equals(other.unit) && attributes.equals(other.attributes);
    }

    /**
     * Validates one attribute set against this family's schema.
     *
     * @param values attributes to validate
     */
    public void validateAttributes(Attributes values) {
        Objects.requireNonNull(values, "Metric attributes must not be null");
        if (!values.matchesSchema(attributes)) {
            throw new IllegalArgumentException("Attributes do not match metric schema for " + name);
        }
    }

}
