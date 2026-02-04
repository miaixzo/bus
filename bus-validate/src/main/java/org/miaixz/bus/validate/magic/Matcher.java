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
package org.miaixz.bus.validate.magic;

import org.miaixz.bus.core.Provider;
import org.miaixz.bus.core.lang.EnumValue;
import org.miaixz.bus.validate.Context;

/**
 * Validator interface.
 *
 * @param <T> The type of the object to be validated.
 * @param <K> The type of the annotation associated with the validator.
 * @author Kimi Liu
 * @since Java 17+
 */
@FunctionalInterface
public interface Matcher<T, K> extends Provider {

    /**
     * Converts a {@link Validator} to a {@link Matcher}.
     *
     * @param <T>       The generic type of the Validator.
     * @param validator The Validator object.
     * @return A {@code Matcher} object.
     */
    static <T> Matcher<T, ?> of(Validator<T> validator) {
        /**
         * Adapter implementation that wraps a Validator as a Matcher. This implementation ignores the annotation
         * parameter and delegates to the underlying validator.
         */
        return (object, annotation, context) -> validator.on(object, context);
    }

    /**
     * Creates a new validator that is the logical negation of the given validator.
     *
     * @param <T>     The generic type of the object to be validated.
     * @param <K>     The generic type of the validator annotation.
     * @param matcher The validator to negate.
     * @return A new validator whose result is always the opposite of the input validator's result.
     */
    static <T, K> Matcher<T, K> not(Matcher<T, K> matcher) {
        /**
         * Negating matcher implementation that inverts the result of the wrapped matcher. This implementation applies
         * logical NOT to the validation result.
         */
        return (object, anno, context) -> !matcher.on(object, anno, context);
    }

    /**
     * Validates the given object.
     *
     * @param object     The object to be validated.
     * @param annotation The annotation on the object being validated.
     * @param context    The validation context.
     * @return {@code true} if the validation passes, {@code false} otherwise.
     */
    boolean on(T object, K annotation, Context context);

    /**
     * Returns the type of this provider.
     *
     * @return The provider type, which is {@link EnumValue.Povider#VALIDATE}.
     */
    @Override
    default Object type() {
        return EnumValue.Povider.VALIDATE;
    }

}
