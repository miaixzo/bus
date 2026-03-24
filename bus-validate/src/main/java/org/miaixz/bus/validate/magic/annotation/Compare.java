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

import org.miaixz.bus.core.lang.EnumValue;
import org.miaixz.bus.validate.Builder;

import java.lang.annotation.*;

/**
 * Annotation for validating by comparing the values of two parameters.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
public @interface Compare {

    /**
     * The comparison condition.
     *
     * @return the comparison condition enum.
     */
    EnumValue.Compare cond() default EnumValue.Compare.EQ;

    /**
     * The name of the parameter to compare with.
     *
     * @return the name of the other parameter.
     */
    String with();

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
    String errmsg() default "The value of ${field} does not meet the comparison rule";

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
