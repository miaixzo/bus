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

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.annotation.Link;
import org.miaixz.bus.core.lang.annotation.RelationType;
import org.miaixz.bus.core.lang.annotation.resolve.attribute.AnnotationAttribute;
import org.miaixz.bus.core.lang.annotation.resolve.attribute.MirroredAnnotationAttribute;
import org.miaixz.bus.core.lang.annotation.resolve.synthesize.AnnotationSynthesizer;
import org.miaixz.bus.core.lang.annotation.resolve.synthesize.SynthesizedAnnotation;
import org.miaixz.bus.core.text.CharsBacker;
import org.miaixz.bus.core.xyz.ObjectKit;

/**
 * Post-processor for annotation objects whose attributes have the {@link Link} annotation with {@link Link#type()} set
 * to {@link RelationType#MIRROR_FOR}.
 * <p>
 * When this processor completes, both the original attribute annotated with {@link Link} and the target attribute
 * pointed to by {@link Link} will be wrapped and replaced with {@link MirroredAnnotationAttribute}.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class MirrorLinkAnnotationPostProcessor extends AbstractLinkAnnotationPostProcessor {

    /**
     * The relation types handled by this processor: only {@link RelationType#MIRROR_FOR}.
     */
    private static final RelationType[] PROCESSED_RELATION_TYPES = new RelationType[] { RelationType.MIRROR_FOR };

    /**
     * Returns the order value for this processor.
     *
     * @return {@code Integer.MIN_VALUE + 1}
     */
    @Override
    public int order() {
        return Integer.MIN_VALUE + 1;
    }

    /**
     * Returns the relation types handled by this processor: only {@link RelationType#MIRROR_FOR}.
     *
     * @return an array containing only {@link RelationType#MIRROR_FOR}
     */
    @Override
    protected RelationType[] processTypes() {
        return PROCESSED_RELATION_TYPES;
    }

    /**
     * Wraps the pair of mirrored synthesized annotation attributes as {@link MirroredAnnotationAttribute} objects, and
     * replaces the corresponding {@link AnnotationAttribute} in each synthesized annotation instance.
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

        // Mirror attributes always appear in pairs, so exactly three cases are possible:
        // 1. Neither attribute is mirrored yet: proceed with further processing.
        // 2. Both attributes are already mirrored and point to each other: no further action needed.
        // 3. Exactly one attribute is mirrored but does not point to the current original: throw an exception.
        if (originalAttribute instanceof MirroredAnnotationAttribute
                || linkedAttribute instanceof MirroredAnnotationAttribute) {
            checkMirrored(originalAttribute, linkedAttribute);
            return;
        }

        // Validate the mirror relationship
        checkMirrorRelation(annotation, originalAttribute, linkedAttribute);
        // Wrap the mirror attribute pair and replace the corresponding attributes
        final AnnotationAttribute mirroredOriginalAttribute = new MirroredAnnotationAttribute(originalAttribute,
                linkedAttribute);
        originalAnnotation.setAttribute(originalAttribute.getAttributeName(), mirroredOriginalAttribute);
        final AnnotationAttribute mirroredTargetAttribute = new MirroredAnnotationAttribute(linkedAttribute,
                originalAttribute);
        linkedAnnotation.setAttribute(annotation.attribute(), mirroredTargetAttribute);
    }

    /**
     * Checks whether the mirror relationship is correctly established between two attributes.
     *
     * @param original the original attribute
     * @param mirror   the mirror attribute
     */
    private void checkMirrored(final AnnotationAttribute original, final AnnotationAttribute mirror) {
        final boolean originalAttributeMirrored = original instanceof MirroredAnnotationAttribute;
        final boolean mirrorAttributeMirrored = mirror instanceof MirroredAnnotationAttribute;

        // Validation passes: both are mirrored and each points to the other
        final boolean passed = originalAttributeMirrored && mirrorAttributeMirrored
                && ObjectKit.equals(
                        ((MirroredAnnotationAttribute) original).getLinked(),
                        ((MirroredAnnotationAttribute) mirror).getOriginal());
        if (passed) {
            return;
        }

        // Validation failed: assemble the error message for the exception
        final String errorMsg;
        // The original attribute is already mirrored with a different attribute
        if (originalAttributeMirrored && !mirrorAttributeMirrored) {
            errorMsg = CharsBacker.format(
                    "attribute [{}] cannot mirror for [{}], because it's already mirrored for [{}]",
                    original.getAttribute(),
                    mirror.getAttribute(),
                    ((MirroredAnnotationAttribute) original).getLinked());
        }
        // The mirror attribute is already mirrored with a different attribute
        else if (!originalAttributeMirrored && mirrorAttributeMirrored) {
            errorMsg = CharsBacker.format(
                    "attribute [{}] cannot mirror for [{}], because it's already mirrored for [{}]",
                    mirror.getAttribute(),
                    original.getAttribute(),
                    ((MirroredAnnotationAttribute) mirror).getLinked());
        }
        // Both are mirrored but neither points to the other (theoretically unreachable)
        else {
            errorMsg = CharsBacker.format(
                    "attribute [{}] cannot mirror for [{}], because [{}] already mirrored for [{}] and  [{}] already mirrored for [{}]",
                    mirror.getAttribute(),
                    original.getAttribute(),
                    mirror.getAttribute(),
                    ((MirroredAnnotationAttribute) mirror).getLinked(),
                    original.getAttribute(),
                    ((MirroredAnnotationAttribute) original).getLinked());
        }

        throw new IllegalArgumentException(errorMsg);
    }

    /**
     * Performs basic validation on the mirror relationship.
     *
     * @param annotation the {@link Link} annotation
     * @param original   the original attribute
     * @param mirror     the mirror attribute
     */
    private void checkMirrorRelation(
            final Link annotation,
            final AnnotationAttribute original,
            final AnnotationAttribute mirror) {
        // The mirror attribute must exist
        checkLinkedAttributeNotNull(original, mirror, annotation);
        // The mirror attribute must have the same return type
        checkAttributeType(original, mirror);
        // The mirror attribute must also have a corresponding @Link annotation
        final Link mirrorAttributeAnnotation = getLinkAnnotation(mirror, RelationType.MIRROR_FOR);
        Assert.isTrue(
                ObjectKit.isNotNull(mirrorAttributeAnnotation)
                        && RelationType.MIRROR_FOR.equals(mirrorAttributeAnnotation.type()),
                "mirror attribute [{}] of original attribute [{}] must marked by @Link, and also @LinkType.type() must is [{}]",
                mirror.getAttribute(),
                original.getAttribute(),
                RelationType.MIRROR_FOR);
        checkLinkedSelf(original, mirror);
    }

}
