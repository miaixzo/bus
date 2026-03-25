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
 * Represents an annotation attribute with a forced alias. When {@link #getValue()} is called, it always returns the
 * value of {@link #linked}.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class ForceAliasedAnnotationAttribute extends AbstractWrappedAnnotationAttribute {

    /**
     * Constructs a new {@code ForceAliasedAnnotationAttribute}.
     *
     * @param origin The original attribute
     * @param linked The force-aliased attribute
     */
    public ForceAliasedAnnotationAttribute(AnnotationAttribute origin, AnnotationAttribute linked) {
        super(origin, linked);
    }

    /**
     * Always returns the value of {@link #linked}'s {@link AnnotationAttribute#getValue()}.
     *
     * @return The return value of {@link #linked}'s {@link AnnotationAttribute#getValue()}
     */
    @Override
    public Object getValue() {
        return linked.getValue();
    }

    /**
     * Always returns the value of {@link #linked}'s {@link AnnotationAttribute#isValueEquivalentToDefaultValue()}.
     *
     * @return The return value of {@link #linked}'s {@link AnnotationAttribute#isValueEquivalentToDefaultValue()}
     */
    @Override
    public boolean isValueEquivalentToDefaultValue() {
        return linked.isValueEquivalentToDefaultValue();
    }

    /**
     * Always returns the value of {@link #linked}'s {@link AnnotationAttribute#getAttributeType()}.
     *
     * @return The return value of {@link #linked}'s {@link AnnotationAttribute#getAttributeType()}
     */
    @Override
    public Class<?> getAttributeType() {
        return linked.getAttributeType();
    }

}
