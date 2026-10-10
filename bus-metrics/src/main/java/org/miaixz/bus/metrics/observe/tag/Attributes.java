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
package org.miaixz.bus.metrics.observe.tag;

import java.util.*;

/**
 * Ordered, immutable collection of typed metric attributes.
 *
 * @author Kimi Liu
 */
public final class Attributes {

    /**
     * Shared immutable empty attribute collection.
     */
    private static final Attributes EMPTY = new Attributes(List.of());

    /**
     * Values in canonical attribute-key order.
     */
    private final List<Value<?>> values;

    /**
     * Creates an immutable attribute collection from canonical values.
     *
     * @param values canonical values
     */
    private Attributes(List<Value<?>> values) {
        this.values = List.copyOf(values);
    }

    /**
     * Returns the empty attribute collection.
     *
     * @return shared empty attributes
     */
    public static Attributes empty() {
        return EMPTY;
    }

    /**
     * Creates a builder.
     *
     * @return new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates a single typed attribute.
     *
     * @param descriptor attribute descriptor
     * @param value      attribute value
     * @param <T>        value type
     * @return immutable attributes
     */
    public static <T> Attributes of(AttributeDescriptor<T> descriptor, T value) {
        return builder().put(descriptor, value).build();
    }

    /**
     * Converts legacy string tags to typed string attributes.
     *
     * @param tags legacy tags
     * @return immutable attributes
     */
    public static Attributes fromTags(Tag... tags) {
        Objects.requireNonNull(tags, "Tags must not be null");
        Builder builder = builder();
        for (Tag tag : tags) {
            Objects.requireNonNull(tag, "Tag must not be null");
            builder.put(AttributeDescriptor.string(tag.key()), tag.value());
        }
        return builder.build();
    }

    /**
     * Returns typed values sorted by attribute key.
     *
     * @return immutable value list
     */
    public List<Value<?>> values() {
        return values;
    }

    /**
     * Returns the value assigned to a descriptor, or {@code null} when absent.
     *
     * @param descriptor descriptor to find
     * @param <T>        value type
     * @return value or {@code null}
     */
    @SuppressWarnings("unchecked")
    public <T> T get(AttributeDescriptor<T> descriptor) {
        Objects.requireNonNull(descriptor, "Attribute descriptor must not be null");
        for (Value<?> value : values) {
            if (value.descriptor().equals(descriptor)) {
                return (T) value.value();
            }
        }
        return null;
    }

    /**
     * Converts the values to legacy string tags without changing this collection.
     *
     * @return newly allocated tag array
     */
    public Tag[] toTags() {
        Tag[] tags = new Tag[values.size()];
        for (int i = 0; i < values.size(); i++) {
            Value<?> value = values.get(i);
            tags[i] = Tag.of(value.descriptor().key(), String.valueOf(value.value()));
        }
        return tags;
    }

    /**
     * Checks whether these values exactly implement an attribute schema.
     *
     * @param schema expected schema
     * @return {@code true} when descriptors match in key order
     */
    public boolean matchesSchema(List<AttributeDescriptor<?>> schema) {
        Objects.requireNonNull(schema, "Attribute schema must not be null");
        if (schema.size() != values.size()) {
            return false;
        }
        List<AttributeDescriptor<?>> ordered = new ArrayList<>(schema);
        ordered.sort((left, right) -> left.key().compareTo(right.key()));
        for (int i = 0; i < ordered.size(); i++) {
            if (!ordered.get(i).equals(values.get(i).descriptor())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns whether this collection has no values.
     *
     * @return {@code true} when empty
     */
    public boolean isEmpty() {
        return values.isEmpty();
    }

    @Override
    public boolean equals(Object object) {
        return this == object || object instanceof Attributes that && values.equals(that.values);
    }

    @Override
    public int hashCode() {
        return values.hashCode();
    }

    @Override
    public String toString() {
        return values.toString();
    }

    /**
     * One validated descriptor/value pair.
     *
     * @param descriptor attribute descriptor
     * @param value      attribute value
     * @param <T>        value type
     */
    public record Value<T>(AttributeDescriptor<T> descriptor, T value) {

        /**
         * Validates descriptor and value.
         */
        public Value {
            descriptor = Objects.requireNonNull(descriptor, "Attribute descriptor must not be null");
            descriptor.validateValue(value);
        }
    }

    /**
     * Builder that rejects duplicate keys and emits canonical key order.
     */
    public static final class Builder {

        /**
         * Pending values keyed in canonical order.
         */
        private final Map<String, Value<?>> values = new TreeMap<>();

        /**
         * Creates an empty attribute builder.
         */
        private Builder() {
            // No initialization required.
        }

        /**
         * Adds one typed value.
         *
         * @param descriptor attribute descriptor
         * @param value      attribute value
         * @param <T>        value type
         * @return this builder
         * @throws IllegalArgumentException if an attribute with the same key was already added
         */
        public <T> Builder put(AttributeDescriptor<T> descriptor, T value) {
            Value<T> entry = new Value<>(descriptor, value);
            Value<?> previous = values.putIfAbsent(descriptor.key(), entry);
            if (previous != null) {
                throw new IllegalArgumentException("Duplicate attribute key: " + descriptor.key());
            }
            return this;
        }

        /**
         * Builds an immutable collection.
         *
         * @return immutable attributes
         */
        public Attributes build() {
            if (values.isEmpty()) {
                return EMPTY;
            }
            return new Attributes(Collections.unmodifiableList(new ArrayList<>(values.values())));
        }
    }

}
