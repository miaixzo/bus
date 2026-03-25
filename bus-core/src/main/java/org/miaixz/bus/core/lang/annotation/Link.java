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
package org.miaixz.bus.core.lang.annotation;

import java.lang.annotation.*;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.annotation.resolve.synthesize.SynthesizedAggregateAnnotation;

/**
 * Used within the same annotation, or between attributes of different annotations that have a certain relationship, to
 * indicate that these attributes have a specific association. After obtaining a synthesized annotation via
 * {@link SynthesizedAggregateAnnotation}, the attribute values will be adjusted according to this annotation.
 * <p>
 * This annotation has three sub-annotations: {@link MirrorFor}, {@link ForceAliasFor}, and {@link AliasFor}. Using
 * those sub-annotations is equivalent to using {@link Link} directly. Note that when an attribute has multiple
 * {@link Link} or {@link Link}-based annotations simultaneously, only the one declared topmost will take effect; the
 * rest are ignored.
 *
 * <b>Note: the priority of this annotation is lower than {@link Alias}.</b>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.ANNOTATION_TYPE })
public @interface Link {

    /**
     * The annotation type to link. When not specified, defaults to the annotation class where the attribute is
     * declared.
     *
     * @return The annotation type
     */
    Class<? extends Annotation> annotation() default Annotation.class;

    /**
     * The associated attribute in the annotation specified by {@link #annotation()}.
     *
     * @return The attribute name
     */
    String attribute() default Normal.EMPTY;

    /**
     * The type of relationship between the attribute specified by {@link #attribute()} and the current annotation
     * attribute.
     *
     * @return The relationship type
     */
    RelationType type() default RelationType.MIRROR_FOR;

}
