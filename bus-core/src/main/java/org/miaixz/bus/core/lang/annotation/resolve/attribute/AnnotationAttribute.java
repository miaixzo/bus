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

import org.miaixz.bus.core.lang.annotation.resolve.processor.SynthesizedAnnotationPostProcessor;
import org.miaixz.bus.core.lang.annotation.resolve.synthesize.SynthesizedAggregateAnnotation;
import org.miaixz.bus.core.lang.annotation.resolve.synthesize.SynthesizedAnnotation;
import org.miaixz.bus.core.xyz.MethodKit;

/**
 * Represents an attribute of an annotation, equivalent to the bound {@link Method} of the invocation target.
 * <p>
 * During the parsing and value retrieval process of {@link SynthesizedAggregateAnnotation}, the annotation attributes
 * of {@link SynthesizedAnnotation} can be configured so that one annotation object can retrieve attribute values from
 * another annotation object.
 * <p>
 * In general, annotation attribute processing occurs during the {@link SynthesizedAnnotationPostProcessor} invocation.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface AnnotationAttribute {

    /**
     * Returns the annotation object.
     *
     * @return The annotation object
     */
    Annotation getAnnotation();

    /**
     * Returns the method corresponding to the annotation attribute.
     *
     * @return The method corresponding to the annotation attribute
     */
    Method getAttribute();

    /**
     * Returns the annotation class declaring this attribute.
     *
     * @return The annotation class declaring this attribute
     */
    default Class<?> getAnnotationType() {
        return getAttribute().getDeclaringClass();
    }

    /**
     * Returns the attribute name.
     *
     * @return The attribute name
     */
    default String getAttributeName() {
        return getAttribute().getName();
    }

    /**
     * Returns the annotation attribute value.
     *
     * @return The annotation attribute value
     */
    default Object getValue() {
        return MethodKit.invoke(getAnnotation(), getAttribute());
    }

    /**
     * Returns whether the attribute value equals its default value.
     *
     * @return {@code true} if the attribute value equals its default value
     */
    boolean isValueEquivalentToDefaultValue();

    /**
     * Returns the attribute type.
     *
     * @return The attribute type
     */
    default Class<?> getAttributeType() {
        return getAttribute().getReturnType();
    }

    /**
     * Returns the annotation of the specified type on the attribute.
     *
     * @param <T>            The annotation type
     * @param annotationType The annotation type
     * @return The annotation object, or {@code null} if not present
     */
    default <T extends Annotation> T getAnnotation(final Class<T> annotationType) {
        return getAttribute().getAnnotation(annotationType);
    }

    /**
     * Returns whether this annotation attribute is wrapped by a {@link WrappedAnnotationAttribute}.
     *
     * @return {@code true} if this attribute is wrapped
     */
    default boolean isWrapped() {
        return false;
    }

}
