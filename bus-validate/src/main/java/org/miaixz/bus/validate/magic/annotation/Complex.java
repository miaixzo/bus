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
import org.miaixz.bus.validate.magic.Matcher;

import java.lang.annotation.*;

/**
 * A meta-annotation for creating custom validation annotations. By placing this on an annotation definition, you mark
 * it as a validation annotation that can be processed by the validation framework.
 * <p>
 * It links the custom annotation to a specific {@link Matcher} implementation that contains the validation logic.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.ANNOTATION_TYPE })
public @interface Complex {

    /**
     * Specifies the name of the validator to be used. This is used to look up the validator in the
     * {@link org.miaixz.bus.validate.Registry}. If both {@code value} and {@code clazz} are specified, {@code clazz}
     * takes precedence.
     *
     * @return the name of the validator.
     */
    String value() default Normal.EMPTY;

    /**
     * Specifies the {@link Matcher} class that implements the validation logic. This provides a direct, type-safe way
     * to link the annotation to its validator, and it is prioritized over the {@code value} attribute.
     *
     * @return the validator class.
     */
    Class<? extends Matcher> clazz() default Matcher.class;

}
