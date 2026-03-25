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
package org.miaixz.bus.validate.magic.annotation;

import java.lang.annotation.*;

/**
 * A marker annotation that indicates a class, method, or parameter should be intercepted for validation.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD, ElementType.PARAMETER, ElementType.FIELD })
public @interface Valid {

    /**
     * Specifies the properties to be validated. For example: `{"id", "name"}`. If empty, all properties are considered
     * for validation unless explicitly skipped.
     *
     * @return an array of property names to validate.
     */
    String[] value() default {};

    /**
     * Specifies the properties to be ignored during validation. For example: `{"created", "creator"}`.
     *
     * @return an array of property names to skip.
     */
    String[] skip() default {};

    /**
     * Specifies whether to perform a deep validation on the object's properties. If {@code true}, the validator will
     * recursively validate the fields of the annotated object.
     *
     * @return {@code true} for deep validation, {@code false} otherwise.
     */
    boolean inside() default true;

}
