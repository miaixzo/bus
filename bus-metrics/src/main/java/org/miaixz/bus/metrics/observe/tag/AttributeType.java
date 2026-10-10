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

/**
 * Supported metric attribute value types.
 *
 * @author Kimi Liu
 */
public enum AttributeType {

    /**
     * UTF-16 Java string value.
     */
    STRING(String.class),

    /**
     * Signed 64-bit integer value.
     */
    LONG(Long.class),

    /**
     * IEEE 754 double-precision value.
     */
    DOUBLE(Double.class),

    /**
     * Boolean value.
     */
    BOOLEAN(Boolean.class);

    /**
     * Exact Java value class accepted by this attribute type.
     */
    private final Class<?> valueClass;

    /**
     * Associates the attribute kind with its exact Java value class.
     *
     * @param valueClass accepted value class
     */
    AttributeType(Class<?> valueClass) {
        this.valueClass = valueClass;
    }

    /**
     * Checks whether the supplied value exactly matches this attribute type.
     *
     * @param value value to check
     * @return {@code true} when the value is non-null and has the expected type
     */
    public boolean accepts(Object value) {
        return valueClass.isInstance(value);
    }

    /**
     * Validates a value against this type.
     *
     * @param value value to validate
     * @throws IllegalArgumentException when the value is null or has a different type
     */
    public void validate(Object value) {
        if (!accepts(value)) {
            throw new IllegalArgumentException("Expected attribute value of type " + this + " but got "
                    + (value == null ? "null" : value.getClass().getSimpleName()));
        }
    }

}
