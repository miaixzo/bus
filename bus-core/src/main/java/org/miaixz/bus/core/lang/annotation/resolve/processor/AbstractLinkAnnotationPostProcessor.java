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
package org.miaixz.bus.core.lang.annotation.resolve.processor;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.annotation.Link;
import org.miaixz.bus.core.lang.annotation.RelationType;
import org.miaixz.bus.core.lang.annotation.resolve.attribute.AnnotationAttribute;
import org.miaixz.bus.core.lang.annotation.resolve.synthesize.AnnotationSynthesizer;
import org.miaixz.bus.core.lang.annotation.resolve.synthesize.SynthesizedAggregateAnnotation;
import org.miaixz.bus.core.lang.annotation.resolve.synthesize.SynthesizedAnnotation;
import org.miaixz.bus.core.xyz.AnnoKit;
import org.miaixz.bus.core.xyz.ArrayKit;
import org.miaixz.bus.core.xyz.ObjectKit;

/**
 * Base implementation of {@link SynthesizedAnnotationPostProcessor}, used for processing attributes annotated with
 * {@link Link} in annotation objects.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public abstract class AbstractLinkAnnotationPostProcessor implements SynthesizedAnnotationPostProcessor {

    /**
     * If an annotation attribute has a {@link Link} annotation, and the {@link Link#type()} exists in
     * {@link #processTypes()}, and the annotation object specified by {@link Link} exists in the current
     * {@link SynthesizedAggregateAnnotation}, then retrieves the synthesized annotation corresponding to the type from
     * the aggregator, along with its specified attribute, and delegates all related data to
     * {@link #processLinkedAttribute} for processing.
     *
     * @param synthesizedAnnotation the synthesized annotation being processed
     * @param synthesizer           the annotation synthesizer
     */
    @Override
    public void process(final SynthesizedAnnotation synthesizedAnnotation, final AnnotationSynthesizer synthesizer) {
        final Map<String, AnnotationAttribute> attributeMap = new HashMap<>(synthesizedAnnotation.getAttributes());
        attributeMap.forEach((originalAttributeName, originalAttribute) -> {
            // Retrieve the Link annotation on the attribute
            final Link link = getLinkAnnotation(originalAttribute, processTypes());
            if (ObjectKit.isNull(link)) {
                return;
            }
            // Retrieve the linked annotation object
            final SynthesizedAnnotation linkedAnnotation = getLinkedAnnotation(
                    link,
                    synthesizer,
                    synthesizedAnnotation.annotationType());
            if (ObjectKit.isNull(linkedAnnotation)) {
                return;
            }
            final AnnotationAttribute linkedAttribute = linkedAnnotation.getAttributes().get(link.attribute());
            // Process the linked attribute
            processLinkedAttribute(
                    synthesizer,
                    link,
                    synthesizedAnnotation,
                    synthesizedAnnotation.getAttributes().get(originalAttributeName),
                    linkedAnnotation,
                    linkedAttribute);
        });
    }

    /**
     * Returns the {@link RelationType} values that this processor handles. Only attributes whose {@link Link#type()} is
     * among the returned values will be processed.
     *
     * @return the supported {@link RelationType} values
     */
    protected abstract RelationType[] processTypes();

    /**
     * Processes the linked synthesized annotation object and its associated attribute.
     *
     * @param synthesizer        the annotation synthesizer
     * @param annotation         the {@link Link} annotation object on {@code originalAttribute}
     * @param originalAnnotation the {@link SynthesizedAnnotation} currently being processed
     * @param originalAttribute  the attribute to be processed on {@code originalAnnotation}
     * @param linkedAnnotation   the linked annotation object pointed to by {@link Link}
     * @param linkedAttribute    the linked attribute in {@code linkedAnnotation} pointed to by {@link Link}; this
     *                           parameter may be {@code null}
     */
    protected abstract void processLinkedAttribute(
            AnnotationSynthesizer synthesizer,
            Link annotation,
            SynthesizedAnnotation originalAnnotation,
            AnnotationAttribute originalAttribute,
            SynthesizedAnnotation linkedAnnotation,
            AnnotationAttribute linkedAttribute);

    /**
     * Retrieves the {@link Link} annotation of the specified relation type from an annotation attribute.
     *
     * @param attribute     the annotation attribute
     * @param relationTypes the relation types to filter by
     * @return the {@link Link} annotation, or {@code null} if not found
     */
    protected Link getLinkAnnotation(final AnnotationAttribute attribute, final RelationType... relationTypes) {
        return Optional.ofNullable(attribute)
                .map(t -> AnnoKit.getSynthesizedAnnotation(attribute.getAttribute(), Link.class))
                .filter(a -> ArrayKit.contains(relationTypes, a.type())).getOrNull();
    }

    /**
     * Retrieves the annotation object specified by {@link Link#annotation()} from the synthesized annotation.
     *
     * @param annotation  the {@link Link} annotation
     * @param synthesizer the annotation synthesizer
     * @param defaultType the default annotation type when {@link Link#annotation()} is {@code Annotation.class}
     * @return the corresponding {@link SynthesizedAnnotation}, or {@code null} if not found
     */
    protected SynthesizedAnnotation getLinkedAnnotation(
            final Link annotation,
            final AnnotationSynthesizer synthesizer,
            final Class<? extends Annotation> defaultType) {
        final Class<?> targetAnnotationType = getLinkedAnnotationType(annotation, defaultType);
        return synthesizer.getSynthesizedAnnotation(targetAnnotationType);
    }

    /**
     * Returns {@code defaultType} if the type from {@link Link#annotation()} is {@code Annotation.class}; otherwise
     * returns the type specified by {@link Link#annotation()}.
     *
     * @param annotation  the {@link Link} annotation
     * @param defaultType the default annotation type
     * @return the resolved annotation type
     */
    protected Class<?> getLinkedAnnotationType(final Link annotation, final Class<?> defaultType) {
        return ObjectKit.equals(annotation.annotation(), Annotation.class) ? defaultType : annotation.annotation();
    }

    /**
     * Validates that the return types of two annotation attributes are consistent.
     *
     * @param original the original attribute
     * @param alias    the alias attribute
     */
    protected void checkAttributeType(final AnnotationAttribute original, final AnnotationAttribute alias) {
        Assert.equals(
                original.getAttributeType(),
                alias.getAttributeType(),
                "return type of the linked attribute [{}] is inconsistent with the original [{}]",
                original.getAttribute(),
                alias.getAttribute());
    }

    /**
     * Checks whether the annotation attribute pointed to by {@link Link} is the attribute itself.
     *
     * @param original the source annotation attribute with the {@link Link} annotation
     * @param linked   the target annotation attribute pointed to by {@link Link}
     */
    protected void checkLinkedSelf(final AnnotationAttribute original, final AnnotationAttribute linked) {
        final boolean linkSelf = (original == linked)
                || ObjectKit.equals(original.getAttribute(), linked.getAttribute());
        Assert.isFalse(linkSelf, "cannot link self [{}]", original.getAttribute());
    }

    /**
     * Checks whether the annotation attribute pointed to by {@link Link} exists.
     *
     * @param original        the source annotation attribute with the {@link Link} annotation
     * @param linkedAttribute the target annotation attribute pointed to by {@link Link}
     * @param annotation      the {@link Link} annotation
     */
    protected void checkLinkedAttributeNotNull(
            final AnnotationAttribute original,
            final AnnotationAttribute linkedAttribute,
            final Link annotation) {
        Assert.notNull(
                linkedAttribute,
                "cannot find linked attribute [{}] of original [{}] in [{}]",
                original.getAttribute(),
                annotation.attribute(),
                getLinkedAnnotationType(annotation, original.getAnnotationType()));
    }

}
