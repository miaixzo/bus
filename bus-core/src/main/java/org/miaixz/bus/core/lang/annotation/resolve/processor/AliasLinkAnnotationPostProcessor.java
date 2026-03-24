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

import java.util.function.BinaryOperator;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.annotation.Link;
import org.miaixz.bus.core.lang.annotation.RelationType;
import org.miaixz.bus.core.lang.annotation.resolve.attribute.AbstractWrappedAnnotationAttribute;
import org.miaixz.bus.core.lang.annotation.resolve.attribute.AliasedAnnotationAttribute;
import org.miaixz.bus.core.lang.annotation.resolve.attribute.AnnotationAttribute;
import org.miaixz.bus.core.lang.annotation.resolve.attribute.ForceAliasedAnnotationAttribute;
import org.miaixz.bus.core.lang.annotation.resolve.synthesize.AnnotationSynthesizer;
import org.miaixz.bus.core.lang.annotation.resolve.synthesize.SynthesizedAnnotation;
import org.miaixz.bus.core.xyz.ObjectKit;

/**
 * Post-processor for annotation objects whose attributes have the {@link Link} annotation with {@link Link#type()} set
 * to {@link RelationType#ALIAS_FOR} or {@link RelationType#FORCE_ALIAS_FOR}.
 * <p>
 * When this processor completes, the target annotation attributes pointed to by the {@link Link} annotation will be
 * wrapped and replaced with {@link AliasedAnnotationAttribute} or {@link ForceAliasedAnnotationAttribute}.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class AliasLinkAnnotationPostProcessor extends AbstractLinkAnnotationPostProcessor {

    /**
     * The relation types handled by this processor: {@link RelationType#ALIAS_FOR} and
     * {@link RelationType#FORCE_ALIAS_FOR}.
     */
    private static final RelationType[] PROCESSED_RELATION_TYPES = new RelationType[] { RelationType.ALIAS_FOR,
            RelationType.FORCE_ALIAS_FOR };

    /**
     * Returns the order value for this processor.
     *
     * @return {@code Integer.MIN_VALUE + 2}
     */
    @Override
    public int order() {
        return Integer.MIN_VALUE + 2;
    }

    /**
     * Returns the relation types handled by this processor: {@link RelationType#ALIAS_FOR} and
     * {@link RelationType#FORCE_ALIAS_FOR}.
     *
     * @return an array containing {@link RelationType#ALIAS_FOR} and {@link RelationType#FORCE_ALIAS_FOR}
     */
    @Override
    protected RelationType[] processTypes() {
        return PROCESSED_RELATION_TYPES;
    }

    /**
     * Retrieves the target annotation attribute pointed to by {@link Link}, and wraps it as
     * {@link AliasedAnnotationAttribute} or {@link ForceAliasedAnnotationAttribute} depending on whether
     * {@link Link#type()} is {@link RelationType#ALIAS_FOR} or {@link RelationType#FORCE_ALIAS_FOR}. The wrapped
     * attribute then replaces the original target attribute in the corresponding synthesized annotation.
     *
     * @param synthesizer        the annotation synthesizer
     * @param annotation         the {@link Link} annotation object on {@code originalAttribute}
     * @param originalAnnotation the {@link SynthesizedAnnotation} currently being processed
     * @param originalAttribute  the attribute to be processed on {@code originalAnnotation}
     * @param linkedAnnotation   the linked annotation object pointed to by {@link Link}
     * @param linkedAttribute    the linked attribute in {@code linkedAnnotation} pointed to by {@link Link}; this
     *                           parameter may be {@code null}
     */
    @Override
    protected void processLinkedAttribute(
            final AnnotationSynthesizer synthesizer,
            final Link annotation,
            final SynthesizedAnnotation originalAnnotation,
            final AnnotationAttribute originalAttribute,
            final SynthesizedAnnotation linkedAnnotation,
            final AnnotationAttribute linkedAttribute) {
        // Validate alias relation
        checkAliasRelation(annotation, originalAttribute, linkedAttribute);
        // Handle ALIAS_FOR type relation
        if (RelationType.ALIAS_FOR.equals(annotation.type())) {
            wrappingLinkedAttribute(synthesizer, originalAttribute, linkedAttribute, AliasedAnnotationAttribute::new);
            return;
        }
        // Handle FORCE_ALIAS_FOR type relation
        wrappingLinkedAttribute(synthesizer, originalAttribute, linkedAttribute, ForceAliasedAnnotationAttribute::new);
    }

    /**
     * Wraps the specified annotation attribute. If the attribute is already wrapped, recursively traverses the tree
     * structure rooted at it and wraps all leaf nodes.
     *
     * @param synthesizer       the annotation synthesizer
     * @param originalAttribute the original source attribute
     * @param aliasAttribute    the alias attribute to wrap
     * @param wrapping          the wrapping function to apply
     */
    private void wrappingLinkedAttribute(
            final AnnotationSynthesizer synthesizer,
            final AnnotationAttribute originalAttribute,
            final AnnotationAttribute aliasAttribute,
            final BinaryOperator<AnnotationAttribute> wrapping) {
        // Not a wrapped attribute: wrap directly
        if (!aliasAttribute.isWrapped()) {
            processAttribute(synthesizer, originalAttribute, aliasAttribute, wrapping);
            return;
        }
        // Already wrapped: wrap all leaf nodes of the tree
        final AbstractWrappedAnnotationAttribute wrapper = (AbstractWrappedAnnotationAttribute) aliasAttribute;
        wrapper.getAllLinkedNonWrappedAttributes()
                .forEach(t -> processAttribute(synthesizer, originalAttribute, t, wrapping));
    }

    /**
     * Retrieves the specified annotation attribute and wraps it with an additional layer.
     *
     * @param synthesizer       the annotation synthesizer
     * @param originalAttribute the original source attribute
     * @param target            the target attribute to wrap
     * @param wrapping          the wrapping function to apply
     */
    private void processAttribute(
            final AnnotationSynthesizer synthesizer,
            final AnnotationAttribute originalAttribute,
            final AnnotationAttribute target,
            final BinaryOperator<AnnotationAttribute> wrapping) {
        Optional.ofNullable(target.getAnnotationType()).map(synthesizer::getSynthesizedAnnotation).ifPresent(
                t -> t.replaceAttribute(target.getAttributeName(), old -> wrapping.apply(old, originalAttribute)));
    }

    /**
     * Performs basic validation on the alias relationship.
     *
     * @param annotation        the {@link Link} annotation
     * @param originalAttribute the original attribute
     * @param linkedAttribute   the linked (alias) attribute
     */
    private void checkAliasRelation(
            final Link annotation,
            final AnnotationAttribute originalAttribute,
            final AnnotationAttribute linkedAttribute) {
        checkLinkedAttributeNotNull(originalAttribute, linkedAttribute, annotation);
        checkAttributeType(originalAttribute, linkedAttribute);
        checkCircularDependency(originalAttribute, linkedAttribute);
    }

    /**
     * Checks whether two attributes form a circular alias dependency.
     *
     * @param original the original attribute
     * @param alias    the alias attribute
     */
    private void checkCircularDependency(final AnnotationAttribute original, final AnnotationAttribute alias) {
        checkLinkedSelf(original, alias);
        final Link annotation = getLinkAnnotation(alias, RelationType.ALIAS_FOR, RelationType.FORCE_ALIAS_FOR);
        if (ObjectKit.isNull(annotation)) {
            return;
        }
        final Class<?> aliasAnnotationType = getLinkedAnnotationType(annotation, alias.getAnnotationType());
        if (ObjectKit.notEquals(aliasAnnotationType, original.getAnnotationType())) {
            return;
        }
        Assert.notEquals(
                annotation.attribute(),
                original.getAttributeName(),
                "circular reference between the alias attribute [{}] and the original attribute [{}]",
                alias.getAttribute(),
                original.getAttribute());
    }

}
