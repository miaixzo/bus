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
import java.util.Collection;
import java.util.Map;

import org.miaixz.bus.core.lang.annotation.Link;
import org.miaixz.bus.core.lang.annotation.resolve.processor.SynthesizedAnnotationPostProcessor;

/**
 * Annotation synthesizer, used to process a given set of annotation objects that have a direct or indirect association
 * with {@link #getSource()}, and return "synthesized" annotation objects whose attributes may differ from the
 * originals.
 *
 * <p>
 * Synthesized annotations are generally used to handle annotation objects in a class hierarchy that have direct or
 * indirect associations. When an instance is created, these annotation objects are collected and filtered by type using
 * {@link SynthesizedAnnotationSelector}, resulting in a set of effective annotations with no duplicate types. These
 * effective annotations are wrapped as {@link SynthesizedAnnotation} objects and ultimately used to "synthesize" a
 * {@link SynthesizedAggregateAnnotation}. {@link SynthesizedAnnotationSelector} is the first hook in the synthesized
 * annotation lifecycle; customize the selector to intercept the scanning of original annotations.
 *
 * <p>
 * After the synthesizer completes scanning and loading, it will invoke {@link SynthesizedAnnotationPostProcessor}
 * instances in order. Post-processors allow secondary adjustment of the synthesized annotations, typically to adjust
 * attributes based on {@link Link} annotations. {@link SynthesizedAnnotationPostProcessor} is the second hook in the
 * lifecycle; customize post-processors to intercept the initialization of synthesized annotations.
 *
 * <p>
 * Use {@link #synthesize(Class)} to obtain the "synthesized" annotation, whose attribute values may differ from the
 * original annotation.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface AnnotationSynthesizer {

    /**
     * Returns the original source from which the synthesized annotations originate.
     *
     * @return the original source object
     */
    Object getSource();

    /**
     * Returns the annotation selector used to resolve duplicate annotation types.
     *
     * @return the annotation selector
     */
    SynthesizedAnnotationSelector getAnnotationSelector();

    /**
     * Returns the collection of post-processors applied to synthesized annotations.
     *
     * @return the annotation post-processors
     */
    Collection<SynthesizedAnnotationPostProcessor> getAnnotationPostProcessors();

    /**
     * Returns the synthesized annotation of the given type.
     *
     * @param annotationType the annotation type
     * @return the synthesized annotation, or {@code null} if not found
     */
    SynthesizedAnnotation getSynthesizedAnnotation(Class<?> annotationType);

    /**
     * Returns all synthesized annotations.
     *
     * @return a map from annotation type to synthesized annotation instance
     */
    Map<Class<? extends Annotation>, SynthesizedAnnotation> getAllSynthesizedAnnotation();

    /**
     * Returns the synthesized annotation of the given type.
     *
     * @param annotationType the annotation type
     * @param <T>            the annotation type
     * @return the synthesized annotation instance
     */
    <T extends Annotation> T synthesize(Class<T> annotationType);

}
