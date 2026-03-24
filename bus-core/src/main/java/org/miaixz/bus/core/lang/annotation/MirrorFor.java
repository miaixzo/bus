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

/**
 * Sub-annotation of {@link Link}. Indicates that the annotation attribute and the specified attribute are mirrors of
 * each other, allowing the value of one to be obtained through the other.
 * <p>
 * They follow these rules:
 * <ul>
 * <li>Two attributes that are mirrors of each other must both specify the other via a {@link Link} annotation with type
 * {@code MIRROR_FOR}.</li>
 * <li>Two mirrored attributes must have the same type.</li>
 * <li>When getting the value and both values differ, exactly one must be non-default; that value is returned
 * first.</li>
 * <li>When both values are default or both are non-default, the two values must be equal.</li>
 * </ul>
 * <b>Note: when used together with {@link Link}, {@link ForceAliasFor}, or {@link AliasFor}, only the annotation
 * declared topmost will take effect.</b>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Documented
@Link(type = RelationType.MIRROR_FOR)
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.ANNOTATION_TYPE })
public @interface MirrorFor {

    /**
     * The annotation type to link. When not specified, defaults to the annotation class where the attribute is
     * declared.
     *
     * @return The annotation type
     */
    @Link(annotation = Link.class, attribute = "annotation", type = RelationType.FORCE_ALIAS_FOR)
    Class<? extends Annotation> annotation() default Annotation.class;

    /**
     * The associated attribute in the annotation specified by {@link #annotation()}.
     *
     * @return The attribute name
     */
    @Link(annotation = Link.class, attribute = "attribute", type = RelationType.FORCE_ALIAS_FOR)
    String attribute() default Normal.EMPTY;

}
