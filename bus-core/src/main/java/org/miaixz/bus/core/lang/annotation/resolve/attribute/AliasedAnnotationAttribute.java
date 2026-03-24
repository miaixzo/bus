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

/**
 * Represents an attribute with an alias. When the alias attribute value is its default value, the original attribute's
 * value is returned first. When the alias attribute value is non-default, the alias attribute's value is returned
 * first.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class AliasedAnnotationAttribute extends AbstractWrappedAnnotationAttribute {

    /**
     * Constructs a new {@code AliasedAnnotationAttribute}.
     *
     * @param origin The original attribute
     * @param linked The alias attribute
     */
    public AliasedAnnotationAttribute(final AnnotationAttribute origin, final AnnotationAttribute linked) {
        super(origin, linked);
    }

    /**
     * Returns {@link #original}'s value if {@link #linked} has its default value; otherwise returns {@link #linked}'s
     * value.
     *
     * @return The attribute value
     */
    @Override
    public Object getValue() {
        return linked.isValueEquivalentToDefaultValue() ? super.getValue() : linked.getValue();
    }

    /**
     * Returns {@code true} when both {@link #original} and {@link #linked} have their default values.
     *
     * @return {@code true} if both original and linked have default values
     */
    @Override
    public boolean isValueEquivalentToDefaultValue() {
        return linked.isValueEquivalentToDefaultValue() && original.isValueEquivalentToDefaultValue();
    }

}
