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
import java.util.Objects;
import java.util.stream.Stream;

import org.miaixz.bus.core.text.CharsBacker;
import org.miaixz.bus.core.xyz.AnnoKit;
import org.miaixz.bus.core.xyz.ClassKit;
import org.miaixz.bus.core.xyz.MethodKit;

/**
 * A basic implementation of {@link AnnotationMapping} that simply wraps an annotation object. This class does not
 * perform any advanced attribute resolution like aliasing or overriding.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class GenericAnnotationMapping implements AnnotationMapping<Annotation> {

    /**
     * The wrapped annotation object.
     */
    private final Annotation annotation;
    /**
     * Indicates whether this is a root annotation.
     */
    private final boolean isRoot;
    /**
     * The attributes (methods) of the annotation.
     */
    private final Method[] attributes;

    /**
     * Constructs a new {@code GenericAnnotationMapping} with the given annotation and root status.
     *
     * @param annotation The annotation object to wrap.
     * @param isRoot     {@code true} if this is a root annotation, {@code false} otherwise.
     */
    public GenericAnnotationMapping(final Annotation annotation, final boolean isRoot) {
        this.annotation = Objects.requireNonNull(annotation);
        this.isRoot = isRoot;
        this.attributes = AnnoKit.getAnnotationAttributes(annotation.annotationType());
    }

    /**
     * Creates a new {@code GenericAnnotationMapping} instance.
     *
     * @param annotation The annotation object to wrap.
     * @param isRoot     {@code true} if this is a root annotation, {@code false} otherwise.
     * @return A new {@code GenericAnnotationMapping} instance.
     */
    public static GenericAnnotationMapping of(final Annotation annotation, final boolean isRoot) {
        return new GenericAnnotationMapping(annotation, isRoot);
    }

    /**
     * Checks whether the current annotation is a root annotation.
     *
     * @return {@code true} if the annotation is a root annotation, {@code false} otherwise.
     */
    @Override
    public boolean isRoot() {
        return isRoot;
    }

    /**
     * Retrieves the original annotation object.
     *
     * @return The original annotation object.
     */
    @Override
    public Annotation getAnnotation() {
        return annotation;
    }

    /**
     * Returns the original annotation object, as this mapping does not perform resolution. This method is equivalent to
     * {@link #getAnnotation()}.
     *
     * @return The original annotation object.
     */
    @Override
    public Annotation getResolvedAnnotation() {
        return getAnnotation();
    }

    /**
     * Always returns {@code false} as this mapping does not perform attribute resolution.
     *
     * @return {@code false}.
     */
    @Override
    public boolean isResolved() {
        return false;
    }

    /**
     * Retrieves the original attributes (methods) of the annotation.
     *
     * @return An array of {@link Method} objects representing the annotation's attributes.
     */
    @Override
    public Method[] getAttributes() {
        return attributes;
    }

    /**
     * Retrieves the value of a specific attribute from the original annotation.
     *
     * @param attributeName The name of the attribute.
     * @param attributeType The expected type of the attribute's value.
     * @param <R>           The return type of the attribute value.
     * @return The attribute value, or {@code null} if the attribute is not found or its type is incompatible.
     */
    @Override
    public <R> R getAttributeValue(final String attributeName, final Class<R> attributeType) {
        return Stream.of(attributes).filter(attribute -> CharsBacker.equals(attribute.getName(), attributeName))
                .filter(attribute -> ClassKit.isAssignable(attributeType, attribute.getReturnType())).findFirst()
                .map(method -> MethodKit.invoke(annotation, method)).map(attributeType::cast).orElse(null);
    }

    /**
     * Retrieves the resolved attribute value. For {@code GenericAnnotationMapping}, this is the same as
     * {@link #getAttributeValue(String, Class)} as no resolution is performed.
     *
     * @param attributeName The name of the attribute.
     * @param attributeType The expected type of the attribute's value.
     * @param <R>           The return type of the attribute value.
     * @return The attribute value, or {@code null} if the attribute is not found or its type is incompatible.
     */
    @Override
    public <R> R getResolvedAttributeValue(final String attributeName, final Class<R> attributeType) {
        return getAttributeValue(attributeName, attributeType);
    }

    /**
     * Compares this instance with the specified object for equality. Two {@code GenericAnnotationMapping} instances are
     * considered equal if their wrapped annotations and root status are equal.
     *
     * @param o The object to compare with.
     * @return {@code true} if the objects are equal, {@code false} otherwise.
     */
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final GenericAnnotationMapping that = (GenericAnnotationMapping) o;
        return isRoot == that.isRoot && annotation.equals(that.annotation);
    }

    /**
     * Returns a hash code value for the object.
     *
     * @return A hash code value for this object.
     */
    @Override
    public int hashCode() {
        return Objects.hash(annotation, isRoot);
    }

}
