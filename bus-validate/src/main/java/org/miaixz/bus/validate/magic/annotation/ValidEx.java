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

import org.miaixz.bus.core.lang.exception.ValidateException;

import java.lang.annotation.*;

/**
 * An annotation for specifying a custom exception to be thrown upon validation failure, replacing the default
 * {@link ValidateException}.
 * <p>
 * This annotation can be used in several places to control exception handling at different levels:
 * <ul>
 * <li><strong>Global Exception:</strong> When used on a parameter of an intercepted method, it defines a global
 * exception for that validation context.</li>
 * <li><strong>Field-Specific Exception:</strong> When marked on a field within an object undergoing deep validation
 * (e.g., with {@code @Inside}), it specifies an exception for that particular field.</li>
 * <li><strong>Validator-Specific Exception:</strong> When used on a validation annotation's definition, it sets the
 * exception for that specific validator.</li>
 * </ul>
 * <strong>Exception Priority:</strong> When a validation fails, the exception to be thrown is determined in the
 * following order of precedence:
 * <ol>
 * <li>The global exception, if defined.</li>
 * <li>The field-specific exception, if defined.</li>
 * <li>The validator-specific exception, if defined on the validation annotation.</li>
 * </ol>
 * If none of the above are defined, a default {@link ValidateException} will be thrown.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.ANNOTATION_TYPE, ElementType.PARAMETER, ElementType.FIELD })
public @interface ValidEx {

    /**
     * The custom exception class to be thrown on validation failure.
     *
     * @return the exception class.
     */
    Class<? extends ValidateException> value() default ValidateException.class;

}
