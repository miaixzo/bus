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

import java.util.Objects;

/**
 * Schema entry for one typed metric attribute.
 *
 * @param key            attribute key
 * @param type           value type
 * @param strictIdentity whether cardinality overflow must reject this identity instead of merging it
 * @param <T>            Java value type
 * @author Kimi Liu
 */
public record AttributeDescriptor<T>(String key, AttributeType type, boolean strictIdentity) {

    /**
     * Validates the schema entry.
     */
    public AttributeDescriptor {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Attribute key must not be blank");
        }
        type = Objects.requireNonNull(type, "Attribute type must not be null");
    }

    /**
     * Creates a non-strict string attribute.
     *
     * @param key attribute key
     * @return descriptor
     */
    public static AttributeDescriptor<String> string(String key) {
        return string(key, false);
    }

    /**
     * Creates a string attribute.
     *
     * @param key            attribute key
     * @param strictIdentity strict identity flag
     * @return descriptor
     */
    public static AttributeDescriptor<String> string(String key, boolean strictIdentity) {
        return new AttributeDescriptor<>(key, AttributeType.STRING, strictIdentity);
    }

    /**
     * Creates a non-strict long attribute.
     *
     * @param key attribute key
     * @return descriptor
     */
    public static AttributeDescriptor<Long> longValue(String key) {
        return longValue(key, false);
    }

    /**
     * Creates a long attribute.
     *
     * @param key            attribute key
     * @param strictIdentity strict identity flag
     * @return descriptor
     */
    public static AttributeDescriptor<Long> longValue(String key, boolean strictIdentity) {
        return new AttributeDescriptor<>(key, AttributeType.LONG, strictIdentity);
    }

    /**
     * Creates a non-strict double attribute.
     *
     * @param key attribute key
     * @return descriptor
     */
    public static AttributeDescriptor<Double> doubleValue(String key) {
        return doubleValue(key, false);
    }

    /**
     * Creates a double attribute.
     *
     * @param key            attribute key
     * @param strictIdentity strict identity flag
     * @return descriptor
     */
    public static AttributeDescriptor<Double> doubleValue(String key, boolean strictIdentity) {
        return new AttributeDescriptor<>(key, AttributeType.DOUBLE, strictIdentity);
    }

    /**
     * Creates a non-strict boolean attribute.
     *
     * @param key attribute key
     * @return descriptor
     */
    public static AttributeDescriptor<Boolean> bool(String key) {
        return bool(key, false);
    }

    /**
     * Creates a boolean attribute.
     *
     * @param key            attribute key
     * @param strictIdentity strict identity flag
     * @return descriptor
     */
    public static AttributeDescriptor<Boolean> bool(String key, boolean strictIdentity) {
        return new AttributeDescriptor<>(key, AttributeType.BOOLEAN, strictIdentity);
    }

    /**
     * Validates one value against this descriptor.
     *
     * @param value value to validate
     */
    public void validateValue(Object value) {
        type.validate(value);
        if (type == AttributeType.DOUBLE && !Double.isFinite((Double) value)) {
            throw new IllegalArgumentException("Attribute " + key + " must be finite");
        }
    }

}
