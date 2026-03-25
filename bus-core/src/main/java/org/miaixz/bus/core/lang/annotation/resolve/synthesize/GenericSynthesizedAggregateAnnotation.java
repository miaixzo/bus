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
import java.lang.reflect.AnnotatedElement;
import java.util.*;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.annotation.Alias;
import org.miaixz.bus.core.lang.annotation.Link;
import org.miaixz.bus.core.lang.annotation.resolve.attribute.AnnotationAttributeValueProvider;
import org.miaixz.bus.core.lang.annotation.resolve.processor.*;
import org.miaixz.bus.core.lang.annotation.resolve.scanner.AnnotationScanner;
import org.miaixz.bus.core.lang.annotation.resolve.scanner.MetaAnnotationScanner;
import org.miaixz.bus.core.xyz.AnnoKit;
import org.miaixz.bus.core.xyz.ObjectKit;

/**
 * Basic implementation of {@link SynthesizedAggregateAnnotation}, representing an aggregate annotation built from
 * multiple annotation objects, or from multiple root annotation objects and all annotations in their meta-annotation
 * hierarchies.
 *
 * <p>
 * Suppose annotation A exists. If the specified {@link #annotationScanner} supports scanning meta-annotations of
 * annotation A, and A has meta-annotation B, and B has meta-annotation C, then parsing annotation A produces a
 * {@link GenericSynthesizedAggregateAnnotation} that includes root annotation A and its meta-annotations B and C. From
 * the {@link AnnotatedElement} perspective, the resulting synthesized annotation is an annotated element bearing all
 * three annotation objects A, B, and C simultaneously; calling the relevant {@link AnnotatedElement} methods will
 * return the corresponding semantically matching annotation objects.
 *
 * <p>
 * When scanning the root annotation and its meta-annotations, if annotations of the same type appear at different
 * hierarchy levels, the most appropriate one is selected using the {@link SynthesizedAnnotationSelector} specified at
 * construction time. After scanning, each annotation type will appear exactly once in the aggregate. By default,
 * {@link SynthesizedAnnotationSelector#NEAREST_AND_OLDEST_PRIORITY} is used, so only the annotation closest to the root
 * annotation is retained when duplicates are encountered.
 *
 * <p>
 * After the scanned annotations are processed by {@link SynthesizedAnnotationSelector}, they are converted to
 * {@link MetaAnnotation} instances and post-processed using the specified {@link SynthesizedAnnotationPostProcessor}.
 * By default, the following post-processors are registered to provide support for {@link Alias} and {@link Link} and
 * their extended annotations:
 * <ul>
 * <li>{@link AliasAnnotationPostProcessor};</li>
 * <li>{@link MirrorLinkAnnotationPostProcessor};</li>
 * <li>{@link AliasLinkAnnotationPostProcessor};</li>
 * </ul>
 * If custom extension is required, these three processors must be injected into the instance correctly.
 *
 * <p>
 * {@link GenericSynthesizedAggregateAnnotation} supports retrieving annotation attribute values via
 * {@link #getAttributeValue(String, Class)}, or by obtaining an annotation proxy object via {@link #synthesize(Class)}.
 * The returned attribute values may differ from the originals based on {@link Alias} and {@link Link} annotations on
 * the corresponding original annotation attributes. Attribute value retrieval is processed by
 * {@link SynthesizedAnnotationAttributeProcessor}. By default, {@link CachingAnnotationAttributeProcessor} is
 * registered, which causes meta-annotation attributes with the same name and type as a child annotation's attribute to
 * be overridden, and caches the final attribute values.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class GenericSynthesizedAggregateAnnotation extends AbstractAnnotationSynthesizer<List<Annotation>>
        implements SynthesizedAggregateAnnotation {

    /**
     * The root object of this aggregate annotation.
     */
    private final Object root;

    /**
     * Vertical distance from the root object.
     */
    private final int verticalDistance;

    /**
     * Horizontal distance from the root object.
     */
    private final int horizontalDistance;

    /**
     * The annotation attribute processor used to retrieve attribute values from synthesized annotations.
     */
    private final SynthesizedAnnotationAttributeProcessor attributeProcessor;

    /**
     * Constructs an aggregate annotation for the given root annotations and their meta-annotation hierarchies. When
     * duplicate annotation types are encountered in the hierarchy, the one closest to the root annotation and scanned
     * first is preferred; the same rule applies when retrieving attribute values.
     *
     * @param source the root annotation objects
     */
    public GenericSynthesizedAggregateAnnotation(final Annotation... source) {
        this(Arrays.asList(source), new MetaAnnotationScanner());
    }

    /**
     * Constructs an aggregate annotation for the given root annotations using the specified scanner. If the scanner
     * supports traversing the annotation hierarchy, the annotation closest to the root and scanned first is preferred
     * when duplicates are encountered; the same rule applies for attribute value retrieval.
     *
     * @param source            the root annotation objects
     * @param annotationScanner the annotation scanner; must support scanning annotation types
     */
    public GenericSynthesizedAggregateAnnotation(final List<Annotation> source,
            final AnnotationScanner annotationScanner) {
        this(source, SynthesizedAnnotationSelector.NEAREST_AND_OLDEST_PRIORITY,
                new CachingAnnotationAttributeProcessor(),
                Arrays.asList(
                        SynthesizedAnnotationPostProcessor.ALIAS_ANNOTATION_POST_PROCESSOR,
                        SynthesizedAnnotationPostProcessor.MIRROR_LINK_ANNOTATION_POST_PROCESSOR,
                        SynthesizedAnnotationPostProcessor.ALIAS_LINK_ANNOTATION_POST_PROCESSOR),
                annotationScanner);
    }

    /**
     * Constructs an aggregate annotation for the given root annotations using all specified components.
     *
     * @param source                   the annotation objects to look up
     * @param annotationSelector       the synthesized annotation selector
     * @param attributeProcessor       the annotation attribute processor
     * @param annotationPostProcessors the annotation post-processors
     * @param annotationScanner        the annotation scanner; must support scanning annotation types
     */
    public GenericSynthesizedAggregateAnnotation(final List<Annotation> source,
            final SynthesizedAnnotationSelector annotationSelector,
            final SynthesizedAnnotationAttributeProcessor attributeProcessor,
            final Collection<SynthesizedAnnotationPostProcessor> annotationPostProcessors,
            final AnnotationScanner annotationScanner) {
        this(null, 0, 0, source, annotationSelector, attributeProcessor, annotationPostProcessors, annotationScanner);
    }

    /**
     * Constructs an aggregate annotation for the given root annotations using all specified components, with an
     * explicit root object and position in the hierarchy.
     *
     * @param root                     the root object; if {@code null}, this instance is used as the root
     * @param verticalDistance         the vertical distance from the root object
     * @param horizontalDistance       the horizontal distance from the root object
     * @param source                   the annotation objects to look up
     * @param annotationSelector       the synthesized annotation selector
     * @param attributeProcessor       the annotation attribute processor
     * @param annotationPostProcessors the annotation post-processors
     * @param annotationScanner        the annotation scanner; must support scanning annotation types
     */
    GenericSynthesizedAggregateAnnotation(final Object root, final int verticalDistance, final int horizontalDistance,
            final List<Annotation> source, final SynthesizedAnnotationSelector annotationSelector,
            final SynthesizedAnnotationAttributeProcessor attributeProcessor,
            final Collection<SynthesizedAnnotationPostProcessor> annotationPostProcessors,
            final AnnotationScanner annotationScanner) {
        super(source, annotationSelector, annotationPostProcessors, annotationScanner);
        Assert.notNull(attributeProcessor, "attributeProcessor must not null");

        this.root = ObjectKit.defaultIfNull(root, this);
        this.verticalDistance = verticalDistance;
        this.horizontalDistance = horizontalDistance;
        this.attributeProcessor = attributeProcessor;
    }

    /**
     * Returns the root object of this aggregate annotation.
     *
     * @return the root object
     */
    @Override
    public Object getRoot() {
        return root;
    }

    /**
     * Returns the vertical distance from the root object.
     *
     * @return the vertical distance from the root object
     */
    @Override
    public int getVerticalDistance() {
        return verticalDistance;
    }

    /**
     * Returns the horizontal distance from the root object.
     *
     * @return the horizontal distance from the root object
     */
    @Override
    public int getHorizontalDistance() {
        return horizontalDistance;
    }

    /**
     * Breadth-first scans the meta-annotations of each annotation in {@link #source} and loads them into a map keyed by
     * annotation type.
     *
     * @return a map from annotation type to synthesized annotation, preserving insertion order
     */
    @Override
    protected Map<Class<? extends Annotation>, SynthesizedAnnotation> loadAnnotations() {
        final Map<Class<? extends Annotation>, SynthesizedAnnotation> annotationMap = new LinkedHashMap<>();

        // Root annotations have a vertical distance of 0; meta-annotations start at 1
        for (int i = 0; i < source.size(); i++) {
            final Annotation sourceAnnotation = source.get(i);
            Assert.isFalse(AnnoKit.isSynthesizedAnnotation(sourceAnnotation), "source [{}] has been synthesized");
            annotationMap.put(
                    sourceAnnotation.annotationType(),
                    new MetaAnnotation(sourceAnnotation, sourceAnnotation, 0, i));
            Assert.isTrue(
                    annotationScanner.support(sourceAnnotation.annotationType()),
                    "annotation scanner [{}] cannot support scan [{}]",
                    annotationScanner,
                    sourceAnnotation.annotationType());
            annotationScanner.scan((index, annotation) -> {
                final SynthesizedAnnotation oldAnnotation = annotationMap.get(annotation.annotationType());
                final SynthesizedAnnotation newAnnotation = new MetaAnnotation(sourceAnnotation, annotation, index + 1,
                        annotationMap.size());
                if (ObjectKit.isNull(oldAnnotation)) {
                    annotationMap.put(annotation.annotationType(), newAnnotation);
                } else {
                    annotationMap
                            .put(annotation.annotationType(), annotationSelector.choose(oldAnnotation, newAnnotation));
                }
            }, sourceAnnotation.annotationType(), null);
        }
        return annotationMap;
    }

    /**
     * Returns the annotation attribute processor used to retrieve attribute values from synthesized annotations.
     *
     * @return the annotation attribute processor
     */
    @Override
    public SynthesizedAnnotationAttributeProcessor getAnnotationAttributeProcessor() {
        return this.attributeProcessor;
    }

    /**
     * Retrieves the attribute value of the specified name and type from this aggregate. If an {@link Alias} is present,
     * the value of the aliased attribute is returned instead. When multiple annotations at different levels share an
     * attribute with the same name and type, the attribute from the annotation closest to the root takes precedence.
     *
     * @param attributeName the attribute name
     * @param attributeType the attribute type
     * @return the attribute value
     */
    @Override
    public Object getAttributeValue(final String attributeName, final Class<?> attributeType) {
        return attributeProcessor.getAttributeValue(attributeName, attributeType, synthesizedAnnotationMap.values());
    }

    /**
     * Returns the annotation of the specified type from this aggregate, or {@code null} if not present.
     *
     * @param annotationType the annotation type to retrieve
     * @param <T>            the annotation type
     * @return the annotation object, or {@code null} if not found
     */
    @Override
    public <T extends Annotation> T getAnnotation(final Class<T> annotationType) {
        return Optional.ofNullable(annotationType).map(synthesizedAnnotationMap::get)
                .map(SynthesizedAnnotation::getAnnotation).map(annotationType::cast).orElse(null);
    }

    /**
     * Returns whether the specified annotation type is present in this aggregate.
     *
     * @param annotationType the annotation type to check
     * @return {@code true} if the annotation type is present
     */
    @Override
    public boolean isAnnotationPresent(final Class<? extends Annotation> annotationType) {
        return synthesizedAnnotationMap.containsKey(annotationType);
    }

    /**
     * Returns all annotations contained in this aggregate.
     *
     * @return an array of all annotation objects in this aggregate
     */
    @Override
    public Annotation[] getAnnotations() {
        return synthesizedAnnotationMap.values().stream().map(SynthesizedAnnotation::getAnnotation)
                .toArray(Annotation[]::new);
    }

    /**
     * If the specified annotation type is present in this aggregate, creates and returns a dynamic proxy instance of
     * that annotation type; otherwise returns {@code null}.
     *
     * @param annotationType the annotation type to synthesize
     * @param annotation     the synthesized annotation to wrap
     * @param <T>            the annotation type
     * @return the proxy annotation object, or {@code null} if not found
     * @see SynthesizedAnnotationProxy#create(Class, AnnotationAttributeValueProvider, SynthesizedAnnotation)
     */
    @Override
    public <T extends Annotation> T synthesize(final Class<T> annotationType, final SynthesizedAnnotation annotation) {
        return SynthesizedAnnotationProxy.create(annotationType, this, annotation);
    }

    /**
     * Wrapper for an annotation object from the source list or from its meta-annotation hierarchy, used to represent
     * all related annotations in the hierarchical structure of {@link #source}.
     */
    public static class MetaAnnotation extends GenericSynthesizedAnnotation<Annotation, Annotation> {

        /**
         * Constructs a new {@code MetaAnnotation}.
         *
         * @param root               the root annotation object
         * @param annotation         the annotation to wrap
         * @param verticalDistance   the vertical distance from the root annotation
         * @param horizontalDistance the horizontal distance from the root annotation
         */
        protected MetaAnnotation(final Annotation root, final Annotation annotation, final int verticalDistance,
                final int horizontalDistance) {
            super(root, annotation, verticalDistance, horizontalDistance);
        }

    }

}
