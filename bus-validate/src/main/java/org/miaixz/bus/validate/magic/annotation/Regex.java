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

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.validate.Builder;
import org.miaixz.bus.validate.metric.RegexMatcher;

import java.lang.annotation.*;

/**
 * Validates that the annotated string matches a given regular expression.
 *
 * <p>
 * By default, if the object to be validated is null, the validation passes.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.ANNOTATION_TYPE, ElementType.METHOD, ElementType.PARAMETER, ElementType.FIELD })
@Complex(value = Builder._REGEX, clazz = RegexMatcher.class)
public @interface Regex {

    /**
     * The regular expression pattern to match against.
     *
     * @return the regular expression pattern.
     */
    @Filler("pattern")
    String pattern() default Normal.EMPTY;

    /**
     * Specifies whether a string of zero length is permissible. Defaults to {@code false}. If {@code true}, a
     * zero-length string will pass validation without being checked against the regex. If {@code false}, a zero-length
     * string is still subject to the regex validation.
     *
     * @return {@code true} if a zero-length string is allowed, {@code false} otherwise.
     */
    boolean zeroAble() default false;

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
    String errmsg() default "Validation failed for ${field}, please check the data format";

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
