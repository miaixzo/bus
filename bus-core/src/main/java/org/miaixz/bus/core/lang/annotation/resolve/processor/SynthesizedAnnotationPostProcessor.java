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

import java.util.Comparator;

import org.miaixz.bus.core.lang.annotation.Alias;
import org.miaixz.bus.core.lang.annotation.Link;
import org.miaixz.bus.core.lang.annotation.resolve.synthesize.AnnotationSynthesizer;
import org.miaixz.bus.core.lang.annotation.resolve.synthesize.SynthesizedAggregateAnnotation;
import org.miaixz.bus.core.lang.annotation.resolve.synthesize.SynthesizedAnnotation;
import org.miaixz.bus.core.xyz.CompareKit;

/**
 * Post-processor for synthesized annotations. Used to further process the loaded {@link SynthesizedAnnotation} objects
 * after {@link SynthesizedAggregateAnnotation} has loaded all annotations to be synthesized.
 * <p>
 * When multiple {@link SynthesizedAnnotationPostProcessor} instances are executed together, they are sorted by the
 * return value of {@link #order()}, with smaller values executed first.
 * <p>
 * This interface has multiple implementations. Callers must ensure that the invocation order always follows:
 * <ul>
 * <li>{@link AliasAnnotationPostProcessor};</li>
 * <li>{@link MirrorLinkAnnotationPostProcessor};</li>
 * <li>{@link AliasLinkAnnotationPostProcessor};</li>
 * <li>Other post-processors;</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface SynthesizedAnnotationPostProcessor extends Comparable<SynthesizedAnnotationPostProcessor> {

    /**
     * Post-processor for annotation objects whose attributes have {@link Alias}.
     */
    AliasAnnotationPostProcessor ALIAS_ANNOTATION_POST_PROCESSOR = new AliasAnnotationPostProcessor();

    /**
     * Post-processor for annotation objects whose attributes have {@link Link} with mirror relationships to other
     * annotation attributes.
     */
    MirrorLinkAnnotationPostProcessor MIRROR_LINK_ANNOTATION_POST_PROCESSOR = new MirrorLinkAnnotationPostProcessor();

    /**
     * Post-processor for annotation objects whose attributes have {@link Link} with alias relationships to other
     * annotation attributes.
     */
    AliasLinkAnnotationPostProcessor ALIAS_LINK_ANNOTATION_POST_PROCESSOR = new AliasLinkAnnotationPostProcessor();

    /**
     * Returns the order in which this post-processor is called within a group. Smaller values are executed first.
     *
     * @return the order value; defaults to {@link Integer#MAX_VALUE}
     */
    default int order() {
        return Integer.MAX_VALUE;
    }

    /**
     * Compares this post-processor with another by their {@link #order()} values.
     *
     * @param o the other post-processor to compare with
     * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than
     *         the specified object
     */
    @Override
    default int compareTo(final SynthesizedAnnotationPostProcessor o) {
        return CompareKit.compare(this, o, Comparator.comparing(SynthesizedAnnotationPostProcessor::order));
    }

    /**
     * Processes the given synthesized annotation using the provided annotation synthesizer.
     *
     * @param synthesizedAnnotation the synthesized annotation to process
     * @param synthesizer           the annotation synthesizer
     */
    void process(SynthesizedAnnotation synthesizedAnnotation, AnnotationSynthesizer synthesizer);

}
