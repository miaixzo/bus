/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.validate.magic.annotation;

import org.miaixz.bus.validate.Builder;
import org.miaixz.bus.validate.metric.EmailMatcher;

import java.lang.annotation.*;

/**
 * Validates that the annotated string is a valid email address.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.ANNOTATION_TYPE, ElementType.METHOD, ElementType.PARAMETER, ElementType.FIELD })
@Complex(value = Builder._EMAIL, clazz = EmailMatcher.class)
public @interface Email {

    /**
     * The error code to be used when validation fails.
     *
     * @return the error code.
     */
    String errcode() default Builder.DEFAULT_ERRCODE;

    /**
     * The error message to be used when validation fails. The message can be a template with placeholders.
     *
     * @return the error message.
     */
    String errmsg() default "${field} is not a valid email format";

    /**
     * The validation groups this constraint belongs to.
     *
     * @return an array of group names.
     */
    String[] group() default {};

    /**
     * The name of the field being validated.
     *
     * @return the field name.
     */
    String field() default Builder.DEFAULT_FIELD;

}
