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
package org.miaixz.bus.core.lang.annotation.resolve.attribute;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.annotation.RelationType;

/**
 * Represents an annotation attribute that has a corresponding mirrored attribute. When getting the value, the rules of
 * {@link RelationType#MIRROR_FOR} are applied.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class MirroredAnnotationAttribute extends AbstractWrappedAnnotationAttribute {

    /**
     * Constructs a new {@code MirroredAnnotationAttribute}.
     *
     * @param origin The original attribute
     * @param linked The mirrored attribute
     */
    public MirroredAnnotationAttribute(final AnnotationAttribute origin, final AnnotationAttribute linked) {
        super(origin, linked);
    }

    /**
     * Returns the effective value according to the {@link RelationType#MIRROR_FOR} rules. If both values are the same
     * (both default or both non-default and equal), returns the original value. If exactly one is non-default, returns
     * that non-default value.
     *
     * @return the effective mirrored attribute value
     */
    @Override
    public Object getValue() {
        final boolean originIsDefault = original.isValueEquivalentToDefaultValue();
        final boolean targetIsDefault = linked.isValueEquivalentToDefaultValue();
        final Object originValue = original.getValue();
        final Object targetValue = linked.getValue();

        // Both are default or both are non-default: values must be equal
        if (originIsDefault == targetIsDefault) {
            Assert.equals(
                    originValue,
                    targetValue,
                    "the values of attributes [{}] and [{}] that mirror each other are different: [{}] <==> [{}]",
                    original.getAttribute(),
                    linked.getAttribute(),
                    originValue,
                    targetValue);
            return originValue;
        }

        // One is non-default: return the non-default value
        return originIsDefault ? targetValue : originValue;
    }

    /**
     * Returns {@code true} when both {@link #original} and {@link #linked} have their default values.
     *
     * @return {@code true} if both original and linked have default values
     */
    @Override
    public boolean isValueEquivalentToDefaultValue() {
        return original.isValueEquivalentToDefaultValue() && linked.isValueEquivalentToDefaultValue();
    }

}
