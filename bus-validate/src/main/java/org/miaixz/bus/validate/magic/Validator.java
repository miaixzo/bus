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
package org.miaixz.bus.validate.magic;

import org.miaixz.bus.validate.Context;

/**
 * Defines a simple validator interface.
 *
 * @param <T> The type of the object to be validated.
 * @author Kimi Liu
 * @since Java 21+
 */
@FunctionalInterface
public interface Validator<T> {

    /**
     * Creates a new validator that is the logical negation of the given validator.
     *
     * @param <T>       The generic type of the object to be validated.
     * @param validator The validator to negate.
     * @return A new validator whose result is always the opposite of the input validator's result.
     */
    static <T> Validator<T> not(Validator<T> validator) {
        /**
         * Negating validator implementation that inverts the result of the wrapped validator. This implementation
         * applies logical NOT to the validation result.
         */
        return (object, context) -> !validator.on(object, context);
    }

    /**
     * Validates the given object.
     *
     * @param object  The object to be validated.
     * @param context The context of the current validation.
     * @return {@code true} if the validation passes, {@code false} otherwise.
     */
    boolean on(T object, Context context);

}
