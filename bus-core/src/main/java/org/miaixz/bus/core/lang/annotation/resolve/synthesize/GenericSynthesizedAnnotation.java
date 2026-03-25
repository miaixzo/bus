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
package org.miaixz.bus.core.lang.annotation.resolve.synthesize;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.annotation.resolve.attribute.AnnotationAttribute;
import org.miaixz.bus.core.lang.annotation.resolve.attribute.CacheableAnnotationAttribute;
import org.miaixz.bus.core.xyz.ClassKit;
import org.miaixz.bus.core.xyz.MethodKit;
import org.miaixz.bus.core.xyz.ObjectKit;

/**
 * Basic implementation of {@link SynthesizedAnnotation}.
 *
 * @param <R> the type of the root object
 * @param <T> the annotation type
 * @author Kimi Liu
 * @since Java 21+
 */
public class GenericSynthesizedAnnotation<R, T extends Annotation> implements SynthesizedAnnotation {

    /**
     * The root object associated with this synthesized annotation.
     */
    private final R root;

    /**
     * The original annotation being synthesized.
     */
    private final T annotation;

    /**
     * Cache of annotation attribute methods, keyed by attribute name.
     */
    private final Map<String, AnnotationAttribute> attributeMethodCaches;

    /**
     * The vertical distance from the root object to this synthesized annotation.
     */
    private final int verticalDistance;

    /**
     * The horizontal distance from the root object to this synthesized annotation.
     */
    private final int horizontalDistance;

    /**
     * Constructs a new {@code GenericSynthesizedAnnotation}.
     *
     * @param root               the root object
     * @param annotation         the annotation to synthesize
     * @param verticalDistance   the vertical distance from the root object
     * @param horizontalDistance the horizontal distance from the root object
     */
    protected GenericSynthesizedAnnotation(final R root, final T annotation, final int verticalDistance,
            final int horizontalDistance) {
        this.root = root;
        this.annotation = annotation;
        this.verticalDistance = verticalDistance;
        this.horizontalDistance = horizontalDistance;
        this.attributeMethodCaches = new HashMap<>();
        this.attributeMethodCaches.putAll(loadAttributeMethods());
    }

    /**
     * Loads and returns the annotation attribute methods for the wrapped annotation type.
     *
     * @return a map from attribute name to {@link AnnotationAttribute}
     */
    protected Map<String, AnnotationAttribute> loadAttributeMethods() {
        return Stream.of(MethodKit.getDeclaredMethods(annotation.annotationType())).filter(MethodKit::isAttributeMethod)
                .collect(
                        Collectors.toMap(
                                Method::getName,
                                method -> new CacheableAnnotationAttribute(annotation, method)));
    }

    /**
     * Returns whether this synthesized annotation has an attribute with the given name.
     *
     * @param attributeName the attribute name
     * @return {@code true} if the attribute exists
     */
    public boolean hasAttribute(final String attributeName) {
        return attributeMethodCaches.containsKey(attributeName);
    }

    /**
     * Returns whether this synthesized annotation has an attribute with the given name whose return type is assignable
     * to the specified type.
     *
     * @param attributeName the attribute name
     * @param returnType    the expected return type
     * @return {@code true} if the attribute exists with a compatible type
     */
    @Override
    public boolean hasAttribute(final String attributeName, final Class<?> returnType) {
        return Optional.ofNullable(attributeMethodCaches.get(attributeName))
                .filter(method -> ClassKit.isAssignable(returnType, method.getAttributeType())).isPresent();
    }

    /**
     * Returns all attributes of this synthesized annotation.
     *
     * @return a map from attribute name to {@link AnnotationAttribute}
     */
    @Override
    public Map<String, AnnotationAttribute> getAttributes() {
        return this.attributeMethodCaches;
    }

    /**
     * Sets the annotation attribute for the given attribute name.
     *
     * @param attributeName the attribute name
     * @param attribute     the annotation attribute to set
     */
    @Override
    public void setAttribute(final String attributeName, final AnnotationAttribute attribute) {
        attributeMethodCaches.put(attributeName, attribute);
    }

    /**
     * Replaces the annotation attribute for the given attribute name using the given operator.
     *
     * @param attributeName the attribute name
     * @param operator      the replacement operator applied to the current attribute
     */
    @Override
    public void replaceAttribute(final String attributeName, final UnaryOperator<AnnotationAttribute> operator) {
        final AnnotationAttribute old = attributeMethodCaches.get(attributeName);
        if (ObjectKit.isNotNull(old)) {
            attributeMethodCaches.put(attributeName, operator.apply(old));
        }
    }

    /**
     * Returns the value of the attribute with the given name.
     *
     * @param attributeName the attribute name
     * @return the attribute value, or {@code null} if not found
     */
    @Override
    public Object getAttributeValue(final String attributeName) {
        return Optional.ofNullable(attributeMethodCaches.get(attributeName)).map(AnnotationAttribute::getValue)
                .getOrNull();
    }

    /**
     * Returns the root object associated with this synthesized annotation.
     *
     * @return the root object
     */
    @Override
    public R getRoot() {
        return root;
    }

    /**
     * Returns the original annotation being synthesized.
     *
     * @return the original annotation object
     */
    @Override
    public T getAnnotation() {
        return annotation;
    }

    /**
     * Returns the vertical distance from the root object to this synthesized annotation. By default, this is the number
     * of hierarchy levels between this annotation and the root object.
     *
     * @return the vertical distance from the root object
     */
    @Override
    public int getVerticalDistance() {
        return verticalDistance;
    }

    /**
     * Returns the horizontal distance from the root object to this synthesized annotation. By default, this is the
     * number of annotations already scanned at the same level.
     *
     * @return the horizontal distance from the root object
     */
    @Override
    public int getHorizontalDistance() {
        return horizontalDistance;
    }

    /**
     * Returns the type of the annotation being synthesized.
     *
     * @return the annotation type
     */
    @Override
    public Class<? extends Annotation> annotationType() {
        return annotation.annotationType();
    }

    /**
     * Returns the value of the attribute with the given name and type. Returns {@code null} if the attribute does not
     * exist or its type is not assignable to {@code attributeType}.
     *
     * @param attributeName the attribute name
     * @param attributeType the attribute type
     * @return the attribute value, or {@code null} if not found or type mismatch
     */
    @Override
    public Object getAttributeValue(final String attributeName, final Class<?> attributeType) {
        return Optional.ofNullable(attributeMethodCaches.get(attributeName))
                .filter(method -> ClassKit.isAssignable(attributeType, method.getAttributeType()))
                .map(AnnotationAttribute::getValue).getOrNull();
    }

}
