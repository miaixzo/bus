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
 * Sub-annotation of {@link Link}. Indicates that the “original attribute” serves as an alias for the “linked
 * attribute”.
 * <ul>
 * <li>When the “original attribute” has its default value, getting the “linked attribute” returns the linked
 * attribute's own value.</li>
 * <li>When the “original attribute” has a non-default value, getting the “linked attribute” returns the original
 * attribute's value.</li>
 * </ul>
 * <b>Note: when used together with {@link Link}, {@link ForceAliasFor}, or {@link MirrorFor}, only the annotation
 * declared topmost will take effect.</b>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Documented
@Link(type = RelationType.ALIAS_FOR)
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.ANNOTATION_TYPE })
public @interface AliasFor {

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
     * @return The associated attribute name
     */
    @Link(annotation = Link.class, attribute = "attribute", type = RelationType.FORCE_ALIAS_FOR)
    String attribute() default Normal.EMPTY;

}
