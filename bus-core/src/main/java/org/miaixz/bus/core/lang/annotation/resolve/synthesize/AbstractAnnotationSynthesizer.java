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
import java.util.*;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.annotation.resolve.processor.SynthesizedAnnotationPostProcessor;
import org.miaixz.bus.core.lang.annotation.resolve.scanner.AnnotationScanner;
import org.miaixz.bus.core.xyz.CollKit;
import org.miaixz.bus.core.xyz.MapKit;

/**
 * Base implementation of {@link AnnotationSynthesizer}.
 *
 * @param <T> the type of the current instance
 * @author Kimi Liu
 * @since Java 21+
 */
public abstract class AbstractAnnotationSynthesizer<T> implements AnnotationSynthesizer {

    /**
     * The original source from which the synthesized annotations originate.
     */
    protected final T source;

    /**
     * Map of all annotation instances, including the root annotation and all its meta-annotations.
     */
    protected final Map<Class<? extends Annotation>, SynthesizedAnnotation> synthesizedAnnotationMap;

    /**
     * Cache of already synthesized (proxied) annotation instances.
     */
    private final Map<Class<? extends Annotation>, Annotation> synthesizedProxyAnnotations;

    /**
     * The synthesized annotation selector used to resolve duplicate annotation types.
     */
    protected final SynthesizedAnnotationSelector annotationSelector;

    /**
     * The collection of post-processors applied to synthesized annotations.
     */
    protected final Collection<SynthesizedAnnotationPostProcessor> postProcessors;

    /**
     * The annotation scanner used to discover annotations.
     */
    protected final AnnotationScanner annotationScanner;

    /**
     * Constructs an annotation synthesizer.
     *
     * @param source                   the original source object from which annotations are gathered
     * @param annotationSelector       the selector for choosing between duplicate annotation types
     * @param annotationPostProcessors the post-processors to apply to synthesized annotations
     * @param annotationScanner        the annotation scanner; must support scanning annotation types
     */
    protected AbstractAnnotationSynthesizer(final T source, final SynthesizedAnnotationSelector annotationSelector,
            final Collection<SynthesizedAnnotationPostProcessor> annotationPostProcessors,
            final AnnotationScanner annotationScanner) {
        Assert.notNull(source, "source must not null");
        Assert.notNull(annotationSelector, "annotationSelector must not null");
        Assert.notNull(annotationPostProcessors, "annotationPostProcessors must not null");
        Assert.notNull(annotationPostProcessors, "annotationScanner must not null");

        this.source = source;
        this.annotationSelector = annotationSelector;
        this.annotationScanner = annotationScanner;
        this.postProcessors = CollKit.view(
                CollKit.sort(
                        annotationPostProcessors,
                        Comparator.comparing(SynthesizedAnnotationPostProcessor::order)));
        this.synthesizedProxyAnnotations = new LinkedHashMap<>();
        this.synthesizedAnnotationMap = MapKit.view(loadAnnotations());
        annotationPostProcessors.forEach(
                processor -> synthesizedAnnotationMap.values()
                        .forEach(synthesized -> processor.process(synthesized, this)));
    }

    /**
     * Loads all synthesized annotations required for this synthesizer.
     *
     * @return a map from annotation type to synthesized annotation instance
     */
    protected abstract Map<Class<? extends Annotation>, SynthesizedAnnotation> loadAnnotations();

    /**
     * Synthesizes the final annotation instance of the given type from the provided synthesized annotation.
     *
     * @param annotationType the annotation type to synthesize
     * @param annotation     the synthesized annotation object
     * @param <A>            the annotation type
     * @return the synthesized annotation instance
     */
    protected abstract <A extends Annotation> A synthesize(Class<A> annotationType, SynthesizedAnnotation annotation);

    /**
     * Returns the original source from which the synthesized annotations originate.
     *
     * @return the original source object
     */
    @Override
    public T getSource() {
        return source;
    }

    /**
     * Returns the annotation selector used to resolve duplicate annotation types.
     *
     * @return the annotation selector
     */
    @Override
    public SynthesizedAnnotationSelector getAnnotationSelector() {
        return annotationSelector;
    }

    /**
     * Returns the collection of post-processors applied to synthesized annotations.
     *
     * @return the annotation post-processors
     */
    @Override
    public Collection<SynthesizedAnnotationPostProcessor> getAnnotationPostProcessors() {
        return postProcessors;
    }

    /**
     * Returns the synthesized annotation of the given type.
     *
     * @param annotationType the annotation type
     * @return the synthesized annotation, or {@code null} if not found
     */
    @Override
    public SynthesizedAnnotation getSynthesizedAnnotation(final Class<?> annotationType) {
        return synthesizedAnnotationMap.get(annotationType);
    }

    /**
     * Returns all synthesized annotations.
     *
     * @return a map from annotation type to synthesized annotation instance
     */
    @Override
    public Map<Class<? extends Annotation>, SynthesizedAnnotation> getAllSynthesizedAnnotation() {
        return synthesizedAnnotationMap;
    }

    /**
     * Returns the synthesized (proxy) annotation of the given type, creating and caching it if necessary.
     *
     * @param annotationType the annotation type
     * @param <A>            the annotation type
     * @return the synthesized annotation proxy instance
     */
    @Override
    public <A extends Annotation> A synthesize(final Class<A> annotationType) {
        A annotation = (A) synthesizedProxyAnnotations.get(annotationType);
        if (Objects.nonNull(annotation)) {
            return annotation;
        }
        synchronized (synthesizedProxyAnnotations) {
            annotation = (A) synthesizedProxyAnnotations.get(annotationType);
            if (Objects.isNull(annotation)) {
                final SynthesizedAnnotation synthesizedAnnotation = synthesizedAnnotationMap.get(annotationType);
                annotation = synthesize(annotationType, synthesizedAnnotation);
                synthesizedProxyAnnotations.put(annotationType, annotation);
            }
        }
        return annotation;
    }

}
