/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.core.lang.annotation.resolve;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import org.miaixz.bus.core.lang.annotation.Alias;

/**
 * A wrapper interface for enhancing annotations, providing additional capabilities such as attribute resolution.
 *
 * @param <T> The type of the annotation being wrapped.
 * @author Kimi Liu
 * @see AnnotationMappingProxy
 * @since Java 17+
 */
public interface AnnotationMapping<T extends Annotation> extends Annotation {

    /**
     * Checks whether the current annotation is a root annotation. A root annotation is typically one directly declared
     * on an element, as opposed to a meta-annotation.
     *
     * @return {@code true} if the annotation is a root annotation, {@code false} otherwise.
     */
    boolean isRoot();

    /**
     * Retrieves the original annotation object that this mapping wraps.
     *
     * @return The original annotation object.
     */
    T getAnnotation();

    /**
     * Generates a synthetic annotation object via dynamic proxy, based on the current mapping object. This synthetic
     * annotation behaves like the original but incorporates enhanced features:
     * <ul>
     * <li>Supports attribute aliasing within the same annotation via {@link Alias}.</li>
     * <li>Supports attribute overriding where a child annotation's attribute with the same name and type overrides that
     * of its meta-annotation.</li>
     * </ul>
     * If {@link #isResolved()} returns {@code false}, this method should return the original wrapped annotation object,
     * meaning its return value will be identical to {@link #getAnnotation()}.
     *
     * @return The resolved annotation object, or the original annotation object if {@link #isResolved()} is
     *         {@code false}.
     */
    T getResolvedAnnotation();

    /**
     * Returns the annotation type of this annotation mapping.
     *
     * @return The annotation type.
     */
    @Override
    default Class<? extends Annotation> annotationType() {
        return getAnnotation().annotationType();
    }

    /**
     * Indicates whether the attributes of this annotation have been resolved (i.e., aliasing and overriding applied).
     * If this value is {@code false}, then {@code getResolvedAttributeValue} will return the original attribute values,
     * and {@link #getResolvedAnnotation()} will return the original annotation object.
     *
     * @return {@code true} if annotation attributes have been resolved, {@code false} otherwise.
     */
    boolean isResolved();

    /**
     * Retrieves all attribute methods (representing attributes) of the original annotation.
     *
     * @return An array of {@link Method} objects representing the annotation's attributes.
     */
    Method[] getAttributes();

    /**
     * Retrieves the value of a specific attribute from the original annotation.
     *
     * @param attributeName The name of the attribute.
     * @param attributeType The expected type of the attribute's value.
     * @param <R>           The return type of the attribute value.
     * @return The attribute value.
     */
    <R> R getAttributeValue(final String attributeName, final Class<R> attributeType);

    /**
     * Retrieves the resolved value of a specific attribute, applying aliasing and overriding rules.
     *
     * @param attributeName The name of the attribute.
     * @param attributeType The expected type of the attribute's value.
     * @param <R>           The return type of the attribute value.
     * @return The resolved attribute value.
     */
    <R> R getResolvedAttributeValue(final String attributeName, final Class<R> attributeType);

}
