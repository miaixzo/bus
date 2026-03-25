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
import java.util.Map;
import java.util.function.UnaryOperator;

import org.miaixz.bus.core.lang.annotation.resolve.attribute.AnnotationAttribute;
import org.miaixz.bus.core.lang.annotation.resolve.attribute.AnnotationAttributeValueProvider;
import org.miaixz.bus.core.xyz.CollKit;

/**
 * Represents a synthesized annotation within a {@link SynthesizedAggregateAnnotation}.
 *
 * <p>
 * When multiple synthesized annotations are ordered, {@link #DEFAULT_HIERARCHICAL_COMPARATOR} is used by default to
 * sort them by {@link #getVerticalDistance()} and {@link #getHorizontalDistance()}, ensuring that annotations closer to
 * the root element have higher priority during processing.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface SynthesizedAnnotation extends Annotation, Hierarchical, AnnotationAttributeValueProvider {

    /**
     * Returns the original annotation object being synthesized.
     *
     * @return the original annotation object
     */
    Annotation getAnnotation();

    /**
     * Returns the vertical distance from this synthesized annotation to the root object. By default, this is the number
     * of hierarchy levels between this annotation and the root object.
     *
     * @return the vertical distance from the root object
     */
    @Override
    int getVerticalDistance();

    /**
     * Returns the horizontal distance from this synthesized annotation to the root object. By default, this is the
     * number of annotations already scanned at the same level.
     *
     * @return the horizontal distance from the root object
     */
    @Override
    int getHorizontalDistance();

    /**
     * Returns whether this annotation has an attribute with the given name whose return type is the same as or a
     * subtype of the specified type.
     *
     * @param attributeName the attribute name
     * @param returnType    the expected return type
     * @return {@code true} if the attribute exists with a compatible type
     */
    boolean hasAttribute(String attributeName, Class<?> returnType);

    /**
     * Returns all attributes of this synthesized annotation.
     *
     * @return a map from attribute name to {@link AnnotationAttribute}
     */
    Map<String, AnnotationAttribute> getAttributes();

    /**
     * Sets all attributes of this synthesized annotation from the given map.
     *
     * @param attributes a map from attribute name to {@link AnnotationAttribute}
     */
    default void setAttributes(final Map<String, AnnotationAttribute> attributes) {
        if (CollKit.isNotEmpty(attributes)) {
            attributes.forEach(this::setAttribute);
        }
    }

    /**
     * Sets the annotation attribute for the given attribute name.
     *
     * @param attributeName the attribute name
     * @param attribute     the annotation attribute to set
     */
    void setAttribute(String attributeName, AnnotationAttribute attribute);

    /**
     * Replaces the annotation attribute for the given attribute name using the given operator.
     *
     * @param attributeName the attribute name
     * @param operator      the replacement operator applied to the current attribute
     */
    void replaceAttribute(String attributeName, UnaryOperator<AnnotationAttribute> operator);

    /**
     * Returns the value of the attribute with the given name.
     *
     * @param attributeName the attribute name
     * @return the attribute value, or {@code null} if not found
     */
    Object getAttributeValue(String attributeName);

}
