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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collection;

/**
 * Represents a wrapped {@link AnnotationAttribute}. Some methods in this instance may be delegated to another
 * annotation attribute object, allowing the original annotation attribute's methods to retrieve values from a different
 * annotation attribute. Except for {@link #getValue()}, other method return values should match those of the
 * {@link AnnotationAttribute} object returned by {@link #getOriginal()}.
 * <p>
 * When a wrapper is wrapped multiple times, the rule priority is in reverse order of wrapping sequence. For example, if
 * a and b are mirrors of each other, both should be wrapped with {@link MirroredAnnotationAttribute}. If c is then
 * specified as an alias for a, then c, a, and b must each be wrapped again with {@link AliasedAnnotationAttribute}. At
 * this point a and b are double-wrapped: {@link AliasedAnnotationAttribute} logic executes first, and if its rule does
 * not apply (e.g., c only has its default value), the previous {@link MirroredAnnotationAttribute} logic takes effect.
 * <p>
 * The wrapped {@link AnnotationAttribute} has the actual structure of a binary tree. When a wrapper is re-wrapped, it
 * is equivalent to adding a new root node, and all associated leaf nodes of the tree must be updated accordingly.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface WrappedAnnotationAttribute extends AnnotationAttribute {

    /**
     * Returns the wrapped {@link AnnotationAttribute} object, which may itself be a {@link WrappedAnnotationAttribute}.
     *
     * @return The wrapped annotation attribute
     */
    AnnotationAttribute getOriginal();

    /**
     * Returns the innermost (non-wrapped) original {@link AnnotationAttribute}.
     *
     * @return The innermost original annotation attribute
     */
    AnnotationAttribute getNonWrappedOriginal();

    /**
     * Returns the linked {@link AnnotationAttribute} object that wraps {@link #getOriginal()}.
     *
     * @return The linked annotation attribute
     */
    AnnotationAttribute getLinked();

    /**
     * Traverses the tree structure rooted at this instance and collects all non-wrapped leaf attributes.
     *
     * @return All non-wrapped leaf attributes
     */
    Collection<AnnotationAttribute> getAllLinkedNonWrappedAttributes();

    /**
     * Returns the annotation object.
     *
     * @return The annotation object
     */
    @Override
    default Annotation getAnnotation() {
        return getOriginal().getAnnotation();
    }

    /**
     * Returns the method corresponding to the annotation attribute.
     *
     * @return The method corresponding to the annotation attribute
     */
    @Override
    default Method getAttribute() {
        return getOriginal().getAttribute();
    }

    /**
     * Returns whether the attribute value equals its default value. By default, returns {@code true} only when both
     * {@link #getOriginal()} and {@link #getLinked()} have default values.
     *
     * @return {@code true} if the attribute value equals its default value
     */
    @Override
    boolean isValueEquivalentToDefaultValue();

    /**
     * Returns the attribute type.
     *
     * @return The attribute type
     */
    @Override
    default Class<?> getAttributeType() {
        return getOriginal().getAttributeType();
    }

    /**
     * Returns the annotation of the specified type on the attribute.
     *
     * @param annotationType The annotation type
     * @return The annotation object, or {@code null} if not present
     */
    @Override
    default <T extends Annotation> T getAnnotation(Class<T> annotationType) {
        return getOriginal().getAnnotation(annotationType);
    }

    /**
     * Returns whether this annotation attribute is wrapped by a {@link WrappedAnnotationAttribute}.
     *
     * @return {@code true} always, since this is a wrapped attribute
     */
    @Override
    default boolean isWrapped() {
        return true;
    }

}
