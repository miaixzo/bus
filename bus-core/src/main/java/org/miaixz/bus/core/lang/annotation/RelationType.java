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

import org.miaixz.bus.core.lang.annotation.resolve.synthesize.SynthesizedAggregateAnnotation;

/**
 * The relationship type of annotation attributes.
 * <p>
 * If the attribute annotated with {@link Link} is called the “original attribute”, and the annotation attribute pointed
 * to in the {@link Link} annotation is called the “linked attribute”, then this enum describes the relationship between
 * the “original attribute” and the “linked attribute” during {@link SynthesizedAggregateAnnotation} processing. The
 * attribute values of annotations synthesized via {@link SynthesizedAggregateAnnotation} will vary based on the
 * relationship type specified in {@link Link#type()}.
 * <p>
 * When an annotation has attributes with multiple types of relationships simultaneously, they are processed in the
 * following order:
 * <ol>
 * <li>The {@link Alias} annotation on the attribute;</li>
 * <li>The {@link Link} annotation with {@link Link#type()} equal to {@link #MIRROR_FOR};</li>
 * <li>The {@link Link} annotation with {@link Link#type()} equal to {@link #FORCE_ALIAS_FOR};</li>
 * <li>The {@link Link} annotation with {@link Link#type()} equal to {@link #ALIAS_FOR};</li>
 * </ol>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public enum RelationType {

    /**
     * Indicates that the annotation attribute and the specified attribute are mirrors of each other, allowing the value
     * of one to be obtained through the other.
     * <p>
     * They follow these rules:
     * <ul>
     * <li>Two mirrored attributes must both specify the other via a {@link Link} annotation with type
     * {@code MIRROR_FOR}.</li>
     * <li>Two mirrored attributes must have the same type.</li>
     * <li>When getting the value and both values differ, exactly one must be non-default; that value is returned
     * first.</li>
     * <li>When both values are default or both are non-default, the two values must be equal.</li>
     * </ul>
     */
    MIRROR_FOR,

    /**
     * Indicates that the “original attribute” serves as an alias for the “linked attribute”.
     * <ul>
     * <li>When the “original attribute” has its default value, getting the “linked attribute” returns the linked
     * attribute's own value.</li>
     * <li>When the “original attribute” has a non-default value, getting the “linked attribute” returns the original
     * attribute's value.</li>
     * </ul>
     */
    ALIAS_FOR,

    /**
     * Indicates that the “original attribute” is forcibly used as an alias for the “linked attribute”. The effect is
     * equivalent to adding an {@link Alias} annotation. In all circumstances, getting the “linked attribute” value
     * always directly returns the “original attribute” value.
     */
    FORCE_ALIAS_FOR

}
