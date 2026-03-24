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

import org.miaixz.bus.validate.Builder;
import org.miaixz.bus.validate.metric.SizeMatcher;

/**
 * Validates the size of a String, array, or collection.
 *
 * <p>
 * By default, if the object to be validated is null, the validation passes.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.ANNOTATION_TYPE, ElementType.METHOD, ElementType.PARAMETER, ElementType.FIELD })
@Complex(value = Builder._SIZE, clazz = SizeMatcher.class)
public @interface Size {

    /**
     * The minimum size (inclusive).
     *
     * @return the minimum allowed size.
     */
    @Filler("min")
    int min() default Integer.MIN_VALUE;

    /**
     * The maximum size (inclusive).
     *
     * @return the maximum allowed size.
     */
    @Filler("max")
    int max() default Integer.MAX_VALUE;

    /**
     * Determines whether a value with a length of 0 is considered valid. Defaults to {@code false}.
     * <ul>
     * <li>{@code true}: indicates that a value with zero length passes validation by default.</li>
     * <li>{@code false}: indicates that a value with zero length is still subject to length validation.</li>
     * </ul>
     *
     * @return {@code true} if zero-length values are allowed, {@code false} otherwise.
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
    String errmsg() default "The size of ${field} must be within the specified range. Min: ${min}, Max: ${max}";

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
