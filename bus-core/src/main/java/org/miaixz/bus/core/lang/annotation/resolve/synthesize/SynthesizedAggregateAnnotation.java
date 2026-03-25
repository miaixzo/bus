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

import org.miaixz.bus.core.lang.annotation.Link;
import org.miaixz.bus.core.lang.annotation.resolve.attribute.AnnotationAttributeValueProvider;
import org.miaixz.bus.core.lang.annotation.resolve.processor.SynthesizedAnnotationAttributeProcessor;
import org.miaixz.bus.core.lang.annotation.resolve.processor.SynthesizedAnnotationPostProcessor;

import java.lang.annotation.Annotation;

/**
 * Represents an aggregate annotation that combines a group of annotation objects based on specific rules, allowing
 * certain annotation attributes to be "synthesized" with values that may differ from the originals.
 *
 * <p>
 * Synthesized aggregate annotations are used to handle annotation objects in a class hierarchy that have direct or
 * indirect associations. When an instance is created, these annotation objects are collected and filtered by type using
 * {@link SynthesizedAnnotationSelector}, resulting in a set of effective annotations with no duplicate types. These are
 * wrapped as {@link SynthesizedAnnotation} objects. {@link SynthesizedAnnotationSelector} is the first lifecycle hook;
 * customize it to intercept the annotation scanning process.
 *
 * <p>
 * After scanning is complete, {@link SynthesizedAnnotationPostProcessor} instances are invoked in order.
 * Post-processors allow secondary adjustment of synthesized annotations, typically for adjusting attributes based on
 * {@link Link}. {@link SynthesizedAnnotationPostProcessor} is the second lifecycle hook.
 *
 * <p>
 * Synthesized annotations can be obtained via {@link #synthesize(Class)}. The returned annotation may be the original
 * or a dynamic proxy whose attribute values are provided by {@link SynthesizedAnnotationAttributeProcessor}.
 * {@link SynthesizedAnnotationAttributeProcessor} is the third lifecycle hook; customize it to intercept attribute
 * value retrieval.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface SynthesizedAggregateAnnotation
        extends AggregateAnnotation, Hierarchical, AnnotationSynthesizer, AnnotationAttributeValueProvider {

    /**
     * Returns the vertical distance from this aggregate to its root object. Since the aggregate annotation is itself
     * the root, this always returns {@code 0}.
     *
     * @return {@code 0}
     */
    @Override
    default int getVerticalDistance() {
        return 0;
    }

    /**
     * Returns the horizontal distance from this aggregate to its root object. Since the aggregate annotation is itself
     * the root, this always returns {@code 0}.
     *
     * @return {@code 0}
     */
    @Override
    default int getHorizontalDistance() {
        return 0;
    }

    /**
     * Returns the annotation of the specified type from this aggregate, or {@code null} if not present.
     *
     * @param annotationType the annotation type to retrieve
     * @param <T>            the annotation type
     * @return the annotation object, or {@code null} if not found
     */
    <T extends Annotation> T getAnnotation(Class<T> annotationType);

    /**
     * Returns the annotation attribute processor used to retrieve attribute values from synthesized annotations.
     *
     * @return the annotation attribute processor
     */
    SynthesizedAnnotationAttributeProcessor getAnnotationAttributeProcessor();

    /**
     * Returns the annotation type of this aggregate, which is the class of this instance.
     *
     * @return the annotation type
     */
    @Override
    default Class<? extends Annotation> annotationType() {
        return this.getClass();
    }

    /**
     * Retrieves the attribute value of the specified name and type from this aggregate.
     *
     * @param attributeName the attribute name
     * @param attributeType the attribute type
     * @return the attribute value
     */
    @Override
    Object getAttributeValue(String attributeName, Class<?> attributeType);

}
